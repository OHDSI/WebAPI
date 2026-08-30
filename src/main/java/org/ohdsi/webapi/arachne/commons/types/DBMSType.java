/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Company: Odysseus Data Services, Inc.
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Mikhail Mironov
 * Created: January 31, 2017
 *
 */

package org.ohdsi.webapi.arachne.commons.types;

public enum DBMSType {
    // Set of databases both supported by OHDSI/SqlRender and OHDSI/DatabaseConnector
    POSTGRESQL("PostgreSQL", "postgresql"),
    MS_SQL_SERVER("MS SQL Server", "sql server"),
    PDW("SQL Server Parallel Data Warehouse", "pdw"),
    REDSHIFT("Redshift", "redshift"),
    ORACLE("Oracle", "oracle"),
    IMPALA("Impala", "impala"),
    BIGQUERY("Google BigQuery", "bigquery"),
    NETEZZA("Netezza", "netezza"),
    HIVE("Apache Hive", "hive"),
    SPARK("Spark", "spark"),
    SNOWFLAKE("Snowflake", "snowflake"),
    SYNAPSE("Azure Synapse", "synapse");

    private String label;
    // For further pass into SqlRender.translateSql as "targetDialect" and DatabaseConnector as "dbms"
    private String ohdsiDB;

    DBMSType(String label, String ohdsiDB) {

        this.label = label;
        this.ohdsiDB = ohdsiDB;
    }

    public String getValue() {

        return this.toString();
    }

    public String getLabel() {

        return label;
    }

    public String getOhdsiDB() {

        return ohdsiDB;
    }

    public void setOhdsiDB(String ohdsiDB) {

        this.ohdsiDB = ohdsiDB;
    }
}
