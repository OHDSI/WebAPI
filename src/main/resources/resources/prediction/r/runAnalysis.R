setwd("./")
tryCatch({
  unzip('@packageFile', exdir = file.path(".", "@analysisDir"))
  callr::rcmd("build", c("@analysisDir", c("--no-build-vignettes")), echo = TRUE, show = TRUE)
  pkg_file <- list.files(path = ".", pattern = "\\.tar\\.gz")[1]
  tryCatch({
    install.packages(pkg_file, repos = NULL, type="source", INSTALL_opts=c("--no-multiarch"))
  }, finally = {
    file.remove(pkg_file)
  })
}, finally = {
  unlink('@analysisDir', recursive = TRUE, force = TRUE)
})

library(DatabaseConnector)
library(@packageName)

tryCatch({
        dbms <- Sys.getenv("DBMS_TYPE")
        connectionString <- Sys.getenv("CONNECTION_STRING")
        user <- Sys.getenv("DBMS_USERNAME")
        pwd <- Sys.getenv("DBMS_PASSWORD")
        cdmDatabaseSchema <- Sys.getenv("DBMS_SCHEMA")
        cdmDatabaseName <- Sys.getenv("DATA_SOURCE_NAME")
        resultsDatabaseSchema <- Sys.getenv("RESULT_SCHEMA")
        cohortDatabaseSchema <- Sys.getenv("TARGET_SCHEMA")
        cohortTable <- Sys.getenv("COHORT_TARGET_TABLE")
        driversPath <- (function(path) if (path == "") NULL else path)( Sys.getenv("JDBC_DRIVER_PATH") )

        connectionDetails <- DatabaseConnector::createConnectionDetails(dbms = dbms,
                                                                        connectionString = connectionString,
                                                                        user = user,
                                                                        password = pwd,
                                                                        pathToDriver = driversPath)

        databaseDetails <- PatientLevelPrediction::createDatabaseDetails(connectionDetails = connectionDetails,
                                                                         cdmDatabaseSchema = cdmDatabaseSchema,
                                                                         cdmDatabaseName = cdmDatabaseName,
                                                                         cohortDatabaseSchema = cohortDatabaseSchema,
                                                                         cohortTable = cohortTable,
                                                                         outcomeDatabaseSchema = cohortDatabaseSchema,
                                                                         outcomeTable = cohortTable,
                                                                         cdmVersion = 5)

        logSettings <- PatientLevelPrediction::createLogSettings(verbosity = "INFO",
                                                                 timeStamp = T,
                                                                 logName = 'skeletonPlp')

        # Evaluating can't use global environment in child threads
        connectionDetails$user <- function() Sys.getenv("DBMS_USERNAME")
        connectionDetails$password <- function() Sys.getenv("DBMS_PASSWORD")
        connectionDetails$connectionString <- function() Sys.getenv("CONNECTION_STRING")

        outputFolder <- file.path(getwd(), 'results')
        dir.create(outputFolder)

        PatientLevelPrediction::setPythonEnvironment(envname = 'PLP', envtype = 'conda')
        execute(databaseDetails = databaseDetails,
                outputFolder = outputFolder,
                createCohorts = T,
                runAnalyses = T,
                createValidationPackage = F,
                packageResults = T,
                minCellCount = 5,
                viewShiny = T,
                logSettings = logSettings)
        # To run PLP Viewer shiny app call:
        # PatientLevelPrediction::viewMultiplePlp(outputFolder)
}, finally = {
        remove.packages('@packageName')
})
