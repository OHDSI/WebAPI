package org.ohdsi.webapi;

import java.util.Date;

public interface IExecutionInfo<T extends IExecutionInfo> {
    Date getStartTime();
    Integer getExecutionDuration();
    GenerationStatus getStatus();
    boolean getIsValid();
    boolean getIsCanceled();
    String getMessage();
    T setStartTime(Date startTime);
    T setExecutionDuration(Integer executionDuration);
    T setStatus(GenerationStatus status);
    T setIsValid(boolean valid);
    T setMessage(String message);
}
