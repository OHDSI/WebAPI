package org.ohdsi.webapi.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.circe.vocabulary.ConceptSetExpressionQueryBuilder;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.conceptset.ConceptSetGenerationInfoRepository;
import org.ohdsi.webapi.evidence.CohortStudyMapping;
import org.ohdsi.webapi.evidence.CohortStudyMappingRepository;
import org.ohdsi.webapi.evidence.ConceptCohortMapping;
import org.ohdsi.webapi.evidence.ConceptCohortMappingRepository;
import org.ohdsi.webapi.evidence.ConceptOfInterestMapping;
import org.ohdsi.webapi.evidence.ConceptOfInterestMappingRepository;
import org.ohdsi.webapi.evidence.DrugEvidence;
import org.ohdsi.webapi.evidence.EvidenceDetails;
import org.ohdsi.webapi.evidence.EvidenceSummary;
import org.ohdsi.webapi.evidence.EvidenceUniverse;
import org.ohdsi.webapi.evidence.HoiEvidence;
import org.ohdsi.webapi.evidence.DrugHoiEvidence;
import org.ohdsi.webapi.evidence.DrugLabel;
import org.ohdsi.webapi.evidence.DrugLabelInfo;
import org.ohdsi.webapi.evidence.DrugLabelRepository;
import org.ohdsi.webapi.evidence.EvidenceInfo;
import org.ohdsi.webapi.evidence.DrugRollUpEvidence;
import org.ohdsi.webapi.evidence.Evidence;
import org.ohdsi.webapi.evidence.SpontaneousReport;
import org.ohdsi.webapi.evidence.EvidenceSearch;
import org.ohdsi.webapi.evidence.negativecontrols.NegativeControlDTO;
import org.ohdsi.webapi.evidence.negativecontrols.NegativeControlMapper;
import org.ohdsi.webapi.evidence.negativecontrols.NegativeControlTaskParameters;
import org.ohdsi.webapi.evidence.negativecontrols.NegativeControlTasklet;
import org.ohdsi.webapi.job.GeneratesNotification;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.PreparedSqlRender;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

/**
 * Provides REST services for querying the Common Evidence Model
 *
 * @summary REST services for querying the Common Evidence Model See
 * <a href="https://github.com/OHDSI/CommonEvidenceModel">https://github.com/OHDSI/CommonEvidenceModel</a>
 */
@Path("/evidence")
@Component
public class EvidenceService extends AbstractDaoService implements GeneratesNotification {

    private static final String NAME = "negativeControlsAnalysisJob";

    @Autowired
    private JobTemplate jobTemplate;

    @Autowired
    private DrugLabelRepository drugLabelRepository;

    @Autowired
    private ConceptCohortMappingRepository mappingRepository;

    @Autowired
    private ConceptOfInterestMappingRepository conceptOfInterestMappingRepository;

    @Autowired
    private CohortStudyMappingRepository cohortStudyMappingRepository;

    @Autowired
    private ConceptSetGenerationInfoRepository conceptSetGenerationInfoRepository;

    @Autowired
    private ConceptSetService conceptSetService;

    private final RowMapper<DrugLabelInfo> drugLabelRowMapper = new RowMapper<DrugLabelInfo>() {
        @Override
        public DrugLabelInfo mapRow(final ResultSet rs, final int arg1) throws SQLException {
            final DrugLabelInfo returnVal = new DrugLabelInfo();
            returnVal.conceptId = rs.getString("CONCEPT_ID");
            returnVal.conceptName = rs.getString("CONCEPT_NAME");
            returnVal.usaProductLabelExists = rs.getInt("US_SPL_LABEL");
            return returnVal;
        }
    };

    public static class DrugConditionSourceSearchParams {

        @JsonProperty("targetDomain")
        public String targetDomain = "CONDITION";
        @JsonProperty("drugConceptIds")
        public int[] drugConceptIds;
        @JsonProperty("conditionConceptIds")
        public int[] conditionConceptIds;
        @JsonProperty("sourceIds")
        public String[] sourceIds;

        public String getDrugConceptIds() {
            return StringUtils.join(drugConceptIds, ',');
        }

        public String getConditionConceptIds() {
            return StringUtils.join(conditionConceptIds, ',');
        }

        public String getSourceIds() {
            if (sourceIds != null) {
                List<String> ids = Arrays.stream(sourceIds)
                        .map(sourceId -> sourceId.replaceAll("(\"|')", ""))
                        .collect(Collectors.toList());
                return "'" + StringUtils.join(ids, "','") + "'";
            }
            return "''";
        }
    }

    /**
     * <a href="https://github.com/OHDSI/Penelope">PENELOPE</a> function: search
     * the cohort_study table for the selected cohortId in the WebAPI DB
     *
     * @summary Find studies for a cohort - will be depreciated
     * @deprecated
     * @param cohortId The cohort Id
     * @return A list of studies related to the cohort
     */
    @GET
    @Path("study/{cohortId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<CohortStudyMapping> getCohortStudyMapping(@PathParam("cohortId") int cohortId) {
        return cohortStudyMappingRepository.findByCohortDefinitionId(cohortId);
    }

    /**
     * <a href="https://github.com/OHDSI/Penelope">PENELOPE</a> function: search
     * the COHORT_CONCEPT_MAP for the selected cohortId in the WebAPI DB
     *
     * @summary Find cohorts for a concept - will be depreciated
     * @deprecated
     * @param conceptId The concept Id of interest
     * @return A list of cohorts for the specified conceptId
     */
    @GET
    @Path("mapping/{conceptId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<ConceptCohortMapping> getConceptCohortMapping(@PathParam("conceptId") int conceptId) {
        return mappingRepository.findByConceptId(conceptId);
    }

    /**
     * <a href="https://github.com/OHDSI/Penelope">PENELOPE</a> function:
     * reference to a manually curated table related concept_of_interest in
     * WebAPI for use with PENELOPE. This will be depreciated in a future
     * release.
     *
     * @summary Find a custom concept mapping - will be depreciated
     * @deprecated
     * @param conceptId The conceptId of interest
     * @return A list of concepts based on the conceptId of interest
     */
    @GET
    @Path("conceptofinterest/{conceptId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<ConceptOfInterestMapping> getConceptOfInterest(@PathParam("conceptId") int conceptId) {
        return conceptOfInterestMappingRepository.findAllByConceptId(conceptId);
    }

    /**
     * <a href="https://github.com/OHDSI/Penelope">PENELOPE</a> function:
     * reference to the list of product labels in the WebAPI DRUG_LABELS table
     * that associates a product label SET_ID to the RxNorm ingredient. This
     * will be depreciated in a future release as this can be found using the
     * OMOP vocabulary
     *
     * @summary Find a drug label - will be depreciated
     * @deprecated
     * @param setid The drug label setId
     * @return The set of drug labels that match the setId specified.
     */
    @GET
    @Path("label/{setid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<DrugLabel> getDrugLabel(@PathParam("setid") String setid) {
        return drugLabelRepository.findAllBySetid(setid);
    }

    /**
     * <a href="https://github.com/OHDSI/Penelope">PENELOPE</a> function: search
     * the DRUG_LABELS.search_name for the searchTerm
     *
     * @summary Search for a drug label - will be depreciated
     * @deprecated
     * @param searchTerm The search term
     * @return A list of drug labels matching the search term
     */
    @GET
    @Path("labelsearch/{searchTerm}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<DrugLabel> searchDrugLabels(@PathParam("searchTerm") String searchTerm) {
        return drugLabelRepository.searchNameContainsTerm(searchTerm);
    }

    /**
     * Provides a high level description of the information found in the Common
     * Evidence Model (CEM).
     *
     * @summary Get summary of the Common Evidence Model (CEM) contents
     * @param sourceKey The source key containing the CEM daimon
     * @return A collection of evidence information stored in CEM
     */
    @GET
    @Path("{sourceKey}/info")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<EvidenceInfo> getInfo(@PathParam("sourceKey") String sourceKey) {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        String sqlPath = "/resources/evidence/sql/getInfo.sql";
        String tqName = "cem_schema";
        String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.CEM);
        PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sqlPath, tqName, tqValue);
        return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), (rs, rowNum) -> {

            EvidenceInfo info = new EvidenceInfo();
            info.title = rs.getString("TITLE");
            info.description = rs.getString("DESCRIPTION");
            info.provenance = rs.getString("PROVENANCE");
            info.contributor = rs.getString("CONTRIBUTOR");
            info.contactName = rs.getString("CONTACT_NAME");
            info.creationDate = rs.getDate("CREATION_DATE");
            info.coverageStartDate = rs.getDate("COVERAGE_START_DATE");
            info.coverageEndDate = rs.getDate("COVERAGE_END_DATE");
            info.versionIdentifier = rs.getString("VERSION_IDENTIFIER");
            return info;
        });
    }

    /**
     * Searches the evidence base for evidence related to one ore more drug and
     * condition combinations for the source(s) specified
     *
     * @param sourceKey The source key containing the CEM daimon
     * @param searchParams
     * @return
     */
    @POST
    @Path("{sourceKey}/drugconditionpairs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<DrugHoiEvidence> getDrugConditionPairs(@PathParam("sourceKey") String sourceKey, DrugConditionSourceSearchParams searchParams) {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        String sql = getDrugHoiEvidenceSQL(source, searchParams);
        return getSourceJdbcTemplate(source).query(sql, (rs, rowNum) -> {
            String evidenceSource = rs.getString("SOURCE_ID");
            String mappingType = rs.getString("MAPPING_TYPE");
            String drugConceptId = rs.getString("DRUG_CONCEPT_ID");
            String drugConceptName = rs.getString("DRUG_CONCEPT_NAME");
            String conditionConceptId = rs.getString("CONDITION_CONCEPT_ID");
            String conditionConceptName = rs.getString("CONDITION_CONCEPT_NAME");
            String uniqueIdentifier = rs.getString("UNIQUE_IDENTIFIER");

            DrugHoiEvidence evidence = new DrugHoiEvidence();
            evidence.evidenceSource = evidenceSource;
            evidence.mappingType = mappingType;
            evidence.drugConceptId = drugConceptId;
            evidence.drugConceptName = drugConceptName;
            evidence.hoiConceptId = conditionConceptId;
            evidence.hoiConceptName = conditionConceptName;
            evidence.uniqueIdentifier = uniqueIdentifier;

            return evidence;
        });
    }

    /**
     * Retrieves a list of evidence for the specified drug conceptId
     *
     * @summary Get Evidence For Drug
     * @param sourceKey The source key containing the CEM daimon
     * @param id - An RxNorm Drug Concept Id
     * @return A list of evidence
     */
    @GET
    @Path("{sourceKey}/drug/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<DrugEvidence> getDrugEvidence(@PathParam("sourceKey") String sourceKey, @PathParam("id") final Long id) {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        PreparedStatementRenderer psr = prepareGetEvidenceForConcept(source, id);
        return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), (rs, rowNum) -> {
            String evidenceSource = rs.getString("SOURCE_ID");
            String hoi = rs.getString("CONCEPT_ID_2");
            String hoiName = rs.getString("CONCEPT_ID_2_NAME");
            String statType = rs.getString("STATISTIC_VALUE_TYPE");
            BigDecimal statVal = rs.getBigDecimal("STATISTIC_VALUE");
            String relationshipType = rs.getString("RELATIONSHIP_ID");
            String uniqueIdentifier = rs.getString("UNIQUE_IDENTIFIER");
            String uniqueIdentifierType = rs.getString("UNIQUE_IDENTIFIER_TYPE");

            DrugEvidence evidence = new DrugEvidence();
            evidence.evidenceSource = evidenceSource;
            evidence.hoiConceptId = hoi;
            evidence.hoiConceptName = hoiName;
            evidence.relationshipType = relationshipType;
            evidence.statisticType = statType;
            evidence.statisticValue = statVal;
            evidence.uniqueIdentifier = uniqueIdentifier;
            evidence.uniqueIdentifierType = uniqueIdentifierType;

            return evidence;
        });
    }

    /**
     * Retrieves a list of evidence for the specified health outcome of interest
     * (hoi) conceptId
     *
     * @summary Get Evidence For Health Outcome
     * @param sourceKey The source key containing the CEM daimon
     * @param id The conceptId for the health outcome of interest
     * @return A list of evidence
     */
    @GET
    @Path("{sourceKey}/hoi/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<HoiEvidence> getHoiEvidence(@PathParam("sourceKey") String sourceKey, @PathParam("id") final Long id) {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        PreparedStatementRenderer psr = prepareGetEvidenceForConcept(source, id);
        return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), (rs, rowNum) -> {
            String evidenceSource = rs.getString("SOURCE_ID");
            String drug = rs.getString("CONCEPT_ID_1");
            String drugName = rs.getString("CONCEPT_ID_1_NAME");
            String statType = rs.getString("STATISTIC_VALUE_TYPE");
            BigDecimal statVal = rs.getBigDecimal("STATISTIC_VALUE");
            String relationshipType = rs.getString("RELATIONSHIP_ID");
            String uniqueIdentifier = rs.getString("UNIQUE_IDENTIFIER");
            String uniqueIdentifierType = rs.getString("UNIQUE_IDENTIFIER_TYPE");

            HoiEvidence evidence = new HoiEvidence();
            evidence.evidenceSource = evidenceSource;
            evidence.drugConceptId = drug;
            evidence.drugConceptName = drugName;
            evidence.relationshipType = relationshipType;
            evidence.statisticType = statType;
            evidence.statisticValue = statVal;
            evidence.uniqueIdentifier = uniqueIdentifier;
            evidence.uniqueIdentifierType = uniqueIdentifierType;

            return evidence;
        });
    }

    /**
     * Retrieves a list of RxNorm ingredients from the concept set and
     * determines if we have label evidence for them.
     *
     * @summary Get Drug Labels For RxNorm Ingredients
     * @param sourceKey The source key of the CEM daimon
     * @param identifiers The list of RxNorm Ingredients concepts or ancestors
     * @return A list of evidence for the drug and HOI
     */
    @Path("{sourceKey}/druglabel")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Collection<DrugLabelInfo> getDrugIngredientLabel(@PathParam("sourceKey") String sourceKey, long[] identifiers) {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        return executeGetDrugLabels(identifiers, source);
    }

    /**
     * Retrieves a list of evidence for the specified health outcome of interest
     * and drug as defined in the key parameter.
     *
     * @summary Get Evidence For Drug & Health Outcome
     * @param sourceKey The source key of the CEM daimon
     * @param key The key must be structured as {drugConceptId}-{hoiConceptId}
     * @return A list of evidence for the drug and HOI
     */
    @GET
    @Path("{sourceKey}/drughoi/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<DrugHoiEvidence> getDrugHoiEvidence(@PathParam("sourceKey") String sourceKey, @PathParam("key") final String key) {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        PreparedStatementRenderer psr = prepareGetDrugHoiEvidence(key, source);
        return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), (rs, rowNum) -> {
            String evidenceSource = rs.getString("SOURCE_ID");
            String drug = rs.getString("CONCEPT_ID_1");
            String drugName = rs.getString("CONCEPT_ID_1_NAME");
            String hoi = rs.getString("CONCEPT_ID_2");
            String hoiName = rs.getString("CONCEPT_ID_2_NAME");
            String statType = rs.getString("STATISTIC_VALUE_TYPE");
            BigDecimal statVal = rs.getBigDecimal("STATISTIC_VALUE");
            String relationshipType = rs.getString("RELATIONSHIP_ID");
            String uniqueIdentifier = rs.getString("UNIQUE_IDENTIFIER");
            String uniqueIdentifierType = rs.getString("UNIQUE_IDENTIFIER_TYPE");

            DrugHoiEvidence evidence = new DrugHoiEvidence();
            evidence.evidenceSource = evidenceSource;
            evidence.drugConceptId = drug;
            evidence.drugConceptName = drugName;
            evidence.hoiConceptId = hoi;
            evidence.hoiConceptName = hoiName;
            evidence.relationshipType = relationshipType;
            evidence.statisticType = statType;
            evidence.statisticValue = statVal;
            evidence.uniqueIdentifier = uniqueIdentifier;
            evidence.uniqueIdentifierType = uniqueIdentifierType;

            return evidence;
        });
    }

    /**
     * Originally provided a roll up of evidence from LAERTES
     *
     * @summary Depreciated
     * @deprecated
     * @param sourceKey The source key of the CEM daimon
     * @param id The RxNorm drug conceptId
     * @param filter Specified the type of rollup level (ingredient, clinical
     * drug, branded drug)
     * @return A list of evidence rolled up
     */
    @GET
    @Path("{sourceKey}/drugrollup/{filter}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDrugRollupIngredientEvidence(@PathParam("sourceKey") String sourceKey, @PathParam("id") final Long id, @PathParam("filter") final String filter) {
        String warningMessage = "This method will be deprecated in the next release. Instead, please use the new REST endpoint: evidence/{sourceKey}/drug/{id}";
        ArrayList<DrugRollUpEvidence> evidence = new ArrayList<>();
        return Response.ok(evidence).header("Warning: 299", warningMessage).build();
    }

    /**
     * Retrieve all evidence from Common Evidence Model (CEM) for a given
     * conceptId
     *
     * @summary Get evidence for a concept
     * @param sourceKey The source key of the CEM daimon
     * @param id The conceptId of interest
     * @return A list of evidence matching the conceptId of interest
     */
    @GET
    @Path("{sourceKey}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Evidence> getEvidence(@PathParam("sourceKey") String sourceKey, @PathParam("id") final Long id) {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        PreparedStatementRenderer psr = prepareGetEvidenceForConcept(source, id);
        return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), (rs, rowNum) -> {
            String evidenceSource = rs.getString("SOURCE_ID");
            String drug = rs.getString("CONCEPT_ID_1");
            String drugName = rs.getString("CONCEPT_ID_1_NAME");
            String hoi = rs.getString("CONCEPT_ID_2");
            String hoiName = rs.getString("CONCEPT_ID_2_NAME");
            String statType = rs.getString("STATISTIC_VALUE_TYPE");
            BigDecimal statVal = rs.getBigDecimal("STATISTIC_VALUE");
            String relationshipType = rs.getString("RELATIONSHIP_ID");
            String uniqueIdentifier = rs.getString("UNIQUE_IDENTIFIER");
            String uniqueIdentifierType = rs.getString("UNIQUE_IDENTIFIER_TYPE");

            Evidence evidence = new Evidence();
            evidence.evidenceSource = evidenceSource;
            evidence.drugConceptId = drug;
            evidence.drugConceptName = drugName;
            evidence.hoiConceptId = hoi;
            evidence.hoiConceptName = hoiName;
            evidence.relationshipType = relationshipType;
            evidence.statisticType = statType;
            evidence.statisticValue = statVal;
            evidence.uniqueIdentifier = uniqueIdentifier;
            evidence.uniqueIdentifierType = uniqueIdentifierType;

            return evidence;
        });
    }

    /**
     * Originally provided an evidence summary from LAERTES
     *
     * @summary Depreciated
     * @deprecated
     * @param sourceKey The source key of the CEM daimon
     * @param conditionID The condition conceptId
     * @param drugID The drug conceptId
     * @param evidenceGroup The evidence group
     * @return A summary of evidence
     */
    @GET
    @Path("{sourceKey}/evidencesummary")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEvidenceSummaryBySource(@PathParam("sourceKey") String sourceKey, @QueryParam("conditionID") String conditionID, @QueryParam("drugID") String drugID, @QueryParam("evidenceGroup") String evidenceGroup) {
        String warningMessage = "This method will be deprecated in the next release. Instead, please use the new REST endpoint: evidence/{sourceKey}/drug/{id}";
        ArrayList<EvidenceSummary> evidenceSummary = new ArrayList<>();
        return Response.ok(evidenceSummary).header("Warning: 299", warningMessage).build();
    }

    /**
     * Originally provided an evidence details from LAERTES
     *
     * @summary Depreciated
     * @deprecated
     * @param sourceKey The source key of the CEM daimon
     * @param conditionID The condition conceptId
     * @param drugID The drug conceptId
     * @param evidenceType The evidence type
     * @return A list of evidence details
     * @throws org.codehaus.jettison.json.JSONException
     * @throws java.io.IOException
     */
    @GET
    @Path("{sourceKey}/evidencedetails")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEvidenceDetails(@PathParam("sourceKey") String sourceKey,
            @QueryParam("conditionID") String conditionID,
            @QueryParam("drugID") String drugID,
            @QueryParam("evidenceType") String evidenceType)
            throws JSONException, IOException {
        String warningMessage = "This method will be deprecated in the next release. Instead, please use the new REST endpoint: evidence/{sourceKey}/drug/{id}";
        ArrayList<EvidenceDetails> evidenceDetails = new ArrayList<>();
        return Response.ok(evidenceDetails).header("Warning: 299", warningMessage).build();
    }

    /**
     * Originally provided an summary from spontaneous reports from LAERTES
     *
     * @summary Depreciated
     * @deprecated
     * @param sourceKey The source key of the CEM daimon
     * @param search The search term
     * @return A list of spontaneous report summaries
     * @throws JSONException
     * @throws IOException
     */
    @POST
    @Path("{sourceKey}/spontaneousreports")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getSpontaneousReports(@PathParam("sourceKey") String sourceKey, EvidenceSearch search) throws JSONException, IOException {
        String warningMessage = "This method will be deprecated in the next release.";
        ArrayList<SpontaneousReport> returnVal = new ArrayList<>();
        return Response.ok(returnVal).header("Warning: 299", warningMessage).build();
    }

    /**
     * Originally provided an evidence search from LAERTES
     *
     * @summary Depreciated
     * @deprecated
     * @param sourceKey The source key of the CEM daimon
     * @param search The search term
     * @return A list of evidence
     * @throws JSONException
     * @throws IOException
     */
    @POST
    @Path("{sourceKey}/evidencesearch")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response evidenceSearch(@PathParam("sourceKey") String sourceKey, EvidenceSearch search) throws JSONException, IOException {
        String warningMessage = "This method will be deprecated in the next release.";
        ArrayList<EvidenceUniverse> returnVal = new ArrayList<>();
        return Response.ok(returnVal).header("Warning: 299", warningMessage).build();
    }

    /**
     * Originally provided a label evidence search from LAERTES
     *
     * @summary Depreciated
     * @deprecated
     * @param sourceKey The source key of the CEM daimon
     * @param search The search term
     * @return A list of evidence
     * @throws JSONException
     * @throws IOException
     */
    @POST
    @Path("{sourceKey}/labelevidence")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response labelEvidence(@PathParam("sourceKey") String sourceKey, EvidenceSearch search) throws JSONException, IOException {
        String warningMessage = "This method will be deprecated in the next release.";
        ArrayList<EvidenceUniverse> returnVal = new ArrayList<>();
        return Response.ok(returnVal).header("Warning: 299", warningMessage).build();
    }

    /**
     * Queues up a negative control generation task to compute negative controls
     * using Common Evidence Model (CEM)
     *
     * @summary Generate negative controls
     * @param sourceKey The source key of the CEM daimon
     * @param task - The negative control task with parameters
     * @return information about the negative control job
     * @throws Exception
     */
    @POST
    @Path("{sourceKey}/negativecontrols")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public JobExecutionResource queueNegativeControlsJob(@PathParam("sourceKey") String sourceKey, NegativeControlTaskParameters task) throws Exception {
        if (task == null) {
            return null;
        }
        JobParametersBuilder builder = new JobParametersBuilder();

        // Get a JDBC template for the OHDSI source repository 
        // and the source dialect for use when we write the results
        // back to the OHDSI repository
        JdbcTemplate jdbcTemplate = getJdbcTemplate();
        task.setJdbcTemplate(jdbcTemplate);
        String ohdsiDatasourceSourceDialect = getSourceDialect();
        task.setSourceDialect(ohdsiDatasourceSourceDialect);
        task.setOhdsiSchema(this.getOhdsiSchema());

        // source key comes from the client, we look it up here and hand it off to the tasklet
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        // Verify the source has both the evidence & results daimon configured
        // and throw an exception if either is missing
        String cemSchema = source.getTableQualifier(SourceDaimon.DaimonType.CEM);
        String cemResultsSchema = source.getTableQualifierOrNull(SourceDaimon.DaimonType.CEMResults);
        if (cemSchema == null) {
            throw NotFoundException("Evidence daimon not configured for source.");
        }
        if (cemResultsSchema == null) {
            throw NotFoundException("Results daimon not configured for source.");
        }

        task.setSource(source);

        if (!StringUtils.isEmpty(task.getJobName())) {
            builder.addString("jobName", limitJobParams(task.getJobName()));
        }
        builder.addString("concept_set_id", ("" + task.getConceptSetId()));
        builder.addString("concept_set_name", task.getConceptSetName());
        builder.addString("concept_domain_id", task.getConceptDomainId());
        builder.addString("source_id", ("" + source.getSourceId()));

        // Create a set of parameters to store with the generation info
        JSONObject params = new JSONObject();
        // If/when we want to treat these concepts as lists, this
        // code will do the trick
        //JSONArray conceptsToInclude = new JSONArray();
        //JSONArray conceptsToExclude = new JSONArray();
        //for(int i = 0; i < task.getConceptsToInclude().length; i++) {
        //	conceptsToInclude.put(task.getConceptsToInclude()[i]);
        //}
        //for(int i = 0; i < task.getConceptsToExclude().length; i++) {
        //	conceptsToExclude.put(task.getConceptsToExclude()[i]);
        //}
        params.put("csToInclude", task.getCsToInclude());
        params.put("csToExclude", task.getCsToExclude());
        builder.addString("params", params.toString());

        // Resolve the concept set expressions for the included and excluded
        // concept sets if specified
        ConceptSetExpressionQueryBuilder csBuilder = new ConceptSetExpressionQueryBuilder();
        ConceptSetExpression csExpression;
        String csSQL = "";
        if (task.getCsToInclude() > 0) {
            try {
                csExpression = conceptSetService.getConceptSetExpression(task.getCsToInclude());
                csSQL = csBuilder.buildExpressionQuery(csExpression);
            } catch (Exception e) {
                log.warn("Failed to build Inclusion expression query", e);
            }
        }
        task.setCsToIncludeSQL(csSQL);
        csSQL = "";
        if (task.getCsToExclude() > 0) {
            try {
                csExpression = conceptSetService.getConceptSetExpression(task.getCsToExclude());
                csSQL = csBuilder.buildExpressionQuery(csExpression);
            } catch (Exception e) {
                log.warn("Failed to build Exclusion expression query", e);
            }
        }
        task.setCsToExcludeSQL(csSQL);

        final String taskString = task.toString();
        final JobParameters jobParameters = builder.toJobParameters();
        log.info("Beginning run for negative controls analysis task: {}", taskString);

        NegativeControlTasklet tasklet = new NegativeControlTasklet(task, getSourceJdbcTemplate(task.getSource()), task.getJdbcTemplate(),
                getTransactionTemplate(), this.conceptSetGenerationInfoRepository, this.getSourceDialect());

        return this.jobTemplate.launchTasklet(NAME, "negativeControlsAnalysisStep", tasklet, jobParameters);
    }

    /**
     * Retrieves the negative controls for a concept set
     *
     * @summary Retrieve negative controls
     * @param sourceKey The source key of the CEM daimon
     * @param conceptSetId The concept set id
     * @return The list of negative controls
     */
    @GET
    @Path("{sourceKey}/negativecontrols/{conceptsetid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Collection<NegativeControlDTO> getNegativeControls(@PathParam("sourceKey") String sourceKey, @PathParam("conceptsetid") int conceptSetId) throws Exception {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        PreparedStatementRenderer psr = this.prepareGetNegativeControls(source, conceptSetId);
        final List<NegativeControlDTO> recs = getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), new NegativeControlMapper());
        return recs;
    }

    /**
     * Retrieves parameterized SQL used to generate negative controls
     *
     * @summary Retrieves parameterized SQL used to generate negative controls
     * @param sourceKey The source key of the CEM daimon
     * @return The list of negative controls
     */
    @GET
    @Path("{sourceKey}/negativecontrols/sql")
    @Produces(MediaType.TEXT_PLAIN)
    public String getNegativeControlsSqlStatement(@PathParam("sourceKey") String sourceKey,
            @DefaultValue("CONDITION") @QueryParam("conceptDomain") String conceptDomain,
            @DefaultValue("DRUG") @QueryParam("targetDomain") String targetDomain,
            @DefaultValue("192671") @QueryParam("conceptOfInterest") String conceptOfInterest) {
        NegativeControlTaskParameters task = new NegativeControlTaskParameters();
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        task.setSource(source);
        task.setCsToIncludeSQL("");
        task.setCsToExcludeSQL("");
        task.setConceptDomainId(conceptDomain);
        task.setOutcomeOfInterest(targetDomain);
        CharSequence csCommaDelimited = ",";
        if (conceptOfInterest.contains(csCommaDelimited)) {
            task.setConceptsOfInterest(conceptOfInterest.split(","));
        } else {
            task.setConceptsOfInterest(new String[]{conceptOfInterest});
        }
        return getNegativeControlSql(task);
    }

    @Override
    public String getJobName() {
        return NAME;
    }

    @Override
    public String getExecutionFoldingKey() {
        return "concept_set_id";
    }

    /**
     * Retrieve the SQL used to generate negative controls
     *
     * @summary Get negative control SQL
     * @param task The task containing the parameters for generating negative
     * controls
     * @return The SQL script for generating negative controls
     */
    public static String getNegativeControlSql(NegativeControlTaskParameters task) {
        StringBuilder sb = new StringBuilder();
        String resourceRoot = "/resources/evidence/sql/negativecontrols/";
        Source source = task.getSource();
        String cemSchema = source.getTableQualifier(SourceDaimon.DaimonType.CEM);
        String cemResultsSchema = source.getTableQualifierOrNull(SourceDaimon.DaimonType.CEMResults);
        String vocabularySchema = source.getTableQualifierOrNull(SourceDaimon.DaimonType.Vocabulary);
        if (vocabularySchema == null) {
            vocabularySchema = cemSchema;
        }
        String translatedSchema = task.getTranslatedSchema();
        if (translatedSchema == null) {
            translatedSchema = cemSchema;
        }

        String csToExcludeSQL = SqlRender.renderSql(task.getCsToExcludeSQL(),
                new String[]{"vocabulary_database_schema"},
                new String[]{vocabularySchema}
        );
        String csToIncludeSQL = SqlRender.renderSql(task.getCsToIncludeSQL(),
                new String[]{"vocabulary_database_schema"},
                new String[]{vocabularySchema}
        );

        String outcomeOfInterest = task.getOutcomeOfInterest().toLowerCase();
        String conceptsOfInterest = JoinArray(task.getConceptsOfInterest());
        String csToInclude = String.valueOf(task.getCsToInclude());
        String csToExclude = String.valueOf(task.getCsToExclude());
        String medlineWinnenburgTable = translatedSchema + ".MEDLINE_WINNENBURG";
        String splicerTable = translatedSchema + ".SPLICER";
        String aeolusTable = translatedSchema + ".AEOLUS";
        String conceptsToExcludeData = "#NC_EXCLUDED_CONCEPTS";
        String conceptsToIncludeData = "#NC_INCLUDED_CONCEPTS";
        String broadConceptsData = cemSchema + ".NC_LU_BROAD_CONCEPTS";
        String drugInducedConditionsData = cemSchema + ".NC_LU_DRUG_INDUCED_CONDITIONS";
        String pregnancyConditionData = cemSchema + ".NC_LU_PREGNANCY_CONDITIONS";

        String[] params = new String[]{"outcomeOfInterest", "conceptsOfInterest", "vocabulary", "cem_schema", "cem_results_schema", "translatedSchema"};
        String[] values = new String[]{outcomeOfInterest, conceptsOfInterest, vocabularySchema, cemSchema, cemResultsSchema, translatedSchema};

        String sqlFile = "findConceptUniverse.sql";
        sb.append("-- ").append(sqlFile).append("\n\n");
        String sql = ResourceHelper.GetResourceAsString(resourceRoot + sqlFile);
        sql = SqlRender.renderSql(sql, params, values);
        sb.append(sql + "\n\n");

        sqlFile = "findDrugIndications.sql";
        sb.append("-- ").append(sqlFile).append("\n\n");
        sql = ResourceHelper.GetResourceAsString(resourceRoot + sqlFile);
        sql = SqlRender.renderSql(sql, params, values);
        sb.append(sql + "\n\n");

        sqlFile = "findConcepts.sql";
        sb.append("-- User excluded - ").append(sqlFile).append("\n\n");
        sql = ResourceHelper.GetResourceAsString(resourceRoot + sqlFile);
        sql = SqlRender.renderSql(sql,
                ArrayUtils.addAll(params, new String[]{"storeData", "conceptSetId", "conceptSetExpression"}),
                ArrayUtils.addAll(values, new String[]{conceptsToExcludeData, csToExclude, csToExcludeSQL})
        );
        sb.append(sql + "\n\n");

        sqlFile = "findConcepts.sql";
        sb.append("-- User included - ").append(sqlFile).append("\n\n");
        sql = ResourceHelper.GetResourceAsString(resourceRoot + sqlFile);
        sql = SqlRender.renderSql(sql,
                ArrayUtils.addAll(params, new String[]{"storeData", "conceptSetId", "conceptSetExpression"}),
                ArrayUtils.addAll(values, new String[]{conceptsToIncludeData, csToInclude, csToIncludeSQL})
        );
        sb.append(sql + "\n\n");

        sqlFile = "pullEvidencePrep.sql";
        sb.append("-- ").append(sqlFile).append("\n\n");
        sql = ResourceHelper.GetResourceAsString(resourceRoot + sqlFile);
        sql = SqlRender.renderSql(sql, params, values);
        sb.append(sql + "\n\n");

        sqlFile = "pullEvidence.sql";
        sb.append("-- MEDLINE_WINNENBURG -- ").append(sqlFile).append("\n\n");
        sql = ResourceHelper.GetResourceAsString(resourceRoot + sqlFile);
        sql = SqlRender.renderSql(sql,
                ArrayUtils.addAll(params, new String[]{"adeType", "adeData"}),
                ArrayUtils.addAll(values, new String[]{"MEDLINE_WINNENBURG", medlineWinnenburgTable})
        );
        sb.append(sql + "\n\n");

        sqlFile = "pullEvidence.sql";
        sb.append("-- SPLICER -- ").append(sqlFile).append("\n\n");
        sql = ResourceHelper.GetResourceAsString(resourceRoot + sqlFile);
        sql = SqlRender.renderSql(sql,
                ArrayUtils.addAll(params, new String[]{"adeType", "adeData"}),
                ArrayUtils.addAll(values, new String[]{"SPLICER", splicerTable})
        );
        sb.append(sql + "\n\n");

        sqlFile = "pullEvidence.sql";
        sb.append("-- AEOLUS -- ").append(sqlFile).append("\n\n");
        sql = ResourceHelper.GetResourceAsString(resourceRoot + sqlFile);
        sql = SqlRender.renderSql(sql,
                ArrayUtils.addAll(params, new String[]{"adeType", "adeData"}),
                ArrayUtils.addAll(values, new String[]{"AEOLUS", aeolusTable})
        );
        sb.append(sql + "\n\n");

        sqlFile = "pullEvidencePost.sql";
        sb.append("-- ").append(sqlFile).append("\n\n");
        sql = ResourceHelper.GetResourceAsString(resourceRoot + sqlFile);
        sql = SqlRender.renderSql(sql, params, values);
        sb.append(sql + "\n\n");

        sqlFile = "summarizeEvidence.sql";
        sb.append("-- ").append(sqlFile).append("\n\n");
        sql = ResourceHelper.GetResourceAsString(resourceRoot + sqlFile);
        sql = SqlRender.renderSql(sql,
                ArrayUtils.addAll(params, new String[]{"broadConceptsData", "drugInducedConditionsData", "pregnancyConditionData", "conceptsToExclude", "conceptsToInclude"}),
                ArrayUtils.addAll(values, new String[]{broadConceptsData, drugInducedConditionsData, pregnancyConditionData, conceptsToExcludeData, conceptsToIncludeData})
        );
        sb.append(sql + "\n\n");

        sqlFile = "optimizeEvidence.sql";
        sb.append("-- ").append(sqlFile).append("\n\n");
        sql = ResourceHelper.GetResourceAsString(resourceRoot + sqlFile);
        sql = SqlRender.renderSql(sql, params, values);
        sb.append(sql + "\n\n");

        sqlFile = "deleteJobResults.sql";
        sb.append("-- ").append(sqlFile).append("\n\n");
        sql = EvidenceService.getJobResultsDeleteStatementSql(cemResultsSchema, task.getConceptSetId());
        sb.append(sql + "\n\n");

        sqlFile = "exportNegativeControls.sql";
        sb.append("-- ").append(sqlFile).append("\n\n");
        sql = ResourceHelper.GetResourceAsString(resourceRoot + sqlFile);
        sql = SqlRender.renderSql(sql,
                ArrayUtils.addAll(params, new String[]{"conceptSetId"}),
                ArrayUtils.addAll(values, new String[]{Integer.toString(task.getConceptSetId())})
        );
        sb.append(sql + "\n\n");

        sql = SqlTranslate.translateSql(sb.toString(), source.getSourceDialect());

        return sql;
    }

    /**
     * SQL to delete negative controls job results
     *
     * @summary SQL to delete negative controls job results
     * @param cemResultsSchema The CEM results schema
     * @param conceptSetId The concept set ID
     * @return The SQL statement
     */
    public static String getJobResultsDeleteStatementSql(String cemResultsSchema, int conceptSetId) {
        String sql = ResourceHelper.GetResourceAsString("/resources/evidence/sql/negativecontrols/deleteJobResults.sql");
        sql = SqlRender.renderSql(sql,
                (new String[]{"cem_results_schema", "conceptSetId"}),
                (new String[]{cemResultsSchema, Integer.toString(conceptSetId)})
        );
        return sql;
    }

    /**
     * SQL to insert negative controls
     *
     * @summary SQL to insert negative controls
     * @param task The negative control task and parameters
     * @return The SQL statement
     */
    public static String getNegativeControlInsertStatementSql(NegativeControlTaskParameters task) {
        String sql = ResourceHelper.GetResourceAsString("/resources/evidence/sql/negativecontrols/insertNegativeControls.sql");
        sql = SqlRender.renderSql(sql, new String[]{"ohdsiSchema"}, new String[]{task.getOhdsiSchema()});
        sql = SqlTranslate.translateSql(sql, task.getSourceDialect());

        return sql;
    }

    protected PreparedStatementRenderer prepareExecuteGetDrugLabels(long[] identifiers, Source source) {
        String sqlPath = "/resources/evidence/sql/getDrugLabelForIngredients.sql";
        String cemSchema = source.getTableQualifier(SourceDaimon.DaimonType.CEM);
        String vocabularySchema = source.getTableQualifierOrNull(SourceDaimon.DaimonType.Vocabulary);
        if (vocabularySchema == null) {
            vocabularySchema = cemSchema;
        }
        String[] tableQualifierNames = new String[]{"cem_schema", "vocabularySchema"};
        String[] tableQualifierValues = new String[]{cemSchema, vocabularySchema};
        return new PreparedStatementRenderer(source, sqlPath, tableQualifierNames, tableQualifierValues, "conceptIds", identifiers);
    }

    /**
     * Get the SQL for obtaining product label evidence from a set of RxNorm
     * Ingredients
     *
     * @summary SQL for obtaining product label evidence
     * @param identifiers The list of RxNorm Ingredient conceptIds
     * @param source The source that contains the CEM daimon
     * @return A prepared SQL statement
     */
    protected Collection<DrugLabelInfo> executeGetDrugLabels(long[] identifiers, Source source) {
        Collection<DrugLabelInfo> info = new ArrayList<>();
        if (identifiers.length == 0) {
            return info;
        } else {
            int parameterLimit = PreparedSqlRender.getParameterLimit(source);
            if (parameterLimit > 0 && identifiers.length > parameterLimit) {
                info = executeGetDrugLabels(Arrays.copyOfRange(identifiers, parameterLimit, identifiers.length), source);
                System.out.println("executeGetDrugLabels: " + info.size());
                identifiers = Arrays.copyOfRange(identifiers, 0, parameterLimit);
            }
            PreparedStatementRenderer psr = prepareExecuteGetDrugLabels(identifiers, source);
            info.addAll(getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), this.drugLabelRowMapper));
            return info;
        }
    }

    /**
     * Get the SQL for obtaining evidence for a drug/condition pair for source
     * ids
     *
     * @summary SQL for obtaining evidence for a drug/hoi pair by source
     * @param source The source that contains the CEM daimon
     * @return A prepared SQL statement
     */
    protected String getDrugHoiEvidenceSQL(Source source, DrugConditionSourceSearchParams searchParams) {
        String sqlPath = "/resources/evidence/sql/getDrugConditionPairBySourceId.sql";
        String sql = ResourceHelper.GetResourceAsString(sqlPath);
        String cemSchema = source.getTableQualifier(SourceDaimon.DaimonType.CEM);
        String vocabularySchema = source.getTableQualifierOrNull(SourceDaimon.DaimonType.Vocabulary);
        if (vocabularySchema == null) {
            vocabularySchema = cemSchema;
        }
        String[] params = new String[]{"cem_schema", "vocabularySchema", "targetDomain", "sourceIdList", "drugList", "conditionList"};
        String[] values = new String[]{cemSchema, vocabularySchema, searchParams.targetDomain.toUpperCase(), searchParams.getSourceIds(), searchParams.getDrugConceptIds(), searchParams.getConditionConceptIds()};
        sql = SqlRender.renderSql(sql, params, values);
        sql = SqlTranslate.translateSql(sql, source.getSourceDialect());
        return sql;
    }

    /**
     * Get the SQL for obtaining evidence for a drug/hoi combination
     *
     * @summary SQL for obtaining evidence for a drug/hoi combination
     * @param key The drug-hoi conceptId pair
     * @param source The source that contains the CEM daimon
     * @return A prepared SQL statement
     */
    protected PreparedStatementRenderer prepareGetDrugHoiEvidence(final String key, Source source) {
        String[] par = key.split("-");
        String drug_id = par[0];
        String hoi_id = par[1];
        String sqlPath = "/resources/evidence/sql/getDrugHoiEvidence.sql";
        String cemSchema = source.getTableQualifier(SourceDaimon.DaimonType.CEM);
        String vocabularySchema = source.getTableQualifierOrNull(SourceDaimon.DaimonType.Vocabulary);
        if (vocabularySchema == null) {
            vocabularySchema = cemSchema;
        }
        String[] tableQualifierNames = new String[]{"cem_schema", "vocabularySchema"};
        String[] tableQualifierValues = new String[]{cemSchema, vocabularySchema};
        String[] names = new String[]{"drug_id", "hoi_id"};
        Object[] values = new Integer[]{Integer.parseInt(drug_id), Integer.parseInt(hoi_id)};
        return new PreparedStatementRenderer(source, sqlPath, tableQualifierNames, tableQualifierValues, names, values);
    }

    /**
     * Get the SQL for obtaining evidence for a concept
     *
     * @summary SQL for obtaining evidence for a concept
     * @param source The source that contains the CEM daimon
     * @param conceptId The conceptId of interest
     * @return A prepared SQL statement
     */
    protected PreparedStatementRenderer prepareGetEvidenceForConcept(Source source, Long conceptId) {
        String sqlPath = "/resources/evidence/sql/getEvidenceForConcept.sql";
        String cemSchema = source.getTableQualifier(SourceDaimon.DaimonType.CEM);
        String vocabularySchema = source.getTableQualifierOrNull(SourceDaimon.DaimonType.Vocabulary);
        if (vocabularySchema == null) {
            vocabularySchema = cemSchema;
        }
        String[] tableQualifierNames = new String[]{"cem_schema", "vocabularySchema"};
        String[] tableQualifierValues = new String[]{cemSchema, vocabularySchema};
        String[] names = new String[]{"id"};
        Object[] values = new Long[]{conceptId};
        return new PreparedStatementRenderer(source, sqlPath, tableQualifierNames, tableQualifierValues, names, values);
    }

    /**
     * Get the SQL for obtaining negative controls for the concept set specified
     *
     * @summary SQL for obtaining negative controls
     * @param source The source that contains the CEM daimon
     * @param conceptSetId The conceptSetId associated to the negative controls
     * @return A prepared SQL statement
     */
    protected PreparedStatementRenderer prepareGetNegativeControls(Source source, int conceptSetId) {
        String sqlPath = "/resources/evidence/sql/negativecontrols/getNegativeControls.sql";
        String cemResultsSchema = source.getTableQualifier(SourceDaimon.DaimonType.CEMResults);
        String[] tableQualifierNames = new String[]{"cem_results_schema"};
        String[] tableQualifierValues = new String[]{cemResultsSchema};
        String[] names = new String[]{"conceptSetId"};
        Object[] values = new Object[]{conceptSetId};
        return new PreparedStatementRenderer(source, sqlPath, tableQualifierNames, tableQualifierValues, names, values);
    }
    
    private static String JoinArray(final String[] array) {
        String result = "";

        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                result += ",";
            }

            result += "'" + array[i] + "'";
        }

        return result;
    }

    private static String limitJobParams(String param) {
        if (param.length() >= 250) {
            return param.substring(0, 245) + "...";
        }
        return param;
    }

    private Exception NotFoundException(String evidence_daimon_not_configured_for_source) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
