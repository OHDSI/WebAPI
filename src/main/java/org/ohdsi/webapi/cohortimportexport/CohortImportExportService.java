package org.ohdsi.webapi.cohortimportexport;

import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
/**
 * Imports exports cohorts using something like http://localhost:8080/WebAPI/import/cohort/7
 *
 */
@Path("/cohort/")
@Component
public class CohortImportExportService {

	@Autowired
	public CohortRepository cohortRepository;
	
	@Autowired
    private TransactionTemplate transactionTemplate;
	
	@Autowired
    private EntityManager em;

	@GET
	@Path("import/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<CohortEntity> getCohortListById(@PathParam("id") final long id) {

		List<CohortEntity> d = this.cohortRepository.getAllCohortsForId(id);
		

		return d;
	}
	
	@POST
	@Path("export")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String saveCohortListToCDM(final List<CohortEntity> cohort) {

		this.transactionTemplate.execute(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction(TransactionStatus status) {
                int i = 0;
                for (CohortEntity cohortEntity : cohort) {
                	em.persist(cohortEntity);
                    if (i % 5 == 0) { //5, same as the JDBC batch size
                        //flush a batch of inserts and release memory:
                        em.flush();
                        em.clear();
                    }
                    i++;
                }
                return null;
            }
        });
        
		//System.out.println(cohort);

		return "ok";
	}

}
