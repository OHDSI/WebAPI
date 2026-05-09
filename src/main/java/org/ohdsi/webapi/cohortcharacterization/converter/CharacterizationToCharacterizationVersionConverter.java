package org.ohdsi.webapi.cohortcharacterization.converter;

import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.cohortcharacterization.CcService;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.specification.CohortCharacterizationImpl;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.util.ExportUtil;
import org.ohdsi.webapi.versioning.domain.CharacterizationVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class CharacterizationToCharacterizationVersionConverter
        extends BaseConversionServiceAwareConverter<CohortCharacterizationEntity, CharacterizationVersion> {
    @Autowired
    private CcService ccService;

    @Autowired
    private Environment env;

    @Override
    public CharacterizationVersion convert(CohortCharacterizationEntity source) {
        CohortCharacterizationImpl characterizationImpl =
                conversionService.convert(source, CohortCharacterizationImpl.class);
        ExportUtil.clearCreateAndUpdateInfo(characterizationImpl);
        characterizationImpl.getFeatureAnalyses().forEach(ExportUtil::clearCreateAndUpdateInfo);
        characterizationImpl.getCohorts().forEach(ExportUtil::clearCreateAndUpdateInfo);
        characterizationImpl.setOrganizationName(env.getRequiredProperty("organization.name"));

        String expression = Utils.serialize(characterizationImpl, true);

        CharacterizationVersion target = new CharacterizationVersion();
        target.setAssetId(source.getId());
        target.setAssetJson(expression);

        return target;
    }
}
