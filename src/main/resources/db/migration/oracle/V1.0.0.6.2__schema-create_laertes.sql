-- Authors: Richard D Boyce, Erica Voss, Lee Evans
-- 2014/2015
-- Postgresql script

ALTER TABLE EVIDENCE_SOURCES ADD coverage_start_date date;
ALTER TABLE EVIDENCE_SOURCES ADD coverage_end_date date;
COMMENT ON COLUMN evidence_sources.coverage_start_date IS 'The start date of coverage for the resource. Data can be trusted on or after this date and up to and including the coverage_end_date';
COMMENT ON COLUMN evidence_sources.coverage_end_date IS 'The date of coverage for the resource. Data can be trusted on or after the coverage_start_date date and up to and including this date';


ALTER TABLE LAERTES_SUMMARY DROP eb05;
ALTER TABLE LAERTES_SUMMARY DROP ebgm;
ALTER TABLE LAERTES_SUMMARY ADD prr numeric;
