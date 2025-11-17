package org.ohdsi.webapi.mvc.controller;

import java.util.List;

import jakarta.persistence.EntityManager;

import org.ohdsi.webapi.cohort.CohortEntity;
import org.ohdsi.webapi.cohort.CohortRepository;
import org.ohdsi.webapi.mvc.AbstractMvcController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring MVC version of CohortService
 *
 * Migration Status: Replaces /service/CohortService.java (Jersey)
 * Endpoints: 2 endpoints (1 GET, 1 POST)
 * Complexity: Simple - read and batch import operations
 *
 * Service to read/write to the Cohort table
 */
@RestController
@RequestMapping("/cohort")
public class CohortMvcController extends AbstractMvcController {

    @Autowired
    private CohortRepository cohortRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private EntityManager em;

    /**
     * Retrieves all cohort entities for the given cohort definition id
     * from the COHORT table
     *
     * Jersey: GET /WebAPI/cohort/{id}
     * Spring MVC: GET /WebAPI/v2/cohort/{id}
     *
     * @param id Cohort Definition id
     * @return List of CohortEntity
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CohortEntity>> getCohortListById(@PathVariable("id") final long id) {
        List<CohortEntity> cohorts = this.cohortRepository.getAllCohortsForId(id);
        return ok(cohorts);
    }

    /**
     * Imports a List of CohortEntity into the COHORT table
     *
     * Jersey: POST /WebAPI/cohort/import
     * Spring MVC: POST /WebAPI/v2/cohort/import
     *
     * @param cohort List of CohortEntity
     * @return status message
     */
    @PostMapping(value = "/import",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> saveCohortListToCDM(@RequestBody final List<CohortEntity> cohort) {
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

        return ok("ok");
    }
}
