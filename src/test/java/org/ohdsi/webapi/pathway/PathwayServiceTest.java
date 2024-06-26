package org.ohdsi.webapi.pathway;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisGenerationEntity;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;
import org.ohdsi.webapi.pathway.repository.PathwayAnalysisGenerationRepository;
import org.springframework.core.convert.support.GenericConversionService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PathwayServiceTest {

    @Mock
    private PathwayAnalysisGenerationRepository pathwayAnalysisGenerationRepository;
    @Mock
    private GenericConversionService genericConversionService;
    @Mock
    private PathwayAnalysisGenerationEntity pathwayAnalysisGenerationEntity;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PathwayAnalysisDTO pathwayAnalysisDTO;
    @Mock
    private PathwayAnalysisEntity pathwayAnalysisEntity;
    @InjectMocks
    private PathwayServiceImpl sut;

    @Test
    public void shouldGetByGenerationId() {
        when(pathwayAnalysisGenerationRepository.findOne(anyLong(), any())).thenReturn(pathwayAnalysisGenerationEntity);
        when(pathwayAnalysisGenerationEntity.getPathwayAnalysis()).thenReturn(pathwayAnalysisEntity);
        when(genericConversionService.convert(eq(pathwayAnalysisEntity), eq(PathwayAnalysisDTO.class))).thenReturn(pathwayAnalysisDTO);
        PathwayAnalysisDTO result = sut.getByGenerationId(1);
        assertEquals(result, pathwayAnalysisDTO);
    }

}
