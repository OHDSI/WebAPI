package org.ohdsi.webapi.util;

import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.util.CommonHelper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.net.MalformedURLException;

public class ResourceUtils {
    public static final String RESOURCE_PREFIX = "resource:";
    public static final String CLASSPATH_PREFIX = "classpath:";
    public static final String FILE_PREFIX = "file:";

    public static Resource mapPathToResource(final String path) {
        CommonHelper.assertNotBlank("path", path);
        if (path.startsWith(RESOURCE_PREFIX)) {
            return new ClassPathResource(path.substring(RESOURCE_PREFIX.length()));
        } else if (path.startsWith(CLASSPATH_PREFIX)) {
            return new ClassPathResource(path.substring(CLASSPATH_PREFIX.length()));
        } else if (path.startsWith(HttpConstants.SCHEME_HTTP) || path.startsWith(HttpConstants.SCHEME_HTTPS)) {
            return newUrlResource(path);
        } else if (path.startsWith(FILE_PREFIX)) {
            return new FileSystemResource(path.substring(FILE_PREFIX.length()));
        } else {
            return new FileSystemResource(path);
        }
    }

    private static UrlResource newUrlResource(final String url) {
        try {
            return new UrlResource(url);
        } catch (final MalformedURLException e) {
            throw new TechnicalException(e);
        }
    }
}
