package org.ohdsi.webapi.executionengine.service;

import static com.google.common.io.Files.createTempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFile;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFileContent;
import org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * This class responsible for:
 * <li> if the analysis results file is too large, then it is split into volumes, which allows us to store huge data in DB without a headache
 * <li> It counts the number of files in the analysis
 */
@Service
public class AnalysisZipRepackService {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AnalysisZipRepackService.class);
    public static final String MEDIA_TYPE = "application";

    public AnalysisRepackResult process(List<AnalysisResultFileContent> originalFileContent, int zipVolumeSizeMb) {

        if (CollectionUtils.isEmpty(originalFileContent)) {
            return new AnalysisRepackResult(originalFileContent);
        }

        File temporaryDir = createTempDir();

        AnalysisRepackResult analysisRepackResult = new AnalysisRepackResult(originalFileContent);
        try {
            AnalysisResultFileContent analysisResultZip = originalFileContent.stream()
                    .filter(content -> AnalysisZipUtils.isResultArchive(content.getAnalysisResultFile().getFileName()))
                    .findFirst().orElse(null);

            if (analysisResultZip != null) {
                List<AnalysisResultFileContent> contentsWithoutAnalysisResultZip = originalFileContent.stream()
                        .filter(content -> !content.equals(analysisResultZip))
                        .collect(Collectors.toList());

                Path analysisResultZipPath = AnalysisZipUtils.createFileInTempDir(temporaryDir, analysisResultZip.getAnalysisResultFile().getFileName(), analysisResultZip.getContents());
                int amountFilesInAnalysis = contentsWithoutAnalysisResultZip.size() + AnalysisZipUtils.getAmountFilesInArchive(analysisResultZipPath);

                AnalysisZipUtils.repackZipWithMultivalue(analysisResultZipPath, zipVolumeSizeMb);
                List<AnalysisResultFileContent> contentsForRepackedAnalysisResultZip = getContentsForMultivalueZip(temporaryDir, analysisResultZip);

                List<AnalysisResultFileContent> resultFileContents = ListUtils.union(contentsWithoutAnalysisResultZip, contentsForRepackedAnalysisResultZip);
                analysisRepackResult = new AnalysisRepackResult(resultFileContents, amountFilesInAnalysis);
            }

        } catch (Exception e) {
            LOGGER.error("Cannot split archives", e);
        } finally {
            FileUtils.deleteQuietly(temporaryDir);
        }

        return analysisRepackResult;
    }

    private List<AnalysisResultFileContent> getContentsForMultivalueZip(File temporaryDir, AnalysisResultFileContent analysisResultContent) throws IOException {

        ExecutionEngineAnalysisStatus execution = analysisResultContent.getAnalysisResultFile().getExecution();

        List<AnalysisResultFileContent> resultFileContents = new ArrayList<>();

        for (File file : temporaryDir.listFiles()) {
            Path path = file.toPath();

            AnalysisResultFileContent analysisResultFileContent = new AnalysisResultFileContent();
            analysisResultFileContent.setAnalysisResultFile(getFileMetadata(path, execution));
            analysisResultFileContent.setContents(Files.readAllBytes(path));
            resultFileContents.add(analysisResultFileContent);
        }
        return resultFileContents;
    }

    private AnalysisResultFile getFileMetadata(Path path, ExecutionEngineAnalysisStatus execution) {

        AnalysisResultFile analysisResultFile = new AnalysisResultFile();
        analysisResultFile.setFileName(path.getFileName().toString());
        analysisResultFile.setMediaType(MEDIA_TYPE);
        analysisResultFile.setExecution(execution);
        return analysisResultFile;
    }

    public static class AnalysisRepackResult {

        private final List<AnalysisResultFileContent> resultFileContents;
        private final int amountFilesInAnalysis;

        public AnalysisRepackResult(List<AnalysisResultFileContent> resultFileContents, int amountFilesInAnalysis) {

            this.resultFileContents = CollectionUtils.isNotEmpty(resultFileContents) ? resultFileContents : Collections.emptyList();
            this.amountFilesInAnalysis = amountFilesInAnalysis;
        }

        public AnalysisRepackResult(List<AnalysisResultFileContent> resultFileContents) {

            this.resultFileContents = CollectionUtils.isNotEmpty(resultFileContents) ? resultFileContents : Collections.emptyList();
            this.amountFilesInAnalysis = resultFileContents.size();
        }

        public List<AnalysisResultFileContent> getResultFileContents() {

            return resultFileContents;
        }

        public int getAmountFilesInAnalysis() {

            return amountFilesInAnalysis;
        }
    }

}
