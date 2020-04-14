package org.ohdsi.webapi.executionengine.service;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.compress.utils.IOUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFileContent;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFileContentList;
import org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus;

public class ArchiveServiceTest {

    private ArchiveService archiveService = new ArchiveService();
    private AnalysisResultFileContentSensitiveInfoServiceImpl sensitiveInfoService = new AnalysisResultFileContentSensitiveInfoServiceImpl();
    private ExecutionEngineAnalysisStatus execution;
    private List<AnalysisResultFileContent> resultFileContents;

    @Before
    public void setUp() throws Exception {

        execution = new ExecutionEngineAnalysisStatus();

        sensitiveInfoService = new AnalysisResultFileContentSensitiveInfoServiceImpl();
        sensitiveInfoService.init();

        resultFileContents = Arrays.asList(
                new AnalysisResultFileContent(execution, "file.zip", "application", getTestArchive("/sensitiveinfo/analysis.zip")),
                new AnalysisResultFileContent(execution, "stdout.txt", "text/plain", getTestArchive("/sensitiveinfo/stdout.txt"))
        );
    }

    @Test
    public void splitZipArchivesIntoMultiplyVolumes() throws Exception {

        List<AnalysisResultFileContent> resultFileContents = archiveService.splitZipArchivesIntoMultiplyVolumes(this.resultFileContents, 1);
        List<String> fileNamesAfterFilterSensitiveInfoMethod = resultFileContents.stream().map(f -> f.getAnalysisResultFile().getFileName()).collect(Collectors.toList());
        assertThat(
                fileNamesAfterFilterSensitiveInfoMethod,
                Matchers.containsInAnyOrder("file.zip", "file.z01", "file.z02", "file.z03", "file.z04", "stdout.txt")
        );
    }


    @Test
    public void splitZipArchivesIntoMultiplyVolumes2() throws Exception {

        List<AnalysisResultFileContent> resultFileContents = archiveService.splitZipArchivesIntoMultiplyVolumes(this.resultFileContents, 5);
        List<String> fileNamesAfterFilterSensitiveInfoMethod = resultFileContents.stream().map(f -> f.getAnalysisResultFile().getFileName()).collect(Collectors.toList());
        assertThat(
                fileNamesAfterFilterSensitiveInfoMethod,
                Matchers.containsInAnyOrder("file.zip", "stdout.txt")
        );
    }


    private byte[] getTestArchive(String file) throws IOException {

        InputStream resourceAsStream = this.getClass().getResourceAsStream(file);
        return IOUtils.toByteArray(resourceAsStream);
    }
}