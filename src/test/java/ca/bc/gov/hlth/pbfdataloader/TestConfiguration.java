package ca.bc.gov.hlth.pbfdataloader;

import org.springframework.batch.core.Job;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
