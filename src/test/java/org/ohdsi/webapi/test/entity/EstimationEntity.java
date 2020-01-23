package org.ohdsi.webapi.test.entity;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.analysis.AnalysisCohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetailsRepository;
import org.ohdsi.webapi.estimation.Estimation;
import org.ohdsi.webapi.estimation.EstimationController;
import org.ohdsi.webapi.estimation.EstimationService;
import org.ohdsi.webapi.estimation.dto.EstimationDTO;
import org.ohdsi.webapi.estimation.repository.EstimationRepository;
import org.ohdsi.webapi.estimation.specification.EstimationAnalysisImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;

import static org.junit.Assert.assertEquals;
import static org.ohdsi.analysis.estimation.design.EstimationTypeEnum.COMPARATIVE_COHORT_ANALYSIS;
import static org.ohdsi.webapi.test.TestConstants.NEW_TEST_ENTITY;

public class EstimationEntity extends TestImport {
    @Autowired
    protected ConversionService conversionService;
    @Autowired
    protected EstimationController pleController;
    @Autowired
    protected EstimationRepository pleRepository;
    @Autowired
    protected EstimationService pleService;
    @Autowired
    protected CohortDefinitionDetailsRepository cdRepository;
    private EstimationDTO firstSavedDTO;
    private static String PLE_SPECIFICATION;

    @BeforeClass
    public static void setPleSpecification() throws IOException {

        File ple_spec = new File("src/test/resources/ple-specification.json");
        PLE_SPECIFICATION = FileUtils.readFileToString(ple_spec, StandardCharsets.UTF_8);
    }

    @Override
    public void tearDownDB() {

        pleRepository.deleteAll();
    }

    @Test
    public void shouldImportWhenHashcodesOfCDsAndCSsAreDifferent() throws Exception {

        //Arrange
        File pleFile = new File("src/test/resources/ple-example-for-import.json");
        String pleStr = FileUtils.readFileToString(pleFile, StandardCharsets.UTF_8);

        EstimationAnalysisImpl ple = Utils.deserialize(pleStr, EstimationAnalysisImpl.class);

        //Action
        pleController.importAnalysis(ple);

        cdRepository.findAll().forEach(cd -> {
            cd.setExpression(cd.getExpression().replaceAll("5.0.0", "6.0.0"));
            cdRepository.save(cd);
        });
        EstimationDTO importedEs = pleController.importAnalysis(ple);

        //Assert
        assertEquals("Comparative effectiveness of ACE inhibitors vs Thiazide diuretics as first-line monotherapy for hypertension (1)",
                pleController.getAnalysis(importedEs.getId()).getName());
        EstimationAnalysisImpl importedExpression = pleService.getAnalysisExpression(importedEs.getId());
        List<AnalysisCohortDefinition> cds = importedExpression.getCohortDefinitions();
        assertEquals("New users of ACE inhibitors as first-line monotherapy for hypertension (1)", cds.get(0).getName());
        assertEquals("New users of Thiazide-like diuretics as first-line monotherapy for hypertension (1)", cds.get(1).getName());
        assertEquals("Acute myocardial infarction events (1)", cds.get(2).getName());
        assertEquals("Angioedema events (1)", cds.get(3).getName());
    }

    @Override
    protected Object createCopy(Object dto) throws Exception {

        return pleController.copy(((EstimationDTO) dto).getId());
    }

    @Override
    protected String getDtoName(Object dto) {

        return ((EstimationDTO) dto).getName();
    }

    @Override
    protected void initFirstDTO() throws Exception {

        firstSavedDTO = createEntity(NEW_TEST_ENTITY);
    }

    @Override
    protected Object getFirstSavedDTO() {

        return firstSavedDTO;
    }

    @Override
    protected EstimationDTO createEntity(String name) throws Exception {

        return createEntity(createAndInitIncomingEntity(name));
    }

    @Override
    protected EstimationDTO createEntity(Object dto) throws Exception {

        return pleController.createEstimation((Estimation) dto);
    }

    @Override
    protected Estimation createAndInitIncomingEntity(String name) {

        Estimation estimation = new Estimation();
        estimation.setName(name);
        estimation.setType(COMPARATIVE_COHORT_ANALYSIS);
        estimation.setSpecification(PLE_SPECIFICATION);
        return estimation;
    }

    @Override
    protected String getConstraintName() {

        return "uq_es_name";
    }

    @Override
    protected Integer getDtoId(Object dto) {

        return ((EstimationDTO) dto).getId();
    }

    @Override
    protected Object getEntity(int id) {

        return pleService.getAnalysis(id);
    }

    @Override
    protected Object getExportEntity(Object entity) {

        return pleController.exportAnalysis(((Estimation) entity).getId());
    }

    @Override
    protected void setExportName(Object entity, String name) {

        ((EstimationAnalysisImpl) entity).setName(name);
    }

    @Override
    protected Object doImport(Object dto) throws Exception {

        return pleController.importAnalysis((EstimationAnalysisImpl) dto);
    }
}
