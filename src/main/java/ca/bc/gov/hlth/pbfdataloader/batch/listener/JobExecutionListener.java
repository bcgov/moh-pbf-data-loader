package ca.bc.gov.hlth.pbfdataloader.batch.listener;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ca.bc.gov.hlth.pbfdataloader.persistence.repository.PBFClinicPayeeRepository;
import ca.bc.gov.hlth.pbfdataloader.persistence.repository.PatientRegisterRepository;

@Component
public class JobExecutionListener extends JobExecutionListenerSupport {
	private static final Logger logger = LoggerFactory.getLogger(JobExecutionListener.class);
	
	@Autowired
	private PatientRegisterRepository patientRegisterRepository;
	
	@Autowired
	private PBFClinicPayeeRepository pbfClinicPayeeRepository;
	
	@AfterJob
	@Transactional
	public void afterJob(JobExecution jobExecution) {
		
		if (jobExecution.getStatus() == BatchStatus.FAILED) {
			logger.error("Job failed. Rolling back data.");
			
			String tpcpyFile = jobExecution.getJobParameters().getString("tpcpyFile");
			String tpcprtFile = jobExecution.getJobParameters().getString("tpcprtFile");

			// If there is a job failure roll back the data if the file has been processed
			if (new File(tpcpyFile).exists()) {
				pbfClinicPayeeRepository.deleteNew();
				pbfClinicPayeeRepository.restoreArchived();
			}
			if (new File(tpcprtFile).exists()) {
				patientRegisterRepository.deleteNew();
				patientRegisterRepository.restoreArchived();
			}
	    }
	}
	
}
