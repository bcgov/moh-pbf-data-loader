package ca.bc.gov.hlth.pbfdataloader.batch;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
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
	
	private static final String DATE_FORMAT = "yyyyMMdd";
	
	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private Job importJob;
	
	@Value("${sftp.file.tpcprt}")
	private String tpcprtFile;
	
	@Value("${sftp.file.tpcpy}")
	private String tpcpyFile;

	@Scheduled(cron = "${batch.cron}")
	public void schedule() throws Exception {
		logger.info("Running job");
		
		// Build the inputFile names dynamically
		String date = new SimpleDateFormat(DATE_FORMAT).format(new Date());
		String tpcprtFileWithDate = StringUtils.replace(tpcprtFile, DATE_FORMAT, date);
		String tpcpyFileWithDate = StringUtils.replace(tpcpyFile, DATE_FORMAT, date);
		
		JobParameters params = new JobParametersBuilder()
				.addString("tpcprtFile", tpcprtFileWithDate)
				.addString("tpcpyFile", tpcpyFileWithDate)
				.addDate("date", new Date())
				.toJobParameters();
		JobExecution execution = jobLauncher.run(importJob, params);
		logger.info("Job finished with status " + execution.getStatus());
	}
	
}
