package org.ohdsi.webapi.executionengine.service;

import static org.ohdsi.webapi.executionengine.service.ExecutionEngineStatus.OFFLINE;
import static org.ohdsi.webapi.executionengine.service.ExecutionEngineStatus.ONLINE;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.ohdsi.webapi.service.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ExecutionEngineStatusServiceImpl implements ExecutionEngineStatusService{

    private volatile ExecutionEngineStatus executionEngineStatus = OFFLINE;
    
    private final Logger logger = LoggerFactory.getLogger(ExecutionEngineStatusServiceImpl.class);
    
    private final HttpClient client;
    private final String executionEngineUrl;
    private final String executionEngineToken;

    @Autowired
    public ExecutionEngineStatusServiceImpl(
            final HttpClient client,
            @Value("${executionengine.url}")
            final String executionEngineURL,
            @Value("${executionengine.token}")
            final String executionEngineToken) {

        this.client = client;
        this.executionEngineUrl = executionEngineURL;
        this.executionEngineToken = executionEngineToken;
    }


    @Scheduled(fixedDelayString = "${execution.status.period}")
    public void checkExecutionEngineStatus() {

        WebTarget webTarget = client.target(executionEngineUrl + "/metrics");
        try {
            webTarget
                    .request(MediaType.TEXT_PLAIN)
                    .accept(MediaType.TEXT_PLAIN)
                    .header("Authorization", executionEngineToken)
                    .get();
            if (!isExecutionEngineOnline()) {
                logger.info("Execution engine is online");
            }
            this.executionEngineStatus = ONLINE;
        } catch (Exception e) {
            if (isExecutionEngineOnline()) {
                logger.error("Execution is unavailable, due to {}", e.getMessage());
            }
            this.executionEngineStatus = OFFLINE;
        }
    }
   
    @Override
    public ExecutionEngineStatus getExecutionEngineStatus() {
        return this.executionEngineStatus;
    }
    
    private boolean isExecutionEngineOnline() {
        return ONLINE.equals(this.executionEngineStatus);
    }
}
