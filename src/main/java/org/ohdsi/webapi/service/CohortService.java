package org.ohdsi.webapi.service;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.ohdsi.webapi.cohort.CohortEntity;
import org.ohdsi.webapi.cohort.CohortRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Service to read/write to the Cohort table
 */
@Path("/cohort/")
@Component
public class CohortService {

	@Autowired
	public CohortRepository cohortRepository;
	
	@Autowired
    private TransactionTemplate transactionTemplate;
	
	@Autowired
    private EntityManager em;

	/**
	 * Retrieves all cohort entities for the given cohort definition id 
	 * from the COHORT table
	 * 
	 * @param id Cohort Definition id
	 * @return List of CohortEntity
	 */
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<CohortEntity> getCohortListById(@PathParam("id") final long id) {

		List<CohortEntity> d = this.cohortRepository.getAllCohortsForId(id);
		return d;
	}
	
	/**
	 * Imports a List of CohortEntity into the COHORT table
	 * 
	 * @param cohort List of CohortEntity
	 * @return status
	 */
	@POST
	@Path("import")
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
       
		return "ok";
	}

}
