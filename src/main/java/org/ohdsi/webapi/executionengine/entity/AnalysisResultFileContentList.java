package org.ohdsi.webapi.executionengine.entity;

import java.util.ArrayList;
import java.util.List;

public class AnalysisResultFileContentList {
    private List<AnalysisResultFileContent> files;

    private boolean hasErrors;

    public AnalysisResultFileContentList() {
        this.files = new ArrayList<>();
    }

    public List<AnalysisResultFileContent> getFiles() {
        return files;
    }

    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    public boolean isSuccessfullyFiltered() {
        return !hasErrors;
    }
}
