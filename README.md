###WebAPI
OHDSI WebAPI contains all OHDSI services that can be called from OHDSI applications
#### Getting Started
##### Compilation
Compiling the WebAPI project will require Maven.  Any Maven compliant IDE will be able to resolve all dependencies and compile the project.

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











