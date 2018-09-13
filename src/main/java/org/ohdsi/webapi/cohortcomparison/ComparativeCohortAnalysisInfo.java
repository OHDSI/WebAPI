/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ohdsi.webapi.cohortcomparison;

import java.io.Serializable;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.webapi.service.dto.ComparativeCohortAnalysisDTO;

/**
 * @author Frank DeFalco <fdefalco@ohdsi.org>
 */
public class ComparativeCohortAnalysisInfo extends ComparativeCohortAnalysisDTO implements Serializable {
    private String treatmentCaption;
    private String treatmentCohortDefinition;
    private String comparatorCaption;
    private String comparatorCohortDefinition;
    private String outcomeCaption;
    private String outcomeCohortDefinition;
    private String psInclusionCaption;
    private ConceptSetExpression psInclusionConceptSet; 
    private String psInclusionConceptSetSql;
    private String psExclusionCaption;
    private ConceptSetExpression psExclusionConceptSet;
    private String psExclusionConceptSetSql;
    private String omInclusionCaption;
    private ConceptSetExpression omInclusionConceptSet;
    private String omInclusionConceptSetSql;
    private String omExclusionCaption;
    private ConceptSetExpression omExclusionConceptSet;
    private String omExclusionConceptSetSql;
    private String negativeControlCaption;
    private ConceptSetExpression negativeControlConceptSet;
    private String negativeControlConceptSetSql;
  
    /**
     * @return the comparatorCaption
     */
    public String getComparatorCaption() {
        return comparatorCaption;
    }

    /**
     * @return the comparatorCohortDefinition
     */
    public String getComparatorCohortDefinition() {
        return comparatorCohortDefinition;
    }

    /**
     * @return the negativeControlCaption
     */
    public String getNegativeControlCaption() {
        return negativeControlCaption;
    }

    /**
     * @return the negativeControlConceptSet
     */
    public ConceptSetExpression getNegativeControlConceptSet() {
        return negativeControlConceptSet;
    }

    /**
     * @return the omExclusionCaption
     */
    public String getOmExclusionCaption() {
        return omExclusionCaption;
    }

    /**
     * @return the omExclusionConceptSet
     */
    public ConceptSetExpression getOmExclusionConceptSet() {
        return omExclusionConceptSet;
    }

    /**
     * @return the omInclusionCaption
     */
    public String getOmInclusionCaption() {
        return omInclusionCaption;
    }

    /**
     * @return the omInclusionConceptSet
     */
    public ConceptSetExpression getOmInclusionConceptSet() {
        return omInclusionConceptSet;
    }

    /**
     * @return the outcomeCaption
     */
    public String getOutcomeCaption() {
        return outcomeCaption;
    }

    /**
     * @return the outcomeCohortDefinition
     */
    public String getOutcomeCohortDefinition() {
        return outcomeCohortDefinition;
    }

    /**
     * @return the psExclusionCaption
     */
    public String getPsExclusionCaption() {
        return psExclusionCaption;
    }

    /**
     * @return the psExclusionConceptSet
     */
    public ConceptSetExpression getPsExclusionConceptSet() {
        return psExclusionConceptSet;
    }

    /**
     * @return the psInclusionCaption
     */
    public String getPsInclusionCaption() {
        return psInclusionCaption;
    }

    /**
     * @return the psInclusionConceptSet
     */
    public ConceptSetExpression getPsInclusionConceptSet() {
        return psInclusionConceptSet;
    }

    /**
     * @return the treatmentCaption
     */
    public String getTreatmentCaption() {
        return treatmentCaption;
    }

    /**
     * @return the treatmentCohortDefinition
     */
    public String getTreatmentCohortDefinition() {
        return treatmentCohortDefinition;
    }

    /**
     * @param comparatorCaption the comparatorCaption to set
     */
    public void setComparatorCaption(String comparatorCaption) {
        this.comparatorCaption = comparatorCaption;
    }

    /**
     * @param comparatorCohortDefinition the comparatorCohortDefinition to set
     */
    public void setComparatorCohortDefinition(String comparatorCohortDefinition) {
        this.comparatorCohortDefinition = comparatorCohortDefinition;
    }

    /**
     * @param negativeControlCaption the negativeControlCaption to set
     */
    public void setNegativeControlCaption(String negativeControlCaption) {
        this.negativeControlCaption = negativeControlCaption;
    }

    /**
     * @param negativeControlConceptSet the negativeControlConceptSet to set
     */
    public void setNegativeControlConceptSet(ConceptSetExpression negativeControlConceptSet) {
        this.negativeControlConceptSet = negativeControlConceptSet;
    }

    /**
     * @param omExclusionCaption the omExclusionCaption to set
     */
    public void setOmExclusionCaption(String omExclusionCaption) {
        this.omExclusionCaption = omExclusionCaption;
    }

    /**
     * @param omExclusionConceptSet the omExclusionConceptSet to set
     */
    public void setOmExclusionConceptSet(ConceptSetExpression omExclusionConceptSet) {
        this.omExclusionConceptSet = omExclusionConceptSet;
    }

    /**
     * @param omInclusionCaption the omInclusionCaption to set
     */
    public void setOmInclusionCaption(String omInclusionCaption) {
        this.omInclusionCaption = omInclusionCaption;
    }

    /**
     * @param omInclusionConceptSet the omInclusionConceptSet to set
     */
    public void setOmInclusionConceptSet(ConceptSetExpression omInclusionConceptSet) {
        this.omInclusionConceptSet = omInclusionConceptSet;
    }

    /**
     * @param outcomeCaption the outcomeCaption to set
     */
    public void setOutcomeCaption(String outcomeCaption) {
        this.outcomeCaption = outcomeCaption;
    }

    /**
     * @param outcomeCohortDefinition the outcomeCohortDefinition to set
     */
    public void setOutcomeCohortDefinition(String outcomeCohortDefinition) {
        this.outcomeCohortDefinition = outcomeCohortDefinition;
    }

    /**
     * @param psExclusionCaption the psExclusionCaption to set
     */
    public void setPsExclusionCaption(String psExclusionCaption) {
        this.psExclusionCaption = psExclusionCaption;
    }

    /**
     * @param psExclusionConceptSet the psExclusionConceptSet to set
     */
    public void setPsExclusionConceptSet(ConceptSetExpression psExclusionConceptSet) {
        this.psExclusionConceptSet = psExclusionConceptSet;
    }

    /**
     * @param psInclusionCaption the psInclusionCaption to set
     */
    public void setPsInclusionCaption(String psInclusionCaption) {
        this.psInclusionCaption = psInclusionCaption;
    }

    /**
     * @param psInclusionConceptSet the psInclusionConceptSet to set
     */
    public void setPsInclusionConceptSet(ConceptSetExpression psInclusionConceptSet) {
        this.psInclusionConceptSet = psInclusionConceptSet;
    }

    /**
     * @param treatmentCaption the treatmentCaption to set
     */
    public void setTreatmentCaption(String treatmentCaption) {
        this.treatmentCaption = treatmentCaption;
    }

    /**
     * @param treatmentCohortDefinition the treatmentCohortDefinition to set
     */
    public void setTreatmentCohortDefinition(String treatmentCohortDefinition) {
        this.treatmentCohortDefinition = treatmentCohortDefinition;
    }

    /**
     * @return the negativeControlConceptSetSql
     */
    public String getNegativeControlConceptSetSql() {
        return negativeControlConceptSetSql;
    }

    /**
     * @return the omExclusionConceptSetSql
     */
    public String getOmExclusionConceptSetSql() {
        return omExclusionConceptSetSql;
    }

    /**
     * @return the omInclusionConceptSetSql
     */
    public String getOmInclusionConceptSetSql() {
        return omInclusionConceptSetSql;
    }

    /**
     * @return the psExclusionConceptSetSql
     */
    public String getPsExclusionConceptSetSql() {
        return psExclusionConceptSetSql;
    }

    /**
     * @return the psInclusionConceptSetSql
     */
    public String getPsInclusionConceptSetSql() {
        return psInclusionConceptSetSql;
    }

    /**
     * @param negativeControlConceptSetSql the negativeControlConceptSetSql to set
     */
    public void setNegativeControlConceptSetSql(String negativeControlConceptSetSql) {
        this.negativeControlConceptSetSql = negativeControlConceptSetSql;
    }

    /**
     * @param omExclusionConceptSetSql the omExclusionConceptSetSql to set
     */
    public void setOmExclusionConceptSetSql(String omExclusionConceptSetSql) {
        this.omExclusionConceptSetSql = omExclusionConceptSetSql;
    }

    /**
     * @param omInclusionConceptSetSql the omInclusionConceptSetSql to set
     */
    public void setOmInclusionConceptSetSql(String omInclusionConceptSetSql) {
        this.omInclusionConceptSetSql = omInclusionConceptSetSql;
    }

    /**
     * @param psExclusionConceptSetSql the psExclusionConceptSetSql to set
     */
    public void setPsExclusionConceptSetSql(String psExclusionConceptSetSql) {
        this.psExclusionConceptSetSql = psExclusionConceptSetSql;
    }

    /**
     * @param psInclusionConceptSetSql the psInclusionConceptSetSql to set
     */
    public void setPsInclusionConceptSetSql(String psInclusionConceptSetSql) {
        this.psInclusionConceptSetSql = psInclusionConceptSetSql;
    }
}
