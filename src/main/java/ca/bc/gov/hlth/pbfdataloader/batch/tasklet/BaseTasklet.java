package ca.bc.gov.hlth.pbfdataloader.batch.tasklet;

import java.io.File;

import org.springframework.batch.core.scope.context.ChunkContext;

public class BaseTasklet {
	
	protected boolean tpcpyFileExists(ChunkContext chunkContext) {
		File tpcpyFile = (File)chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().get("tpcpyTempFile");
		return tpcpyFile != null && tpcpyFile.exists();
		
	}
	
	protected boolean tpcprtFileFileExists(ChunkContext chunkContext) {
		File tpcprtFile = (File)chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().get("tpcprtTempFile");
		return tpcprtFile != null && tpcprtFile.exists();
	}

}
