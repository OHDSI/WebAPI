package org.ohdsi.webapi.review;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.ohdsi.webapi.user.dto.UserDTO;

import java.time.Instant;

public class ReviewActionDTO {
	private Integer id;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
	private Instant timestamp;
	private Integer version;
	private UserDTO user;
	private String type;
	private String comment;
	private String revokeComment;
	private String supportingInfo;
	private UserDTO representative;

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

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public UserDTO getUser() {
		return user;
	}

	public void setUser(UserDTO user) {
		this.user = user;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public UserDTO getRepresentative() {
		return representative;
	}

	public void setRepresentative(UserDTO representative) {
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
}
