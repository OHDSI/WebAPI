package org.ohdsi.webapi.cohortsample;

import org.ohdsi.webapi.cohortsample.dto.SampleParametersDTO;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.ws.rs.NotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class CohortSamplingService extends AbstractDaoService {
    private final static CohortSampleRowMapper sampleRowMapper = new CohortSampleRowMapper();
    private final static CohortSampleElementRowMapper elementRowMapper = new CohortSampleElementRowMapper();
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public CohortSamplingService(
            TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    public List<CohortSample> findSamples(Source source, int cohortDefinitionId) {
        JdbcTemplate jdbcTemplate = getSourceJdbcTemplate(source);
        return jdbcTemplate.query(
                new PreparedStatementRenderer(source, "/resources/cohortsample/sql/findSampleByCohortDefinitionId.sql",
                        "results_schema", source.getTableQualifier(SourceDaimon.DaimonType.Results),
                        "cohortDefinitionId", cohortDefinitionId
                ).getSql(), sampleRowMapper);
    }

    public List<SampleElement> findSampleElements(Source source, int cohortSampleId) {
        JdbcTemplate jdbcTemplate = getSourceJdbcTemplate(source);
        return jdbcTemplate.query(
                new PreparedStatementRenderer(source, "/resources/cohortsample/sql/findElementsByCohortSampleId.sql",
                        "results_schema", source.getTableQualifier(SourceDaimon.DaimonType.Results),
                        "cohortSampleId", cohortSampleId
                ).getSql(), elementRowMapper);
    }

    public CohortSample findSample(Source source, int cohortSampleId) {
        JdbcTemplate jdbcTemplate = getSourceJdbcTemplate(source);
        List<CohortSample> samples = jdbcTemplate.query(
                new PreparedStatementRenderer(source, "/resources/cohortsample/sql/findSampleById.sql",
                        "results_schema", source.getTableQualifier(SourceDaimon.DaimonType.Results),
                        "cohortSampleId", cohortSampleId
                ).getSql(), sampleRowMapper);
        if (samples == null || samples.isEmpty()) {
            throw new NotFoundException("Cohort sample with ID " + cohortSampleId + " does not exist.");
        }
        return samples.get(0);
    }

    public CohortSample createSample(Source source, int cohortDefinitionId, SampleParametersDTO sampleParameters) {
        JdbcTemplate jdbcTemplate = getSourceJdbcTemplate(source);

        CohortSample sample = new CohortSample();
        sample.setCohortDefinitionId(cohortDefinitionId);
        sample.setAgeMin(sampleParameters.getAgeMin());
        sample.setAgeMax(sampleParameters.getAgeMax());
        sample.setGenderConceptId(sampleParameters.getGenderConceptId());

        final List<SampleElement> elements = sampleElements(sample, jdbcTemplate, source);

        transactionTemplate.execute((TransactionCallback<Void>) transactionStatus -> {
            int sampleId = insertSample(jdbcTemplate, source, sampleParameters, cohortDefinitionId);
            sample.setId(sampleId);

            insertSampledElements(source, jdbcTemplate, sampleId, elements);

            sample.setElements(elements);

            return null;
        });

        return sample;
    }

    private int insertSample(JdbcTemplate jdbcTemplate, Source source, SampleParametersDTO sampleParameters, int cohortDefinitionId) {
        String[] parameters = new String[] { "results_schema" };
        String[] parameterValues = new String[] { source.getTableQualifier(SourceDaimon.DaimonType.Results) };
        String[] sqlParameters = new String[] { "cohortDefinitionId", "size", "ageMin", "ageMax", "genderConceptId", "createdById", "createdDate" };
        Object[] sqlParameterValues = new Object[] { cohortDefinitionId, sampleParameters.getSize(), sampleParameters.getAgeMin(), sampleParameters.getAgeMax(), sampleParameters.getGenderConceptId(), getCurrentUser(), new Date()};

        return jdbcTemplate
                .execute(new InsertStatementCallback(
                        new PreparedStatementRenderer(source, "/resources/cohortsample/sql/insertSample.sql", parameters, parameterValues, sqlParameters, sqlParameterValues).getSql(),
                        "id"));
    }

    private void insertSampledElements(Source source, JdbcTemplate jdbcTemplate, int sampleId, List<SampleElement> elements) {
        String[] parameters = new String[] { "results_schema" };
        String[] parameterValues = new String[] { source.getTableQualifier(SourceDaimon.DaimonType.Results) };
        String[] sqlParameters = new String[] { "cohortSampleId", "rank", "personId", "age", "genderConceptId" };
        Object[] sqlValues = new Object[5];
        sqlValues[0] = sampleId;

        String[] statements = new String[elements.size()];
        int i = 0;
        for (SampleElement element : elements) {
            sqlValues[1] = element.getRank();
            sqlValues[2] = element.getPersonId();
            sqlValues[3] = element.getAge();
            sqlValues[4] = element.getGenderConceptId();
            statements[i] = new PreparedStatementRenderer(source, "/resources/cohortsample/sql/insertSample.sql", parameters, parameterValues, sqlParameters, sqlValues).getSql();
            i++;
        }

        jdbcTemplate.batchUpdate(statements);
    }

    private List<SampleElement> sampleElements(CohortSample sample, JdbcTemplate jdbcTemplate, Source source) {
        StringBuilder expressionBuilder = new StringBuilder();
        Map<String, String> parameters = new LinkedHashMap<>();
        Map<String, Object> sqlVariables = new LinkedHashMap<>();

        parameters.put("results_schema", source.getTableQualifier(SourceDaimon.DaimonType.Results));
        parameters.put("CDM_schema", source.getTableQualifier(SourceDaimon.DaimonType.CDM));
        sqlVariables.put("cohort_definition_id", sample.getCohortDefinitionId());
        if (sample.getAgeMin() != null) {
            expressionBuilder.append("AND cast(year(c.cohort_start_date) - p.year_of_birth as int) >= @age_min");
            sqlVariables.put("age_min", sample.getAgeMin());
        }
        if (sample.getAgeMax() != null) {
            expressionBuilder.append("AND cast(year(c.cohort_start_date) - p.year_of_birth as int) < @gender_concept_id").append(sample.getAgeMax());
            sqlVariables.put("age_max", sample.getAgeMax());
        }
        if (sample.getGenderConceptId() != null) {
            expressionBuilder.append("AND p.gender_concept_id = ").append(sample.getGenderConceptId());
            sqlVariables.put("gender_concept_id", sample.getGenderConceptId());
        }

        parameters.put("expression", expressionBuilder.toString());

        PreparedStatementRenderer renderer = new PreparedStatementRenderer(source, "/resources/cohortsample/sql/generateSample.sql",
                parameters.keySet().toArray(new String[0]),
                parameters.values().toArray(new String[0]),
                sqlVariables.keySet().toArray(new String[0]),
                sqlVariables.values().toArray(new Object[0]));

        CancelableJdbcTemplate template = new CancelableJdbcTemplate();
        template.setMaxRows(sample.getSize());

        return jdbcTemplate.query(renderer.getSql(), (rs, rowNum) -> {
            SampleElement element = new SampleElement();
            element.setRank(rowNum);
            element.setAge(rs.getInt("age"));
            element.setGenderConceptId(rs.getInt("gender_concept_id"));
            element.setPersonId(rs.getLong("person_id"));
            return element;
        });
    }

    public void deleteSample(Source source, int sampleId) {
        JdbcTemplate jdbcTemplate = getSourceJdbcTemplate(source);
        String resultsSchema = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        String sql = new PreparedStatementRenderer(
                source,
                "/resources/cohortsample/sql/deleteSampleById.sql",
                "results_schema",
                resultsSchema,
                "cohortSampleId",
                sampleId)
                .getSql();

        transactionTemplate.execute((TransactionCallback<Void>) transactionStatus -> {
            jdbcTemplate.update(sql);
            return null;
        });
    }

    private static class CohortSampleRowMapper implements RowMapper<CohortSample> {
        @Override
        public CohortSample mapRow(ResultSet rs, int rowNum) throws SQLException {
            CohortSample sample = new CohortSample();
            sample.setId(rs.getInt("id"));
            sample.setCohortDefinitionId(rs.getInt("cohort_definition_id"));
            sample.setSize(rs.getInt("size"));
            sample.setGenderConceptId(rs.getInt("gender_concept_id"));
            sample.setAgeMax(rs.getInt("age_max"));
            sample.setAgeMin(rs.getInt("age_min"));
            return sample;
        }
    }


    private static class CohortSampleElementRowMapper implements RowMapper<SampleElement> {
        @Override
        public SampleElement mapRow(ResultSet rs, int rowNum) throws SQLException {
            SampleElement sample = new SampleElement();
            sample.setRank(rs.getInt("rank"));
            sample.setSampleId(rs.getInt("cohort_sample_id"));
            sample.setPersonId(rs.getInt("person_id"));
            sample.setGenderConceptId(rs.getInt("gender_concept_id"));
            sample.setAge(rs.getInt("age"));
            return sample;
        }
    }

    private static class InsertStatementCallback implements StatementCallback<Integer> {
        private final String sql;
        private final String idColumn;

        InsertStatementCallback(String sql, String idColumn) {
            this.sql = sql;
            this.idColumn = idColumn;
        }

        @Override
        public Integer doInStatement(Statement stmt) throws SQLException {
            stmt.executeUpdate(sql, new String[] { idColumn });

            return stmt.getGeneratedKeys().getInt(idColumn);
        }
    }
}
