package ca.bc.gov.hlth.pbfdataloader.batch;

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
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
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
import ca.bc.gov.hlth.pbfdataloader.batch.tasklet.PurgePBFClinicPayeeTasklet;
import ca.bc.gov.hlth.pbfdataloader.batch.tasklet.PurgeClientRegisterTasklet;
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
	
	@Value("${file.input.tpcrt}")
	private String tpcrtFile;
	
	@Value("${file.input.tpcpy}")
	private String tpcpyFile;
	
	@Bean
	public Job importTpcpyJob(JobCompletionNotificationListener listener, Step step1, Step step2, Step step3, Step step4) {
	    return jobBuilderFactory.get("importTpcpyJob")
	      .incrementer(new RunIdIncrementer())
	      .listener(listener)
	      .start(step1)
	      .next(step2)
	      .next(step3)
	      .next(step4)
	      .build();
	}

	@Bean
    public Step step1() {
	   	logger.info("Building Step 1 - Purge PBFClinicPayee table");
	   	// Purge PBFClinicPayee table
        return stepBuilderFactory.get("step1")
                .tasklet(purgePBFClinicPayeeTasklet())
                .build();
    }
   
   @Bean
   public Step step2() {
	   	logger.info("Building Step 2 - Purge ClientRegister table");
	   	// Purge ClientRegister table
       return stepBuilderFactory.get("step2")
               .tasklet(purgeClientRegisterTasklet())
               .build();
   }
	
	@Bean
	public Step step3(ItemWriter<PBFClinicPayee> writer) {
		logger.info("Building Step 3 - Load the PBFClinicPayee data");
		// Load the PBFClinicPayee data
	    return stepBuilderFactory.get("step3")
	      .<PBFClinicPayee, PBFClinicPayee> chunk(10)
	      .reader(pbfClientPayeeReader())
	      .processor(pbfClientPayeeProcessor())
	      .writer(writer)
	      .build();
	}
	
	@Bean
	public Step step4(ItemWriter<PatientRegister> writer) {
		logger.info("Building Step 4 - Load the PatientRegister data");
		// Load the PatientRegister data
	    return stepBuilderFactory.get("step2")
	      .<PatientRegister, PatientRegister> chunk(10)
	      .reader(patientRegisterReader())
	      .processor(patientRegisterProcessor())
	      .writer(writer)
	      .build();
	}

	@Bean
	public FlatFileItemReader<PBFClinicPayee> pbfClientPayeeReader() {
		
	    return new FlatFileItemReaderBuilder<PBFClinicPayee>().name("tpcpyItemReader")
	      .resource(new FileSystemResource(tpcpyFile))
	      .strict(false)
	      .linesToSkip(1)
	      .delimited()
	      .names("PAYENUM", "EFCTVDT", "CNCLDT", "RPTGRP")
	      .fieldSetMapper(new PBFClinicPayeeFieldSetMapper())
	      .build();
	}

	@Bean
	public FlatFileItemReader<PatientRegister> patientRegisterReader() {
		
	    return new FlatFileItemReaderBuilder<PatientRegister>().name("tpcrtItemReader")
	      .resource(new FileSystemResource(tpcrtFile))
	      .strict(false)
	      .linesToSkip(1)
	      .delimited()
	      .names("PHN","PAYENUM","RPRCTNR","EFCTVDT","CNCLDT","SPCLND","RGRSNCD","DRGRSNCD")
	      //.names("PHN","PAYENUM","RPRCTNR","EFCTVDT","CNCLDT","SPCLND","RGRSNCD","DRGRSNCD", "CNCLRSN")
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
	public PurgePBFClinicPayeeTasklet purgePBFClinicPayeeTasklet() {
		PurgePBFClinicPayeeTasklet tasklet = new PurgePBFClinicPayeeTasklet();
		tasklet.setInputFile(tpcpyFile);
		return tasklet;
	}

	@StepScope
	@Bean
	public PurgeClientRegisterTasklet purgeClientRegisterTasklet() {
		PurgeClientRegisterTasklet tasklet = new PurgeClientRegisterTasklet();
		tasklet.setInputFile(tpcrtFile);
		return tasklet;
	}
	
}
