package org.ohdsi.webapi.prediction.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class RunPlpArgs {
  @JsonProperty("minCovariateFraction")
  private Float minCovariateFraction = 0.001f;

  @JsonProperty("normalizeData")
  private Boolean normalizeData = true;

  /**
   * Either &#x27;person&#x27; or &#x27;time&#x27; specifying the type of evaluation used. &#x27;time&#x27; find the date where testFraction of patients had an index after the date and assigns patients with an index prior to this date into the training set and post the date into the test set &#x27;person&#x27; splits the data into test (1-testFraction of the data) and train (validationFraction of the data) sets.  The split is stratified by the class label. 
   */
  public enum TestSplitEnum {
    TIME("time"),
    
    PERSON("person");

    private String value;

    TestSplitEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static TestSplitEnum fromValue(String text) {
      for (TestSplitEnum b : TestSplitEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("testSplit")
  private TestSplitEnum testSplit = TestSplitEnum.TIME;

  @JsonProperty("testFraction")
  private Float testFraction = 0.25f;

  @JsonProperty("splitSeed")
  private Float splitSeed = null;

  @JsonProperty("nfold")
  private Integer nfold = 3;

  public RunPlpArgs minCovariateFraction(Float minCovariateFraction) {
    this.minCovariateFraction = minCovariateFraction;
    return this;
  }

  /**
   * The minimum fraction of target population who must have a covariate for it to be included in the model training 
   * @return minCovariateFraction
   **/
  @JsonProperty("minCovariateFraction")
  public Float getMinCovariateFraction() {
    return minCovariateFraction;
  }

  public void setMinCovariateFraction(Float minCovariateFraction) {
    this.minCovariateFraction = minCovariateFraction;
  }

  public RunPlpArgs normalizeData(Boolean normalizeData) {
    this.normalizeData = normalizeData;
    return this;
  }

  /**
   * Whether to normalise the covariates before training 
   * @return normalizeData
   **/
  @JsonProperty("normalizeData")
  public Boolean isisNormalizeData() {
    return normalizeData;
  }

  public void setNormalizeData(Boolean normalizeData) {
    this.normalizeData = normalizeData;
  }

  public RunPlpArgs testSplit(TestSplitEnum testSplit) {
    this.testSplit = testSplit;
    return this;
  }

  /**
   * Either &#x27;person&#x27; or &#x27;time&#x27; specifying the type of evaluation used. &#x27;time&#x27; find the date where testFraction of patients had an index after the date and assigns patients with an index prior to this date into the training set and post the date into the test set &#x27;person&#x27; splits the data into test (1-testFraction of the data) and train (validationFraction of the data) sets.  The split is stratified by the class label. 
   * @return testSplit
   **/
  @JsonProperty("testSplit")
  public TestSplitEnum getTestSplit() {
    return testSplit;
  }

  public void setTestSplit(TestSplitEnum testSplit) {
    this.testSplit = testSplit;
  }

  public RunPlpArgs testFraction(Float testFraction) {
    this.testFraction = testFraction;
    return this;
  }

  /**
   * The fraction of the data to be used as the test set in the patient split evaluation 
   * @return testFraction
   **/
  @JsonProperty("testFraction")
  public Float getTestFraction() {
    return testFraction;
  }

  public void setTestFraction(Float testFraction) {
    this.testFraction = testFraction;
  }

  public RunPlpArgs splitSeed(Float splitSeed) {
    this.splitSeed = splitSeed;
    return this;
  }

  /**
   * The seed used to split the test/train set when using a person type testSplit 
   * @return splitSeed
   **/
  @JsonProperty("splitSeed")
  public Float getSplitSeed() {
    return splitSeed;
  }

  public void setSplitSeed(Float splitSeed) {
    this.splitSeed = splitSeed;
  }

  public RunPlpArgs nfold(Integer nfold) {
    this.nfold = nfold;
    return this;
  }

  /**
   * The number of folds used in the cross validation 
   * @return nfold
   **/
  @JsonProperty("nfold")
  public Integer getNfold() {
    return nfold;
  }

  public void setNfold(Integer nfold) {
    this.nfold = nfold;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RunPlpArgs runPlpArgs = (RunPlpArgs) o;
    return Objects.equals(this.minCovariateFraction, runPlpArgs.minCovariateFraction) &&
        Objects.equals(this.normalizeData, runPlpArgs.normalizeData) &&
        Objects.equals(this.testSplit, runPlpArgs.testSplit) &&
        Objects.equals(this.testFraction, runPlpArgs.testFraction) &&
        Objects.equals(this.splitSeed, runPlpArgs.splitSeed) &&
        Objects.equals(this.nfold, runPlpArgs.nfold);
  }

  @Override
  public int hashCode() {
    return Objects.hash(minCovariateFraction, normalizeData, testSplit, testFraction, splitSeed, nfold);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RunPlpArgs {\n");
    
    sb.append("    minCovariateFraction: ").append(toIndentedString(minCovariateFraction)).append("\n");
    sb.append("    normalizeData: ").append(toIndentedString(normalizeData)).append("\n");
    sb.append("    testSplit: ").append(toIndentedString(testSplit)).append("\n");
    sb.append("    testFraction: ").append(toIndentedString(testFraction)).append("\n");
    sb.append("    splitSeed: ").append(toIndentedString(splitSeed)).append("\n");
    sb.append("    nfold: ").append(toIndentedString(nfold)).append("\n");
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
