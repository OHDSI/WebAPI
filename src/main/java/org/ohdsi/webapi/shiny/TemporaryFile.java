package org.ohdsi.webapi.shiny;


import java.nio.file.Path;

public class TemporaryFile {
    private final String filename;
    private final Path file;

    public TemporaryFile(String filename, Path file) {
        this.filename = filename;
        this.file = file;
    }

    public String getFilename() {
        return filename;
    }

    public Path getFile() {
        return file;
    }
}
