package org.ohdsi.webapi.cohortsample.dto;

import org.ohdsi.webapi.cohortsample.SampleElement;

import java.util.List;

public class CohortSampleDTO {
    private int id;
    private int size;
    private List<SampleElement> elements;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<SampleElement> getElements() {
        return elements;
    }

    public void setElements(List<SampleElement> elements) {
        this.elements = elements;
    }
}
