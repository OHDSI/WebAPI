/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.evidence;

import java.io.Serializable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author asena5
 */
@Entity(name = "NegativeControlRecord")
@Table(name = "CONCEPT_SET_NEGATIVE_CONTROLS")
public class NegativeControlRecord implements Serializable {

    @Id
    @GeneratedValue  
    @Access(AccessType.PROPERTY) 
    @Column(name = "id")
    private int id;
    
    @Column(name = "source_id")
    private int sourceId;

    @Column(name = "concept_set_id")
    private int conceptSetId;

    @Column(name = "concept_set_name")
    private String conceptSetName;

    @Column(name = "concept_id")
    private int conceptId;

    @Column(name = "concept_name")
    private String conceptName;

    @Column(name = "domain_id")
    private String domainId;

    @Column(name = "medline_ct")
    private double medlineCt;

    @Column(name = "medline_case")
    private double medlineCase;

    @Column(name = "medline_other")
    private double medlineOther;

    @Column(name = "semmeddb_ct_t")
    private double semmeddbCtT;

    @Column(name = "semmeddb_case_t")
    private double semmeddbCaseT;

    @Column(name = "semmeddb_other_t")
    private double semmeddbOtherT;

    @Column(name = "semmeddb_ct_f")
    private double semmeddbCtF;

    @Column(name = "semmeddb_case_f")
    private double semmeddbCaseF;

    @Column(name = "semmeddb_other_f")
    private double semmeddbOtherF;

    @Column(name = "eu_spc")
    private double euSPC;

    @Column(name = "spl_adr")
    private double splADR;

    @Column(name = "aers")
    private double aers;

    @Column(name = "aers_prr")
    private double aersPRR;

    @Column(name = "medline_ct_scaled")
    private double medlineCtScaled;

    @Column(name = "medline_case_scaled")
    private double medlineCaseScaled;

    @Column(name = "medline_other_scaled")
    private double medlineOtherScaled;

    @Column(name = "semmeddb_ct_t_scaled")
    private double semmeddbCtTScaled;

    @Column(name = "semmeddb_case_t_scaled")
    private double semmeddbCaseTScaled;

    @Column(name = "semmeddb_other_t_scaled")
    private double semmeddbOtherTScaled;

    @Column(name = "semmeddb_ct_f_scaled")
    private double semmeddbCtFScaled;

    @Column(name = "semmeddb_case_f_scaled")
    private double semmeddbCaseFScaled;

    @Column(name = "semmeddb_other_f_scaled")
    private double semmeddbOtherFScaled;

    @Column(name = "eu_spc_scaled")
    private double euSPCScaled;

    @Column(name = "spl_adr_scaled")
    private double splADRScaled;

    @Column(name = "aers_scaled")
    private double aersScaled;

    @Column(name = "aers_prr_scaled")
    private double aersPRRScaled;

    @Column(name = "medline_ct_beta")
    private double medlineCtBeta;

    @Column(name = "medline_case_beta")
    private double medlineCaseBeta;

    @Column(name = "medline_other_beta")
    private double medlineOtherBeta;

    @Column(name = "semmeddb_ct_t_beta")
    private double semmeddbCtTBeta;

    @Column(name = "semmeddb_case_t_beta")
    private double semmeddbCaseTBeta;

    @Column(name = "semmeddb_other_t_beta")
    private double semmeddbOtherTBeta;

    @Column(name = "semmeddb_ct_f_beta")
    private double semmeddbCtFBeta;

    @Column(name = "semmeddb_case_f_beta")
    private double semmeddbCaseFBeta;

    @Column(name = "semmeddb_other_f_beta")
    private double semmeddbOtherFBeta;

    @Column(name = "eu_spc_beta")
    private double euSPCBeta;

    @Column(name = "spl_adr_beta")
    private double splADRBeta;

    @Column(name = "aers_beta")
    private double aersBeta;

    @Column(name = "aers_prr_beta")
    private double aersPRRBeta;

    @Column(name = "raw_prediction")
    private double rawPrediction;

    @Column(name = "prediction")
    private double prediction;

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
     * @return the aersPRR
     */
    public double getAersPRR() {
        return aersPRR;
    }

    /**
     * @return the aersPRRBeta
     */
    public double getAersPRRBeta() {
        return aersPRRBeta;
    }

    /**
     * @return the aersPRRScaled
     */
    public double getAersPRRScaled() {
        return aersPRRScaled;
    }

    /**
     * @return the aers
     */
    public double getAers() {
        return aers;
    }

    /**
     * @return the aersBeta
     */
    public double getAersBeta() {
        return aersBeta;
    }

    /**
     * @return the aersScaled
     */
    public double getAersScaled() {
        return aersScaled;
    }

    /**
     * @return the conceptId
     */
    public int getConceptId() {
        return conceptId;
    }

    /**
     * @return the conceptName
     */
    public String getConceptName() {
        return conceptName;
    }

    /**
     * @return the conceptSetId
     */
    public int getConceptSetId() {
        return conceptSetId;
    }

    /**
     * @return the domainId
     */
    public String getDomainId() {
        return domainId;
    }

    /**
     * @return the euSPC
     */
    public double getEu_spc() {
        return getEuSPC();
    }

    /**
     * @return the euSPCBeta
     */
    public double getEuSPCBeta() {
        return euSPCBeta;
    }

    /**
     * @return the euSPCScaled
     */
    public double getEuSPCScaled() {
        return euSPCScaled;
    }

    /**
     * @return the medlineCase
     */
    public double getMedlineCase() {
        return medlineCase;
    }

    /**
     * @return the medlineCaseBeta
     */
    public double getMedlineCaseBeta() {
        return medlineCaseBeta;
    }

    /**
     * @return the medlineCaseScaled
     */
    public double getMedlineCaseScaled() {
        return medlineCaseScaled;
    }

    /**
     * @return the medlineCt
     */
    public double getMedlineCt() {
        return medlineCt;
    }

    /**
     * @return the medlineCtScaled
     */
    public double getMedlineCtScaled() {
        return medlineCtScaled;
    }

    /**
     * @return the medlineOther
     */
    public double getMedlineOther() {
        return medlineOther;
    }

    /**
     * @return the medlineOtherBeta
     */
    public double getMedlineOtherBeta() {
        return medlineOtherBeta;
    }

    /**
     * @return the medlineOtherScaled
     */
    public double getMedlineOtherScaled() {
        return medlineOtherScaled;
    }

    /**
     * @return the prediction
     */
    public double getPrediction() {
        return prediction;
    }

    /**
     * @return the semmeddbCaseF
     */
    public double getSemmeddbCaseF() {
        return semmeddbCaseF;
    }

    /**
     * @return the semmeddbCaseFBeta
     */
    public double getSemmeddbCaseFBeta() {
        return semmeddbCaseFBeta;
    }

    /**
     * @return the semmeddbCaseFScaled
     */
    public double getSemmeddbCaseFScaled() {
        return semmeddbCaseFScaled;
    }

    /**
     * @return the semmeddbCaseT
     */
    public double getSemmeddbCaseT() {
        return semmeddbCaseT;
    }

    /**
     * @return the semmeddbCaseTBeta
     */
    public double getSemmeddbCaseTBeta() {
        return semmeddbCaseTBeta;
    }

    /**
     * @return the semmeddbCtF
     */
    public double getSemmeddbCtF() {
        return semmeddbCtF;
    }

    /**
     * @return the semmeddbCtFBeta
     */
    public double getSemmeddbCtFBeta() {
        return semmeddbCtFBeta;
    }

    /**
     * @return the semmeddbCtT
     */
    public double getSemmeddbCtT() {
        return semmeddbCtT;
    }

    /**
     * @return the semmeddbCtTBeta
     */
    public double getSemmeddbCtTBeta() {
        return semmeddbCtTBeta;
    }

    /**
     * @return the semmeddbCtTScaled
     */
    public double getSemmeddbCtTScaled() {
        return semmeddbCtTScaled;
    }

    /**
     * @return the semmeddbOtherFBeta
     */
    public double getSemmeddbOtherFBeta() {
        return semmeddbOtherFBeta;
    }

    /**
     * @return the semmeddbOtherT
     */
    public double getSemmeddbOtherT() {
        return semmeddbOtherT;
    }

    /**
     * @return the semmeddbOtherTBeta
     */
    public double getSemmeddbOtherTBeta() {
        return semmeddbOtherTBeta;
    }

    /**
     * @return the semmeddbOtherTScaled
     */
    public double getSemmeddbOtherTScaled() {
        return semmeddbOtherTScaled;
    }

    /**
     * @return the splADR
     */
    public double getSplADR() {
        return splADR;
    }

    /**
     * @return the splADRBeta
     */
    public double getSplADRBeta() {
        return splADRBeta;
    }

    /**
     * @return the splADRScaled
     */
    public double getSplADRScaled() {
        return splADRScaled;
    }

    /**
     * @param aersPRR the aersPRR to set
     */
    public void setAersPRR(double aersPRR) {
        this.aersPRR = aersPRR;
    }

    /**
     * @param aersPRRBeta the aersPRRBeta to set
     */
    public void setAersPRRBeta(double aersPRRBeta) {
        this.aersPRRBeta = aersPRRBeta;
    }

    /**
     * @param aersPRRScaled the aersPRRScaled to set
     */
    public void setAersPRRScaled(double aersPRRScaled) {
        this.aersPRRScaled = aersPRRScaled;
    }

    /**
     * @param aers the aers to set
     */
    public void setAers(double aers) {
        this.aers = aers;
    }

    /**
     * @param aersBeta the aersBeta to set
     */
    public void setAersBeta(double aersBeta) {
        this.aersBeta = aersBeta;
    }

    /**
     * @param aersScaled the aersScaled to set
     */
    public void setAersScaled(double aersScaled) {
        this.aersScaled = aersScaled;
    }

    /**
     * @param conceptId the conceptId to set
     */
    public void setConceptId(int conceptId) {
        this.conceptId = conceptId;
    }

    /**
     * @param concept_name the conceptName to set
     */
    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    /**
     * @param conceptSetId the conceptSetId to set
     */
    public void setConceptSetId(int conceptSetId) {
        this.conceptSetId = conceptSetId;
    }

    /**
     * @param domain_id the domainId to set
     */
    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    /**
     * @param eu_spc the euSPC to set
     */
    public void setEu_spc(double eu_spc) {
        this.setEuSPC(eu_spc);
    }

    /**
     * @param euSPCBeta the euSPCBeta to set
     */
    public void setEuSPCBeta(double euSPCBeta) {
        this.euSPCBeta = euSPCBeta;
    }

    /**
     * @param euSPCScaled the euSPCScaled to set
     */
    public void setEuSPCScaled(double euSPCScaled) {
        this.euSPCScaled = euSPCScaled;
    }

    /**
     * @param medlineCase the medlineCase to set
     */
    public void setMedlineCase(double medlineCase) {
        this.medlineCase = medlineCase;
    }

    /**
     * @param medlineCaseBeta the medlineCaseBeta to set
     */
    public void setMedlineCaseBeta(double medlineCaseBeta) {
        this.medlineCaseBeta = medlineCaseBeta;
    }

    /**
     * @param medlineCaseScaled the medlineCaseScaled to set
     */
    public void setMedlineCaseScaled(double medlineCaseScaled) {
        this.medlineCaseScaled = medlineCaseScaled;
    }

    /**
     * @param medline_ct the medlineCt to set
     */
    public void setMedlineCt(double medlineCt) {
        this.medlineCt = medlineCt;
    }

    /**
     * @param medlineCtScaled the medlineCtScaled to set
     */
    public void setMedlineCtScaled(double medlineCtScaled) {
        this.medlineCtScaled = medlineCtScaled;
    }

    /**
     * @param medlineOther the medlineOther to set
     */
    public void setMedlineOther(double medlineOther) {
        this.medlineOther = medlineOther;
    }

    /**
     * @param medlineOtherBeta the medlineOtherBeta to set
     */
    public void setMedlineOtherBeta(double medlineOtherBeta) {
        this.medlineOtherBeta = medlineOtherBeta;
    }

    /**
     * @param medlineOtherScaled the medlineOtherScaled to set
     */
    public void setMedlineOtherScaled(double medlineOtherScaled) {
        this.medlineOtherScaled = medlineOtherScaled;
    }

    /**
     * @param prediction the prediction to set
     */
    public void setPrediction(double prediction) {
        this.prediction = prediction;
    }

    /**
     * @param semmeddbCaseF the semmeddbCaseF to set
     */
    public void setSemmeddbCaseF(double semmeddbCaseF) {
        this.semmeddbCaseF = semmeddbCaseF;
    }

    /**
     * @param semmeddbCaseFBeta the semmeddbCaseFBeta to set
     */
    public void setSemmeddbCaseFBeta(double semmeddbCaseFBeta) {
        this.semmeddbCaseFBeta = semmeddbCaseFBeta;
    }

    /**
     * @param semmeddbCaseFScaled the semmeddbCaseFScaled to set
     */
    public void setSemmeddbCaseFScaled(double semmeddbCaseFScaled) {
        this.semmeddbCaseFScaled = semmeddbCaseFScaled;
    }

    /**
     * @param semmeddbCaseT the semmeddbCaseT to set
     */
    public void setSemmeddbCaseT(double semmeddbCaseT) {
        this.semmeddbCaseT = semmeddbCaseT;
    }

    /**
     * @param semmeddbCaseTBeta the semmeddbCaseTBeta to set
     */
    public void setSemmeddbCaseTBeta(double semmeddbCaseTBeta) {
        this.semmeddbCaseTBeta = semmeddbCaseTBeta;
    }

    /**
     * @param semmeddbCtF the semmeddbCtF to set
     */
    public void setSemmeddbCtF(double semmeddbCtF) {
        this.semmeddbCtF = semmeddbCtF;
    }

    /**
     * @param semmeddbCtFBeta the semmeddbCtFBeta to set
     */
    public void setSemmeddbCtFBeta(double semmeddbCtFBeta) {
        this.semmeddbCtFBeta = semmeddbCtFBeta;
    }


    /**
     * @param semmeddbCtT the semmeddbCtT to set
     */
    public void setSemmeddbCtT(double semmeddbCtT) {
        this.semmeddbCtT = semmeddbCtT;
    }

    /**
     * @param semmeddbCtTBeta the semmeddbCtTBeta to set
     */
    public void setSemmeddbCtTBeta(double semmeddbCtTBeta) {
        this.semmeddbCtTBeta = semmeddbCtTBeta;
    }

    /**
     * @param semmeddbCtTScaled the semmeddbCtTScaled to set
     */
    public void setSemmeddbCtTScaled(double semmeddbCtTScaled) {
        this.semmeddbCtTScaled = semmeddbCtTScaled;
    }

    /**
     * @param semmeddbOtherFBeta the semmeddbOtherFBeta to set
     */
    public void setSemmeddbOtherFBeta(double semmeddbOtherFBeta) {
        this.semmeddbOtherFBeta = semmeddbOtherFBeta;
    }

    /**
     * @param semmeddbOtherT the semmeddbOtherT to set
     */
    public void setSemmeddbOtherT(double semmeddbOtherT) {
        this.semmeddbOtherT = semmeddbOtherT;
    }

    /**
     * @param semmeddbOtherTBeta the semmeddbOtherTBeta to set
     */
    public void setSemmeddbOtherTBeta(double semmeddbOtherTBeta) {
        this.semmeddbOtherTBeta = semmeddbOtherTBeta;
    }

    /**
     * @param semmeddbOtherTScaled the semmeddbOtherTScaled to set
     */
    public void setSemmeddbOtherTScaled(double semmeddbOtherTScaled) {
        this.semmeddbOtherTScaled = semmeddbOtherTScaled;
    }

    /**
     * @param splADR the splADR to set
     */
    public void setSplADR(double splADR) {
        this.splADR = splADR;
    }

    /**
     * @param splADRBeta the splADRBeta to set
     */
    public void setSplADRBeta(double splADRBeta) {
        this.splADRBeta = splADRBeta;
    }

    /**
     * @param splADRScaled the splADRScaled to set
     */
    public void setSplADRScaled(double splADRScaled) {
        this.splADRScaled = splADRScaled;
    }

    /**
     * @return the conceptSetName
     */
    public String getConceptSetName() {
        return conceptSetName;
    }

    /**
     * @param conceptSetName the conceptSetName to set
     */
    public void setConceptSetName(String conceptSetName) {
        this.conceptSetName = conceptSetName;
    }

    /**
     * @return the rawPrediction
     */
    public double getRawPrediction() {
        return rawPrediction;
    }

    /**
     * @param rawPrediction the rawPrediction to set
     */
    public void setRawPrediction(double rawPrediction) {
        this.rawPrediction = rawPrediction;
    }

    /**
     * @return the euSPC
     */
    public double getEuSPC() {
        return euSPC;
    }

    /**
     * @param euSPC the euSPC to set
     */
    public void setEuSPC(double euSPC) {
        this.euSPC = euSPC;
    }

    /**
     * @return the semmeddbCtFScaled
     */
    public double getSemmeddbCtFScaled() {
        return semmeddbCtFScaled;
    }

    /**
     * @param semmeddbCtFScaled the semmeddbCtFScaled to set
     */
    public void setSemmeddbCtFScaled(double semmeddbCtFScaled) {
        this.semmeddbCtFScaled = semmeddbCtFScaled;
    }

    /**
     * @return the medlineCtBeta
     */
    public double getMedlineCtBeta() {
        return medlineCtBeta;
    }

    /**
     * @param medlineCtBeta the medlineCtBeta to set
     */
    public void setMedlineCtBeta(double medlineCtBeta) {
        this.medlineCtBeta = medlineCtBeta;
    }

    /**
     * @return the semmeddbCaseTScaled
     */
    public double getSemmeddbCaseTScaled() {
        return semmeddbCaseTScaled;
    }

    /**
     * @return the semmeddbOtherF
     */
    public double getSemmeddbOtherF() {
        return semmeddbOtherF;
    }

    /**
     * @return the semmeddbOtherFScaled
     */
    public double getSemmeddbOtherFScaled() {
        return semmeddbOtherFScaled;
    }

    /**
     * @param semmeddbCaseTScaled the semmeddbCaseTScaled to set
     */
    public void setSemmeddbCaseTScaled(double semmeddbCaseTScaled) {
        this.semmeddbCaseTScaled = semmeddbCaseTScaled;
    }

    /**
     * @param semmeddbOtherF the semmeddbOtherF to set
     */
    public void setSemmeddbOtherF(double semmeddbOtherF) {
        this.semmeddbOtherF = semmeddbOtherF;
    }

    /**
     * @param semmeddbOtherFScaled the semmeddbOtherFScaled to set
     */
    public void setSemmeddbOtherFScaled(double semmeddbOtherFScaled) {
        this.semmeddbOtherFScaled = semmeddbOtherFScaled;
    }

    /**
     * @return the sourceId
     */
    public int getSourceId() {
        return sourceId;
    }

    /**
     * @param sourceId the sourceId to set
     */
    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

}
