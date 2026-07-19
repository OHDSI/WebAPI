package com.odysseusinc.scheduler.api.v1.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.odysseusinc.scheduler.model.JobExecutingType;

import javax.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ArachneJobDTO {
    private Long id;
    private boolean enabled;
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Date startDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Date nextExecution;
    @NotNull
    private JobExecutingType frequency;
    private List<DayOfWeek> weekDays = new ArrayList<>();
    private Date recurringUntilDate;
    private Integer recurringTimes;
    private Boolean isClosed;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public boolean isEnabled() {

        return enabled;
    }

    public void setEnabled(boolean enabled) {

        this.enabled = enabled;
    }

    public Date getStartDate() {

        return startDate;
    }

    public void setStartDate(Date startDate) {

        this.startDate = startDate;
    }

    public JobExecutingType getFrequency() {

        return frequency;
    }

    public void setFrequency(JobExecutingType frequency) {

        this.frequency = frequency;
    }

    public List<DayOfWeek> getWeekDays() {

        return weekDays;
    }

    public void setWeekDays(List<DayOfWeek> weekDays) {

        this.weekDays = weekDays;
    }

    public Date getRecurringUntilDate() {

        return recurringUntilDate;
    }

    public void setRecurringUntilDate(Date recurringUntilDate) {

        this.recurringUntilDate = recurringUntilDate;
    }

    public Integer getRecurringTimes() {

        return recurringTimes;
    }

    public void setRecurringTimes(Integer recurringTimes) {

        this.recurringTimes = recurringTimes;
    }

    public Boolean getClosed() {

        return isClosed;
    }

    public void setClosed(Boolean closed) {

        isClosed = closed;
    }

    public Date getNextExecution() {

        return nextExecution;
    }

    public void setNextExecution(Date nextExecution) {

        this.nextExecution = nextExecution;
    }
}
