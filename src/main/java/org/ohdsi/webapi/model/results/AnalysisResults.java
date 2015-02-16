package org.ohdsi.webapi.model.results;


/**
 * 
 * For OHDSI implementations, this corresponds to the HERACLES_RESULTS table
 *
 */
public class AnalysisResults {

	public static final String COHORT_DEFINITION_ID = "COHORT_DEFINITION_ID";
	public static final String ANALYSIS_ID = "ANALYSIS_ID";
	public static final String STRATUM_1 = "STRATUM_1";
	public static final String STRATUM_2 = "STRATUM_2";
	public static final String STRATUM_3 = "STRATUM_3";
	public static final String STRATUM_4 = "STRATUM_4";
	public static final String STRATUM_5 = "STRATUM_5";
	public static final String COUNT_VALUE = "COUNT_VALUE";
	
	private int cohortDefinitionId;
	
	private int analysisId;
	
	private String stratum1;
	
	private String stratum2;
	
	private String stratum3;
	
	private String stratum4;
	
	private String stratum5;
	
	private int countValue;

	public int getCohortDefinitionId() {
		return cohortDefinitionId;
	}

	public void setCohortDefinitionId(int cohortDefinitionId) {
		this.cohortDefinitionId = cohortDefinitionId;
	}

	public int getAnalysisId() {
		return analysisId;
	}

	public void setAnalysisId(int analysisId) {
		this.analysisId = analysisId;
	}

	public String getStratum1() {
		return stratum1;
	}

	public void setStratum1(String stratum1) {
		this.stratum1 = stratum1;
	}

	public String getStratum2() {
		return stratum2;
	}

	public void setStratum2(String stratum2) {
		this.stratum2 = stratum2;
	}

	public String getStratum3() {
		return stratum3;
	}

	public void setStratum3(String stratum3) {
		this.stratum3 = stratum3;
	}

	public String getStratum4() {
		return stratum4;
	}

	public void setStratum4(String stratum4) {
		this.stratum4 = stratum4;
	}

	public String getStratum5() {
		return stratum5;
	}

	public void setStratum5(String stratum5) {
		this.stratum5 = stratum5;
	}

	public int getCountValue() {
		return countValue;
	}

	public void setCountValue(int countValue) {
		this.countValue = countValue;
	}
	
}
