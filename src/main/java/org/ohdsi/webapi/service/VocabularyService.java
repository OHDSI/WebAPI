package org.ohdsi.webapi.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.helper.ResourceHelper;
import org.ohdsi.webapi.vocabulary.Vocabulary;
import org.ohdsi.webapi.vocabulary.VocabularyInfo;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

/**
 * @author fdefalco
 */
@Path("/vocabulary/")
@Component
public class VocabularyService extends AbstractDAOService {
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Vocabulary> getVocabularies() {
        String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getVocabularies.sql");
        sql_statement = SqlRender.renderSql(sql_statement, new String[] { "CDM_schema" }, new String[] { getCdmSchema() });
        sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", getDialect());
        
        return getJdbcTemplate().query(sql_statement, new RowMapper<Vocabulary>() {
            
            @Override
            public Vocabulary mapRow(final ResultSet resultSet, final int arg1) throws SQLException {
                final Vocabulary vocabulary = new Vocabulary();
                vocabulary.vocabularyId = resultSet.getString("VOCABULARY_ID");
                vocabulary.vocabularyName = resultSet.getString("VOCABULARY_NAME");
                vocabulary.vocabularyReference = resultSet.getString("VOCABULARY_REFERENCE");
                vocabulary.vocabularyVersion = resultSet.getString("VOCABULARY_VERSION");
                vocabulary.vocabularyConceptId = resultSet.getLong("VOCABULARY_CONCEPT_ID");
                return vocabulary;
            }
        });
    }
    
    @GET
    @Path("info")
    @Produces(MediaType.APPLICATION_JSON)
    public VocabularyInfo getInfo() {
        final VocabularyInfo info = new VocabularyInfo();
        
        String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getInfo.sql");
        info.dialect = getDialect();
        
        sql_statement = SqlRender.renderSql(sql_statement, new String[] { "CDM_schema" }, new String[] { getCdmSchema() });
        sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", getDialect());
        
        return getJdbcTemplate().queryForObject(sql_statement, new RowMapper<VocabularyInfo>() {
            
            @Override
            public VocabularyInfo mapRow(final ResultSet resultSet, final int arg1) throws SQLException {
                info.version = resultSet.getString("VOCABULARY_VERSION");
                return info;
            }
        });
    }
    

}
