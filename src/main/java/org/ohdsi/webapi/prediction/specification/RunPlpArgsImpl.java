package org.ohdsi.webapi.prediction.specification;

import org.ohdsi.analysis.prediction.design.RunPlpArgs;
import org.ohdsi.analysis.prediction.design.TestSplitEnum;

/**
 *
 * @author asena5
 */
public class RunPlpArgsImpl implements RunPlpArgs {
  private Float minCovariateFraction = 0.001f;
  private Boolean normalizeData = true;
  private TestSplitEnum testSplit = TestSplitEnum.TIME;
  private Float testFraction = 0.25f;
  private Float splitSeed = null;
  private Integer nfold = 3;

  /**
   * The minimum fraction of target population who must have a covariate for it to be included in the model training 
   * @return minCovariateFraction
   **/
  @Override
  public Float getMinCovariateFraction() {
    return minCovariateFraction;
  }

    /**
     *
     * @param minCovariateFraction
     */
    public void setMinCovariateFraction(Float minCovariateFraction) {
    this.minCovariateFraction = minCovariateFraction;
  }

  /**
   * Whether to normalise the covariates before training 
   * @return normalizeData
   **/
  @Override
  public Boolean getNormalizeData() {
    return normalizeData;
  }

    /**
     *
     * @param normalizeData
     */
    public void setNormalizeData(Boolean normalizeData) {
    this.normalizeData = normalizeData;
  }

  /**
   * Either &#x27;person&#x27; or &#x27;time&#x27; specifying the type of evaluation used. &#x27;time&#x27; find the date where testFraction of patients had an index after the date and assigns patients with an index prior to this date into the training set and post the date into the test set &#x27;person&#x27; splits the data into test (1-testFraction of the data) and train (validationFraction of the data) sets.  The split is stratified by the class label. 
   * @return testSplit
   **/
  @Override
  public TestSplitEnum getTestSplit() {
    return testSplit;
  }

    /**
     *
     * @param testSplit
     */
    public void setTestSplit(TestSplitEnum testSplit) {
    this.testSplit = testSplit;
  }

  /**
   * The fraction of the data to be used as the test set in the patient split evaluation 
   * @return testFraction
   **/
  @Override
  public Float getTestFraction() {
    return testFraction;
  }

    /**
     *
     * @param testFraction
     */
    public void setTestFraction(Float testFraction) {
    this.testFraction = testFraction;
  }

  /**
   * The seed used to split the test/train set when using a person type testSplit 
   * @return splitSeed
   **/
  @Override
  public Float getSplitSeed() {
    return splitSeed;
  }

    /**
     *
     * @param splitSeed
     */
    public void setSplitSeed(Float splitSeed) {
    this.splitSeed = splitSeed;
  }

  /**
   * The number of folds used in the cross validation 
   * @return nfold
   **/
  @Override
  public Integer getNfold() {
    return nfold;
  }

    /**
     *
     * @param nfold
     */
    public void setNfold(Integer nfold) {
    this.nfold = nfold;
  }
}
