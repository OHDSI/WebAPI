package org.ohdsi.webapi.cohortcharacterization.report;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.cohortcharacterization.dto.CcPrevalenceStat;

import java.util.ArrayList;
import java.util.List;

public abstract class ExportItem<T extends ExportItem> implements Comparable<T> {
    public final Integer analysisId;
    public final String analysisName;
    public final Long strataId;
    public final String strataName;
    public final Long covariateId;
    public final String covariateName;
    public final String covariateShortName;
    public final String faType;
    public String domainId;
    public final Long conceptId;
    public final String conceptName;

    public ExportItem(CcPrevalenceStat ccResult) {
        this.analysisId = ccResult.getAnalysisId();
        this.analysisName = ccResult.getAnalysisName();
        this.strataId = ccResult.getStrataId();
        this.strataName = getStrataName(ccResult.getStrataName());
        this.covariateId = ccResult.getCovariateId();
        this.covariateName = ccResult.getCovariateName();
        this.covariateShortName = extractMeaningfulCovariateName(ccResult.getCovariateName());
        this.faType = ccResult.getFaType();
        this.conceptId = ccResult.getConceptId();
        this.conceptName = ccResult.getConceptName();
    }

    public ExportItem(PrevalenceItem item) {
        this.analysisId = item.analysisId;
        this.analysisName = item.analysisName;
        this.strataId = item.strataId;
        this.strataName = getStrataName(item.strataName);;
        this.covariateId = item.covariateId;
        this.covariateName = item.covariateName;
        this.covariateShortName = extractMeaningfulCovariateName(item.covariateName);
        this.faType = item.faType;
        this.conceptId = item.conceptId;
        this.conceptName = item.conceptName;
    }

    protected List<String> getValueList() {
        List<String> values = new ArrayList<>();
        values.add(String.valueOf(this.analysisId));
        values.add(this.analysisName);
        values.add(String.valueOf(this.strataId));
        values.add(this.strataName);
        values.add(String.valueOf(this.covariateId));
        values.add(this.covariateName);
        values.add(this.covariateShortName);
        return values;
    }

    @Override
    public int compareTo(ExportItem that) {
        int res = analysisId.compareTo(analysisId);
        if (res == 0) {
            covariateName.compareToIgnoreCase(that.covariateName);
        }
        return res;
    }

    @JsonIgnore
    public String[] getValueArray() {
        List<String> values = getValueList();
        return values.toArray(new String[values.size()]);
    }

    protected String extractMeaningfulCovariateName(String fullName) {
        if (fullName == null) {
            return StringUtils.EMPTY;
        }
        String[] nameParts = fullName.split(":");
        if (nameParts.length < 2) {
            nameParts = fullName.split("=");
        }
        if (nameParts.length != 2) {
            return fullName;
        } else {
            return nameParts[1];
        }
    }

    protected String getStrataName(String value) {
        return StringUtils.isNotEmpty(value) ? value : "All stratas";
    }

    protected double calcDiff(T another) {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExportItem<?> that = (ExportItem<?>) o;

        if (analysisId != null ? !analysisId.equals(that.analysisId) : that.analysisId != null) return false;
        if (strataId != null ? !strataId.equals(that.strataId) : that.strataId != null) return false;
        if (covariateId != null ? !covariateId.equals(that.covariateId) : that.covariateId != null) return false;
        if (domainId != null ? !domainId.equals(that.domainId) : that.domainId != null) return false;
        if (conceptId != null ? !conceptId.equals(that.conceptId) : that.conceptId != null) return false;
        return conceptName != null ? conceptName.equals(that.conceptName) : that.conceptName == null;
    }

    @Override
    public int hashCode() {
        int result = analysisId != null ? analysisId.hashCode() : 0;
        result = 31 * result + (strataId != null ? strataId.hashCode() : 0);
        result = 31 * result + (covariateId != null ? covariateId.hashCode() : 0);
        result = 31 * result + (domainId != null ? domainId.hashCode() : 0);
        result = 31 * result + (conceptId != null ? conceptId.hashCode() : 0);
        return result;
    }
}
