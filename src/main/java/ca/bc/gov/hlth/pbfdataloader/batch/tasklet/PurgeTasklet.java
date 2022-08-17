package ca.bc.gov.hlth.pbfdataloader.batch.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import ca.bc.gov.hlth.pbfdataloader.persistence.repository.PBFClinicPayeeRepository;
import ca.bc.gov.hlth.pbfdataloader.persistence.repository.PatientRegisterRepository;

/**
 * Tasklet to purge archived records once the job has completed.
 */
public class PurgeTasklet extends BaseTasklet implements Tasklet {
	
	@Autowired
	private PatientRegisterRepository patientRegisterRepository;

	@Autowired
	private PBFClinicPayeeRepository pbfClinicPayeeRepository;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		if (tpcpyFileExists(chunkContext)) {
			pbfClinicPayeeRepository.deleteArchived();
		}
		if (tpcprtFileFileExists(chunkContext)) {
			patientRegisterRepository.deleteArchived();
		}
		return RepeatStatus.FINISHED;
	}

}
