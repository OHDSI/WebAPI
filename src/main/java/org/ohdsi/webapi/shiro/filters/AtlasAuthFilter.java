package org.ohdsi.webapi.shiro.filters;

import org.apache.shiro.web.filter.authc.AuthenticatingFilter;

/**
 * Filter to check for a user, who has already logged in using external service
 */
public abstract class AtlasAuthFilter extends AuthenticatingFilter {
}
