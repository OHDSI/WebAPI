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

package org.ohdsi.circe.check;

import org.ohdsi.circe.check.checkers.*;
import org.ohdsi.circe.cohortdefinition.CohortExpression;

import java.util.ArrayList;
import java.util.List;

public class Checker implements Check {

    private List<Check> getChecks() {
        List<Check> checks = new ArrayList<>();
        checks.add(new UnusedConceptsCheck());
        checks.add(new ExitCriteriaCheck());
        checks.add(new ExitCriteriaDaysOffsetCheck());
        checks.add(new RangeCheck());
        checks.add(new IncompleteRuleCheck());
        checks.add(new InitialEventCheck());
        checks.add(new NoExitCriteriaCheck());
        checks.add(new ConceptSetCriteriaCheck());
        checks.add(new GenderCriteriaCheck());
        checks.add(new DrugExposureCheck());
        checks.add(new TreatmentLineCheck());
        checks.add(new MeasurementCheck());
        checks.add(new DrugEraCheck());
        checks.add(new OcurrenceCheck());
        checks.add(new DuplicatesCriteriaCheck());
        checks.add(new DuplicatesConceptSetCheck());
        checks.add(new DrugDomainCheck());
        checks.add(new EmptyConceptSetCheck());
        checks.add(new EventsProgressionCheck());
        checks.add(new TimeWindowCheck());
        checks.add(new TimePatternCheck());
        checks.add(new EmptyDomainTypeCheck());
        checks.add(new DomainTypeCheck());
        checks.add(new CriteriaContradictionsCheck());
        checks.add(new DeathTimeWindowCheck());
        return checks;
    }

    @Override
    public List<Warning> check(final CohortExpression expression) {

        List<Warning> result = new ArrayList<>();
        for(Check check : getChecks()) {
            result.addAll(check.check(expression));
        }
        return result;
    }
}