package ca.bc.gov.hlth.pbfdataloader.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.lang3.Streams;
import org.pgpainless.sop.SOPImpl;
import org.springframework.beans.factory.annotation.Value;

import sop.DecryptionResult;
import sop.Ready;
import sop.ReadyWithResult;
import sop.SOP;
import sop.exception.SOPGPException.BadData;
import sop.exception.SOPGPException.CertCannotEncrypt;
import sop.exception.SOPGPException.KeyIsProtected;
import sop.exception.SOPGPException.MissingArg;
import sop.exception.SOPGPException.UnsupportedAsymmetricAlgo;

public class PGPService {
	
	@Value("${pgp.key.file}")
	private String keyFile;
	
	public static void decrypt(File file) throws IOException {
		SOP sop = new SOPImpl();
		//byte[] aliceCert = ...; // Alice' certificate
		byte[] bobKey = Files.readAllBytes(Paths.get("c:/pbf/0x1B899C93-sec.asc"));    // Bobs secret key 
		//byte[] bobCert = ...;   // Bobs certificate

		byte[] ciphertext = Files.readAllBytes(file.toPath()); // the encrypted message

		ReadyWithResult<DecryptionResult> readyWithResult = sop.decrypt()
				
		        .withKey(bobKey)
		  //      .verifyWith(aliceCert)
		        .withKeyPassword("pbfdataloader") // if decryption key is protected
		        .ciphertext(ciphertext);
//		File decryptedZip = new File("c:/pbf/helloworld.pgp");
		File decryptedZip = new File("c:/pbf/0x1B899C93-pub.asc.encrypted.txt");
		readyWithResult.writeTo(new FileOutputStream(decryptedZip));		
	}
	
//	public static void decrypt2() {
//		 DecryptionStream decryptionStream = PGPainless.decryptAndOrVerify()
//	                .onInputStream(encryptedInputStream)
//	                .withOptions(new ConsumerOptions()
//	                        .addDecryptionKey(bobSecKeys, secretKeyProtector)
//	                        .addVerificationCert(alicePubKeys)
//	                );
//
//	        Streams.pipeAll(decryptionStream, outputStream);
//	        decryptionStream.close();
//
//	        // Result contains information like signature status etc.
//	        OpenPgpMetadata metadata = decryptionStream.getResult();
//	}
	
	public static void main(String[] args) throws IOException {
		
		SOP sop = new SOPImpl();
		
//		Ready ready = generateKey(sop);
//		
//		extractCertificate(sop, ready.getBytes());

//		String pubKey = "c:/pbf/pgpainless/wes.kubo-pub.asc";
		String pubKey = "c:/pbf/pbf_pgp_pub.txt";
		
//		String secKey = "c:/pbf/pgpainless/wes.kubo-sec.asc";
		String secKey = "c:/pbf/pbf_private.txt";
		
		encryptMessage(sop, pubKey);
		
//		String encryptedFile = "c:/pbf/pgpainless/encrypted.pgp";
		String encryptedFile = "c:/pbf/msp_tpcprt_vw-testing.csv.asc";
		decryptMessage(sop, secKey, encryptedFile);

	}
	
	private static Ready generateKey(SOP sop) throws MissingArg, UnsupportedAsymmetricAlgo, IOException {
		
		// generate key
		Ready ready = sop.generateKey()
		        .userId("Wes Kubo <wes.kubo@cgi.com>")
		        //.withKeyPassword("f00b4r")
		        .generate();
		
		File secretKeyFile = new File("c:/pbf/pgpainless/wes.kubo-sec.asc");
		//secretKeyFile.createNewFile();
		ready.writeTo(new FileOutputStream(secretKeyFile));

		return ready;
		
	}
	
	private static void extractCertificate(SOP sop, byte[] secretKey) throws BadData, IOException {
		// extract certificate
		Ready ready = sop.extractCert().key(secretKey);
		
		File publicKeyFile = new File("c:/pbf/pgpainless/wes.kubo-pub.asc");
		//publicKeyFile.createNewFile();
		ready.writeTo(new FileOutputStream(publicKeyFile));
	}
	
	private static void encryptMessage(SOP sop, String publicKeyPath) throws KeyIsProtected, CertCannotEncrypt, UnsupportedAsymmetricAlgo, BadData, IOException {
		// encrypt and sign a message
		File publicKeyFile = new File(publicKeyPath);

		byte[] publicKey = new FileInputStream(publicKeyFile).readAllBytes();

		byte[] plaintext = "Hello, World!\n".getBytes(); // plaintext

		Ready ready = sop.encrypt()
		        // encrypt for each recipient
		        .withCert(publicKey)
//		        .withCert(aliceCert)
		        // Optionally: Sign the message
//		        .signWith(aliceKey)
//		        .withKeyPassword("sw0rdf1sh") // if signing key is protected
		        // provide the plaintext
		        .plaintext(plaintext);
		
		File encryptedFile = new File("c:/pbf/pgpainless/encrypted.pgp");
		ready.writeTo(new FileOutputStream(encryptedFile));
		
		
		
	}

	private static void decryptMessage(SOP sop, String secretKeyPath, String encryptedFilePath) throws FileNotFoundException, IOException {
		// decrypt a message and verify its signature(s)
		File secretKeyFile = new File(secretKeyPath);
		byte[] secretKey = new FileInputStream(secretKeyFile).readAllBytes();

		File encryptedFile = new File(encryptedFilePath);
		byte[] ciphertext = new FileInputStream(encryptedFile).readAllBytes();

		ReadyWithResult<DecryptionResult> readyWithResult = sop.decrypt()
		        .withKey(secretKey)
//		        .verifyWith(aliceCert)
//		        .withKeyPassword("password123") // if decryption key is protected
		        .ciphertext(ciphertext);
		
		File decryptedFile = new File("c:/pbf/pgpainless/decrypted.pgp");
		readyWithResult.writeTo(new FileOutputStream(decryptedFile));
	}
	
}
