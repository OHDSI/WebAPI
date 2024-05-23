ALTER TABLE ${ohdsiSchema}.source
  ADD COLUMN check_connection boolean not null DEFAULT true;