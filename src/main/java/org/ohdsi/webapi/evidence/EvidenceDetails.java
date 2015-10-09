/*
 * Copyright 2015 fdefalco.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ohdsi.webapi.evidence;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class EvidenceDetails {

	//metadata on annotation
	
	@JsonProperty("label")
	public String label;
	
	@JsonProperty("lastSavedOn")
	public String lastSavedOn;
	
	@JsonProperty("wasGeneratedBy")
	public String wasGeneratedBy;
	
	@JsonProperty("annotatedAt")
	public String annotatedAt;
	
	@JsonProperty("annotatedBy")
	public String annotatedBy;
	
	@JsonProperty("motivatedBy")
	public String motivatedBy;
	
	@JsonProperty("metaType")
	public String metaType;
	
	//Source(Target)
	
	@JsonProperty("target")
	public String target;
	
	@JsonProperty("studyType")
	public String studyType;
	
	@JsonProperty("sourceURL")
	public String sourceURL;
	
	@JsonProperty("text")
	public String text;
	
	//Tagging(multiple Body)
	
	@JsonProperty("bodyLabel")
	public String bodyLabel;
	
	@JsonProperty("description")
	public String description;
	
	@JsonProperty("tagType")
	public String tagType;
	
	@JsonProperty("ImedsDrug")
	public String ImedsDrug;
	
	@JsonProperty("ImedsHoi")
	public String ImedsHoi;
	
	@JsonProperty("predicateLabel")
	public String predicateLabel;
	
	//Selector
	
	@JsonProperty("selector")
	public String selector;
	
	@JsonProperty("splSection")
	public String splSection;
	
	@JsonProperty("exact")
	public String exact;
	
	@JsonProperty("prefix")
	public String prefix;
	
	@JsonProperty("postfix")
	public String postfix;

}
