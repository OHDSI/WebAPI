package org.ohdsi.webapi.cyclops.specification;

import java.math.BigDecimal;
import org.ohdsi.analysis.cyclops.design.*;
import org.ohdsi.webapi.RLangClassImpl;

public class ControlImpl extends RLangClassImpl implements Control {
  private Integer maxIterations = 1000;
  private BigDecimal tolerance = new BigDecimal(.000001);
  private ConvergenceTypeEnum convergenceType = ConvergenceTypeEnum.GRADIENT;
  private CvTypeEnum cvType = CvTypeEnum.AUTO;
  private BigDecimal fold = new BigDecimal(10);
  private BigDecimal lowerLimit = new BigDecimal(0.01);
  private BigDecimal upperLimit = new BigDecimal(20);
  private BigDecimal gridSteps = new BigDecimal(10);
  private BigDecimal cvRepetitions = new BigDecimal(1);
  private BigDecimal minCVData = new BigDecimal(100);
  private NoiseLevelEnum noiseLevel = NoiseLevelEnum.SILENT;
  private BigDecimal threads = new BigDecimal(1);
  private BigDecimal seed = null;
  private Boolean resetCoefficients = false;
  private BigDecimal startingVariance = new BigDecimal(-1);
  private Boolean useKKTSwindle = false;
  private BigDecimal tuneSwindle = new BigDecimal(10);
  private String selectorType = "auto";
  private BigDecimal initialBound = new BigDecimal(2);
  private BigDecimal maxBoundCount = new BigDecimal(5);
  private Boolean autoSearch = true;
  private String algorithm = "ccd";
  private String controlAttrClass = "cyclopsControl";

  /**
   * maximum iterations of Cyclops to attempt before returning a failed-to-converge error  
   * @return maxIterations
   **/
  @Override
  public Integer getMaxIterations() {
    return maxIterations;
  }

  public void setMaxIterations(Integer maxIterations) {
    this.maxIterations = maxIterations;
  }

  /**
   * maximum relative change in convergence criterion from successive iterations to achieve convergence 
   * @return tolerance
   **/
  @Override
  public BigDecimal getTolerance() {
    return tolerance;
  }

  public void setTolerance(BigDecimal tolerance) {
    this.tolerance = tolerance;
  }

  /**
   * name of convergence criterion to employ 
   * @return convergenceType
   **/
  @Override
  public ConvergenceTypeEnum getConvergenceType() {
    return convergenceType;
  }

  public void setConvergenceType(ConvergenceTypeEnum convergenceType) {
    this.convergenceType = convergenceType;
  }

  /**
   * name of cross validation search. Option \&quot;auto\&quot; selects an auto-search following BBR. Option \&quot;grid\&quot; selects a grid-search cross validation  
   * @return cvType
   **/
  @Override
  public CvTypeEnum getCvType() {
    return cvType;
  }

  public void setCvType(CvTypeEnum cvType) {
    this.cvType = cvType;
  }

  /**
   * Number of random folds to employ in cross validation 
   * @return fold
   **/
  @Override
  public BigDecimal getFold() {
    return fold;
  }

  public void setFold(BigDecimal fold) {
    this.fold = fold;
  }

  /**
   * Lower prior variance limit for grid-search 
   * @return lowerLimit
   **/
  @Override
  public BigDecimal getLowerLimit() {
    return lowerLimit;
  }

  public void setLowerLimit(BigDecimal lowerLimit) {
    this.lowerLimit = lowerLimit;
  }

  /**
   * Upper prior variance limit for grid-search 
   * @return upperLimit
   **/
  @Override
  public BigDecimal getUpperLimit() {
    return upperLimit;
  }

  public void setUpperLimit(BigDecimal upperLimit) {
    this.upperLimit = upperLimit;
  }

  /**
   * Number of steps in grid-search 
   * @return gridSteps
   **/
  @Override
  public BigDecimal getGridSteps() {
    return gridSteps;
  }

  public void setGridSteps(BigDecimal gridSteps) {
    this.gridSteps = gridSteps;
  }

  /**
   * Number of repetitions of X-fold cross validation 
   * @return cvRepetitions
   **/
  @Override
  public BigDecimal getCvRepetitions() {
    return cvRepetitions;
  }

  public void setCvRepetitions(BigDecimal cvRepetitions) {
    this.cvRepetitions = cvRepetitions;
  }

  /**
   * Minumim number of data for cross validation 
   * @return minCVData
   **/
  @Override
  public BigDecimal getMinCVData() {
    return minCVData;
  }

  public void setMinCVData(BigDecimal minCVData) {
    this.minCVData = minCVData;
  }

  /**
   * level of Cyclops screen output (\&quot;silent\&quot;, \&quot;quiet\&quot;, \&quot;noisy\&quot;) 
   * @return noiseLevel
   **/
  @Override
  public NoiseLevelEnum getNoiseLevel() {
    return noiseLevel;
  }

  public void setNoiseLevel(NoiseLevelEnum noiseLevel) {
    this.noiseLevel = noiseLevel;
  }

  /**
   * Specify number of CPU threads to employ in cross-validation; default &#x3D; 1 (auto &#x3D; -1) 
   * @return threads
   **/
  @Override
  public BigDecimal getThreads() {
    return threads;
  }

  public void setThreads(BigDecimal threads) {
    this.threads = threads;
  }

  /**
   * Specify random number generator seed. A null value sets seed via Sys.time. 
   * @return seed
   **/
  @Override
  public BigDecimal getSeed() {
    return seed;
  }

  public void setSeed(BigDecimal seed) {
    this.seed = seed;
  }

  /**
   * Reset all coefficients to 0 between model fits under cross-validation 
   * @return resetCoefficients
   **/
  @Override
  public Boolean getResetCoefficients() {
    return resetCoefficients;
  }

  public void setResetCoefficients(Boolean resetCoefficients) {
    this.resetCoefficients = resetCoefficients;
  }

  /**
   * Starting variance for auto-search cross-validation; default &#x3D; -1 (use estimate based on data) 
   * @return startingVariance
   **/
  @Override
  public BigDecimal getStartingVariance() {
    return startingVariance;
  }

  public void setStartingVariance(BigDecimal startingVariance) {
    this.startingVariance = startingVariance;
  }

  /**
   * Use the Karush-Kuhn-Tucker conditions to limit search 
   * @return useKKTSwindle
   **/
  @Override
  public Boolean getUseKKTSwindle() {
    return useKKTSwindle;
  }

  public void setUseKKTSwindle(Boolean useKKTSwindle) {
    this.useKKTSwindle = useKKTSwindle;
  }

  /**
   * Size multiplier for active set 
   * @return tuneSwindle
   **/
  @Override
  public BigDecimal getTuneSwindle() {
    return tuneSwindle;
  }

  public void setTuneSwindle(BigDecimal tuneSwindle) {
    this.tuneSwindle = tuneSwindle;
  }

  /**
   * name of exchangeable sampling unit. Option \&quot;byPid\&quot; selects entire strata. Option \&quot;byRow\&quot; selects single rows. If set to \&quot;auto\&quot;, \&quot;byRow\&quot; will be used for all models except conditional models where the average number of rows per stratum is smaller than the number of strata. 
   * @return selectorType
   **/
  @Override
  public String getSelectorType() {
    return selectorType;
  }

  public void setSelectorType(String selectorType) {
    this.selectorType = selectorType;
  }

  /**
   * Starting trust-region size 
   * @return initialBound
   **/
  @Override
  public BigDecimal getInitialBound() {
    return initialBound;
  }

  public void setInitialBound(BigDecimal initialBound) {
    this.initialBound = initialBound;
  }

  /**
   * Maximum number of tries to decrease initial trust-region size 
   * @return maxBoundCount
   **/
  @Override
  public BigDecimal getMaxBoundCount() {
    return maxBoundCount;
  }

  public void setMaxBoundCount(BigDecimal maxBoundCount) {
    this.maxBoundCount = maxBoundCount;
  }

  /**
   * The auto search setting 
   * @return autoSearch
   **/
  @Override
  public Boolean getAutoSearch() {
    return autoSearch;
  }

  public void setAutoSearch(Boolean autoSearch) {
    this.autoSearch = autoSearch;
  }

  /**
   * The algorithm setting 
   * @return algorithm
   **/
  @Override
  public String getAlgorithm() {
    return algorithm;
  }

  public void setAlgorithm(String algorithm) {
    this.algorithm = algorithm;
  }

  /**
   * Get controlAttrClass
   * @return controlAttrClass
   **/
  @Override
  public String getAttrClass() {
    return controlAttrClass;
  }

  @Override
  public void setAttrClass(String attrClass) {
    this.controlAttrClass = attrClass;
  }
}
