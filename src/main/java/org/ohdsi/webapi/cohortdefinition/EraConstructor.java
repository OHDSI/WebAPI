
package org.ohdsi.webapi.cohortdefinition;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EraConstructor {
	
	@JsonProperty("ConstructorCollapseType")
	public CollapseType constructor = new CollapseType();
	
}
