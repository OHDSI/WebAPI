package org.ohdsi.webapi;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class JobInvalidator {

    @Autowired
    JobExplorer jobExplorer;
    @Autowired
    JobRepository jobRepository;

    @PostConstruct
    private void invalidateGenerations(){
        jobExplorer.getJobNames().forEach(
                name -> jobExplorer.findRunningJobExecutions(name)
                        .forEach(job -> {
                            job.setStatus(BatchStatus.FAILED);
                            job.setExitStatus(new ExitStatus(ExitStatus.FAILED.getExitCode(), "Invalidated by system"));
                            jobRepository.update(job);
                        }));
    }
}
