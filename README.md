###WebAPI
OHDSI WebAPI contains all OHDSI services that can be called from OHDSI applications

NOTE: Check license information for individual sources on the [Web API documentation page](http://www.ohdsi.org/web/wiki/doku.php?id=documentation:software:webapi)

#### Getting Started

New documentation can be found a the [Web API Installation Guide](http://www.ohdsi.org/web/wiki/doku.php?id=documentation:software:webapi:webapi_installation_guide)

##### Compilation
Compiling the WebAPI project will require Maven.  Maven is a command line tool that will build the WAR for deployment to a servlet container.

##### JDBC Drivers
JDBC Drivers are not included with the source or any release packages.  Obtaining JDBC Drivers and making them available on the hosting server via environment classpath or web server configuration are left as an excercise for the developer / system administrator.  

##### Configuration
Once the source code is built and the resulting libraries are deployed to your web environment the **web.xml** file needs to be updated for your specific environment

**database.driver** : this parameter specifies the class name of the driver for your database environment *(ie com.microsoft.sqlserver.jdbc.SQLServerDriver)*

**database.url** : this parameter specifies the connection string for your database environment

**database.dialect** this parameter specifies the dialect of your database environment and is used by the SQLRender library to translate the embedded templated queries to one of the supported dialects (SQL Server, Oracle, PostgreSQL, Amazon RedShift)

##### Testing
Once your configuration is completed you can test the functionality of the WebAPI with the following types of requests:

* retrieve concept information for concept id 0
```
http://<YOUR SERVER>/WebAPI/vocabulary/concept/0
```
* find related concepts for concept id 0
```
http://<YOUR SERVER>/WebAPI/vocabulary/concept/0/related
```
* search the vocabulary for all concepts with the string cardiomyopathy in the concept name
```
http://<YOUR SERVER>/WebAPI/vocabulary/search/cardiomyopathy
```

#### Services
The collection of RESTful services available in the WebAPI project.

##### VocabularyService
A RESTful service for querying the CDM Vocabulary.  Leveraged by [HERMES](https://github.com/OHDSI/Hermes).

##### SqlRenderService
A RESTful service that wraps the SQLRender project.

##### JobService
A RESTful service that returns JobInstanceResource or JobExecutionResource objects.  Typically a service will launch/queue a job and will be given JobExecutionResource.  This object will encapsulate the Job's id and Job's executionId, start/end times, and status.

The JobService can be used to check on the status of a Job's execution as well as query information of a Job.

/job/{jobId} - returns JobInstanceResource from which you can obtain Job id and Job name.

/job/{jobId}/execution/{executionId} - return JobExecutionResource from which you can obtain JobInstanceResource information as well as the start/end times, status, etc.

/job/execution/{executionId} - is an alternative to the previous endpoint.

See Jobs below for more detail.

#### Jobs
Services within WebAPI may submit asynchronous jobs.  WebAPI uses Spring Batch for this "job server".  Spring Batch requires a few DB objects and will attempt to create these (tables, sequences) upon startup.  Spring Batch will review the DataSource to determine the vendor-specific script to execute.
You may review DDL for your specific RDBMS vendor here: https://github.com/spring-projects/spring-batch/blob/3.0.3.RELEASE/spring-batch-core/src/main/resources/org/springframework/batch/core/ 

Services should use the org.ohdsi.webapi.JobTemplate to launch Jobs.  

See org.ohdsi.webapi.exampleapplication.ExampleApplicationConfig & ExampleApplicationWithJobService for how to submit jobs.

See JobServiceIT (integration test) for more detail (java client).

