package ca.bc.gov.hlth.pbfdataloader.batch.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import ca.bc.gov.hlth.pbfdataloader.persistence.repository.PatientRegisterRepository;

public class PurgeClientRegisterTasklet extends PurgeTasklet implements Tasklet {
	
	@Autowired
	private PatientRegisterRepository patientRegisterRepository;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		if (fileExists()) {
			patientRegisterRepository.deleteAll();	
		}		
		return RepeatStatus.FINISHED;
	}

}
