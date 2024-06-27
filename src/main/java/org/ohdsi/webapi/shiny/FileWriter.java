package org.ohdsi.webapi.shiny;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.InternalServerErrorException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

@Component
public class FileWriter {

    private static final Logger LOG = LoggerFactory.getLogger(FileWriter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Path writeTextFile(Path path, Consumer<PrintWriter> writer) {
        try (OutputStream out = Files.newOutputStream(path); PrintWriter printWriter = new PrintWriter(out)) {
            writer.accept(printWriter);
            return path;
        } catch (IOException e) {
            LOG.error("Failed to write file", e);
            throw new InternalServerErrorException();
        }
    }

    public Path writeObjectAsJsonFile(Path parentDir, Object object, String filename) {
        try {
            Path file = Files.createFile(parentDir.resolve(filename));
            try (OutputStream out = Files.newOutputStream(file)) {
                objectMapper.writeValue(out, object);
            }
            return file;
        } catch (IOException e) {
            LOG.error("Failed to package Cohort Counts Shiny application", e);
            throw new InternalServerErrorException();
        }
    }

    public void writeJsonNodeToFile(JsonNode object, Path path) {
        try {
            objectMapper.writeValue(path.toFile(), object);
        } catch (IOException e) {
            LOG.error("Failed to write json file", e);
            throw new InternalServerErrorException();
        }
    }


}
