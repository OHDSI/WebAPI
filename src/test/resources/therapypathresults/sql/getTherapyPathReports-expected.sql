select ReportId, ReportCaption, [Year], Disease, DataSource
from result_schema.Report
order by DataSource, Disease, [Year]
