package org.ohdsi.webapi.common.generation;

import org.ohdsi.webapi.source.Source;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@MappedSuperclass
public class CommonGeneration {

    @Id
    @Column
    protected Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    protected Source source;

    @Column(name = "hash_code")
    protected Integer hashCode;

    @Column(name = "start_time")
    @Temporal(TemporalType.TIMESTAMP)
    protected Date startTime;

    @Column(name = "end_time")
    @Temporal(TemporalType.TIMESTAMP)
    protected Date endTime;

    @Column
    protected String status;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Source getSource() {

        return source;
    }

    public void setSource(Source source) {

        this.source = source;
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

    public String getStatus() {

        return status;
    }

    public void setStatus(String status) {

        this.status = status;
    }
}
