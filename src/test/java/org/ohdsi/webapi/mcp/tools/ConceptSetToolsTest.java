package org.ohdsi.webapi.mcp.tools;

import org.junit.jupiter.api.Test;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.webapi.conceptset.ConceptSetService;
import org.ohdsi.webapi.mcp.support.McpResult;
import org.ohdsi.webapi.mcp.support.McpToolContext;
import org.ohdsi.webapi.service.VocabularyService;
import org.ohdsi.webapi.service.dto.ConceptSetDTO;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConceptSetToolsTest {

    private final ConceptSetService conceptSets = mock(ConceptSetService.class);
    private final VocabularyService vocab = mock(VocabularyService.class);
    private final McpToolContext context = mock(McpToolContext.class);
    private final ConceptSetTools tools = new ConceptSetTools(conceptSets, vocab, context);

    @Test
    void listReturnsConceptSets() {
        when(conceptSets.getConceptSets()).thenReturn(List.of());

        McpResult r = tools.conceptsetList();

        assertThat(r.ok()).isTrue();
        verify(conceptSets).getConceptSets();
    }

    @Test
    void getReturnsConceptSet() {
        ConceptSetDTO dto = new ConceptSetDTO();
        dto.setId(7);
        when(conceptSets.getConceptSet(7)).thenReturn(dto);

        McpResult r = tools.conceptsetGet(7);

        assertThat(r.ok()).isTrue();
        verify(conceptSets).getConceptSet(7);
    }

    @Test
    void expressionReturnsRawExpression() {
        ConceptSetExpression expr = new ConceptSetExpression();
        when(conceptSets.getConceptSetExpressionById(7)).thenReturn(expr);

        McpResult r = tools.conceptsetExpression(7);

        assertThat(r.ok()).isTrue();
        verify(conceptSets).getConceptSetExpressionById(7);
    }

    @Test
    void createDelegatesToConceptSetService() {
        ConceptSetDTO input = new ConceptSetDTO();
        input.setName("New Set");
        ConceptSetDTO created = new ConceptSetDTO();
        created.setId(9);
        when(conceptSets.createConceptSet(input)).thenReturn(created);

        McpResult r = tools.conceptsetCreate(input);

        assertThat(r.ok()).isTrue();
        verify(conceptSets).createConceptSet(input);
    }

    @Test
    void updateDelegatesToConceptSetService() {
        ConceptSetDTO input = new ConceptSetDTO();
        input.setName("Updated Set");
        ConceptSetDTO updated = new ConceptSetDTO();
        updated.setId(7);
        when(conceptSets.updateConceptSet(7, input)).thenReturn(updated);

        McpResult r = tools.conceptsetUpdate(7, input);

        assertThat(r.ok()).isTrue();
        verify(conceptSets).updateConceptSet(7, input);
    }

    @Test
    void resolveResolvesSourceKeyThenExpressionThenResolves() {
        ConceptSetExpression expr = new ConceptSetExpression();
        when(context.requireSource("DEMO_CDM")).thenReturn("DEMO_CDM");
        when(conceptSets.getConceptSetExpressionById(7)).thenReturn(expr);
        when(vocab.resolveConceptSetExpression("DEMO_CDM", expr)).thenReturn(List.of(1L, 2L, 3L));

        McpResult r = tools.conceptsetResolve("DEMO_CDM", 7);

        assertThat(r.ok()).isTrue();
        verify(context).requireSource("DEMO_CDM");
        verify(conceptSets).getConceptSetExpressionById(7);
        verify(vocab).resolveConceptSetExpression("DEMO_CDM", expr);
    }

    @Test
    void resolveUnknownSourceKeyBecomesInvalidInput() {
        when(context.requireSource("NOPE"))
                .thenThrow(new IllegalArgumentException("Unknown sourceKey 'NOPE'. Valid keys: [DEMO_CDM]"));

        McpResult r = tools.conceptsetResolve("NOPE", 7);

        assertThat(r.ok()).isFalse();
        assertThat(r.status()).isEqualTo("invalid_input");
    }
}
