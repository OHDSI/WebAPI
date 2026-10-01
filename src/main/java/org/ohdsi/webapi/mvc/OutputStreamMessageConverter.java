package org.ohdsi.webapi.mvc;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * HttpMessageConverter for ByteArrayOutputStream.
 * Allows controllers to return ByteArrayOutputStream directly,
 * which is useful for streaming/downloading generated content.
 */
@Component
public class OutputStreamMessageConverter implements HttpMessageConverter<ByteArrayOutputStream> {

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        // We don't support reading ByteArrayOutputStream from requests
        return false;
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return ByteArrayOutputStream.class.equals(clazz);
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        // Support all media types
        return Collections.singletonList(MediaType.ALL);
    }

    @Override
    public ByteArrayOutputStream read(Class<? extends ByteArrayOutputStream> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        throw new UnsupportedOperationException("Reading ByteArrayOutputStream not supported");
    }

    @Override
    public void write(ByteArrayOutputStream outputStream, MediaType contentType, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {

        // Write the ByteArrayOutputStream contents to the response output stream
        outputStream.writeTo(outputMessage.getBody());
    }
}
