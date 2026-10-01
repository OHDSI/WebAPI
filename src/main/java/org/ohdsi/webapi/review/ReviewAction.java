package org.ohdsi.webapi.review;

import org.ohdsi.webapi.shiro.Entities.UserEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "review_action")
public class ReviewAction {
	@Id
	@SequenceGenerator(name = "review_action_seq_generator", sequenceName = "review_action_seq", allocationSize = 1)
	@GeneratedValue(generator = "concept_set_annotation_generator")
	@Column(name = "id")
	private Integer id;

	@Column(name = "timestamp")
	private Instant timestamp;

	@Column(name = "asset_type", nullable = false)
	private String assetType;

	@Column(name = "asset_id", nullable = false)
	private Integer assetId;

	@Column(name = "version")
	private Integer version;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private UserEntity user;

	@Column(name = "action")
	private String action;

	@Column(name = "comment")
	private String comment;

	@Column(name = "revoke_comment")
	private String revokeComment;

	@Column(name = "supporting_info")
	private String supportingInfo;

	@ManyToOne
	@JoinColumn(name = "representative_id")
	private UserEntity representative;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	public String getAssetType() {
		return assetType;
	}

	public void setAssetType(String assetType) {
		this.assetType = assetType;
	}

	public Integer getAssetId() {
		return assetId;
	}

	public void setAssetId(Integer assetId) {
		this.assetId = assetId;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public UserEntity getUser() {
		return user;
	}

	public void setUser(UserEntity user) {
		this.user = user;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public UserEntity getRepresentative() {
		return representative;
	}

	public void setRepresentative(UserEntity representative) {
		this.representative = representative;
	}

	public String getSupportingInfo() {
		return supportingInfo;
	}

	public void setSupportingInfo(String supportingInfo) {
		this.supportingInfo = supportingInfo;
	}

	public String getRevokeComment() {
		return revokeComment;
	}

	public void setRevokeComment(String revokeComment) {
		this.revokeComment = revokeComment;
	}

	public interface Type {
		String CREATE = "CREATE";
		String APPROVE = "APPROVE";
		String REVOKE = "REVOKE";
	}

}
