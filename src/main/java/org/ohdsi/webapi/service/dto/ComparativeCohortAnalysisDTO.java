package org.ohdsi.webapi.service.dto;

public class ComparativeCohortAnalysisDTO extends CommonEntityDTO {
  private Integer analysisId;

  private String name;

  private Integer treatmentId;

  private Integer comparatorId;

  private Integer outcomeId;

  private String modelType;

  private int timeAtRiskStart;

  private int timeAtRiskEnd;

  private int addExposureDaysToEnd;

  private int minimumWashoutPeriod;

  private int minimumDaysAtRisk;

  private int rmSubjectsInBothCohorts;

  private int rmPriorOutcomes;

  private int psAdjustment;

  private int psExclusionId;

  private int psInclusionId;

  private int psDemographics;

  private int psDemographicsGender;

  private int psDemographicsRace;

  private int psDemographicsEthnicity;

  private int psDemographicsAge;

  private int psDemographicsYear;

  private int psDemographicsMonth;

  private int psTrim;

  private int psTrimFraction;

  private int psMatch;

  private int psMatchMaxRatio;

  private int psStrat;

  private int psStratNumStrata;

  private int psConditionOcc;

  private int psConditionOcc365d;

  private int psConditionOcc30d;

  private int psConditionOccInpt180d;

  private int psConditionEra;

  private int psConditionEraEver;

  private int psConditionEraOverlap;

  private int psConditionGroup;

  private int psConditionGroupMeddra;

  private int psConditionGroupSnomed;

  private int psDrugExposure;

  private int psDrugExposure365d;

  private int psDrugExposure30d;

  private int psDrugEra;

  private int psDrugEra365d;

  private int psDrugEra30d;

  private int psDrugEraOverlap;

  private int psDrugEraEver;

  private int psDrugGroup;

  private int psProcedureOcc;

  private int psProcedureOcc365d;

  private int psProcedureOcc30d;

  private int psProcedureGroup;

  private int psObservation;

  private int psObservation365d;

  private int psObservation30d;

  private int psObservationCount365d;

  private int psMeasurement;

  private int psMeasurement365d;

  private int psMeasurement30d;

  private int psMeasurementCount365d;

  private int psMeasurementBelow;

  private int psMeasurementAbove;

  private int psConceptCounts;

  private int psRiskScores;

  private int psRiskScoresCharlson;

  private int psRiskScoresDcsi;

  private int psRiskScoresChads2;

  private int psRiskScoresChads2vasc;

  private int psInteractionYear;

  private int psInteractionMonth;

  private int omCovariates;

  private int omExclusionId;

  private int omInclusionId;

  private int omDemographics;

  private int omDemographicsGender;

  private int omDemographicsRace;

  private int omDemographicsEthnicity;

  private int omDemographicsAge;

  private int omDemographicsYear;

  private int omDemographicsMonth;

  private int omTrim;

  private int omTrimFraction;

  private int omMatch;

  private int omMatchMaxRatio;

  private int omStrat;

  private int omStratNumStrata;

  private int omConditionOcc;

  private int omConditionOcc365d;

  private int omConditionOcc30d;

  private int omConditionOccInpt180d;

  private int omConditionEra;

  private int omConditionEraEver;

  private int omConditionEraOverlap;

  private int omConditionGroup;

  private int omConditionGroupMeddra;

  private int omConditionGroupSnomed;

  private int omDrugExposure;

  private int omDrugExposure365d;

  private int omDrugExposure30d;

  private int omDrugEra;

  private int omDrugEra365d;

  private int omDrugEra30d;

  private int omDrugEraOverlap;

  private int omDrugEraEver;

  private int omDrugGroup;

  private int omProcedureOcc;

  private int omProcedureOcc365d;

  private int omProcedureOcc30d;

  private int omProcedureGroup;

  private int omObservation;

  private int omObservation365d;

  private int omObservation30d;

  private int omObservationCount365d;

  private int omMeasurement;

  private int omMeasurement365d;

  private int omMeasurement30d;

  private int omMeasurementCount365d;

  private int omMeasurementBelow;

  private int omMeasurementAbove;

  private int omConceptCounts;

  private int omRiskScores;

  private int omRiskScoresCharlson;

  private int omRiskScoresDcsi;

  private int omRiskScoresChads2;

  private int omRiskScoresChads2vasc;

  private int omInteractionYear;

  private int omInteractionMonth;

  private int delCovariatesSmallCount;

  private int negativeControlId;

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

  public Integer getComparatorId() {
    return comparatorId;
  }

  public void setComparatorId(Integer comparatorId) {
    this.comparatorId = comparatorId;
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

  public int getRmSubjectsInBothCohorts() {
    return rmSubjectsInBothCohorts;
  }

  public void setRmSubjectsInBothCohorts(int rmSubjectsInBothCohorts) {
    this.rmSubjectsInBothCohorts = rmSubjectsInBothCohorts;
  }

  public int getRmPriorOutcomes() {
    return rmPriorOutcomes;
  }

  public void setRmPriorOutcomes(int rmPriorOutcomes) {
    this.rmPriorOutcomes = rmPriorOutcomes;
  }

  public int getPsAdjustment() {
    return psAdjustment;
  }

  public void setPsAdjustment(int psAdjustment) {
    this.psAdjustment = psAdjustment;
  }

  public int getPsExclusionId() {
    return psExclusionId;
  }

  public void setPsExclusionId(int psExclusionId) {
    this.psExclusionId = psExclusionId;
  }

  public int getPsInclusionId() {
    return psInclusionId;
  }

  public void setPsInclusionId(int psInclusionId) {
    this.psInclusionId = psInclusionId;
  }

  public int getPsDemographics() {
    return psDemographics;
  }

  public void setPsDemographics(int psDemographics) {
    this.psDemographics = psDemographics;
  }

  public int getPsDemographicsGender() {
    return psDemographicsGender;
  }

  public void setPsDemographicsGender(int psDemographicsGender) {
    this.psDemographicsGender = psDemographicsGender;
  }

  public int getPsDemographicsRace() {
    return psDemographicsRace;
  }

  public void setPsDemographicsRace(int psDemographicsRace) {
    this.psDemographicsRace = psDemographicsRace;
  }

  public int getPsDemographicsEthnicity() {
    return psDemographicsEthnicity;
  }

  public void setPsDemographicsEthnicity(int psDemographicsEthnicity) {
    this.psDemographicsEthnicity = psDemographicsEthnicity;
  }

  public int getPsDemographicsAge() {
    return psDemographicsAge;
  }

  public void setPsDemographicsAge(int psDemographicsAge) {
    this.psDemographicsAge = psDemographicsAge;
  }

  public int getPsDemographicsYear() {
    return psDemographicsYear;
  }

  public void setPsDemographicsYear(int psDemographicsYear) {
    this.psDemographicsYear = psDemographicsYear;
  }

  public int getPsDemographicsMonth() {
    return psDemographicsMonth;
  }

  public void setPsDemographicsMonth(int psDemographicsMonth) {
    this.psDemographicsMonth = psDemographicsMonth;
  }

  public int getPsTrim() {
    return psTrim;
  }

  public void setPsTrim(int psTrim) {
    this.psTrim = psTrim;
  }

  public int getPsTrimFraction() {
    return psTrimFraction;
  }

  public void setPsTrimFraction(int psTrimFraction) {
    this.psTrimFraction = psTrimFraction;
  }

  public int getPsMatch() {
    return psMatch;
  }

  public void setPsMatch(int psMatch) {
    this.psMatch = psMatch;
  }

  public int getPsMatchMaxRatio() {
    return psMatchMaxRatio;
  }

  public void setPsMatchMaxRatio(int psMatchMaxRatio) {
    this.psMatchMaxRatio = psMatchMaxRatio;
  }

  public int getPsStrat() {
    return psStrat;
  }

  public void setPsStrat(int psStrat) {
    this.psStrat = psStrat;
  }

  public int getPsStratNumStrata() {
    return psStratNumStrata;
  }

  public void setPsStratNumStrata(int psStratNumStrata) {
    this.psStratNumStrata = psStratNumStrata;
  }

  public int getPsConditionOcc() {
    return psConditionOcc;
  }

  public void setPsConditionOcc(int psConditionOcc) {
    this.psConditionOcc = psConditionOcc;
  }

  public int getPsConditionOcc365d() {
    return psConditionOcc365d;
  }

  public void setPsConditionOcc365d(int psConditionOcc365d) {
    this.psConditionOcc365d = psConditionOcc365d;
  }

  public int getPsConditionOcc30d() {
    return psConditionOcc30d;
  }

  public void setPsConditionOcc30d(int psConditionOcc30d) {
    this.psConditionOcc30d = psConditionOcc30d;
  }

  public int getPsConditionOccInpt180d() {
    return psConditionOccInpt180d;
  }

  public void setPsConditionOccInpt180d(int psConditionOccInpt180d) {
    this.psConditionOccInpt180d = psConditionOccInpt180d;
  }

  public int getPsConditionEra() {
    return psConditionEra;
  }

  public void setPsConditionEra(int psConditionEra) {
    this.psConditionEra = psConditionEra;
  }

  public int getPsConditionEraEver() {
    return psConditionEraEver;
  }

  public void setPsConditionEraEver(int psConditionEraEver) {
    this.psConditionEraEver = psConditionEraEver;
  }

  public int getPsConditionEraOverlap() {
    return psConditionEraOverlap;
  }

  public void setPsConditionEraOverlap(int psConditionEraOverlap) {
    this.psConditionEraOverlap = psConditionEraOverlap;
  }

  public int getPsConditionGroup() {
    return psConditionGroup;
  }

  public void setPsConditionGroup(int psConditionGroup) {
    this.psConditionGroup = psConditionGroup;
  }

  public int getPsConditionGroupMeddra() {
    return psConditionGroupMeddra;
  }

  public void setPsConditionGroupMeddra(int psConditionGroupMeddra) {
    this.psConditionGroupMeddra = psConditionGroupMeddra;
  }

  public int getPsConditionGroupSnomed() {
    return psConditionGroupSnomed;
  }

  public void setPsConditionGroupSnomed(int psConditionGroupSnomed) {
    this.psConditionGroupSnomed = psConditionGroupSnomed;
  }

  public int getPsDrugExposure() {
    return psDrugExposure;
  }

  public void setPsDrugExposure(int psDrugExposure) {
    this.psDrugExposure = psDrugExposure;
  }

  public int getPsDrugExposure365d() {
    return psDrugExposure365d;
  }

  public void setPsDrugExposure365d(int psDrugExposure365d) {
    this.psDrugExposure365d = psDrugExposure365d;
  }

  public int getPsDrugExposure30d() {
    return psDrugExposure30d;
  }

  public void setPsDrugExposure30d(int psDrugExposure30d) {
    this.psDrugExposure30d = psDrugExposure30d;
  }

  public int getPsDrugEra() {
    return psDrugEra;
  }

  public void setPsDrugEra(int psDrugEra) {
    this.psDrugEra = psDrugEra;
  }

  public int getPsDrugEra365d() {
    return psDrugEra365d;
  }

  public void setPsDrugEra365d(int psDrugEra365d) {
    this.psDrugEra365d = psDrugEra365d;
  }

  public int getPsDrugEra30d() {
    return psDrugEra30d;
  }

  public void setPsDrugEra30d(int psDrugEra30d) {
    this.psDrugEra30d = psDrugEra30d;
  }

  public int getPsDrugEraOverlap() {
    return psDrugEraOverlap;
  }

  public void setPsDrugEraOverlap(int psDrugEraOverlap) {
    this.psDrugEraOverlap = psDrugEraOverlap;
  }

  public int getPsDrugEraEver() {
    return psDrugEraEver;
  }

  public void setPsDrugEraEver(int psDrugEraEver) {
    this.psDrugEraEver = psDrugEraEver;
  }

  public int getPsDrugGroup() {
    return psDrugGroup;
  }

  public void setPsDrugGroup(int psDrugGroup) {
    this.psDrugGroup = psDrugGroup;
  }

  public int getPsProcedureOcc() {
    return psProcedureOcc;
  }

  public void setPsProcedureOcc(int psProcedureOcc) {
    this.psProcedureOcc = psProcedureOcc;
  }

  public int getPsProcedureOcc365d() {
    return psProcedureOcc365d;
  }

  public void setPsProcedureOcc365d(int psProcedureOcc365d) {
    this.psProcedureOcc365d = psProcedureOcc365d;
  }

  public int getPsProcedureOcc30d() {
    return psProcedureOcc30d;
  }

  public void setPsProcedureOcc30d(int psProcedureOcc30d) {
    this.psProcedureOcc30d = psProcedureOcc30d;
  }

  public int getPsProcedureGroup() {
    return psProcedureGroup;
  }

  public void setPsProcedureGroup(int psProcedureGroup) {
    this.psProcedureGroup = psProcedureGroup;
  }

  public int getPsObservation() {
    return psObservation;
  }

  public void setPsObservation(int psObservation) {
    this.psObservation = psObservation;
  }

  public int getPsObservation365d() {
    return psObservation365d;
  }

  public void setPsObservation365d(int psObservation365d) {
    this.psObservation365d = psObservation365d;
  }

  public int getPsObservation30d() {
    return psObservation30d;
  }

  public void setPsObservation30d(int psObservation30d) {
    this.psObservation30d = psObservation30d;
  }

  public int getPsObservationCount365d() {
    return psObservationCount365d;
  }

  public void setPsObservationCount365d(int psObservationCount365d) {
    this.psObservationCount365d = psObservationCount365d;
  }

  public int getPsMeasurement() {
    return psMeasurement;
  }

  public void setPsMeasurement(int psMeasurement) {
    this.psMeasurement = psMeasurement;
  }

  public int getPsMeasurement365d() {
    return psMeasurement365d;
  }

  public void setPsMeasurement365d(int psMeasurement365d) {
    this.psMeasurement365d = psMeasurement365d;
  }

  public int getPsMeasurement30d() {
    return psMeasurement30d;
  }

  public void setPsMeasurement30d(int psMeasurement30d) {
    this.psMeasurement30d = psMeasurement30d;
  }

  public int getPsMeasurementCount365d() {
    return psMeasurementCount365d;
  }

  public void setPsMeasurementCount365d(int psMeasurementCount365d) {
    this.psMeasurementCount365d = psMeasurementCount365d;
  }

  public int getPsMeasurementBelow() {
    return psMeasurementBelow;
  }

  public void setPsMeasurementBelow(int psMeasurementBelow) {
    this.psMeasurementBelow = psMeasurementBelow;
  }

  public int getPsMeasurementAbove() {
    return psMeasurementAbove;
  }

  public void setPsMeasurementAbove(int psMeasurementAbove) {
    this.psMeasurementAbove = psMeasurementAbove;
  }

  public int getPsConceptCounts() {
    return psConceptCounts;
  }

  public void setPsConceptCounts(int psConceptCounts) {
    this.psConceptCounts = psConceptCounts;
  }

  public int getPsRiskScores() {
    return psRiskScores;
  }

  public void setPsRiskScores(int psRiskScores) {
    this.psRiskScores = psRiskScores;
  }

  public int getPsRiskScoresCharlson() {
    return psRiskScoresCharlson;
  }

  public void setPsRiskScoresCharlson(int psRiskScoresCharlson) {
    this.psRiskScoresCharlson = psRiskScoresCharlson;
  }

  public int getPsRiskScoresDcsi() {
    return psRiskScoresDcsi;
  }

  public void setPsRiskScoresDcsi(int psRiskScoresDcsi) {
    this.psRiskScoresDcsi = psRiskScoresDcsi;
  }

  public int getPsRiskScoresChads2() {
    return psRiskScoresChads2;
  }

  public void setPsRiskScoresChads2(int psRiskScoresChads2) {
    this.psRiskScoresChads2 = psRiskScoresChads2;
  }

  public int getPsRiskScoresChads2vasc() {
    return psRiskScoresChads2vasc;
  }

  public void setPsRiskScoresChads2vasc(int psRiskScoresChads2vasc) {
    this.psRiskScoresChads2vasc = psRiskScoresChads2vasc;
  }

  public int getPsInteractionYear() {
    return psInteractionYear;
  }

  public void setPsInteractionYear(int psInteractionYear) {
    this.psInteractionYear = psInteractionYear;
  }

  public int getPsInteractionMonth() {
    return psInteractionMonth;
  }

  public void setPsInteractionMonth(int psInteractionMonth) {
    this.psInteractionMonth = psInteractionMonth;
  }

  public int getOmCovariates() {
    return omCovariates;
  }

  public void setOmCovariates(int omCovariates) {
    this.omCovariates = omCovariates;
  }

  public int getOmExclusionId() {
    return omExclusionId;
  }

  public void setOmExclusionId(int omExclusionId) {
    this.omExclusionId = omExclusionId;
  }

  public int getOmInclusionId() {
    return omInclusionId;
  }

  public void setOmInclusionId(int omInclusionId) {
    this.omInclusionId = omInclusionId;
  }

  public int getOmDemographics() {
    return omDemographics;
  }

  public void setOmDemographics(int omDemographics) {
    this.omDemographics = omDemographics;
  }

  public int getOmDemographicsGender() {
    return omDemographicsGender;
  }

  public void setOmDemographicsGender(int omDemographicsGender) {
    this.omDemographicsGender = omDemographicsGender;
  }

  public int getOmDemographicsRace() {
    return omDemographicsRace;
  }

  public void setOmDemographicsRace(int omDemographicsRace) {
    this.omDemographicsRace = omDemographicsRace;
  }

  public int getOmDemographicsEthnicity() {
    return omDemographicsEthnicity;
  }

  public void setOmDemographicsEthnicity(int omDemographicsEthnicity) {
    this.omDemographicsEthnicity = omDemographicsEthnicity;
  }

  public int getOmDemographicsAge() {
    return omDemographicsAge;
  }

  public void setOmDemographicsAge(int omDemographicsAge) {
    this.omDemographicsAge = omDemographicsAge;
  }

  public int getOmDemographicsYear() {
    return omDemographicsYear;
  }

  public void setOmDemographicsYear(int omDemographicsYear) {
    this.omDemographicsYear = omDemographicsYear;
  }

  public int getOmDemographicsMonth() {
    return omDemographicsMonth;
  }

  public void setOmDemographicsMonth(int omDemographicsMonth) {
    this.omDemographicsMonth = omDemographicsMonth;
  }

  public int getOmTrim() {
    return omTrim;
  }

  public void setOmTrim(int omTrim) {
    this.omTrim = omTrim;
  }

  public int getOmTrimFraction() {
    return omTrimFraction;
  }

  public void setOmTrimFraction(int omTrimFraction) {
    this.omTrimFraction = omTrimFraction;
  }

  public int getOmMatch() {
    return omMatch;
  }

  public void setOmMatch(int omMatch) {
    this.omMatch = omMatch;
  }

  public int getOmMatchMaxRatio() {
    return omMatchMaxRatio;
  }

  public void setOmMatchMaxRatio(int omMatchMaxRatio) {
    this.omMatchMaxRatio = omMatchMaxRatio;
  }

  public int getOmStrat() {
    return omStrat;
  }

  public void setOmStrat(int omStrat) {
    this.omStrat = omStrat;
  }

  public int getOmStratNumStrata() {
    return omStratNumStrata;
  }

  public void setOmStratNumStrata(int omStratNumStrata) {
    this.omStratNumStrata = omStratNumStrata;
  }

  public int getOmConditionOcc() {
    return omConditionOcc;
  }

  public void setOmConditionOcc(int omConditionOcc) {
    this.omConditionOcc = omConditionOcc;
  }

  public int getOmConditionOcc365d() {
    return omConditionOcc365d;
  }

  public void setOmConditionOcc365d(int omConditionOcc365d) {
    this.omConditionOcc365d = omConditionOcc365d;
  }

  public int getOmConditionOcc30d() {
    return omConditionOcc30d;
  }

  public void setOmConditionOcc30d(int omConditionOcc30d) {
    this.omConditionOcc30d = omConditionOcc30d;
  }

  public int getOmConditionOccInpt180d() {
    return omConditionOccInpt180d;
  }

  public void setOmConditionOccInpt180d(int omConditionOccInpt180d) {
    this.omConditionOccInpt180d = omConditionOccInpt180d;
  }

  public int getOmConditionEra() {
    return omConditionEra;
  }

  public void setOmConditionEra(int omConditionEra) {
    this.omConditionEra = omConditionEra;
  }

  public int getOmConditionEraEver() {
    return omConditionEraEver;
  }

  public void setOmConditionEraEver(int omConditionEraEver) {
    this.omConditionEraEver = omConditionEraEver;
  }

  public int getOmConditionEraOverlap() {
    return omConditionEraOverlap;
  }

  public void setOmConditionEraOverlap(int omConditionEraOverlap) {
    this.omConditionEraOverlap = omConditionEraOverlap;
  }

  public int getOmConditionGroup() {
    return omConditionGroup;
  }

  public void setOmConditionGroup(int omConditionGroup) {
    this.omConditionGroup = omConditionGroup;
  }

  public int getOmConditionGroupMeddra() {
    return omConditionGroupMeddra;
  }

  public void setOmConditionGroupMeddra(int omConditionGroupMeddra) {
    this.omConditionGroupMeddra = omConditionGroupMeddra;
  }

  public int getOmConditionGroupSnomed() {
    return omConditionGroupSnomed;
  }

  public void setOmConditionGroupSnomed(int omConditionGroupSnomed) {
    this.omConditionGroupSnomed = omConditionGroupSnomed;
  }

  public int getOmDrugExposure() {
    return omDrugExposure;
  }

  public void setOmDrugExposure(int omDrugExposure) {
    this.omDrugExposure = omDrugExposure;
  }

  public int getOmDrugExposure365d() {
    return omDrugExposure365d;
  }

  public void setOmDrugExposure365d(int omDrugExposure365d) {
    this.omDrugExposure365d = omDrugExposure365d;
  }

  public int getOmDrugExposure30d() {
    return omDrugExposure30d;
  }

  public void setOmDrugExposure30d(int omDrugExposure30d) {
    this.omDrugExposure30d = omDrugExposure30d;
  }

  public int getOmDrugEra() {
    return omDrugEra;
  }

  public void setOmDrugEra(int omDrugEra) {
    this.omDrugEra = omDrugEra;
  }

  public int getOmDrugEra365d() {
    return omDrugEra365d;
  }

  public void setOmDrugEra365d(int omDrugEra365d) {
    this.omDrugEra365d = omDrugEra365d;
  }

  public int getOmDrugEra30d() {
    return omDrugEra30d;
  }

  public void setOmDrugEra30d(int omDrugEra30d) {
    this.omDrugEra30d = omDrugEra30d;
  }

  public int getOmDrugEraOverlap() {
    return omDrugEraOverlap;
  }

  public void setOmDrugEraOverlap(int omDrugEraOverlap) {
    this.omDrugEraOverlap = omDrugEraOverlap;
  }

  public int getOmDrugEraEver() {
    return omDrugEraEver;
  }

  public void setOmDrugEraEver(int omDrugEraEver) {
    this.omDrugEraEver = omDrugEraEver;
  }

  public int getOmDrugGroup() {
    return omDrugGroup;
  }

  public void setOmDrugGroup(int omDrugGroup) {
    this.omDrugGroup = omDrugGroup;
  }

  public int getOmProcedureOcc() {
    return omProcedureOcc;
  }

  public void setOmProcedureOcc(int omProcedureOcc) {
    this.omProcedureOcc = omProcedureOcc;
  }

  public int getOmProcedureOcc365d() {
    return omProcedureOcc365d;
  }

  public void setOmProcedureOcc365d(int omProcedureOcc365d) {
    this.omProcedureOcc365d = omProcedureOcc365d;
  }

  public int getOmProcedureOcc30d() {
    return omProcedureOcc30d;
  }

  public void setOmProcedureOcc30d(int omProcedureOcc30d) {
    this.omProcedureOcc30d = omProcedureOcc30d;
  }

  public int getOmProcedureGroup() {
    return omProcedureGroup;
  }

  public void setOmProcedureGroup(int omProcedureGroup) {
    this.omProcedureGroup = omProcedureGroup;
  }

  public int getOmObservation() {
    return omObservation;
  }

  public void setOmObservation(int omObservation) {
    this.omObservation = omObservation;
  }

  public int getOmObservation365d() {
    return omObservation365d;
  }

  public void setOmObservation365d(int omObservation365d) {
    this.omObservation365d = omObservation365d;
  }

  public int getOmObservation30d() {
    return omObservation30d;
  }

  public void setOmObservation30d(int omObservation30d) {
    this.omObservation30d = omObservation30d;
  }

  public int getOmObservationCount365d() {
    return omObservationCount365d;
  }

  public void setOmObservationCount365d(int omObservationCount365d) {
    this.omObservationCount365d = omObservationCount365d;
  }

  public int getOmMeasurement() {
    return omMeasurement;
  }

  public void setOmMeasurement(int omMeasurement) {
    this.omMeasurement = omMeasurement;
  }

  public int getOmMeasurement365d() {
    return omMeasurement365d;
  }

  public void setOmMeasurement365d(int omMeasurement365d) {
    this.omMeasurement365d = omMeasurement365d;
  }

  public int getOmMeasurement30d() {
    return omMeasurement30d;
  }

  public void setOmMeasurement30d(int omMeasurement30d) {
    this.omMeasurement30d = omMeasurement30d;
  }

  public int getOmMeasurementCount365d() {
    return omMeasurementCount365d;
  }

  public void setOmMeasurementCount365d(int omMeasurementCount365d) {
    this.omMeasurementCount365d = omMeasurementCount365d;
  }

  public int getOmMeasurementBelow() {
    return omMeasurementBelow;
  }

  public void setOmMeasurementBelow(int omMeasurementBelow) {
    this.omMeasurementBelow = omMeasurementBelow;
  }

  public int getOmMeasurementAbove() {
    return omMeasurementAbove;
  }

  public void setOmMeasurementAbove(int omMeasurementAbove) {
    this.omMeasurementAbove = omMeasurementAbove;
  }

  public int getOmConceptCounts() {
    return omConceptCounts;
  }

  public void setOmConceptCounts(int omConceptCounts) {
    this.omConceptCounts = omConceptCounts;
  }

  public int getOmRiskScores() {
    return omRiskScores;
  }

  public void setOmRiskScores(int omRiskScores) {
    this.omRiskScores = omRiskScores;
  }

  public int getOmRiskScoresCharlson() {
    return omRiskScoresCharlson;
  }

  public void setOmRiskScoresCharlson(int omRiskScoresCharlson) {
    this.omRiskScoresCharlson = omRiskScoresCharlson;
  }

  public int getOmRiskScoresDcsi() {
    return omRiskScoresDcsi;
  }

  public void setOmRiskScoresDcsi(int omRiskScoresDcsi) {
    this.omRiskScoresDcsi = omRiskScoresDcsi;
  }

  public int getOmRiskScoresChads2() {
    return omRiskScoresChads2;
  }

  public void setOmRiskScoresChads2(int omRiskScoresChads2) {
    this.omRiskScoresChads2 = omRiskScoresChads2;
  }

  public int getOmRiskScoresChads2vasc() {
    return omRiskScoresChads2vasc;
  }

  public void setOmRiskScoresChads2vasc(int omRiskScoresChads2vasc) {
    this.omRiskScoresChads2vasc = omRiskScoresChads2vasc;
  }

  public int getOmInteractionYear() {
    return omInteractionYear;
  }

  public void setOmInteractionYear(int omInteractionYear) {
    this.omInteractionYear = omInteractionYear;
  }

  public int getOmInteractionMonth() {
    return omInteractionMonth;
  }

  public void setOmInteractionMonth(int omInteractionMonth) {
    this.omInteractionMonth = omInteractionMonth;
  }

  public int getDelCovariatesSmallCount() {
    return delCovariatesSmallCount;
  }

  public void setDelCovariatesSmallCount(int delCovariatesSmallCount) {
    this.delCovariatesSmallCount = delCovariatesSmallCount;
  }

  public int getNegativeControlId() {
    return negativeControlId;
  }

  public void setNegativeControlId(int negativeControlId) {
    this.negativeControlId = negativeControlId;
  }
}
