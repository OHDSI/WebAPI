package org.ohdsi.webapi.pathway.domain;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.ohdsi.webapi.model.CommonEntity;

@Entity(name = "pathway_analysis")
public class PathwayAnalysisEntity extends CommonEntity {

    @Id
    @GenericGenerator(
        name = "pathway_analysis_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "pathway_analysis_sequence"),
            @Parameter(name = "increment_size", value = "1")
        }
    )
    @GeneratedValue(generator = "pathway_analysis_generator")
    private Integer id;

    @Column
    private String name;

    @OneToMany(mappedBy = "pathwayAnalysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PathwayTargetCohort> targetCohorts = new HashSet<>();

    @OneToMany(mappedBy = "pathwayAnalysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PathwayEventCohort> eventCohorts = new HashSet<>();

    @Column(name = "combination_window")
    private Integer combinationWindow;

    @Column(name = "min_segment_length")
    private Integer minSegmentLength;
		
    @Column(name = "min_cell_count")
    private Integer minCellCount;

    @Column(name = "max_depth")
    private Integer maxDepth;

    @Column(name = "allow_repeats")
    private boolean allowRepeats;

    @Column(name = "hash_code")
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

    public Set<PathwayTargetCohort> getTargetCohorts() {

        return targetCohorts;
    }

    public void setTargetCohorts(Set<PathwayTargetCohort> targetCohorts) {

        this.targetCohorts = targetCohorts;
    }

    public Set<PathwayEventCohort> getEventCohorts() {

        return eventCohorts;
    }

    public void setEventCohorts(Set<PathwayEventCohort> eventCohorts) {

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
