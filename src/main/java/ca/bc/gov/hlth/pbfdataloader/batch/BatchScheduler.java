package ca.bc.gov.hlth.pbfdataloader.batch;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class BatchScheduler {
	private static final Logger logger = LoggerFactory.getLogger(BatchScheduler.class);
	
	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private Job importJob;
	
	@Value("${file.input.tpcprt}")
	private String tpcprtFile;
	
	@Value("${file.input.tpcpy}")
	private String tpcpyFile;

	@Scheduled(cron = "${batch.cron}")
	public void schedule() throws Exception {
		logger.info("Running job");
		JobParameters params = new JobParametersBuilder()
				.addString("tpcrtFile", tpcprtFile)
				.addString("tpcpyFile", tpcpyFile)
				.addDate("date", new Date())
				.toJobParameters();
		JobExecution execution = jobLauncher.run(importJob, params);
		logger.info("Job finished with status " + execution.getStatus());
	}
	
}
