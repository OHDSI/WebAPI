package org.ohdsi.webapi.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 */
public abstract class AbstractDaoService {
    
    protected final Log log = LogFactory.getLog(getClass());
    
    @Value("${datasource.cdm.schema}")
    private String cdmSchema;
    
    @Value("${datasource.ohdsi.schema}")
    private String ohdsiSchema;
    
    @Value("${datasource.dialect}")
    private String dialect;
    
    @Value("${datasource.dialect.source}")
    private String sourceDialect;
    
    @Value("${datasource.cohort.schema}")
    private String cohortSchema;
    
    @Value("${datasource.cohort.table}")
    private String cohortTable;
    
    @Value("${datasource.achilles.results.table}")
    private String achillesResultsTable;
    
    @Value("${datasource.achilles.results_dist.table}")
    private String achillesResultsDistTable;
    
    @Value("${datasource.heracles.results.table}")
    private String heraclesResultsTable;
    
    @Value("${datasource.heracles.results_dist.table}")
    private String heraclesResultsDistTable;
    
    @Value("${source.name}")
    private String sourceName;
    
    @Value("${cdm.version}")
    private String cdmVersion;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * @return the cdmSchema
     */
    public String getCdmSchema() {
        return cdmSchema;
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
     * @return the jdbcTemplate
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
	 * @return the cohortSchema
	 */
	public String getCohortSchema() {
		return cohortSchema;
	}

	/**
	 * @param cohortSchema the cohortSchema to set
	 */
	public void setCohortSchema(String cohortSchema) {
		this.cohortSchema = cohortSchema;
	}

	/**
	 * @return the cohortTable
	 */
	public String getCohortTable() {
		return cohortTable;
	}

	/**
	 * @param cohortTable the cohortTable to set
	 */
	public void setCohortTable(String cohortTable) {
		this.cohortTable = cohortTable;
	}

	/**
	 * @return the achillesResultsTable
	 */
	public String getAchillesResultsTable() {
		return achillesResultsTable;
	}

	/**
	 * @param achillesResultsTable the achillesResultsTable to set
	 */
	public void setAchillesResultsTable(String achillesResultsTable) {
		this.achillesResultsTable = achillesResultsTable;
	}

	/**
	 * @return the achillesResultsDistTable
	 */
	public String getAchillesResultsDistTable() {
		return achillesResultsDistTable;
	}

	/**
	 * @param achillesResultsDistTable the achillesResultsDistTable to set
	 */
	public void setAchillesResultsDistTable(String achillesResultsDistTable) {
		this.achillesResultsDistTable = achillesResultsDistTable;
	}

	/**
	 * @return the heraclesResultsTable
	 */
	public String getHeraclesResultsTable() {
		return heraclesResultsTable;
	}

	/**
	 * @param heraclesResultsTable the heraclesResultsTable to set
	 */
	public void setHeraclesResultsTable(String heraclesResultsTable) {
		this.heraclesResultsTable = heraclesResultsTable;
	}

	/**
	 * @return the heraclesResultsDistTable
	 */
	public String getHeraclesResultsDistTable() {
		return heraclesResultsDistTable;
	}

	/**
	 * @param heraclesResultsDistTable the heraclesResultsDistTable to set
	 */
	public void setHeraclesResultsDistTable(String heraclesResultsDistTable) {
		this.heraclesResultsDistTable = heraclesResultsDistTable;
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
}
