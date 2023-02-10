package org.ohdsi.webapi.service.cscompare;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.circe.vocabulary.ConceptSetExpressionQueryBuilder;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.conceptset.ConceptSetComparison;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;

@Service
public class ConceptSetCompareService extends AbstractDaoService {
    private static final String TEMP_TABLE_CREATE_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/createConceptSetTempTable.sql");
    private static final String TEMP_TABLE_FILL_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/fillConceptSetTempTable.sql");
    private final String CONCEPT_SET_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getConceptByCodeAndVocabulary.sql");
    private final String COMPARE_STATEMENT = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/compareConceptSets.sql");
    private static final String TEMP_TABLE_NAME_TEMPLATE = "cs_code_%s";
    private static final int DEFAULT_BATCH_SIZE = 100;

    public static final RowMapper<ConceptSetComparison> CONCEPT_SET_COMPARISON_ROW_MAPPER = (rs, rowNum) -> {
        ConceptSetComparison csc = new ConceptSetComparison();
        csc.conceptId = rs.getLong("concept_id");
        csc.conceptIn1Only = rs.getLong("concept_in_1_only");
        csc.conceptIn2Only = rs.getLong("concept_in_2_only");
        csc.conceptIn1And2 = rs.getLong("concept_in_both_1_and_2");
        csc.conceptName = rs.getString("concept_name");
        csc.standardConcept = rs.getString("standard_concept");
        csc.invalidReason = rs.getString("invalid_reason");
        csc.conceptCode = rs.getString("concept_code");
        csc.domainId = rs.getString("domain_id");
        csc.vocabularyId = rs.getString("vocabulary_id");
        csc.validStartDate = rs.getDate("valid_start_date");
        csc.validEndDate = rs.getDate("valid_end_date");
        csc.conceptClassId = rs.getString("concept_class_id");
        return csc;
    };

    public Collection<ConceptSetComparison> compareConceptSets(final String sourceKey,
                                                               final CompareArbitraryDto dto) throws Exception {
        final ConceptSetExpression[] csExpressionList = dto.compareTargets;
        if (csExpressionList.length != 2) {
            throw new Exception("You must specify two concept set expressions in order to use this method.");
        }

        final Source source = getSourceRepository().findBySourceKey(sourceKey);
        final String vocabSchema = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

        final Function<JdbcTemplate, TransactionCallback<Collection<ConceptSetComparison>>> callbackFunction =
                jdbcTemplate -> (TransactionCallback<Collection<ConceptSetComparison>>) transactionStatus -> {
            try {
                final String csQuery1 = getQuery(csExpressionList[0], dto.types[0], source, jdbcTemplate);
                final String csQuery2 = getQuery(csExpressionList[1], dto.types[1], source, jdbcTemplate);

                // Insert the queries into the overall comparison script
                String sql = SqlRender.renderSql(COMPARE_STATEMENT, new String[]{"cs1_expression", "cs2_expression"}, new String[]{csQuery1, csQuery2});
                sql = SqlRender.renderSql(sql, new String[]{"vocabulary_database_schema"}, new String[]{vocabSchema});
                sql = SqlTranslate.translateSql(sql, source.getSourceDialect());

                // Execute the query
                return jdbcTemplate.query(sql, CONCEPT_SET_COMPARISON_ROW_MAPPER);
            } catch (Exception ex) {
                log.error("An error occurred during the comparing of concept sets", ex);
                throw ex;
            }
        };


        return executeInTransaction(source, callbackFunction);
    }

    private String getQuery(final ConceptSetExpression csExpression, final ExpressionType type,
                            final Source source, final JdbcTemplate jdbcTemplate) {
        if (type == ExpressionType.CONCEPT_NAME_CODE_AND_VOCABULARY_ID_ONLY) {
            final String tempTableName = createTempTable(source, jdbcTemplate);
            fillTable(tempTableName, csExpression, jdbcTemplate);
            return StringUtils.replace(CONCEPT_SET_QUERY_TEMPLATE, "@temp_table", tempTableName);
        } else {
            final ConceptSetExpressionQueryBuilder builder = new ConceptSetExpressionQueryBuilder();
            return builder.buildExpressionQuery(csExpression);
        }
    }

    private String createTempTable(final Source source, final JdbcTemplate jdbcTemplate) {
        final String tableName = String.format(TEMP_TABLE_NAME_TEMPLATE,
                StringUtils.replace(UUID.randomUUID().toString(), "-", ""));

        String createStatement = SqlRender.renderSql(TEMP_TABLE_CREATE_TEMPLATE, new String[]{"temp_table"}, new String[]{tableName});
        createStatement = SqlTranslate.translateSql(createStatement, source.getSourceDialect());

        jdbcTemplate.execute(createStatement);

        return tableName;
    }

    private void fillTable(final String tableName, final ConceptSetExpression expression, final JdbcTemplate jdbcTemplate) {
        if (expression.items.length > 0) {
            final String insertStatement = SqlRender.renderSql(TEMP_TABLE_FILL_TEMPLATE, new String[]{"temp_table"}, new String[]{tableName});
            jdbcTemplate.batchUpdate(insertStatement,
                    Arrays.asList(expression.items),
                    DEFAULT_BATCH_SIZE,
                    (PreparedStatement ps, ConceptSetExpression.ConceptSetItem item) -> {
                        ps.setString(1, item.concept.conceptCode);
                        ps.setString(2, item.concept.vocabularyId);
                    });

        }
    }
}