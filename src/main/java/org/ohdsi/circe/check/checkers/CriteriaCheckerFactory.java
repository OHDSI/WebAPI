package org.ohdsi.circe.check.checkers;

import java.util.Objects;
import java.util.function.Function;

import org.ohdsi.circe.cohortdefinition.*;

class CriteriaCheckerFactory {

    private ConceptSet conceptSet;

    private CriteriaCheckerFactory(ConceptSet concept) {

        this.conceptSet = concept;
    }

    static CriteriaCheckerFactory getFactory(ConceptSet conceptSet) {
        return new CriteriaCheckerFactory(conceptSet);
    }

    Function<Criteria, Boolean> getCriteriaChecker(Criteria criteria) {
        Function<Criteria, Boolean> result = c -> false;
        if (criteria instanceof ConditionEra) {
            result = c -> Objects.equals(((ConditionEra)c).codesetId, conceptSet.id);
        } else if (criteria instanceof ConditionOccurrence) {
            result = c ->
                    Objects.equals(((ConditionOccurrence)c).codesetId, conceptSet.id) || Objects.equals(((ConditionOccurrence)c).conditionSourceConcept, conceptSet.id);
        } else if (criteria instanceof Death) {
            result = c -> Objects.equals(((Death)c).codesetId, conceptSet.id);
        } else if (criteria instanceof DeviceExposure) {
            result = c -> Objects.equals(((DeviceExposure)c).codesetId, conceptSet.id);
        } else if (criteria instanceof DoseEra) {
            result = c -> Objects.equals(((DoseEra)c).codesetId, conceptSet.id);
        } else if (criteria instanceof DrugEra) {
            result = c -> Objects.equals(((DrugEra)c).codesetId, conceptSet.id);
        } else if (criteria instanceof DrugExposure) {
            result = c -> Objects.equals(((DrugExposure)c).codesetId, conceptSet.id) || Objects.equals(((DrugExposure)c).drugSourceConcept, conceptSet.id);
        } else if (criteria instanceof TreatmentLine) {
            result = c -> Objects.equals(((TreatmentLine)c).codesetId, conceptSet.id);
        } else if (criteria instanceof Measurement) {
            result = c -> Objects.equals(((Measurement)c).codesetId, conceptSet.id) || Objects.equals(((Measurement)c).measurementSourceConcept, conceptSet.id);
        } else if (criteria instanceof Observation) {
            result = c -> Objects.equals(((Observation)c).codesetId, conceptSet.id) || Objects.equals(((Observation)c).observationSourceConcept, conceptSet.id);
        } else if (criteria instanceof ProcedureOccurrence) {
            result = c -> Objects.equals(((ProcedureOccurrence)c).codesetId, conceptSet.id) || Objects.equals(((ProcedureOccurrence)c).procedureSourceConcept, conceptSet.id);
        } else if (criteria instanceof Specimen) {
            result = c -> Objects.equals(((Specimen)c).codesetId, conceptSet.id);
        } else if (criteria instanceof VisitOccurrence) {
            result = c -> Objects.equals(((VisitOccurrence) c).codesetId, conceptSet.id);
        } else if (criteria instanceof LocationRegion) {
            result = c -> Objects.equals(((LocationRegion) c).codesetId, conceptSet.id);
        }
        return result;
    }
}