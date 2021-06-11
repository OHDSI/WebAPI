/*
 *   Copyright 2017 Observational Health Data Sciences and Informatics
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *   Authors: Vitaly Koulakov
 *
 */

package org.ohdsi.circe.check.checkers;

import static org.ohdsi.circe.check.operations.Operations.match;

import java.util.Objects;
import org.ohdsi.circe.check.WarningSeverity;
import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;
import org.ohdsi.circe.cohortdefinition.TreatmentLine;

public class TreatmentLineCheck extends BaseCorelatedCriteriaCheck {

    private static final String EMPTY_VALUE_ERROR = "Treatment Line of %s contains empty %s";
//    private static final String MISSING_DAYS_INFO = "Using drug era at %s criteria on medical claims (e.g., biologics) may not be accurate due to missing days supply information";

    @Override
    protected WarningSeverity defineSeverity() {

        return WarningSeverity.INFO;
    }

    protected void checkCriteria(CorelatedCriteria criteria, String groupName, WarningReporter reporter) {

        match(criteria.criteria)
                .isA(TreatmentLine.class)
                .then(c -> match(criteria)
                        .when(treatmentLine -> Objects.isNull(treatmentLine.startWindow.start) && Objects.isNull(treatmentLine.startWindow.end)
                                && Objects.isNull(treatmentLine.endWindow.start))
                        .then(() -> reporter.add(EMPTY_VALUE_ERROR, groupName)));
    }

}