select analysis_id
FROM  @tableQualifier.cohort_analysis_list_xref
WHERE cohort_id = @cohort_definition_id and source_id = @source_id
