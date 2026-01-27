package org.ohdsi.webapi.service.cscompare;

import org.ohdsi.webapi.conceptset.ConceptSet;
import org.ohdsi.webapi.conceptset.ConceptSetRepository;
import org.ohdsi.webapi.service.cscompare.repository.ConceptSetSpecifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class ConceptSetFilterService {

	private static final Logger log = LoggerFactory.getLogger(ConceptSetFilterService.class);

	private final ConceptSetRepository conceptSetRepository;

	public ConceptSetFilterService(
		ConceptSetRepository conceptSetRepository
	) {
		this.conceptSetRepository = conceptSetRepository;
	}

	@Transactional(readOnly = true)
	public List<ConceptSet> filterConceptSets(ConceptSetFilterCriteria criteria) {
		log.info("Filtering concept sets with criteria: {}", criteria);

		// Convert LocalDate to Date with proper boundaries
		Date createdFrom = toDateStartOfDay(criteria.getCreatedFrom());
		Date createdTo = toDateEndOfDay(criteria.getCreatedTo());
		Date updatedFrom = toDateStartOfDay(criteria.getUpdatedFrom());
		Date updatedTo = toDateEndOfDay(criteria.getUpdatedTo());

		log.debug("Converted date filters: createdFrom={}, createdTo={}, updatedFrom={}, updatedTo={}",
			createdFrom, createdTo, updatedFrom, updatedTo);

		// Build specification
		Specification<ConceptSet> spec = ConceptSetSpecifications.withAllFilters(
			createdFrom,
			createdTo,
			updatedFrom,
			updatedTo,
			criteria.getTagIds(),
			criteria.getAuthorIds(),
			criteria.getConceptSetIds()
		);

		// Execute query
		List<ConceptSet> filteredSets = conceptSetRepository.findAll(spec);

		log.info("Found {} concept sets after date, tag, and author filtering", filteredSets.size());

		return filteredSets;
	}

	/**
	 * Convert LocalDate to Date at start of day (00:00:00.000)
	 * Returns null if localDate is null (treated as minus infinity)
	 */
	private Date toDateStartOfDay(LocalDate localDate) {
		if (localDate == null) {
			return null;
		}
		return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * Convert LocalDate to Date at end of day (23:59:59.999)
	 * Returns null if localDate is null (treated as plus infinity)
	 */
	private Date toDateEndOfDay(LocalDate localDate) {
		if (localDate == null) {
			return null;
		}
		return Date.from(localDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * Criteria class for filtering concept sets
	 */
	public static class ConceptSetFilterCriteria {
		private LocalDate createdFrom;
		private LocalDate createdTo;
		private LocalDate updatedFrom;
		private LocalDate updatedTo;
		private List<Integer> tagIds;
		private List<Long> authorIds;
		private List<Integer> conceptSetIds;

		public LocalDate getCreatedFrom() {
			return createdFrom;
		}

		public void setCreatedFrom(LocalDate createdFrom) {
			this.createdFrom = createdFrom;
		}

		public LocalDate getCreatedTo() {
			return createdTo;
		}

		public void setCreatedTo(LocalDate createdTo) {
			this.createdTo = createdTo;
		}

		public LocalDate getUpdatedFrom() {
			return updatedFrom;
		}

		public void setUpdatedFrom(LocalDate updatedFrom) {
			this.updatedFrom = updatedFrom;
		}

		public LocalDate getUpdatedTo() {
			return updatedTo;
		}

		public void setUpdatedTo(LocalDate updatedTo) {
			this.updatedTo = updatedTo;
		}

		public List<Integer> getTagIds() {
			return tagIds;
		}

		public void setTagIds(List<Integer> tagIds) {
			this.tagIds = tagIds;
		}

		public List<Long> getAuthorIds() {
			return authorIds;
		}

		public void setAuthorIds(List<Long> authorIds) {
			this.authorIds = authorIds;
		}

		@Override
		public String toString() {
			return "ConceptSetFilterCriteria{" +
				"createdFrom=" + createdFrom +
				", createdTo=" + createdTo +
				", updatedFrom=" + updatedFrom +
				", updatedTo=" + updatedTo +
				", tagIds=" + tagIds +
				", authorIds=" + authorIds +
				'}';
		}

		public List<Integer> getConceptSetIds() {
			return conceptSetIds;
		}

		public void setConceptSetIds(List<Integer> conceptSetIds) {
			this.conceptSetIds = conceptSetIds;
		}
	}
}