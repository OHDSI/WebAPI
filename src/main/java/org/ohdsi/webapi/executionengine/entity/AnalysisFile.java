package org.ohdsi.webapi.executionengine.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
