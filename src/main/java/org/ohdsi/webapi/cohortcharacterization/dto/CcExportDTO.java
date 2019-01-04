package org.ohdsi.webapi.cohortcharacterization.dto;

import org.ohdsi.analysis.cohortcharacterization.design.CohortCharacterization;
import org.ohdsi.analysis.cohortcharacterization.design.CohortCharacterizationStrata;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisDTO;

import java.util.Collection;
import java.util.Collections;

public class CcExportDTO extends BaseCcDTO<CohortDTO, FeAnalysisDTO> implements CohortCharacterization {
    @Override
    public Collection<? extends CohortCharacterizationStrata> getStratas() {
        return Collections.emptyList();
    }
}
