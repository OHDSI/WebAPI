-- Add ON DELETE CASCADE to both entity and role FK constraints on all sec_* access tables.
-- Deleting a parent entity or a role will automatically remove its permission grants.

-- sec_cohort_definition
ALTER TABLE ${ohdsiSchema}.sec_cohort_definition
    DROP CONSTRAINT fk_scd_cohort_definition_id,
    DROP CONSTRAINT fk_scd_sec_role_id;
ALTER TABLE ${ohdsiSchema}.sec_cohort_definition
    ADD CONSTRAINT fk_scd_cohort_definition_id
        FOREIGN KEY (cohort_definition_id)
        REFERENCES ${ohdsiSchema}.cohort_definition(id)
        ON DELETE CASCADE,
    ADD CONSTRAINT fk_scd_sec_role_id
        FOREIGN KEY (role_id)
        REFERENCES ${ohdsiSchema}.sec_role(id)
        ON DELETE CASCADE;

-- sec_concept_set
ALTER TABLE ${ohdsiSchema}.sec_concept_set
    DROP CONSTRAINT fk_scs_concept_set_id,
    DROP CONSTRAINT fk_scs_sec_role_id;
ALTER TABLE ${ohdsiSchema}.sec_concept_set
    ADD CONSTRAINT fk_scs_concept_set_id
        FOREIGN KEY (concept_set_id)
        REFERENCES ${ohdsiSchema}.concept_set(concept_set_id)
        ON DELETE CASCADE,
    ADD CONSTRAINT fk_scs_sec_role_id
        FOREIGN KEY (role_id)
        REFERENCES ${ohdsiSchema}.sec_role(id)
        ON DELETE CASCADE;

-- sec_cohort_characterization
ALTER TABLE ${ohdsiSchema}.sec_cohort_characterization
    DROP CONSTRAINT fk_scc_cohort_characterization_id,
    DROP CONSTRAINT fk_scc_sec_role_id;
ALTER TABLE ${ohdsiSchema}.sec_cohort_characterization
    ADD CONSTRAINT fk_scc_cohort_characterization_id
        FOREIGN KEY (cohort_characterization_id)
        REFERENCES ${ohdsiSchema}.cohort_characterization(id)
        ON DELETE CASCADE,
    ADD CONSTRAINT fk_scc_sec_role_id
        FOREIGN KEY (role_id)
        REFERENCES ${ohdsiSchema}.sec_role(id)
        ON DELETE CASCADE;

-- sec_ir_analysis
ALTER TABLE ${ohdsiSchema}.sec_ir_analysis
    DROP CONSTRAINT fk_sia_ir_analysis_id,
    DROP CONSTRAINT fk_sia_sec_role_id;
ALTER TABLE ${ohdsiSchema}.sec_ir_analysis
    ADD CONSTRAINT fk_sia_ir_analysis_id
        FOREIGN KEY (ir_id)
        REFERENCES ${ohdsiSchema}.ir_analysis(id)
        ON DELETE CASCADE,
    ADD CONSTRAINT fk_sia_sec_role_id
        FOREIGN KEY (role_id)
        REFERENCES ${ohdsiSchema}.sec_role(id)
        ON DELETE CASCADE;

-- sec_fe_analysis
ALTER TABLE ${ohdsiSchema}.sec_fe_analysis
    DROP CONSTRAINT fk_sfa_fe_analysis_id,
    DROP CONSTRAINT fk_sfa_sec_role_id;
ALTER TABLE ${ohdsiSchema}.sec_fe_analysis
    ADD CONSTRAINT fk_sfa_fe_analysis_id
        FOREIGN KEY (fe_analysis_id)
        REFERENCES ${ohdsiSchema}.fe_analysis(id)
        ON DELETE CASCADE,
    ADD CONSTRAINT fk_sfa_sec_role_id
        FOREIGN KEY (role_id)
        REFERENCES ${ohdsiSchema}.sec_role(id)
        ON DELETE CASCADE;

-- sec_pathway_analysis
ALTER TABLE ${ohdsiSchema}.sec_pathway_analysis
    DROP CONSTRAINT fk_spa_pathway_analysis_id,
    DROP CONSTRAINT fk_spa_sec_role_id;
ALTER TABLE ${ohdsiSchema}.sec_pathway_analysis
    ADD CONSTRAINT fk_spa_pathway_analysis_id
        FOREIGN KEY (pathway_analysis_id)
        REFERENCES ${ohdsiSchema}.pathway_analysis(id)
        ON DELETE CASCADE,
    ADD CONSTRAINT fk_spa_sec_role_id
        FOREIGN KEY (role_id)
        REFERENCES ${ohdsiSchema}.sec_role(id)
        ON DELETE CASCADE;

-- sec_reusable
ALTER TABLE ${ohdsiSchema}.sec_reusable
    DROP CONSTRAINT fk_sr_reusable_id,
    DROP CONSTRAINT fk_sr_sec_role_id;
ALTER TABLE ${ohdsiSchema}.sec_reusable
    ADD CONSTRAINT fk_sr_reusable_id
        FOREIGN KEY (reusable_id)
        REFERENCES ${ohdsiSchema}.reusable(id)
        ON DELETE CASCADE,
    ADD CONSTRAINT fk_sr_sec_role_id
        FOREIGN KEY (role_id)
        REFERENCES ${ohdsiSchema}.sec_role(id)
        ON DELETE CASCADE;

-- sec_source
ALTER TABLE ${ohdsiSchema}.sec_source
    DROP CONSTRAINT fk_ss_source_id,
    DROP CONSTRAINT fk_ss_sec_role_id;
ALTER TABLE ${ohdsiSchema}.sec_source
    ADD CONSTRAINT fk_ss_source_id
        FOREIGN KEY (source_id)
        REFERENCES ${ohdsiSchema}.source(source_id)
        ON DELETE CASCADE,
    ADD CONSTRAINT fk_ss_sec_role_id
        FOREIGN KEY (role_id)
        REFERENCES ${ohdsiSchema}.sec_role(id)
        ON DELETE CASCADE;
