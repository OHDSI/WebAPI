package org.ohdsi.webapi.cohortresults;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity(name = "VisualizationData")
@Table(name = "heracles_visualization_data")
public class VisualizationData implements Serializable {

    private static final long serialVersionUID = -567692689983359944L;

    @Id
    @GenericGenerator(
        name = "visualization_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "heracles_vis_data_sequence"),
            @Parameter(name = "increment_size", value = "1")
        }
    )
    @GeneratedValue(generator = "visualization_generator")
    @Column(name = "ID")
    private int id;

    @Column(name = "COHORT_DEFINITION_ID")
    private int cohortDefinitionId;

    @Column(name = "SOURCE_ID")
    private int sourceId;

    @Column(name = "VISUALIZATION_KEY")
    private String visualizationKey;

    @Column(name = "DATA")
    private String data;

    @Column(name = "END_TIME")
    private Date endTime;

    @Column(name = "DRILLDOWN_ID")
    private int drilldownId;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the cohortDefinitionId
     */
    public int getCohortDefinitionId() {
        return cohortDefinitionId;
    }

    /**
     * @param cohortDefinitionId the cohortDefinitionId to set
     */
    public void setCohortDefinitionId(int cohortDefinitionId) {
        this.cohortDefinitionId = cohortDefinitionId;
    }

    /**
     * @return the source_id
     */
    public int getSourceId() {
        return sourceId;
    }

    /**
     * @param source_id the source_id to set
     */
    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    /**
     * @return the visualizationKey
     */
    public String getVisualizationKey() {
        return visualizationKey;
    }

    /**
     * @param visualizationKey the visualizationKey to set
     */
    public void setVisualizationKey(String visualizationKey) {
        this.visualizationKey = visualizationKey;
    }

    /**
     * @return the data
     */
    public String getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * @return the endTime
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * @return the drilldownId
     */
    public int getDrilldownId() {
        return drilldownId;
    }

    /**
     * @param drilldownId the drilldownId to set
     */
    public void setDrilldownId(int drilldownId) {
        this.drilldownId = drilldownId;
    }

}
