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

public class DescendantRecordAndPersonCount extends DescendantRecordCount {
    private Long personCount;
    private Long descendantPersonCount;

    public Long getPersonCount() {
        return personCount;
    }

    public void setPersonCount(Long personCount) {
        this.personCount = personCount;
    }

    public Long getDescendantPersonCount() {
        return descendantPersonCount;
    }

    public void setDescendantPersonCount(Long descendantPersonCount) {
        this.descendantPersonCount = descendantPersonCount;
    }

    public List<Long> getValues() {
        return Arrays.asList(getRecordCount(), getDescendantRecordCount(), personCount, descendantPersonCount);
    }
}
