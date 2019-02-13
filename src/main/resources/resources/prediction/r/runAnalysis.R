library(devtools)

setwd("./")
unzip('@packageFile', exdir = file.path(".", "@analysisDir"))
install_local(file.path(".", "@analysisDir"))
unlink('@analysisDir', recursive = TRUE, force = TRUE)

library(DatabaseConnector)
library(@packageName)

dbms <- Sys.getenv("DBMS_TYPE")
connectionString <- Sys.getenv("CONNECTION_STRING")
user <- Sys.getenv("DBMS_USERNAME")
pwd <- Sys.getenv("DBMS_PASSWORD")
cdmDatabaseSchema <- Sys.getenv("DBMS_SCHEMA")
resultsDatabaseSchema <- Sys.getenv("RESULT_SCHEMA")
cohortsDatabaseSchema <- Sys.getenv("TARGET_SCHEMA")
cohortTable <- Sys.getenv("COHORT_TARGET_TABLE")

connectionDetails <- DatabaseConnector::createConnectionDetails(dbms = dbms,
                                                                connectionString = connectionString,
                                                                user = user,
                                                                password = pwd)

execute(connectionDetails = connectionDetails,
        cdmDatabaseSchema = cdmDatabaseSchema,
        cohortDatabaseSchema = cohortsDatabaseSchema,
        cohortTable = cohortTable,
        oracleTempSchema = tempDatabaseSchema,
        outputFolder = file.path('.', 'results'),
        createCohorts = T,
        runAnalyses = T,
        createValidationPackage = F,
        packageResults = T,
        minCellCount = 5,
        cdmVersion = 5)
