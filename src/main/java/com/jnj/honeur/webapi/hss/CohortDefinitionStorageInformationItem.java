package com.jnj.honeur.webapi.hss;

public class CohortDefinitionStorageInformationItem extends StorageInformationItem {

    private CohortDefinitionStorageInformationItem previous;

    public CohortDefinitionStorageInformationItem getPrevious() {
        return previous;
    }

    public void setPrevious(CohortDefinitionStorageInformationItem previous) {
        this.previous = previous;
    }
}
