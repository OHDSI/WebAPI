-- WebAPI 3.0.0 Baseline Migration
-- This script creates the complete schema for a fresh WebAPI 3.0 installation.
-- For upgrades from WebAPI 2.14 or 2.15, other migration scripts will be applied instead.
--
-- This baseline includes all schema changes up through WebAPI 2.15.x

-- Flyway requires that schema already exists
-- CREATE SCHEMA IF NOT EXISTS ${ohdsiSchema};



--
-- Name: achilles_cache_seq; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.achilles_cache_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: achilles_cache; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.achilles_cache (
    id bigint DEFAULT nextval('${ohdsiSchema}.achilles_cache_seq'::regclass) NOT NULL,
    source_id integer NOT NULL,
    cache_name character varying NOT NULL,
    cache text
);


--
-- Name: analysis_execution_sequence; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.analysis_execution_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: analysis_generation_info; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.analysis_generation_info (
    job_execution_id integer NOT NULL,
    design character varying NOT NULL,
    hash_code character varying NOT NULL,
    created_by_id integer
);


--
-- Name: batch_job_execution; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.batch_job_execution (
    job_execution_id bigint NOT NULL,
    version bigint,
    job_instance_id bigint NOT NULL,
    create_time timestamp without time zone NOT NULL,
    start_time timestamp without time zone,
    end_time timestamp without time zone,
    status character varying(10),
    exit_code character varying(2500),
    exit_message character varying(2500),
    last_updated timestamp without time zone,
    job_configuration_location character varying(2500)
);


--
-- Name: batch_job_execution_context; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.batch_job_execution_context (
    job_execution_id bigint NOT NULL,
    short_context character varying(2500) NOT NULL,
    serialized_context text
);


--
-- Name: batch_job_execution_params; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.batch_job_execution_params (
    job_execution_id bigint NOT NULL,
    parameter_name character varying(100) NOT NULL,
    parameter_type character varying(100) NOT NULL,
    parameter_value character varying(2500),
    identifying character(1) NOT NULL
);


--
-- Name: batch_job_execution_seq; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.batch_job_execution_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: batch_job_instance; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.batch_job_instance (
    job_instance_id bigint NOT NULL,
    version bigint,
    job_name character varying(100) NOT NULL,
    job_key character varying(32) NOT NULL
);


--
-- Name: batch_job_seq; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.batch_job_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: batch_step_execution; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.batch_step_execution (
    step_execution_id bigint NOT NULL,
    version bigint NOT NULL,
    step_name character varying(100) NOT NULL,
    job_execution_id bigint NOT NULL,
    start_time timestamp without time zone,
    end_time timestamp without time zone,
    status character varying(10),
    commit_count bigint,
    read_count bigint,
    filter_count bigint,
    write_count bigint,
    read_skip_count bigint,
    write_skip_count bigint,
    process_skip_count bigint,
    rollback_count bigint,
    exit_code character varying(2500),
    exit_message character varying(2500),
    last_updated timestamp without time zone,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: batch_step_execution_context; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.batch_step_execution_context (
    step_execution_id bigint NOT NULL,
    short_context character varying(2500) NOT NULL,
    serialized_context text
);


--
-- Name: batch_step_execution_seq; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.batch_step_execution_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: cca; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.cca (
    cca_id integer NOT NULL,
    name character varying(255),
    treatment_id integer NOT NULL,
    comparator_id integer NOT NULL,
    outcome_id integer NOT NULL,
    model_type character varying(50) NOT NULL,
    time_at_risk_start integer NOT NULL,
    time_at_risk_end integer NOT NULL,
    add_exposure_days_to_end integer NOT NULL,
    minimum_washout_period integer NOT NULL,
    minimum_days_at_risk integer NOT NULL,
    rm_subjects_in_both_cohorts integer NOT NULL,
    rm_prior_outcomes integer NOT NULL,
    ps_adjustment integer NOT NULL,
    ps_exclusion_id integer NOT NULL,
    ps_inclusion_id integer NOT NULL,
    ps_demographics integer NOT NULL,
    ps_demographics_gender integer NOT NULL,
    ps_demographics_race integer NOT NULL,
    ps_demographics_ethnicity integer NOT NULL,
    ps_demographics_age integer NOT NULL,
    ps_demographics_year integer NOT NULL,
    ps_demographics_month integer NOT NULL,
    ps_trim integer NOT NULL,
    ps_trim_fraction double precision NOT NULL,
    ps_match integer NOT NULL,
    ps_match_max_ratio integer NOT NULL,
    ps_strat integer NOT NULL,
    ps_strat_num_strata integer NOT NULL,
    ps_condition_occ integer NOT NULL,
    ps_condition_occ_365d integer NOT NULL,
    ps_condition_occ_30d integer NOT NULL,
    ps_condition_occ_inpt180d integer NOT NULL,
    ps_condition_era integer NOT NULL,
    ps_condition_era_ever integer NOT NULL,
    ps_condition_era_overlap integer NOT NULL,
    ps_condition_group integer NOT NULL,
    ps_condition_group_meddra integer NOT NULL,
    ps_condition_group_snomed integer NOT NULL,
    ps_drug_exposure integer NOT NULL,
    ps_drug_exposure_365d integer NOT NULL,
    ps_drug_exposure_30d integer NOT NULL,
    ps_drug_era integer NOT NULL,
    ps_drug_era_365d integer NOT NULL,
    ps_drug_era_30d integer NOT NULL,
    ps_drug_era_overlap integer NOT NULL,
    ps_drug_era_ever integer NOT NULL,
    ps_drug_group integer NOT NULL,
    ps_procedure_occ integer NOT NULL,
    ps_procedure_occ_365d integer NOT NULL,
    ps_procedure_occ_30d integer NOT NULL,
    ps_procedure_group integer NOT NULL,
    ps_observation integer NOT NULL,
    ps_observation_365d integer NOT NULL,
    ps_observation_30d integer NOT NULL,
    ps_observation_count_365d integer NOT NULL,
    ps_measurement integer NOT NULL,
    ps_measurement_365d integer NOT NULL,
    ps_measurement_30d integer NOT NULL,
    ps_measurement_count_365d integer NOT NULL,
    ps_measurement_below integer NOT NULL,
    ps_measurement_above integer NOT NULL,
    ps_concept_counts integer NOT NULL,
    ps_risk_scores integer NOT NULL,
    ps_risk_scores_charlson integer NOT NULL,
    ps_risk_scores_dcsi integer NOT NULL,
    ps_risk_scores_chads2 integer NOT NULL,
    ps_risk_scores_chads2vasc integer NOT NULL,
    ps_interaction_year integer NOT NULL,
    ps_interaction_month integer NOT NULL,
    om_covariates integer NOT NULL,
    om_exclusion_id integer NOT NULL,
    om_inclusion_id integer NOT NULL,
    om_demographics integer NOT NULL,
    om_demographics_gender integer NOT NULL,
    om_demographics_race integer NOT NULL,
    om_demographics_ethnicity integer NOT NULL,
    om_demographics_age integer NOT NULL,
    om_demographics_year integer NOT NULL,
    om_demographics_month integer NOT NULL,
    om_trim integer NOT NULL,
    om_trim_fraction double precision NOT NULL,
    om_match integer NOT NULL,
    om_match_max_ratio integer NOT NULL,
    om_strat integer NOT NULL,
    om_strat_num_strata integer NOT NULL,
    om_condition_occ integer NOT NULL,
    om_condition_occ_365d integer NOT NULL,
    om_condition_occ_30d integer NOT NULL,
    om_condition_occ_inpt180d integer NOT NULL,
    om_condition_era integer NOT NULL,
    om_condition_era_ever integer NOT NULL,
    om_condition_era_overlap integer NOT NULL,
    om_condition_group integer NOT NULL,
    om_condition_group_meddra integer NOT NULL,
    om_condition_group_snomed integer NOT NULL,
    om_drug_exposure integer NOT NULL,
    om_drug_exposure_365d integer NOT NULL,
    om_drug_exposure_30d integer NOT NULL,
    om_drug_era integer NOT NULL,
    om_drug_era_365d integer NOT NULL,
    om_drug_era_30d integer NOT NULL,
    om_drug_era_overlap integer NOT NULL,
    om_drug_era_ever integer NOT NULL,
    om_drug_group integer NOT NULL,
    om_procedure_occ integer NOT NULL,
    om_procedure_occ_365d integer NOT NULL,
    om_procedure_occ_30d integer NOT NULL,
    om_procedure_group integer NOT NULL,
    om_observation integer NOT NULL,
    om_observation_365d integer NOT NULL,
    om_observation_30d integer NOT NULL,
    om_observation_count_365d integer NOT NULL,
    om_measurement integer NOT NULL,
    om_measurement_365d integer NOT NULL,
    om_measurement_30d integer NOT NULL,
    om_measurement_count_365d integer NOT NULL,
    om_measurement_below integer NOT NULL,
    om_measurement_above integer NOT NULL,
    om_concept_counts integer NOT NULL,
    om_risk_scores integer NOT NULL,
    om_risk_scores_charlson integer NOT NULL,
    om_risk_scores_dcsi integer NOT NULL,
    om_risk_scores_chads2 integer NOT NULL,
    om_risk_scores_chads2vasc integer NOT NULL,
    om_interaction_year integer NOT NULL,
    om_interaction_month integer NOT NULL,
    del_covariates_small_count integer NOT NULL,
    negative_control_id integer NOT NULL,
    created timestamp(3) without time zone NOT NULL,
    modified timestamp(3) without time zone NOT NULL,
    sec_user_id integer NOT NULL,
    created_by_id integer,
    modified_by_id integer
);


--
-- Name: cca_execution_sequence; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.cca_execution_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: cca_execution; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.cca_execution (
    cca_execution_id integer DEFAULT nextval('${ohdsiSchema}.cca_execution_sequence'::regclass) NOT NULL,
    cca_id integer NOT NULL,
    source_id integer NOT NULL,
    treatment_id integer NOT NULL,
    comparator_id integer NOT NULL,
    outcome_id integer NOT NULL,
    model_type character varying(50) NOT NULL,
    time_at_risk_start integer NOT NULL,
    time_at_risk_end integer NOT NULL,
    add_exposure_days_to_end integer NOT NULL,
    minimum_washout_period integer NOT NULL,
    minimum_days_at_risk integer NOT NULL,
    rm_subjects_in_both_cohorts integer NOT NULL,
    rm_prior_outcomes integer NOT NULL,
    ps_adjustment integer NOT NULL,
    ps_exclusion_id integer NOT NULL,
    ps_inclusion_id integer NOT NULL,
    ps_demographics integer NOT NULL,
    ps_demographics_gender integer NOT NULL,
    ps_demographics_race integer NOT NULL,
    ps_demographics_ethnicity integer NOT NULL,
    ps_demographics_age integer NOT NULL,
    ps_demographics_year integer NOT NULL,
    ps_demographics_month integer NOT NULL,
    ps_trim integer NOT NULL,
    ps_trim_fraction double precision NOT NULL,
    ps_match integer NOT NULL,
    ps_match_max_ratio integer NOT NULL,
    ps_strat integer NOT NULL,
    ps_strat_num_strata integer NOT NULL,
    ps_condition_occ integer NOT NULL,
    ps_condition_occ_365d integer NOT NULL,
    ps_condition_occ_30d integer NOT NULL,
    ps_condition_occ_inpt180d integer NOT NULL,
    ps_condition_era integer NOT NULL,
    ps_condition_era_ever integer NOT NULL,
    ps_condition_era_overlap integer NOT NULL,
    ps_condition_group integer NOT NULL,
    ps_condition_group_meddra integer NOT NULL,
    ps_condition_group_snomed integer NOT NULL,
    ps_drug_exposure integer NOT NULL,
    ps_drug_exposure_365d integer NOT NULL,
    ps_drug_exposure_30d integer NOT NULL,
    ps_drug_era integer NOT NULL,
    ps_drug_era_365d integer NOT NULL,
    ps_drug_era_30d integer NOT NULL,
    ps_drug_era_overlap integer NOT NULL,
    ps_drug_era_ever integer NOT NULL,
    ps_drug_group integer NOT NULL,
    ps_procedure_occ integer NOT NULL,
    ps_procedure_occ_365d integer NOT NULL,
    ps_procedure_occ_30d integer NOT NULL,
    ps_procedure_group integer NOT NULL,
    ps_observation integer NOT NULL,
    ps_observation_365d integer NOT NULL,
    ps_observation_30d integer NOT NULL,
    ps_observation_count_365d integer NOT NULL,
    ps_measurement integer NOT NULL,
    ps_measurement_365d integer NOT NULL,
    ps_measurement_30d integer NOT NULL,
    ps_measurement_count_365d integer NOT NULL,
    ps_measurement_below integer NOT NULL,
    ps_measurement_above integer NOT NULL,
    ps_concept_counts integer NOT NULL,
    ps_risk_scores integer NOT NULL,
    ps_risk_scores_charlson integer NOT NULL,
    ps_risk_scores_dcsi integer NOT NULL,
    ps_risk_scores_chads2 integer NOT NULL,
    ps_risk_scores_chads2vasc integer NOT NULL,
    ps_interaction_year integer NOT NULL,
    ps_interaction_month integer NOT NULL,
    om_covariates integer NOT NULL,
    om_exclusion_id integer NOT NULL,
    om_inclusion_id integer NOT NULL,
    om_demographics integer NOT NULL,
    om_demographics_gender integer NOT NULL,
    om_demographics_race integer NOT NULL,
    om_demographics_ethnicity integer NOT NULL,
    om_demographics_age integer NOT NULL,
    om_demographics_year integer NOT NULL,
    om_demographics_month integer NOT NULL,
    om_trim integer NOT NULL,
    om_trim_fraction double precision NOT NULL,
    om_match integer NOT NULL,
    om_match_max_ratio integer NOT NULL,
    om_strat integer NOT NULL,
    om_strat_num_strata integer NOT NULL,
    om_condition_occ integer NOT NULL,
    om_condition_occ_365d integer NOT NULL,
    om_condition_occ_30d integer NOT NULL,
    om_condition_occ_inpt180d integer NOT NULL,
    om_condition_era integer NOT NULL,
    om_condition_era_ever integer NOT NULL,
    om_condition_era_overlap integer NOT NULL,
    om_condition_group integer NOT NULL,
    om_condition_group_meddra integer NOT NULL,
    om_condition_group_snomed integer NOT NULL,
    om_drug_exposure integer NOT NULL,
    om_drug_exposure_365d integer NOT NULL,
    om_drug_exposure_30d integer NOT NULL,
    om_drug_era integer NOT NULL,
    om_drug_era_365d integer NOT NULL,
    om_drug_era_30d integer NOT NULL,
    om_drug_era_overlap integer NOT NULL,
    om_drug_era_ever integer NOT NULL,
    om_drug_group integer NOT NULL,
    om_procedure_occ integer NOT NULL,
    om_procedure_occ_365d integer NOT NULL,
    om_procedure_occ_30d integer NOT NULL,
    om_procedure_group integer NOT NULL,
    om_observation integer NOT NULL,
    om_observation_365d integer NOT NULL,
    om_observation_30d integer NOT NULL,
    om_observation_count_365d integer NOT NULL,
    om_measurement integer NOT NULL,
    om_measurement_365d integer NOT NULL,
    om_measurement_30d integer NOT NULL,
    om_measurement_count_365d integer NOT NULL,
    om_measurement_below integer NOT NULL,
    om_measurement_above integer NOT NULL,
    om_concept_counts integer NOT NULL,
    om_risk_scores integer NOT NULL,
    om_risk_scores_charlson integer NOT NULL,
    om_risk_scores_dcsi integer NOT NULL,
    om_risk_scores_chads2 integer NOT NULL,
    om_risk_scores_chads2vasc integer NOT NULL,
    om_interaction_year integer NOT NULL,
    om_interaction_month integer NOT NULL,
    del_covariates_small_count integer NOT NULL,
    negative_control_id integer NOT NULL,
    executed timestamp(3) without time zone NOT NULL,
    execution_duration integer NOT NULL,
    execution_status integer NOT NULL,
    sec_user_id integer NOT NULL
);


--
-- Name: cca_execution_ext; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.cca_execution_ext (
    cca_execution_id integer NOT NULL,
    update_password character varying
);


--
-- Name: cca_sequence; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.cca_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: cdm_cache_seq; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.cdm_cache_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: cdm_cache; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.cdm_cache (
    id bigint DEFAULT nextval('${ohdsiSchema}.cdm_cache_seq'::regclass) NOT NULL,
    concept_id integer NOT NULL,
    source_id integer NOT NULL,
    record_count bigint,
    descendant_record_count bigint,
    person_count bigint,
    descendant_person_count bigint
);


--
-- Name: cohort; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.cohort (
    cohort_definition_id integer NOT NULL,
    subject_id bigint NOT NULL,
    cohort_start_date date NOT NULL,
    cohort_end_date date NOT NULL
);


--
-- Name: cohort_analysis_gen_info; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.cohort_analysis_gen_info (
    source_id integer NOT NULL,
    cohort_id integer NOT NULL,
    last_execution timestamp(3) without time zone,
    execution_duration integer,
    fail_message character varying(2000),
    progress integer DEFAULT 0
);


--
-- Name: cohort_analysis_list_xref; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.cohort_analysis_list_xref (
    source_id integer NOT NULL,
    cohort_id integer NOT NULL,
    analysis_id integer NOT NULL
);


--
-- Name: cohort_concept_map; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.cohort_concept_map (
    cohort_definition_id integer NOT NULL,
    cohort_definition_name character varying(255),
    concept_id integer
);


--
-- Name: cohort_definition; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.cohort_definition (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    description character varying(1000),
    expression_type character varying(50),
    created_date timestamp(3) without time zone,
    modified_date timestamp(3) without time zone,
    created_by_id integer,
    modified_by_id integer
);


--
-- Name: cohort_definition_details; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.cohort_definition_details (
    id integer NOT NULL,
    expression text NOT NULL,
    hash_code integer
);


--
-- Name: cohort_definition_sequence; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.cohort_definition_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: cohort_generation_info; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.cohort_generation_info (
    id integer NOT NULL,
    source_id integer NOT NULL,
    start_time timestamp(3) without time zone,
    execution_duration integer,
    status integer NOT NULL,
    is_valid boolean NOT NULL,
    fail_message character varying(2000),
    person_count bigint,
    record_count bigint,
    is_canceled boolean DEFAULT false NOT NULL,
    created_by_id integer,
    is_demographic boolean DEFAULT false NOT NULL,
    cc_generate_id integer
);


--
-- Name: cohort_inclusion; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.cohort_inclusion (
    cohort_definition_id integer NOT NULL,
    rule_sequence integer NOT NULL,
    name character varying(255),
    description character varying(1000)
);


--
-- Name: cohort_inclusion_result; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.cohort_inclusion_result (
    cohort_definition_id integer NOT NULL,
    inclusion_rule_mask bigint NOT NULL,
    person_count bigint NOT NULL
);


--
-- Name: cohort_inclusion_stats; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.cohort_inclusion_stats (
    cohort_definition_id integer NOT NULL,
    rule_sequence integer NOT NULL,
    person_count bigint NOT NULL,
    gain_count bigint NOT NULL,
    person_total bigint NOT NULL
);


--
-- Name: cohort_sample; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.cohort_sample (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    cohort_definition_id integer NOT NULL,
    source_id integer NOT NULL,
    size integer NOT NULL,
    age_min smallint,
    age_max smallint,
    age_mode character varying(24),
    gender_concept_ids character varying(255),
    created_by_id integer,
    created_date timestamp with time zone DEFAULT now() NOT NULL,
    modified_by_id integer,
    modified_date timestamp with time zone
);


--
-- Name: cohort_sample_sequence; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.cohort_sample_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: cohort_study; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.cohort_study (
    cohort_study_id integer NOT NULL,
    cohort_definition_id integer,
    study_type integer,
    study_name character varying(1000),
    study_url character varying(1000)
);


--
-- Name: cohort_study_cohort_study_id_seq; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.cohort_study_cohort_study_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: cohort_study_cohort_study_id_seq; Type: SEQUENCE OWNED BY; Schema: webapi; Owner: -
--

ALTER SEQUENCE ${ohdsiSchema}.cohort_study_cohort_study_id_seq OWNED BY ${ohdsiSchema}.cohort_study.cohort_study_id;


--
-- Name: cohort_summary_stats; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.cohort_summary_stats (
    cohort_definition_id integer NOT NULL,
    base_count bigint NOT NULL,
    final_count bigint NOT NULL
);


--
-- Name: cohort_tag; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.cohort_tag (
    asset_id integer NOT NULL,
    tag_id integer NOT NULL
);


--
-- Name: cohort_version; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.cohort_version (
    asset_id bigint NOT NULL,
    comment character varying,
    description character varying,
    version integer DEFAULT 1 NOT NULL,
    asset_json character varying NOT NULL,
    archived boolean DEFAULT false NOT NULL,
    created_by_id integer,
    created_date timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: concept_of_interest; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.concept_of_interest (
    id integer NOT NULL,
    concept_id integer,
    concept_of_interest_id integer
);


--
-- Name: concept_of_interest_id_seq; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.concept_of_interest_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: concept_of_interest_id_seq; Type: SEQUENCE OWNED BY; Schema: webapi; Owner: -
--

ALTER SEQUENCE ${ohdsiSchema}.concept_of_interest_id_seq OWNED BY ${ohdsiSchema}.concept_of_interest.id;


--
-- Name: concept_set_sequence; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.concept_set_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: concept_set; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.concept_set (
    concept_set_id integer DEFAULT nextval('${ohdsiSchema}.concept_set_sequence'::regclass) NOT NULL,
    concept_set_name character varying(255) NOT NULL,
    created_date timestamp with time zone,
    modified_date timestamp with time zone,
    created_by_id integer,
    modified_by_id integer,
    description character varying(1000)
);


--
-- Name: concept_set_annotation_sequence; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.concept_set_annotation_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: concept_set_annotation; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.concept_set_annotation (
    concept_set_annotation_id integer DEFAULT nextval('${ohdsiSchema}.concept_set_annotation_sequence'::regclass) NOT NULL,
    concept_set_id integer NOT NULL,
    concept_id integer,
    annotation_details character varying,
    vocabulary_version character varying,
    created_by_id integer,
    created_date timestamp with time zone DEFAULT now() NOT NULL,
    modified_by_id integer,
    modified_date timestamp with time zone,
    concept_set_version integer,
    copied_from_concept_set_ids character varying(1000)
);


--
-- Name: concept_set_generation_info; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.concept_set_generation_info (
    concept_set_id integer NOT NULL,
    source_id integer NOT NULL,
    generation_type integer NOT NULL,
    start_time timestamp without time zone NOT NULL,
    execution_duration integer,
    status integer NOT NULL,
    is_valid boolean NOT NULL,
    params text NOT NULL,
    is_canceled boolean DEFAULT false NOT NULL
);


--
-- Name: concept_set_item_sequence; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.concept_set_item_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: concept_set_item; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.concept_set_item (
    concept_set_item_id integer DEFAULT nextval('${ohdsiSchema}.concept_set_item_sequence'::regclass) NOT NULL,
    concept_set_id integer NOT NULL,
    concept_id integer NOT NULL,
    is_excluded integer NOT NULL,
    include_descendants integer NOT NULL,
    include_mapped integer NOT NULL
);


--
-- Name: negative_controls_sequence; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.negative_controls_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: concept_set_negative_controls; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.concept_set_negative_controls (
    id integer DEFAULT nextval('${ohdsiSchema}.negative_controls_sequence'::regclass) NOT NULL,
    evidence_job_id bigint NOT NULL,
    source_id integer NOT NULL,
    concept_set_id integer NOT NULL
);


--
-- Name: concept_set_tag; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.concept_set_tag (
    asset_id integer NOT NULL,
    tag_id integer NOT NULL
);


--
-- Name: concept_set_version; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.concept_set_version (
    asset_id bigint NOT NULL,
    comment character varying,
    version integer DEFAULT 1 NOT NULL,
    asset_json character varying NOT NULL,
    archived boolean DEFAULT false NOT NULL,
    created_by_id integer,
    created_date timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: drug_hoi_evidence_sequence; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.drug_hoi_evidence_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: drug_hoi_evidence; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.drug_hoi_evidence (
    id integer DEFAULT nextval('${ohdsiSchema}.drug_hoi_evidence_sequence'::regclass) NOT NULL,
    drug_hoi_relationship character varying(50),
    evidence_type character varying(4000),
    supports character varying(1),
    evidence_source_code_id integer,
    statistic_value numeric,
    evidence_linkout character varying(4000),
    statistic_type character varying(4000)
);


--
-- Name: COLUMN drug_hoi_evidence.id; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.drug_hoi_evidence.id IS 'primary key';


--
-- Name: COLUMN drug_hoi_evidence.drug_hoi_relationship; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.drug_hoi_evidence.drug_hoi_relationship IS 'foreign key to the drug_HOI_relationship id';


--
-- Name: COLUMN drug_hoi_evidence.evidence_type; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.drug_hoi_evidence.evidence_type IS 'the type of evidence (literature, product label, pharmacovigilance, EHR)';


--
-- Name: COLUMN drug_hoi_evidence.supports; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.drug_hoi_evidence.supports IS 'Whether or not the relationship of evidence is to refute the assertion';


--
-- Name: COLUMN drug_hoi_evidence.evidence_source_code_id; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.drug_hoi_evidence.evidence_source_code_id IS 'a code indicating the actual source of evidence (e.g., PubMed, US SPLs, EU SPC, VigiBase, etc)';


--
-- Name: COLUMN drug_hoi_evidence.statistic_value; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.drug_hoi_evidence.statistic_value IS 'For literature-like (e.g., PubMed abstracts, product labeling) sources this holds the count of the number of items of the evidence type present in the evidence base from that source (several rules are used to derive the counts, see documentation on the knowledge-base wiki). From signal detection sources, the result of applying the algorithm indicated in the evidence_type column is shown.';


--
-- Name: COLUMN drug_hoi_evidence.evidence_linkout; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.drug_hoi_evidence.evidence_linkout IS 'For literature-like (e.g., PubMed abstracts, product labeling), this holds a URL that will resolve to a query against the RDF endpoint for all resources used to generate the evidence_count. For signal detection sources, this holds a link to metadata on the algorithm and how it was applied to arrive at the statistical value.';


--
-- Name: COLUMN drug_hoi_evidence.statistic_type; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.drug_hoi_evidence.statistic_type IS 'For literature-like (e.g., PubMed abstracts, product labeling), and other count based methods this holds COUNT. For signal detection sources, this holds a string indicating the type of the result value (e.g., AERS_EBGM, AERS_EB05)';


--
-- Name: drug_hoi_relationship; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.drug_hoi_relationship (
    id character varying(50) NOT NULL,
    drug integer,
    rxnorm_drug character varying(4000),
    hoi integer,
    snomed_hoi character varying(4000)
);


--
-- Name: COLUMN drug_hoi_relationship.drug; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.drug_hoi_relationship.drug IS 'OMOP/IMEDS Concept ID for the drug';


--
-- Name: COLUMN drug_hoi_relationship.rxnorm_drug; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.drug_hoi_relationship.rxnorm_drug IS 'RxNorm Preferred Term of the Drug';


--
-- Name: COLUMN drug_hoi_relationship.hoi; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.drug_hoi_relationship.hoi IS 'OMOP/IMEDS Concept ID for the Health Outcome of Interest';


--
-- Name: COLUMN drug_hoi_relationship.snomed_hoi; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.drug_hoi_relationship.snomed_hoi IS 'SNOMED preferred term of the Health Outcome of Interest';


--
-- Name: drug_labels; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.drug_labels (
    drug_label_id bigint NOT NULL,
    search_name character varying(255),
    ingredient_concept_id bigint,
    ingredient_concept_name character varying(255),
    setid character varying(255),
    date timestamp(3) without time zone,
    cohort_id integer,
    image_url character varying(255)
);


--
-- Name: ee_analysis_status; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.ee_analysis_status (
    id integer DEFAULT nextval('${ohdsiSchema}.analysis_execution_sequence'::regclass) NOT NULL,
    executionstatus character varying,
    job_execution_id bigint
);


--
-- Name: evidence_sources_sequence; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.evidence_sources_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: evidence_sources; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.evidence_sources (
    id integer DEFAULT nextval('${ohdsiSchema}.evidence_sources_sequence'::regclass) NOT NULL,
    title character varying(4000),
    description character varying(4000),
    contributer character varying(4000),
    creator character varying(4000),
    creation_date date NOT NULL,
    rights character varying(4000),
    source character varying(4000),
    coverage_start_date date,
    coverage_end_date date
);


--
-- Name: COLUMN evidence_sources.title; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.evidence_sources.title IS 'a short name for the evidence source. Same as http://purl.org/dc/elements/1.1/title';


--
-- Name: COLUMN evidence_sources.description; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.evidence_sources.description IS 'Description of the evidence source. Same as http://purl.org/dc/elements/1.1/description';


--
-- Name: COLUMN evidence_sources.contributer; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.evidence_sources.contributer IS 'Same as http://purl.org/dc/elements/1.1/contributor';


--
-- Name: COLUMN evidence_sources.creator; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.evidence_sources.creator IS 'Same as http://purl.org/dc/elements/1.1/creator';


--
-- Name: COLUMN evidence_sources.creation_date; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.evidence_sources.creation_date IS 'Date that the source was created. For example, if the source was created in 2010 but added to the knowledge base in 2014, the creation date would be 2010';


--
-- Name: COLUMN evidence_sources.rights; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.evidence_sources.rights IS 'Same as http://purl.org/dc/elements/1.1/rights';


--
-- Name: COLUMN evidence_sources.source; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.evidence_sources.source IS 'The source from which this data was derived. Same as http://purl.org/dc/elements/1.1/source';


--
-- Name: COLUMN evidence_sources.coverage_start_date; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.evidence_sources.coverage_start_date IS 'The start date of coverage for the resource. Data can be trusted on or after this date and up to and including the coverage_end_date';


--
-- Name: COLUMN evidence_sources.coverage_end_date; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.evidence_sources.coverage_end_date IS 'The date of coverage for the resource. Data can be trusted on or after the coverage_start_date date and up to and including this date';


--
-- Name: exampleapp_widget; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.exampleapp_widget (
    id bigint NOT NULL,
    name character varying(50)
);


--
-- Name: fe_aggregate_sequence; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.fe_aggregate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: fe_conceptset_sequence; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.fe_conceptset_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: fe_analysis_conceptset; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.fe_analysis_conceptset (
    id bigint DEFAULT nextval('${ohdsiSchema}.fe_conceptset_sequence'::regclass) NOT NULL,
    fe_analysis_id integer NOT NULL,
    expression character varying
);


--
-- Name: feas_study_generation_info; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.feas_study_generation_info (
    study_id integer NOT NULL,
    source_id integer NOT NULL,
    start_time timestamp(3) without time zone,
    execution_duration integer,
    status integer NOT NULL,
    is_valid boolean NOT NULL,
    is_canceled boolean DEFAULT false NOT NULL
);


--
-- Name: feas_study_inclusion_stats; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.feas_study_inclusion_stats (
    study_id integer NOT NULL,
    rule_sequence integer NOT NULL,
    name character varying(255) NOT NULL,
    person_count bigint NOT NULL,
    gain_count bigint NOT NULL,
    person_total bigint NOT NULL
);


--
-- Name: feas_study_index_stats; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.feas_study_index_stats (
    study_id integer NOT NULL,
    person_count bigint NOT NULL,
    match_count bigint NOT NULL
);


--
-- Name: feas_study_result; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.feas_study_result (
    study_id integer NOT NULL,
    inclusion_rule_mask bigint NOT NULL,
    person_count bigint NOT NULL
);


--
-- Name: feasibility_inclusion; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.feasibility_inclusion (
    study_id integer NOT NULL,
    sequence integer NOT NULL,
    name character varying(255),
    description character varying(1000),
    expression text
);


--
-- Name: feasibility_study; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.feasibility_study (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    description character varying(1000),
    index_def_id integer,
    result_def_id integer,
    created_date timestamp(3) without time zone,
    modified_date timestamp(3) without time zone,
    created_by_id integer,
    modified_by_id integer
);


--
-- Name: feasibility_study_sequence; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.feasibility_study_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: generation_cache_sequence; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.generation_cache_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: generation_cache; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.generation_cache (
    id integer DEFAULT nextval('${ohdsiSchema}.generation_cache_sequence'::regclass) NOT NULL,
    type character varying NOT NULL,
    design_hash integer NOT NULL,
    source_id integer NOT NULL,
    result_checksum character varying,
    created_date date DEFAULT now() NOT NULL
);


--
-- Name: heracles_analysis; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.heracles_analysis (
    analysis_id integer NOT NULL,
    analysis_name character varying(255),
    stratum_1_name character varying(255),
    stratum_2_name character varying(255),
    stratum_3_name character varying(255),
    stratum_4_name character varying(255),
    stratum_5_name character varying(255),
    analysis_type character varying(255)
);


--
-- Name: heracles_heel_results; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.heracles_heel_results (
    cohort_definition_id integer,
    analysis_id integer,
    heracles_heel_warning character varying(255),
    id integer NOT NULL
);


--
-- Name: heracles_heel_results_id_seq; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.heracles_heel_results_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: heracles_heel_results_id_seq; Type: SEQUENCE OWNED BY; Schema: webapi; Owner: -
--

ALTER SEQUENCE ${ohdsiSchema}.heracles_heel_results_id_seq OWNED BY ${ohdsiSchema}.heracles_heel_results.id;


--
-- Name: heracles_results; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.heracles_results (
    cohort_definition_id integer,
    analysis_id integer,
    stratum_1 character varying(255),
    stratum_2 character varying(255),
    stratum_3 character varying(255),
    stratum_4 character varying(255),
    stratum_5 character varying(255),
    count_value bigint,
    last_update_time timestamp without time zone DEFAULT now(),
    id integer NOT NULL
);


--
-- Name: heracles_results_dist; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.heracles_results_dist (
    cohort_definition_id integer,
    analysis_id integer,
    stratum_1 character varying(255),
    stratum_2 character varying(255),
    stratum_3 character varying(255),
    stratum_4 character varying(255),
    stratum_5 character varying(255),
    count_value bigint,
    min_value double precision,
    max_value double precision,
    avg_value double precision,
    stdev_value double precision,
    median_value double precision,
    p10_value double precision,
    p25_value double precision,
    p75_value double precision,
    p90_value double precision,
    last_update_time timestamp without time zone DEFAULT now(),
    id integer NOT NULL
);


--
-- Name: heracles_results_dist_id_seq; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.heracles_results_dist_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: heracles_results_dist_id_seq; Type: SEQUENCE OWNED BY; Schema: webapi; Owner: -
--

ALTER SEQUENCE ${ohdsiSchema}.heracles_results_dist_id_seq OWNED BY ${ohdsiSchema}.heracles_results_dist.id;


--
-- Name: heracles_results_id_seq; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.heracles_results_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: heracles_results_id_seq; Type: SEQUENCE OWNED BY; Schema: webapi; Owner: -
--

ALTER SEQUENCE ${ohdsiSchema}.heracles_results_id_seq OWNED BY ${ohdsiSchema}.heracles_results.id;


--
-- Name: heracles_vis_data_sequence; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.heracles_vis_data_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: heracles_visualization_data; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.heracles_visualization_data (
    id integer DEFAULT nextval('${ohdsiSchema}.heracles_vis_data_sequence'::regclass) NOT NULL,
    cohort_definition_id integer NOT NULL,
    source_id integer NOT NULL,
    visualization_key character varying(300) NOT NULL,
    drilldown_id integer,
    data text NOT NULL,
    end_time timestamp(3) without time zone NOT NULL
);


--
-- Name: heracles_viz_data_sequence; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.heracles_viz_data_sequence
    START WITH 0
    INCREMENT BY 1
    MINVALUE 0
    NO MAXVALUE
    CACHE 1;


--
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ir_analysis_dist; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.ir_analysis_dist (
    analysis_id integer NOT NULL,
    target_id integer NOT NULL,
    outcome_id integer NOT NULL,
    strata_sequence integer,
    dist_type integer NOT NULL,
    total bigint NOT NULL,
    avg_value double precision NOT NULL,
    std_dev double precision NOT NULL,
    min_value integer NOT NULL,
    p10_value integer NOT NULL,
    p25_value integer NOT NULL,
    median_value integer NOT NULL,
    p75_value integer NOT NULL,
    p90_value integer NOT NULL,
    max_value integer,
    id integer NOT NULL
);


--
-- Name: ir_analysis_dist_id_seq; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.ir_analysis_dist_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ir_analysis_dist_id_seq; Type: SEQUENCE OWNED BY; Schema: webapi; Owner: -
--

ALTER SEQUENCE ${ohdsiSchema}.ir_analysis_dist_id_seq OWNED BY ${ohdsiSchema}.ir_analysis_dist.id;


--
-- Name: ir_version; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.ir_version (
    asset_id bigint NOT NULL,
    comment character varying,
    description character varying,
    version integer DEFAULT 1 NOT NULL,
    asset_json character varying NOT NULL,
    archived boolean DEFAULT false NOT NULL,
    created_by_id integer,
    created_date timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: laertes_summary_sequence; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.laertes_summary_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: laertes_summary; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.laertes_summary (
    id integer DEFAULT nextval('${ohdsiSchema}.laertes_summary_sequence'::regclass) NOT NULL,
    report_order integer,
    report_name character varying(4000),
    ingredient_id integer,
    ingredient character varying(4000),
    clinical_drug_id integer,
    clinical_drug character varying(4000),
    hoi_id integer,
    hoi character varying(4000),
    medline_ct_count integer,
    medline_case_count integer,
    medline_other_count integer,
    splicer_count integer,
    eu_spc_count integer,
    semmeddb_ct_count integer,
    semmeddb_case_count integer,
    semmeddb_neg_ct_count integer,
    semmeddb_neg_case_count integer,
    aers_report_count integer,
    prr numeric,
    ctd_chemical_disease_count integer,
    semmeddb_other_count integer,
    semmeddb_neg_other_count integer
);


--
-- Name: COLUMN laertes_summary.id; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.laertes_summary.id IS 'primary key';


--
-- Name: COLUMN laertes_summary.report_order; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.laertes_summary.report_order IS 'there are several reports in this summary, this is an identifier for each report';


--
-- Name: COLUMN laertes_summary.report_name; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.laertes_summary.report_name IS 'there are several reports in this summary, this is a name of the report';


--
-- Name: COLUMN laertes_summary.ingredient_id; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.laertes_summary.ingredient_id IS 'a drug ingredient CONCEPT_ID';


--
-- Name: COLUMN laertes_summary.ingredient; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.laertes_summary.ingredient IS 'a drug ingredient name';


--
-- Name: COLUMN laertes_summary.clinical_drug_id; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.laertes_summary.clinical_drug_id IS 'if a clinical drug exists, the clinical drug CONCEPT_ID';


--
-- Name: COLUMN laertes_summary.clinical_drug; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.laertes_summary.clinical_drug IS 'if a clinical drug exists, the clinical drug name';


--
-- Name: COLUMN laertes_summary.hoi_id; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.laertes_summary.hoi_id IS 'the HOI CONCEPT_ID, this is at the SNOMED level';


--
-- Name: COLUMN laertes_summary.hoi; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.laertes_summary.hoi IS 'the HOI name, this is at the SNOMED level';


--
-- Name: COLUMN laertes_summary.splicer_count; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.laertes_summary.splicer_count IS 'counts of SPLs that mention specific drugs and hois';


--
-- Name: COLUMN laertes_summary.eu_spc_count; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.laertes_summary.eu_spc_count IS 'counts of SPCs that mention specific drugs and hois';


--
-- Name: output_file_contents; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.output_file_contents (
    output_file_id integer NOT NULL,
    file_contents bytea
);


--
-- Name: pathway_version; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.pathway_version (
    asset_id bigint NOT NULL,
    comment character varying,
    version integer DEFAULT 1 NOT NULL,
    asset_json character varying NOT NULL,
    archived boolean DEFAULT false NOT NULL,
    created_by_id integer,
    created_date timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: penelope_laertes_uni_pivot; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.penelope_laertes_uni_pivot (
    condition_concept_id integer,
    condition_concept_name character varying(255),
    ingredient_concept_id integer,
    ingredient_concept_name character varying(255),
    medline_ct integer,
    medline_case integer,
    medline_other integer,
    semmeddb_ct_t integer,
    semmeddb_case_t integer,
    semmeddb_other_t integer,
    semmeddb_ct_f integer,
    semmeddb_case_f integer,
    semmeddb_other_f integer,
    eu_spc integer,
    spl_adr integer,
    aers integer,
    aers_prr numeric,
    aers_prr_original numeric,
    id integer NOT NULL
);


--
-- Name: penelope_laertes_uni_pivot_id_seq; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.penelope_laertes_uni_pivot_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: penelope_laertes_uni_pivot_id_seq; Type: SEQUENCE OWNED BY; Schema: webapi; Owner: -
--

ALTER SEQUENCE ${ohdsiSchema}.penelope_laertes_uni_pivot_id_seq OWNED BY ${ohdsiSchema}.penelope_laertes_uni_pivot.id;


--
-- Name: penelope_laertes_universe; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.penelope_laertes_universe (
    id bigint NOT NULL,
    condition_concept_id integer,
    condition_concept_name character varying(255),
    ingredient_concept_id integer,
    ingredient_concept_name character varying(255),
    evidence_type character varying(255),
    supports character(1),
    statistic_value numeric,
    evidence_linkouts text
);


--
-- Name: plp; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.plp (
    plp_id integer NOT NULL,
    name character varying(255),
    treatment_id integer NOT NULL,
    outcome_id integer NOT NULL,
    model_type character varying(255) NOT NULL,
    time_at_risk_start integer NOT NULL,
    time_at_risk_end integer NOT NULL,
    add_exposure_days_to_end integer NOT NULL,
    minimum_washout_period integer NOT NULL,
    minimum_days_at_risk integer NOT NULL,
    require_time_at_risk integer NOT NULL,
    minimum_time_at_risk integer NOT NULL,
    sample integer NOT NULL,
    sample_size integer NOT NULL,
    first_exposure_only integer NOT NULL,
    include_all_outcomes integer NOT NULL,
    rm_prior_outcomes integer NOT NULL,
    prior_outcome_lookback integer NOT NULL,
    test_split integer NOT NULL,
    test_fraction character varying(255),
    n_fold character varying(255),
    mo_alpha character varying(255),
    mo_class_weight character varying(255),
    mo_index_folder character varying(255),
    mo_k character varying(255),
    mo_learn_rate character varying(255),
    mo_learning_rate character varying(255),
    mo_max_depth character varying(255),
    mo_min_impurity_split character varying(255),
    mo_min_rows character varying(255),
    mo_min_samples_leaf character varying(255),
    mo_min_samples_split character varying(255),
    mo_mtries character varying(255),
    mo_nestimators character varying(255),
    mo_nthread character varying(255),
    mo_ntrees character varying(255),
    mo_plot character varying(255),
    mo_seed character varying(255),
    mo_size character varying(255),
    mo_variance character varying(255),
    mo_var_imp character varying(255),
    cv_exclusion_id integer NOT NULL,
    cv_inclusion_id integer NOT NULL,
    cv_demographics integer NOT NULL,
    cv_demographics_gender integer NOT NULL,
    cv_demographics_race integer NOT NULL,
    cv_demographics_ethnicity integer NOT NULL,
    cv_demographics_age integer NOT NULL,
    cv_demographics_year integer NOT NULL,
    cv_demographics_month integer NOT NULL,
    cv_condition_occ integer NOT NULL,
    cv_condition_occ_365d integer NOT NULL,
    cv_condition_occ_30d integer NOT NULL,
    cv_condition_occ_inpt180d integer NOT NULL,
    cv_condition_era integer NOT NULL,
    cv_condition_era_ever integer NOT NULL,
    cv_condition_era_overlap integer NOT NULL,
    cv_condition_group integer NOT NULL,
    cv_condition_group_meddra integer NOT NULL,
    cv_condition_group_snomed integer NOT NULL,
    cv_drug_exposure integer NOT NULL,
    cv_drug_exposure_365d integer NOT NULL,
    cv_drug_exposure_30d integer NOT NULL,
    cv_drug_era integer NOT NULL,
    cv_drug_era_365d integer NOT NULL,
    cv_drug_era_30d integer NOT NULL,
    cv_drug_era_overlap integer NOT NULL,
    cv_drug_era_ever integer NOT NULL,
    cv_drug_group integer NOT NULL,
    cv_procedure_occ integer NOT NULL,
    cv_procedure_occ_365d integer NOT NULL,
    cv_procedure_occ_30d integer NOT NULL,
    cv_procedure_group integer NOT NULL,
    cv_observation integer NOT NULL,
    cv_observation_365d integer NOT NULL,
    cv_observation_30d integer NOT NULL,
    cv_observation_count_365d integer NOT NULL,
    cv_measurement integer NOT NULL,
    cv_measurement_365d integer NOT NULL,
    cv_measurement_30d integer NOT NULL,
    cv_measurement_count_365d integer NOT NULL,
    cv_measurement_below integer NOT NULL,
    cv_measurement_above integer NOT NULL,
    cv_concept_counts integer NOT NULL,
    cv_risk_scores integer NOT NULL,
    cv_risk_scores_charlson integer NOT NULL,
    cv_risk_scores_dcsi integer NOT NULL,
    cv_risk_scores_chads2 integer NOT NULL,
    cv_risk_scores_chads2vasc integer NOT NULL,
    cv_interaction_year integer NOT NULL,
    cv_interaction_month integer NOT NULL,
    del_covariates_small_count integer NOT NULL,
    created timestamp(3) without time zone NOT NULL,
    modified timestamp(3) without time zone,
    created_by_id integer,
    modified_by_id integer
);


--
-- Name: plp_sequence; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.plp_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: reusable_seq; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.reusable_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: reusable; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.reusable (
    id integer DEFAULT nextval('${ohdsiSchema}.reusable_seq'::regclass) NOT NULL,
    name character varying NOT NULL,
    description character varying,
    data text NOT NULL,
    created_by_id integer,
    created_date timestamp with time zone DEFAULT now() NOT NULL,
    modified_by_id integer,
    modified_date timestamp with time zone
);


--
-- Name: reusable_tag; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.reusable_tag (
    asset_id integer NOT NULL,
    tag_id integer NOT NULL
);


--
-- Name: reusable_version; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.reusable_version (
    asset_id bigint NOT NULL,
    comment character varying,
    description character varying,
    version integer DEFAULT 1 NOT NULL,
    asset_json character varying NOT NULL,
    archived boolean DEFAULT false NOT NULL,
    created_by_id integer,
    created_date timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: schema_version; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.schema_version (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


--
-- Name: sec_permission_sequence; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.sec_permission_sequence
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sec_permission; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.sec_permission (
    id integer DEFAULT nextval('${ohdsiSchema}.sec_permission_sequence'::regclass) NOT NULL,
    value character varying(255) NOT NULL,
    description character varying(255)
);


--
-- Name: COLUMN sec_permission.id; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.sec_permission.id IS 'Primary key';


--
-- Name: COLUMN sec_permission.value; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.sec_permission.value IS 'Permission';


--
-- Name: COLUMN sec_permission.description; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.sec_permission.description IS 'Desctiption of permission';


--
-- Name: sec_permission_id_seq; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.sec_permission_id_seq
    START WITH 500
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sec_role_sequence; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.sec_role_sequence
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sec_role; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.sec_role (
    id integer DEFAULT nextval('${ohdsiSchema}.sec_role_sequence'::regclass) NOT NULL,
    name character varying(255),
    system_role boolean DEFAULT false NOT NULL
);


--
-- Name: COLUMN sec_role.id; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.sec_role.id IS 'primary key';


--
-- Name: COLUMN sec_role.name; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.sec_role.name IS 'Role name';


--
-- Name: sec_role_group_seq; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.sec_role_group_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sec_role_group; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.sec_role_group (
    id integer DEFAULT nextval('${ohdsiSchema}.sec_role_group_seq'::regclass) NOT NULL,
    provider character varying NOT NULL,
    group_dn character varying NOT NULL,
    group_name character varying,
    role_id integer NOT NULL,
    job_id bigint
);


--
-- Name: sec_role_permission_sequence; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.sec_role_permission_sequence
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sec_role_permission; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.sec_role_permission (
    id integer DEFAULT nextval('${ohdsiSchema}.sec_role_permission_sequence'::regclass) NOT NULL,
    role_id integer NOT NULL,
    permission_id integer NOT NULL,
    status character varying(255)
);


--
-- Name: COLUMN sec_role_permission.id; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.sec_role_permission.id IS 'Primary key';


--
-- Name: COLUMN sec_role_permission.role_id; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.sec_role_permission.role_id IS 'Foreign key to SEC_ROLE';


--
-- Name: COLUMN sec_role_permission.permission_id; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.sec_role_permission.permission_id IS 'Foreign key to SEC_PERMISSION';


--
-- Name: COLUMN sec_role_permission.status; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.sec_role_permission.status IS 'Status of relation between role and permission';


--
-- Name: sec_user_sequence; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.sec_user_sequence
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sec_user; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.sec_user (
    id integer DEFAULT nextval('${ohdsiSchema}.sec_user_sequence'::regclass) NOT NULL,
    login character varying(1024),
    name character varying(100),
    last_viewed_notifications_time timestamp with time zone,
    origin character varying(32) DEFAULT 'SYSTEM'::character varying NOT NULL
);


--
-- Name: COLUMN sec_user.id; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.sec_user.id IS 'primary key';


--
-- Name: COLUMN sec_user.login; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.sec_user.login IS 'Login';


--
-- Name: COLUMN sec_user.name; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.sec_user.name IS 'Displayed name for user';


--
-- Name: sec_user_role_sequence; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.sec_user_role_sequence
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sec_user_role; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.sec_user_role (
    id integer DEFAULT nextval('${ohdsiSchema}.sec_user_role_sequence'::regclass) NOT NULL,
    user_id integer NOT NULL,
    role_id integer NOT NULL,
    status character varying(255),
    origin character varying(32) DEFAULT 'SYSTEM'::character varying NOT NULL
);


--
-- Name: COLUMN sec_user_role.id; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.sec_user_role.id IS 'Primary key';


--
-- Name: COLUMN sec_user_role.user_id; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.sec_user_role.user_id IS 'Foreign key to SEC_USER';


--
-- Name: COLUMN sec_user_role.role_id; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.sec_user_role.role_id IS 'Foreign key to SEC_ROLE';


--
-- Name: COLUMN sec_user_role.status; Type: COMMENT; Schema: webapi; Owner: -
--

COMMENT ON COLUMN ${ohdsiSchema}.sec_user_role.status IS 'Status of relation between user and role';


--
-- Name: source_sequence; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.source_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: source; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.source (
    source_id integer DEFAULT nextval('${ohdsiSchema}.source_sequence'::regclass) NOT NULL,
    source_name character varying(255) NOT NULL,
    source_key character varying(50) NOT NULL,
    source_connection character varying(8000) NOT NULL,
    source_dialect character varying(255) NOT NULL,
    username character varying(255),
    password character varying(255),
    krb_auth_method character varying DEFAULT 'PASSWORD'::character varying NOT NULL,
    keytab_name character varying,
    krb_keytab bytea,
    krb_admin_server character varying,
    deleted_date timestamp without time zone,
    created_by_id integer,
    created_date date,
    modified_by_id integer,
    modified_date date,
    is_cache_enabled boolean DEFAULT false NOT NULL,
    check_connection boolean DEFAULT true NOT NULL
);


--
-- Name: source_daimon_sequence; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.source_daimon_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: source_daimon; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.source_daimon (
    source_daimon_id integer DEFAULT nextval('${ohdsiSchema}.source_daimon_sequence'::regclass) NOT NULL,
    source_id integer NOT NULL,
    daimon_type integer NOT NULL,
    table_qualifier character varying(255) NOT NULL,
    priority integer NOT NULL
);


--
-- Name: tag_seq; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.tag_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tag; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.tag (
    id integer DEFAULT nextval('${ohdsiSchema}.tag_seq'::regclass) NOT NULL,
    name character varying NOT NULL,
    type integer DEFAULT 0 NOT NULL,
    count integer DEFAULT 0 NOT NULL,
    show_group boolean DEFAULT false NOT NULL,
    icon character varying,
    color character varying,
    multi_selection boolean DEFAULT false NOT NULL,
    permission_protected boolean DEFAULT false NOT NULL,
    mandatory boolean DEFAULT false NOT NULL,
    allow_custom boolean DEFAULT false NOT NULL,
    description character varying,
    created_by_id integer,
    created_date timestamp with time zone DEFAULT now() NOT NULL,
    modified_by_id integer,
    modified_date timestamp with time zone
);


--
-- Name: tag_group; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.tag_group (
    tag_id integer NOT NULL,
    group_id integer NOT NULL
);


--
-- Name: tool; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.tool (
    id bigint NOT NULL,
    name character varying(255) NOT NULL,
    url character varying(1000) NOT NULL,
    description character varying(1000),
    is_enabled boolean,
    created_by_id integer,
    modified_by_id integer,
    created_date timestamp with time zone DEFAULT now() NOT NULL,
    modified_date timestamp with time zone
);


--
-- Name: tool_seq; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.tool_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_import_job_seq; Type: SEQUENCE; Schema: webapi; Owner: -
--

CREATE SEQUENCE ${ohdsiSchema}.user_import_job_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_import_job; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.user_import_job (
    id bigint DEFAULT nextval('${ohdsiSchema}.user_import_job_seq'::regclass) NOT NULL,
    is_enabled boolean DEFAULT false NOT NULL,
    start_date timestamp with time zone,
    frequency character varying NOT NULL,
    recurring_times integer NOT NULL,
    recurring_until_date timestamp with time zone,
    cron character varying NOT NULL,
    last_executed_at timestamp with time zone,
    executed_times integer DEFAULT 0 NOT NULL,
    is_closed boolean DEFAULT false NOT NULL,
    provider_type character varying NOT NULL,
    preserve_roles boolean DEFAULT true NOT NULL,
    user_roles character varying
);


--
-- Name: user_import_job_weekdays; Type: TABLE; Schema: webapi; Owner: -
--

CREATE TABLE ${ohdsiSchema}.user_import_job_weekdays (
    user_import_job_id bigint NOT NULL,
    day_of_week character varying NOT NULL
);


--
-- Name: cohort_study cohort_study_id; Type: DEFAULT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort_study ALTER COLUMN cohort_study_id SET DEFAULT nextval('${ohdsiSchema}.cohort_study_cohort_study_id_seq'::regclass);


--
-- Name: concept_of_interest id; Type: DEFAULT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.concept_of_interest ALTER COLUMN id SET DEFAULT nextval('${ohdsiSchema}.concept_of_interest_id_seq'::regclass);


--
-- Name: heracles_heel_results id; Type: DEFAULT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.heracles_heel_results ALTER COLUMN id SET DEFAULT nextval('${ohdsiSchema}.heracles_heel_results_id_seq'::regclass);


--
-- Name: heracles_results id; Type: DEFAULT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.heracles_results ALTER COLUMN id SET DEFAULT nextval('${ohdsiSchema}.heracles_results_id_seq'::regclass);


--
-- Name: heracles_results_dist id; Type: DEFAULT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.heracles_results_dist ALTER COLUMN id SET DEFAULT nextval('${ohdsiSchema}.heracles_results_dist_id_seq'::regclass);


--
-- Name: ir_analysis_dist id; Type: DEFAULT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.ir_analysis_dist ALTER COLUMN id SET DEFAULT nextval('${ohdsiSchema}.ir_analysis_dist_id_seq'::regclass);


--
-- Name: penelope_laertes_uni_pivot id; Type: DEFAULT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.penelope_laertes_uni_pivot ALTER COLUMN id SET DEFAULT nextval('${ohdsiSchema}.penelope_laertes_uni_pivot_id_seq'::regclass);


--
-- Name: achilles_cache achilles_cache_pk; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.achilles_cache
    ADD CONSTRAINT achilles_cache_pk PRIMARY KEY (id);


--
-- Name: ee_analysis_status analysis_execution_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.ee_analysis_status
    ADD CONSTRAINT analysis_execution_pkey PRIMARY KEY (id);


--
-- Name: analysis_generation_info analysis_generation_info_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.analysis_generation_info
    ADD CONSTRAINT analysis_generation_info_pkey PRIMARY KEY (job_execution_id);


--
-- Name: batch_job_execution_context batch_job_execution_context_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.batch_job_execution_context
    ADD CONSTRAINT batch_job_execution_context_pkey PRIMARY KEY (job_execution_id);


--
-- Name: batch_job_execution batch_job_execution_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.batch_job_execution
    ADD CONSTRAINT batch_job_execution_pkey PRIMARY KEY (job_execution_id);


--
-- Name: batch_job_instance batch_job_instance_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.batch_job_instance
    ADD CONSTRAINT batch_job_instance_pkey PRIMARY KEY (job_instance_id);


--
-- Name: batch_step_execution_context batch_step_execution_context_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.batch_step_execution_context
    ADD CONSTRAINT batch_step_execution_context_pkey PRIMARY KEY (step_execution_id);


--
-- Name: batch_step_execution batch_step_execution_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.batch_step_execution
    ADD CONSTRAINT batch_step_execution_pkey PRIMARY KEY (step_execution_id);


--
-- Name: cca_execution_ext cca_execution_ext_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cca_execution_ext
    ADD CONSTRAINT cca_execution_ext_pkey PRIMARY KEY (cca_execution_id);


--
-- Name: cca_execution cca_execution_pk; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cca_execution
    ADD CONSTRAINT cca_execution_pk PRIMARY KEY (cca_execution_id);


--
-- Name: cdm_cache cdm_cache_pk; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cdm_cache
    ADD CONSTRAINT cdm_cache_pk PRIMARY KEY (id);


--
-- Name: cdm_cache cdm_cache_un; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cdm_cache
    ADD CONSTRAINT cdm_cache_un UNIQUE (concept_id, source_id);


--
-- Name: cohort_analysis_gen_info cohort_analysis_gen_info_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort_analysis_gen_info
    ADD CONSTRAINT cohort_analysis_gen_info_pkey PRIMARY KEY (source_id, cohort_id);


--
-- Name: cohort_analysis_list_xref cohort_analysis_list_xref_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort_analysis_list_xref
    ADD CONSTRAINT cohort_analysis_list_xref_pkey PRIMARY KEY (source_id, cohort_id, analysis_id);


--
-- Name: cohort_concept_map cohort_concept_map_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort_concept_map
    ADD CONSTRAINT cohort_concept_map_pkey PRIMARY KEY (cohort_definition_id);


--
-- Name: cohort_inclusion cohort_inclusion_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort_inclusion
    ADD CONSTRAINT cohort_inclusion_pkey PRIMARY KEY (cohort_definition_id);


--
-- Name: cohort_inclusion_result cohort_inclusion_result_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort_inclusion_result
    ADD CONSTRAINT cohort_inclusion_result_pkey PRIMARY KEY (cohort_definition_id);


--
-- Name: cohort_inclusion_stats cohort_inclusion_stats_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort_inclusion_stats
    ADD CONSTRAINT cohort_inclusion_stats_pkey PRIMARY KEY (cohort_definition_id);


--
-- Name: cohort cohort_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort
    ADD CONSTRAINT cohort_pkey PRIMARY KEY (cohort_definition_id, subject_id);


--
-- Name: cohort_sample cohort_sample_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort_sample
    ADD CONSTRAINT cohort_sample_pkey PRIMARY KEY (id);


--
-- Name: cohort_summary_stats cohort_summary_stats_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort_summary_stats
    ADD CONSTRAINT cohort_summary_stats_pkey PRIMARY KEY (cohort_definition_id);


--
-- Name: exampleapp_widget exampleapp_widget_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.exampleapp_widget
    ADD CONSTRAINT exampleapp_widget_pkey PRIMARY KEY (id);


--
-- Name: feas_study_inclusion_stats feas_study_inclusion_stats_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.feas_study_inclusion_stats
    ADD CONSTRAINT feas_study_inclusion_stats_pkey PRIMARY KEY (study_id);


--
-- Name: feas_study_index_stats feas_study_index_stats_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.feas_study_index_stats
    ADD CONSTRAINT feas_study_index_stats_pkey PRIMARY KEY (study_id);


--
-- Name: feas_study_result feas_study_result_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.feas_study_result
    ADD CONSTRAINT feas_study_result_pkey PRIMARY KEY (study_id);


--
-- Name: feasibility_inclusion feasibility_inclusion_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.feasibility_inclusion
    ADD CONSTRAINT feasibility_inclusion_pkey PRIMARY KEY (study_id, sequence);


--
-- Name: heracles_analysis heracles_analysis_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.heracles_analysis
    ADD CONSTRAINT heracles_analysis_pkey PRIMARY KEY (analysis_id);


--
-- Name: heracles_heel_results heracles_heel_results_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.heracles_heel_results
    ADD CONSTRAINT heracles_heel_results_pkey PRIMARY KEY (id);


--
-- Name: heracles_results_dist heracles_results_dist_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.heracles_results_dist
    ADD CONSTRAINT heracles_results_dist_pkey PRIMARY KEY (id);


--
-- Name: heracles_results heracles_results_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.heracles_results
    ADD CONSTRAINT heracles_results_pkey PRIMARY KEY (id);


--
-- Name: ir_analysis_dist ir_analysis_dist_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.ir_analysis_dist
    ADD CONSTRAINT ir_analysis_dist_pkey PRIMARY KEY (id);


--
-- Name: batch_job_instance job_inst_un; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.batch_job_instance
    ADD CONSTRAINT job_inst_un UNIQUE (job_name, job_key);


--
-- Name: output_file_contents output_file_contents_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.output_file_contents
    ADD CONSTRAINT output_file_contents_pkey PRIMARY KEY (output_file_id);


--
-- Name: penelope_laertes_uni_pivot penelope_laertes_uni_pivot_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.penelope_laertes_uni_pivot
    ADD CONSTRAINT penelope_laertes_uni_pivot_pkey PRIMARY KEY (id);


--
-- Name: penelope_laertes_universe penelope_laertes_universe_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.penelope_laertes_universe
    ADD CONSTRAINT penelope_laertes_universe_pkey PRIMARY KEY (id);


--
-- Name: sec_permission permission_unique; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.sec_permission
    ADD CONSTRAINT permission_unique UNIQUE (value);


--
-- Name: cca pk_cca_cca_id; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cca
    ADD CONSTRAINT pk_cca_cca_id PRIMARY KEY (cca_id);


--
-- Name: feasibility_study pk_clinical_trial_protocol; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.feasibility_study
    ADD CONSTRAINT pk_clinical_trial_protocol PRIMARY KEY (id);


--
-- Name: cohort_definition pk_cohort_definition; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort_definition
    ADD CONSTRAINT pk_cohort_definition PRIMARY KEY (id);


--
-- Name: cohort_definition_details pk_cohort_definition_details; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort_definition_details
    ADD CONSTRAINT pk_cohort_definition_details PRIMARY KEY (id);


--
-- Name: cohort_generation_info pk_cohort_generation_info; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort_generation_info
    ADD CONSTRAINT pk_cohort_generation_info PRIMARY KEY (id, source_id);


--
-- Name: cohort_study pk_cohort_study; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort_study
    ADD CONSTRAINT pk_cohort_study PRIMARY KEY (cohort_study_id);


--
-- Name: cohort_tag pk_cohort_tags_id; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort_tag
    ADD CONSTRAINT pk_cohort_tags_id PRIMARY KEY (asset_id, tag_id);


--
-- Name: cohort_version pk_cohort_version_id; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort_version
    ADD CONSTRAINT pk_cohort_version_id PRIMARY KEY (asset_id, version);


--
-- Name: concept_of_interest pk_concept_of_interest; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.concept_of_interest
    ADD CONSTRAINT pk_concept_of_interest PRIMARY KEY (id);


--
-- Name: concept_set pk_concept_set; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.concept_set
    ADD CONSTRAINT pk_concept_set PRIMARY KEY (concept_set_id);


--
-- Name: concept_set_annotation pk_concept_set_annotation_id; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.concept_set_annotation
    ADD CONSTRAINT pk_concept_set_annotation_id PRIMARY KEY (concept_set_annotation_id);


--
-- Name: concept_set_generation_info pk_concept_set_generation_info; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.concept_set_generation_info
    ADD CONSTRAINT pk_concept_set_generation_info PRIMARY KEY (concept_set_id, source_id);


--
-- Name: concept_set_item pk_concept_set_item; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.concept_set_item
    ADD CONSTRAINT pk_concept_set_item PRIMARY KEY (concept_set_item_id);


--
-- Name: concept_set_negative_controls pk_concept_set_nc; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.concept_set_negative_controls
    ADD CONSTRAINT pk_concept_set_nc PRIMARY KEY (id);


--
-- Name: concept_set_tag pk_concept_set_tags_id; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.concept_set_tag
    ADD CONSTRAINT pk_concept_set_tags_id PRIMARY KEY (asset_id, tag_id);


--
-- Name: concept_set_version pk_concept_set_version_id; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.concept_set_version
    ADD CONSTRAINT pk_concept_set_version_id PRIMARY KEY (asset_id, version);


--
-- Name: drug_hoi_evidence pk_drug_hoi_evidence; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.drug_hoi_evidence
    ADD CONSTRAINT pk_drug_hoi_evidence PRIMARY KEY (id);


--
-- Name: drug_hoi_relationship pk_drug_hoi_relationship; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.drug_hoi_relationship
    ADD CONSTRAINT pk_drug_hoi_relationship PRIMARY KEY (id);


--
-- Name: drug_labels pk_drug_labels; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.drug_labels
    ADD CONSTRAINT pk_drug_labels PRIMARY KEY (drug_label_id);


--
-- Name: evidence_sources pk_evidence_sources; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.evidence_sources
    ADD CONSTRAINT pk_evidence_sources PRIMARY KEY (id);


--
-- Name: fe_analysis_conceptset pk_fe_conceptset_id; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.fe_analysis_conceptset
    ADD CONSTRAINT pk_fe_conceptset_id PRIMARY KEY (id);


--
-- Name: feas_study_generation_info pk_feas_study_generation_info; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.feas_study_generation_info
    ADD CONSTRAINT pk_feas_study_generation_info PRIMARY KEY (study_id, source_id);


--
-- Name: generation_cache pk_generation_cache; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.generation_cache
    ADD CONSTRAINT pk_generation_cache PRIMARY KEY (id);


--
-- Name: heracles_visualization_data pk_heracles_viz_data; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.heracles_visualization_data
    ADD CONSTRAINT pk_heracles_viz_data PRIMARY KEY (id);


--
-- Name: ir_version pk_ir_version_id; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.ir_version
    ADD CONSTRAINT pk_ir_version_id PRIMARY KEY (asset_id, version);


--
-- Name: laertes_summary pk_laertes_summary; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.laertes_summary
    ADD CONSTRAINT pk_laertes_summary PRIMARY KEY (id);


--
-- Name: pathway_version pk_pathway_version_id; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.pathway_version
    ADD CONSTRAINT pk_pathway_version_id PRIMARY KEY (asset_id, version);


--
-- Name: plp pk_plp_plp_id; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.plp
    ADD CONSTRAINT pk_plp_plp_id PRIMARY KEY (plp_id);


--
-- Name: reusable pk_reusable_id; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.reusable
    ADD CONSTRAINT pk_reusable_id PRIMARY KEY (id);


--
-- Name: reusable_tag pk_reusable_tag_id; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.reusable_tag
    ADD CONSTRAINT pk_reusable_tag_id PRIMARY KEY (asset_id, tag_id);


--
-- Name: reusable_version pk_reusable_version_id; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.reusable_version
    ADD CONSTRAINT pk_reusable_version_id PRIMARY KEY (asset_id, version);


--
-- Name: sec_permission pk_sec_permission; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.sec_permission
    ADD CONSTRAINT pk_sec_permission PRIMARY KEY (id);


--
-- Name: sec_role pk_sec_role; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.sec_role
    ADD CONSTRAINT pk_sec_role PRIMARY KEY (id);


--
-- Name: sec_role_permission pk_sec_role_permission; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.sec_role_permission
    ADD CONSTRAINT pk_sec_role_permission PRIMARY KEY (id);


--
-- Name: sec_user pk_sec_user; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.sec_user
    ADD CONSTRAINT pk_sec_user PRIMARY KEY (id);


--
-- Name: sec_user_role pk_sec_user_role; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.sec_user_role
    ADD CONSTRAINT pk_sec_user_role PRIMARY KEY (id);


--
-- Name: source pk_source; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.source
    ADD CONSTRAINT pk_source PRIMARY KEY (source_id);


--
-- Name: source_daimon pk_source_daimon; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.source_daimon
    ADD CONSTRAINT pk_source_daimon PRIMARY KEY (source_daimon_id);


--
-- Name: tag pk_tags_id; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.tag
    ADD CONSTRAINT pk_tags_id PRIMARY KEY (id);


--
-- Name: tool pk_tool; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.tool
    ADD CONSTRAINT pk_tool PRIMARY KEY (id);


--
-- Name: user_import_job pk_user_import_job; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.user_import_job
    ADD CONSTRAINT pk_user_import_job PRIMARY KEY (id);


--
-- Name: user_import_job_weekdays pk_user_import_job_weekdays; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.user_import_job_weekdays
    ADD CONSTRAINT pk_user_import_job_weekdays PRIMARY KEY (user_import_job_id, day_of_week);


--
-- Name: sec_role_permission role_permission_unique; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.sec_role_permission
    ADD CONSTRAINT role_permission_unique UNIQUE (role_id, permission_id);


--
-- Name: schema_version schema_version_pk; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.schema_version
    ADD CONSTRAINT schema_version_pk PRIMARY KEY (installed_rank);


--
-- Name: sec_role_group sec_role_group_pkey; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.sec_role_group
    ADD CONSTRAINT sec_role_group_pkey PRIMARY KEY (id);


--
-- Name: sec_role sec_role_name_uq; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.sec_role
    ADD CONSTRAINT sec_role_name_uq UNIQUE (name, system_role);


--
-- Name: sec_user sec_user_login_unique; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.sec_user
    ADD CONSTRAINT sec_user_login_unique UNIQUE (login);


--
-- Name: source source_key_unique; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.source
    ADD CONSTRAINT source_key_unique UNIQUE (source_key);


--
-- Name: sec_role_group uc_provider_group_role; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.sec_role_group
    ADD CONSTRAINT uc_provider_group_role UNIQUE (provider, group_dn, role_id, job_id);


--
-- Name: source_daimon un_source_daimon; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.source_daimon
    ADD CONSTRAINT un_source_daimon UNIQUE (source_id, daimon_type);


--
-- Name: cohort_definition uq_cd_name; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort_definition
    ADD CONSTRAINT uq_cd_name UNIQUE (name);


--
-- Name: concept_set uq_cs_name; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.concept_set
    ADD CONSTRAINT uq_cs_name UNIQUE (concept_set_name);


--
-- Name: generation_cache uq_gc_hash; Type: CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.generation_cache
    ADD CONSTRAINT uq_gc_hash UNIQUE (type, design_hash, source_id);


--
-- Name: achilles_cache_source_id_idx; Type: INDEX; Schema: webapi; Owner: -
--

CREATE UNIQUE INDEX achilles_cache_source_id_idx ON ${ohdsiSchema}.achilles_cache USING btree (source_id, cache_name);


--
-- Name: bjep_job_exec_params_idx; Type: INDEX; Schema: webapi; Owner: -
--

CREATE INDEX bjep_job_exec_params_idx ON ${ohdsiSchema}.batch_job_execution_params USING btree (job_execution_id);


--
-- Name: cdm_cache_concept_id_idx; Type: INDEX; Schema: webapi; Owner: -
--

CREATE INDEX cdm_cache_concept_id_idx ON ${ohdsiSchema}.cdm_cache USING btree (concept_id, source_id);


--
-- Name: cohort_tags_cohort_id_idx; Type: INDEX; Schema: webapi; Owner: -
--

CREATE INDEX cohort_tags_cohort_id_idx ON ${ohdsiSchema}.cohort_tag USING btree (asset_id);


--
-- Name: cohort_tags_tag_id_idx; Type: INDEX; Schema: webapi; Owner: -
--

CREATE INDEX cohort_tags_tag_id_idx ON ${ohdsiSchema}.cohort_tag USING btree (tag_id);


--
-- Name: cohort_version_asset_idx; Type: INDEX; Schema: webapi; Owner: -
--

CREATE INDEX cohort_version_asset_idx ON ${ohdsiSchema}.cohort_version USING btree (asset_id);


--
-- Name: concept_set_tags_concept_id_idx; Type: INDEX; Schema: webapi; Owner: -
--

CREATE INDEX concept_set_tags_concept_id_idx ON ${ohdsiSchema}.concept_set_tag USING btree (asset_id);


--
-- Name: concept_set_tags_tag_id_idx; Type: INDEX; Schema: webapi; Owner: -
--

CREATE INDEX concept_set_tags_tag_id_idx ON ${ohdsiSchema}.concept_set_tag USING btree (tag_id);


--
-- Name: concept_set_version_asset_idx; Type: INDEX; Schema: webapi; Owner: -
--

CREATE INDEX concept_set_version_asset_idx ON ${ohdsiSchema}.concept_set_version USING btree (asset_id);


--
-- Name: heracles_viz_data_idx; Type: INDEX; Schema: webapi; Owner: -
--

CREATE INDEX heracles_viz_data_idx ON ${ohdsiSchema}.heracles_visualization_data USING btree (cohort_definition_id, source_id, visualization_key);


--
-- Name: heracles_viz_data_unq_idx; Type: INDEX; Schema: webapi; Owner: -
--

CREATE UNIQUE INDEX heracles_viz_data_unq_idx ON ${ohdsiSchema}.heracles_visualization_data USING btree (cohort_definition_id, source_id, visualization_key, drilldown_id);


--
-- Name: hh_idx_cohort_id_analysis_id; Type: INDEX; Schema: webapi; Owner: -
--

CREATE INDEX hh_idx_cohort_id_analysis_id ON ${ohdsiSchema}.heracles_heel_results USING btree (cohort_definition_id, analysis_id);


--
-- Name: hr_idx_cohort_def_id; Type: INDEX; Schema: webapi; Owner: -
--

CREATE INDEX hr_idx_cohort_def_id ON ${ohdsiSchema}.heracles_results USING btree (cohort_definition_id);


--
-- Name: hr_idx_cohort_def_id_dt; Type: INDEX; Schema: webapi; Owner: -
--

CREATE INDEX hr_idx_cohort_def_id_dt ON ${ohdsiSchema}.heracles_results USING btree (cohort_definition_id, last_update_time);


--
-- Name: hr_idx_cohort_id_analysis_id; Type: INDEX; Schema: webapi; Owner: -
--

CREATE INDEX hr_idx_cohort_id_analysis_id ON ${ohdsiSchema}.heracles_results USING btree (cohort_definition_id, analysis_id);


--
-- Name: hr_idx_cohort_id_first_res; Type: INDEX; Schema: webapi; Owner: -
--

CREATE INDEX hr_idx_cohort_id_first_res ON ${ohdsiSchema}.heracles_results USING btree (cohort_definition_id, analysis_id, count_value, stratum_1);


--
-- Name: hrd_idx_cohort_def_id; Type: INDEX; Schema: webapi; Owner: -
--

CREATE INDEX hrd_idx_cohort_def_id ON ${ohdsiSchema}.heracles_results_dist USING btree (cohort_definition_id);


--
-- Name: hrd_idx_cohort_def_id_dt; Type: INDEX; Schema: webapi; Owner: -
--

CREATE INDEX hrd_idx_cohort_def_id_dt ON ${ohdsiSchema}.heracles_results_dist USING btree (cohort_definition_id, last_update_time);


--
-- Name: hrd_idx_cohort_id_analysis_id; Type: INDEX; Schema: webapi; Owner: -
--

CREATE INDEX hrd_idx_cohort_id_analysis_id ON ${ohdsiSchema}.heracles_results_dist USING btree (cohort_definition_id, analysis_id);


--
-- Name: hrd_idx_cohort_id_first_res; Type: INDEX; Schema: webapi; Owner: -
--

CREATE INDEX hrd_idx_cohort_id_first_res ON ${ohdsiSchema}.heracles_results_dist USING btree (cohort_definition_id, analysis_id, count_value, stratum_1);


--
-- Name: idx_cohort_sample_source; Type: INDEX; Schema: webapi; Owner: -
--

CREATE INDEX idx_cohort_sample_source ON ${ohdsiSchema}.cohort_sample USING btree (cohort_definition_id, source_id);


--
-- Name: idx_penelope_laertes_uni_pivot; Type: INDEX; Schema: webapi; Owner: -
--

CREATE INDEX idx_penelope_laertes_uni_pivot ON ${ohdsiSchema}.penelope_laertes_uni_pivot USING btree (ingredient_concept_id, condition_concept_id);

ALTER TABLE ${ohdsiSchema}.penelope_laertes_uni_pivot CLUSTER ON idx_penelope_laertes_uni_pivot;


--
-- Name: ir_version_asset_idx; Type: INDEX; Schema: webapi; Owner: -
--

CREATE INDEX ir_version_asset_idx ON ${ohdsiSchema}.ir_version USING btree (asset_id);


--
-- Name: pathway_version_asset_idx; Type: INDEX; Schema: webapi; Owner: -
--

CREATE INDEX pathway_version_asset_idx ON ${ohdsiSchema}.pathway_version USING btree (asset_id);


--
-- Name: reusable_name_idx; Type: INDEX; Schema: webapi; Owner: -
--

CREATE UNIQUE INDEX reusable_name_idx ON ${ohdsiSchema}.reusable USING btree (lower((name)::text));


--
-- Name: reusable_tag_reusableidx; Type: INDEX; Schema: webapi; Owner: -
--

CREATE INDEX reusable_tag_reusableidx ON ${ohdsiSchema}.reusable_tag USING btree (asset_id);


--
-- Name: reusable_tag_tag_id_idx; Type: INDEX; Schema: webapi; Owner: -
--

CREATE INDEX reusable_tag_tag_id_idx ON ${ohdsiSchema}.reusable_tag USING btree (tag_id);


--
-- Name: reusable_version_asset_idx; Type: INDEX; Schema: webapi; Owner: -
--

CREATE INDEX reusable_version_asset_idx ON ${ohdsiSchema}.reusable_version USING btree (asset_id);


--
-- Name: schema_version_s_idx; Type: INDEX; Schema: webapi; Owner: -
--

CREATE INDEX schema_version_s_idx ON ${ohdsiSchema}.schema_version USING btree (success);


--
-- Name: tags_name_idx; Type: INDEX; Schema: webapi; Owner: -
--

CREATE UNIQUE INDEX tags_name_idx ON ${ohdsiSchema}.tag USING btree (lower((name)::text));


--
-- Name: achilles_cache achilles_cache_fk; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.achilles_cache
    ADD CONSTRAINT achilles_cache_fk FOREIGN KEY (source_id) REFERENCES ${ohdsiSchema}.source(source_id) ON DELETE CASCADE;


--
-- Name: cca cca_created_by_id_fkey; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cca
    ADD CONSTRAINT cca_created_by_id_fkey FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);


--
-- Name: cca cca_modified_by_id_fkey; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cca
    ADD CONSTRAINT cca_modified_by_id_fkey FOREIGN KEY (modified_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);


--
-- Name: cdm_cache cdm_cache_fk; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cdm_cache
    ADD CONSTRAINT cdm_cache_fk FOREIGN KEY (source_id) REFERENCES ${ohdsiSchema}.source(source_id) ON DELETE CASCADE;


--
-- Name: cohort_definition cohort_definition_created_by_id_fkey; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort_definition
    ADD CONSTRAINT cohort_definition_created_by_id_fkey FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);


--
-- Name: cohort_definition cohort_definition_modified_by_id_fkey; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort_definition
    ADD CONSTRAINT cohort_definition_modified_by_id_fkey FOREIGN KEY (modified_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);


--
-- Name: cohort_generation_info cohort_generation_info_created_by_id_fkey; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort_generation_info
    ADD CONSTRAINT cohort_generation_info_created_by_id_fkey FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);


--
-- Name: cohort_tag cohort_tags_fk_definitions; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort_tag
    ADD CONSTRAINT cohort_tags_fk_definitions FOREIGN KEY (asset_id) REFERENCES ${ohdsiSchema}.cohort_definition(id) ON DELETE CASCADE;


--
-- Name: cohort_tag cohort_tags_fk_tags; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort_tag
    ADD CONSTRAINT cohort_tags_fk_tags FOREIGN KEY (tag_id) REFERENCES ${ohdsiSchema}.tag(id) ON DELETE CASCADE;


--
-- Name: concept_set concept_set_created_by_id_fkey; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.concept_set
    ADD CONSTRAINT concept_set_created_by_id_fkey FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);


--
-- Name: concept_set concept_set_modified_by_id_fkey; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.concept_set
    ADD CONSTRAINT concept_set_modified_by_id_fkey FOREIGN KEY (modified_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);


--
-- Name: concept_set_tag concept_set_tags_fk_sets; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.concept_set_tag
    ADD CONSTRAINT concept_set_tags_fk_sets FOREIGN KEY (asset_id) REFERENCES ${ohdsiSchema}.concept_set(concept_set_id) ON DELETE CASCADE;


--
-- Name: concept_set_tag concept_set_tags_fk_tags; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.concept_set_tag
    ADD CONSTRAINT concept_set_tags_fk_tags FOREIGN KEY (tag_id) REFERENCES ${ohdsiSchema}.tag(id) ON DELETE CASCADE;


--
-- Name: feasibility_study feasibility_study_created_by_id_fkey; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.feasibility_study
    ADD CONSTRAINT feasibility_study_created_by_id_fkey FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);


--
-- Name: feasibility_study feasibility_study_modified_by_id_fkey; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.feasibility_study
    ADD CONSTRAINT feasibility_study_modified_by_id_fkey FOREIGN KEY (modified_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);


--
-- Name: cohort_analysis_gen_info fk_cagi_cohort_id; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort_analysis_gen_info
    ADD CONSTRAINT fk_cagi_cohort_id FOREIGN KEY (cohort_id) REFERENCES ${ohdsiSchema}.cohort_definition(id);


--
-- Name: cohort_analysis_list_xref fk_calx_source_id; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort_analysis_list_xref
    ADD CONSTRAINT fk_calx_source_id FOREIGN KEY (source_id, cohort_id) REFERENCES ${ohdsiSchema}.cohort_analysis_gen_info(source_id, cohort_id);


--
-- Name: analysis_generation_info fk_cgi_sec_user; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.analysis_generation_info
    ADD CONSTRAINT fk_cgi_sec_user FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);


--
-- Name: cohort_definition_details fk_cohort_definition_details_cohort_definition; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort_definition_details
    ADD CONSTRAINT fk_cohort_definition_details_cohort_definition FOREIGN KEY (id) REFERENCES ${ohdsiSchema}.cohort_definition(id);


--
-- Name: cohort_generation_info fk_cohort_generation_info_cohort_definition; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort_generation_info
    ADD CONSTRAINT fk_cohort_generation_info_cohort_definition FOREIGN KEY (id) REFERENCES ${ohdsiSchema}.cohort_definition(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: cohort_sample fk_cohort_sample_definition_id; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort_sample
    ADD CONSTRAINT fk_cohort_sample_definition_id FOREIGN KEY (cohort_definition_id) REFERENCES ${ohdsiSchema}.cohort_definition(id) ON DELETE CASCADE;


--
-- Name: cohort_sample fk_cohort_sample_source_id; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort_sample
    ADD CONSTRAINT fk_cohort_sample_source_id FOREIGN KEY (source_id) REFERENCES ${ohdsiSchema}.source(source_id) ON DELETE CASCADE;


--
-- Name: cohort_version fk_cohort_version_asset_id; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort_version
    ADD CONSTRAINT fk_cohort_version_asset_id FOREIGN KEY (asset_id) REFERENCES ${ohdsiSchema}.cohort_definition(id) ON DELETE CASCADE;


--
-- Name: cohort_version fk_cohort_version_sec_user_creator; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.cohort_version
    ADD CONSTRAINT fk_cohort_version_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);


--
-- Name: concept_set_annotation fk_concept_set; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.concept_set_annotation
    ADD CONSTRAINT fk_concept_set FOREIGN KEY (concept_set_id) REFERENCES ${ohdsiSchema}.concept_set(concept_set_id) ON DELETE CASCADE;


--
-- Name: concept_set_generation_info fk_concept_set_generation_info_concept_set; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.concept_set_generation_info
    ADD CONSTRAINT fk_concept_set_generation_info_concept_set FOREIGN KEY (concept_set_id) REFERENCES ${ohdsiSchema}.concept_set(concept_set_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: concept_set_version fk_concept_set_version_asset_id; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.concept_set_version
    ADD CONSTRAINT fk_concept_set_version_asset_id FOREIGN KEY (asset_id) REFERENCES ${ohdsiSchema}.concept_set(concept_set_id) ON DELETE CASCADE;


--
-- Name: concept_set_version fk_concept_set_version_sec_user_creator; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.concept_set_version
    ADD CONSTRAINT fk_concept_set_version_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);


--
-- Name: drug_hoi_evidence fk_drug_hoi_relationship; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.drug_hoi_evidence
    ADD CONSTRAINT fk_drug_hoi_relationship FOREIGN KEY (drug_hoi_relationship) REFERENCES ${ohdsiSchema}.drug_hoi_relationship(id);


--
-- Name: drug_hoi_evidence fk_evidence_sources; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.drug_hoi_evidence
    ADD CONSTRAINT fk_evidence_sources FOREIGN KEY (evidence_source_code_id) REFERENCES ${ohdsiSchema}.evidence_sources(id);


--
-- Name: feas_study_generation_info fk_feas_study_generation_info_feasibility_study; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.feas_study_generation_info
    ADD CONSTRAINT fk_feas_study_generation_info_feasibility_study FOREIGN KEY (study_id) REFERENCES ${ohdsiSchema}.feasibility_study(id);


--
-- Name: feasibility_inclusion fk_feasibility_inclusion_feasibility_study; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.feasibility_inclusion
    ADD CONSTRAINT fk_feasibility_inclusion_feasibility_study FOREIGN KEY (study_id) REFERENCES ${ohdsiSchema}.feasibility_study(id);


--
-- Name: feasibility_study fk_feasibility_study_cohort_definition_index; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.feasibility_study
    ADD CONSTRAINT fk_feasibility_study_cohort_definition_index FOREIGN KEY (index_def_id) REFERENCES ${ohdsiSchema}.cohort_definition(id);


--
-- Name: feasibility_study fk_feasibility_study_cohort_definition_result; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.feasibility_study
    ADD CONSTRAINT fk_feasibility_study_cohort_definition_result FOREIGN KEY (result_def_id) REFERENCES ${ohdsiSchema}.cohort_definition(id);


--
-- Name: generation_cache fk_gc_source_id_source; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.generation_cache
    ADD CONSTRAINT fk_gc_source_id_source FOREIGN KEY (source_id) REFERENCES ${ohdsiSchema}.source(source_id);


--
-- Name: ir_version fk_ir_version_sec_user_creator; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.ir_version
    ADD CONSTRAINT fk_ir_version_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);


--
-- Name: pathway_version fk_pathway_version_sec_user_creator; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.pathway_version
    ADD CONSTRAINT fk_pathway_version_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);


--
-- Name: reusable fk_reusable_sec_user_creator; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.reusable
    ADD CONSTRAINT fk_reusable_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);


--
-- Name: reusable fk_reusable_sec_user_updater; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.reusable
    ADD CONSTRAINT fk_reusable_sec_user_updater FOREIGN KEY (modified_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);


--
-- Name: reusable_version fk_reusable_version_asset_id; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.reusable_version
    ADD CONSTRAINT fk_reusable_version_asset_id FOREIGN KEY (asset_id) REFERENCES ${ohdsiSchema}.reusable(id) ON DELETE CASCADE;


--
-- Name: reusable_version fk_reusable_version_sec_user_creator; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.reusable_version
    ADD CONSTRAINT fk_reusable_version_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);


--
-- Name: sec_role_group fk_role_group_job; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.sec_role_group
    ADD CONSTRAINT fk_role_group_job FOREIGN KEY (job_id) REFERENCES ${ohdsiSchema}.user_import_job(id) ON DELETE CASCADE;


--
-- Name: sec_role_permission fk_role_permission_to_permission; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.sec_role_permission
    ADD CONSTRAINT fk_role_permission_to_permission FOREIGN KEY (permission_id) REFERENCES ${ohdsiSchema}.sec_permission(id);


--
-- Name: sec_role_permission fk_role_permission_to_role; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.sec_role_permission
    ADD CONSTRAINT fk_role_permission_to_role FOREIGN KEY (role_id) REFERENCES ${ohdsiSchema}.sec_role(id);


--
-- Name: source_daimon fk_source_daimon_source_id; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.source_daimon
    ADD CONSTRAINT fk_source_daimon_source_id FOREIGN KEY (source_id) REFERENCES ${ohdsiSchema}.source(source_id);


--
-- Name: tag fk_tags_sec_user_creator; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.tag
    ADD CONSTRAINT fk_tags_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);


--
-- Name: tag fk_tags_sec_user_updater; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.tag
    ADD CONSTRAINT fk_tags_sec_user_updater FOREIGN KEY (modified_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);


--
-- Name: tool fk_tool_ser_user_creator; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.tool
    ADD CONSTRAINT fk_tool_ser_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);


--
-- Name: tool fk_tool_ser_user_updater; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.tool
    ADD CONSTRAINT fk_tool_ser_user_updater FOREIGN KEY (modified_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);


--
-- Name: sec_user_role fk_user_role_to_role; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.sec_user_role
    ADD CONSTRAINT fk_user_role_to_role FOREIGN KEY (role_id) REFERENCES ${ohdsiSchema}.sec_role(id);


--
-- Name: sec_user_role fk_user_role_to_user; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.sec_user_role
    ADD CONSTRAINT fk_user_role_to_user FOREIGN KEY (user_id) REFERENCES ${ohdsiSchema}.sec_user(id);


--
-- Name: batch_job_execution_context job_exec_ctx_fk; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.batch_job_execution_context
    ADD CONSTRAINT job_exec_ctx_fk FOREIGN KEY (job_execution_id) REFERENCES ${ohdsiSchema}.batch_job_execution(job_execution_id);


--
-- Name: batch_job_execution_params job_exec_params_fk; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.batch_job_execution_params
    ADD CONSTRAINT job_exec_params_fk FOREIGN KEY (job_execution_id) REFERENCES ${ohdsiSchema}.batch_job_execution(job_execution_id);


--
-- Name: batch_step_execution job_exec_step_fk; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.batch_step_execution
    ADD CONSTRAINT job_exec_step_fk FOREIGN KEY (job_execution_id) REFERENCES ${ohdsiSchema}.batch_job_execution(job_execution_id);


--
-- Name: batch_job_execution job_inst_exec_fk; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.batch_job_execution
    ADD CONSTRAINT job_inst_exec_fk FOREIGN KEY (job_instance_id) REFERENCES ${ohdsiSchema}.batch_job_instance(job_instance_id);


--
-- Name: plp plp_created_by_id_fkey; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.plp
    ADD CONSTRAINT plp_created_by_id_fkey FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);


--
-- Name: plp plp_modified_by_id_fkey; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.plp
    ADD CONSTRAINT plp_modified_by_id_fkey FOREIGN KEY (modified_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);


--
-- Name: reusable_tag reusable_tag_fk_reusable; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.reusable_tag
    ADD CONSTRAINT reusable_tag_fk_reusable FOREIGN KEY (asset_id) REFERENCES ${ohdsiSchema}.reusable(id) ON DELETE CASCADE;


--
-- Name: reusable_tag reusable_tag_fk_tag; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.reusable_tag
    ADD CONSTRAINT reusable_tag_fk_tag FOREIGN KEY (tag_id) REFERENCES ${ohdsiSchema}.tag(id) ON DELETE CASCADE;


--
-- Name: source source_created_by_id_fkey; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.source
    ADD CONSTRAINT source_created_by_id_fkey FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);


--
-- Name: source source_modified_by_id_fkey; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.source
    ADD CONSTRAINT source_modified_by_id_fkey FOREIGN KEY (modified_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);


--
-- Name: batch_step_execution_context step_exec_ctx_fk; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.batch_step_execution_context
    ADD CONSTRAINT step_exec_ctx_fk FOREIGN KEY (step_execution_id) REFERENCES ${ohdsiSchema}.batch_step_execution(step_execution_id);


--
-- Name: tag_group tag_groups_group_fk; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.tag_group
    ADD CONSTRAINT tag_groups_group_fk FOREIGN KEY (group_id) REFERENCES ${ohdsiSchema}.tag(id) ON DELETE CASCADE;


--
-- Name: tag_group tag_groups_tag_fk; Type: FK CONSTRAINT; Schema: webapi; Owner: -
--

ALTER TABLE ONLY ${ohdsiSchema}.tag_group
    ADD CONSTRAINT tag_groups_tag_fk FOREIGN KEY (tag_id) REFERENCES ${ohdsiSchema}.tag(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

\unrestrict t474pauZ1C9C4oxWzVXL50RwAAGy6Pwr94VLSbCfAFfMAYbxJtKqDszoeHg67vD

