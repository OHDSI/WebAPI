package org.ohdsi.webapi.cohortimportexport;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
/**
 * Imports exports cohorts using something like http://localhost:8080/WebAPI/import/cohort/7
 *
 */
@Path("/cohort/")
@Component
public class CohortImportExportService {

	@Autowired
	public CohortRepository cohortRepository;

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
	public String saveCohortListToCDM(List<CohortEntity> cohort) {

		this.cohortRepository.save(cohort);
		//System.out.println(cohort);

		return "ok";
	}

}
