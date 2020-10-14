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
package org.ohdsi.webapi.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.EvictionAdvisor;
import org.ehcache.jsr107.Eh107Configuration;
import org.ohdsi.webapi.cdmresults.eviction.CDMEvictionAdvisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 *
 * @author cknoll1
 */
@Path("/cache")
@Component
public class CacheService {
	
	public static class ClearCacheResult {
		public List<CacheInfo> clearedCaches;

		private ClearCacheResult() {
			this.clearedCaches = new ArrayList<>();
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
	
	private CacheManager cacheManager;
	
	@Autowired
	public CacheService (CacheManager cacheManager) {
		
		this.cacheManager = cacheManager;
	}
	
	@GET
  @Path("/clear")
  @Produces(MediaType.APPLICATION_JSON)	
	public ClearCacheResult clearAll() {
		ClearCacheResult result = new ClearCacheResult();
		
		for(String cacheName : cacheManager.getCacheNames()) {
			Cache cache = cacheManager.getCache(cacheName);
			CacheInfo info = new CacheInfo();
			info.cacheName = cacheName;
			info.entries = StreamSupport.stream(cache.spliterator(), false).count();
			result.clearedCaches.add(info);
			cache.clear();
		}
		return result;	
	}

	/**
	 * Checks whether cache data is actual.
	 * During specified amount of period it runs cache inspections.
	 * The goal of inspection is to ensure value stored in the cache is still actual
	 * and wasn't changed by some external process.
	 * Cache should be configured with EvictionAdvisor that implements CDMEvictionAdvisor interface
	 * to provide current actual value required by the inspection.
	 * @see CDMEvictionAdvisor
	 */
	@Scheduled(fixedDelayString = "${cache.inspection.period}", initialDelayString = "${cache.inspection.period}")
	public void inspectCaches() {

		logger.info("Starting cache invalidation");
		cacheManager.getCacheNames().forEach(name -> {

			Cache<Object, Object> cache = cacheManager.getCache(name);
			Configuration cfg = cache.getConfiguration(Eh107Configuration.class);
			if (cfg instanceof Eh107Configuration) {
				CacheConfiguration cacheCfg = (CacheConfiguration) ((Eh107Configuration)cfg).unwrap(CacheConfiguration.class);
				EvictionAdvisor advisor = cacheCfg.getEvictionAdvisor();
				if (advisor instanceof CDMEvictionAdvisor) {
					CDMEvictionAdvisor cdmEvictionAdvisor = (CDMEvictionAdvisor) advisor;
					for(Cache.Entry<?, ?> entry : cache) {
						Object key = entry.getKey();
						Object current = entry.getValue();
						logger.debug("Running eviction adviser: {} with key: {}", cdmEvictionAdvisor.getClass(), key);
						Object actual = cdmEvictionAdvisor.getActualValue(key, current);
						if (!Objects.equals(entry.getValue(), actual)) {
							logger.debug("Replacing value of the key: {}", key);
							cache.replace(key, actual);
						}
					}
				}
			}
		});
	}

}
