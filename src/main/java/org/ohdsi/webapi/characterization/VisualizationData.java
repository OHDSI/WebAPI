package org.ohdsi.webapi.characterization;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity(name = "VisualizationData")
@Table(name="achilles_visualization_data")
public class VisualizationData implements Serializable {

	private static final long serialVersionUID = -2413507499996446764L;

	@Id
	  @GeneratedValue  
	  @Column(name="ID")  
	  private int id;

	  @Column(name="SOURCE_ID")
	  private int sourceId;
	  
	  @Column(name="VISUALIZATION_KEY")
	  private String visualizationKey;
	  
	  @Column(name="DATA")
	  private String data;
	  
	  @Column(name="END_TIME")
	  private Date endTime;
	  
	  @Column(name="DRILLDOWN_ID")
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
