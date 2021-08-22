package org.ohdsi.webapi.cohortsample;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.ohdsi.webapi.model.CommonEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;

/**
 * Cohort sample details.
 */
@Entity(name = "CohortSample")
@Table(name = "cohort_sample")
public class CohortSample extends CommonEntity<Integer> {
	@Id
	@GenericGenerator(
			name = "cohort_sample_generator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@Parameter(name = "sequence_name", value = "cohort_sample_sequence"),
					@Parameter(name = "increment_size", value = "1")
			}
	)
	@GeneratedValue(generator = "cohort_sample_generator")
	private Integer id;

	@Column
	private String name;

	@Column(name = "cohort_definition_id")
	private int cohortDefinitionId;

	@Column(name = "source_id")
	private int sourceId;

	@Column(name = "age_mode")
	private String ageMode;

	@Column(name = "age_min")
	private Integer ageMin;

	@Column(name = "age_max")
	private Integer ageMax;

	@Column(name = "gender_concept_ids")
	private String genderConceptIds;

	@Column(name = "\"size\"")
	private int size;

	@Transient
	private List<SampleElement> elements;

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public int getCohortDefinitionId() {
		return cohortDefinitionId;
	}

	public void setCohortDefinitionId(int cohortDefinitionId) {
		this.cohortDefinitionId = cohortDefinitionId;
	}

	public List<SampleElement> getElements() {
		return elements;
	}

	public void setElements(List<SampleElement> elements) {
		this.elements = elements;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public Integer getAgeMin() {
		return ageMin;
	}

	public void setAgeMin(Integer ageMin) {
		this.ageMin = ageMin;
	}

	public Integer getAgeMax() {
		return ageMax;
	}

	public void setAgeMax(Integer ageMax) {
		this.ageMax = ageMax;
	}

	public String getGenderConceptIds() {
		return genderConceptIds;
	}

	public void setGenderConceptIds(String genderConceptIds) {
		this.genderConceptIds = genderConceptIds;
	}

	public int getSourceId() {
		return sourceId;
	}

	public void setSourceId(int sourceId) {
		this.sourceId = sourceId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getAgeMode() {
		return ageMode;
	}

	public void setAgeMode(String ageMode) {
		this.ageMode = ageMode;
	}
}
