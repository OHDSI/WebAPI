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
import java.util.stream.StreamSupport;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
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
	
}
