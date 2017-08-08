package org.ohdsi.webapi.cohortresults;

/**
 *
 */
public class EntropyAttr {
    
    private String date;
    
    private float entropy;
    
    private String insitution;
    
    /**
     * @return the date
     */
    public String getDate() {
        return date;
    }
    
    /**
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date = date;
    }
    
    /**
     * @return the entropy
     */
    public float getEntropy() {
        return entropy;
    }
    
    /**
     * @param entropy the entropy to set
     */
    public void setEntropy(float entropy) {
        this.entropy = entropy;
    }
    
    /**
     * @return the insitution
     */
    public String getInsitution() {
        return insitution;
    }
    
    /**
     * @param insitution the insitution to set
     */
    public void setInsitution(String insitution) {
        this.insitution = insitution;
    }
    
}
