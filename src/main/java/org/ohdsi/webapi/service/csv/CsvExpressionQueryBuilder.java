package org.ohdsi.webapi.service.csv;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class CsvExpressionQueryBuilder {
    private final String CONCEPT_COLUMN_TEMPLATE = "('%s','%s')";

    private final String conceptSetIncludeTemplate = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/conceptSetInclude.sql");
    private final String conceptSetQueryTemplate = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getConceptByCodeAndVocabulary.sql");

    public String buildExpressionQuery(final ConceptSetExpression expression) {
        final List<Concept> includeConcepts = Arrays.stream(expression.items)
                .map(item -> item.concept)
                .collect(Collectors.toList());

        return StringUtils.replace(conceptSetIncludeTemplate, "@includeQuery", this.buildConceptSetQuery(includeConcepts));
    }

    private String buildConceptSetQuery(final List<Concept> concepts) {
        if (concepts.size() == 0) {
            return "select concept_id from @vocabulary_database_schema.CONCEPT where 0=1";
        } else {
            return StringUtils.replace(conceptSetQueryTemplate, "@conceptColumns", StringUtils.join(this.getConceptColumns(concepts), ","));
        }
    }

    private List<String> getConceptColumns(final List<Concept> concepts) {
        return concepts.stream()
                .map(concept -> String.format(CONCEPT_COLUMN_TEMPLATE, concept.conceptCode, concept.vocabularyId))
                .collect(Collectors.toList());
    }
}
