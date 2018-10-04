package org.ohdsi.webapi.common.generation;

import java.util.Date;

public class CommonGenerationDTO {
    private Long id;
    private String status;
    private String sourceKey;
    private Integer hashCode;
    private Date startTime;
    private Date endTime;
    private String exitMessage;

    public Long getId() {

        return id;
    }

    public void setId(final Long id) {

        this.id = id;
    }

    public String getStatus() {

        return status;
    }

    public void setStatus(final String status) {

        this.status = status;
    }

    public String getSourceKey() {

        return sourceKey;
    }

    public void setSourceKey(final String sourceKey) {

        this.sourceKey = sourceKey;
    }

    public Integer getHashCode() {

        return hashCode;
    }

    public void setHashCode(Integer hashCode) {

        this.hashCode = hashCode;
    }

    public Date getStartTime() {

        return startTime;
    }

    public void setStartTime(Date startTime) {

        this.startTime = startTime;
    }

    public Date getEndTime() {

        return endTime;
    }

    public void setEndTime(Date endTime) {

        this.endTime = endTime;
    }

    public String getExitMessage() {

        return exitMessage;
    }

    public void setExitMessage(String exitMessage) {

        this.exitMessage = exitMessage;
    }
}
