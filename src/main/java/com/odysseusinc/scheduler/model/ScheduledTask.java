package com.odysseusinc.scheduler.model;

public abstract class ScheduledTask<T extends ArachneJob> implements Runnable {
    protected final T job;

    protected ScheduledTask(T job) {

        this.job = job;
    }

    public T getJob() {

        return job;
    }
}
