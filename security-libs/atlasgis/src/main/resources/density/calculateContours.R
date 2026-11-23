library(DatabaseConnector)
library(maptools)
library(raster)
library(rgeos) # required for "gBuffer"
library(geojson) # required for "as.geojson"
# https://vita.had.co.nz/papers/density-estimation.pdf
library(KernSmooth)
library(SqlRender)

# https://github.com/tidyverse/ggplot2/issues/2534
# https://stackoverflow.com/questions/14379828/how-does-one-turn-contour-lines-into-filled-contours

raster2contourPolys <- function(r, levels = NULL) {

  ## set-up levels
  levels <- sort(levels)
  plevels <- c(min(values(r), na.rm=TRUE), levels, max(values(r), na.rm=TRUE)) # pad with raster range
  llevels <- paste(plevels[-length(plevels)], plevels[-1], sep=" - ")
  llevels[1] <- paste("<", min(levels))
  llevels[length(llevels)] <- paste(">", max(levels))

  ## convert raster object to matrix so it can be fed into contourLines
  xmin <- extent(r)@xmin
  xmax <- extent(r)@xmax
  ymin <- extent(r)@ymin
  ymax <- extent(r)@ymax
  rx <- seq(xmin, xmax, length.out=ncol(r))
  ry <- seq(ymin, ymax, length.out=nrow(r))
  rz <- t(as.matrix(r))
  rz <- rz[,ncol(rz):1] # reshape

  ## get contour lines and convert to SpatialLinesDataFrame
  cat("Converting to contour lines...\n")
  cl <- contourLines(rx,ry,rz,levels=levels)
  cl <- ContourLines2SLDF(cl)

  ## extract coordinates to generate overall boundary polygon
  xy <- coordinates(r)[which(!is.na(values(r))),]
  i <- chull(xy)
  b <- xy[c(i,i[1]),]
  b <- SpatialPolygons(list(Polygons(list(Polygon(b, hole = FALSE)), "1")))

  ## add buffer around lines and cut boundary polygon
  cat("Converting contour lines to polygons...\n")
  bcl <- gBuffer(cl, width = 0.0001) # add small buffer so it cuts bounding poly
  cp <- gDifference(b, bcl)

  ## restructure and make polygon number the ID
  polys <- list()
  for(j in seq_along(cp@polygons[[1]]@Polygons)) {
    polys[[j]] <- Polygons(list(cp@polygons[[1]]@Polygons[[j]]),j)
  }
  cp <- SpatialPolygons(polys)
  cp <- SpatialPolygonsDataFrame(cp, data.frame(id=seq_along(cp)))

  ## cut the raster by levels
  rc <- cut(r, breaks=plevels)

  ## loop through each polygon, create internal buffer, select points and define overlap with raster
  cat("Adding attributes to polygons...\n")
  l <- character(length(cp))
  for(j in seq_along(cp)) {
    p <- cp[cp$id==j,]
    bp <- gBuffer(p, width = -max(res(r))) # use a negative buffer to obtain internal points
    if(!is.null(bp)) {
      xy <- SpatialPoints(coordinates(bp@polygons[[1]]@Polygons[[1]]))[1]
      l[j] <- llevels[extract(rc,xy)]
    }
    else {
      xy <- coordinates(gCentroid(p)) # buffer will not be calculated for smaller polygons, so grab centroid
      l[j] <- llevels[extract(rc,xy)]
    }
  }

  ## assign level to each polygon
  cp$level <- factor(l, levels=llevels)
  cp$min <- plevels[-length(plevels)][cp$level]
  cp$max <- plevels[-1][cp$level]
  cp <- cp[!is.na(cp$level),] # discard small polygons that did not capture a raster point
  df <- unique(cp@data[,c("level","min","max")]) # to be used after holes are defined
  df <- df[order(df$min),]
  row.names(df) <- df$level
  llevels <- df$level

  ## define depressions in higher levels (ie holes)
  cat("Defining holes...\n")
  spolys <- list()
  p <- cp[cp$level==llevels[1],] # add deepest layer
  p <- gUnaryUnion(p)
  spolys[[1]] <- Polygons(p@polygons[[1]]@Polygons, ID=llevels[1])
  for(i in seq(length(llevels)-1)) {
    p1 <- cp[cp$level==llevels[i+1],] # upper layer
    p2 <- cp[cp$level==llevels[i],] # lower layer
    x <- numeric(length(p2)) # grab one point from each of the deeper polygons
    y <- numeric(length(p2))
    id <- numeric(length(p2))
    for(j in seq_along(p2)) {
      xy <- coordinates(p2@polygons[[j]]@Polygons[[1]])[1,]
      x[j] <- xy[1]; y[j] <- xy[2]
      id[j] <- as.numeric(p2@polygons[[j]]@ID)
    }
    xy <- SpatialPointsDataFrame(cbind(x,y), data.frame(id=id))
    holes <- over(xy, p1)$id
    holes <- xy$id[which(!is.na(holes))]
    if(length(holes)>0) {
      p2 <- p2[p2$id %in% holes,] # keep the polygons over the shallower polygon
      p1 <- gUnaryUnion(p1) # simplify each group of polygons
      p2 <- gUnaryUnion(p2)
      p <- gDifference(p1, p2) # cut holes in p1
    } else { p <- gUnaryUnion(p1) }
    spolys[[i+1]] <- Polygons(p@polygons[[1]]@Polygons, ID=llevels[i+1]) # add level
  }
  cp <- SpatialPolygons(spolys, pO=seq_along(llevels), proj4string=CRS(proj4string(r))) # compile into final object
  cp <- SpatialPolygonsDataFrame(cp, df)
  cat("Done!")
  cp
}

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

# fit <- kde2d(res$lon, res$lat, h = 0.015, n = 100)

bandwidth <- 0.005
if (nrow(res) == 0) { # Empty geojson when no points found
  cp <- '{"type": "FeatureCollection", "features": []}'
} else {
  fit <- bkde2D(res[,c('LONGITUDE', 'LATITUDE')], bandwidth = bandwidth, gridsize = c(250, 250))
  fit$x <- fit$x1
  fit$y <- fit$x2
  fit$z <- fit$fhat * bandwidth

  levels <- c(0.00000001, 0.1, 10, 100, 500, 1000)

  r <- raster(fit)
  cp <- raster2contourPolys(r, levels)
}

cat(as.geojson(cp), file = "geo.json")

# lines <- contourLines(
#   fit$x,
#   fit$y,
#   fit$z,
#   nlevels = length(levels),
#   levels = levels
# )

# linesShp <- ContourLines2SLDF(lines)

# gjson <- geojson_json(linesShp)
# geojson_write(gjson, file = "geo.json")
