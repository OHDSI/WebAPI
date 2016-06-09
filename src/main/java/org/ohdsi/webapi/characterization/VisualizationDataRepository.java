package org.ohdsi.webapi.characterization;

import org.ohdsi.webapi.characterization.VisualizationData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface VisualizationDataRepository extends CrudRepository<VisualizationData, Long> {

    public List<VisualizationData> findBySourceId(
//			int cohortDefinitionId,
            int sourceId
    );

    public VisualizationData findBySourceIdAndVisualizationKey(
//			int cohortDefinitionId,
            int sourceId,
            String visualizationKey
    );

    public List<VisualizationData> findDistinctVisualizationDataBySourceIdAndVisualizationKey(
//			int cohortDefinitionId,
            int sourceId,
            String visualizationKey
    );

    public VisualizationData findBySourceIdAndVisualizationKeyAndDrilldownId(
//			int cohortDefinitionId,
            int sourceId,
            String visualizationKey,
            int drilldownId
    );

    @Transactional
    public Long deleteBySourceIdAndVisualizationKey(/*int cohortDefinitionId,*/
                                                    int sourceId,
                                                    String visualizationKey);

    @Transactional
    public Long deleteBySourceIdAndVisualizationKeyAndDrilldownId(
//			int cohortDefinitionId,
            int sourceId,
            String visualizationKey,
            int drilldownId
    );

}
