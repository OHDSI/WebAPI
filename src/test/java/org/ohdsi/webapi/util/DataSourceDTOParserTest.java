package org.ohdsi.webapi.util;

import com.odysseusinc.arachne.commons.types.DBMSType;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.DataSourceUnsecuredDTO;
import org.junit.Test;
import org.ohdsi.webapi.source.Source;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;

public class DataSourceDTOParserTest {

    public static final String PGSQL_CONN_STR = "jdbc:postgresql://localhost:5432/ohdsi?ssl=true&user=user&password=secret";
    public static final String PGSQL_WO_PWD_CONN_STR = "jdbc:postgresql://localhost:5432/ohdsi?ssl=true";
    public static final String MSSQL_CONN_STR = "jdbc:sqlserver://localhost:1433;databaseName=ohdsi;user=msuser;password=password";
    public static final String PDW_CONN_STR = "jdbc:sqlserver://yourserver.database.windows.net:1433;database=yourdatabase;user=pdw_user;password=pdw_password;" +
            "encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";
    public static final String REDSHIFT_CONN_STR = "jdbc:redshift://examplecluster.abc123xyz789.us-west-2.redshift.amazonaws.com:5439/" +
            "dev?ssl=true&UID=your_username&PWD=your_password";
    public static final String NETEZZA_CONN_STR = "jdbc:netezza://main:5490/sales;user=admin;password=password;loglevel=2;logdirpath=C:\\";
    public static final String IMPALA_AUTH3_CONN_STR = "jdbc:impala://node1.example.com:18000/default2;AuthMech=3;UID=cloudera;PWD=cloudera";
    public static final String IMPALA_AUTH0_CONN_STR = "jdbc:impala://localhost:21050;AuthMech=0;";
    public static final String IMPALA_AUTH1_CONN_STR = "jdbc:impala://node1.example.com:21050;AuthMech=1;" +
            "KrbRealm=EXAMPLE.COM;KrbHostFQDN=node1.example.com;KrbServiceName=impala";
    private static final String BIGQUERY_CONN_STR = "jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443;" +
            "ProjectId=MyBigQueryProject;OAuthType=0;OAuthServiceAcctEmail==bqtest1@data-driver-testing.iam.gserviceaccount.com;" +
            "OAuthPvtKeyPath=C:\\SecureFiles\\ServiceKeyFile.p12;";
    public static final String ORACLE_WO_PWD_CONN_STR = "jdbc:oracle:thin:@myhost:1521:orcl";
    public static final String ORACLE_WITH_PWD_CONN_STR = "jdbc:oracle:thin:scott/tiger@myhost:1521:orcl";
    public static final String HIVE_CONN_STR = "jdbc:hive2://sandbox-hdp.hortonworks.com:2181/synpuf_531_orc;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2";

    @Test
    public void parseDTO() {
        DataSourceUnsecuredDTO dto;
        dto = DataSourceDTOParser.parseDTO(getPostgreSQLSource());
        assertThat(dto.getType(), is(DBMSType.POSTGRESQL));
        assertThat(dto.getConnectionString(), is(PGSQL_CONN_STR));
        assertThat(dto.getUsername(), is("user"));
        assertThat(dto.getPassword(), is("secret"));

        dto = DataSourceDTOParser.parseDTO(getPostgreSQLPasswordSource());
        assertThat(dto.getType(), is(DBMSType.POSTGRESQL));
        assertThat(dto.getConnectionString(), is(PGSQL_WO_PWD_CONN_STR));
        assertThat(dto.getUsername(), is("user"));
        assertThat(dto.getPassword(), is("password"));

        dto = DataSourceDTOParser.parseDTO(getMSSQLSource());
        assertThat(dto.getType(), is(DBMSType.MS_SQL_SERVER));
        assertThat(dto.getConnectionString(), is((MSSQL_CONN_STR)));
        assertThat(dto.getUsername(), is("msuser"));
        assertThat(dto.getPassword(), is("password"));

        dto = DataSourceDTOParser.parseDTO(getPDWSource());
        assertThat(dto.getType(), is(DBMSType.PDW));
        assertThat(dto.getConnectionString(), is(PDW_CONN_STR));
        assertThat(dto.getUsername(), is("pdw_user"));
        assertThat(dto.getPassword(), is("pdw_password"));

        dto = DataSourceDTOParser.parseDTO(getRedshiftSource());
        assertThat(dto.getType(), is(DBMSType.REDSHIFT));
        assertThat(dto.getConnectionString(), is(REDSHIFT_CONN_STR));
        assertThat(dto.getUsername(), is("your_username"));
        assertThat(dto.getPassword(), is("your_password"));

        dto = DataSourceDTOParser.parseDTO(getNetezzaSource());
        assertThat(dto.getType(), is(DBMSType.NETEZZA));
        assertThat(dto.getConnectionString(), is(NETEZZA_CONN_STR));
        assertThat(dto.getUsername(), is("admin"));
        assertThat(dto.getPassword(), is("password"));

        dto = DataSourceDTOParser.parseDTO(getImpalaAuth3Source());
        assertThat(dto.getType(), is(DBMSType.IMPALA));
        assertThat(dto.getConnectionString(), is(IMPALA_AUTH3_CONN_STR));
        assertThat(dto.getUsername(), is("cloudera"));
        assertThat(dto.getPassword(), is("cloudera"));

        dto = DataSourceDTOParser.parseDTO(getImpalaAuth0Source());
        assertThat(dto.getType(), is(DBMSType.IMPALA));
        assertThat(dto.getConnectionString(), is(IMPALA_AUTH0_CONN_STR));
        assertThat(dto.getUsername(), is(nullValue()));
        assertThat(dto.getPassword(), is(nullValue()));

        dto = DataSourceDTOParser.parseDTO(getImpalaAuth1Source());
        assertThat(dto.getType(), is(DBMSType.IMPALA));
        assertThat(dto.getConnectionString(), is(IMPALA_AUTH1_CONN_STR));
        assertThat(dto.getUsername(), is(nullValue()));
        assertThat(dto.getPassword(), is(nullValue()));

        dto = DataSourceDTOParser.parseDTO(getBigQuerySource());
        assertThat(dto.getType(), is(DBMSType.BIGQUERY));
        assertThat(dto.getConnectionString(), is(BIGQUERY_CONN_STR));
        assertThat(dto.getUsername(), is(nullValue()));
        assertThat(dto.getPassword(), is(nullValue()));

        dto = DataSourceDTOParser.parseDTO(getOracleWOPwdSource());
        assertThat(dto.getType(), is(DBMSType.ORACLE));
        assertThat(dto.getConnectionString(), is(ORACLE_WO_PWD_CONN_STR));
        assertThat(dto.getUsername(), is(nullValue()));
        assertThat(dto.getPassword(), is(nullValue()));

        dto = DataSourceDTOParser.parseDTO(getOracleWithPwdSource());
        assertThat(dto.getType(), is(DBMSType.ORACLE));
        assertThat(dto.getConnectionString(), is(ORACLE_WITH_PWD_CONN_STR));
        assertThat(dto.getUsername(), is("scott"));
        assertThat(dto.getPassword(), is("tiger"));

        dto = DataSourceDTOParser.parseDTO(getHiveSource());
        assertThat(dto.getType(), is(DBMSType.HIVE));
        assertThat(dto.getConnectionString(), is(HIVE_CONN_STR));
        assertThat(dto.getUsername(), is(nullValue()));
        assertThat(dto.getPassword(), is(nullValue()));
    }

    private Source getPostgreSQLPasswordSource() {
        Source source = new Source();
        source.setSourceDialect("postgresql");
        source.setSourceConnection(PGSQL_WO_PWD_CONN_STR);
        source.setUsername("user");
        source.setPassword("password");
        return source;
    }

    private Source getOracleWithPwdSource() {
        Source source = new Source();
        source.setSourceDialect("oracle");
        source.setSourceConnection(ORACLE_WITH_PWD_CONN_STR);
        return source;
    }

    private Source getOracleWOPwdSource() {
        Source source = new Source();
        source.setSourceDialect("oracle");
        source.setSourceConnection(ORACLE_WO_PWD_CONN_STR);
        return source;
    }

    private Source getBigQuerySource() {
        Source source = new Source();
        source.setSourceDialect("bigquery");
        source.setSourceConnection(BIGQUERY_CONN_STR);
        return source;
    }

    private Source getImpalaAuth1Source() {
        Source source = new Source();
        source.setSourceDialect("impala");
        source.setSourceConnection(IMPALA_AUTH1_CONN_STR);
        return source;
    }

    private Source getImpalaAuth0Source() {
        Source source = new Source();
        source.setSourceDialect("impala");
        source.setSourceConnection(IMPALA_AUTH0_CONN_STR);
        return source;
    }

    private Source getImpalaAuth3Source() {
        Source source = new Source();
        source.setSourceDialect("impala");
        source.setSourceConnection(IMPALA_AUTH3_CONN_STR);
        return source;
    }

    private Source getNetezzaSource() {
        Source source = new Source();
        source.setSourceDialect("netezza");
        source.setSourceConnection(NETEZZA_CONN_STR);
        return source;
    }

    private Source getRedshiftSource() {
        Source source = new Source();
        source.setSourceDialect("redshift");
        source.setSourceConnection(REDSHIFT_CONN_STR);
        return source;
    }

    private Source getPDWSource() {
        Source source = new Source();
        source.setSourceDialect("pdw");
        source.setSourceConnection(PDW_CONN_STR);
        return source;
    }

    private Source getMSSQLSource() {
        Source source = new Source();
        source.setSourceDialect("sql server");
        source.setSourceConnection(MSSQL_CONN_STR);
        return source;
    }

    private Source getPostgreSQLSource() {
        Source source = new Source();
        source.setSourceDialect("postgresql");
        source.setSourceConnection(PGSQL_CONN_STR);
        return source;
    }

    private Source getHiveSource() {
        Source source = new Source();
        source.setSourceDialect("hive");
        source.setSourceConnection(HIVE_CONN_STR);
        return source;
    }
}