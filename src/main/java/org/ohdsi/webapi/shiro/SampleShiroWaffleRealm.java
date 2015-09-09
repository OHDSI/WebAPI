package org.ohdsi.webapi.shiro;

import org.apache.shiro.authz.AuthorizationInfo;
import waffle.shiro.AbstractWaffleRealm;
import waffle.shiro.WaffleFqnPrincipal;

/**
 * Created by GMalikov on 08.09.2015.
 */
public class SampleShiroWaffleRealm extends AbstractWaffleRealm{
    @Override
    protected AuthorizationInfo buildAuthorizationInfo(WaffleFqnPrincipal waffleFqnPrincipal) {
        return null;
    }
}
