package org.ohdsi.webapi.executionengine.repository;

import org.ohdsi.webapi.executionengine.entity.AnalysisResultFileContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisResultFileContentRepository extends JpaRepository<AnalysisResultFileContent, Integer> {
}
