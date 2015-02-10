package org.ohdsi.webapi.exampleapplication;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.vocabulary.Concept;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
@Path("/example")
public class ExampleApplicationWithJobService extends AbstractDaoService {
    
    @Autowired
    JobTemplate jobTemplate;
    
    public static class ExampleApplicationTasklet implements Tasklet {
        
        private static final Log log = LogFactory.getLog(ExampleApplicationTasklet.class);
        
        private final List<Concept> concepts;
        
        public ExampleApplicationTasklet(final List<Concept> concepts) {
            this.concepts = concepts;
        }
        
        @Override
        public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
            // set contextual data in JobExecutionContext
            chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()
                    .put("concepts", this.concepts);
            log.info("Tasklet execution >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            //                Thread.sleep(14000L);
            return RepeatStatus.FINISHED;
        }
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public JobExecutionResource queueJob() throws Exception {
        //Allow unique combinations of JobParameters to run in parallel.  An empty JobParameters() would only allow a JobInstance to run at a time.
        final JobParameters jobParameters = new JobParametersBuilder().addString("param", "parameter with 250 char limit")
                .addLong("time", System.currentTimeMillis()).toJobParameters();
        final List<Concept> concepts = new ArrayList<Concept>();
        final Concept c1 = new Concept();
        c1.conceptName = "c1";
        final Concept c2 = new Concept();
        c2.conceptName = "c2";
        concepts.add(c1);
        concepts.add(c2);
        return this.jobTemplate.launchTasklet("OhdsiExampleJob", "OhdsiExampleStep",
            new ExampleApplicationTasklet(concepts), jobParameters);
    }
}
