package org.ohdsi.webapi;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@Lazy(false)
@EnableCaching
public class CacheConfig {

}
