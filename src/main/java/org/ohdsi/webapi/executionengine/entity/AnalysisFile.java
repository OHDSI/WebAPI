package org.ohdsi.webapi.executionengine.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity(name = "input_files")
public class AnalysisFile {

    @Id
    @GenericGenerator(
        name = "analysis_input_file_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @Parameter(name = "sequence_name", value = "input_file_seq"),
                @Parameter(name = "increment_size", value = "1")
        }
    )
    @GeneratedValue(generator = "analysis_input_file_generator")
    @Column
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "execution_id", nullable = false, updatable = false)
    private ExecutionEngineAnalysisStatus analysisExecution;

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

    public ExecutionEngineAnalysisStatus getAnalysisExecution() {

        return analysisExecution;
    }

    public void setAnalysisExecution(ExecutionEngineAnalysisStatus analysisExecution) {

        this.analysisExecution = analysisExecution;
    }
}
