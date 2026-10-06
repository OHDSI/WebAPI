package org.ohdsi.webapi.arachne.scheduler.service;

import org.ohdsi.webapi.arachne.scheduler.exception.JobNotFoundException;
import org.ohdsi.webapi.arachne.scheduler.model.ArachneJob;
import org.ohdsi.webapi.arachne.scheduler.model.JobExecutingType;
import org.ohdsi.webapi.arachne.scheduler.model.ScheduledTask;

import java.util.Date;
import java.util.Objects;

class ScheduledTaskDelegate<T extends ArachneJob> implements Runnable {

    private final ScheduledTask<T> task;
    private final BaseJobService<T> jobService;

    ScheduledTaskDelegate(ScheduledTask<T> task, BaseJobService<T> jobService) {
        this.task = task;
        this.jobService = jobService;
    }

    @Override
    public void run() {
        try {
            task.run();
        } finally {
            T job = task.getJob();
            job.setLastExecutedAt(new Date());
            job.setExecutedTimes(job.getExecutedTimes() + 1);
            if (Objects.equals(JobExecutingType.ONCE, job.getFrequency())) {
                job.setEnabled(false);
            }
            try {
                jobService.updateJob(job);
            } catch (JobNotFoundException ignored) {
            }
        }
    }
}
