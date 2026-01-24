package org.ohdsi.webapi.shiro.filters;

import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.shiro.TokenManager;
import org.ohdsi.webapi.util.UserUtils;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.exception.http.FoundAction;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.engine.savedrequest.SavedRequestHandler;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Custom SavedRequestHandler that redirects to the Atlas UI URL with JWT token
 * after successful OIDC authentication.
 *
 * This handler:
 * 1. Extracts user info from the pac4j profile (from CallContext)
 * 2. Registers the user in WebAPI
 * 3. Generates a JWT token
 * 4. Redirects to Atlas UI with the token in the URL
 */
public class AtlasRedirectSavedRequestHandler implements SavedRequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(AtlasRedirectSavedRequestHandler.class);

    private final String atlasUrl;
    private final PermissionManager permissionManager;
    private final int tokenExpirationIntervalInSeconds;
    private final Set<String> defaultRoles;

    public AtlasRedirectSavedRequestHandler(String atlasUrl, PermissionManager permissionManager,
                                            int tokenExpirationIntervalInSeconds, Set<String> defaultRoles) {
        this.atlasUrl = atlasUrl;
        this.permissionManager = permissionManager;
        this.tokenExpirationIntervalInSeconds = tokenExpirationIntervalInSeconds;
        this.defaultRoles = defaultRoles;
        logger.info("AtlasRedirectSavedRequestHandler initialized with URL: {}", atlasUrl);
    }

    @Override
    public void save(CallContext ctx) {
        // Don't save the request - we always want to redirect to Atlas
        logger.debug("AtlasRedirectSavedRequestHandler.save() called - ignoring save");
    }

    @Override
    public HttpAction restore(CallContext ctx, String defaultUrl) {
        String redirectUrl = atlasUrl != null ? atlasUrl : defaultUrl;

        try {
            // Get the user profile from pac4j's ProfileManager (not Shiro - auth hasn't happened yet)
            ProfileManager profileManager = new ProfileManager(ctx.webContext(), ctx.sessionStore());
            List<UserProfile> profiles = profileManager.getProfiles();

            logger.info("AtlasRedirectSavedRequestHandler: Found {} profiles", profiles.size());

            if (!profiles.isEmpty()) {
                UserProfile profile = profiles.get(0);

                // Log all profile attributes for debugging
                logger.info("AtlasRedirectSavedRequestHandler: Profile attributes: {}", profile.getAttributes());

                // Try to get login from various attributes (prefer email for login identifier)
                String login = (String) profile.getAttribute("email");
                if (login == null) {
                    login = (String) profile.getAttribute("preferred_username");
                }
                if (login == null) {
                    login = (String) profile.getAttribute("username");
                }
                if (login == null) {
                    login = profile.getId();
                }

                // Try to get display name from various attributes
                String name = (String) profile.getAttribute("name");
                if (name == null) {
                    name = (String) profile.getAttribute("username");
                }
                if (name == null) {
                    name = (String) profile.getAttribute("nickname");
                }
                if (name == null) {
                    // Try combining given_name and family_name
                    String givenName = (String) profile.getAttribute("given_name");
                    String familyName = (String) profile.getAttribute("family_name");
                    if (givenName != null || familyName != null) {
                        name = ((givenName != null ? givenName : "") + " " + (familyName != null ? familyName : "")).trim();
                    }
                }
                String clientName = profile.getClientName();

                logger.info("AtlasRedirectSavedRequestHandler: login={}, name={}, client={}",
                           login, name, clientName);

                if (login != null) {
                    login = UserUtils.toLowerCase(login);

                    // Register user if needed
                    if (name == null) {
                        name = login;
                    }
                    permissionManager.registerUser(login, name, defaultRoles);

                    // Generate JWT token
                    Date expiration = getExpirationDate(tokenExpirationIntervalInSeconds);
                    String jwt = TokenManager.createJsonWebToken(login, null, expiration);

                    // Construct URL with token at hash root: {baseUrl}#/{clientName}/{jwt}
                    // The Vue Router expects the token at the hash root, not appended to an existing hash path
                    String baseUrl = redirectUrl;
                    int hashIndex = redirectUrl.indexOf('#');
                    if (hashIndex > 0) {
                        // Remove the hash and everything after it
                        baseUrl = redirectUrl.substring(0, hashIndex);
                    }
                    String urlWithToken = baseUrl.replaceAll("/+$", "") + "/#/" + clientName + "/" + jwt;
                    logger.info("AtlasRedirectSavedRequestHandler.restore() - redirecting with token to: {}",
                               urlWithToken.substring(0, Math.min(urlWithToken.length(), 100)) + "...");

                    return new FoundAction(urlWithToken);
                } else {
                    logger.warn("AtlasRedirectSavedRequestHandler: No login identifier found in profile");
                }
            } else {
                logger.warn("AtlasRedirectSavedRequestHandler: No profiles found in context");
            }
        } catch (Exception e) {
            logger.error("AtlasRedirectSavedRequestHandler: Error generating token", e);
        }

        // Fallback: redirect without token
        logger.info("AtlasRedirectSavedRequestHandler.restore() - redirecting without token to: {}", redirectUrl);
        return new FoundAction(redirectUrl);
    }

    private Date getExpirationDate(int expirationIntervalInSeconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, expirationIntervalInSeconds);
        return calendar.getTime();
    }
}
