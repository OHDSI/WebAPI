package org.ohdsi.webapi.shiny;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ohdsi.webapi.pathway.PathwayService;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;
import org.ohdsi.webapi.pathway.dto.internal.PathwayAnalysisResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
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
        assertEquals(brief.getName(), "cohort_pathways_analysis_" + GENERATION_ID + "_" + SOURCE_KEY);
        assertEquals(brief.getTitle(), "pathwayAnalysis (SynPuf110k)");
        assertEquals(brief.getDescription(), "desc");
    }

    @Test
    public void shouldPackageApp() {
        when(pathwayService.getByGenerationId(eq(GENERATION_ID))).thenReturn(createPathwayAnalysisDTO());
        when(pathwayService.getResultingPathways(eq((long) GENERATION_ID))).thenReturn(createPathwayAnalysisResult());
        PackagingStrategy packagingStrategy = mock(PackagingStrategy.class);
        TemporaryFile result = sut.packageApp(GENERATION_ID, SOURCE_KEY, packagingStrategy);
        assertNotNull(result);
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
        pathwayAnalysisDTO.setName("pathwayAnalysis");
        pathwayAnalysisDTO.setDescription("desc");
        return pathwayAnalysisDTO;
    }
}
