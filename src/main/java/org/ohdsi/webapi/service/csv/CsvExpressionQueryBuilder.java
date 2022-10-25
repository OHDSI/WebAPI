package org.ohdsi.webapi.service.csv;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;

import java.util.ArrayList;
import java.util.Iterator;

public class CsvExpressionQueryBuilder {

    final String conceptSetIncludeTemplate = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/conceptSetInclude.sql");
    final String conceptSetQueryTemplate = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getConceptByCodeAndVocabulary.sql");

    private ArrayList<String> getConceptColumns(ArrayList<Concept> concepts) {
        ArrayList<String> conceptColumnsList = new ArrayList();
        Iterator iter = concepts.iterator();

        while (iter.hasNext()) {
            Concept concept = (Concept) iter.next();
            String conceptColumns = "(" + String.join(", ", "'" + concept.conceptCode + "'", "'" + concept.vocabularyId + "'") + ")";
            conceptColumnsList.add(conceptColumns);
        }

        return conceptColumnsList;
    }

    private String buildConceptSetQuery(ArrayList<Concept> concepts) {
        if (concepts.size() == 0) {
            return "select concept_id from @vocabulary_database_schema.CONCEPT where 0=1";
        } else {
            final ArrayList<String> queries = new ArrayList();
            if (concepts.size() > 0) {
                queries.add(StringUtils.replace(conceptSetQueryTemplate, "@conceptColumns", StringUtils.join(this.getConceptColumns(concepts), ", ")));
            }

            return StringUtils.join(queries, "UNION");
        }
    }

    public String buildExpressionQuery(ConceptSetExpression expression) {
        final ArrayList<Concept> includeConcepts = new ArrayList();
        final ConceptSetExpression.ConceptSetItem[] concepts = expression.items;

        for (int i = 0; i < concepts.length; ++i) {
            includeConcepts.add(concepts[i].concept);
        }

        String conceptSetQuery = StringUtils.replace(conceptSetIncludeTemplate, "@includeQuery", this.buildConceptSetQuery(includeConcepts));

        return conceptSetQuery;
    }
}
