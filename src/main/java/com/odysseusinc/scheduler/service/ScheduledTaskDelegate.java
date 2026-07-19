package com.odysseusinc.scheduler.service;

import com.odysseusinc.scheduler.exception.JobNotFoundException;
import com.odysseusinc.scheduler.model.ArachneJob;
import com.odysseusinc.scheduler.model.JobExecutingType;
import com.odysseusinc.scheduler.model.ScheduledTask;

import java.util.Date;
import java.util.Objects;

class ScheduledTaskDelegate<T extends ArachneJob> implements Runnable {

    private final ScheduledTask<T> task;
    private final com.odysseusinc.scheduler.service.BaseJobService<T> jobService;

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