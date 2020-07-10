package org.ohdsi.webapi.service.dto;

public class PatientLevelPredictionAnalysisDTO extends CommonEntityDTO {
  private Integer analysisId;

  private String name;

  private Integer treatmentId;

  private Integer outcomeId;

  private String modelType;

  private int timeAtRiskStart;

  private int timeAtRiskEnd;

  private int addExposureDaysToEnd;

  private int minimumWashoutPeriod;

  private int minimumDaysAtRisk;

  private int requireTimeAtRisk;

  private int minimumTimeAtRisk;

  private int sample;

  private int sampleSize;

  private int firstExposureOnly;

  private int includeAllOutcomes;

  private int rmPriorOutcomes;

  private int priorOutcomeLookback;

  private int testSplit;

  private String testFraction;

  private String nFold;

  private String moAlpha;

  private String moClassWeight;

  private String moIndexFolder;

  private String moK;

  private String moLearnRate;

  private String moLearningRate;

  private String moMaxDepth;

  private String moMinImpuritySplit;

  private String moMinRows;

  private String moMinSamplesLeaf;

  private String moMinSamplesSplit;

  private String moMTries;

  private String moNEstimators;

  private String moNThread;

  private String moNTrees;

  private String moPlot;

  private String moSeed;

  private String moSize;

  private String moVariance;

  private String moVarImp;

  private int cvDemographics;

  private int cvExclusionId;

  private int cvInclusionId;

  private int cvDemographicsGender;

  private int cvDemographicsRace;

  private int cvDemographicsEthnicity;

  private int cvDemographicsAge;

  private int cvDemographicsYear;

  private int cvDemographicsMonth;

  private int cvConditionOcc;

  private int cvConditionOcc365d;

  private int cvConditionOcc30d;

  private int cvConditionOccInpt180d;

  private int cvConditionEra;

  private int cvConditionEraEver;

  private int cvConditionEraOverlap;

  private int cvConditionGroup;

  private int cvConditionGroupMeddra;

  private int cvConditionGroupSnomed;

  private int cvDrugExposure;

  private int cvDrugExposure365d;

  private int cvDrugExposure30d;

  private int cvDrugEra;

  private int cvDrugEra365d;

  private int cvDrugEra30d;

  private int cvDrugEraOverlap;

  private int cvDrugEraEver;

  private int cvDrugGroup;

  private int cvProcedureOcc;

  private int cvProcedureOcc365d;

  private int cvProcedureOcc30d;

  private int cvProcedureGroup;

  private int cvObservation;

  private int cvObservation365d;

  private int cvObservation30d;

  private int cvObservationCount365d;

  private int cvMeasurement;

  private int cvMeasurement365d;

  private int cvMeasurement30d;

  private int cvMeasurementCount365d;

  private int cvMeasurementBelow;

  private int cvMeasurementAbove;

  private int cvConceptCounts;

  private int cvRiskScores;

  private int cvRiskScoresCharlson;

  private int cvRiskScoresDcsi;

  private int cvRiskScoresChads2;

  private int cvRiskScoresChads2vasc;

  private int cvInteractionYear;

  private int cvInteractionMonth;

  private int delCovariatesSmallCount;

  public Integer getId() {
    return analysisId;
  }

  public void setAnalysisId(Integer analysisId) {
    this.analysisId = analysisId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getTreatmentId() {
    return treatmentId;
  }

  public void setTreatmentId(Integer treatmentId) {
    this.treatmentId = treatmentId;
  }

  public Integer getOutcomeId() {
    return outcomeId;
  }

  public void setOutcomeId(Integer outcomeId) {
    this.outcomeId = outcomeId;
  }

  public String getModelType() {
    return modelType;
  }

  public void setModelType(String modelType) {
    this.modelType = modelType;
  }

  public int getTimeAtRiskStart() {
    return timeAtRiskStart;
  }

  public void setTimeAtRiskStart(int timeAtRiskStart) {
    this.timeAtRiskStart = timeAtRiskStart;
  }

  public int getTimeAtRiskEnd() {
    return timeAtRiskEnd;
  }

  public void setTimeAtRiskEnd(int timeAtRiskEnd) {
    this.timeAtRiskEnd = timeAtRiskEnd;
  }

  public int getAddExposureDaysToEnd() {
    return addExposureDaysToEnd;
  }

  public void setAddExposureDaysToEnd(int addExposureDaysToEnd) {
    this.addExposureDaysToEnd = addExposureDaysToEnd;
  }

  public int getMinimumWashoutPeriod() {
    return minimumWashoutPeriod;
  }

  public void setMinimumWashoutPeriod(int minimumWashoutPeriod) {
    this.minimumWashoutPeriod = minimumWashoutPeriod;
  }

  public int getMinimumDaysAtRisk() {
    return minimumDaysAtRisk;
  }

  public void setMinimumDaysAtRisk(int minimumDaysAtRisk) {
    this.minimumDaysAtRisk = minimumDaysAtRisk;
  }

  public int getRequireTimeAtRisk() {
    return requireTimeAtRisk;
  }

  public void setRequireTimeAtRisk(int requireTimeAtRisk) {
    this.requireTimeAtRisk = requireTimeAtRisk;
  }

  public int getMinimumTimeAtRisk() {
    return minimumTimeAtRisk;
  }

  public void setMinimumTimeAtRisk(int minimumTimeAtRisk) {
    this.minimumTimeAtRisk = minimumTimeAtRisk;
  }

  public int getSample() {
    return sample;
  }

  public void setSample(int sample) {
    this.sample = sample;
  }

  public int getSampleSize() {
    return sampleSize;
  }

  public void setSampleSize(int sampleSize) {
    this.sampleSize = sampleSize;
  }

  public int getFirstExposureOnly() {
    return firstExposureOnly;
  }

  public void setFirstExposureOnly(int firstExposureOnly) {
    this.firstExposureOnly = firstExposureOnly;
  }

  public int getIncludeAllOutcomes() {
    return includeAllOutcomes;
  }

  public void setIncludeAllOutcomes(int includeAllOutcomes) {
    this.includeAllOutcomes = includeAllOutcomes;
  }

  public int getRmPriorOutcomes() {
    return rmPriorOutcomes;
  }

  public void setRmPriorOutcomes(int rmPriorOutcomes) {
    this.rmPriorOutcomes = rmPriorOutcomes;
  }

  public int getPriorOutcomeLookback() {
    return priorOutcomeLookback;
  }

  public void setPriorOutcomeLookback(int priorOutcomeLookback) {
    this.priorOutcomeLookback = priorOutcomeLookback;
  }

  public int getTestSplit() {
    return testSplit;
  }

  public void setTestSplit(int testSplit) {
    this.testSplit = testSplit;
  }

  public String getTestFraction() {
    return testFraction;
  }

  public void setTestFraction(String testFraction) {
    this.testFraction = testFraction;
  }

  public String getnFold() {
    return nFold;
  }

  public void setnFold(String nFold) {
    this.nFold = nFold;
  }

  public String getMoAlpha() {
    return moAlpha;
  }

  public void setMoAlpha(String moAlpha) {
    this.moAlpha = moAlpha;
  }

  public String getMoClassWeight() {
    return moClassWeight;
  }

  public void setMoClassWeight(String moClassWeight) {
    this.moClassWeight = moClassWeight;
  }

  public String getMoIndexFolder() {
    return moIndexFolder;
  }

  public void setMoIndexFolder(String moIndexFolder) {
    this.moIndexFolder = moIndexFolder;
  }

  public String getMoK() {
    return moK;
  }

  public void setMoK(String moK) {
    this.moK = moK;
  }

  public String getMoLearnRate() {
    return moLearnRate;
  }

  public void setMoLearnRate(String moLearnRate) {
    this.moLearnRate = moLearnRate;
  }

  public String getMoLearningRate() {
    return moLearningRate;
  }

  public void setMoLearningRate(String moLearningRate) {
    this.moLearningRate = moLearningRate;
  }

  public String getMoMaxDepth() {
    return moMaxDepth;
  }

  public void setMoMaxDepth(String moMaxDepth) {
    this.moMaxDepth = moMaxDepth;
  }

  public String getMoMinImpuritySplit() {
    return moMinImpuritySplit;
  }

  public void setMoMinImpuritySplit(String moMinImpuritySplit) {
    this.moMinImpuritySplit = moMinImpuritySplit;
  }

  public String getMoMinRows() {
    return moMinRows;
  }

  public void setMoMinRows(String moMinRows) {
    this.moMinRows = moMinRows;
  }

  public String getMoMinSamplesLeaf() {
    return moMinSamplesLeaf;
  }

  public void setMoMinSamplesLeaf(String moMinSamplesLeaf) {
    this.moMinSamplesLeaf = moMinSamplesLeaf;
  }

  public String getMoMinSamplesSplit() {
    return moMinSamplesSplit;
  }

  public void setMoMinSamplesSplit(String moMinSamplesSplit) {
    this.moMinSamplesSplit = moMinSamplesSplit;
  }

  public String getMoMTries() {
    return moMTries;
  }

  public void setMoMTries(String moMTries) {
    this.moMTries = moMTries;
  }

  public String getMoNEstimators() {
    return moNEstimators;
  }

  public void setMoNEstimators(String moNEstimators) {
    this.moNEstimators = moNEstimators;
  }

  public String getMoNThread() {
    return moNThread;
  }

  public void setMoNThread(String moNThread) {
    this.moNThread = moNThread;
  }

  public String getMoNTrees() {
    return moNTrees;
  }

  public void setMoNTrees(String moNTrees) {
    this.moNTrees = moNTrees;
  }

  public String getMoPlot() {
    return moPlot;
  }

  public void setMoPlot(String moPlot) {
    this.moPlot = moPlot;
  }

  public String getMoSeed() {
    return moSeed;
  }

  public void setMoSeed(String moSeed) {
    this.moSeed = moSeed;
  }

  public String getMoSize() {
    return moSize;
  }

  public void setMoSize(String moSize) {
    this.moSize = moSize;
  }

  public String getMoVariance() {
    return moVariance;
  }

  public void setMoVariance(String moVariance) {
    this.moVariance = moVariance;
  }

  public String getMoVarImp() {
    return moVarImp;
  }

  public void setMoVarImp(String moVarImp) {
    this.moVarImp = moVarImp;
  }

  public int getCvDemographics() {
    return cvDemographics;
  }

  public void setCvDemographics(int cvDemographics) {
    this.cvDemographics = cvDemographics;
  }

  public int getCvExclusionId() {
    return cvExclusionId;
  }

  public void setCvExclusionId(int cvExclusionId) {
    this.cvExclusionId = cvExclusionId;
  }

  public int getCvInclusionId() {
    return cvInclusionId;
  }

  public void setCvInclusionId(int cvInclusionId) {
    this.cvInclusionId = cvInclusionId;
  }

  public int getCvDemographicsGender() {
    return cvDemographicsGender;
  }

  public void setCvDemographicsGender(int cvDemographicsGender) {
    this.cvDemographicsGender = cvDemographicsGender;
  }

  public int getCvDemographicsRace() {
    return cvDemographicsRace;
  }

  public void setCvDemographicsRace(int cvDemographicsRace) {
    this.cvDemographicsRace = cvDemographicsRace;
  }

  public int getCvDemographicsEthnicity() {
    return cvDemographicsEthnicity;
  }

  public void setCvDemographicsEthnicity(int cvDemographicsEthnicity) {
    this.cvDemographicsEthnicity = cvDemographicsEthnicity;
  }

  public int getCvDemographicsAge() {
    return cvDemographicsAge;
  }

  public void setCvDemographicsAge(int cvDemographicsAge) {
    this.cvDemographicsAge = cvDemographicsAge;
  }

  public int getCvDemographicsYear() {
    return cvDemographicsYear;
  }

  public void setCvDemographicsYear(int cvDemographicsYear) {
    this.cvDemographicsYear = cvDemographicsYear;
  }

  public int getCvDemographicsMonth() {
    return cvDemographicsMonth;
  }

  public void setCvDemographicsMonth(int cvDemographicsMonth) {
    this.cvDemographicsMonth = cvDemographicsMonth;
  }

  public int getCvConditionOcc() {
    return cvConditionOcc;
  }

  public void setCvConditionOcc(int cvConditionOcc) {
    this.cvConditionOcc = cvConditionOcc;
  }

  public int getCvConditionOcc365d() {
    return cvConditionOcc365d;
  }

  public void setCvConditionOcc365d(int cvConditionOcc365d) {
    this.cvConditionOcc365d = cvConditionOcc365d;
  }

  public int getCvConditionOcc30d() {
    return cvConditionOcc30d;
  }

  public void setCvConditionOcc30d(int cvConditionOcc30d) {
    this.cvConditionOcc30d = cvConditionOcc30d;
  }

  public int getCvConditionOccInpt180d() {
    return cvConditionOccInpt180d;
  }

  public void setCvConditionOccInpt180d(int cvConditionOccInpt180d) {
    this.cvConditionOccInpt180d = cvConditionOccInpt180d;
  }

  public int getCvConditionEra() {
    return cvConditionEra;
  }

  public void setCvConditionEra(int cvConditionEra) {
    this.cvConditionEra = cvConditionEra;
  }

  public int getCvConditionEraEver() {
    return cvConditionEraEver;
  }

  public void setCvConditionEraEver(int cvConditionEraEver) {
    this.cvConditionEraEver = cvConditionEraEver;
  }

  public int getCvConditionEraOverlap() {
    return cvConditionEraOverlap;
  }

  public void setCvConditionEraOverlap(int cvConditionEraOverlap) {
    this.cvConditionEraOverlap = cvConditionEraOverlap;
  }

  public int getCvConditionGroup() {
    return cvConditionGroup;
  }

  public void setCvConditionGroup(int cvConditionGroup) {
    this.cvConditionGroup = cvConditionGroup;
  }

  public int getCvConditionGroupMeddra() {
    return cvConditionGroupMeddra;
  }

  public void setCvConditionGroupMeddra(int cvConditionGroupMeddra) {
    this.cvConditionGroupMeddra = cvConditionGroupMeddra;
  }

  public int getCvConditionGroupSnomed() {
    return cvConditionGroupSnomed;
  }

  public void setCvConditionGroupSnomed(int cvConditionGroupSnomed) {
    this.cvConditionGroupSnomed = cvConditionGroupSnomed;
  }

  public int getCvDrugExposure() {
    return cvDrugExposure;
  }

  public void setCvDrugExposure(int cvDrugExposure) {
    this.cvDrugExposure = cvDrugExposure;
  }

  public int getCvDrugExposure365d() {
    return cvDrugExposure365d;
  }

  public void setCvDrugExposure365d(int cvDrugExposure365d) {
    this.cvDrugExposure365d = cvDrugExposure365d;
  }

  public int getCvDrugExposure30d() {
    return cvDrugExposure30d;
  }

  public void setCvDrugExposure30d(int cvDrugExposure30d) {
    this.cvDrugExposure30d = cvDrugExposure30d;
  }

  public int getCvDrugEra() {
    return cvDrugEra;
  }

  public void setCvDrugEra(int cvDrugEra) {
    this.cvDrugEra = cvDrugEra;
  }

  public int getCvDrugEra365d() {
    return cvDrugEra365d;
  }

  public void setCvDrugEra365d(int cvDrugEra365d) {
    this.cvDrugEra365d = cvDrugEra365d;
  }

  public int getCvDrugEra30d() {
    return cvDrugEra30d;
  }

  public void setCvDrugEra30d(int cvDrugEra30d) {
    this.cvDrugEra30d = cvDrugEra30d;
  }

  public int getCvDrugEraOverlap() {
    return cvDrugEraOverlap;
  }

  public void setCvDrugEraOverlap(int cvDrugEraOverlap) {
    this.cvDrugEraOverlap = cvDrugEraOverlap;
  }

  public int getCvDrugEraEver() {
    return cvDrugEraEver;
  }

  public void setCvDrugEraEver(int cvDrugEraEver) {
    this.cvDrugEraEver = cvDrugEraEver;
  }

  public int getCvDrugGroup() {
    return cvDrugGroup;
  }

  public void setCvDrugGroup(int cvDrugGroup) {
    this.cvDrugGroup = cvDrugGroup;
  }

  public int getCvProcedureOcc() {
    return cvProcedureOcc;
  }

  public void setCvProcedureOcc(int cvProcedureOcc) {
    this.cvProcedureOcc = cvProcedureOcc;
  }

  public int getCvProcedureOcc365d() {
    return cvProcedureOcc365d;
  }

  public void setCvProcedureOcc365d(int cvProcedureOcc365d) {
    this.cvProcedureOcc365d = cvProcedureOcc365d;
  }

  public int getCvProcedureOcc30d() {
    return cvProcedureOcc30d;
  }

  public void setCvProcedureOcc30d(int cvProcedureOcc30d) {
    this.cvProcedureOcc30d = cvProcedureOcc30d;
  }

  public int getCvProcedureGroup() {
    return cvProcedureGroup;
  }

  public void setCvProcedureGroup(int cvProcedureGroup) {
    this.cvProcedureGroup = cvProcedureGroup;
  }

  public int getCvObservation() {
    return cvObservation;
  }

  public void setCvObservation(int cvObservation) {
    this.cvObservation = cvObservation;
  }

  public int getCvObservation365d() {
    return cvObservation365d;
  }

  public void setCvObservation365d(int cvObservation365d) {
    this.cvObservation365d = cvObservation365d;
  }

  public int getCvObservation30d() {
    return cvObservation30d;
  }

  public void setCvObservation30d(int cvObservation30d) {
    this.cvObservation30d = cvObservation30d;
  }

  public int getCvObservationCount365d() {
    return cvObservationCount365d;
  }

  public void setCvObservationCount365d(int cvObservationCount365d) {
    this.cvObservationCount365d = cvObservationCount365d;
  }

  public int getCvMeasurement() {
    return cvMeasurement;
  }

  public void setCvMeasurement(int cvMeasurement) {
    this.cvMeasurement = cvMeasurement;
  }

  public int getCvMeasurement365d() {
    return cvMeasurement365d;
  }

  public void setCvMeasurement365d(int cvMeasurement365d) {
    this.cvMeasurement365d = cvMeasurement365d;
  }

  public int getCvMeasurement30d() {
    return cvMeasurement30d;
  }

  public void setCvMeasurement30d(int cvMeasurement30d) {
    this.cvMeasurement30d = cvMeasurement30d;
  }

  public int getCvMeasurementCount365d() {
    return cvMeasurementCount365d;
  }

  public void setCvMeasurementCount365d(int cvMeasurementCount365d) {
    this.cvMeasurementCount365d = cvMeasurementCount365d;
  }

  public int getCvMeasurementBelow() {
    return cvMeasurementBelow;
  }

  public void setCvMeasurementBelow(int cvMeasurementBelow) {
    this.cvMeasurementBelow = cvMeasurementBelow;
  }

  public int getCvMeasurementAbove() {
    return cvMeasurementAbove;
  }

  public void setCvMeasurementAbove(int cvMeasurementAbove) {
    this.cvMeasurementAbove = cvMeasurementAbove;
  }

  public int getCvConceptCounts() {
    return cvConceptCounts;
  }

  public void setCvConceptCounts(int cvConceptCounts) {
    this.cvConceptCounts = cvConceptCounts;
  }

  public int getCvRiskScores() {
    return cvRiskScores;
  }

  public void setCvRiskScores(int cvRiskScores) {
    this.cvRiskScores = cvRiskScores;
  }

  public int getCvRiskScoresCharlson() {
    return cvRiskScoresCharlson;
  }

  public void setCvRiskScoresCharlson(int cvRiskScoresCharlson) {
    this.cvRiskScoresCharlson = cvRiskScoresCharlson;
  }

  public int getCvRiskScoresDcsi() {
    return cvRiskScoresDcsi;
  }

  public void setCvRiskScoresDcsi(int cvRiskScoresDcsi) {
    this.cvRiskScoresDcsi = cvRiskScoresDcsi;
  }

  public int getCvRiskScoresChads2() {
    return cvRiskScoresChads2;
  }

  public void setCvRiskScoresChads2(int cvRiskScoresChads2) {
    this.cvRiskScoresChads2 = cvRiskScoresChads2;
  }

  public int getCvRiskScoresChads2vasc() {
    return cvRiskScoresChads2vasc;
  }

  public void setCvRiskScoresChads2vasc(int cvRiskScoresChads2vasc) {
    this.cvRiskScoresChads2vasc = cvRiskScoresChads2vasc;
  }

  public int getCvInteractionYear() {
    return cvInteractionYear;
  }

  public void setCvInteractionYear(int cvInteractionYear) {
    this.cvInteractionYear = cvInteractionYear;
  }

  public int getCvInteractionMonth() {
    return cvInteractionMonth;
  }

  public void setCvInteractionMonth(int cvInteractionMonth) {
    this.cvInteractionMonth = cvInteractionMonth;
  }

  public int getDelCovariatesSmallCount() {
    return delCovariatesSmallCount;
  }

  public void setDelCovariatesSmallCount(int delCovariatesSmallCount) {
    this.delCovariatesSmallCount = delCovariatesSmallCount;
  }
}
