package org.ohdsi.webapi.pathway.dto;

import org.ohdsi.analysis.pathway.design.PathwayAnalysis;
import org.springframework.stereotype.Component;

@Component
public class PathwayAnalysisExportDTO extends BasePathwayAnalysisDTO<PathwayCohortExportDTO> implements PathwayAnalysis {
}
