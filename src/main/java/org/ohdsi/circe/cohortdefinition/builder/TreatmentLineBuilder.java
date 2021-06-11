package org.ohdsi.circe.cohortdefinition.builder;

import org.apache.commons.lang3.StringUtils;

import org.ohdsi.circe.cohortdefinition.TreatmentLine;
import org.ohdsi.circe.cohortdefinition.builders.CriteriaColumn;
import org.ohdsi.circe.cohortdefinition.builders.CriteriaSqlBuilder;
import org.ohdsi.circe.helper.ResourceHelper;

import java.util.*;

import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildDateRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildNumericRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.getConceptIdsFromConcepts;

public class TreatmentLineBuilder<T extends TreatmentLine> extends CriteriaSqlBuilder<T> {
    private final static String TREATMENT_LINE_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/treatmentLine.sql");

    // default columns are those that are specified in the template, and dont' need to be added if specifeid in 'additionalColumns'
    private final Set<CriteriaColumn> DEFAULT_COLUMNS = new HashSet<>(Arrays.asList(CriteriaColumn.START_DATE, CriteriaColumn.END_DATE, CriteriaColumn.VISIT_ID));

    @Override
    protected Set<CriteriaColumn> getDefaultColumns() {
        return DEFAULT_COLUMNS;
    }

    @Override
    protected String getQueryTemplate() {
        return TREATMENT_LINE_TEMPLATE;
    }

    @Override
    protected String getTableColumnForCriteriaColumn(CriteriaColumn column) {
        switch (column) {
            case DOMAIN_CONCEPT:
                return "C.drug_concept_id";
            case ERA_OCCURRENCES:
                return "C.drug_exposure_count";
            case GAP_DAYS:
                return "C.gap_days";
            case DURATION:
                return "DATEDIFF(d,C.drug_era_start_date, C.drug_era_end_date)";
            default:
                throw new IllegalArgumentException("Invalid CriteriaColumn for Treatment Line:" + column.toString());
        }
    }

    @Override
    protected String embedCodesetClause(String query, T criteria) {

        String codesetClause = "";
        if (criteria.codesetId != null) {
            codesetClause = String.format("where tl.drug_concept_id in (SELECT concept_id from  #Codesets where codeset_id = %d)", criteria.codesetId);
        }
        return StringUtils.replace(query, "@codesetClause", codesetClause);
    }

    @Override
    protected String embedOrdinalExpression(String query, T criteria, List<String> whereClauses) {

        // first
        if (criteria.first != null && criteria.first == true) {
            whereClauses.add("C.ordinal = 1");
            query = StringUtils.replace(query, "@ordinalExpression", ", row_number() over (PARTITION BY tl.person_id ORDER BY tl.line_start_date, tl.treatment_line_id) as ordinal");
        } else {
            query = StringUtils.replace(query, "@ordinalExpression", "");
        }
        return query;
    }

    @Override
    protected List<String> resolveJoinClauses(T criteria) {

        ArrayList<String> joinClauses = new ArrayList<>();

        // join to PERSON
        if (criteria.age != null || (criteria.gender != null && criteria.gender.length > 0)) {
            joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
        }

        return joinClauses;
    }

    @Override
    protected List<String> resolveWhereClauses(T criteria) {

        List<String> whereClauses = new ArrayList<>();

        // treatmentLineStartDate
        if (criteria.treatmentLineStartDate != null) {
            whereClauses.add(buildDateRangeClause("C.line_start_date", criteria.treatmentLineStartDate));
        }

        // treatmentLineEndDate
        if (criteria.treatmentLineEndDate != null) {
            whereClauses.add(buildDateRangeClause("C.line_end_date", criteria.treatmentLineEndDate));
        }

        if (criteria.treatmentLineDrugEraStartDate != null) {
            whereClauses.add(buildDateRangeClause("C.drug_era_start_date", criteria.treatmentLineDrugEraStartDate));
        }

        if (criteria.treatmentLineDrugEraEndDate != null) {
            whereClauses.add(buildDateRangeClause("C.drug_era_end_date", criteria.treatmentLineDrugEraEndDate));
        }

        if (criteria.treatmentLineNumber != null) {
            whereClauses.add(buildNumericRangeClause("C.line_number", criteria.treatmentLineNumber));
        }

        if (criteria.totalCycleNumber != null) {
            whereClauses.add(buildNumericRangeClause("C.total_cycle_number", criteria.totalCycleNumber));
        }

        if (criteria.drugExposureCount != null) {
            whereClauses.add(buildNumericRangeClause("C.drug_exposure_count", criteria.drugExposureCount));
        }

        // treatment line type
        if (criteria.treatmentLineType != null && criteria.treatmentLineType.length > 0) {
            whereClauses.add(String.format("P.treatment_type_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.treatmentLineType), ",")));
        }

        if (criteria.age != null) {
            whereClauses.add(buildNumericRangeClause("YEAR(C.line_start_date) - P.year_of_birth", criteria.age));
        }

        // gender
        if (criteria.gender != null && criteria.gender.length > 0) {
            whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.gender), ",")));
        }

        return whereClauses;
    }
}
