package org.ohdsi.webapi.cohortresults;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface VisualizationDataRepository extends CrudRepository<VisualizationData, Long> {

	public List<VisualizationData> findByCohortDefinitionIdAndSourceId(
			int cohortDefinitionId,
			int sourceId
		);
	
	public VisualizationData findByCohortDefinitionIdAndSourceIdAndVisualizationKey(
				int cohortDefinitionId,
				int sourceId,
				String visualizationKey
			);
	
	public List<VisualizationData> findDistinctVisualizationDataByCohortDefinitionIdAndSourceIdAndVisualizationKey(
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
	
	@Transactional
	public Long deleteByCohortDefinitionIdAndSourceIdAndVisualizationKey(int cohortDefinitionId,
			int sourceId,
			String visualizationKey);
	
	@Transactional
	public Long deleteByCohortDefinitionIdAndSourceIdAndVisualizationKeyAndDrilldownId(
			int cohortDefinitionId,
			int sourceId,
			String visualizationKey,
			int drilldownId
		);
	
	


}
