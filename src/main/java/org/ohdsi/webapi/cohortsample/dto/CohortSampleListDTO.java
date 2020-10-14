package org.ohdsi.webapi.cohortsample.dto;

import org.ohdsi.webapi.GenerationStatus;

import java.util.List;

public class CohortSampleListDTO {
	private int cohortDefinitionId;
	private int sourceId;
	private GenerationStatus generationStatus;
	private boolean isValid;
	private List<CohortSampleDTO> samples;

	public int getCohortDefinitionId() {
		return cohortDefinitionId;
	}

	public void setCohortDefinitionId(int cohortDefinitionId) {
		this.cohortDefinitionId = cohortDefinitionId;
	}

	public int getSourceId() {
		return sourceId;
	}

	public void setSourceId(int sourceId) {
		this.sourceId = sourceId;
	}

	public GenerationStatus getGenerationStatus() {
		return generationStatus;
	}

	public void setGenerationStatus(GenerationStatus generationStatus) {
		this.generationStatus = generationStatus;
	}

	public boolean isValid() {
		return isValid;
	}

	public void setIsValid(boolean valid) {
		isValid = valid;
	}

	public List<CohortSampleDTO> getSamples() {
		return samples;
	}

	public void setSamples(List<CohortSampleDTO> samples) {
		this.samples = samples;
	}
}
