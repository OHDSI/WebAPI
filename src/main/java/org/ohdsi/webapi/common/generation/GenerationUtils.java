package org.ohdsi.webapi.common.generation;

import org.apache.shiro.SecurityUtils;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.source.Source;

import javax.ws.rs.ForbiddenException;

public class GenerationUtils {

    public static void checkSourceAccess(Source source) {
        if (!SecurityUtils.getSubject().isPermitted(String.format(Security.SOURCE_ACCESS_PERMISSION, source.getSourceKey()))){
            throw new ForbiddenException();
        }
    }


}
