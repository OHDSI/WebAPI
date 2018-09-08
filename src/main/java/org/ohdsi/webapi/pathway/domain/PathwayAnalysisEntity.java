package org.ohdsi.webapi.pathway.domain;

import org.ohdsi.webapi.shiro.Entities.UserEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import java.util.Date;
import java.util.List;

@Entity(name = "pathway_analyses")
public class PathwayAnalysisEntity {

    @Id
    @SequenceGenerator(name = "pathways_analyses_pk_sequence", sequenceName = "pathway_analyses_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pathways_analyses_pk_sequence")
    private Long id;

    @Column
    private String name;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "pathwayAnalysis", orphanRemoval = true)
    private List<PathwayTargetCohort> targetCohorts;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "pathwayAnalysis", orphanRemoval = true)
    private List<PathwayEventCohort> eventCohorts;

    @Column(name = "combination_window")
    private Integer combinationWindow;

    @Column(name = "min_cell_count")
    private Integer minCellCount;

    @Column(name = "max_depth")
    private Integer maxDepth;

    @ManyToOne
    @JoinColumn(name="created_by")
    private UserEntity createdBy;

    @Column(name = "created_at")
    private Date createdAt = new Date();

    @ManyToOne
    @JoinColumn(name="updated_by")
    private UserEntity updatedBy;

    @Column(name = "updated_at")
    private Date updatedAt;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public List<PathwayTargetCohort> getTargetCohorts() {

        return targetCohorts;
    }

    public void setTargetCohorts(List<PathwayTargetCohort> targetCohorts) {

        this.targetCohorts = targetCohorts;
    }

    public List<PathwayEventCohort> getEventCohorts() {

        return eventCohorts;
    }

    public void setEventCohorts(List<PathwayEventCohort> eventCohorts) {

        this.eventCohorts = eventCohorts;
    }

    public Integer getCombinationWindow() {

        return combinationWindow;
    }

    public void setCombinationWindow(Integer combinationWindow) {

        this.combinationWindow = combinationWindow;
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

    public UserEntity getCreatedBy() {

        return createdBy;
    }

    public void setCreatedBy(UserEntity createdBy) {

        this.createdBy = createdBy;
    }

    public Date getCreatedAt() {

        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {

        this.createdAt = createdAt;
    }

    public UserEntity getUpdatedBy() {

        return updatedBy;
    }

    public void setUpdatedBy(UserEntity updatedBy) {

        this.updatedBy = updatedBy;
    }

    public Date getUpdatedAt() {

        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {

        this.updatedAt = updatedAt;
    }
}
