package org.ohdsi.webapi.evidence.negativecontrols;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ohdsi.webapi.source.Source;
import org.springframework.jdbc.core.JdbcTemplate;

public class NegativeControlTaskParameters {

    private String jobName;

    private Source source;

    private int conceptSetId;

    private String conceptSetName;

    private String sourceKey;

    private String[] conceptsOfInterest;

    private int csToInclude = 0;

    private String csToIncludeSQL = "";

    private int csToExclude = 0;

    private String csToExcludeSQL = "";

    private String conceptDomainId;

    private String outcomeOfInterest;

    private JdbcTemplate jdbcTemplate;

    private String sourceDialect;

    private String ohdsiSchema;

    private String translatedSchema;

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
     * @return the conceptsOfInterest
     */
    public String[] getConceptsOfInterest() {
        return conceptsOfInterest;
    }

    /**
     * @param conceptsOfInterest the conceptsOfInterest to set
     */
    public void setConceptsOfInterest(String[] conceptsOfInterest) {
        this.conceptsOfInterest = conceptsOfInterest;
    }

    @Override
    public String toString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this);
        } catch (Exception e) {
        }

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
     * @return the outcomeOfInterest
     */
    public String getOutcomeOfInterest() {
        return outcomeOfInterest;
    }

    /**
     * @param conceptDomainId the conceptDomainId to set
     */
    public void setConceptDomainId(String conceptDomainId) {
        this.conceptDomainId = conceptDomainId;
    }

    /**
     * @param outcomeOfInterest the outcomeOfInterest to set
     */
    public void setOutcomeOfInterest(String outcomeOfInterest) {
        this.outcomeOfInterest = outcomeOfInterest;
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

    /**
     * @return the csToInclude
     */
    public int getCsToInclude() {
        return csToInclude;
    }

    /**
     * @param csToInclude the csToInclude to set
     */
    public void setCsToInclude(int csToInclude) {
        this.csToInclude = csToInclude;
    }

    /**
     * @return the csToExclude
     */
    public int getCsToExclude() {
        return csToExclude;
    }

    /**
     * @param conceptsToExclude the csToExclude to set
     */
    public void setCsToExclude(int conceptsToExclude) {
        this.csToExclude = conceptsToExclude;
    }

    /**
     * @return the csToIncludeSQL
     */
    public String getCsToIncludeSQL() {
        return csToIncludeSQL;
    }

    /**
     * @param csToIncludeSQL the csToIncludeSQL to set
     */
    public void setCsToIncludeSQL(String csToIncludeSQL) {
        this.csToIncludeSQL = csToIncludeSQL;
    }

    /**
     * @return the csToExcludeSQL
     */
    public String getCsToExcludeSQL() {
        return csToExcludeSQL;
    }

    /**
     * @param csToExcludeSQL the csToExcludeSQL to set
     */
    public void setCsToExcludeSQL(String csToExcludeSQL) {
        this.csToExcludeSQL = csToExcludeSQL;
    }

    /**
     * @return the translatedSchema
     */
    public String getTranslatedSchema() {
        return translatedSchema;
    }

    /**
     * @param translatedSchema the translatedSchema to set
     */
    public void setTranslatedSchema(String translatedSchema) {
        this.translatedSchema = translatedSchema;
    }
}
