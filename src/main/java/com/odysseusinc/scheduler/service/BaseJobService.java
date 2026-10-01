package com.odysseusinc.scheduler.service;

import com.odysseusinc.scheduler.exception.JobNotFoundException;
import com.odysseusinc.scheduler.model.ArachneJob;

public interface BaseJobService<T extends ArachneJob> {

    T createJob(T job);

    T updateJob(T job) throws JobNotFoundException;

    void delete(T job);

    void reassignAllJobs();
}
