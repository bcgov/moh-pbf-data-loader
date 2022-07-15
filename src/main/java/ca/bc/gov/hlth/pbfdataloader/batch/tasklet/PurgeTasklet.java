package ca.bc.gov.hlth.pbfdataloader.batch.tasklet;

import java.io.File;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import ca.bc.gov.hlth.pbfdataloader.persistence.repository.PBFClinicPayeeRepository;
import ca.bc.gov.hlth.pbfdataloader.persistence.repository.PatientRegisterRepository;

public class PurgeTasklet implements Tasklet {
	
	@Autowired
	private PatientRegisterRepository patientRegisterRepository;

	@Autowired
	private PBFClinicPayeeRepository pbfClinicPayeeRepository;

	private String tpcprtFile;

	private String tpcpyFile;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		if (new File(tpcpyFile).exists()) {
			pbfClinicPayeeRepository.deleteArchived();
		}
		if (new File(tpcprtFile).exists()) {
			patientRegisterRepository.deleteArchived();
		}
		return RepeatStatus.FINISHED;
	}
	
	public String getTpcprtFile() {
		return tpcprtFile;
	}

	public void setTpcprtFile(String tpcprtFile) {
		this.tpcprtFile = tpcprtFile;
	}

	public String getTpcpyFile() {
		return tpcpyFile;
	}

	public void setTpcpyFile(String tpcpyFile) {
		this.tpcpyFile = tpcpyFile;
	}
}
