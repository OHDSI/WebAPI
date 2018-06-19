package org.ohdsi.webapi.executionengine.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity(name = "input_files")
public class AnalysisFile {

    @Id
    @GeneratedValue
    @Column
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "execution_id", nullable = false, updatable = false)
    private AnalysisExecution analysisExecution;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_contents", columnDefinition = "BYTEA")
    @Basic(fetch = FetchType.LAZY)
    private byte[] contents;

    public AnalysisFile() {

    }


    public Long getId() {

        return id;
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

    public AnalysisExecution getAnalysisExecution() {

        return analysisExecution;
    }

    public void setAnalysisExecution(AnalysisExecution analysisExecution) {

        this.analysisExecution = analysisExecution;
    }
}
