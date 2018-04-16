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
    @JsonProperty("title")
    public String title;
    
    @JsonProperty("description")
    public String description;
		
    @JsonProperty("provenance")
    public String provenance;

    @JsonProperty("contributor")
    public String contributor;

    @JsonProperty("contactName")
    public String contactName;

    @JsonProperty("creationDate")
    public Date creationDate;

    @JsonProperty("coverageStartDate")
    public Date coverageStartDate;

    @JsonProperty("coverageEndDate")
    public Date coverageEndDate;
		
    @JsonProperty("versionIdentifier")
    public String versionIdentifier;		
}
