package org.ohdsi.webapi.shiny;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.InternalServerErrorException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

@Component
public class ManifestUtils {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger LOG = LoggerFactory.getLogger(ManifestUtils.class);

    public JsonNode parseManifest(Path path) {
        try (InputStream in = Files.newInputStream(path)) {
            return objectMapper.readTree(in);
        } catch (IOException e) {
            LOG.error("Failed to parse manifest", e);
            throw new InternalServerErrorException();
        }
    }

    public Consumer<Path> addDataToManifest(JsonNode manifest, Path root) {
        return file -> {
            JsonNode node = manifest.get("files");
            if (node.isObject()) {
                ObjectNode filesNode = (ObjectNode) node;
                Path relative = root.relativize(file);
                ObjectNode item = filesNode.putObject(relative.toString().replace("\\", "/"));
                item.put("checksum", checksum(file));
            } else {
                LOG.error("Wrong manifest.json, there is no files section");
                throw new InternalServerErrorException();
            }
        };
    }

    private String checksum(Path path) {
        try (InputStream in = Files.newInputStream(path)) {
            return DigestUtils.md5Hex(in);
        } catch (IOException e) {
            LOG.error("Failed to calculate checksum", e);
            throw new InternalServerErrorException();
        }
    }


}
