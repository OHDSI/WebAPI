package org.ohdsi.webapi.review;

import org.apache.shiro.SecurityUtils;
import org.jetbrains.annotations.NotNull;
import org.ohdsi.webapi.shiro.Entities.PermissionEntity_;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.Entities.RolePermissionEntity;
import org.ohdsi.webapi.shiro.Entities.RolePermissionEntity_;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserEntity_;
import org.ohdsi.webapi.shiro.Entities.UserRoleEntity;
import org.ohdsi.webapi.shiro.Entities.UserRoleEntity_;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.user.converter.UserEntityToUserDTOConverter;
import org.ohdsi.webapi.user.dto.UserDTO;
import org.ohdsi.webapi.util.jpa.JpaSugar;
import org.ohdsi.webapi.util.jpa.JpaSugar.Filter;
import org.ohdsi.webapi.versioning.domain.ConceptSetVersion;
import org.ohdsi.webapi.versioning.service.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.Clock;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.tag.TagSecurityUtils.CONCEPT_SET;
import static org.ohdsi.webapi.util.jpa.JpaSugar.Condition.has;
import static org.ohdsi.webapi.util.jpa.JpaSugar.Filter.subquery;

@Path(ReviewService.REVIEW)
@Controller
public class ReviewService {
	public static final String REVIEW = "review";
	private static final String REVIEW_DELEGATE = "review:delegate";

	@PersistenceContext
	private EntityManager em;
	@Autowired
	private Clock clock;
	@Autowired
	private UserEntityToUserDTOConverter userConverter;
	@Autowired
	private PermissionManager permissionManager;

	@POST
	@Path("/{type}/{id}/approve")
	@Transactional
	public void approve(@PathParam("type") String type, @PathParam("id") Integer id, Approval approve) {
		UserEntity approver = Optional.ofNullable(approve.approverId).map(validateApprover(type)).orElse(null);
		Integer version = VersionService.getLatest(em, ConceptSetVersion.class, id);
		getApproval(type, id, version).ifPresent(approval -> {
			throw new BadRequestException("Already approved by [" + approval.getUser().getName() + "]");
		});
		String comment = Optional.ofNullable(approve)
			.map(Approval::getComment)
			.orElse(null);

		UserEntity currentUser = permissionManager.getCurrentUser();
		UserEntity formalApprover = currentUser.equals(approver) ? currentUser : approver;

		ReviewAction action = create(validateType(type), id, formalApprover, ReviewAction.Type.APPROVE, version, currentUser, comment, approve.getSupportingInfo(), null);
		em.persist(action);
	}

	@POST
	@Path("/{type}/{id}/revoke/{versionId}")
	@Transactional
	public void revoke(@PathParam("type") String type, @PathParam("id") Integer id, @PathParam("versionId") Integer version, RevokeRequest request) {

		TypedQuery<ReviewAction> query = JpaSugar.query(em, ReviewAction.class, (cb, cq) -> {
			Root<ReviewAction> root = cq.from(ReviewAction.class);
			cq.select(root);
			cq.where(
				cb.equal(root.get(ReviewAction_.assetType), validateType(type)),
				cb.equal(root.get(ReviewAction_.assetId), id),
				cb.equal(root.get(ReviewAction_.version), version)
			);
			cq.orderBy(cb.desc(root.get(ReviewAction_.timestamp)));
			return em.createQuery(cq);
		});
		query.setMaxResults(1);
		ReviewAction actionToRevoke = query.getSingleResult();
		if (actionToRevoke != null) {
			ReviewAction revokeAction = create(validateType(type), id, permissionManager.getCurrentUser(), ReviewAction.Type.REVOKE, actionToRevoke.getVersion(), null, actionToRevoke.getComment(), actionToRevoke.getSupportingInfo(), request.getComment());
			em.persist(revokeAction);
		}
	}

	public static class RevokeRequest {
		private String comment;

		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}
	}

	@GET
	@Path("/{type}/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Map<Integer, ReviewActionDTO> listByObjectId(@PathParam("type") String type, @PathParam("id") Integer id) {
		TypedQuery<ReviewAction> query = JpaSugar.query(em, ReviewAction.class, (cb, cq) -> {
			Root<ReviewAction> root = cq.from(ReviewAction.class);
			cq.select(root);
			cq.where(
				cb.equal(root.get(ReviewAction_.assetType), validateType(type)),
				cb.equal(root.get(ReviewAction_.assetId), id)
			);
			cq.orderBy(cb.desc(root.get(ReviewAction_.timestamp)));
			return em.createQuery(cq);
		});
		Map<Integer, ReviewActionDTO> reviewActionsByVersion = query.getResultStream().collect(Collectors.toMap(ReviewAction::getVersion, this::toDto, toLatest()));
		return reviewActionsByVersion.entrySet().stream()
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				Map.Entry::getValue
			));
	}

	@POST
	@Path("/{type}/approved")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Map<Integer, Map<Integer, ReviewActionDTO>> listByObjectIds(
		@PathParam("type") String type,
		@RequestBody List<Integer> ids
	) {
		if (ids == null || ids.isEmpty()) {
			return new HashMap<>();
		}
		String validatedType = validateType(type);

		// Get ALL review actions for the requested assets
		List<ReviewAction> allActions = JpaSugar.select(em, ReviewAction.class)
			.where(has(ReviewAction_.assetType, validatedType))
			.getResultStream()
			.filter(action -> ids.contains(action.getAssetId()))
			.collect(Collectors.toList());

		// Group by assetId, then by version, keeping only the latest action per version
		return allActions.stream()
			.collect(Collectors.groupingBy(
				ReviewAction::getAssetId,
				Collectors.groupingBy(
					ReviewAction::getVersion,
					Collectors.collectingAndThen(
						Collectors.maxBy(Comparator.comparing(ReviewAction::getTimestamp)),
						opt -> opt.map(this::toDto).orElse(null)
					)
				)
			))
			.entrySet().stream()
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				entry -> entry.getValue().entrySet().stream()
					.filter(versionEntry -> versionEntry.getValue() != null)
					.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue
					))
			))
			.entrySet().stream()
			.filter(entry -> !entry.getValue().isEmpty())
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				Map.Entry::getValue
			));
	}

	@GET
	@Path("/{type}/approvers")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public List<UserDTO> getApprovers(@PathParam("type") String type) {
		return JpaSugar.select(em, UserEntity.class).where(
			hasRoleWithPermission(REVIEW + ":" + type)
		).getResultStream().map(userConverter::convert).collect(Collectors.toList());
	}

	@NotNull
	public Optional<ReviewActionDTO> getApproval(String type, Integer id, Integer version) {
		return JpaSugar.select(em, ReviewAction.class).where(
			has(ReviewAction_.assetType, validateType(type)),
			has(ReviewAction_.assetId, id),
			has(ReviewAction_.version, version)
		).getResultStream().map(this::toDto).reduce(toLatest()).filter(action1 ->
			Objects.equals(action1.getType(), ReviewAction.Type.APPROVE)
		);
	}

	private ReviewAction create(String type, Integer assetId, UserEntity user, String create, Integer version, UserEntity representative, String comment, String supportingInfo, String revokeComment) {
		ReviewAction action = new ReviewAction();
		action.setUser(user);
		action.setAssetType(type);
		action.setAssetId(assetId);
		action.setAction(create);
		action.setTimestamp(clock.instant());
		action.setVersion(version);
		action.setRepresentative(representative);
		action.setComment(comment);
		action.setRevokeComment(revokeComment);
		action.setSupportingInfo(supportingInfo);
		return action;
	}

	private ReviewActionDTO toDto(ReviewAction entity) {
		ReviewActionDTO dto = new ReviewActionDTO();
		dto.setId(entity.getId());
		dto.setTimestamp(entity.getTimestamp());
		dto.setVersion(entity.getVersion());
		dto.setUser(userConverter.convert(entity.getUser()));
		dto.setType(entity.getAction());
		dto.setComment(entity.getComment());
		dto.setRevokeComment(entity.getRevokeComment());
		dto.setSupportingInfo(entity.getSupportingInfo());
		dto.setRepresentative(userConverter.convert(entity.getRepresentative()));
		return dto;
	}

	private Function<Long, UserEntity> validateApprover(String type) {
		return approverId -> {
			if (SecurityUtils.getSubject().isPermitted(REVIEW_DELEGATE)) {
				return JpaSugar.select(em, UserEntity.class).where(
					hasRoleWithPermission(REVIEW + ":" + type), has(UserEntity_.id, approverId)
				).getResultStream().findFirst().orElseThrow(() ->
					new BadRequestException("Not an id that refers to user with valid approval permission: " + approverId)
				);
			} else {
				throw new ForbiddenException("User not authorized to delegate the approval");
			}
		};
	}

	private static BinaryOperator<ReviewActionDTO> toLatest() {
		return (a, b) -> a.getTimestamp().compareTo(b.getTimestamp()) > 0 ? a : b;
	}

	private static String validateType(String type) {
		if (Objects.equals(type, CONCEPT_SET)) {
			return type;
		} else {
			throw new BadRequestException("Reviews are not supported for [" + type + "]");
		}
	}

	private static Filter<UserEntity> hasRoleWithPermission(String permission) {
		return subquery(UserEntity.class, UserRoleEntity.class, UserRoleEntity_.user).where(
			subquery(RoleEntity.class, RolePermissionEntity.class, RolePermissionEntity_.role).where(
				has(RolePermissionEntity_.permission, PermissionEntity_.value, permission)
			).on(UserRoleEntity_.role)
		);
	}

	public static class Approval {
		private String comment;
		private Long approverId;
		private String supportingInfo;

		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}

		public Long getApproverId() {
			return approverId;
		}

		public void setApproverId(Long approverId) {
			this.approverId = approverId;
		}

		public String getSupportingInfo() {
			return supportingInfo;
		}

		public void setSupportingInfo(String supportingInfo) {
			this.supportingInfo = supportingInfo;
		}
	}
}
