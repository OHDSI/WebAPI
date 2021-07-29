package org.ohdsi.webapi.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import org.aspectj.lang.JoinPoint;
import org.junit.Test;

public class JsonMappingTest {

  @Test
  public void testJsonMapping() throws JsonProcessingException {

    ObjectMapper mapper = new ObjectMapper();

    Exception e = new Exception("some exception");

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    e.printStackTrace(pw);
    String s = sw.toString();

    Result r = new Result();
    r.setStatus("success");
    r.setErrorString(s);
    r.setReturnedValueType(null);
    r.setInvocationDate(new Date().toString());
    s = mapper.writeValueAsString(r);
    System.out.println(s);
  }

  class Result {
    private String status;
    private String returnedValueType;
    private String signature;
    private String signatureName;
    private String invocationDate;
    private String errorString;


    public Result() {

      super();
    }

    public void Result(JoinPoint joinPoint, Throwable returnedThrowable, Date invocationDate) {

      setState(joinPoint, returnedThrowable, invocationDate);
    }

    protected void setState(JoinPoint joinPoint,
                            Throwable returnedThrowable, Date invocationDate) {

      this.signatureName = joinPoint.getSignature().getName();
      this.signature = joinPoint.getSignature().toLongString();
      this.status = "failed";
      this.returnedValueType = "no return value since operation failed";
      /// set error string
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      returnedThrowable.printStackTrace(pw);
      errorString = sw.toString();
      setInvocationDate(invocationDate.toString());
    }

    protected void setState(JoinPoint joinPoint, Object returnedValue, Date invocationDate) {

      this.signatureName = joinPoint.getSignature().getName();
      this.signature = joinPoint.getSignature().toLongString();
      this.status = "succeeded";

      if (returnedValue == null) {
        this.returnedValueType = "null";
      } else {
        this.returnedValueType = returnedValue.getClass().getName();
      }
      errorString = "";
      setInvocationDate(invocationDate.toString());
    }

    public String getInvocationDate() {

      return invocationDate;
    }

    public void setInvocationDate(String date) {

      this.invocationDate = date;
    }

    public String getStatus() {

      return status;
    }

    public void setStatus(String status) {

      this.status = status;
    }

    public String getErrorString() {

      return errorString;
    }

    public void setErrorString(String stringException) {

      this.errorString = stringException;
    }

    public String getReturnedValueType() {

      return returnedValueType;
    }

    public void setReturnedValueType(String returnedValueType) {

      this.returnedValueType = returnedValueType;
    }

    public String getSignature() {

      return signature;
    }

    public void setSignature(String signature) {

      this.signature = signature;
    }

    public String getSignatureName() {

      return signatureName;
    }

    public void setSignatureName(String signatureName) {

      this.signatureName = signatureName;
    }
  }
}
