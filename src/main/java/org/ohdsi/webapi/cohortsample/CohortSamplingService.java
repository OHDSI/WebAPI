package org.ohdsi.webapi.cohortsample;

import org.ohdsi.webapi.cohortsample.dto.CohortSampleDTO;
import org.ohdsi.webapi.cohortsample.dto.SampleElementDTO;
import org.ohdsi.webapi.cohortsample.dto.SampleParametersDTO;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.user.dto.UserDTO;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.ohdsi.webapi.cohortsample.dto.SampleParametersDTO.GenderDTO.GENDER_FEMALE_CONCEPT_ID;
import static org.ohdsi.webapi.cohortsample.dto.SampleParametersDTO.GenderDTO.GENDER_MALE_CONCEPT_ID;

/**
 * Service to do manage samples of a cohort definition.
 */
@Component
public class CohortSamplingService extends AbstractDaoService {
    private final TransactionTemplate transactionTemplate;
    private final CohortSampleRepository sampleRepository;
    private final CohortSampleElementRowMapper elementRowMapper = new CohortSampleElementRowMapper();

    @Autowired
    public CohortSamplingService(
            TransactionTemplate transactionTemplate,
            CohortSampleRepository sampleRepository) {
        this.transactionTemplate = transactionTemplate;
        this.sampleRepository = sampleRepository;
    }

    /**
     * Find all sample elements of a sample.
     * @param source Source to use
     * @param cohortSampleId sample ID of the elements.
     * @param withRecordCounts whether to return record counts. This makes the query much slower.
     * @return list of elements.
     */
    public List<SampleElement> findSampleElements(Source source, int cohortSampleId, boolean withRecordCounts) {
        JdbcTemplate jdbcTemplate = getSourceJdbcTemplate(source);
        PreparedStatementRenderer renderer;

        if (withRecordCounts) {
            renderer = new PreparedStatementRenderer(source, "/resources/cohortsample/sql/findElementsByCohortSampleIdWithCounts.sql",
                    new String[]{"results_schema", "CDM_schema"},
                    new String[]{source.getTableQualifier(SourceDaimon.DaimonType.Results), source.getTableQualifier(SourceDaimon.DaimonType.CDM)},
                    "cohortSampleId", cohortSampleId);
        } else {
            renderer = new PreparedStatementRenderer(source, "/resources/cohortsample/sql/findElementsByCohortSampleId.sql",
                    "results_schema",
                    source.getTableQualifier(SourceDaimon.DaimonType.Results),
                    "cohortSampleId", cohortSampleId);
        }
        return jdbcTemplate.query(renderer.getSql(), renderer.getOrderedParams(), elementRowMapper);
    }

    /**
     * Create a new sample in given source and cohort definition, using sample parameters.
     * @param source Source to use
     * @param cohortDefinitionId cohort definition ID to sample
     * @param sampleParameters parameters to define the sample
     * @return list of elements.
     */
    public CohortSampleDTO createSample(Source source, int cohortDefinitionId, SampleParametersDTO sampleParameters) {
        JdbcTemplate jdbcTemplate = getSourceJdbcTemplate(source);

        CohortSample sample = new CohortSample();
        sample.setName(sampleParameters.getName());
        sample.setCohortDefinitionId(cohortDefinitionId);
        sample.setSourceId(source.getId());
        sample.setSize(sampleParameters.getSize());

        SampleParametersDTO.AgeDTO age = sampleParameters.getAge();
        if (age != null) {
            switch (age.getMode()) {
                case LESS_THAN:
                case LESS_THAN_OR_EQUAL:
                    sample.setAgeMax(age.getValue());
                    break;
                case GREATER_THAN:
                case GREATER_THAN_OR_EQUAL:
                    sample.setAgeMin(age.getValue());
                    break;
                case EQUAL_TO:
                    sample.setAgeMin(age.getValue());
                    sample.setAgeMax(age.getValue());
                    break;
                case BETWEEN:
                case NOT_BETWEEN:
                    sample.setAgeMin(age.getMin());
                    sample.setAgeMax(age.getMax());
                    break;
            }
            sample.setAgeMode(age.getMode().getSerialName());
        }

        SampleParametersDTO.GenderDTO gender = sampleParameters.getGender();
        if (gender != null) {
            StringBuilder sb = new StringBuilder(12);
            for (Integer conceptId : gender.getConceptIds()) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(conceptId);
            }
            if (gender.isOtherNonBinary()) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(-1);
            }
            sample.setGenderConceptIds(sb.toString());
        }
        sample.setCreatedBy(getCurrentUser());
        sample.setCreatedDate(new Date());

        log.info("Sampling {} elements for cohort {}", sampleParameters.getSize(), cohortDefinitionId);
        final List<SampleElement> elements = sampleElements(sampleParameters, sample, jdbcTemplate, source);

        if (elements.size() < sample.getSize()) {
            sample.setSize(elements.size());
        }

        transactionTemplate.execute((TransactionCallback<Void>) transactionStatus -> {
            log.debug("Saving {} sample elements for cohort {}", sample.getSize(), cohortDefinitionId);
            CohortSample updatedSample = sampleRepository.save(sample);
            insertSampledElements(source, jdbcTemplate, updatedSample.getId(), elements);

            return null;
        });

        return sampleToSampleDTO(sample, elements);
    }

    /** Convert a given sample with given elements to a DTO. */
    public CohortSampleDTO sampleToSampleDTO(CohortSample sample, List<SampleElement> elements) {
        CohortSampleDTO sampleDTO = new CohortSampleDTO();
        sampleDTO.setId(sample.getId());
        sampleDTO.setName(sample.getName());
        sampleDTO.setSize(sample.getSize());
        sampleDTO.setCohortDefinitionId(sample.getCohortDefinitionId());
        sampleDTO.setSourceId(sample.getSourceId());
        sampleDTO.setCreatedDate(sample.getCreatedDate());
        UserEntity createdBy = sample.getCreatedBy();
        if (createdBy != null) {
            UserDTO userDto = new UserDTO();
            userDto.setId(createdBy.getId());
            userDto.setLogin(createdBy.getLogin());
            userDto.setName(createdBy.getName());
            sampleDTO.setCreatedBy(userDto);
        }

        SampleParametersDTO.AgeMode ageMode = SampleParametersDTO.AgeMode.fromSerialName(sample.getAgeMode());
        if (ageMode != null) {
            SampleParametersDTO.AgeDTO age = new SampleParametersDTO.AgeDTO();
            age.setMode(ageMode);
            switch (ageMode) {
                case LESS_THAN:
                case LESS_THAN_OR_EQUAL:
                case EQUAL_TO:
                    age.setValue(sample.getAgeMax());
                    break;
                case GREATER_THAN:
                case GREATER_THAN_OR_EQUAL:
                    age.setValue(sample.getAgeMin());
                    break;
                case BETWEEN:
                case NOT_BETWEEN:
                    age.setMin(sample.getAgeMin());
                    age.setMax(sample.getAgeMax());
                    break;
            }
            sampleDTO.setAge(age);
        }
        if (sample.getGenderConceptIds() != null && !sample.getGenderConceptIds().isEmpty()) {
            List<Integer> conceptIds = Arrays.stream(sample.getGenderConceptIds().split(","))
                    .map(Integer::valueOf)
                    .collect(Collectors.toList());

            SampleParametersDTO.GenderDTO genderDto = new SampleParametersDTO.GenderDTO();

            if (conceptIds.remove(Integer.valueOf(-1))) {
                genderDto.setOtherNonBinary(true);
            }

            genderDto.setConceptIds(conceptIds);
            sampleDTO.setGender(genderDto);
        }

        sampleDTO.setElements(sampleElementToDTO(elements));
        return sampleDTO;
    }

    /** Convert given sample elements DTOs. */
    private List<SampleElementDTO> sampleElementToDTO(List<SampleElement> elements) {
        if (elements == null) {
            return null;
        }

        return elements.stream()
                .map(el -> {
                    SampleElementDTO elementDTO = new SampleElementDTO();
                    elementDTO.setRank(el.getRank());
                    elementDTO.setPersonId(el.getPersonId());
                    elementDTO.setAge(el.getAge());
                    elementDTO.setGenderConceptId(el.getGenderConceptId());
                    elementDTO.setRecordCount(el.getRecordCount());
                    return elementDTO;
                })
                .collect(Collectors.toList());
    }

    /** Insert elements that have been sampled. */
    private void insertSampledElements(Source source, JdbcTemplate jdbcTemplate, int sampleId, List<SampleElement> elements) {
        if (elements.isEmpty()) {
            return;
        }

        String[] parameters = new String[] { "results_schema" };
        String[] parameterValues = new String[] { source.getTableQualifier(SourceDaimon.DaimonType.Results) };
        String[] sqlParameters = new String[] { "cohortSampleId", "rank", "personId", "age", "genderConceptId" };

        String statement = null;
        List<Object[]> variables = new ArrayList<>(elements.size());
        for (SampleElement element : elements) {
            Object[] sqlValues = new Object[] {
                    sampleId,
                    element.getRank(),
                    element.getPersonId(),
                    element.getAge(),
                    element.getGenderConceptId() };

            PreparedStatementRenderer renderer = new PreparedStatementRenderer(source, "/resources/cohortsample/sql/insertSampleElement.sql", parameters, parameterValues, sqlParameters, sqlValues);

            if (statement == null) {
                statement = renderer.getSql();
            }

            variables.add(renderer.getOrderedParams());
        }

        jdbcTemplate.batchUpdate(statement, variables);
    }

    /** Sample elements based on parameters. */
    private List<SampleElement> sampleElements(SampleParametersDTO sampleParametersDTO, CohortSample sample, JdbcTemplate jdbcTemplate, Source source) {
        StringBuilder expressionBuilder = new StringBuilder();
        Map<String, Object> sqlVariables = new LinkedHashMap<>();

        sqlVariables.put("cohort_definition_id", sample.getCohortDefinitionId());

        SampleParametersDTO.AgeMode ageMode = SampleParametersDTO.AgeMode.fromSerialName(sample.getAgeMode());

        if (ageMode != null) {
            switch (ageMode) {
                case LESS_THAN:
                    expressionBuilder.append(" AND cast(year(c.cohort_start_date) - p.year_of_birth as int) < @age");
                    sqlVariables.put("age", sample.getAgeMax());
                    break;
                case LESS_THAN_OR_EQUAL:
                    expressionBuilder.append(" AND cast(year(c.cohort_start_date) - p.year_of_birth as int) <= @age");
                    sqlVariables.put("age", sample.getAgeMax());
                    break;
                case GREATER_THAN:
                    expressionBuilder.append(" AND cast(year(c.cohort_start_date) - p.year_of_birth as int) > @age");
                    sqlVariables.put("age", sample.getAgeMin());
                    break;
                case GREATER_THAN_OR_EQUAL:
                    expressionBuilder.append(" AND cast(year(c.cohort_start_date) - p.year_of_birth as int) >= @age");
                    sqlVariables.put("age", sample.getAgeMin());
                    break;
                case EQUAL_TO:
                    expressionBuilder.append(" AND cast(year(c.cohort_start_date) - p.year_of_birth as int) = @age");
                    sqlVariables.put("age", sample.getAgeMin());
                    break;
                case BETWEEN:
                    expressionBuilder.append(" AND cast(year(c.cohort_start_date) - p.year_of_birth as int) <= @age_max AND cast(year(c.cohort_start_date) - p.year_of_birth as int) >= @age_min");
                    sqlVariables.put("age_min", sample.getAgeMin());
                    sqlVariables.put("age_max", sample.getAgeMax());
                    break;
                case NOT_BETWEEN:
                    expressionBuilder.append(" AND cast(year(c.cohort_start_date) - p.year_of_birth as int) > @age_max AND cast(year(c.cohort_start_date) - p.year_of_birth as int) < @age_min");
                    sqlVariables.put("age_min", sample.getAgeMin());
                    sqlVariables.put("age_max", sample.getAgeMax());
                    break;
            }
        }

        SampleParametersDTO.GenderDTO gender = sampleParametersDTO.getGender();
        if (gender != null) {
            List<Integer> conceptIds = gender.getConceptIds();
            if (gender.isOtherNonBinary()) {
                if (conceptIds.size() == 0) {
                    expressionBuilder.append(" AND p.gender_concept_id NOT IN (")
                            .append(GENDER_MALE_CONCEPT_ID)
                            .append(',')
                            .append(GENDER_FEMALE_CONCEPT_ID)
                            .append(')');
                } else if (conceptIds.size() == 1) {
                    if (conceptIds.get(0) == GENDER_FEMALE_CONCEPT_ID) {
                        expressionBuilder.append(" AND p.gender_concept_id <> ")
                                .append(GENDER_MALE_CONCEPT_ID);

                    } else {
                        expressionBuilder.append(" AND p.gender_concept_id <> ")
                                .append(GENDER_FEMALE_CONCEPT_ID);
                    }
                }
                // else: all genders are selected, no where statement needed
            } else if (!conceptIds.isEmpty()) {
                expressionBuilder.append(" AND p.gender_concept_id IN (");
                for (int i = 0; i < conceptIds.size(); i++) {
                    if (i > 0) {
                        expressionBuilder.append(',');
                    }
                    expressionBuilder.append(conceptIds.get(i));
                }
                expressionBuilder.append(')');
            }
            // else: all genders are selected, no where statement needed
        }

        String[] parameterKeys = new String[] { "results_schema", "CDM_schema", "expression"};
        String[] parameterValues = new String[] {
                source.getTableQualifier(SourceDaimon.DaimonType.Results),
                source.getTableQualifier(SourceDaimon.DaimonType.CDM),
                expressionBuilder.toString() };
        String[] sqlVariableKeys = sqlVariables.keySet().toArray(new String[0]);
        Object[] sqlVariableValues = Stream.of(sqlVariableKeys)
                .map(sqlVariables::get)
                .toArray(Object[]::new);

        PreparedStatementRenderer renderer = new PreparedStatementRenderer(source, "/resources/cohortsample/sql/generateSample.sql",
                parameterKeys,
                parameterValues,
                sqlVariableKeys,
                sqlVariableValues);

        jdbcTemplate.setMaxRows(sample.getSize());

        return jdbcTemplate.query(renderer.getSql(), renderer.getOrderedParams(), (rs, rowNum) -> {
            SampleElement element = new SampleElement();
            element.setRank(rowNum);
            element.setAge(rs.getInt("age"));
            element.setGenderConceptId(rs.getInt("gender_concept_id"));
            element.setPersonId(rs.getLong("person_id"));
            return element;
        });
    }

    /** Delete a sample and its elements. */
    public void deleteSample(int cohortDefinitionId, Source source, int sampleId) {
        JdbcTemplate jdbcTemplate = getSourceJdbcTemplate(source);
        String resultsSchema = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        String sql = new PreparedStatementRenderer(
                        source,
                        "/resources/cohortsample/sql/deleteSampleElementsById.sql",
                        "results_schema",
                        resultsSchema,
                        "cohortSampleId",
                        sampleId).getSql();
        CohortSample sample = sampleRepository.findOne(sampleId);
        if (sample == null) {
            throw new NotFoundException("Sample with ID " + sampleId + " does not exist");
        }
        if (sample.getCohortDefinitionId() != cohortDefinitionId) {
            throw new BadRequestException("Cohort definition ID " + sample.getCohortDefinitionId() + " does not match provided cohort definition id " + cohortDefinitionId);
        }
        if (sample.getSourceId() != source.getId()) {
            throw new BadRequestException("Source " + sample.getSourceId() + " does not match provided source " + source.getId());
        }

        transactionTemplate.execute((TransactionCallback<Void>) transactionStatus -> {
            sampleRepository.delete(sampleId);
            jdbcTemplate.update(sql, sampleId);
            return null;
        });
    }

    /** Maps a SQL result to a sample element. */
    private static class CohortSampleElementRowMapper implements RowMapper<SampleElement> {
        @Override
        public SampleElement mapRow(ResultSet rs, int rowNum) throws SQLException {
            SampleElement sample = new SampleElement();
            sample.setRank(rs.getInt("rank"));
            sample.setSampleId(rs.getInt("cohort_sample_id"));
            sample.setPersonId(rs.getInt("person_id"));
            sample.setGenderConceptId(rs.getInt("gender_concept_id"));
            sample.setAge(rs.getInt("age"));
            sample.setRecordCount(rs.getInt("record_count"));
            return sample;
        }
    }
}
