package org.ohdsi.webapi.executionengine.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;

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
