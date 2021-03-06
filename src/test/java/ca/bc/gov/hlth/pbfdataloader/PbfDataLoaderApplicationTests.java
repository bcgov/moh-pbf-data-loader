package ca.bc.gov.hlth.pbfdataloader;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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

import ca.bc.gov.hlth.pbfdataloader.persistence.repository.PBFClinicPayeeRepository;
import ca.bc.gov.hlth.pbfdataloader.persistence.repository.PatientRegisterRepository;

@SpringBootTest
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts={"classpath:scripts/insert_pbf_clinic_payee.sql",
		"classpath:scripts/insert_patient_register.sql"})
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts={ "classpath:scripts/delete_pbf_clinic_payee.sql",
		"classpath:scripts/delete_patient_register.sql" })
class PbfDataLoaderApplicationTests {

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;
	
	@Autowired
	private PatientRegisterRepository patientRegisterRepository;
	
	@Autowired
	private PBFClinicPayeeRepository pbfClinicPayeeRepository;

	@Test
	public void testImportJob_success() throws Exception {		
		File tpcprtFile = createTempFile("inputs/MSP_TPCPRT_VW.csv");
		File tpcpyFile = createTempFile("inputs/MSP_TPCPY_VW.csv");
		
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

		// Check the record count
		Assertions.assertEquals(43, pbfClinicPayeeRepository.count());
		Assertions.assertEquals(346, patientRegisterRepository.count());
		
		// Validate that the files are deleted
		Assertions.assertFalse(tpcprtFile.exists());
		Assertions.assertFalse(tpcpyFile.exists());
		
		// Check job status
		Assertions.assertEquals(jobExecution.getJobInstance().getJobName(), "importJob");
		Assertions.assertEquals(ExitStatus.COMPLETED, exitStatus);
	}
	
	@Test
	public void testImportJob_failedRecord() throws Exception {		
		File tpcprtFile = createTempFile("inputs/MSP_TPCPRT_VW_one_invalid.csv");
		File tpcpyFile = createTempFile("inputs/MSP_TPCPY_VW_one_invalid.csv");
		
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

		// Check the record count. One record failed for each
		Assertions.assertEquals(42, pbfClinicPayeeRepository.count());
		Assertions.assertEquals(345, patientRegisterRepository.count());
		
		// Validate that the files are deleted
		Assertions.assertFalse(tpcprtFile.exists());
		Assertions.assertFalse(tpcpyFile.exists());
		
		// Check job status
		Assertions.assertEquals(jobExecution.getJobInstance().getJobName(), "importJob");
		Assertions.assertEquals(ExitStatus.COMPLETED, exitStatus);
	}
	
	@Test
	public void testImportJob_pbfClientPayeeFailedJob() throws Exception {		
		File tpcprtFile = createTempFile("inputs/MSP_TPCPRT_VW.csv");
		File tpcpyFile = createTempFile("inputs/MSP_TPCPY_VW_ten_invalid.csv");
		
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
		
		// Validate that the files aren't deleted
		Assertions.assertTrue(tpcprtFile.exists());
		Assertions.assertTrue(tpcpyFile.exists());
		
		// Check job status
		Assertions.assertEquals(jobExecution.getJobInstance().getJobName(), "importJob");
		Assertions.assertEquals(ExitStatus.FAILED.getExitCode(), exitStatus.getExitCode());
	}
	
	@Test
	public void testImportJob_pbfPayeeRegisterFailedJob() throws Exception {		
		File tpcprtFile = createTempFile("inputs/MSP_TPCPRT_VW_ten_invalid.csv");
		File tpcpyFile = createTempFile("inputs/MSP_TPCPY_VW.csv");
		
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
		
		// Validate that the files aren't deleted
		Assertions.assertTrue(tpcprtFile.exists());
		Assertions.assertTrue(tpcpyFile.exists());
		
		// Check job status
		Assertions.assertEquals(jobExecution.getJobInstance().getJobName(), "importJob");
		Assertions.assertEquals(ExitStatus.FAILED.getExitCode(), exitStatus.getExitCode());
	}
	
	private static File createTempFile(String inputFileName) throws IOException {
		Resource inputResource = new ClassPathResource(inputFileName);
		File inputFile = inputResource.getFile();

		// Create a temp file so that we can delete it in the test
		File tempFile = File.createTempFile(FilenameUtils.getBaseName(inputFile.getName()), "." + FilenameUtils.getExtension(inputFile.getName()));

		FileUtils.copyFile(inputFile, tempFile);
		
		return tempFile;
	}
	
    private JobParameters defaultJobParameters(File tpcpyFile, File tpcprtFile) {
		return new JobParametersBuilder()
				.addString("tpcpyFile", tpcpyFile.getPath())
				.addString("tpcprtFile", tpcprtFile.getPath())
				.addDate("date", new Date())
				.toJobParameters();
	}
}
