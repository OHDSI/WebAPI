package org.ohdsi.webapi.common.generation;

import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.source.Source;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;

@MappedSuperclass
public abstract class CommonGeneration {

    @Id
    @Column
    protected Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    protected Source source;

    @Column(name = "start_time")
    @Temporal(TemporalType.TIMESTAMP)
    protected Date startTime;

    @Column(name = "end_time")
    @Temporal(TemporalType.TIMESTAMP)
    protected Date endTime;

    @Column
    protected String status;

    @Column(name = "exit_message")
    private String exitMessage;

    @Embedded
    protected AnalysisGenerationBaseInfo info;

    public Long getId() {

        return id;
    }

    public Source getSource() {

        return source;
    }

    public Date getStartTime() {

        return startTime;
    }

    public Date getEndTime() {

        return endTime;
    }

    public String getStatus() {

        return status;
    }

    public String getExitMessage() {

        return exitMessage;
    }

    public Integer getHashCode() {
        return this.info != null ? this.info.getHashCode() : null;
    }

    public UserEntity getCreatedBy() {

        return this.info != null ? this.info.getCreatedBy() : null;
    }
}
