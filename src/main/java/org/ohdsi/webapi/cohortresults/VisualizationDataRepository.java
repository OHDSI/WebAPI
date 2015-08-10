package org.ohdsi.webapi.cohortresults;

import org.springframework.data.repository.CrudRepository;

public interface VisualizationDataRepository extends CrudRepository<VisualizationData, Long> {

	public VisualizationData findByCohortDefinitionIdAndSourceIdAndVisualizationKey(
				int cohortDefinitionId,
				int sourceId,
				String visualizationKey
			);
	
	public VisualizationData findByCohortDefinitionIdAndSourceIdAndVisualizationKeyAndDrilldownId(
			int cohortDefinitionId,
			int sourceId,
			String visualizationKey,
			int drilldownId
		);
	
	


}
