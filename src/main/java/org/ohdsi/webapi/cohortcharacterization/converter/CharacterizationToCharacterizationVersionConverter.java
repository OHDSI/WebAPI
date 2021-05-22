package org.ohdsi.webapi.cohortcharacterization.converter;

import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.cohortcharacterization.CcService;
import org.ohdsi.webapi.cohortcharacterization.specification.CohortCharacterizationImpl;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.util.ExportUtil;
import org.ohdsi.webapi.versioning.domain.CharacterizationVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class CharacterizationToCharacterizationVersionConverter
        extends BaseConversionServiceAwareConverter<CohortCharacterizationImpl, CharacterizationVersion> {
    @Autowired
    private CcService ccService;

    @Autowired
    private Environment env;

    @Override
    public CharacterizationVersion convert(CohortCharacterizationImpl source) {
        ExportUtil.clearCreateAndUpdateInfo(source);
        source.getFeatureAnalyses().forEach(ExportUtil::clearCreateAndUpdateInfo);
        source.getCohorts().forEach(ExportUtil::clearCreateAndUpdateInfo);
        source.setOrganizationName(env.getRequiredProperty("organization.name"));

        String expression = Utils.serialize(source, true);

        CharacterizationVersion target = new CharacterizationVersion();
        target.setAssetId(source.getId());
        target.setAssetJson(expression);

        return target;
    }
}
