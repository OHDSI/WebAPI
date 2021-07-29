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
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.ExpiryPolicy;
import org.ehcache.jsr107.Eh107Configuration;
import org.ohdsi.webapi.cdmresults.eviction.*;
import org.ohdsi.webapi.cdmresults.keys.RefreshableSourceKey;
import org.ohdsi.webapi.cdmresults.keys.DrilldownKey;
import org.ohdsi.webapi.cdmresults.keys.TreemapKey;
import org.ohdsi.webapi.report.CDMDashboard;
import org.ohdsi.webapi.report.CDMDataDensity;
import org.ohdsi.webapi.report.CDMPersonSummary;
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

    public static final int HEAP_SIZE = 100;
    private final DashboardEvictionAdvisor dashboardEvictionAdvisor;
    private final PersonEvictionAdvisor personEvictionAdvisor;
    private final DrilldownEvictionAdvisor drilldownEvictionAdvisor;
    private final TreemapEvictionAdvisor treemapEvictionAdvisor;
    private final DataDensityEvictionAdivisor dataDensityEvictionAdivisor;

    public CacheConfig(DashboardEvictionAdvisor dashboardEvictionAdvisor,
                       PersonEvictionAdvisor personEvictionAdvisor,
                       DrilldownEvictionAdvisor drilldownEvictionAdvisor,
                       TreemapEvictionAdvisor treemapEvictionAdvisor,
                       DataDensityEvictionAdivisor dataDensityEvictionAdivisor) {
        this.dashboardEvictionAdvisor = dashboardEvictionAdvisor;
        this.personEvictionAdvisor = personEvictionAdvisor;
        this.drilldownEvictionAdvisor = drilldownEvictionAdvisor;
        this.treemapEvictionAdvisor = treemapEvictionAdvisor;
        this.dataDensityEvictionAdivisor = dataDensityEvictionAdivisor;
    }

    @Override
    public void customize(CacheManager cacheManager) {

        ResourcePoolsBuilder resourcePool = ResourcePoolsBuilder.heap(HEAP_SIZE);
        CacheConfigurationBuilder<String, CDMDashboard> dashboardCacheConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, CDMDashboard.class, resourcePool)
                .withEvictionAdvisor(dashboardEvictionAdvisor)
                .withExpiry(ExpiryPolicy.NO_EXPIRY);
        cacheManager.createCache(Constants.Caches.Datasources.DASHBOARD, Eh107Configuration.fromEhcacheCacheConfiguration(dashboardCacheConfig));

        CacheConfigurationBuilder<RefreshableSourceKey, CDMPersonSummary> personCacheConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(RefreshableSourceKey.class, CDMPersonSummary.class, resourcePool)
                .withEvictionAdvisor(personEvictionAdvisor)
                .withExpiry(ExpiryPolicy.NO_EXPIRY);
        cacheManager.createCache(Constants.Caches.Datasources.PERSON, Eh107Configuration.fromEhcacheCacheConfiguration(personCacheConfig));

        CacheConfigurationBuilder<TreemapKey, ArrayNode> domainCacheConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(TreemapKey.class, ArrayNode.class, resourcePool)
                .withEvictionAdvisor(treemapEvictionAdvisor)
                .withExpiry(ExpiryPolicy.NO_EXPIRY);
        cacheManager.createCache(Constants.Caches.Datasources.DOMAIN, Eh107Configuration.fromEhcacheCacheConfiguration(domainCacheConfig));

        CacheConfigurationBuilder<DrilldownKey, JsonNode> drilldownCacheConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(DrilldownKey.class, JsonNode.class, resourcePool)
                .withEvictionAdvisor(drilldownEvictionAdvisor)
                .withExpiry(ExpiryPolicy.NO_EXPIRY);
        cacheManager.createCache(Constants.Caches.Datasources.DRILLDOWN, Eh107Configuration.fromEhcacheCacheConfiguration(drilldownCacheConfig));

        CacheConfigurationBuilder<RefreshableSourceKey, CDMDataDensity> dataDensityCacheConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(RefreshableSourceKey.class, CDMDataDensity.class, resourcePool)
                .withEvictionAdvisor(dataDensityEvictionAdivisor)
                .withExpiry(ExpiryPolicy.NO_EXPIRY);
        cacheManager.createCache(Constants.Caches.Datasources.DATADENSITY, Eh107Configuration.fromEhcacheCacheConfiguration(dataDensityCacheConfig));
    }
}
