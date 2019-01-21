package org.ohdsi.webapi.prediction.specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import org.ohdsi.analysis.prediction.design.AdaBoostSettings;

/**
 * Specification for a Ada Boost Model
 */
public class AdaBoostSettingsImpl extends SeedSettingsImpl implements AdaBoostSettings {

    private List<Integer> nEstimators = new ArrayList<>(Arrays.asList(50));
    private List<BigDecimal> learningRate = new ArrayList<>(Arrays.asList(new BigDecimal(1)));

    /**
     * The maximum number of estimators at which boosting is terminated
     *
     * @return nEstimators
   *
     */
    @Override
    public List<Integer> getNEstimators() {
        return nEstimators;
    }

    /**
     *
     * @param nEstimators
     */
    public void setNEstimators(Object nEstimators) {
        if (nEstimators != null) {
            if (nEstimators instanceof ArrayList) {
                this.nEstimators = (ArrayList<Integer>) nEstimators;
            } else if (nEstimators instanceof Integer) {
                this.nEstimators = new ArrayList<>(Arrays.asList((Integer) nEstimators));
            } else {
                throw new InputMismatchException("Expected ArrayList<Integer> or Integer");
            }
        } else {
            this.nEstimators = null;
        }
    }

    /**
     * Learning rate shrinks the contribution of each classifier by
     * learningRate. There is a trade-off between learningRate and nEstimators .
     *
     * @return learningRate
   *
     */
    @Override
    public List<BigDecimal> getLearningRate() {
        return learningRate;
    }

    /**
     * Set the learning rate
     *
     * @param learningRate
     */
    public void setLearningRate(Object learningRate) {
        if (learningRate != null) {
            if (learningRate instanceof ArrayList) {
                this.learningRate = (ArrayList<BigDecimal>) learningRate;
            } else if (learningRate instanceof Integer) {
                this.learningRate = new ArrayList<>(Arrays.asList(new BigDecimal((Integer) learningRate)));
            } else {
                throw new InputMismatchException("Expected ArrayList<BigDecimal> or Integer");
            }
        } else {
            this.learningRate = null;
        }
    }
}
