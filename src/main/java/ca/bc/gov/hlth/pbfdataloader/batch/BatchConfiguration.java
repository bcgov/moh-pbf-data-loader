package ca.bc.gov.hlth.pbfdataloader.batch;

import java.net.ConnectException;

import javax.sql.DataSource;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import ca.bc.gov.hlth.pbfdataloader.batch.listener.JobCompletionNotificationListener;
import ca.bc.gov.hlth.pbfdataloader.batch.mapper.PBFClinicPayeeFieldSetMapper;
import ca.bc.gov.hlth.pbfdataloader.batch.mapper.PatientRegisterFieldSetMapper;
import ca.bc.gov.hlth.pbfdataloader.batch.processor.PBFClinicPayeeProcessor;
import ca.bc.gov.hlth.pbfdataloader.batch.processor.PatientRegisterProcessor;
import ca.bc.gov.hlth.pbfdataloader.batch.tasklet.ArchivePBFClinicPayeeTasklet;
import ca.bc.gov.hlth.pbfdataloader.batch.tasklet.ArchivePatientRegisterTasklet;
import ca.bc.gov.hlth.pbfdataloader.batch.tasklet.DeleteFilesTasklet;
import ca.bc.gov.hlth.pbfdataloader.batch.tasklet.PurgeClientRegisterTasklet;
import ca.bc.gov.hlth.pbfdataloader.batch.tasklet.PurgePBFClinicPayeeTasklet;
import ca.bc.gov.hlth.pbfdataloader.persistence.entity.PBFClinicPayee;
import ca.bc.gov.hlth.pbfdataloader.persistence.entity.PatientRegister;
import ca.bc.gov.hlth.pbfdataloader.persistence.repository.PBFClinicPayeeRepository;
import ca.bc.gov.hlth.pbfdataloader.persistence.repository.PatientRegisterRepository;

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

	@Value("${batch.chunkSize}")
	private Integer chunkSize;
	
	@Value("${batch.retryLimit}")
	private Integer retryLimit;
	
	@Value("${batch.skipLimit}")
	private Integer skipLimit;
	
	@Bean
	public Job importJob(JobCompletionNotificationListener listener, Step archivePBFClinicPayee, Step archivePatientRegister, Step purgeClientRegister, Step writePBFClinicPayee, Step writeClientRegister, Step purgePBFClinicPayee, Step deleteFiles) {
	    return jobBuilderFactory.get("importJob")
	      .incrementer(new RunIdIncrementer())
	      .listener(listener)
	      .start(archivePBFClinicPayee)
	      .next(archivePatientRegister)
	      .next(purgeClientRegister)
	      .next(writePBFClinicPayee)
	      .next(writeClientRegister)
	      .next(purgePBFClinicPayee)
	      .next(deleteFiles)
	      .build();
	}

	@Bean
    public Step archivePBFClinicPayee(Tasklet archivePBFClinicPayeeTasklet) {
	   	logger.info("Building Step 1 - Archive PBFClinicPayee table");
	   	// Archive PBFClinicPayee table
        return stepBuilderFactory.get("Step 1 - archivePBFClinicPayee")
                .tasklet(archivePBFClinicPayeeTasklet)
                .build();
    }
	
	@Bean
    public Step archivePatientRegister(Tasklet archivePatientRegisterTasklet) {
	   	logger.info("Building Step 2 - Archive PatientRegister table");
	   	// Archive PBFClinicPayee table
        return stepBuilderFactory.get("Step 2 - archivePatientRegister")
                .tasklet(archivePatientRegisterTasklet)
                .build();
    }
   
   @Bean
   public Step purgeClientRegister(Tasklet purgeClientRegisterTasklet) {
	   	logger.info("Building Step 2 - Purge ClientRegister table");
	   	// Purge ClientRegister table
       return stepBuilderFactory.get("Step 2 - purgeClientRegister")
               .tasklet(purgeClientRegisterTasklet)
               .build();
   }
	
	@Bean
	public Step writePBFClinicPayee(ItemReader<PBFClinicPayee> reader, ItemWriter<PBFClinicPayee> writer) {
		logger.info("Building Step 3 - Load the PBFClinicPayee data");
		// Load the PBFClinicPayee data
	    return stepBuilderFactory.get("Step 3 - writePBFClinicPayee")
	      .<PBFClinicPayee, PBFClinicPayee> chunk(10)
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
	      .<PatientRegister, PatientRegister> chunk(10)
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
    public Step purgePBFClinicPayee(Tasklet purgePBFClinicPayeeTasklet) {
	   	logger.info("Building Step 1 - Purge PBFClinicPayee table");
	   	// Archive PBFClinicPayee table
        return stepBuilderFactory.get("Step 1 - archivePBFClinicPayee")
                .tasklet(purgePBFClinicPayeeTasklet)
                .build();
    }
	
	@Bean
	public Step deleteFiles(Tasklet deleteFilesTasklet) {
	   	logger.info("Building Step 5 - Delete files");
	   	// Purge PBFClinicPayee table
        return stepBuilderFactory.get("deleteFiles")
                .tasklet(deleteFilesTasklet)
                .build();
	}

	@StepScope
	@Bean
	public FlatFileItemReader<PBFClinicPayee> pbfClientPayeeReader(@Value("#{jobParameters['tpcpyFile']}") String input) {
		
	    return new FlatFileItemReaderBuilder<PBFClinicPayee>().name("tpcpyItemReader")
	      .resource(new FileSystemResource(input))
	      .strict(false)
	      .linesToSkip(1)
	      .delimited()
	      .names("PAYENUM", "EFCTVDT", "CNCLDT", "RPTGRP")
	      .fieldSetMapper(new PBFClinicPayeeFieldSetMapper())
	      .build();
	}

	@StepScope
	@Bean
	public FlatFileItemReader<PatientRegister> patientRegisterReader(@Value("#{jobParameters['tpcrtFile']}") String input) {
		
	    return new FlatFileItemReaderBuilder<PatientRegister>().name("tpcrtItemReader")
	      .resource(new FileSystemResource(input))
	      .strict(false)
	      .linesToSkip(1)
	      .delimited()
	      .names("PHN", "PAYENUM", "RPRCTNR", "EFCTVDT", "CNCLDT", "SPCLND", "RGRSNCD", "DRGRSNCD", "CNCLRSN")
	      .fieldSetMapper(new PatientRegisterFieldSetMapper())
	      .build();
	}

	@Bean
	public RepositoryItemWriter<PBFClinicPayee> pbfClientPayeeWriter(DataSource dataSource) {
		return new RepositoryItemWriterBuilder<PBFClinicPayee>().repository(pbfClinicPayeeRepository).build();
	}
	
	@Bean
	public RepositoryItemWriter<PatientRegister> patientRegisterWriter(DataSource dataSource) {
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
	public ArchivePBFClinicPayeeTasklet archivePBFClinicPayeeTasklet(@Value("#{jobParameters['tpcpyFile']}") String input) {
		ArchivePBFClinicPayeeTasklet tasklet = new ArchivePBFClinicPayeeTasklet();
		tasklet.setInputFile(input);
		return tasklet;
	}
	
	@StepScope
	@Bean
	public ArchivePatientRegisterTasklet archivePatientRegisterTasklet(@Value("#{jobParameters['tpcrtFile']}") String input) {
		ArchivePatientRegisterTasklet tasklet = new ArchivePatientRegisterTasklet();
		tasklet.setInputFile(input);
		return tasklet;
	}
	
	@StepScope
	@Bean
	public PurgePBFClinicPayeeTasklet purgePBFClinicPayeeTasklet(@Value("#{jobParameters['tpcpyFile']}") String input) {
		PurgePBFClinicPayeeTasklet tasklet = new PurgePBFClinicPayeeTasklet();
		tasklet.setInputFile(input);
		return tasklet;
	}

	@StepScope
	@Bean
	public PurgeClientRegisterTasklet purgeClientRegisterTasklet(@Value("#{jobParameters['tpcrtFile']}") String input) {
		PurgeClientRegisterTasklet tasklet = new PurgeClientRegisterTasklet();
		tasklet.setInputFile(input);
		return tasklet;
	}
	
	@StepScope
	@Bean
	public DeleteFilesTasklet deleteFilesTasklet(@Value("#{jobParameters['tpcpyFile']}") String tpcpyFile, @Value("#{jobParameters['tpcrtFile']}") String tpcrtFile) {
		DeleteFilesTasklet tasklet = new DeleteFilesTasklet();
		tasklet.getFiles().add(tpcpyFile);
		tasklet.getFiles().add(tpcrtFile);
		return tasklet;
	}
	
}