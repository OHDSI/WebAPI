package org.ohdsi.webapi.estimation.specification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.ohdsi.analysis.estimation.design.NegativeControlExposureCohortExpression;

/**
 * The expression that defines the criteria for inclusion and duration of time for cohorts intended for use as negative control exposures. This model is still under desgin and is a placholder for now. 
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class NegativeControlExposureCohortExpressionImpl implements NegativeControlExposureCohortExpression {

}
