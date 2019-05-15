select DOMAIN_ID, DOMAIN_NAME, DOMAIN_CONCEPT_ID 
from @CDM_schema.domain
order by DOMAIN_NAME asc
