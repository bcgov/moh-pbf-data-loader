package ca.bc.gov.hlth.pbfdataloader;

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
import ca.bc.gov.hlth.pbfdataloader.batch.mapper.PayeeFieldSetMapper;
import ca.bc.gov.hlth.pbfdataloader.batch.processor.PBFClinicPayeeProcessor;
import ca.bc.gov.hlth.pbfdataloader.batch.tasklet.PurgePayeeTasklet;
import ca.bc.gov.hlth.pbfdataloader.persistence.entity.PBFClinicPayee;
import ca.bc.gov.hlth.pbfdataloader.persistence.repository.PBFClinicPayeeRepository;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(BatchConfiguration.class);
	
	@Autowired
	public JobBuilderFactory jobBuilderFactory;
	
	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	
	@Autowired
	private PBFClinicPayeeRepository payeeRepository;
	
	@Value("${file.input.tpcrt}")
	private String tpcrtFile;
	
	@Value("${file.input.tpcpy}")
	private String tpcpyFile;
	
	@Bean
	public Job importTpcpyJob(JobCompletionNotificationListener listener, Step step1, Step step2) {
	    return jobBuilderFactory.get("importTpcpyJob")
	      .incrementer(new RunIdIncrementer())
	      .listener(listener)
	      .start(step1)
	      .next(step2)
	      .build();
	}

   @Bean
    public Step step1() {
	   	logger.info("Building Step 1");
        return stepBuilderFactory.get("step1")
                .tasklet(purgePayeeTasklet())
                .build();
    }
	
	@Bean
	public Step step2(ItemWriter<PBFClinicPayee> writer) {
		logger.info("Building Step 2");
	    return stepBuilderFactory.get("step2")
	      .<PBFClinicPayee, PBFClinicPayee> chunk(10)
	      .reader(reader())
	      .processor(processor())
	      .writer(writer)
	      .build();
	}

	@Bean
	public FlatFileItemReader<PBFClinicPayee> reader() {
		
	    return new FlatFileItemReaderBuilder<PBFClinicPayee>().name("tpcpyItemReader")
	      .resource(new FileSystemResource(tpcpyFile))
	      .linesToSkip(1)
	      .delimited()
	      .names("PAYENUM", "EFCTVDT", "CNCLDT", "RPTGRP")
	      .fieldSetMapper(new PayeeFieldSetMapper())
	      .build();
	}
	
	@Bean
	public RepositoryItemWriter<PBFClinicPayee> writer(DataSource dataSource) {
		return new RepositoryItemWriterBuilder<PBFClinicPayee>().repository(payeeRepository).build();
	}
	
	@Bean
	public PBFClinicPayeeProcessor processor() {
	    return new PBFClinicPayeeProcessor();
	}
	
	@StepScope
	@Bean
	public PurgePayeeTasklet purgePayeeTasklet() {
		return new PurgePayeeTasklet();
	}

}
