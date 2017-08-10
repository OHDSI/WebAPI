package org.ohdsi.webapi.cohortresults;


/**
 *
 */
public class DataCompletenessAttr {
    private String covariance;
    private float genderP;
    private float raceP;
    private float ethP;
    
    /**
     * @return the covariance
     */
    public String getCovariance() {
        return covariance;
    }
    
    /**
     * @param covariance the covariance to set
     */
    public void setCovariance(String covariance) {
        this.covariance = covariance;
    }
    
    /**
     * @return the genderP
     */
    public float getGenderP() {
        return genderP;
    }
    
    /**
     * @param genderP the genderP to set
     */
    public void setGenderP(float genderP) {
        this.genderP = genderP;
    }
    
    /**
     * @return the raceP
     */
    public float getRaceP() {
        return raceP;
    }
    
    /**
     * @param raceP the raceP to set
     */
    public void setRaceP(float raceP) {
        this.raceP = raceP;
    }
    
    /**
     * @return the ethP
     */
    public float getEthP() {
        return ethP;
    }
    
    /**
     * @param ethP the ethP to set
     */
    public void setEthP(float ethP) {
        this.ethP = ethP;
    }
    
    
}
