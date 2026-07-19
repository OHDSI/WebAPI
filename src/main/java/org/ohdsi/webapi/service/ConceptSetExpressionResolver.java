package org.ohdsi.webapi.service;

import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.vocabulary.Concept;
import org.ohdsi.webapi.conceptset.ConceptSetItem;
import org.ohdsi.webapi.conceptset.ConceptSetItemRepository;
import org.ohdsi.webapi.service.vocabulary.ConceptSetStrategy;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.PreparedSqlRender;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Service
public class ConceptSetExpressionResolver extends CachingDataSourceAbstractDaoService {

	@Autowired
	private ConceptSetItemRepository conceptSetItemRepository;

	public ConceptSetItemRepository getConceptSetItemRepository() {
		return conceptSetItemRepository;
	}

	private final RowMapper<Concept> rowMapper = (resultSet, arg1) -> {
		final Concept concept = new Concept();
		concept.conceptId = resultSet.getLong("CONCEPT_ID");
		concept.conceptCode = resultSet.getString("CONCEPT_CODE");
		concept.conceptName = resultSet.getString("CONCEPT_NAME");
		concept.standardConcept = resultSet.getString("STANDARD_CONCEPT");
		concept.invalidReason = resultSet.getString("INVALID_REASON");
		concept.conceptClassId = resultSet.getString("CONCEPT_CLASS_ID");
		concept.vocabularyId = resultSet.getString("VOCABULARY_ID");
		concept.domainId = resultSet.getString("DOMAIN_ID");
		concept.validStartDate = resultSet.getDate("VALID_START_DATE");
		concept.validEndDate = resultSet.getDate("VALID_END_DATE");
		return concept;
	};

	public ConceptSetExpression getConceptSetExpression(Source source, int conceptSetId) {
		HashMap<Long, Concept> map = new HashMap<>();

		// create our expression to return
		ConceptSetExpression expression = new ConceptSetExpression();
		ArrayList<ConceptSetExpression.ConceptSetItem> expressionItems = new ArrayList<>();

		List<ConceptSetItem> repositoryItems = new ArrayList<>(getConceptSetItemRepository().findAllByConceptSetId(conceptSetId));


		// collect the unique concept IDs so we can load the concept object later.
		for (ConceptSetItem csi : repositoryItems) {
			map.put(csi.getConceptId(), null);
		}

		// lookup the concepts we need information for
		long[] identifiers = new long[map.size()];
		int identifierIndex = 0;
		for (Long identifier : map.keySet()) {
			identifiers[identifierIndex] = identifier;
			identifierIndex++;
		}

//		String sourceKey;
//		if (Objects.isNull(sourceInfo)) {
//			sourceKey = sourceService.getPriorityVocabularySource().getSourceKey();
//		} else {
//			sourceKey = sourceInfo.sourceKey;
//		}

		Collection<Concept> concepts = executeIdentifierLookup(source, identifiers);

		for (Concept concept : concepts) {
			map.put(concept.conceptId, concept); // associate the concept object to the conceptID in the map
		}

		// put the concept information into the expression along with the concept set item information
		for (ConceptSetItem repositoryItem : repositoryItems) {
			ConceptSetExpression.ConceptSetItem currentItem = new ConceptSetExpression.ConceptSetItem();
			currentItem.concept = map.get(repositoryItem.getConceptId());
			currentItem.includeDescendants = (repositoryItem.getIncludeDescendants() == 1);
			currentItem.includeMapped = (repositoryItem.getIncludeMapped() == 1);
			currentItem.isExcluded = (repositoryItem.getIsExcluded() == 1);
			if (currentItem.concept != null) {
				expressionItems.add(currentItem);
			}
		}
		expression.items = expressionItems.toArray(new ConceptSetExpression.ConceptSetItem[0]); // this will return a new array

		return expression;
	}

	public Collection<Long> resolveConceptSetExpression(Source source, ConceptSetExpression conceptSetExpression) {
		PreparedStatementRenderer psr = new ConceptSetStrategy(conceptSetExpression).prepareStatement(source, null);
		final ArrayList<Long> identifiers = new ArrayList<>();
		getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				identifiers.add(rs.getLong("CONCEPT_ID"));
			}
		});

		return identifiers;
	}

	public Collection<Concept> executeIdentifierLookup(Source source, long[] identifiers) {
		Collection<Concept> concepts = new ArrayList<>();
		if (identifiers.length == 0) {
			return concepts;
		} else {
			// Determine if we need to chunk up ther request based on the parameter
			// limit of the source RDBMS
			int parameterLimit = PreparedSqlRender.getParameterLimit(source);
			if (parameterLimit > 0 && identifiers.length > parameterLimit) {
				concepts = executeIdentifierLookup(source, Arrays.copyOfRange(identifiers, parameterLimit, identifiers.length));
				identifiers = Arrays.copyOfRange(identifiers, 0, parameterLimit);
			}

			PreparedStatementRenderer psr = prepareExecuteIdentifierLookup(identifiers, source);
			return concepts.addAll(getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), this.rowMapper))
				? concepts : new ArrayList<>();
		}
	}

	protected PreparedStatementRenderer prepareExecuteIdentifierLookup(long[] identifiers, Source source) {

		String sqlPath = "/resources/vocabulary/sql/lookupIdentifiers.sql";
		String tqName = "CDM_schema";
		String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

		return new PreparedStatementRenderer(source, sqlPath, tqName, tqValue, "identifiers", identifiers);
	}


}
