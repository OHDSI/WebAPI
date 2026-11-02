package org.ohdsi.webapi.arachne.scheduler.model;

import java.time.DayOfWeek;
import java.util.Date;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;

@MappedSuperclass
public abstract class ArachneJob {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "arachne_job_generator")
    private Long id;

    @NotNull
    @Column(name = "is_enabled")
    private boolean enabled = Boolean.FALSE;

    @NotNull
    @Column(name = "start_date")
    private Date startDate;

    @NotNull
    @Column(name = "frequency")
    @Enumerated(EnumType.STRING)
    private JobExecutingType frequency;

    @Min(0)
    @Column(name = "executed_times")
    private Integer executedTimes = 0;

    @Column(name = "last_executed_at")
    private Date lastExecutedAt;

    @Column(name = "is_closed")
    private Boolean isClosed;

    @Min(0)
    @Column(name = "recurring_times")
    private Integer recurringTimes;

    @Column(name = "recurring_until_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date recurringUntilDate;

    @NotEmpty
    @Column(name = "cron")
    private String cron;

    @Transient
    private Date nextExecution;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public boolean getEnabled() {

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

    @Transient
    public abstract List<DayOfWeek> getWeekDays();

    public abstract void setWeekDays(List<DayOfWeek> weekDays);

    public Integer getExecutedTimes() {

        return executedTimes;
    }

    public void setExecutedTimes(Integer executedTimes) {

        this.executedTimes = executedTimes;
    }

    public Date getLastExecutedAt() {

        return lastExecutedAt;
    }

    public void setLastExecutedAt(Date lastExecutedAt) {

        this.lastExecutedAt = lastExecutedAt;
    }

    public Boolean getClosed() {

        return isClosed;
    }

    public void setClosed(Boolean closed) {

        isClosed = closed;
    }

    public Integer getRecurringTimes() {

        return recurringTimes;
    }

    public void setRecurringTimes(Integer recurringTimes) {

        this.recurringTimes = recurringTimes;
    }

    public Date getRecurringUntilDate() {

        return recurringUntilDate;
    }

    public void setRecurringUntilDate(Date recurringUntilDate) {

        this.recurringUntilDate = recurringUntilDate;
    }

    public String getCron() {

        return cron;
    }

    public void setCron(String cron) {

        this.cron = cron;
    }

    public Date getNextExecution() {

        return nextExecution;
    }

    public void setNextExecution(Date nextExecution) {

        this.nextExecution = nextExecution;
    }

    @Override
    public String toString() {

        return "ArachneJob{" +
                "enabled=" + enabled +
                ", startDate=" + startDate +
                ", frequency=" + frequency +
                ", cron='" + cron + '\'' +
                '}';
    }
}
