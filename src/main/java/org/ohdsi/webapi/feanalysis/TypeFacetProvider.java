package org.ohdsi.webapi.feanalysis;

import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType;
import org.ohdsi.webapi.facets.AbstractTextColumnBasedFacetProvider;
import org.ohdsi.webapi.facets.FacetItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType.CRITERIA_SET;
import static org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType.CUSTOM_FE;
import static org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType.PRESET;

@Component
public class TypeFacetProvider extends AbstractTextColumnBasedFacetProvider {
    private static final String FACET_NAME = "Type";
    private static final String FIELD_NAME = "type";
    private static final String COLUMN_NAME = "type";
    private static final Map<StandardFeatureAnalysisType, String> TYPE_NAMES = new HashMap<>();
    static {
        TYPE_NAMES.put(PRESET, "Preset");
        TYPE_NAMES.put(CRITERIA_SET,"Criteria set");
        TYPE_NAMES.put(CUSTOM_FE, "Custom");
    }

    public TypeFacetProvider(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    protected String getText(ResultSet resultSet) throws SQLException {
        final StandardFeatureAnalysisType type = StandardFeatureAnalysisType.valueOf(getKey(resultSet));
        final String name = TYPE_NAMES.get(type);
        return name != null ? name : "Unknown feature analysis type";
    }

    @Override
    protected String getColumn() {
        return COLUMN_NAME;
    }

    @Override
    public String getName() {
        return FACET_NAME;
    }

    @Override
    public String getField() {
        return FIELD_NAME;
    }

    @Override
    public <T> Predicate createFacetPredicate(List<FacetItem> items, CriteriaBuilder cb, Root<T> root) {
        assert items != null && !items.isEmpty();
        return items.size() == 1
                ? createItemPredicate(items.get(0), cb, root)
                : cb.or(items.stream().map(item -> createItemPredicate(item, cb, root)).toArray(Predicate[]::new));
    }

    private <T> Predicate createItemPredicate(FacetItem item, CriteriaBuilder cb, Root<T> root) {
        final Path field = root.get(getField());
        return cb.equal(field, cb.literal(StandardFeatureAnalysisType.valueOf(item.key)));
    }
}
