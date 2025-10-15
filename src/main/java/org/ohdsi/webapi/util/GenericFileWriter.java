package org.ohdsi.webapi.util;

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

/**
 * Generic utility for writing various types of files
 */
@Component
public class GenericFileWriter {

	private static final Logger LOG = LoggerFactory.getLogger(GenericFileWriter.class);
	private final ObjectMapper objectMapper;

	public GenericFileWriter(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public GenericFileWriter() {
		this.objectMapper = new ObjectMapper();
	}

	/**
	 * Write text content to a file using a PrintWriter consumer
	 *
	 * @param path the file path to write to
	 * @param writer consumer that writes content using PrintWriter
	 * @return the path to the written file
	 * @throws InternalServerErrorException if writing fails
	 */
	public Path writeTextFile(Path path, Consumer<PrintWriter> writer) {
		try (OutputStream out = Files.newOutputStream(path);
				 PrintWriter printWriter = new PrintWriter(out)) {
			writer.accept(printWriter);
			return path;
		} catch (IOException e) {
			LOG.error("Failed to write text file to {}", path, e);
			throw new InternalServerErrorException("Failed to write text file: " + e.getMessage());
		}
	}

	/**
	 * Write an object as JSON to a file
	 *
	 * @param parentDir the parent directory
	 * @param object the object to serialize
	 * @param filename the filename
	 * @return the path to the written file
	 * @throws InternalServerErrorException if writing fails
	 */
	public Path writeObjectAsJsonFile(Path parentDir, Object object, String filename) {
		try {
			Path file = Files.createFile(parentDir.resolve(filename));
			try (OutputStream out = Files.newOutputStream(file)) {
				objectMapper.writeValue(out, object);
			}
			return file;
		} catch (IOException e) {
			LOG.error("Failed to write JSON file {} in {}", filename, parentDir, e);
			throw new InternalServerErrorException("Failed to write JSON file: " + e.getMessage());
		}
	}

	/**
	 * Write a JsonNode directly to a file
	 *
	 * @param jsonNode the JSON content
	 * @param path the file path
	 * @throws InternalServerErrorException if writing fails
	 */
	public void writeJsonNodeToFile(JsonNode jsonNode, Path path) {
		try {
			objectMapper.writeValue(path.toFile(), jsonNode);
		} catch (IOException e) {
			LOG.error("Failed to write JsonNode to {}", path, e);
			throw new InternalServerErrorException("Failed to write JSON file: " + e.getMessage());
		}
	}
}