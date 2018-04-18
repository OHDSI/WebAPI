package com.jnj.honeur.webapi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

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
}