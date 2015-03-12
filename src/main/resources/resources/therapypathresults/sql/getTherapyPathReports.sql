select ReportId, ReportCaption, [Year], Disease, DataSource
from @OHDSI_schema.Report
order by DataSource, Disease, [Year]
