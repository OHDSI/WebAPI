package org.ohdsi.webapi.service;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.support.TransactionTemplate;

/**
 *
 */
public abstract class AbstractDaoService {
    
    protected final Log log = LogFactory.getLog(getClass());
    
    @Value("${datasource.cdm.database}")
    private String cdmDatabase;
    
    @Value("${datasource.cdm.schema}")
    private String cdmSchema;
    
    @Value("${datasource.ohdsi.schema}")
    private String ohdsiSchema;
    
    @Value("${datasource.dialect}")
    private String dialect;
    
    @Value("${datasource.dialect.source}")
    private String sourceDialect;
    
    @Value("${source.name}")
    private String sourceName;
    
    @Value("${cdm.version}")
    private String cdmVersion;
    
    @Autowired
    @Qualifier("cdmJdbcTemplate")
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    @Qualifier("ohdsiJdbcTemplate")
    private JdbcTemplate ohdsiJdbcTemplate;
    
    @Autowired
    private TransactionTemplate transactionTemplate;
    
    @Autowired
    private TransactionTemplate transactionTemplateRequiresNew;
    
    /**
     * if cdmDatabase is provided, return cdmDatabase.cdmSchema, else return cdmSchema.
     * 
     * @return the cdmSchema
     */
    public String getCdmSchema() {
        String schema = (cdmDatabase != null && cdmDatabase.length() > 0) ? cdmDatabase + "." : "";
        return schema + cdmSchema;
    }
    
    /**
     * @param cdmSchema the cdmSchema to set
     */
    public void setCdmSchema(String cdmSchema) {
        this.cdmSchema = cdmSchema;
    }
    
    /**
     * @return the ohdsiSchema
     */
    public String getOhdsiSchema() {
        return ohdsiSchema;
    }
    
    /**
     * @param ohdsiSchema the ohdsiSchema to set
     */
    public void setOhdsiSchema(String ohdsiSchema) {
        this.ohdsiSchema = ohdsiSchema;
    }
    
    /**
     * @return the dialect
     */
    public String getDialect() {
        return dialect;
    }
    
    /**
     * @param dialect the dialect to set
     */
    public void setDialect(String dialect) {
        this.dialect = dialect;
    }
    
    /**
     * Returns JdbcTemplate for CDM schema (potentially read-only). See {@link #ohdsiJdbcTemplate}
     * for JdbcTemplate connected to ohdsi/results DataSource.
     * 
     * @return the jdbcTemplate for the cdm schema
     */
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
    
    /**
     * @return the sourceDialect
     */
    public String getSourceDialect() {
        return sourceDialect;
    }
    
    /**
     * @param sourceDialect the sourceDialect to set
     */
    public void setSourceDialect(String sourceDialect) {
        this.sourceDialect = sourceDialect;
    }
    
    /**
     * @return the sourceName
     */
    public String getSourceName() {
        return sourceName;
    }
    
    /**
     * @param sourceName the sourceName to set
     */
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }
    
    /**
     * @return the cdmVersion
     */
    public String getCdmVersion() {
        return cdmVersion;
    }
    
    /**
     * @param cdmVersion the cdmVersion to set
     */
    public void setCdmVersion(String cdmVersion) {
        this.cdmVersion = cdmVersion;
    }
    
    protected List<Map<String, String>> genericResultSetLoader(String sql) {
        List<Map<String, String>> results = null;
        try {
            results = getJdbcTemplate().query(sql, new RowMapper<Map<String, String>>() {
                
                @Override
                public Map<String, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Map<String, String> result = new HashMap<String, String>();
                    ResultSetMetaData metaData = rs.getMetaData();
                    int colCount = metaData.getColumnCount();
                    for (int i = 1; i <= colCount; i++) {
                        String columnLabel = metaData.getColumnLabel(i);
                        String columnValue = String.valueOf(rs.getObject(i));
                        result.put(columnLabel, columnValue);
                    }
                    return result;
                }
                
            });
            
        } catch (Exception e) {
            log.error("error loading in result set", e);
        }
        return results;
    }
    
    /**
     * @return the transactionTemplate
     */
    public TransactionTemplate getTransactionTemplate() {
        return transactionTemplate;
    }
    
    /**
     * @return the transactionTemplateRequiresNew
     */
    public TransactionTemplate getTransactionTemplateRequiresNew() {
        return transactionTemplateRequiresNew;
    }
    
    /**
     * @param transactionTemplateRequiresNew the transactionTemplateRequiresNew to set
     */
    public void setTransactionTemplateRequiresNew(TransactionTemplate transactionTemplateRequiresNew) {
        this.transactionTemplateRequiresNew = transactionTemplateRequiresNew;
    }

    
    /**
     * @return the ohdsiJdbcTemplate
     */
    public JdbcTemplate getOhdsiJdbcTemplate() {
        return ohdsiJdbcTemplate;
    }
}
