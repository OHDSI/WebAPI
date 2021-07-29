package org.ohdsi.webapi.estimation.dto;

public class EstimationDTO extends EstimationShortDTO {
    private String specification;

    public String getSpecification() {

        return specification;
    }

    public void setSpecification(String specification) {

        this.specification = specification;
    }
}
