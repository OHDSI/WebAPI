package org.ohdsi.webapi.arachne.scheduler.service;

import org.ohdsi.webapi.arachne.scheduler.exception.JobNotFoundException;
import org.ohdsi.webapi.arachne.scheduler.model.ArachneJob;

public interface BaseJobService<T extends ArachneJob> {

    T createJob(T job);

    T updateJob(T job) throws JobNotFoundException;

    void delete(T job);

    void reassignAllJobs();
}
