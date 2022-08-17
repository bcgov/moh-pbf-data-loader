package ca.bc.gov.hlth.pbfdataloader.service;

import java.io.File;
import java.nio.file.Path;

import org.springframework.core.io.FileSystemResource;

/**
 * Custom resource loaded from an SFTP Server. Based on {@link FileSystemResource}}.
 */
public class SFTPResource extends FileSystemResource {
	
	public SFTPResource(String path) {
		super(path);
	}
	
	public SFTPResource(File file) {
		super(file);
	}
	
	public SFTPResource(Path filePath) {
		super(filePath);
	}

	@Override
	public String getDescription() {
		// Just return the fileName and not the whole path since we are only concerned with the
		// original SFTP file name and not its temporary location on the file system
		return getFile().getName();
	}	

}
