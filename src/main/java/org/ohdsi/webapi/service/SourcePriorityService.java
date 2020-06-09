package org.ohdsi.webapi.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.shiro.management.datasource.SourceAccessor;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * This code was extracted from the master branch to fix https://github.com/OHDSI/Atlas/issues/2223 issue.
 * There is no need to merge it into the master as the same functionality implemented there in another place.
 */
@Service
public class SourcePriorityService extends AbstractDaoService {

    @Autowired
    private SourceRepository sourceRepository;
    @Autowired
    private SourceAccessor sourceAccessor;

    private Map<Source, Boolean> connectionAvailability = Collections.synchronizedMap(new PassiveExpiringMap<>(5000));

    public Source getPrioritySourceForDaimon(SourceDaimon.DaimonType daimonType) {

        List<Source> sourcesByDaimonPriority = sourceRepository.findAllSortedByDiamonPrioirty(daimonType);

        for (Source source : sourcesByDaimonPriority) {
            if (!(sourceAccessor.hasAccess(source) && connectionAvailability.computeIfAbsent(source, this::checkConnectionSafe))) {
                continue;
            }
            return source;
        }

        return null;
    }

    public Map<SourceDaimon.DaimonType, Source> getPriorityDaimons() {

        class SourceValidator {
            private Map<Integer, Boolean> checkedSources = new HashMap<>();

            private boolean isSourceAvaialble(Source source) {

                return checkedSources.computeIfAbsent(source.getSourceId(),
                        v -> sourceAccessor.hasAccess(source) && connectionAvailability.computeIfAbsent(source, SourcePriorityService.this::checkConnectionSafe));
            }
        }

        SourceValidator sourceValidator = new SourceValidator();
        Map<SourceDaimon.DaimonType, Source> priorityDaimons = new HashMap<>();
        Arrays.asList(SourceDaimon.DaimonType.values()).forEach(d -> {

            List<Source> sources = sourceRepository.findAllSortedByDiamonPrioirty(d);
            Optional<Source> source = sources.stream().filter(sourceValidator::isSourceAvaialble)
                    .findFirst();
            source.ifPresent(s -> priorityDaimons.put(d, s));
        });
        return priorityDaimons;
    }

    private boolean checkConnectionSafe(Source source) {

        try {
            checkConnection(source);
            return true;
        } catch (CannotGetJdbcConnectionException ex) {
            return false;
        }
    }

    public void checkConnection(Source source) {

        final JdbcTemplate jdbcTemplate = getSourceJdbcTemplate(source);
        jdbcTemplate.execute(SqlTranslate.translateSql("select 1;", source.getSourceDialect()).replaceAll(";$", ""));
    }

}
