select ResultKey ,ResultCount 
from Result 
where ReportId = @id and ResultCount>1
