select
	file_contents
from
	@webapi_schema.output_file_contents
where
	output_file_id = @id;
