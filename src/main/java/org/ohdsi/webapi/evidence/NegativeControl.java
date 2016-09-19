package org.ohdsi.webapi.evidence;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ohdsi.webapi.source.Source;
import org.springframework.jdbc.core.JdbcTemplate;

public class NegativeControl {
        private String jobName;

	private Source source;
        
        private int conceptSetId;
        
        private String conceptSetName;

	private String sourceKey;

        private String[] conceptIds;
        
        private String conceptDomainId;
        
        private String targetDomainId;
        
        private JdbcTemplate jdbcTemplate;
        
        private String sourceDialect;
        
        private String ohdsiSchema;
        
	public String getSourceKey() {
		return sourceKey;
	}

	public void setSourceKey(String sourceKey) {
		this.sourceKey = sourceKey;
	}

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	/**
	 * @return the jobName
	 */
	public String getJobName() {
		return jobName;
	}

	/**
	 * @param jobName the jobName to set
	 */
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

        /**
	 * @return the conceptIds
	 */
	public String[] getConceptIds() {
		return conceptIds;
	}

	/**
	 * @param conceptIds the conceptIds to set
	 */
	public void setConceptIds(String[] conceptIds) {
		this.conceptIds = conceptIds;
	}
        
	@Override
	public String toString() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(this);
		} catch (Exception e) {}

		return super.toString();

	}    

    /**
     * @return the conceptSetId
     */
    public int getConceptSetId() {
        return conceptSetId;
    }

    /**
     * @return the conceptSetName
     */
    public String getConceptSetName() {
        return conceptSetName;
    }

    /**
     * @param conceptSetId the conceptSetId to set
     */
    public void setConceptSetId(int conceptSetId) {
        this.conceptSetId = conceptSetId;
    }

    /**
     * @param conceptSetName the conceptSetName to set
     */
    public void setConceptSetName(String conceptSetName) {
        this.conceptSetName = conceptSetName;
    }

    /**
     * @return the conceptDomainId
     */
    public String getConceptDomainId() {
        return conceptDomainId;
    }

    /**
     * @return the targetDomainId
     */
    public String getTargetDomainId() {
        return targetDomainId;
    }

    /**
     * @param conceptDomainId the conceptDomainId to set
     */
    public void setConceptDomainId(String conceptDomainId) {
        this.conceptDomainId = conceptDomainId;
    }

    /**
     * @param targetDomainId the targetDomainId to set
     */
    public void setTargetDomainId(String targetDomainId) {
        this.targetDomainId = targetDomainId;
    }

    /**
     * @return the jdbcTemplate
     */
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    /**
     * @param jdbcTemplate the jdbcTemplate to set
     */
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
}
