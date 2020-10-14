package org.ohdsi.webapi.pathway.dto;

import org.ohdsi.analysis.CohortMetadata;
import org.ohdsi.webapi.service.dto.CommonEntityDTO;
import org.ohdsi.webapi.user.dto.UserDTO;

import java.util.Date;
import java.util.List;

public abstract class BasePathwayAnalysisDTO<T extends CohortMetadata> extends CommonEntityDTO {

    private Integer id;
    private String name;
    private List<T> targetCohorts;
    private List<T> eventCohorts;
    private Integer combinationWindow;
		private Integer minSegmentLength;
    private Integer minCellCount;
    private Integer maxDepth;
    private boolean allowRepeats;
    private Integer hashCode;

    public Integer getId() {

        return id;
    }

    public void setId(Integer id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public List<T> getTargetCohorts() {

        return targetCohorts;
    }

    public void setTargetCohorts(List<T> targetCohorts) {

        this.targetCohorts = targetCohorts;
    }

    public List<T> getEventCohorts() {

        return eventCohorts;
    }

    public void setEventCohorts(List<T> eventCohorts) {

        this.eventCohorts = eventCohorts;
    }

    public Integer getCombinationWindow() {

        return combinationWindow;
    }

    public void setCombinationWindow(Integer combinationWindow) {

        this.combinationWindow = combinationWindow;
    }

		public Integer getMinSegmentLength() {
			return minSegmentLength;
		}

		public void setMinSegmentLength(Integer minSegmentLength) {
			this.minSegmentLength = minSegmentLength;
		}

    public Integer getMinCellCount() {

        return minCellCount;
    }

    public void setMinCellCount(Integer minCellCount) {

        this.minCellCount = minCellCount;
    }

    public Integer getMaxDepth() {

        return maxDepth;
    }

    public void setMaxDepth(Integer maxDepth) {

        this.maxDepth = maxDepth;
    }

    public boolean isAllowRepeats() {

        return allowRepeats;
    }

    public void setAllowRepeats(boolean allowRepeats) {

        this.allowRepeats = allowRepeats;
    }

    public Integer getHashCode() {

        return hashCode;
    }

    public void setHashCode(Integer hashCode) {

        this.hashCode = hashCode;
    }
}
