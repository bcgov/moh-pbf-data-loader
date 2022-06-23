package ca.bc.gov.hlth.pbfdataloader.batch.tasklet;

import java.io.File;

public abstract class PurgeTasklet {
	
	private String inputFile;

	public String getInputFile() {
		return inputFile;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}
	
	protected Boolean fileExists() {
		return new File(inputFile).exists();
	}

}
