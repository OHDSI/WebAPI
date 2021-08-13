package org.ohdsi.webapi.entity;

import com.odysseusinc.arachne.commons.types.DBMSType;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.KerberosAuthMechanism;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.prediction.PredictionAnalysis;
import org.ohdsi.webapi.prediction.PredictionController;
import org.ohdsi.webapi.prediction.PredictionService;
import org.ohdsi.webapi.prediction.dto.PredictionAnalysisDTO;
import org.ohdsi.webapi.prediction.repository.PredictionAnalysisRepository;
import org.ohdsi.webapi.prediction.specification.PatientLevelPredictionAnalysisImpl;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;

import static org.ohdsi.webapi.test.TestConstants.NEW_TEST_ENTITY;

public class PredictionEntityTest extends AbstractDatabaseTest implements TestCreate, TestCopy<PredictionAnalysisDTO>, TestImport<PredictionAnalysisDTO, PatientLevelPredictionAnalysisImpl> {
    @Autowired
    protected ConversionService conversionService;
    @Autowired
    protected PredictionController plpController;
    @Autowired
    protected PredictionAnalysisRepository plpRepository;
    @Autowired
    private SourceRepository sourceRepository;
    @Autowired
    protected PredictionService plpService;
    private PredictionAnalysisDTO firstSavedDTO;
    private static String PLP_SPECIFICATION;

    @BeforeClass
    public static void setPlpSpecification() throws IOException {

        File plp_spec = new File("src/test/resources/plp-specification.json");
        PLP_SPECIFICATION = FileUtils.readFileToString(plp_spec, StandardCharsets.UTF_8);
    }

    // in JUnit 4 it's impossible to mark methods inside interface with annotations, it was implemented in JUnit 5. After upgrade it's needed
    // to mark interface methods with @Test, @Before, @After and to remove them from this class
    @After
    @Override
    public void tearDownDB() {

        plpRepository.deleteAll();
    }

    @Before
    @Override
    public void init() throws Exception {
        TestCreate.super.init();
        truncateTable(String.format("%s.%s", "public", "source"));
        resetSequence(String.format("%s.%s", "public", "source_sequence"));
        sourceRepository.saveAndFlush(getCdmSource());
    }

    @Test
    @Override
    public void shouldNotCreateEntityWithDuplicateName() {

        TestCreate.super.shouldNotCreateEntityWithDuplicateName();
    }

    @Test
    @Override
    public void shouldCopyWithUniqueName() throws Exception {

        TestCopy.super.shouldCopyWithUniqueName();
    }

    @Test
    @Override
    public void shouldCopyFromCopy() throws Exception {

        TestCopy.super.shouldCopyFromCopy();
    }

    @Test
    @Override
    public void shouldCopySeveralTimesOriginal() throws Exception {

        TestCopy.super.shouldCopySeveralTimesOriginal();
    }

    @Test
    @Override
    public void shouldCopyOfPartlySameName() throws Exception {
        TestCopy.super.shouldCopyOfPartlySameName();
    }

    @Test
    @Override
    public void shouldImportUniqueName() throws Exception {

        TestImport.super.shouldImportUniqueName();
    }

    @Test
    @Override
    public void shouldImportWithTheSameName() throws Exception {

        TestImport.super.shouldImportWithTheSameName();
    }

    @Test
    @Override
    public void shouldImportWhenEntityWithNameExists() throws Exception {

        TestImport.super.shouldImportWhenEntityWithNameExists();
    }

    @Override
    public PredictionAnalysisDTO createCopy(PredictionAnalysisDTO dto) {

        return plpController.copy(dto.getId());
    }

    @Override
    public void initFirstDTO() {

        firstSavedDTO = createEntity(NEW_TEST_ENTITY);
    }

    @Override
    public PredictionAnalysisDTO getFirstSavedDTO() {

        return firstSavedDTO;
    }

    @Override
    public PredictionAnalysisDTO createEntity(String name) {

        return createEntity(createAndInitIncomingEntity(name));
    }

    @Override
    public PredictionAnalysisDTO createEntity(PredictionAnalysisDTO dto) {

        PredictionAnalysis prediction = createPrediction(dto.getName());
        return plpController.createAnalysis(prediction);
    }

    @Override
    public PredictionAnalysisDTO createAndInitIncomingEntity(String name) {

        PredictionAnalysis predictionAnalysis = createPrediction(name);
        return conversionService.convert(predictionAnalysis, PredictionAnalysisDTO.class);
    }

    private PredictionAnalysis createPrediction(String name) {

        PredictionAnalysis prediction = new PredictionAnalysis();
        prediction.setName(name);
        prediction.setSpecification(PLP_SPECIFICATION);
        return prediction;
    }

    @Override
    public String getConstraintName() {

        return "uq_pd_name";
    }

    @Override
    public CommonEntity getEntity(int id) {

        return plpService.getAnalysis(id);
    }

    @Override
    public PatientLevelPredictionAnalysisImpl getExportEntity(CommonEntity entity) {

        return plpController.exportAnalysis(entity.getId().intValue());
    }

    @Override
    public PredictionAnalysisDTO doImport(PatientLevelPredictionAnalysisImpl dto) throws Exception {

        return plpController.importAnalysis(dto);
    }

    private Source getCdmSource() throws SQLException {
        Source source = new Source();
        source.setSourceName("Embedded PG");
        source.setSourceKey("Embedded_PG");
        source.setSourceDialect(DBMSType.POSTGRESQL.getOhdsiDB());
        source.setSourceConnection(getDataSource().getConnection().getMetaData().getURL());
        source.setUsername("postgres");
        source.setPassword("postgres");
        source.setKrbAuthMethod(KerberosAuthMechanism.PASSWORD);

        SourceDaimon cdmDaimon = new SourceDaimon();
        cdmDaimon.setPriority(1);
        cdmDaimon.setDaimonType(SourceDaimon.DaimonType.CDM);
        cdmDaimon.setTableQualifier("cdm");
        cdmDaimon.setSource(source);

        SourceDaimon vocabDaimon = new SourceDaimon();
        vocabDaimon.setPriority(1);
        vocabDaimon.setDaimonType(SourceDaimon.DaimonType.Vocabulary);
        vocabDaimon.setTableQualifier("cdm");
        vocabDaimon.setSource(source);

        SourceDaimon resultsDaimon = new SourceDaimon();
        resultsDaimon.setPriority(1);
        resultsDaimon.setDaimonType(SourceDaimon.DaimonType.Results);
        resultsDaimon.setTableQualifier("results");
        resultsDaimon.setSource(source);

        source.setDaimons(Arrays.asList(cdmDaimon, vocabDaimon, resultsDaimon));

        return source;
    }
}
