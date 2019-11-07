package org.ohdsi.webapi.db.migartion;

import com.odysseusinc.arachne.commons.config.flyway.ApplicationContextAwareSpringMigration;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
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

@Component
public class V2_8_0_20191106092815__migrateEventFAType implements ApplicationContextAwareSpringMigration {
    private final static String SQL_PATH = "/db/migration/java/V2_8_0_20191106092815__migrateEventFAType/updateFaType.sql";
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
        sourceRepository.findAll().forEach(source -> {
            try {
                String resultsSchema = source.getTableQualifierOrNull(SourceDaimon.DaimonType.Results);

                if (resultsSchema == null) {
                    return; // no results in this source
                }

                CancelableJdbcTemplate jdbcTemplate = migrationDAO.getSourceJdbcTemplate(source);

                this.migrationDAO.updateColumnValue(source, jdbcTemplate);

            }
            catch(Exception e) {
                log.error(String.format("Failed to update fa type value for source: %s (%s)", source.getSourceName(), source.getSourceKey()));
            }
        });
    }

    @Service
    public static class MigrationDAO extends AbstractDaoService {
        public void updateColumnValue(Source source, CancelableJdbcTemplate jdbcTemplate) {
            String resultsSchema = source.getTableQualifier(SourceDaimon.DaimonType.Results);
            String[] params = new String[]{"results_schema"};
            String[] values = new String[]{resultsSchema};
            String translatedSql = SqlRender.renderSql(ResourceHelper.GetResourceAsString(SQL_PATH), params, values);
            jdbcTemplate.batchUpdate(translatedSql);
        }
    }
}
