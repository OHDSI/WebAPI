package org.ohdsi.webapi.annotation.annotation;

import java.util.HashSet;
import java.util.Set;
import org.ohdsi.webapi.annotation.result.ResultDto;

public class AnnotationDto {

  private int id;
  private int subjectId;
  private int sampleId;
  private int annotationSetId;
  private String name;
  private Set<ResultDto> results = new HashSet();

  //***** GETTERS/SETTERS ******

  /**
   * @return the id
   */
  public int getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * @return the subjectId
   */
  public int getSubjectId() {
    return subjectId;
  }

  /**
	 * @param subjectId the subjectId to set
	 */
  public void setSubjectId(int subjectId) {
    this.subjectId = subjectId;
  }

  /**
   * @return the cohortId
   */
  public int getsampleId() {
    return sampleId;
  }

  /**
   * @param sampleId the cohortId to set
   */
  public void setsampleId(int sampleId) {
    this.sampleId = sampleId;
  }

  /**
   * @return the setId
   */
  public int getannotationSetId() {
    return annotationSetId;
  }

  /**
   * @param setId the setId to set
   */
  public void setannotationSetId(int setId) {
    this.annotationSetId = setId;
  }

	/**
	 * @return the results
	 */
	public Set<ResultDto> getResults() {
			return new HashSet<ResultDto>(results);
	}

	/**
	 * @param results the results to set
	 */
	protected void setResults(Set<ResultDto> results) {
			this.results = results;
  }

  /**
   * @return the sample name
   */
  public String getSampleName() {
    return name;
  }

  /**
   * @param name to the slected sample name
   */
  public void setSampleName(String name) {
    this.name = name;
  }

}
