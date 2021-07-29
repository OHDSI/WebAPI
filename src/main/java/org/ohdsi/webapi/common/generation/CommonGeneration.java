package org.ohdsi.webapi.common.generation;

import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.source.Source;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
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
