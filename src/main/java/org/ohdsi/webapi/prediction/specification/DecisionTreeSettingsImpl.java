package org.ohdsi.webapi.prediction.specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import org.ohdsi.analysis.prediction.design.ClassWeightEnum;
import org.ohdsi.analysis.prediction.design.DecisionTreeSettings;

/**
 * Specification for a Decision Tree Model
 */
public class DecisionTreeSettingsImpl extends SeedSettingsImpl implements DecisionTreeSettings {

    private List<Integer> maxDepth = new ArrayList<>(Arrays.asList(10));
    private List<Integer> minSamplesSplit = new ArrayList<>(Arrays.asList(2));
    private List<Integer> minSamplesLeaf = new ArrayList<>(Arrays.asList(10));
    private List<BigDecimal> minImpurityDecrease = new ArrayList<>(Arrays.asList(BigDecimal.valueOf(1.0E-7f)));
    private List<ClassWeightEnum> classWeight = new ArrayList<>(Arrays.asList(ClassWeightEnum.NONE));
    private Boolean plot = false;

    /**
     * The maximum depth of the tree
     *
     * @return maxDepth
   *
     */
    @Override
    public List<Integer> getMaxDepth() {
        return maxDepth;
    }

    /**
     *
     * @param maxDepth
     */
    public void setMaxDepth(Object maxDepth) {
        if (maxDepth != null) {
            if (maxDepth instanceof ArrayList) {
                this.maxDepth = (ArrayList<Integer>) maxDepth;
            } else if (maxDepth instanceof Integer) {
                this.maxDepth = new ArrayList<>(Arrays.asList((Integer) maxDepth));
            } else {
                throw new InputMismatchException("Expected ArrayList<Integer> or Integer");
            }
        } else {
            this.maxDepth = null;
        }
    }

    /**
     * The minimum samples per split
     *
     * @return minSamplesSplit
   *
     */
    @Override
    public List<Integer> getMinSamplesSplit() {
        return minSamplesSplit;
    }

    /**
     *
     * @param minSamplesSplit
     */
    public void setMinSamplesSplit(Object minSamplesSplit) {
        if (minSamplesSplit != null) {
            if (minSamplesSplit instanceof ArrayList) {
                this.minSamplesSplit = (ArrayList<Integer>) minSamplesSplit;
            } else if (minSamplesSplit instanceof Integer) {
                this.minSamplesSplit = new ArrayList<>(Arrays.asList((Integer) minSamplesSplit));
            } else {
                throw new InputMismatchException("Expected ArrayList<Integer> or Integer");
            }
        } else {
            this.minSamplesSplit = null;
        }
    }

    /**
     * The minimum number of samples per leaf
     *
     * @return minSampleLeaf
   *
     */
    @Override
    public List<Integer> getMinSamplesLeaf() {
        return minSamplesLeaf;
    }

    /**
     *
     * @param minSamplesLeaf
     */
    public void setMinSamplesLeaf(Object minSamplesLeaf) {
        if (minSamplesLeaf != null) {
            if (minSamplesLeaf instanceof ArrayList) {
                this.minSamplesLeaf = (ArrayList<Integer>) minSamplesLeaf;
            } else if (minSamplesLeaf instanceof Integer) {
                this.minSamplesLeaf = new ArrayList<>(Arrays.asList((Integer) minSamplesLeaf));
            } else {
                throw new InputMismatchException("Expected ArrayList<Integer> or Integer");
            }
        } else {
            this.minSamplesLeaf = null;
        }
    }

    /**
     * Threshold for early stopping in tree growth. A node will split if its
     * impurity is above the threshold, otherwise it is a leaf.
     *
     * @return minImpurityDecrease
   *
     */
    @Override
    public List<BigDecimal> getMinImpurityDecrease() {
        return minImpurityDecrease;
    }

    /**
     *
     * @param minImpurityDecrease
     */
    public void setMinImpurityDecrease(Object minImpurityDecrease) {
        if (minImpurityDecrease != null) {
            if (minImpurityDecrease instanceof ArrayList) {
                this.minImpurityDecrease = (List<BigDecimal>) minImpurityDecrease;
            } else if (minImpurityDecrease instanceof Float) {
                this.minImpurityDecrease = new ArrayList<>(Arrays.asList(new BigDecimal((Float) minImpurityDecrease)));
            } else {
                throw new InputMismatchException("Expected ArrayList<BigDecimal> or Float");
            }
        } else {
            this.minImpurityDecrease = null;
        }
    }

    /**
     * Balance or None
     *
     * @return classWeight
   *
     */
    @Override
    public List<ClassWeightEnum> getClassWeight() {
        return classWeight;
    }

    /**
     *
     * @param classWeight
     */
    public void setClassWeight(List<ClassWeightEnum> classWeight) {
        this.classWeight = classWeight;
    }

    /**
     * Boolean whether to plot the tree (requires python pydotplus module)
     *
     * @return plot
   *
     */
    @Override
    public Boolean getPlot() {
        return plot;
    }

    /**
     *
     * @param plot
     */
    public void setPlot(Boolean plot) {
        this.plot = plot;
    }
}
