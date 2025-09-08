package com.odysseusinc.scheduler.model;

public enum JobExecutingType {
    ONCE("ONCE"),
    HOURLY("HOURLY"),
    DAILY("DAILY"),
    WEEKLY("WEEKLY"),
    MONTHLY("MONTHLY"),
    YEARLY("YEARLY");

    private String title;

    JobExecutingType(String title) {

        this.title = title;
    }

    public String getTitle() {

        return title;
    }
}
