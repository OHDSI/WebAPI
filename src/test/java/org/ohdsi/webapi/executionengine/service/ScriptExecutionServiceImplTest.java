package org.ohdsi.webapi.executionengine.service;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.compress.utils.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFile;
import org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus;
import org.ohdsi.webapi.executionengine.entity.ExecutionEngineGenerationEntity;
import org.ohdsi.webapi.executionengine.repository.ExecutionEngineGenerationRepository;
import org.ohdsi.webapi.shiro.management.datasource.SourceAccessor;

@RunWith(MockitoJUnitRunner.class)
public class ScriptExecutionServiceImplTest {

    public static final long EXECUTION_ID = 1L;
    @Mock
    private ExecutionEngineGenerationRepository executionEngineGenerationRepository;

    @Mock
    private SourceAccessor sourceAccessor;

    @InjectMocks
    @Spy
    private ScriptExecutionServiceImpl scriptExecutionService;

    @Spy
    private ExecutionEngineAnalysisStatus executionEngineAnalysisStatus = new ExecutionEngineAnalysisStatus();

    @Spy
    private ExecutionEngineGenerationEntity executionEngineGenerationEntity = new DummyExecutionEngineGenerationEntity();


    @Before
    public void setUp() throws Exception {

        doReturn(executionEngineAnalysisStatus)
                .when(executionEngineGenerationEntity)
                .getAnalysisExecution();
        doReturn(Optional.of(executionEngineGenerationEntity))
                .when(executionEngineGenerationRepository)
                .findById(eq(EXECUTION_ID));
        doNothing()
                .when(sourceAccessor)
                .checkAccess(any());
    }

    @Test
    public void getExecutionResultWithMultivalueZip() throws Exception {

        executionEngineAnalysisStatus.setResultFiles(Arrays.asList(
                        createAnalysisResultFile("/analysisresult/stdout.txt"),
                        createAnalysisResultFile("/analysisresult/file1.txt"),
                        createAnalysisResultFile("/analysisresult/multivalue/analysis_result.zip"),
                        createAnalysisResultFile("/analysisresult/multivalue/analysis_result.z01"),
                        createAnalysisResultFile("/analysisresult/multivalue/analysis_result.z02"),
                        createAnalysisResultFile("/analysisresult/multivalue/analysis_result.z03"),
                        createAnalysisResultFile("/analysisresult/multivalue/analysis_result.z04")
                ));

        File executionResult = scriptExecutionService.getExecutionResult(EXECUTION_ID);
        ZipFile zipFile = new ZipFile(executionResult);

        List<String> fileNamesInZip = ((List<FileHeader>) zipFile.getFileHeaders()).stream().map(FileHeader::getFileName).collect(Collectors.toList());

        MatcherAssert.assertThat(fileNamesInZip, Matchers.containsInAnyOrder(
                "stdout.txt", "file1.txt",
                "analysis/CohortCharacterization.zip", "analysis/runAnalysis.R", "analysis/.Rhistory"));

    }

    @Test
    public void getExecutionResultWithoutMultivolumeZip() throws Exception {

        executionEngineAnalysisStatus.setResultFiles(Arrays.asList(
                        createAnalysisResultFile("/analysisresult/stdout.txt"),
                        createAnalysisResultFile("/analysisresult/file1.txt"),
                        createAnalysisResultFile("/analysisresult/analysis_result.zip")
                ));

        File executionResult = scriptExecutionService.getExecutionResult(EXECUTION_ID);
        ZipFile zipFile = new ZipFile(executionResult);

        List<String> fileNamesInZip = ((List<FileHeader>) zipFile.getFileHeaders()).stream().map(FileHeader::getFileName).collect(Collectors.toList());

        MatcherAssert.assertThat(fileNamesInZip, Matchers.containsInAnyOrder(
                "stdout.txt", "file1.txt",
                "analysis/", //for some reason getFileHeaders returns directories for a normal zip, and dont for multivalue zip
                "analysis/CohortCharacterization.zip", "analysis/runAnalysis.R", "analysis/.Rhistory"));

    }

    @Test
    public void getExecutionResultWithoutZip() throws Exception {

        executionEngineAnalysisStatus.setResultFiles(Arrays.asList(
                        createAnalysisResultFile("/analysisresult/stdout.txt"),
                        createAnalysisResultFile("/analysisresult/file1.txt"),
                        createAnalysisResultFile("/analysisresult/file2.txt")
                ));

        File executionResult = scriptExecutionService.getExecutionResult(EXECUTION_ID);
        ZipFile zipFile = new ZipFile(executionResult);

        List<String> fileNamesInZip = ((List<FileHeader>) zipFile.getFileHeaders()).stream().map(FileHeader::getFileName).collect(Collectors.toList());

        MatcherAssert.assertThat(fileNamesInZip, Matchers.containsInAnyOrder("stdout.txt", "file1.txt", "file2.txt"));

    }

    private AnalysisResultFile createAnalysisResultFile(String fileName) throws IOException {

        AnalysisResultFile analysisResultFile = spy(new AnalysisResultFile());
        analysisResultFile.setFileName(new File(fileName).getName());
        analysisResultFile.setMediaType("Application");
        doReturn(IOUtils.toByteArray(this.getClass().getResourceAsStream(fileName)))
                .when(analysisResultFile)
                .getContents();

        return analysisResultFile;
    }


    public static class DummyExecutionEngineGenerationEntity extends ExecutionEngineGenerationEntity {

    }


}