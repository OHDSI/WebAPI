package org.ohdsi.webapi.cohortcharacterization.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.ohdsi.analysis.cohortcharacterization.design.CohortCharacterization;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisDTO;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CcExportDTO extends BaseCcDTO<CohortDTO, FeAnalysisDTO> implements CohortCharacterization {
}
