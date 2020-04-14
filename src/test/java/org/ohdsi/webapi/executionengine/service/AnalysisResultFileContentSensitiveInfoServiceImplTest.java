package org.ohdsi.webapi.executionengine.service;

import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFileContent;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFileContentList;
import org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus;

public class AnalysisResultFileContentSensitiveInfoServiceImplTest {

    private AnalysisResultFileContentSensitiveInfoServiceImpl sensitiveInfoService = new AnalysisResultFileContentSensitiveInfoServiceImpl();
    private ExecutionEngineAnalysisStatus execution;
    private AnalysisResultFileContentList source;

    @Before
    public void setUp() throws Exception {

        execution = new ExecutionEngineAnalysisStatus();

        sensitiveInfoService = new AnalysisResultFileContentSensitiveInfoServiceImpl();
        sensitiveInfoService.init();

        AnalysisResultFileContent zipFileContent = new AnalysisResultFileContent(execution, "file.zip", "application", getTestArchive("/sensitiveinfo/analysis.zip"));
        AnalysisResultFileContent stdoutFileContent = new AnalysisResultFileContent(execution, "stdout.txt", "text/plain", getTestArchive("/sensitiveinfo/stdout.txt"));

        source = new AnalysisResultFileContentList();
        source.getFiles().add(zipFileContent);
        source.getFiles().add(stdoutFileContent);

    }

    @Test
    public void filterSensitiveInfoRepackZipArchieWithSizeLessChanOrigin() throws Exception {

        sensitiveInfoService.setZipChunkSizeMb(1);

        AnalysisResultFileContentList analysisResultFileContentList = sensitiveInfoService.filterSensitiveInfo(source, Collections.emptyMap());

        Assert.assertEquals(6, analysisResultFileContentList.getFiles().size());

        List<String> fileNamesAfterFilterSensitiveInfoMethod = analysisResultFileContentList.getFiles().stream().map(f -> f.getAnalysisResultFile().getFileName()).collect(Collectors.toList());
        assertThat(
                fileNamesAfterFilterSensitiveInfoMethod,
                Matchers.containsInAnyOrder("file.zip", "file.z01", "file.z02", "file.z03", "file.z04", "stdout.txt")
        );

    }


    @Test
    public void filterSensitiveInfoRepackZipArchieWithSizeMoreChanOrigin() throws Exception {

        sensitiveInfoService.setZipChunkSizeMb(3);

        AnalysisResultFileContentList analysisResultFileContentList = sensitiveInfoService.filterSensitiveInfo(source, Collections.emptyMap());

        Assert.assertEquals(3, analysisResultFileContentList.getFiles().size());

        List<String> fileNamesAfterFilterSensitiveInfoMethod = analysisResultFileContentList.getFiles().stream().map(f -> f.getAnalysisResultFile().getFileName()).collect(Collectors.toList());
        assertThat(
                fileNamesAfterFilterSensitiveInfoMethod,
                Matchers.containsInAnyOrder("file.zip", "file.z01", "stdout.txt")
        );

    }

    @Test
    public void filterSensitiveInfoRepackZipArchieAsSingleZip() throws Exception {

        sensitiveInfoService.setZipChunkSizeMb(5);

        AnalysisResultFileContentList analysisResultFileContentList = sensitiveInfoService.filterSensitiveInfo(source, Collections.emptyMap());

        Assert.assertEquals(2, analysisResultFileContentList.getFiles().size());

        List<String> fileNamesAfterFilterSensitiveInfoMethod = analysisResultFileContentList.getFiles().stream().map(f -> f.getAnalysisResultFile().getFileName()).collect(Collectors.toList());
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