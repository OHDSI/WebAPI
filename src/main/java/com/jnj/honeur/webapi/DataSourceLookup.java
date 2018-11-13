package com.jnj.honeur.webapi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.jnj.honeur.webapi.source.SourceDaimonContext;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.lookup.DataSourceLookupFailureException;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;


/**
 * Manages all application data sources
 * Hold the primary data source
 * Creates one data source for each Source / SourceDaimon
 *
 * @author Peter Moorthamer
 * Date: 02/feb/2018
 */
@Component
public class DataSourceLookup implements org.springframework.jdbc.datasource.lookup.DataSourceLookup {

    private static final Log LOG = LogFactory.getLog(DataSourceLookup.class);

    public static final String PRIMARY_DATA_SOURCE_KEY = "webapi";

    private final Map<String, String> databaseDriverMapping = new HashMap<>();
    private final Map<SourceDaimonContext, DataSource> dataSourceMap = new HashMap<>();

    @Autowired
    private DataSource primaryDataSource;

    public DataSourceLookup() {
        databaseDriverMapping.put("postgresql", "org.postgresql.Driver");
        databaseDriverMapping.put("oracle", "oracle.jdbc.driver.OracleDriver");
        databaseDriverMapping.put("sql server", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        databaseDriverMapping.put("redshift", "com.amazon.redshift.jdbc.Driver");
        databaseDriverMapping.put("impala", "com.cloudera.impala.jdbc4.Driver");
    }

    public void initDataSources(final Iterable<Source> sources) {
        LOG.debug("initDataSources");
        dataSourceMap.clear();

        for(Source source:sources) {
            for(SourceDaimon sourceDaimon: source.getDaimons()) {
                final DataSource dataSource = createDataSource(source, sourceDaimon.getDaimonType());
                dataSourceMap.put(new SourceDaimonContext(source.getSourceKey(), sourceDaimon.getDaimonType()), dataSource);
            }
         }
    }

    private DataSource createDataSource(Source source, SourceDaimon.DaimonType daimonType) {
        LOG.debug(String.format("Create datasource for source '%s' and daimon type '%s'", source.getSourceKey(), daimonType.name()));
        final DriverManagerDataSource ds = new DriverManagerDataSource(source.getSourceConnection());
        ds.setDriverClassName(databaseDriverMapping.get(source.getSourceDialect()));
        ds.setSchema(source.getTableQualifier(daimonType));

        return ds;
    }

    private DataSource getDataSource(SourceDaimonContext sourceDaimonContext) {
        final DataSource dataSource = dataSourceMap.get(sourceDaimonContext);
        if(dataSource != null) {
            return dataSource;
        }
        return getPrimaryDataSource();
    }

    @Override
    public DataSource getDataSource(final String tenantIdentifier) throws DataSourceLookupFailureException {
        LOG.debug(String.format("getDataSource %s", tenantIdentifier));
        if(PRIMARY_DATA_SOURCE_KEY.equals(tenantIdentifier)) {
            return getPrimaryDataSource();
        }
        try {
            SourceDaimonContext sourceDaimonContext = new SourceDaimonContext(tenantIdentifier);
            return getDataSource(sourceDaimonContext);
        } catch (Exception e) {
            throw new DataSourceLookupFailureException(String.format("Invalid Source Daimon Context key %s", tenantIdentifier), e);
        }
    }

    public DataSource getPrimaryDataSource() {
        return primaryDataSource;
    }
    public void setPrimaryDataSource(DataSource primaryDataSource) {
        this.primaryDataSource = primaryDataSource;
    }

    public int getDataSourceCount() {
        return dataSourceMap.size();
    }
}
