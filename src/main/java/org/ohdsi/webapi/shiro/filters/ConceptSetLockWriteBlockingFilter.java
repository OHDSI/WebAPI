package org.ohdsi.webapi.shiro.filters;

import com.google.common.collect.Iterables;
import org.apache.commons.lang3.BooleanUtils;
import org.ohdsi.webapi.service.lock.ConceptSetLockingService;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConceptSetLockWriteBlockingFilter extends UrlBasedAuthorizingFilter {

	private final boolean snapshotLockingEnabled;
	private final ConceptSetLockingService conceptSetLockingService;

	public ConceptSetLockWriteBlockingFilter(boolean snapshotLockingEnabled, ConceptSetLockingService conceptSetLockingService) {
		this.snapshotLockingEnabled = snapshotLockingEnabled;
		this.conceptSetLockingService = conceptSetLockingService;
	}

	private static final Pattern RESTRICTED_PATH_PATTERN = Pattern.compile("^conceptset:(\\d+):(items:put|annotation:put|put)$");

	@Override
	protected boolean isPermitted(String pathPermission) {

		if (!snapshotLockingEnabled) {
			return true; // If snapshot locking feature is disabled in the configuration - no restrictions for concept set saving
		}

		return getConceptSetIdIfRestrictedPath(pathPermission)
			.map(conceptSetId -> conceptSetLockingService.areLocked(Collections.singletonList(conceptSetId)))
			.map(Map::values)
			.map(Iterables::getOnlyElement)
			.map(BooleanUtils::negate)
			.orElse(Boolean.TRUE);
	}

	private Optional<Integer> getConceptSetIdIfRestrictedPath(String path) {
		Matcher matcher = RESTRICTED_PATH_PATTERN.matcher(path);
		if (matcher.matches()) {
			Integer conceptSetId = Integer.parseInt(matcher.group(1));
			return Optional.of(conceptSetId);
		}
		return Optional.empty();
	}
}
