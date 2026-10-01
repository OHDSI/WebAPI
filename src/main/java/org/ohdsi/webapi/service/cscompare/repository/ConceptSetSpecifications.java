package org.ohdsi.webapi.service.cscompare.repository;

import org.ohdsi.webapi.conceptset.ConceptSet;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.tag.domain.Tag;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConceptSetSpecifications {

	public static Specification<ConceptSet> withAllFilters(
		Date createdFrom,
		Date createdTo,
		Date updatedFrom,
		Date updatedTo,
		List<Integer> tagIds,
		List<Long> authorIds,
		List<Integer> conceptSetIds
	) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			// Created date range filter
			if (createdFrom != null) {
				predicates.add(criteriaBuilder.greaterThanOrEqualTo(
					root.get("createdDate"), createdFrom));
			}
			if (createdTo != null) {
				predicates.add(criteriaBuilder.lessThanOrEqualTo(
					root.get("createdDate"), createdTo));
			}

			// Modified date range filter
			if (updatedFrom != null) {
				predicates.add(criteriaBuilder.greaterThanOrEqualTo(
					root.get("modifiedDate"), updatedFrom));
			}
			if (updatedTo != null) {
				predicates.add(criteriaBuilder.lessThanOrEqualTo(
					root.get("modifiedDate"), updatedTo));
			}

			// Tag filter
			if (tagIds != null && !tagIds.isEmpty()) {
				Join<ConceptSet, Tag> tagJoin = root.join("tags", JoinType.INNER);
				predicates.add(tagJoin.get("id").in(tagIds));
			}

			// Author filter (by user IDs)
			if (authorIds != null && !authorIds.isEmpty()) {
				Join<ConceptSet, UserEntity> createdByJoin = root.join("createdBy", JoinType.INNER);
				predicates.add(createdByJoin.get("id").in(authorIds));
			}

			// Make DISTINCT to avoid duplicates when joining tags or multiple authors
			if ((tagIds != null && !tagIds.isEmpty()) || (authorIds != null && !authorIds.isEmpty())) {
				query.distinct(true);
			}

			// Concept Set ID filter
			if (conceptSetIds != null && !conceptSetIds.isEmpty()) {
				predicates.add(root.get("id").in(conceptSetIds));
			}

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
	}
}