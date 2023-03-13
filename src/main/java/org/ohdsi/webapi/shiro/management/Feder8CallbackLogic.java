package org.ohdsi.webapi.shiro.management;

import org.pac4j.core.context.JEEContext;
import org.pac4j.core.engine.DefaultCallbackLogic;
import org.pac4j.core.exception.http.FoundAction;
import org.pac4j.core.exception.http.HttpAction;

public class Feder8CallbackLogic extends DefaultCallbackLogic<Object, JEEContext> {

    @Override
    protected HttpAction redirectToOriginallyRequestedUrl(JEEContext context, String defaultUrl) {
        HttpAction httpAction = super.redirectToOriginallyRequestedUrl(context, defaultUrl);
        if (httpAction instanceof FoundAction) {
            return new FoundAction(((FoundAction) httpAction).getLocation().replace(":8080", ""));
        }
        return httpAction;
    }
}
