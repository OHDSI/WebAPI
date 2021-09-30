package org.ohdsi.webapi.executionengine.entity;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;

@Entity(name = "output_files")
public class AnalysisResultFile {

    @Id
    @GenericGenerator(
        name = "analysis_result_file_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @Parameter(name = "sequence_name", value = "output_file_seq"),
                @Parameter(name = "increment_size", value = "1")
        }
    )
    @GeneratedValue(generator = "analysis_result_file_generator")
    @Column
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id", nullable = false, updatable = false)
    private ExecutionEngineAnalysisStatus execution;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "media_type")
    private String mediaType;

    @OneToOne(optional = false, mappedBy = "analysisResultFile", fetch = FetchType.LAZY)
    private AnalysisResultFileContent content;

    public AnalysisResultFile() {

    }

    public AnalysisResultFile(
            ExecutionEngineAnalysisStatus execution,
            String fileName,
            String mediaType) {

        this.execution = execution;
        this.fileName = fileName;
        this.mediaType = mediaType;
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ExecutionEngineAnalysisStatus getExecution() {

        return execution;
    }

    public void setExecution(ExecutionEngineAnalysisStatus execution) {

        this.execution = execution;
    }

    public String getFileName() {

        return fileName;
    }

    public void setFileName(String fileName) {

        this.fileName = fileName;
    }

    public byte[] getContents() {

        return content.getContents();
    }

    public void setContents(byte[] contents) {

        this.content.setContents(contents);
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }
}
