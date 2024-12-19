package org.ohdsi.webapi.executionengine.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;

@Entity(name = "output_file_contents")
public class AnalysisResultFileContent {

    @Id
    @Column(name = "output_file_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "output_file_id")
    @MapsId
    private AnalysisResultFile analysisResultFile;

    @Column(name = "file_contents", columnDefinition = "BYTEA")
    @Basic(fetch = FetchType.LAZY)
    private byte[] contents;

    public AnalysisResultFileContent() {

    }

    public AnalysisResultFileContent(ExecutionEngineAnalysisStatus execution,
                                     String fileName,
                                     String mediaType, byte[] contents) {

        this.analysisResultFile = new AnalysisResultFile(execution, fileName, mediaType);
        this.contents = contents;
    }

    public AnalysisResultFile getAnalysisResultFile() {

        return analysisResultFile;
    }

    public void setAnalysisResultFile(AnalysisResultFile analysisResultFile) {

        this.analysisResultFile = analysisResultFile;
    }

    public byte[] getContents() {

        return contents;
    }

    public void setContents(byte[] contents) {

        this.contents = contents;
    }
}
