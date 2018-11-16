package com.jnj.honeur.webapi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Provides the correct data source based on the tenant context (~ SourceDaimonContext)
 *
 * @author Peter Moorthamer
 * Date: 02/feb/2018
 */
@Component
public class MultiTenantConnectionProviderImpl extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {

    private static final Log LOG = LogFactory.getLog(MultiTenantConnectionProviderImpl.class);

    private DataSourceLookup dataSourceLookup;

    public MultiTenantConnectionProviderImpl(DataSourceLookup dataSourceLookup) {
        this.dataSourceLookup = dataSourceLookup;
    }

    /**
     * Returns the primary data when the tenant context is unknown (e.g. during startup).
     */
    @Override
    protected DataSource selectAnyDataSource() {
        final DataSource primaryDataSource = dataSourceLookup.getPrimaryDataSource();
        LOG.trace("Select any dataSource: " + primaryDataSource);
        return primaryDataSource;
    }

    /**
     * Returns the correct data source based on the tenant context
     */
    @Override
    protected DataSource selectDataSource(String tenantIdentifier) {
        DataSource ds = dataSourceLookup.getDataSource(tenantIdentifier);
        LOG.trace("Select dataSource for "+ tenantIdentifier+ ": " + ds);
        return ds;
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        final Connection connection = super.getConnection(tenantIdentifier);
        setSchema(connection, tenantIdentifier);
        return connection;
    }

    private void setSchema(final Connection connection, final String tenantIdentifier) {
        Statement stmt = null;
        try {
            String schema = dataSourceLookup.getSchema(tenantIdentifier);
            LOG.debug(String.format("Set schema to %s", schema));
            connection.setSchema(schema);
            stmt = connection.createStatement();
            stmt.execute(String.format("set search_path to %s", schema));
        } catch (SQLException e) {
            LOG.warn("Unable to set the DB schema: " + e.getMessage());
        } finally {
            close(stmt);
        }
    }

    private void close(Statement stmt) {
        try {
            if(stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            LOG.warn(e.getMessage());
        }
    }
}