package ca.bc.gov.hlth.pbfdataloader;

import org.mockito.Mockito;
import org.springframework.batch.core.Job;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import ca.bc.gov.hlth.pbfdataloader.service.SFTPService;

@Configuration
public class TestConfiguration {
    @Bean
    public JobLauncherTestUtils getJobLauncherTestUtils() {

        return new JobLauncherTestUtils() {
            @Override
            @Autowired
            public void setJob(@Qualifier("importJob") Job job) {
                super.setJob(job);
            }
        };
    }
    
    @Bean
    @Primary
    public SFTPService sftpService() {
    	return Mockito.mock(SFTPService.class);
    }
}
