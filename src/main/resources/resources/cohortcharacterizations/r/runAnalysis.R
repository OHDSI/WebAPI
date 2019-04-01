library(devtools)
options(devtools.install.args = "--no-multiarch")

setwd("./")
tryCatch({
    unzip('@packageFile', exdir = file.path(".", "@analysisDir"))
    install_local(file.path(".", "@analysisDir"))
}, finally = {
    unlink('@analysisDir', recursive = TRUE, force = TRUE)
})

library(DatabaseConnector)
library(@packageName)

tryCatch({
        maxCores <- parallel::detectCores()

        dbms <- Sys.getenv("DBMS_TYPE")
        connectionString <- Sys.getenv("CONNECTION_STRING")
        user <- Sys.getenv("DBMS_USERNAME")
        pwd <- Sys.getenv("DBMS_PASSWORD")
        cdmDatabaseSchema <- Sys.getenv("DBMS_SCHEMA")
        cohortsDatabaseSchema <- Sys.getenv("TARGET_SCHEMA")
        tempSchema <- if (is.null(Sys.getenv("TEMP_SCHEMA"))) cohortsDatabaseSchema else Sys.getenv("TEMP_SCHEMA")
        cohortTable <- Sys.getenv("COHORT_TARGET_TABLE")
        driverPath <- Sys.getenv("JDBC_DRIVER_PATH")

        connectionDetails <- DatabaseConnector::createConnectionDetails(dbms = dbms,
                                                                        connectionString = connectionString,
                                                                        user = user,
                                                                        password = pwd,
                                                                        pathToDriver = driverPath)

        runAnalysis(connectionDetails = connectionDetails,
                cohortTable = cohortTable,
                sessionId = "",
                cdmSchema = cdmDatabaseSchema,
                resultsSchema = cohortsDatabaseSchema,
                vocabularySchema = cdmDatabaseSchema,
                tempSchema = tempSchema,
                analysisId = @analysisId,
                outputFolder = file.path('.', 'results'))
}, finally = {
        remove.packages('@packageName')
})