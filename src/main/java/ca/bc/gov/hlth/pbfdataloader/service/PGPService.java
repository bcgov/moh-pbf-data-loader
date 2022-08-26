package ca.bc.gov.hlth.pbfdataloader.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.pgpainless.sop.SOPImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import sop.DecryptionResult;
import sop.ReadyWithResult;
import sop.SOP;

@Service
public class PGPService {
	private static final Logger logger = LoggerFactory.getLogger(PGPService.class);
	
	@Value("${pgp.key.file}")
	private String keyFile;
	
	public File decrypt(File encryptedFile) {
		SOP sop = new SOPImpl();
		// decrypt a message and verify its signature(s)
		File secretKeyFile = new File(keyFile);
		try (FileInputStream keyIS = new FileInputStream(secretKeyFile); FileInputStream fileIS =  new FileInputStream(encryptedFile)) {
			byte[] secretKey = keyIS.readAllBytes();

			byte[] ciphertext = fileIS.readAllBytes();

			ReadyWithResult<DecryptionResult> readyWithResult = sop.decrypt()
			        .withKey(secretKey)
			        .ciphertext(ciphertext);
		    
		    File decryptedFile = File.createTempFile(FilenameUtils.getBaseName(encryptedFile.getName()), FilenameUtils.getExtension(encryptedFile.getName()));
			readyWithResult.writeTo(new FileOutputStream(decryptedFile));
			return decryptedFile;
		} catch (IOException e) {
			logger.error("Could not decrypt file {}. {}", encryptedFile.getName(), e.getMessage());
			return null;
		}

	}
	
}
