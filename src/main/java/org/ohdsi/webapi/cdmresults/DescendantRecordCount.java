package org.ohdsi.webapi.cdmresults;

import java.util.Objects;

public class DescendantRecordCount{

    private int id;
    private long recordCount;
    private long descendantRecordCount;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(long recordCount) {
        this.recordCount = recordCount;
    }

    public long getDescendantRecordCount() {
        return descendantRecordCount;
    }

    public void setDescendantRecordCount(long descendantRecordCount) {
        this.descendantRecordCount = descendantRecordCount;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DescendantRecordCount that = (DescendantRecordCount) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}
