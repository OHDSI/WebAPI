package org.ohdsi.webapi.executionengine.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.compress.utils.IOUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFileContent;
import org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus;

public class AnalysisZipRepackServiceTest {

    private AnalysisZipRepackService analysisZipRepackService = new AnalysisZipRepackService();
    private AnalysisResultFileContentSensitiveInfoServiceImpl sensitiveInfoService = new AnalysisResultFileContentSensitiveInfoServiceImpl();
    private ExecutionEngineAnalysisStatus execution;
    private List<AnalysisResultFileContent> resultFileContents;

    @Before
    public void setUp() throws Exception {

        execution = new ExecutionEngineAnalysisStatus();

        sensitiveInfoService = new AnalysisResultFileContentSensitiveInfoServiceImpl();
        sensitiveInfoService.init();

        resultFileContents = Arrays.asList(
                new AnalysisResultFileContent(execution, "analysis_result.zip", "application", getTestArchive("/analysisresult/analysis_result.zip")),
                new AnalysisResultFileContent(execution, "stdout.txt", "text/plain", getTestArchive("/analysisresult/stdout.txt"))
        );
    }

    @Test
    public void processArchivesIsBigEnoughToSplitIntoMultiplyVolumes() {

        List<AnalysisResultFileContent> analysisRepackResult = analysisZipRepackService.process(this.resultFileContents, 1);
        List<String> fileNamesAfterRepack = analysisRepackResult.stream().map(f -> f.getAnalysisResultFile().getFileName()).collect(Collectors.toList());
        assertThat(
                fileNamesAfterRepack,
                Matchers.containsInAnyOrder("analysis_result.zip", "analysis_result.z01", "analysis_result.z02", "analysis_result.z03", "analysis_result.z04", "stdout.txt")
        );
    }


    @Test
    public void processArchivesIsNotBigEnoughToSplitIntoMultiplyVolumes() {

        List<AnalysisResultFileContent> analysisRepackResult = analysisZipRepackService.process(this.resultFileContents, 5);

        List<String> fileNamesAfterRepack = analysisRepackResult.stream().map(f -> f.getAnalysisResultFile().getFileName()).collect(Collectors.toList());
        assertThat(
                fileNamesAfterRepack,
                Matchers.containsInAnyOrder("analysis_result.zip", "stdout.txt")
        );
    }

    @Test
    public void processThereIsNotArchiveToSplit() throws IOException {

        resultFileContents = Arrays.asList(
                new AnalysisResultFileContent(execution, "stdout.txt", "text/plain", getTestArchive("/analysisresult/stdout.txt")),
                new AnalysisResultFileContent(execution, "file1.txt", "text/plain", getTestArchive("/analysisresult/stdout.txt")),
                new AnalysisResultFileContent(execution, "file2.txt", "text/plain", getTestArchive("/analysisresult/stdout.txt"))
        );

        List<AnalysisResultFileContent> analysisRepackResult = analysisZipRepackService.process(this.resultFileContents, 5);

        List<String> fileNamesAfterRepack = analysisRepackResult.stream().map(f -> f.getAnalysisResultFile().getFileName()).collect(Collectors.toList());
        assertThat(
                fileNamesAfterRepack,
                Matchers.containsInAnyOrder("file1.txt", "file2.txt", "stdout.txt")
        );
    }


    private byte[] getTestArchive(String file) throws IOException {

        InputStream resourceAsStream = this.getClass().getResourceAsStream(file);
        return IOUtils.toByteArray(resourceAsStream);
    }
}