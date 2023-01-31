package ca.bc.gov.hlth.pbfdataloader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import ca.bc.gov.hlth.pbfdataloader.persistence.entity.PBFClinicPayee;
import ca.bc.gov.hlth.pbfdataloader.persistence.entity.PatientRegister;
import ca.bc.gov.hlth.pbfdataloader.persistence.repository.PBFClinicPayeeRepository;
import ca.bc.gov.hlth.pbfdataloader.persistence.repository.PatientRegisterRepository;
import ca.bc.gov.hlth.pbfdataloader.service.PGPService;
import ca.bc.gov.hlth.pbfdataloader.service.SFTPService;

@SpringBootTest
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts={"classpath:scripts/insert_pbf_clinic_payee.sql",
		"classpath:scripts/insert_patient_register.sql"})
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts={ "classpath:scripts/delete_pbf_clinic_payee.sql",
		"classpath:scripts/delete_patient_register.sql" })
@ExtendWith(MockitoExtension.class)
class PbfDataLoaderApplicationTests {

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;
	
	@Autowired
	private PatientRegisterRepository patientRegisterRepository;
	
	@Autowired
	private PBFClinicPayeeRepository pbfClinicPayeeRepository;
	
	@Autowired
	private SFTPService sftpService;
	
	@Autowired
	private PGPService pgpService;

	@Test
	public void testImportJob_success() throws Exception {		

		File tpcpyFile = new ClassPathResource("inputs/MSP_TPCPY_VW.zip").getFile();
		File tpcprtFile = new ClassPathResource("inputs/MSP_TPCPRT_VW.zip").getFile();

		mockServices(tpcpyFile, tpcprtFile);

		// Validate initial data
		Assertions.assertEquals(20, pbfClinicPayeeRepository.count());
		Assertions.assertEquals(100, patientRegisterRepository.count());
		
		// Verify that our inputFile exists and is not empty
		Assertions.assertTrue(tpcpyFile.exists());
		Assertions.assertTrue(FileUtils.sizeOfAsBigInteger(tpcpyFile).compareTo(BigInteger.ZERO) > 0);

		Assertions.assertTrue(tpcprtFile.exists());
		Assertions.assertTrue(FileUtils.sizeOfAsBigInteger(tpcprtFile).compareTo(BigInteger.ZERO) > 0);
		
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters(tpcpyFile, tpcprtFile));
		ExitStatus exitStatus = jobExecution.getExitStatus();

		// Check the record count
		List<PBFClinicPayee> payees = pbfClinicPayeeRepository.findAll();
		Assertions.assertEquals(43, payees.size());
		List<PatientRegister> patients = patientRegisterRepository.findAll();
		Assertions.assertEquals(50, patients.size());
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		
		// Verify record content
		PBFClinicPayee payee = payees.get(0);
		assertFalse(payee.getArchived());
		assertEquals("2005-06-30", formatter.format(payee.getCancelDate()));
		assertEquals("1999-08-01", formatter.format(payee.getEffectiveDate()));
		assertEquals("A6048", payee.getPayeeNumber());
		assertNotNull(payee.getPbfClinicPayeeId());
		assertEquals("10864", payee.getReportGroup());
		
		PatientRegister patient = patients.get(0);
		assertEquals("0", patient.getAdministrativeCode());
		assertFalse(patient.getArchived());
		assertEquals("2000-12-31", formatter.format(patient.getCancelDate()));
		assertEquals("M", patient.getCancelReasonCode());
		assertEquals("F", patient.getDeregistrationReasonCode());
		assertEquals("1999-09-01", formatter.format(patient.getEffectiveDate()));
		assertNotNull(patient.getPatientRegisterId());
		assertEquals("A6055", patient.getPayeeNumber());
		assertEquals("8715913345", patient.getPhn());
		assertEquals("JOHN", patient.getRegisteredPractitionerFirstName());
		assertEquals("W", patient.getRegisteredPractitionerMiddleName());		
		assertEquals("SMITH", patient.getRegisteredPractitionerSurname());
		assertEquals("X9487", patient.getRegisteredPractitionerNumber());
		assertNull(patient.getRegistrationReasonCode());
		
		// Check job status
		Assertions.assertEquals(jobExecution.getJobInstance().getJobName(), "importJob");
		Assertions.assertEquals(ExitStatus.COMPLETED, exitStatus);
	}
	
	@Test
	public void testImportJob_failedRecord() throws Exception {		

		File tpcpyFile = new ClassPathResource("inputs/MSP_TPCPY_VW_one_invalid.zip").getFile();
		File tpcprtFile = new ClassPathResource("inputs/MSP_TPCPRT_VW_one_invalid.zip").getFile();

		mockServices(tpcpyFile, tpcprtFile);
		
		// Validate initial data
		Assertions.assertEquals(20, pbfClinicPayeeRepository.count());
		Assertions.assertEquals(100, patientRegisterRepository.count());
		
		// Verify that our inputFile exists and is not empty
		Assertions.assertTrue(tpcpyFile.exists());
		Assertions.assertTrue(FileUtils.sizeOfAsBigInteger(tpcpyFile).compareTo(BigInteger.ZERO) > 0);

		Assertions.assertTrue(tpcprtFile.exists());
		Assertions.assertTrue(FileUtils.sizeOfAsBigInteger(tpcprtFile).compareTo(BigInteger.ZERO) > 0);
		
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters(tpcpyFile, tpcprtFile));
		ExitStatus exitStatus = jobExecution.getExitStatus();

		// Check the record count. One record failed for each
		Assertions.assertEquals(42, pbfClinicPayeeRepository.count());
		Assertions.assertEquals(49, patientRegisterRepository.count());
		
		// Check job status
		Assertions.assertEquals(jobExecution.getJobInstance().getJobName(), "importJob");
		Assertions.assertEquals(ExitStatus.COMPLETED, exitStatus);
	}
	
	@Test
	public void testImportJob_pbfClientPayeeFailedJob() throws Exception {		

		File tpcpyFile = new ClassPathResource("inputs/MSP_TPCPY_VW_ten_invalid.zip").getFile();
		File tpcprtFile = new ClassPathResource("inputs/MSP_TPCPRT_VW.zip").getFile();

		mockServices(tpcpyFile, tpcprtFile);
		
		// Validate initial data
		Assertions.assertEquals(20, pbfClinicPayeeRepository.count());
		Assertions.assertEquals(100, patientRegisterRepository.count());
		
		// Verify that our inputFile exists and is not empty
		Assertions.assertTrue(tpcprtFile.exists());
		Assertions.assertTrue(FileUtils.sizeOfAsBigInteger(tpcprtFile).compareTo(BigInteger.ZERO) > 0);

		Assertions.assertTrue(tpcpyFile.exists());
		Assertions.assertTrue(FileUtils.sizeOfAsBigInteger(tpcpyFile).compareTo(BigInteger.ZERO) > 0);
		
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters(tpcpyFile, tpcprtFile));
		ExitStatus exitStatus = jobExecution.getExitStatus();
		
		// Check the record count. Should be rolled back to original data
		Assertions.assertEquals(20, pbfClinicPayeeRepository.count());
		Assertions.assertEquals(100, patientRegisterRepository.count());
		
		// Check job status
		Assertions.assertEquals(jobExecution.getJobInstance().getJobName(), "importJob");
		Assertions.assertEquals(ExitStatus.FAILED.getExitCode(), exitStatus.getExitCode());
	}
	
	@Test
	public void testImportJob_pbfPayeeRegisterFailedJob() throws Exception {		
		File tpcprtFile = createTempFile("inputs/MSP_TPCPRT_VW_ten_invalid.zip");
		File tpcpyFile = createTempFile("inputs/MSP_TPCPY_VW.zip");
		
		mockServices(tpcpyFile, tpcprtFile);
		
		// Validate initial data
		Assertions.assertEquals(20, pbfClinicPayeeRepository.count());
		Assertions.assertEquals(100, patientRegisterRepository.count());
		
		// Verify that our inputFile exists and is not empty
		Assertions.assertTrue(tpcprtFile.exists());
		Assertions.assertTrue(FileUtils.sizeOfAsBigInteger(tpcprtFile).compareTo(BigInteger.ZERO) > 0);

		Assertions.assertTrue(tpcpyFile.exists());
		Assertions.assertTrue(FileUtils.sizeOfAsBigInteger(tpcpyFile).compareTo(BigInteger.ZERO) > 0);
		
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters(tpcpyFile, tpcprtFile));
		ExitStatus exitStatus = jobExecution.getExitStatus();

		// Check the record count. Should be rolled back to original data
		Assertions.assertEquals(20, pbfClinicPayeeRepository.count());
		Assertions.assertEquals(100, patientRegisterRepository.count());
		
		// Check job status
		Assertions.assertEquals(jobExecution.getJobInstance().getJobName(), "importJob");
		Assertions.assertEquals(ExitStatus.FAILED.getExitCode(), exitStatus.getExitCode());
	}
	
	private static File createTempFile(String inputFileName) throws IOException {
		Resource inputResource = new ClassPathResource(inputFileName);
		return createTempFile(inputResource.getFile());
	}
	
	private static File createTempFile(File inputFile) throws IOException {
		File tempFile = File.createTempFile(FilenameUtils.getBaseName(inputFile.getName()), "." + FilenameUtils.getExtension(inputFile.getName()));

		FileUtils.copyFile(inputFile, tempFile);
		
		return tempFile;		
	}
	
	private void mockServices(File tpcpyFile, File tpcprtFile) throws IOException {
		File tpcpyEncryptedFile = createTempFile(tpcpyFile);
		File tpcpyDecryptedFile = createTempFile(tpcpyFile);
		File tpcprtEncryptedFile = createTempFile(tpcprtFile);
		File tpcprtDecryptedFile = createTempFile(tpcprtFile);

		// Make a copy of the input file since it gets deleted after being decrypted
		Mockito.when(sftpService.getFile(tpcpyFile.getAbsolutePath())).thenReturn(tpcpyEncryptedFile);
		Mockito.when(sftpService.getFile(tpcprtFile.getAbsolutePath())).thenReturn(tpcprtEncryptedFile);

		// Return a copy of the file since the encrypted version (in this case the original file) gets deleted
		Mockito.when(pgpService.decrypt(tpcpyEncryptedFile)).thenReturn(tpcpyDecryptedFile);
		Mockito.when(pgpService.decrypt(tpcprtEncryptedFile)).thenReturn(tpcprtDecryptedFile);
	}
	
    private JobParameters defaultJobParameters(File tpcpyFile, File tpcprtFile) {
		return new JobParametersBuilder()
				.addString("tpcpyFile", tpcpyFile.getPath())
				.addString("tpcprtFile", tpcprtFile.getPath())
				.addDate("date", new Date())
				.toJobParameters();
	}
}

