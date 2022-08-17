package ca.bc.gov.hlth.pbfdataloader.batch;

import java.io.File;
import java.net.ConnectException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.FlatFileFormatException;
import org.springframework.batch.repeat.CompletionPolicy;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.bc.gov.hlth.pbfdataloader.batch.listener.JobExecutionListener;
import ca.bc.gov.hlth.pbfdataloader.batch.mapper.PBFClinicPayeeFieldSetMapper;
import ca.bc.gov.hlth.pbfdataloader.batch.mapper.PatientRegisterFieldSetMapper;
import ca.bc.gov.hlth.pbfdataloader.batch.processor.PBFClinicPayeeProcessor;
import ca.bc.gov.hlth.pbfdataloader.batch.processor.PatientRegisterProcessor;
import ca.bc.gov.hlth.pbfdataloader.batch.tasklet.ArchiveTasklet;
import ca.bc.gov.hlth.pbfdataloader.batch.tasklet.DeleteFilesTasklet;
import ca.bc.gov.hlth.pbfdataloader.batch.tasklet.PurgeTasklet;
import ca.bc.gov.hlth.pbfdataloader.batch.tasklet.SFTPGetTasklet;
import ca.bc.gov.hlth.pbfdataloader.persistence.entity.PBFClinicPayee;
import ca.bc.gov.hlth.pbfdataloader.persistence.entity.PatientRegister;
import ca.bc.gov.hlth.pbfdataloader.persistence.repository.PBFClinicPayeeRepository;
import ca.bc.gov.hlth.pbfdataloader.persistence.repository.PatientRegisterRepository;
import ca.bc.gov.hlth.pbfdataloader.service.SFTPResource;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(BatchConfiguration.class);
	
	@Autowired
	public JobBuilderFactory jobBuilderFactory;
	
	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	
	@Autowired
	private PBFClinicPayeeRepository pbfClinicPayeeRepository;
	
	@Autowired
	private PatientRegisterRepository patientRegisterRepository;
	
	@Value("${batch.retryLimit}")
	private Integer retryLimit;
	
	@Value("${batch.skipLimit}")
	private Integer skipLimit;
	
	@Bean
	public Job importJob(JobExecutionListener listener, Step sftpGet, Step archive, Step writePBFClinicPayee, Step writeClientRegister, Step purge, Step deleteFiles) {
	    return jobBuilderFactory.get("importJob")
	      .incrementer(new RunIdIncrementer())
	      .listener(listener)
	      .start(sftpGet)
	      .next(archive)
	      .next(writePBFClinicPayee)
	      .next(writeClientRegister)
	      .next(purge)
	      .next(deleteFiles)
	      .build();
	}
	
	@Bean
	public Step sftpGet(Tasklet sftpGetTasklet) {
		logger.info("Building Step 1 - SFTP Get");
        return stepBuilderFactory.get("Step 1 - sftpGet")
                .tasklet(sftpGetTasklet)
                .build();
	}

	@Bean
    public Step archive(Tasklet archiveTasklet) {
	   	logger.info("Building Step 2 - Archive tables");
        return stepBuilderFactory.get("Step 2 - archive")
                .tasklet(archiveTasklet)
                .build();
    }

	@Bean
	public Step writePBFClinicPayee(ItemReader<PBFClinicPayee> reader, ItemWriter<PBFClinicPayee> writer) {
		logger.info("Building Step 3 - Load the PBFClinicPayee data");
		// Load the PBFClinicPayee data
	    return stepBuilderFactory.get("Step 3 - writePBFClinicPayee")
	      .<PBFClinicPayee, PBFClinicPayee> chunk(completionPolicy())
	      .reader(reader)
	      .processor(pbfClientPayeeProcessor())
	      .writer(writer)
	      .faultTolerant()
	      .retryLimit(retryLimit)
	      .retry(ConnectException.class)
	      .skipLimit(skipLimit)
	      .skip(FlatFileFormatException.class)
	      .skip(FlatFileParseException.class)
	      .build();
	}
	
	@Bean
	public Step writeClientRegister(ItemReader<PatientRegister> reader, ItemWriter<PatientRegister> writer) {
		logger.info("Building Step 4 - Load the PatientRegister data");
		// Load the PatientRegister data
	    return stepBuilderFactory.get("Step 4 - writeClientRegister")
	      .<PatientRegister, PatientRegister> chunk(completionPolicy())
	      .reader(reader)
	      .processor(patientRegisterProcessor())
	      .writer(writer)
	      .faultTolerant()
	      .retryLimit(retryLimit)
	      .retry(ConnectException.class)
	      .skipLimit(skipLimit)
	      .skip(FlatFileFormatException.class)
	      .skip(FlatFileParseException.class)
	      .build();
	}

	
	@Bean
    public Step purge(Tasklet purgeTasklet) {
	   	logger.info("Building Step 5 - Purge tables");
	   	// Purge tables
        return stepBuilderFactory.get("Step 5 - purge")
                .tasklet(purgeTasklet)
                .build();
    }
	
	@Bean
	public Step deleteFiles(Tasklet deleteFilesTasklet) {
	   	logger.info("Building Step 6 - Delete files");
	   	// Purge PBFClinicPayee table
        return stepBuilderFactory.get("Step 6 - deleteFiles")
                .tasklet(deleteFilesTasklet)
                .build();
	}

	@StepScope
	@Bean
	public FlatFileItemReader<PBFClinicPayee> pbfClientPayeeReader(@Value("#{jobExecutionContext['tpcpyTempFile']}") File input) {
		
	    return new FlatFileItemReaderBuilder<PBFClinicPayee>().name("tpcpyItemReader")
	      .resource(new SFTPResource(input))
	      .strict(false)
	      .linesToSkip(1)
	      .delimited()
	      .names("PAYENUM", "EFCTVDT", "CNCLDT", "RPTGRP")
	      .fieldSetMapper(new PBFClinicPayeeFieldSetMapper())
	      .build();
	}

	@StepScope
	@Bean
	public FlatFileItemReader<PatientRegister> patientRegisterReader(@Value("#{jobExecutionContext['tpcprtTempFile']}") File input) {
		
	    return new FlatFileItemReaderBuilder<PatientRegister>().name("tpcprtItemReader")
	      .resource(new SFTPResource(input))
	      .strict(false)
	      .linesToSkip(1)
	      .delimited()
	      .names("PHN", "PAYENUM", "RPRCTNR", "EFCTVDT", "CNCLDT", "SPCLND", "RGRSNCD", "DRGRSNCD", "CNCLRSN")
	      .fieldSetMapper(new PatientRegisterFieldSetMapper())
	      .build();
	}

	@Bean
	public RepositoryItemWriter<PBFClinicPayee> pbfClientPayeeWriter() {
		return new RepositoryItemWriterBuilder<PBFClinicPayee>().repository(pbfClinicPayeeRepository).build();
	}
	
	@Bean
	public RepositoryItemWriter<PatientRegister> patientRegisterWriter() {
		return new RepositoryItemWriterBuilder<PatientRegister>().repository(patientRegisterRepository).build();
	}
	
	@Bean
	public PBFClinicPayeeProcessor pbfClientPayeeProcessor() {
	    return new PBFClinicPayeeProcessor();
	}
	
	@Bean
	public PatientRegisterProcessor patientRegisterProcessor() {
	    return new PatientRegisterProcessor();
	}
	
	@StepScope
	@Bean
	public SFTPGetTasklet sftpGetTasklet() {
		return new SFTPGetTasklet();
	}
	
	@StepScope
	@Bean
	public ArchiveTasklet archiveTasklet() {
		return new ArchiveTasklet();
	}
	
	@StepScope
	@Bean
	public PurgeTasklet purgeTasklet() {
		return new PurgeTasklet();
	}

	
	@StepScope
	@Bean
	public DeleteFilesTasklet deleteFilesTasklet() {
		return new DeleteFilesTasklet();
	}
	
	@StepScope
	@Bean
	public CompletionPolicy completionPolicy() {
		// Set an arbitrarily large limit since we don't actually want to chunk
		// up the file since we want the whole file processed or not
		// Alternatively we should be using Tasklets instead of chunks
		// but we lose the ease of file reading/processing/writing
		return new SimpleCompletionPolicy(99999);
	}
	
}
