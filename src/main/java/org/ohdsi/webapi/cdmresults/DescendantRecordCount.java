/*
 *
 * Copyright 2017 Observational Health Data Sciences and Informatics
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.ohdsi.webapi.cdmresults;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DescendantRecordCount {

    private int id;
    private Long recordCount;
    private Long descendantRecordCount;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Long getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Long recordCount) {
        this.recordCount = recordCount;
    }

    public Long getDescendantRecordCount() {
        return descendantRecordCount;
    }

    public void setDescendantRecordCount(Long descendantRecordCount) {
        this.descendantRecordCount = descendantRecordCount;
    }

    public List<Long> getValues() {
        return Arrays.asList(recordCount, descendantRecordCount);
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
