package ca.bc.gov.hlth.pbfdataloader.batch.tasklet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class DeleteFilesTasklet implements Tasklet {
	private static final Logger logger = LoggerFactory.getLogger(DeleteFilesTasklet.class);

	private List<String> files = new ArrayList<>();

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		files.forEach(file -> {
			try {
				Files.deleteIfExists(Paths.get(file));
			} catch (IOException e) {
				logger.error("Could not delete file {}. Please delete manually.", file);
			}
		});
		return RepeatStatus.FINISHED;
	}

	public List<String> getFiles() {
		return files;
	}

	public void setFiles(List<String> files) {
		this.files = files;
	}

}
