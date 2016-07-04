-- Authors: Richard D Boyce, Erica Voss, Lee Evans
-- 2014/2015
-- sql server script

ALTER TABLE ${ohdsiSchema}.DRUG_HOI_EVIDENCE ALTER COLUMN statistic_value numeric(19,5) NULL;
