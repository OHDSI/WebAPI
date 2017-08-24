
package org.ohdsi.webapi.cohortdefinition;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EraConstructorSettings {
	
	@JsonProperty("ConstructorCollapseType")
	public CollapseType constructor = new CollapseType();
	
	@JsonProperty("EraPad")
	public int eraPad = 0;
	
}
