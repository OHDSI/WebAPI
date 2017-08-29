package org.ohdsi.webapi.exampleapplication;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.webapi.exampleapplication.model.Widget;
import org.ohdsi.webapi.exampleapplication.repository.WidgetRepository;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 *
 */
@Path("/example")
public class ExampleApplicationWithJobService extends AbstractDaoService {
    
    public static final String EXAMPLE_JOB_NAME = "OhdsiExampleJob";
    
    public static final String EXAMPLE_STEP_NAME = "OhdsiExampleStep";
    
    @Autowired
    private JobTemplate jobTemplate;
    
    @Autowired
    private WidgetRepository widgetRepository;
    
    @Autowired
    private TransactionTemplate transactionTemplate;
    
    @Autowired
    private EntityManager em;
    
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
        return this.jobTemplate.launchTasklet(EXAMPLE_JOB_NAME, EXAMPLE_STEP_NAME, new ExampleApplicationTasklet(concepts),
            jobParameters);
    }
    
    @GET
    @Path("widget")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Widget> findAllWidgets() {
        Page<Widget> page = this.widgetRepository.findAll(new PageRequest(0, 10));
        return page.getContent();
    }
    
    @POST
    @Path("widget")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    //Wrapping in transaction (e.g. TransactionTemplate) not necessary as SimpleJpaRepository.save is annotated with @Transactional.
    public Widget createWidget(Widget w) {
        return this.widgetRepository.save(w);
    }
    
    private List<Widget> createWidgets() {
        List<Widget> widgets = new ArrayList<Widget>();
        for (int x = 0; x < 20; x++) {
            Widget w = new Widget();
            w.setName(RandomStringUtils.randomAlphanumeric(10));
            widgets.add(w);
        }
        return widgets;
    }
    
    @POST
    @Path("widgets/batch")
    public void batchWriteWidgets() {
        final List<Widget> widgets = createWidgets();
        this.transactionTemplate.execute(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction(TransactionStatus status) {
                int i = 0;
                for (Widget w : widgets) {
                    em.persist(w);
                    if (i % 5 == 0) { //5, same as the JDBC batch size
                        //flush a batch of inserts and release memory:
                        log.info("Flushing, clearing");
                        em.flush();
                        em.clear();
                    }
                    i++;
                }
                return null;
            }
        });
        log.info(String.format("Persisted %s widgets", widgets.size()));
    }
    
    @POST
    @Path("widgets")
    public void writeWidgets() {
        final List<Widget> widgets = createWidgets();
        this.widgetRepository.save(widgets);
        log.info(String.format("Persisted %s widgets", widgets.size()));
    }
    
    @POST
    @Path("widget2")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    //@Transactional do not work with JAX-RS default config. Review caveots with @Transactional usage (proxy requirements). 
    //Note that SimpleJpaRepository.save is annotated with @Transactional and will use default (e.g. Propagations.REQUIRES). Illustration of deviating from default propagation.
    public Widget createWidgetWith(final Widget w) {
        try {
            final Widget ret = getTransactionTemplateRequiresNew().execute(new TransactionCallback<Widget>() {
                
                @Override
                public Widget doInTransaction(final TransactionStatus status) {
                    return widgetRepository.save(w);
                }
            });
            return ret;
        } catch (final TransactionException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        
    }
}
