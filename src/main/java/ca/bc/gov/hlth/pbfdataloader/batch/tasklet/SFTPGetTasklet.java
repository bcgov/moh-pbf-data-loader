package ca.bc.gov.hlth.pbfdataloader.batch.tasklet;

import java.io.File;
import java.io.IOException;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import ca.bc.gov.hlth.pbfdataloader.service.PGPService;
import ca.bc.gov.hlth.pbfdataloader.service.SFTPService;

/**
 * Tasklet to get the files from the SFTP server and store as a local temp file.
 */
public class SFTPGetTasklet implements Tasklet {

	@Autowired
	private PGPService pgpService;
	
	@Autowired
	private SFTPService sftpService;
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		JobParameters jobParameters = chunkContext.getStepContext().getStepExecution().getJobParameters();
		
		// Get the encrypted files
		String tpcpyFileName = jobParameters.getString("tpcpyFile");
		File encryptedTpcpyFile = sftpService.getFile(tpcpyFileName);

		String tpcprtFileName = jobParameters.getString("tpcprtFile");
		File encryptedTpcprtFile = sftpService.getFile(tpcprtFileName);
		
		// Decrypt the files
		File decryptedTpcpyFile = decrypt(encryptedTpcpyFile);
		File decryptedTpcprtFile = decrypt(encryptedTpcprtFile);

		ExecutionContext executionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();

		// A FileSystemResource cannot be created with a null file (i.e. if the file can't be pulled from the server)
		// But we can create it from a File with a non-existent filePath so that the Reader recognizes it doesn't exist
		// Use the original fileName in this case since files which were successfully downloaded are named according to the temp file
		executionContext.put("tpcpyTempFile", decryptedTpcpyFile != null ? decryptedTpcpyFile : new File(tpcpyFileName));
		executionContext.put("tpcprtTempFile", decryptedTpcprtFile != null ? decryptedTpcprtFile : new File(tpcprtFileName));

		return RepeatStatus.FINISHED;
	}
	
	public File decrypt(File encryptedFile) {
		if (encryptedFile == null) {
			return null;
		}
		File decryptedFile = pgpService.decrypt(encryptedFile);
		// Delete the encrypted temp file as it's no longer required
		encryptedFile.delete();
		
		return decryptedFile;
	}

}
