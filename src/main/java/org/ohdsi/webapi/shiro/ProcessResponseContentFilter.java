package org.ohdsi.webapi.shiro;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.apache.shiro.web.util.WebUtils;

/**
 *
 * @author gennadiy.anisimov
 */
public abstract class ProcessResponseContentFilter implements Filter {

  @Override
  public void init(FilterConfig fc) throws ServletException {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    if (!shouldProcess(request, response)) {
      chain.doFilter(request, response);
      return;
    }

    if (response.getCharacterEncoding() == null) {
      response.setCharacterEncoding("UTF-8"); 
    }

    HttpServletResponseCopier responseCopier = new HttpServletResponseCopier((HttpServletResponse) response);
    chain.doFilter(request, responseCopier);

    responseCopier.flushBuffer();
    byte[] responseBytes = responseCopier.getCopy();
    
    HttpServletResponse httpResponse = WebUtils.toHttp(response);
    String responseString;
    String contentEncoding = httpResponse.getHeader("Content-Encoding");
    if ("gzip".equalsIgnoreCase(contentEncoding)) {
      responseString = this.readGZip(responseBytes, response.getCharacterEncoding());
    }
    else {
      responseString = new String(responseBytes, response.getCharacterEncoding());
    }

    this.processResponseContent(responseString);
  }

  private void processResponseContent(String content) {
    try {
      this.doProcessResponseContent(content);
    } catch (Exception ex) {
      Logger.getLogger(ProcessResponseContentFilter.class.getName()).log(Level.SEVERE, "Failed to process response content", ex);
    }
  }

  protected abstract boolean shouldProcess(ServletRequest request, ServletResponse response);
  
  protected abstract void doProcessResponseContent(String content) throws Exception;

  protected String parseJsonField(String json, String field) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode rootNode = mapper.readValue(json, JsonNode.class);
    JsonNode fieldNode = rootNode.get(field);
    String fieldValue = fieldNode.asText();
    return fieldValue;
  }

  @Override
  public void destroy() {
  }

  private String readGZip(byte[] data, String encoding) {
    String decompressed = "";
    
    try {
      GZIPInputStream stream = new GZIPInputStream(new ByteArrayInputStream(data));
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream, encoding));
      String line;
      while ((line = reader.readLine()) != null) {
        decompressed += line;
      }
    } catch (IOException ex) {
      Logger.getLogger(ProcessResponseContentFilter.class.getName()).log(Level.SEVERE, "Failed decompress gzipped response content", ex);
    }
    return decompressed;
  }

  protected class HttpServletResponseCopier extends HttpServletResponseWrapper {
    private ServletOutputStream outputStream;
    private PrintWriter writer;
    private ServletOutputStreamCopier copier;

    public HttpServletResponseCopier(HttpServletResponse response) {
      super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (writer != null) {
            throw new IllegalStateException("getWriter() has already been called on this response.");
        }

        if (outputStream == null) {
            outputStream = getResponse().getOutputStream();
            copier = new ServletOutputStreamCopier(outputStream);
        }

        return copier;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (outputStream != null) {
            throw new IllegalStateException("getOutputStream() has already been called on this response.");
        }

        if (writer == null) {
            copier = new ServletOutputStreamCopier(getResponse().getOutputStream());
            writer = new PrintWriter(new OutputStreamWriter(copier, getResponse().getCharacterEncoding()), true);
        }

        return writer;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (writer != null) {
            writer.flush();
        } else if (outputStream != null) {
            copier.flush();
        }
    }

    public byte[] getCopy() {
        if (copier != null) {
            return copier.getCopy();
        } else {
            return new byte[0];
        }
    }
  }
  public class ServletOutputStreamCopier extends ServletOutputStream {

    private OutputStream outputStream;
    private ByteArrayOutputStream copy;

    public ServletOutputStreamCopier(OutputStream outputStream) {
      this.outputStream = outputStream;
      this.copy = new ByteArrayOutputStream(1024);
    }

    @Override
    public void write(int b) throws IOException {
      outputStream.write(b);
      copy.write(b);
    }

    public byte[] getCopy() {
      return copy.toByteArray();
    }

    @Override
    public boolean isReady() {
      return true;
    }

    @Override
    public void setWriteListener(WriteListener wl) {
    }
  }
}
