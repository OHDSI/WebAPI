/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.vocabulary;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author fdefalco
 */
public class VocabularyInfo {
    @JsonProperty("version")
    public String version;
    
    @JsonProperty("dialect")
    public String dialect;
}
