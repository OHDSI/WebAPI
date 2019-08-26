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
        resultsDatabaseSchema <- Sys.getenv("RESULT_SCHEMA")
        cohortsDatabaseSchema <- Sys.getenv("TARGET_SCHEMA")
        cohortTable <- Sys.getenv("COHORT_TARGET_TABLE")
        driversPath <- (function(path) if (path == "") NULL else path)( Sys.getenv("JDBC_DRIVER_PATH") )

        connectionDetails <- DatabaseConnector::createConnectionDetails(dbms = dbms,
                                                                        connectionString = connectionString,
                                                                        user = user,
                                                                        password = pwd,
                                                                        pathToDriver = driversPath)

        outputFolder <- file.path(getwd(), 'results')
        dir.create(outputFolder)

        execute(connectionDetails = connectionDetails,
                cdmDatabaseSchema = cdmDatabaseSchema,
                cohortDatabaseSchema = cohortsDatabaseSchema,
                cohortTable = cohortTable,
                outputFolder = outputFolder,
                createCohorts = T,
                runAnalyses = T,
                createValidationPackage = F,
                packageResults = T,
                minCellCount = 5,
                cdmVersion = 5)

        populateShinyApp(shinyDirectory = file.path(getwd(), 'shiny', 'PLPViewer'), resultDirectory = outputFolder)

        # To run PLP Viewer shiny app call:
        # PatientLevelPrediction::viewPlp(readRDS("./shiny/PLPViewer/data/Analysis_1/plpResult.rds"))
}, finally = {
        remove.packages('@packageName')
})