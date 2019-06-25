package org.ohdsi.webapi.prediction.specification;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import org.ohdsi.analysis.prediction.design.ModelSettings;
import org.ohdsi.analysis.prediction.design.ModelSettingsConst;

/**
 *
 * @author asena5
 */
@JsonSubTypes({
  @JsonSubTypes.Type(value = AdaBoostSettingsImpl.class, name = ModelSettingsConst.ADA_BOOST),
  @JsonSubTypes.Type(value = DecisionTreeSettingsImpl.class, name = ModelSettingsConst.DECISION_TREE),
  @JsonSubTypes.Type(value = GradientBoostingMachineSettingsImpl.class, name = ModelSettingsConst.GRADIENT_BOOSTING_MACHINE),
  @JsonSubTypes.Type(value = KNNSettingsImpl.class, name = ModelSettingsConst.KNN),
  @JsonSubTypes.Type(value = LassoLogisticRegressionSettingsImpl.class, name = ModelSettingsConst.LASSO_LOGISTIC_REGRESSION),
  @JsonSubTypes.Type(value = MLPSettingsImpl.class, name = ModelSettingsConst.MLP),
  @JsonSubTypes.Type(value = NaiveBayesSettingsImpl.class, name = ModelSettingsConst.NAIVE_BAYES),
  @JsonSubTypes.Type(value = RandomForestSettingsImpl.class, name = ModelSettingsConst.RANDOM_FOREST)
})
public class ModelSettingsImpl implements ModelSettings {
    
}
