package org.ohdsi.webapi.cohortdefinition;

import org.ohdsi.analysis.CohortMetadata;
import org.ohdsi.webapi.tag.dto.TagDTO;

import java.util.List;
import java.util.Set;

public interface CohortMetadataExt extends CohortMetadata {
    Set<TagDTO> getTags();
}
