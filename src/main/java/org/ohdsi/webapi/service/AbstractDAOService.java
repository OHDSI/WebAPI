/**
 * The contents of this file are subject to the Regenstrief Public License
 * Version 1.0 (the "License"); you may not use this file except in compliance with the License.
 * Please contact Regenstrief Institute if you would like to obtain a copy of the license.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) Regenstrief Institute.  All Rights Reserved.
 */
package org.ohdsi.webapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 */
public class AbstractDAOService {
    
    @Value("${datasource.cdm.schema}")
    private String cdmSchema;
    
    @Value("${datasource.ohdsi.schema}")
    private String ohdsiSchema;
    
    @Value("${datasource.dialect}")
    private String dialect;
    
    @Value("${datasource.dialect.source}")
    private String sourceDialect;
    
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
}
