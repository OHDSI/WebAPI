package org.ohdsi.webapi.source;

import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.common.DBMSType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Registers "trexdb" as a SqlRender target dialect.
 *
 * TrexDB runs DuckDB, so the duckdb patterns are copied under the trexdb name.
 * SqlTranslate keeps a single JVM-wide pattern table, so this also covers
 * libraries that translate internally (cohort and characterization SQL).
 * A SqlRender release that ships trexdb itself makes this a no-op.
 */
@Component
public class TrexdbDialect {

    public static final String NAME = DBMSType.TREXDB.getOhdsiDB();

    // The SqlRender target whose patterns trexdb reuses.
    static final String BASE_DIALECT = "duckdb";

    private static final String PATTERNS_RESOURCE = "/inst/csv/replacementPatterns.csv";
    private static final Logger log = LoggerFactory.getLogger(TrexdbDialect.class);

    private static volatile boolean registered;

    @PostConstruct
    void init() {
        register();
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        List<String> lines = readBundledPatterns();
        if (lines.stream().anyMatch(line -> line.startsWith(NAME + ","))) {
            registered = true;
            log.info("SqlRender already ships the {} dialect", NAME);
            return;
        }
        List<String> merged = new ArrayList<>(lines);
        String prefix = BASE_DIALECT + ",";
        for (String line : lines) {
            if (line.startsWith(prefix)) {
                merged.add(NAME + line.substring(BASE_DIALECT.length()));
            }
        }
        if (merged.size() == lines.size()) {
            throw new IllegalStateException("SqlRender has no " + BASE_DIALECT + " patterns to register " + NAME + " from");
        }
        try {
            Path file = Files.createTempFile("sqlrender-patterns-", ".csv");
            file.toFile().deleteOnExit();
            Files.write(file, merged, StandardCharsets.UTF_8);
            SqlTranslate.setReplacementPatterns(file.toString());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not register the " + NAME + " SqlRender dialect", e);
        }
        registered = true;
        log.info("Registered SqlRender dialect {} ({} patterns)", NAME, merged.size() - lines.size());
    }

    private static List<String> readBundledPatterns() {
        try (InputStream in = SqlTranslate.class.getResourceAsStream(PATTERNS_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("SqlRender resource " + PATTERNS_RESOURCE + " not found");
            }
            return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
