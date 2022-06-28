package ca.bc.gov.hlth.pbfdataloader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
		File validTpcprtFile = createTempFile("inputs/MSP_TPCPRT_VW.csv");
		File validTpcpyFile = createTempFile("inputs/MSP_TPCPY_VW.csv");
		
		// Validate initial data
		assertEquals(20, pbfClinicPayeeRepository.count());
		assertEquals(100, patientRegisterRepository.count());
		
		// Verify that our inputFile exists and is not empty
		assertTrue(validTpcprtFile.exists());
		assertTrue(FileUtils.sizeOfAsBigInteger(validTpcprtFile).compareTo(BigInteger.ZERO) > 0);
		
		assertTrue(validTpcpyFile.exists());
		assertTrue(FileUtils.sizeOfAsBigInteger(validTpcpyFile).compareTo(BigInteger.ZERO) > 0);
		
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters(validTpcpyFile, validTpcprtFile));
		ExitStatus exitStatus = jobExecution.getExitStatus();
		
		// Validate that the tables are purged
		
		// Check the record count
		assertEquals(43, pbfClinicPayeeRepository.count());
		assertEquals(346, patientRegisterRepository.count());
		
		// Validate that the files are deleted
		assertFalse(validTpcprtFile.exists());
		assertFalse(validTpcpyFile.exists());
		
		// Check job status
		assertEquals(jobExecution.getJobInstance().getJobName(), "importJob");
	    assertEquals(ExitStatus.COMPLETED, exitStatus);
	}
	
	@Test
	public void testImportJob_invalidInput() throws Exception {		
		File invalidTpcprtFile = createTempFile("inputs/MSP_TPCPRT_VW_invalid.csv");
		File invalidTpcpyFile = createTempFile("inputs/MSP_TPCPY_VW_invalid.csv");
		
		// Validate initial data
		assertEquals(20, pbfClinicPayeeRepository.count());
		assertEquals(100, patientRegisterRepository.count());
		
		// Verify that our inputFile exists and is not empty
		assertTrue(invalidTpcprtFile.exists());
		assertTrue(FileUtils.sizeOfAsBigInteger(invalidTpcprtFile).compareTo(BigInteger.ZERO) > 0);
		
		assertTrue(invalidTpcpyFile.exists());
		assertTrue(FileUtils.sizeOfAsBigInteger(invalidTpcpyFile).compareTo(BigInteger.ZERO) > 0);
		
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters(invalidTpcpyFile, invalidTpcprtFile));
		ExitStatus exitStatus = jobExecution.getExitStatus();
		
		// Validate that the tables are purged
		
		// Check the record count
		assertEquals(0, pbfClinicPayeeRepository.count());
		assertEquals(0, patientRegisterRepository.count());
		
		// Validate that the files aren't deleted
		assertTrue(invalidTpcprtFile.exists());
		assertTrue(invalidTpcpyFile.exists());
		
		// Check job status
		assertEquals(jobExecution.getJobInstance().getJobName(), "importJob");
	    assertEquals(ExitStatus.FAILED.getExitCode(), exitStatus.getExitCode());
	}
	
	private static File createTempFile(String inputFileName) throws IOException {
		Resource inputResource = new ClassPathResource(inputFileName);
		File inputFile = inputResource.getFile();

		// Create a temp file so that we can delete it in the test
		File tempFile = File.createTempFile(FilenameUtils.getBaseName(inputFile.getName()), "." + FilenameUtils.getExtension(inputFile.getName()));

		FileUtils.copyFile(inputFile, tempFile);
		
		return tempFile;
	}
	
    private JobParameters defaultJobParameters(File tpcpyFile, File tpcrtFile) throws IOException {
		return new JobParametersBuilder()
				.addString("tpcpyFile", tpcpyFile.getPath())
				.addString("tpcrtFile", tpcrtFile.getPath())
				.addDate("date", new Date())
				.toJobParameters();
	}
}
