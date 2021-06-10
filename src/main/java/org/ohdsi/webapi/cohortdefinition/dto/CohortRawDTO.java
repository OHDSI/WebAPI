/*
 * Copyright 2019 cknoll1.
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
package org.ohdsi.webapi.cohortdefinition.dto;

import org.ohdsi.webapi.cohortdefinition.ExpressionType;

/**
 *
 * @author cknoll1
 */
public class CohortRawDTO extends CohortMetadataDTO {

	private String expression;
	private ExpressionType expressionType;

	public String getExpression() {
		return expression;
	}

	public void setExpression(final String expression) {
		this.expression = expression;
	}

	public ExpressionType getExpressionType() {
		return expressionType;
	}

	public void setExpressionType(final ExpressionType expressionType) {
		this.expressionType = expressionType;
	}
}
