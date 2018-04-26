package org.ohdsi.webapi.executionengine.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity(name = "output_files")
public class AnalysisResultFile {

    @Id
    @GeneratedValue
    @Column
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "execution_id", nullable = false, updatable = false)
    private AnalysisExecution execution;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_contents", columnDefinition = "BYTEA")
    @Basic(fetch = FetchType.LAZY)
    private byte[] contents;

    public AnalysisResultFile() {

    }

    public AnalysisResultFile(
            AnalysisExecution execution,
            String fileName,
            byte[] contents) {

        this.execution = execution;
        this.fileName = fileName;
        this.contents = contents;
    }

    public Long getId() {

        return id;
    }

    public AnalysisExecution getExecution() {

        return execution;
    }

    public void setExecution(AnalysisExecution execution) {

        this.execution = execution;
    }

    public String getFileName() {

        return fileName;
    }

    public void setFileName(String fileName) {

        this.fileName = fileName;
    }

    public byte[] getContents() {

        return contents;
    }

    public void setContents(byte[] contents) {

        this.contents = contents;
    }
}
