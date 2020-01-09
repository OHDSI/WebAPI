package org.ohdsi.webapi.cohortsample.dto;

import javax.ws.rs.BadRequestException;

public class SampleParametersDTO {
    private static final int SIZE_MAX = 500;
    private static final int AGE_MAX = 500;

    private int size;
    private Integer ageMin;
    private Integer ageMax;
    private Integer genderConceptId;

    public void validate() {
        if (size <= 0) {
            throw new BadRequestException("sample paramater size must fall in the range (1, " + SIZE_MAX + ")");
        }
        if (size > SIZE_MAX) {
            throw new BadRequestException("sample paramater size must fall in the range (1, " + SIZE_MAX + ")");
        }
        if (ageMin != null) {
            if (ageMin < 0) {
                throw new BadRequestException("Minimum age may not be less than 0");
            }
            if (ageMin >= AGE_MAX) {
                throw new BadRequestException("Minimum age must be smaller than " + AGE_MAX);
            }
        }
        if (ageMax != null) {
            if (ageMax < 0) {
                throw new BadRequestException("Maximum age may not be less than 0");
            }
            if (ageMax >= 500) {
                throw new BadRequestException("Minimum age must be smaller than " + AGE_MAX);
            }
            if (ageMin != null && ageMax < ageMin) {
                throw new BadRequestException("Maximum age " + ageMax + " may not be less than minimum age " + ageMin);
            }
        }
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

    public Integer getGenderConceptId() {
        return genderConceptId;
    }

    public void setGenderConceptId(Integer genderConceptId) {
        this.genderConceptId = genderConceptId;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
