package ca.bc.gov.hlth.pbfdataloader.batch.tasklet;

import java.util.Map;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import ca.bc.gov.hlth.pbfdataloader.service.SFTPService;

/**
 * Tasklet to delete files from SFTP server once the files have been processed.
 */
public class DeleteFilesTasklet extends BaseTasklet implements Tasklet{
	
	@Autowired
	private SFTPService sftpService;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

		Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();
		if (tpcpyFileExists(chunkContext)) {
			// Use the original fileName on the SFTP server for the actual deletion
			String tpcpyFile = (String)jobParameters.get("tpcpyFile"); 
			sftpService.removeFile(tpcpyFile);
		}
		if (tpcprtFileFileExists(chunkContext)) {
			// Use the original fileName on the SFTP server for the actual deletion
			String tpcprtFile = (String)jobParameters.get("tpcprtFile");
			sftpService.removeFile(tpcprtFile);	
		}
		
		return RepeatStatus.FINISHED;
	}

}
