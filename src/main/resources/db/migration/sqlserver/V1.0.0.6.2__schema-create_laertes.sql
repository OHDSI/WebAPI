-- Authors: Richard D Boyce, Erica Voss, Lee Evans
-- 2014/2015
-- Postgresql script

ALTER TABLE EVIDENCE_SOURCES ADD coverage_start_date date;
ALTER TABLE EVIDENCE_SOURCES ADD coverage_end_date date;

ALTER TABLE LAERTES_SUMMARY DROP COLUMN eb05;
ALTER TABLE LAERTES_SUMMARY DROP COLUMN ebgm;
ALTER TABLE LAERTES_SUMMARY ADD prr numeric;
