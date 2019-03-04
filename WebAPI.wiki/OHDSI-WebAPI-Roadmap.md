## SHIRO 

Security layer for WebAPI.  
See [WebAPI shiro branch](https://github.com/OHDSI/WebAPI/tree/shiro)

Integration points - REST Methods, OLYMPUS Installation

New Development Pending - User interface for editing security / permission settings
Planned as shiro branch of [ATLAS] (https://github.com/OHDSI/ATLAS)

## Projects / Tags 

Persistence layer and application integration for a way to tag assets such as Concept Sets, Feasibility Studies and Cohorts as belonging to a particular project or area of study.

## Activity / Telemetry

Monitoring usage of a WebAPI installation

## Sharing

Ability to import and export assets across WebAPI installations.  Assets include cohort definitions, concept set expressions and aggregate analysis results.

## CHARON Branch 
Connecting Health Analytics in R for OHDSI Network ([greek reference](https://en.wikipedia.org/wiki/Charon_(mythology)))

R Service Bus
Integration of Open Analytics' [R Service Bus](http://www.openanalytics.eu/r-service-bus)

There are several R packages being developed within the OHDSI community.   ([Patient Level Prediction](https://github.com/OHDSI/PatientLevelPrediction) and [Cohort Method] (https://github.com/OHDSI/CohortMethod) are two good examples)  While these packages can be run independent of any other software requirements the intent of the OHDSI community is to make these sophisticated algorithms accessible to a broader audience.  To accomplish this we intend to establish a framework of tools that allow users to leverage a web based interface to perform analyses supported by the R packages contributed to the efforts of the OHDSI community.

The integration of the Open Analytics R Service Bus will provide an asychronous way to launch long running R analyses.  Calls to the WebAPI will kick off these analyses using the existing Job Manager features of the WebAPI.  This will allow interfaces such as [ATLAS] (https://github.com/OHDSI/ATLAS) to develop user experiences that take advantage of the ongoing R method development.

## Person Services

Provide a set of services that expose person level details for use in person profile visualization and reporting.  Prerequisite: SHIRO

## Completed
9/20/2015 - Concept Set Persistence - [Merged] (https://github.com/OHDSI/WebAPI/pull/57) 