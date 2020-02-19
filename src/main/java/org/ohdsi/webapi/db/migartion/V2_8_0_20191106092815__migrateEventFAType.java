package org.ohdsi.webapi.db.migartion;

import com.odysseusinc.arachne.commons.config.flyway.ApplicationContextAwareSpringMigration;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceRepository;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Component
public class V2_8_0_20191106092815__migrateEventFAType implements ApplicationContextAwareSpringMigration {
    private final static String UPDATE_VALUE_SQL = ResourceHelper.GetResourceAsString(
            "/db/migration/java/V2_8_0_20191106092815__migrateEventFAType/updateFaType.sql");
    private final static String UPDATE_VALUE_IMPALA_SQL = ResourceHelper.GetResourceAsString(
            "/db/migration/java/V2_8_0_20191106092815__migrateEventFAType/updateFaTypeImpala.sql");
    private static final Logger log = LoggerFactory.getLogger(V2_8_0_20191106092815__migrateEventFAType.class);

    private final SourceRepository sourceRepository;
    private final V2_8_0_20191106092815__migrateEventFAType.MigrationDAO migrationDAO;

    @Autowired
    public V2_8_0_20191106092815__migrateEventFAType(final SourceRepository sourceRepository,
                                                        final V2_8_0_20191106092815__migrateEventFAType.MigrationDAO migrationDAO) {
        this.sourceRepository = sourceRepository;
        this.migrationDAO = migrationDAO;
    }

    @Override
    public void migrate() throws Exception {
        List<Source> sources = sourceRepository.findAll();
        sources.stream()
                .filter(source -> source.getTableQualifierOrNull(SourceDaimon.DaimonType.Results) != null)
                .forEach(source -> {
                    try {
                        CancelableJdbcTemplate jdbcTemplate = migrationDAO.getSourceJdbcTemplate(source);

                        this.migrationDAO.updateColumnValue(source, jdbcTemplate);
                    } catch (Exception e) {
                        log.error(String.format("Failed to update fa type value for source: %s (%s)", source.getSourceName(), source.getSourceKey()));
                        throw e;
                    }
                });
    }

    @Service
    public static class MigrationDAO extends AbstractDaoService {
        public void updateColumnValue(Source source, CancelableJdbcTemplate jdbcTemplate) {
            String resultsSchema = source.getTableQualifier(SourceDaimon.DaimonType.Results);
            String[] params = new String[]{"results_schema"};
            String[] values = new String[]{resultsSchema};
            String translatedSql;
            // Impala can't update non-kudu tables, so use special script with temp table
            if (Source.IMPALA_DATASOURCE.equals(source.getSourceDialect())) {
                translatedSql = SqlRender.renderSql(UPDATE_VALUE_IMPALA_SQL, params, values);
            } else {
                translatedSql = SqlRender.renderSql(UPDATE_VALUE_SQL, params, values);
            }
            for (String sql: SqlSplit.splitSql(translatedSql)) {
                jdbcTemplate.execute(sql);
            }
        }
    }
}
