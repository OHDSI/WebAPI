package org.ohdsi.webapi.db.migartion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.odysseusinc.arachne.commons.config.flyway.ApplicationContextAwareSpringMigration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.json.JSONObject;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceRepository;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import static org.ohdsi.webapi.Constants.Params.GENERATION_ID;

@Component
public class V2_8_0_20190410103000__migratePathwayResults implements ApplicationContextAwareSpringMigration {

	private final static String SQL_PATH = "/db/migration/java/V2_8_0_20190410103000__migratePathwayResults/";
	private static final Logger log = LoggerFactory.getLogger(V2_8_0_20190410103000__migratePathwayResults.class);
	private final SourceRepository sourceRepository;
	private final MigrationDAO migrationDAO;
	private final Environment env;

	@Service
	public static class MigrationDAO extends AbstractDaoService {

		public void savePathwayCodes(List<Object[]> pathwayCodes, Source source, CancelableJdbcTemplate jdbcTemplate) {
			String resultsSchema = source.getTableQualifier(SourceDaimon.DaimonType.Results);
			String[] params;
			String[] values;

			List<PreparedStatementCreator> creators = new ArrayList<>();

			// clear existing results to prevent double-inserts
			params = new String[]{"results_schema"};
			values = new String[]{resultsSchema};

			// delete only codes that belongs current Atlas instance
			List<Object[]> executionIdAndCodes = pathwayCodes.stream().map(v -> new Object[]{ v[0], v[1] }).collect(Collectors.toList());
			String deleteSql = SqlRender.renderSql("DELETE FROM @results_schema.pathway_analysis_codes WHERE pathway_analysis_generation_id = ? AND code = ?", params, values);
			String translatedSql = SqlTranslate.translateSingleStatementSql(deleteSql, source.getSourceDialect());
			jdbcTemplate.batchUpdate(translatedSql, executionIdAndCodes);

			String saveCodesSql = SqlRender.renderSql(ResourceHelper.GetResourceAsString(SQL_PATH + "saveCodes.sql"), params, values);
			saveCodesSql = SqlTranslate.translateSingleStatementSql(saveCodesSql, source.getSourceDialect());
			jdbcTemplate.batchUpdate(saveCodesSql, pathwayCodes);
		}
	}

	private static class EventCohort {

		public int cohortId;
		public String name;
	}

	@Autowired
	public V2_8_0_20190410103000__migratePathwayResults(final SourceRepository sourceRepository,
					final MigrationDAO migrationDAO,
					final Environment env) {
		this.sourceRepository = sourceRepository;
		this.migrationDAO = migrationDAO;
		this.env = env;
	}

	@Override
	public void migrate() throws JsonProcessingException {

		String webAPISchema = this.env.getProperty("spring.jpa.properties.hibernate.default_schema");

		sourceRepository.findAll().forEach(source -> {
			try {

				String[] params;
				String[] values;
				String translatedSql;
				String resultsSchema = source.getTableQualifierOrNull(SourceDaimon.DaimonType.Results);

				if (resultsSchema == null) {
					return; // no results in this source
				}

				CancelableJdbcTemplate jdbcTemplate = migrationDAO.getSourceJdbcTemplate(source);

				// step 1: ensure tables are created and have correct columns
				params = new String[]{"results_schema"};
				values = new String[]{source.getTableQualifier(SourceDaimon.DaimonType.Results)};
				String ensureTablesSql = SqlRender.renderSql(ResourceHelper.GetResourceAsString(SQL_PATH + "ensureTables.sql"), params, values);
				translatedSql = SqlTranslate.translateSql(ensureTablesSql, source.getSourceDialect());
				Arrays.asList(SqlSplit.splitSql(translatedSql)).forEach(jdbcTemplate::execute);

				// step 2: populate pathway_analysis_paths
				params = new String[]{"results_schema"};
				values = new String[]{source.getTableQualifier(SourceDaimon.DaimonType.Results)};
				String savePathwaysSql = SqlRender.renderSql(ResourceHelper.GetResourceAsString(SQL_PATH + "migratePathwayResults.sql"), params, values);

				translatedSql = SqlTranslate.translateSql(savePathwaysSql, source.getSourceDialect());
				Arrays.asList(SqlSplit.splitSql(translatedSql)).forEach(jdbcTemplate::execute);

				// step 3: populate pathway_analysis_codes from each generated design for the given source
				// load the generated designs
				params = new String[]{"webapi_schema", "source_id"};
				values = new String[]{webAPISchema, Integer.toString(source.getSourceId())};
				String generatedDesignSql = SqlRender.renderSql(ResourceHelper.GetResourceAsString(SQL_PATH + "getPathwayGeneratedDesigns.sql"), params, values);
				translatedSql = SqlTranslate.translateSingleStatementSql(generatedDesignSql, this.migrationDAO.getDialect());

				Map<Long, List<EventCohort>> designEventCohorts = migrationDAO.getJdbcTemplate().query(translatedSql, rs -> {
					Map<Long, List<EventCohort>> result = new HashMap<>();
					while (rs.next()) {
						String design = rs.getString("design");
						JSONObject jsonObject = new JSONObject(design);
						// parse design and fetch list of event cohorts
						List<EventCohort> eventCohorts = jsonObject.getJSONArray("eventCohorts").toList()
										.stream().map(obj -> {
											Map ecJson = (Map) obj;
											EventCohort c = new EventCohort();
											c.name = String.valueOf(ecJson.get("name"));
											return c;
										})
										.sorted(Comparator.comparing(d -> d.name))
										.collect(Collectors.toList());

						int index = 0;
						for (EventCohort ec : eventCohorts) {
							ec.cohortId = (int) Math.pow(2, index++); // assign each cohort an ID based on their name-sort order, as a power of 2
						}
						result.put(rs.getLong(GENERATION_ID), eventCohorts);
					}
					return result;
				});

				//fetch the distinct generation_id, combo_id from the source
				params = new String[]{"results_schema"};
				values = new String[]{resultsSchema};
				String distinctGenerationComboIdsSql = SqlRender.renderSql(ResourceHelper.GetResourceAsString(SQL_PATH + "getPathwayGeneratedCodes.sql"), params, values);
				translatedSql = SqlTranslate.translateSingleStatementSql(distinctGenerationComboIdsSql, source.getSourceDialect());

				// retrieve list of generation-comboId-Name-isCombo values
				List<Object[]> generatedComboNames = jdbcTemplate.query(translatedSql, (rs) -> {
					// values of String[] are: "generation_id", "code", "name", "is_combo"
					List<Object[]> result = new ArrayList<>();

					while (rs.next()) {
						Long generationId = rs.getLong("pathway_analysis_generation_id");
						Long comboId = rs.getLong("combo_id");

						if (!designEventCohorts.containsKey(generationId)) {
							continue; // skip this record, since we do not have a design for it
						}
						List<EventCohort> eventCohorts = designEventCohorts.get(generationId);
						List<EventCohort> comboCohorts = eventCohorts.stream().filter(ec -> (ec.cohortId & comboId) > 0).collect(Collectors.toList());
						String names = comboCohorts.stream()
										.map(c -> c.name)
										.collect(Collectors.joining(","));
						result.add(new Object[]{generationId, comboId, names, comboCohorts.size() > 1 ? 1 : 0});
					}
					return result;
				});

				this.migrationDAO.savePathwayCodes(generatedComboNames, source, jdbcTemplate);

			}
			catch(Exception e) {
				log.error(String.format("Failed to migration pathways for source: %s (%s)", source.getSourceName(), source.getSourceKey()));
			}
		});
	}
}
