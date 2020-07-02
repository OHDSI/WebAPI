/*
 * Copyright 2019 cknoll1.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ohdsi.webapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.ExpiryPolicy;
import org.ehcache.jsr107.Eh107Configuration;
import org.ohdsi.webapi.cdmresults.eviction.DashboardEvictionAdvisor;
import org.ohdsi.webapi.report.CDMDashboard;
import org.ohdsi.webapi.report.CDMDataDensity;
import org.ohdsi.webapi.report.CDMPersonSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

import javax.cache.CacheManager;

/**
 *
 * @author cknoll1
 */
@Configuration
@EnableCaching
public class CacheConfig implements JCacheManagerCustomizer {

    private DashboardEvictionAdvisor dashboardEvictionAdvisor;

    public CacheConfig(DashboardEvictionAdvisor dashboardEvictionAdvisor) {
        this.dashboardEvictionAdvisor = dashboardEvictionAdvisor;
    }

    @Override
    public void customize(CacheManager cacheManager) {

        ResourcePoolsBuilder resourcePool = ResourcePoolsBuilder.heap(100);
        CacheConfigurationBuilder<String, CDMDashboard> dashboardCacheConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, CDMDashboard.class, resourcePool)
                .withEvictionAdvisor(dashboardEvictionAdvisor)
                .withExpiry(ExpiryPolicy.NO_EXPIRY);
        cacheManager.createCache("datasources.dashboard", Eh107Configuration.fromEhcacheCacheConfiguration(dashboardCacheConfig));

        CacheConfigurationBuilder<Object, CDMPersonSummary> personCacheConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, CDMPersonSummary.class, resourcePool)
                .withExpiry(ExpiryPolicy.NO_EXPIRY);
        cacheManager.createCache("datasources.person", Eh107Configuration.fromEhcacheCacheConfiguration(personCacheConfig));

        CacheConfigurationBuilder<Object, ArrayNode> domainCacheConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, ArrayNode.class, resourcePool)
                .withExpiry(ExpiryPolicy.NO_EXPIRY);
        cacheManager.createCache("datasources.domain", Eh107Configuration.fromEhcacheCacheConfiguration(domainCacheConfig));

        CacheConfigurationBuilder<Object, JsonNode> drilldownCacheConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, JsonNode.class, resourcePool)
                .withExpiry(ExpiryPolicy.NO_EXPIRY);
        cacheManager.createCache("datasources.drilldown", Eh107Configuration.fromEhcacheCacheConfiguration(drilldownCacheConfig));

        CacheConfigurationBuilder<Object, CDMDataDensity> dataDensityCacheConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, CDMDataDensity.class, resourcePool)
                .withExpiry(ExpiryPolicy.NO_EXPIRY);
        cacheManager.createCache("datasources.dataDensity", Eh107Configuration.fromEhcacheCacheConfiguration(dataDensityCacheConfig));
    }
}
