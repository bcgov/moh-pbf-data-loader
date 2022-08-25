package ca.bc.gov.hlth.pbfdataloader.service;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

@Service
public class SFTPService {
	private static final Logger logger = LoggerFactory.getLogger(SFTPService.class);
	
	@Value("${sftp.hostname}")
	private String hostname;
	
	@Value("${sftp.key.username}")
	private String keyUsername;
	
	@Value("${sftp.key.file}")
	private String keyFile;

	public void removeFile(String fileName) {
		try (SSHClient sshClient = setupSshj();
			SFTPClient sftpClient = sshClient.newSFTPClient()) {
		    sftpClient.rm(fileName);
		    logger.info("Deleted file {} from SFTP server.", fileName);
		} catch (IOException e) {
			logger.error("Could not remove file {} from SFTP server. Please delete manually. {}", fileName, e.getMessage());
		}
	}

	public File getFile(String fileName) {
		try (SSHClient sshClient = setupSshj(); SFTPClient sftpClient = sshClient.newSFTPClient()) {
			// All files need to be downloaded to a file to create a temp file and delete it later
		    File tempFile = File.createTempFile(FilenameUtils.getBaseName(fileName), FilenameUtils.getExtension(fileName));
		    
		    sftpClient.get(fileName, tempFile.getAbsolutePath());
		    logger.info("Downloaded file {} from SFTP server to temp file {}.", fileName, tempFile.getAbsoluteFile());
		    return tempFile;
		} catch (IOException e) {
			logger.warn("Could not get file {} from SFTP server. {}", fileName, e.getMessage());
			return null;
		}
	}
	
	private SSHClient setupSshj() throws IOException {
	    SSHClient client = new SSHClient();
	    client.addHostKeyVerifier(new PromiscuousVerifier());
	    client.connect(hostname);
	    client.authPublickey(keyUsername, keyFile);
	    return client;
	}
}
