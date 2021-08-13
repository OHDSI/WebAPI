package org.ohdsi.webapi.check.checker.cohort;

import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.CohortMethodAnalysis;
import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.ComparativeCohortAnalysis;
import org.ohdsi.webapi.check.builder.DuplicateValidatorBuilder;
import org.ohdsi.webapi.check.builder.IterableForEachValidatorBuilder;
import org.ohdsi.webapi.check.builder.NotNullNotEmptyValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.check.checker.BaseChecker;
import org.ohdsi.webapi.check.checker.tag.helper.TagHelper;
import org.ohdsi.webapi.cohortcharacterization.dto.BaseCcDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisShortDTO;
import org.ohdsi.webapi.service.dto.CommonEntityExtDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.ohdsi.webapi.check.checker.characterization.helper.CharacterizationHelper.prepareCohortBuilder;
import static org.ohdsi.webapi.check.checker.characterization.helper.CharacterizationHelper.prepareFeatureAnalysesBuilder;
import static org.ohdsi.webapi.check.checker.characterization.helper.CharacterizationHelper.prepareStratifyRuleBuilder;

@Component
public class CohortChecker extends BaseChecker<CohortDTO> {
    private final TagHelper<CohortDTO> tagHelper;

    public CohortChecker(TagHelper<CohortDTO> tagHelper) {
        this.tagHelper = tagHelper;
    }

    @PostConstruct
    public void init() {
        createValidator();
    }

    @Override
    protected List<ValidatorGroupBuilder<CohortDTO, ?>> getGroupBuilder() {

        return Arrays.asList(
                tagHelper.prepareTagBuilder()
        );
    }
}
