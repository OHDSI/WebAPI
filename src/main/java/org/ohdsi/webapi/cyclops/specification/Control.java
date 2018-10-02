package org.ohdsi.webapi.cyclops.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.math.BigDecimal;

public class Control {
  @JsonProperty("maxIterations")
  private Integer maxIterations = 1000;

  @JsonProperty("tolerance")
  private BigDecimal tolerance = new BigDecimal(1000000);

  /**
   * name of convergence criterion to employ 
   */
  public enum ConvergenceTypeEnum {
    GRADIENT("gradient"),
    
    MITTAL("mittal"),
    
    LANGE("lange");

    private String value;

    ConvergenceTypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static ConvergenceTypeEnum fromValue(String text) {
      for (ConvergenceTypeEnum b : ConvergenceTypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("convergenceType")
  private ConvergenceTypeEnum convergenceType = ConvergenceTypeEnum.GRADIENT;

  /**
   * name of cross validation search. Option \&quot;auto\&quot; selects an auto-search following BBR. Option \&quot;grid\&quot; selects a grid-search cross validation  
   */
  public enum CvTypeEnum {
    AUTO("auto"),
    
    GRID("grid");

    private String value;

    CvTypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static CvTypeEnum fromValue(String text) {
      for (CvTypeEnum b : CvTypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("cvType")
  private CvTypeEnum cvType = CvTypeEnum.AUTO;

  @JsonProperty("fold")
  private BigDecimal fold = new BigDecimal(10);

  @JsonProperty("lowerLimit")
  private BigDecimal lowerLimit = new BigDecimal(0.01);

  @JsonProperty("upperLimit")
  private BigDecimal upperLimit = new BigDecimal(20);

  @JsonProperty("gridSteps")
  private BigDecimal gridSteps = new BigDecimal(10);

  @JsonProperty("cvRepetitions")
  private BigDecimal cvRepetitions = new BigDecimal(1);

  @JsonProperty("minCVData")
  private BigDecimal minCVData = new BigDecimal(100);

  /**
   * level of Cyclops screen output (\&quot;silent\&quot;, \&quot;quiet\&quot;, \&quot;noisy\&quot;) 
   */
  public enum NoiseLevelEnum {
    SILENT("silent"),
    
    QUIET("quiet"),
    
    NOISY("noisy");

    private String value;

    NoiseLevelEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static NoiseLevelEnum fromValue(String text) {
      for (NoiseLevelEnum b : NoiseLevelEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("noiseLevel")
  private NoiseLevelEnum noiseLevel = NoiseLevelEnum.SILENT;

  @JsonProperty("threads")
  private BigDecimal threads = new BigDecimal(1);

  @JsonProperty("seed")
  private BigDecimal seed = null;

  @JsonProperty("resetCoefficients")
  private Boolean resetCoefficients = false;

  @JsonProperty("startingVariance")
  private BigDecimal startingVariance = new BigDecimal(-1);

  @JsonProperty("useKKTSwindle")
  private Boolean useKKTSwindle = false;

  @JsonProperty("tuneSwindle")
  private BigDecimal tuneSwindle = new BigDecimal(10);

  @JsonProperty("selectorType")
  private String selectorType = "auto";

  @JsonProperty("initialBound")
  private BigDecimal initialBound = new BigDecimal(2);

  @JsonProperty("maxBoundCount")
  private BigDecimal maxBoundCount = new BigDecimal(5);

  @JsonProperty("autoSearch")
  private Boolean autoSearch = true;

  @JsonProperty("algorithm")
  private String algorithm = "ccd";

  @JsonProperty("attr_class")
  private String attrClass = "cyclopsControl";

  public Control maxIterations(Integer maxIterations) {
    this.maxIterations = maxIterations;
    return this;
  }

  /**
   * maximum iterations of Cyclops to attempt before returning a failed-to-converge error  
   * @return maxIterations
   **/
  @JsonProperty("maxIterations")
  public Integer getMaxIterations() {
    return maxIterations;
  }

  public void setMaxIterations(Integer maxIterations) {
    this.maxIterations = maxIterations;
  }

  public Control tolerance(BigDecimal tolerance) {
    this.tolerance = tolerance;
    return this;
  }

  /**
   * maximum relative change in convergence criterion from successive iterations to achieve convergence 
   * @return tolerance
   **/
  @JsonProperty("tolerance")
  public BigDecimal getTolerance() {
    return tolerance;
  }

  public void setTolerance(BigDecimal tolerance) {
    this.tolerance = tolerance;
  }

  public Control convergenceType(ConvergenceTypeEnum convergenceType) {
    this.convergenceType = convergenceType;
    return this;
  }

  /**
   * name of convergence criterion to employ 
   * @return convergenceType
   **/
  @JsonProperty("convergenceType")
  public ConvergenceTypeEnum getConvergenceType() {
    return convergenceType;
  }

  public void setConvergenceType(ConvergenceTypeEnum convergenceType) {
    this.convergenceType = convergenceType;
  }

  public Control cvType(CvTypeEnum cvType) {
    this.cvType = cvType;
    return this;
  }

  /**
   * name of cross validation search. Option \&quot;auto\&quot; selects an auto-search following BBR. Option \&quot;grid\&quot; selects a grid-search cross validation  
   * @return cvType
   **/
  @JsonProperty("cvType")
  public CvTypeEnum getCvType() {
    return cvType;
  }

  public void setCvType(CvTypeEnum cvType) {
    this.cvType = cvType;
  }

  public Control fold(BigDecimal fold) {
    this.fold = fold;
    return this;
  }

  /**
   * Number of random folds to employ in cross validation 
   * @return fold
   **/
  @JsonProperty("fold")
  public BigDecimal getFold() {
    return fold;
  }

  public void setFold(BigDecimal fold) {
    this.fold = fold;
  }

  public Control lowerLimit(BigDecimal lowerLimit) {
    this.lowerLimit = lowerLimit;
    return this;
  }

  /**
   * Lower prior variance limit for grid-search 
   * @return lowerLimit
   **/
  @JsonProperty("lowerLimit")
  public BigDecimal getLowerLimit() {
    return lowerLimit;
  }

  public void setLowerLimit(BigDecimal lowerLimit) {
    this.lowerLimit = lowerLimit;
  }

  public Control upperLimit(BigDecimal upperLimit) {
    this.upperLimit = upperLimit;
    return this;
  }

  /**
   * Upper prior variance limit for grid-search 
   * @return upperLimit
   **/
  @JsonProperty("upperLimit")
  public BigDecimal getUpperLimit() {
    return upperLimit;
  }

  public void setUpperLimit(BigDecimal upperLimit) {
    this.upperLimit = upperLimit;
  }

  public Control gridSteps(BigDecimal gridSteps) {
    this.gridSteps = gridSteps;
    return this;
  }

  /**
   * Number of steps in grid-search 
   * @return gridSteps
   **/
  @JsonProperty("gridSteps")
  public BigDecimal getGridSteps() {
    return gridSteps;
  }

  public void setGridSteps(BigDecimal gridSteps) {
    this.gridSteps = gridSteps;
  }

  public Control cvRepetitions(BigDecimal cvRepetitions) {
    this.cvRepetitions = cvRepetitions;
    return this;
  }

  /**
   * Number of repetitions of X-fold cross validation 
   * @return cvRepetitions
   **/
  @JsonProperty("cvRepetitions")
  public BigDecimal getCvRepetitions() {
    return cvRepetitions;
  }

  public void setCvRepetitions(BigDecimal cvRepetitions) {
    this.cvRepetitions = cvRepetitions;
  }

  public Control minCVData(BigDecimal minCVData) {
    this.minCVData = minCVData;
    return this;
  }

  /**
   * Minumim number of data for cross validation 
   * @return minCVData
   **/
  @JsonProperty("minCVData")
  public BigDecimal getMinCVData() {
    return minCVData;
  }

  public void setMinCVData(BigDecimal minCVData) {
    this.minCVData = minCVData;
  }

  public Control noiseLevel(NoiseLevelEnum noiseLevel) {
    this.noiseLevel = noiseLevel;
    return this;
  }

  /**
   * level of Cyclops screen output (\&quot;silent\&quot;, \&quot;quiet\&quot;, \&quot;noisy\&quot;) 
   * @return noiseLevel
   **/
  @JsonProperty("noiseLevel")
  public NoiseLevelEnum getNoiseLevel() {
    return noiseLevel;
  }

  public void setNoiseLevel(NoiseLevelEnum noiseLevel) {
    this.noiseLevel = noiseLevel;
  }

  public Control threads(BigDecimal threads) {
    this.threads = threads;
    return this;
  }

  /**
   * Specify number of CPU threads to employ in cross-validation; default &#x3D; 1 (auto &#x3D; -1) 
   * @return threads
   **/
  @JsonProperty("threads")
  public BigDecimal getThreads() {
    return threads;
  }

  public void setThreads(BigDecimal threads) {
    this.threads = threads;
  }

  public Control seed(BigDecimal seed) {
    this.seed = seed;
    return this;
  }

  /**
   * Specify random number generator seed. A null value sets seed via Sys.time. 
   * @return seed
   **/
  @JsonProperty("seed")
  public BigDecimal getSeed() {
    return seed;
  }

  public void setSeed(BigDecimal seed) {
    this.seed = seed;
  }

  public Control resetCoefficients(Boolean resetCoefficients) {
    this.resetCoefficients = resetCoefficients;
    return this;
  }

  /**
   * Reset all coefficients to 0 between model fits under cross-validation 
   * @return resetCoefficients
   **/
  @JsonProperty("resetCoefficients")
  public Boolean isisResetCoefficients() {
    return resetCoefficients;
  }

  public void setResetCoefficients(Boolean resetCoefficients) {
    this.resetCoefficients = resetCoefficients;
  }

  public Control startingVariance(BigDecimal startingVariance) {
    this.startingVariance = startingVariance;
    return this;
  }

  /**
   * Starting variance for auto-search cross-validation; default &#x3D; -1 (use estimate based on data) 
   * @return startingVariance
   **/
  @JsonProperty("startingVariance")
  public BigDecimal getStartingVariance() {
    return startingVariance;
  }

  public void setStartingVariance(BigDecimal startingVariance) {
    this.startingVariance = startingVariance;
  }

  public Control useKKTSwindle(Boolean useKKTSwindle) {
    this.useKKTSwindle = useKKTSwindle;
    return this;
  }

  /**
   * Use the Karush-Kuhn-Tucker conditions to limit search 
   * @return useKKTSwindle
   **/
  @JsonProperty("useKKTSwindle")
  public Boolean isisUseKKTSwindle() {
    return useKKTSwindle;
  }

  public void setUseKKTSwindle(Boolean useKKTSwindle) {
    this.useKKTSwindle = useKKTSwindle;
  }

  public Control tuneSwindle(BigDecimal tuneSwindle) {
    this.tuneSwindle = tuneSwindle;
    return this;
  }

  /**
   * Size multiplier for active set 
   * @return tuneSwindle
   **/
  @JsonProperty("tuneSwindle")
  public BigDecimal getTuneSwindle() {
    return tuneSwindle;
  }

  public void setTuneSwindle(BigDecimal tuneSwindle) {
    this.tuneSwindle = tuneSwindle;
  }

  public Control selectorType(String selectorType) {
    this.selectorType = selectorType;
    return this;
  }

  /**
   * name of exchangeable sampling unit. Option \&quot;byPid\&quot; selects entire strata. Option \&quot;byRow\&quot; selects single rows. If set to \&quot;auto\&quot;, \&quot;byRow\&quot; will be used for all models except conditional models where the average number of rows per stratum is smaller than the number of strata. 
   * @return selectorType
   **/
  @JsonProperty("selectorType")
  public String getSelectorType() {
    return selectorType;
  }

  public void setSelectorType(String selectorType) {
    this.selectorType = selectorType;
  }

  public Control initialBound(BigDecimal initialBound) {
    this.initialBound = initialBound;
    return this;
  }

  /**
   * Starting trust-region size 
   * @return initialBound
   **/
  @JsonProperty("initialBound")
  public BigDecimal getInitialBound() {
    return initialBound;
  }

  public void setInitialBound(BigDecimal initialBound) {
    this.initialBound = initialBound;
  }

  public Control maxBoundCount(BigDecimal maxBoundCount) {
    this.maxBoundCount = maxBoundCount;
    return this;
  }

  /**
   * Maximum number of tries to decrease initial trust-region size 
   * @return maxBoundCount
   **/
  @JsonProperty("maxBoundCount")
  public BigDecimal getMaxBoundCount() {
    return maxBoundCount;
  }

  public void setMaxBoundCount(BigDecimal maxBoundCount) {
    this.maxBoundCount = maxBoundCount;
  }

  public Control autoSearch(Boolean autoSearch) {
    this.autoSearch = autoSearch;
    return this;
  }

  /**
   * The auto search setting 
   * @return autoSearch
   **/
  @JsonProperty("autoSearch")
  public Boolean isisAutoSearch() {
    return autoSearch;
  }

  public void setAutoSearch(Boolean autoSearch) {
    this.autoSearch = autoSearch;
  }

  public Control algorithm(String algorithm) {
    this.algorithm = algorithm;
    return this;
  }

  /**
   * The algorithm setting 
   * @return algorithm
   **/
  @JsonProperty("algorithm")
  public String getAlgorithm() {
    return algorithm;
  }

  public void setAlgorithm(String algorithm) {
    this.algorithm = algorithm;
  }

  public Control attrClass(String attrClass) {
    this.attrClass = attrClass;
    return this;
  }

  /**
   * Get attrClass
   * @return attrClass
   **/
  @JsonProperty("attr_class")
  public String getAttrClass() {
    return attrClass;
  }

  public void setAttrClass(String attrClass) {
    this.attrClass = attrClass;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Control control = (Control) o;
    return Objects.equals(this.maxIterations, control.maxIterations) &&
        Objects.equals(this.tolerance, control.tolerance) &&
        Objects.equals(this.convergenceType, control.convergenceType) &&
        Objects.equals(this.cvType, control.cvType) &&
        Objects.equals(this.fold, control.fold) &&
        Objects.equals(this.lowerLimit, control.lowerLimit) &&
        Objects.equals(this.upperLimit, control.upperLimit) &&
        Objects.equals(this.gridSteps, control.gridSteps) &&
        Objects.equals(this.cvRepetitions, control.cvRepetitions) &&
        Objects.equals(this.minCVData, control.minCVData) &&
        Objects.equals(this.noiseLevel, control.noiseLevel) &&
        Objects.equals(this.threads, control.threads) &&
        Objects.equals(this.seed, control.seed) &&
        Objects.equals(this.resetCoefficients, control.resetCoefficients) &&
        Objects.equals(this.startingVariance, control.startingVariance) &&
        Objects.equals(this.useKKTSwindle, control.useKKTSwindle) &&
        Objects.equals(this.tuneSwindle, control.tuneSwindle) &&
        Objects.equals(this.selectorType, control.selectorType) &&
        Objects.equals(this.initialBound, control.initialBound) &&
        Objects.equals(this.maxBoundCount, control.maxBoundCount) &&
        Objects.equals(this.autoSearch, control.autoSearch) &&
        Objects.equals(this.algorithm, control.algorithm) &&
        Objects.equals(this.attrClass, control.attrClass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(maxIterations, tolerance, convergenceType, cvType, fold, lowerLimit, upperLimit, gridSteps, cvRepetitions, minCVData, noiseLevel, threads, seed, resetCoefficients, startingVariance, useKKTSwindle, tuneSwindle, selectorType, initialBound, maxBoundCount, autoSearch, algorithm, attrClass);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Control {\n");
    
    sb.append("    maxIterations: ").append(toIndentedString(maxIterations)).append("\n");
    sb.append("    tolerance: ").append(toIndentedString(tolerance)).append("\n");
    sb.append("    convergenceType: ").append(toIndentedString(convergenceType)).append("\n");
    sb.append("    cvType: ").append(toIndentedString(cvType)).append("\n");
    sb.append("    fold: ").append(toIndentedString(fold)).append("\n");
    sb.append("    lowerLimit: ").append(toIndentedString(lowerLimit)).append("\n");
    sb.append("    upperLimit: ").append(toIndentedString(upperLimit)).append("\n");
    sb.append("    gridSteps: ").append(toIndentedString(gridSteps)).append("\n");
    sb.append("    cvRepetitions: ").append(toIndentedString(cvRepetitions)).append("\n");
    sb.append("    minCVData: ").append(toIndentedString(minCVData)).append("\n");
    sb.append("    noiseLevel: ").append(toIndentedString(noiseLevel)).append("\n");
    sb.append("    threads: ").append(toIndentedString(threads)).append("\n");
    sb.append("    seed: ").append(toIndentedString(seed)).append("\n");
    sb.append("    resetCoefficients: ").append(toIndentedString(resetCoefficients)).append("\n");
    sb.append("    startingVariance: ").append(toIndentedString(startingVariance)).append("\n");
    sb.append("    useKKTSwindle: ").append(toIndentedString(useKKTSwindle)).append("\n");
    sb.append("    tuneSwindle: ").append(toIndentedString(tuneSwindle)).append("\n");
    sb.append("    selectorType: ").append(toIndentedString(selectorType)).append("\n");
    sb.append("    initialBound: ").append(toIndentedString(initialBound)).append("\n");
    sb.append("    maxBoundCount: ").append(toIndentedString(maxBoundCount)).append("\n");
    sb.append("    autoSearch: ").append(toIndentedString(autoSearch)).append("\n");
    sb.append("    algorithm: ").append(toIndentedString(algorithm)).append("\n");
    sb.append("    attrClass: ").append(toIndentedString(attrClass)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
