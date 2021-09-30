package org.ohdsi.webapi.cyclops.specification;

import java.math.BigDecimal;
import java.util.InputMismatchException;
import org.ohdsi.analysis.cyclops.design.*;
import org.ohdsi.webapi.RLangClassImpl;

/**
 *
 * @author asena5
 */
public class ControlImpl extends RLangClassImpl implements Control {

    private Integer maxIterations = 1000;
    private BigDecimal tolerance = BigDecimal.valueOf(.000001);
    private ConvergenceTypeEnum convergenceType = ConvergenceTypeEnum.GRADIENT;
    private CvTypeEnum cvType = CvTypeEnum.AUTO;
    private Integer fold = 10;
    private BigDecimal lowerLimit = BigDecimal.valueOf(0.01);
    private BigDecimal upperLimit = new BigDecimal(20);
    private Integer gridSteps = 10;
    private Integer cvRepetitions = 1;
    private Integer minCVData = 100;
    private NoiseLevelEnum noiseLevel = NoiseLevelEnum.SILENT;
    private Integer threads = 1;
    private Integer seed = null;
    private Boolean resetCoefficients = false;
    private BigDecimal startingVariance = new BigDecimal(-1);
    private Boolean useKKTSwindle = false;
    private Integer tuneSwindle = 10;
    private SelectorTypeEnum selectorType = SelectorTypeEnum.AUTO;
    private BigDecimal initialBound = new BigDecimal(2);
    private Integer maxBoundCount = 5;
    private Boolean autoSearch = true;
    private AlgorithmTypeEnum algorithm = AlgorithmTypeEnum.CCD;
    private String controlAttrClass = "cyclopsControl";

    /**
     * maximum iterations of Cyclops to attempt before returning a
     * failed-to-converge error
     *
     * @return maxIterations
     *
     */
    @Override
    public Integer getMaxIterations() {
        return maxIterations;
    }

    /**
     *
     * @param maxIterations
     */
    public void setMaxIterations(Integer maxIterations) {
        this.maxIterations = maxIterations;
    }

    /**
     * maximum relative change in convergence criterion from successive
     * iterations to achieve convergence
     *
     * @return tolerance
     *
     */
    @Override
    public BigDecimal getTolerance() {
        return tolerance;
    }

    /**
     *
     * @param tolerance
     */
    public void setTolerance(BigDecimal tolerance) {
        this.tolerance = tolerance;
    }

    /**
     * name of convergence criterion to employ
     *
     * @return convergenceType
     *
     */
    @Override
    public ConvergenceTypeEnum getConvergenceType() {
        return convergenceType;
    }

    /**
     *
     * @param convergenceType
     */
    public void setConvergenceType(ConvergenceTypeEnum convergenceType) {
        this.convergenceType = convergenceType;
    }

    /**
     * name of cross validation search. Option \&quot;auto\&quot; selects an
     * auto-search following BBR. Option \&quot;grid\&quot; selects a
     * grid-search cross validation
     *
     * @return cvType
     *
     */
    @Override
    public CvTypeEnum getCvType() {
        return cvType;
    }

    /**
     *
     * @param cvType
     */
    public void setCvType(CvTypeEnum cvType) {
        this.cvType = cvType;
    }

    /**
     * Number of random folds to employ in cross validation
     *
     * @return fold
     *
     */
    @Override
    public Integer getFold() {
        return fold;
    }

    /**
     *
     * @param fold
     */
    public void setFold(Object fold) {
        if (fold != null) {
            if (fold instanceof Integer) {
                this.fold = (Integer) fold;
            } else if (fold instanceof BigDecimal) {
                this.fold = ((BigDecimal) fold).intValue();
            } else {
                throw new InputMismatchException("Expected Integer or BigDecimal");
            }
        } else {
            this.fold = null;
        }
    }

    /**
     * Lower prior variance limit for grid-search
     *
     * @return lowerLimit
     *
     */
    @Override
    public BigDecimal getLowerLimit() {
        return lowerLimit;
    }

    /**
     *
     * @param lowerLimit
     */
    public void setLowerLimit(BigDecimal lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    /**
     * Upper prior variance limit for grid-search
     *
     * @return upperLimit
     *
     */
    @Override
    public BigDecimal getUpperLimit() {
        return upperLimit;
    }

    /**
     *
     * @param upperLimit
     */
    public void setUpperLimit(BigDecimal upperLimit) {
        this.upperLimit = upperLimit;
    }

    /**
     * Number of steps in grid-search
     *
     * @return gridSteps
     *
     */
    @Override
    public Integer getGridSteps() {
        return gridSteps;
    }

    /**
     *
     * @param gridSteps
     */
    public void setGridSteps(Object gridSteps) {
        if (gridSteps != null) {
            if (gridSteps instanceof Integer) {
                this.gridSteps = (Integer) gridSteps;
            } else if (gridSteps instanceof BigDecimal) {
                this.gridSteps = ((BigDecimal) gridSteps).intValue();
            } else {
                throw new InputMismatchException("Expected Integer or BigDecimal");
            }
        } else {
            this.gridSteps = null;
        }
    }

    /*
    public void setGridSteps(BigDecimal gridSteps) {
        this.gridSteps = gridSteps.intValue();
    }
     */
    /**
     * Number of repetitions of X-fold cross validation
     *
     * @return cvRepetitions
     *
     */
    @Override
    public Integer getCvRepetitions() {
        return cvRepetitions;
    }

    /**
     *
     * @param cvRepetitions
     */
    public void setCvRepetitions(Object cvRepetitions) {
        if (cvRepetitions != null) {
            if (cvRepetitions instanceof Integer) {
                this.cvRepetitions = (Integer) cvRepetitions;
            } else if (cvRepetitions instanceof BigDecimal) {
                this.cvRepetitions = ((BigDecimal) cvRepetitions).intValue();
            } else {
                throw new InputMismatchException("Expected Integer or BigDecimal");
            }
        } else {
            this.cvRepetitions = null;
        }
    }

    /**
     * Minimum number of data for cross validation
     *
     * @return minCVData
     *
     */
    @Override
    public Integer getMinCVData() {
        return minCVData;
    }

    /**
     *
     * @param minCVData
     */
    public void setMinCVData(Object minCVData) {
        if (minCVData != null) {
            if (minCVData instanceof Integer) {
                this.minCVData = (Integer) minCVData;
            } else if (minCVData instanceof BigDecimal) {
                this.minCVData = ((BigDecimal) minCVData).intValue();
            } else {
                throw new InputMismatchException("Expected Integer or BigDecimal");
            }
        } else {
            this.minCVData = null;
        }
    }

    /**
     * level of Cyclops screen output (\&quot;silent\&quot;,
     * \&quot;quiet\&quot;, \&quot;noisy\&quot;)
     *
     * @return noiseLevel
     *
     */
    @Override
    public NoiseLevelEnum getNoiseLevel() {
        return noiseLevel;
    }

    /**
     *
     * @param noiseLevel
     */
    public void setNoiseLevel(NoiseLevelEnum noiseLevel) {
        this.noiseLevel = noiseLevel;
    }

    /**
     * Specify number of CPU threads to employ in cross-validation; default
     * &#x3D; 1 (auto &#x3D; -1)
     *
     * @return threads
     *
     */
    @Override
    public Integer getThreads() {
        return threads;
    }

    /**
     *
     * @param threads
     */
    public void setThreads(Object threads) {
        if (threads != null) {
            if (threads instanceof Integer) {
                this.threads = (Integer) threads;
            } else if (threads instanceof BigDecimal) {
                this.threads = ((BigDecimal) threads).intValue();
            } else {
                throw new InputMismatchException("Expected Integer or BigDecimal");
            }
        } else {
            this.threads = null;
        }
    }

    /**
     * Specify random number generator seed. A null value sets seed via
     * Sys.time.
     *
     * @return seed
     *
     */
    @Override
    public Integer getSeed() {
        return seed;
    }

    /**
     *
     * @param seed
     */
    public void setSeed(Object seed) {
        if (seed != null) {
            if (seed instanceof Integer) {
                this.seed = (Integer) seed;
            } else if (seed instanceof BigDecimal) {
                this.seed = ((BigDecimal) seed).intValue();
            } else {
                throw new InputMismatchException("Expected Integer or BigDecimal");
            }
        } else {
            this.seed = null;
        }
    }

    /**
     * Reset all coefficients to 0 between model fits under cross-validation
     *
     * @return resetCoefficients
     *
     */
    @Override
    public Boolean getResetCoefficients() {
        return resetCoefficients;
    }

    /**
     *
     * @param resetCoefficients
     */
    public void setResetCoefficients(Boolean resetCoefficients) {
        this.resetCoefficients = resetCoefficients;
    }

    /**
     * Starting variance for auto-search cross-validation; default &#x3D; -1
     * (use estimate based on data)
     *
     * @return startingVariance
     *
     */
    @Override
    public BigDecimal getStartingVariance() {
        return startingVariance;
    }

    /**
     *
     * @param startingVariance
     */
    public void setStartingVariance(BigDecimal startingVariance) {
        this.startingVariance = startingVariance;
    }

    /**
     * Use the Karush-Kuhn-Tucker conditions to limit search
     *
     * @return useKKTSwindle
     *
     */
    @Override
    public Boolean getUseKKTSwindle() {
        return useKKTSwindle;
    }

    /**
     *
     * @param useKKTSwindle
     */
    public void setUseKKTSwindle(Boolean useKKTSwindle) {
        this.useKKTSwindle = useKKTSwindle;
    }

    /**
     * Size multiplier for active set
     *
     * @return tuneSwindle
     *
     */
    @Override
    public Integer getTuneSwindle() {
        return tuneSwindle;
    }

    /**
     *
     * @param tuneSwindle
     */
    public void setTuneSwindle(Integer tuneSwindle) {
        this.tuneSwindle = tuneSwindle;
    }

    /**
     * name of exchangeable sampling unit. Option \&quot;byPid\&quot; selects
     * entire strata. Option \&quot;byRow\&quot; selects single rows. If set to
     * \&quot;auto\&quot;, \&quot;byRow\&quot; will be used for all models
     * except conditional models where the average number of rows per stratum is
     * smaller than the number of strata.
     *
     * @return selectorType
     *
     */
    @Override
    public SelectorTypeEnum getSelectorType() {
        return selectorType;
    }

    /**
     *
     * @param selectorType
     */
    public void setSelectorType(SelectorTypeEnum selectorType) {
        this.selectorType = selectorType;
    }

    /**
     *
     * @param selectorType
     */
    public void setSelectorType(String selectorType) {
        this.selectorType = SelectorTypeEnum.fromValue(selectorType);
    }

    /**
     * Starting trust-region size
     *
     * @return initialBound
     *
     */
    @Override
    public BigDecimal getInitialBound() {
        return initialBound;
    }

    /**
     *
     * @param initialBound
     */
    public void setInitialBound(BigDecimal initialBound) {
        this.initialBound = initialBound;
    }

    /**
     * Maximum number of tries to decrease initial trust-region size
     *
     * @return maxBoundCount
     *
     */
    @Override
    public Integer getMaxBoundCount() {
        return maxBoundCount;
    }

    /**
     *
     * @param maxBoundCount
     */
    public void setMaxBoundCount(Object maxBoundCount) {
        if (maxBoundCount != null) {
            if (maxBoundCount instanceof Integer) {
                this.maxBoundCount = (Integer) maxBoundCount;
            } else if (maxBoundCount instanceof BigDecimal) {
                this.maxBoundCount = ((BigDecimal) maxBoundCount).intValue();
            } else {
                throw new InputMismatchException("Expected Integer or BigDecimal");
            }
        } else {
            this.maxBoundCount = null;
        }
    }

    /**
     * The auto search setting
     *
     * @return autoSearch
     *
     */
    @Override
    public Boolean getAutoSearch() {
        return autoSearch;
    }

    /**
     *
     * @param autoSearch
     */
    public void setAutoSearch(Boolean autoSearch) {
        this.autoSearch = autoSearch;
    }

    /**
     * The algorithm setting
     *
     * @return algorithm
     *
     */
    @Override
    public AlgorithmTypeEnum getAlgorithm() {
        return algorithm;
    }

    /**
     *
     * @param algorithm
     */
    public void setAlgorithm(AlgorithmTypeEnum algorithm) {
        this.algorithm = algorithm;
    }

    /**
     *
     * @param algorithm
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm = AlgorithmTypeEnum.fromValue(algorithm);
    }

    /**
     * Get controlAttrClass
     *
     * @return controlAttrClass
     *
     */
    @Override
    public String getAttrClass() {
        return controlAttrClass;
    }

    /**
     *
     * @param attrClass
     */
    @Override
    public void setAttrClass(String attrClass) {
        this.controlAttrClass = attrClass;
    }
}
