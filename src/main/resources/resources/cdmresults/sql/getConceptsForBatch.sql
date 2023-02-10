select group_id, min(concept_id) as min_concept, max(concept_id) as max_concept FROM
(
	select concept_id, ordinal / @batch_size as group_id 
  FROM ( 
		select concept_id, row_number() over (order by concept_id) as ordinal
		FROM @resultTableQualifier.achilles_result_concept_count c
	) Q
) G
group by group_id