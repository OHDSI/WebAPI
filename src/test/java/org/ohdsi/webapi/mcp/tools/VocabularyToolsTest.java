package org.ohdsi.webapi.mcp.tools;

import org.junit.jupiter.api.Test;
import org.ohdsi.vocabulary.Concept;
import org.ohdsi.webapi.mcp.support.McpResult;
import org.ohdsi.webapi.mcp.support.McpToolContext;
import org.ohdsi.webapi.service.VocabularyService;
import org.ohdsi.webapi.vocabulary.ConceptSearch;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VocabularyToolsTest {

    private final VocabularyService vocab = mock(VocabularyService.class);
    private final McpToolContext context = mock(McpToolContext.class);
    private final VocabularyTools tools = new VocabularyTools(vocab, context);

    @Test
    void searchReturnsOkEnvelopeWithConcepts() {
        Concept c = new Concept();
        c.conceptId = 1L;
        c.conceptName = "Diabetes";
        when(vocab.executeSearch(any(ConceptSearch.class))).thenReturn(List.of(c));

        McpResult r = tools.vocabSearchConcepts("diabetes", null, null, null);

        assertThat(r.ok()).isTrue();
        assertThat((List<Concept>) r.data())
                .extracting(concept -> concept.conceptName)
                .contains("Diabetes");
    }

    @Test
    void getConceptReturnsOkEnvelope() {
        Concept c = new Concept();
        c.conceptId = 201826L;
        c.conceptName = "Type 2 diabetes mellitus";
        when(vocab.getConcept(201826L)).thenReturn(c);

        McpResult r = tools.vocabGetConcept(201826L);

        assertThat(r.ok()).isTrue();
        assertThat(((Concept) r.data()).conceptName).isEqualTo("Type 2 diabetes mellitus");
    }

    @Test
    void relatedConceptsReturnsOkEnvelope() {
        when(vocab.getRelatedConcepts(1L)).thenReturn(List.of());

        McpResult r = tools.vocabRelatedConcepts(1L);

        assertThat(r.ok()).isTrue();
    }

    @Test
    void descendantsResolvesSourceKeyThenCallsService() {
        when(context.requireSource("DEMO_CDM")).thenReturn("DEMO_CDM");
        when(vocab.getDescendantConcepts(eq("DEMO_CDM"), eq(1L))).thenReturn(List.of());

        McpResult r = tools.vocabConceptDescendants("DEMO_CDM", 1L);

        assertThat(r.ok()).isTrue();
    }

    @Test
    void unknownSourceKeyBecomesInvalidInput() {
        when(context.requireSource("NOPE"))
                .thenThrow(new IllegalArgumentException("Unknown sourceKey 'NOPE'. Valid keys: [DEMO_CDM]"));

        McpResult r = tools.vocabConceptDescendants("NOPE", 1L);

        assertThat(r.ok()).isFalse();
        assertThat(r.status()).isEqualTo("invalid_input");
    }
}
