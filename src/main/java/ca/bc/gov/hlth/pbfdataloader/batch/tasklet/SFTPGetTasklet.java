package ca.bc.gov.hlth.pbfdataloader.batch.tasklet;

import java.io.File;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import ca.bc.gov.hlth.pbfdataloader.service.SFTPService;

/**
 * Tasklet to get the files from the SFTP server and store as a local temp file.
 */
public class SFTPGetTasklet implements Tasklet {

	@Autowired
	private SFTPService sftpService;
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		JobParameters jobParameters = chunkContext.getStepContext().getStepExecution().getJobParameters(); 
		String tpcpyFileName = jobParameters.getString("tpcpyFile");
		File tpcpyFile = sftpService.getFile(tpcpyFileName);

		String tpcprtFileName = jobParameters.getString("tpcprtFile");
		File tpcprtFile = sftpService.getFile(tpcprtFileName);

		ExecutionContext executionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
		executionContext.put("tpcpyTempFile", tpcpyFile);
		executionContext.put("tpcprtTempFile", tpcprtFile);

		return RepeatStatus.FINISHED;
	}

}
