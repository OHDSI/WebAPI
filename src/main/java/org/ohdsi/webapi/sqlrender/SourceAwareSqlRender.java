package org.ohdsi.webapi.sqlrender;

import org.apache.commons.lang3.ArrayUtils;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.util.SourceUtils;
import org.springframework.stereotype.Component;

import static org.ohdsi.webapi.Constants.Params.*;

@Component
public class SourceAwareSqlRender {
    private static final String[] DAIMONS = {RESULTS_DATABASE_SCHEMA, CDM_DATABASE_SCHEMA, TEMP_DATABASE_SCHEMA, VOCABULARY_DATABASE_SCHEMA};
    private final SourceService sourceService;

    public SourceAwareSqlRender(SourceService sourceService) {
        this.sourceService = sourceService;
    }

    public String renderSql(int sourceId, String sql, String paramater, String value) {
        return renderSql(sourceId, sql, new String[]{paramater}, new String[]{value});
    }

    public String renderSql(int sourceId, String sql, String[] parameters, String[] values) {
        final Source source = sourceService.findBySourceId(sourceId);
        if(source == null) {
            throw new IllegalArgumentException("wrong source: " + sourceId);
        }
        final String resultsQualifier = SourceUtils.getResultsQualifier(source);
        final String cdmQualifier = SourceUtils.getCdmQualifier(source);
        final String tempQualifier = SourceUtils.getTempQualifier(source, resultsQualifier);
        final String vocabularyQualifier = SourceUtils.getVocabularyQualifier(source);
        return SqlRender.renderSql(sql, ArrayUtils.addAll(parameters, DAIMONS), ArrayUtils.addAll(values, resultsQualifier, cdmQualifier, tempQualifier, vocabularyQualifier));
    }
}
