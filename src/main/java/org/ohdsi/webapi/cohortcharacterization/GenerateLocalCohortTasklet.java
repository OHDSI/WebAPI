package org.ohdsi.webapi.cohortcharacterization;

import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.service.CohortGenerationService;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;

import static org.ohdsi.webapi.Constants.Params.COHORT_CHARACTERIZATION_ID;

public class GenerateLocalCohortTasklet extends BaseGenerateLocalCohortTasklet {

  private final CcService ccService;

  public GenerateLocalCohortTasklet(CcService ccService,
                                    TransactionTemplate transactionTemplate,
                                    CohortGenerationService cohortGenerationService,
                                    SourceRepository sourceRepository) {
    super(transactionTemplate, cohortGenerationService, sourceRepository);
    this.ccService = ccService;
  }

  @Override
  protected List<CohortDefinition> getCohortDefinitions(ChunkContext chunkContext) {

    Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();
    CohortCharacterizationEntity characterization = ccService.findById(Long.valueOf(jobParameters.get(COHORT_CHARACTERIZATION_ID).toString()));
    return characterization.getCohortDefinitions();
  }
}
