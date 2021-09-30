package org.ohdsi.webapi.executionengine.service;

import static com.google.common.io.Files.createTempDir;

import com.odysseusinc.arachne.execution_engine_common.util.CommonFileUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.exception.AtlasException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalysisZipUtils {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AnalysisZipUtils.class);
    private static final String EXTENSION_ZIP = "zip";
    private static final String ZIP_VOLUME_EXT_PATTERN = "z[0-9]+$";

    public static boolean isArchiveVolume(Path path) {

        String filename = path.getFileName().toString();
        return isArchiveVolume(filename);
    }

    public static boolean isArchiveVolume(String filename) {

        String extension = FilenameUtils.getExtension(filename);
        Pattern pattern = Pattern.compile(ZIP_VOLUME_EXT_PATTERN);
        Matcher matcher = pattern.matcher(extension);
        return matcher.find();
    }

    public static boolean isArchive(String filename) {

        String extension = FilenameUtils.getExtension(filename);
        return EXTENSION_ZIP.equalsIgnoreCase(extension);
    }

    public static boolean isResultArchive(String filename) {

        return isArchive(filename) && StringUtils.containsIgnoreCase(filename, "result");
    }

    public static boolean isResultArchiveVolume(String filename) {

        return isArchiveVolume(filename) && StringUtils.containsIgnoreCase(filename, "result");
    }

    public static Path createFileInTempDir(File tempDir, String fileName, byte[] contents) {

        try {
            Path path = new File(tempDir, fileName).toPath();
            Files.write(path, contents, StandardOpenOption.CREATE_NEW);
            return path;
        } catch (Exception e) {
            LOGGER.error("File writing error for file: {}", fileName, e);
            throw new AtlasException(e);
        }
    }

    public static void repackZipWithMultivalue(Path zipPath, int zipVolumeSizeMb) {

        File temporaryDir = createTempDir();
        try {
            CommonFileUtils.unzipFiles(zipPath.toFile(), temporaryDir);

            AnalysisZipUtils.deleteZipWithVolumes(zipPath);

            long zipChunkSizeInBytes = zipVolumeSizeMb * 1024 * 1024;
            CommonFileUtils.compressAndSplit(
                    temporaryDir,
                    zipPath.toFile(),
                    zipChunkSizeInBytes
            );
        } catch (ZipException e) {
            LOGGER.error("Error unzipping file", e);
        } catch (IOException e) {
            LOGGER.error("File writing error", e);
        } finally {
            FileUtils.deleteQuietly(temporaryDir);
        }
    }

    public static ZipParameters getHeadersForFilesThatWillBeAddedToZip(String fileName) {

        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(CompressionMethod.DEFLATE);
        parameters.setCompressionLevel(CompressionLevel.MAXIMUM);
        parameters.setIncludeRootFolder(false);
        parameters.setReadHiddenFiles(false);
        parameters.setFileNameInZip(fileName); // this would be the name of the file for this entry in the zip file
        return parameters;
    }


    public static void deleteZipWithVolumes(Path zipPath) throws ZipException {
        ZipFile zipFile = new ZipFile(zipPath.toFile());
        List<File> files = zipFile.getSplitZipFiles();
        files.forEach(File::delete);
    }
}
