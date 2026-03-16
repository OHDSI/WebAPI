package org.ohdsi.webapi.security.authz;

import java.util.HashSet;
import java.util.Set;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;

import org.ohdsi.webapi.security.authz.access.EntityAccessService;
import org.ohdsi.webapi.security.authz.access.UserAuthorizations;
import org.ohdsi.webapi.util.CacheHelper;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationCacheService {
  // create cache
  @Component
  public static class CachingSetup implements JCacheManagerCustomizer {

    public static final String AUTH_INFO_CACHE = "authorizationInfo";

    @Override
    public void customize(CacheManager cacheManager) {
      // due to unit tests causing application contexts to reload cache manager
      // caches, we
      // have to check for the existance of a cache before creating it
      Set<String> cacheNames = CacheHelper.getCacheNames(cacheManager);
      // Evict when a user, role or permission is modified/deleted.
      if (!cacheNames.contains(AUTH_INFO_CACHE)) {
        cacheManager.createCache(AUTH_INFO_CACHE, new MutableConfiguration<Long, UserAuthorizations>()
            // for List types in cache, you need to show the compiler how your List.class ->
            // (GenericInfo) List -> List of Strings
            .setTypes(Long.class, UserAuthorizations.class)
            .setStoreByValue(false)
            .setStatisticsEnabled(true));
      }
    }
  }

  private final UserRoleRepository userRoleRepository;
  private final EntityAccessService entityAccessService;
  private final CacheManager cacheManager;

  public AuthorizationCacheService(
      CacheManager cacheManager,
      UserRoleRepository userRoleRepository,
      EntityAccessService entityAccessService) {
    this.cacheManager = cacheManager;
    this.userRoleRepository = userRoleRepository;
    this.entityAccessService = entityAccessService;
  }

  private Cache<Long, UserAuthorizations> authInfoCache() {
    Cache<Long, UserAuthorizations> cache = cacheManager.getCache(CachingSetup.AUTH_INFO_CACHE);
    if (cache == null) {
      throw new IllegalStateException("Cache not found: " + CachingSetup.AUTH_INFO_CACHE);
    }
    return cache;
  }

  /**
   * Return the full authorization info for the specified user.
   * Includes global wildcard permissions and per-entity access maps.
   * The result is cached by userId.
   * 
   * @param userId The User (by ID) to fetch the authorization info
   * @return The complete UserAuthorizations for this user.
   */
  @Cacheable(cacheNames = CachingSetup.AUTH_INFO_CACHE)
  public UserAuthorizations getUserAuthorizations(Long userId) {
    return entityAccessService.buildUserAuthorizations(userId);
  }

  public void evictUsersWithRole(Long roleId) {
    Cache<Long, UserAuthorizations> cache = authInfoCache();
    Set<Long> usersToEvict = new HashSet<>(userRoleRepository.findUserIdsByRoleId(roleId));
    cache.removeAll(usersToEvict);
  }

  @CacheEvict(cacheNames = CachingSetup.AUTH_INFO_CACHE, allEntries = true)
  public void clearCache() {
    // no-op, clears cache
  }

}
