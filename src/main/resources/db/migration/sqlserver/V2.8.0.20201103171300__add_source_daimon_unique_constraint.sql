delete sd1 from ${ohdsiSchema}.source_daimon sd1
where sd1.priority = -1
  and sd1.source_daimon_id < (
    select max(source_daimon_id)
    from ${ohdsiSchema}.source_daimon sd2
    where sd1.source_id = sd2.source_id
      and sd1.daimon_type = sd2.daimon_type);

ALTER TABLE ${ohdsiSchema}.source_daimon ADD CONSTRAINT un_source_daimon UNIQUE (source_id,daimon_type);