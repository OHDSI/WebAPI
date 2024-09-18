package org.ohdsi.webapi.shiny;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ohdsi.webapi.pathway.PathwayService;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;
import org.ohdsi.webapi.pathway.dto.PathwayPopulationResultsDTO;
import org.ohdsi.webapi.pathway.dto.internal.PathwayAnalysisResult;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CohortPathwaysShinyPackagingServiceTest {

    private static final int GENERATION_ID = 1;
    private static final String SOURCE_KEY = "SynPuf110k";

    @Mock
    private PathwayService pathwayService;
    @Spy
    private ManifestUtils manifestUtils;
    @Spy
    private FileWriter fileWriter;

    @InjectMocks
    private CohortPathwaysShinyPackagingService sut;

    @Test
    public void shouldGetBrief() {
        when(pathwayService.getByGenerationId(eq(GENERATION_ID))).thenReturn(createPathwayAnalysisDTO());
        when(pathwayService.getResultingPathways(eq((long) GENERATION_ID))).thenReturn(createPathwayAnalysisResult());

        ApplicationBrief brief = sut.getBrief(GENERATION_ID, SOURCE_KEY);
        assertEquals(brief.getName(), "txp_" + GENERATION_ID + "_" + SOURCE_KEY);
        assertEquals(brief.getTitle(), "Pathway_8_gv1x_SynPuf110k");
        assertEquals(brief.getDescription(), "desc");
    }

    @Test
    public void shouldPopulateAppData() {
        when(pathwayService.findDesignByGenerationId(eq((long) GENERATION_ID))).thenReturn("design json");
        when(pathwayService.getGenerationResults(eq((long) GENERATION_ID))).thenReturn(createPathwayGenerationResults());

        CommonShinyPackagingService.ShinyAppDataConsumers dataConsumers = Mockito.mock(CommonShinyPackagingService.ShinyAppDataConsumers.class, Answers.RETURNS_DEEP_STUBS.get());
        sut.populateAppData(GENERATION_ID, SOURCE_KEY, dataConsumers);

        verify(dataConsumers.getTextFiles(), times(1)).accept(eq("design.json"), anyString());
        verify(dataConsumers.getJsonObjects(), times(1)).accept(eq("chartData.json"), any(PathwayPopulationResultsDTO.class));
    }

    private PathwayPopulationResultsDTO createPathwayGenerationResults() {
        return new PathwayPopulationResultsDTO(Collections.emptyList(), Collections.emptyList());
    }

    @Test
    public void shouldReturnIncidenceType() {
        assertEquals(sut.getType(), CommonAnalysisType.COHORT_PATHWAY);
    }


    private PathwayAnalysisResult createPathwayAnalysisResult() {
        return new PathwayAnalysisResult();
    }

    private PathwayAnalysisDTO createPathwayAnalysisDTO() {
        PathwayAnalysisDTO pathwayAnalysisDTO = new PathwayAnalysisDTO();
        pathwayAnalysisDTO.setId(8);
        pathwayAnalysisDTO.setName("pathwayAnalysis");
        pathwayAnalysisDTO.setDescription("desc");
        return pathwayAnalysisDTO;
    }
}
