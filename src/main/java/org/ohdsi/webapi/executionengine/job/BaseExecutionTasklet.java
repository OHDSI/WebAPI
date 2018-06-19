package org.ohdsi.webapi.executionengine.job;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;

public abstract class BaseExecutionTasklet implements Tasklet, StepExecutionListener {
    
    private ExecutionContext executionContext;
    
    @Override
    public void beforeStep(final StepExecution stepExecution) {
        
        this.executionContext = stepExecution
            .getJobExecution()
            .getExecutionContext();
    }

    @Override
    public ExitStatus afterStep(final StepExecution stepExecution) {

        return ExitStatus.COMPLETED;
    }

    public boolean contains(String key) {
        
        return executionContext.containsKey(key);
    }

    public void put(final String key, final Object value) {
        
        executionContext.put(key, value);
    }
    
    public Object get(final String key) {
        
        return executionContext.get(key);
    }
    
    public void putInt(final String key, final int value) {

        executionContext.putInt(key, value);
    }
    
    public int getInt(final String key) {
        
        return executionContext.getInt(key);
    }
}
