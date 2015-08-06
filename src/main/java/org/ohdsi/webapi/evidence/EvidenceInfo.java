/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.evidence;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 *
 * @author rkboyce
 */
public class EvidenceInfo {
    @JsonProperty("TITLE")
    public String title;
    
    @JsonProperty("DESCRIPTION")
    public String description;

    @JsonProperty("CONTRIBUTER")
    public String contributer;

    @JsonProperty("CREATOR")
    public String creator;

    @JsonProperty("CREATION_DATE")
    public Date creationDate;

    @JsonProperty("RIGHTS")
    public String rights;

    @JsonProperty("SOURCE")
    public String source;

    @JsonProperty("COVERAGE_START_DATE")
    public Date coverageStartDate;

    @JsonProperty("COVERAGE_END_DATE")
    public Date coverageEndDate;
}
