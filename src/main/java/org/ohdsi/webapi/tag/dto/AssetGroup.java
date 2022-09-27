package org.ohdsi.webapi.tag.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class AssetGroup {
    @JsonProperty
    private List<Integer> cohorts = new ArrayList<>();
    @JsonProperty
    private List<Integer> conceptSets = new ArrayList<>();
    @JsonProperty
    private List<Long> characterizations = new ArrayList<>();
    @JsonProperty
    private List<Integer> incidenceRates = new ArrayList<>();
    @JsonProperty
    private List<Integer> pathways = new ArrayList<>();
    @JsonProperty
    private List<Integer> reusables = new ArrayList<>();

    public List<Integer> getCohorts() {
        return cohorts;
    }

    public void setCohorts(List<Integer> cohorts) {
        this.cohorts = cohorts;
    }

    public List<Integer> getConceptSets() {
        return conceptSets;
    }

    public void setConceptSets(List<Integer> conceptSets) {
        this.conceptSets = conceptSets;
    }

    public List<Long> getCharacterizations() {
        return characterizations;
    }

    public void setCharacterizations(List<Long> characterizations) {
        this.characterizations = characterizations;
    }

    public List<Integer> getIncidenceRates() {
        return incidenceRates;
    }

    public void setIncidenceRates(List<Integer> incidenceRates) {
        this.incidenceRates = incidenceRates;
    }

    public List<Integer> getPathways() {
        return pathways;
    }

    public void setPathways(List<Integer> pathways) {
        this.pathways = pathways;
    }

    public List<Integer> getReusables() {
        return reusables;
    }

    public void setReusables(final List<Integer> reusables) {
        this.reusables = reusables;
    }
}