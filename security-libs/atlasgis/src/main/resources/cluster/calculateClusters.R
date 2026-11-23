library(DatabaseConnector)
library(geojson) # required for "as.geojson"
library(sp)
library(SqlRender)
library(dplyr)

dbms <- Sys.getenv("DBMS_TYPE")
connectionString <- Sys.getenv("CONNECTION_STRING")
user <- Sys.getenv("DBMS_USERNAME")
pwd <- Sys.getenv("DBMS_PASSWORD")
cdmSchema <- Sys.getenv("DBMS_SCHEMA")
resultSchema <- Sys.getenv("RESULT_SCHEMA")
driversPath <- (function(path) if (path == "") NULL else path)( Sys.getenv("JDBC_DRIVER_PATH") )

connectionDetails <- DatabaseConnector::createConnectionDetails(
  dbms=dbms,
  connectionString=connectionString,
  user=user,
  password=pwd,
  pathToDriver = driversPath
)

sql <- SqlRender::readSql("getLocation.sql")
sql <- SqlRender::render(sql, resultSchema = resultSchema, cdmSchema = cdmSchema)
sql <- SqlRender::translate(sql, connectionDetails$dbms)

con <- DatabaseConnector::connect(connectionDetails)
res <- DatabaseConnector::querySql(con, sql)
disconnect(con)

clusterCnt <- 10

if (nrow(res) == 0) { # Empty geojson when no points found
  sp <- '{"type": "FeatureCollection", "features": []}'
} else if (clusterCnt < nrow(res)) {
    # kmeans uses random number generator so seed must be reseted each time to get constant results
    # for the same input dataset between calls
    set.seed(1)
    clusters <- kmeans(res[,c('LONGITUDE', 'LATITUDE')], clusterCnt)

    centersWithSubjectIds <- left_join(
        as.data.frame(clusters$centers),
        res[clusters$cluster %in% seq(1,clusterCnt)[clusters$size==1], ],
        by = c('LONGITUDE' = 'LONGITUDE', 'LATITUDE' = 'LATITUDE')
    )
    size <- clusters$size

    sp <- SpatialPointsDataFrame(
        centersWithSubjectIds[,c('LONGITUDE', 'LATITUDE')],
        data.frame(size, subject_id = centersWithSubjectIds[,c('SUBJECT_ID')])
    )
} else {
    sp <- SpatialPointsDataFrame(
        res[,c('LONGITUDE', 'LATITUDE')],
        data.frame(subject_id = res[, c('SUBJECT_ID')], size = 1)
    )
}

cat(as.geojson(sp), file = "clusters.json")
