package org.ohdsi.webapi.report;

import java.util.List;

/**
 * Created by taa7016 on 10/4/2016.
 */
public class CDMDataDensity {
    private List<ConceptQuartileRecord> conceptsPerPerson;
    private List<SeriesPerPerson> recordsPerPerson;
    private List<SeriesPerPerson> totalRecords;

    public List<SeriesPerPerson> getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(List<SeriesPerPerson> totalRecords) {
        this.totalRecords = totalRecords;
    }

    public List<SeriesPerPerson> getRecordsPerPerson() {
        return recordsPerPerson;
    }

    public void setRecordsPerPerson(List<SeriesPerPerson> recordsPerPerson) {
        this.recordsPerPerson = recordsPerPerson;
    }

    public List<ConceptQuartileRecord> getConceptsPerPerson() {
        return conceptsPerPerson;
    }

    public void setConceptsPerPerson(List<ConceptQuartileRecord> conceptsPerPerson) {
        this.conceptsPerPerson = conceptsPerPerson;
    }
}
