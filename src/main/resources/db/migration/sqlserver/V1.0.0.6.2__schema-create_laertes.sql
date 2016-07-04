-- Authors: Richard D Boyce, Erica Voss, Lee Evans
-- 2014/2015
-- Postgresql script

ALTER TABLE ${ohdsiSchema}.EVIDENCE_SOURCES ADD coverage_start_date date;
ALTER TABLE ${ohdsiSchema}.EVIDENCE_SOURCES ADD coverage_end_date date;

ALTER TABLE ${ohdsiSchema}.LAERTES_SUMMARY DROP COLUMN eb05;
ALTER TABLE ${ohdsiSchema}.LAERTES_SUMMARY DROP COLUMN ebgm;
ALTER TABLE ${ohdsiSchema}.LAERTES_SUMMARY ADD prr numeric;
