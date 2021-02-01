package org.ohdsi.webapi.annotation.annotation;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.Id;
import javax.persistence.Table;
import org.ohdsi.webapi.annotation.result.Result;
import org.ohdsi.webapi.annotation.set.QuestionSet;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity(name = "Annotation")
@Table(
  name = "annotation"
)
public class Annotation {

  @Id
  @GeneratedValue
  @Column(name = "annotation_id")
  private Long id;

  @Column(name = "subject_id")
  private Long subjectId;

  @Column(name = "cohort_id")
  private Long cohortId;

  // @Column(name = "sample_name")
  // private String sampleName;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "set_id")
  private QuestionSet set;

  @OneToMany(
    fetch = FetchType.EAGER,
    mappedBy = "annotation",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  private Set<Result> results = new HashSet();

  //***** GETTERS/SETTERS ******

  /**
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
	 * @param id the id to set
	 */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * @return the subjectId
   */
  public Long getSubjectId() {
    return subjectId;
  }

  /**
   * @param subjectId the subjectId to set
   */
  public void setSubjectId(Long subjectId) {
    this.subjectId = subjectId;
  }

  /**
   * @return the cohortId
   */
  public Long getCohortId() {
    return cohortId;
  }

  /**
   * @param cohortId the cohortId to set
   */
  public void setCohortId(Long cohortId) {
    this.cohortId = cohortId;
  }

  /**
   * @return the set
   */
  public QuestionSet getSet() {
    return set;
  }

  /**
   * @param set the set to set
   */
  public void setSet(QuestionSet set) {
    this.set = set;
  }

	/**
	 * @return the results
	 */
	public Set<Result> getResults() {
			return new HashSet<Result>(results);
	}

	/**
	 * @param results the results to set
	 */
	protected void setResults(Set<Result> results) {
			this.results = results;
	}

  public void addToResults(Result result) {
      result.setAnnotation(this);
      this.results.add(result);
  }


// public void setSampleName(String name) {
//     this.sampleName = name;
// }

// public String getSampleName() {
//     return this.sampleName;
// }

}
