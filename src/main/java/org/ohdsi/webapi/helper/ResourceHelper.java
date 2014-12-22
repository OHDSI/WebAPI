package org.ohdsi.webapi.helper;

import java.io.InputStream;
import org.apache.commons.io.IOUtils;
/**
 *
 * @author fdefalco
 */
public class ResourceHelper {
    /**
     *
     * @param resource
     * @return
     */
    public static String GetResourceAsString(String resource) {
        InputStream inputStream = ResourceHelper.class.getResourceAsStream(resource);
        String content = "";
        try {
            content = IOUtils.toString(inputStream, "UTF-8");
        } catch (Exception exception) {
            throw new RuntimeException("Resource not found: " + resource);
        }

        return content;
    }    
}
