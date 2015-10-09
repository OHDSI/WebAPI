package org.ohdsi.webapi.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * http://stackoverflow.com/questions/29712554/how-to-download-a-file-using-a-java-rest-service-and-a-data-stream
 * 
 */

@Provider
public class OutputStreamWriter implements MessageBodyWriter<ByteArrayOutputStream> {

    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return ByteArrayOutputStream.class == type;
    }

    @Override
    public long getSize(ByteArrayOutputStream t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(ByteArrayOutputStream t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {
        t.writeTo(entityStream);
    }
}