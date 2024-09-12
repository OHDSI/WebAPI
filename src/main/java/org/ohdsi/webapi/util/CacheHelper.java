package org.ohdsi.webapi.util;

import java.lang.management.ManagementFactory;
import java.util.Set;
import javax.cache.CacheManager;
import javax.cache.management.CacheStatisticsMXBean;
import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

/**
 *
 * @author cknoll1
 */
public class CacheHelper {
	public static CacheStatisticsMXBean getCacheStats(CacheManager cacheManager, String cacheName) throws RuntimeException {
		try {
			final MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();

			final Set<ObjectInstance> cacheBeans = beanServer.queryMBeans(
							ObjectName.getInstance("javax.cache:type=CacheStatistics,CacheManager=*,Cache=*"),
							null);
			String cacheManagerName = cacheManager.getURI().toString().replace(":", ".");
			ObjectInstance cacheBean = cacheBeans.stream()
							.filter(b -> 
											b.getObjectName().getKeyProperty("CacheManager").equals(cacheManagerName)
											&& b.getObjectName().getKeyProperty("Cache").equals(cacheName)
							).findFirst().orElseThrow(() -> new IllegalArgumentException(String.format("No cache found for cache manager = %s, cache = %s", cacheManagerName, cacheName)));
			final CacheStatisticsMXBean cacheStatisticsMXBean = MBeanServerInvocationHandler.newProxyInstance(beanServer, cacheBean.getObjectName(), CacheStatisticsMXBean.class, false);
			return cacheStatisticsMXBean;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
