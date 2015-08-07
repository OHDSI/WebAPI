package org.ohdsi.webapi.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.ohdsi.webapi.evidence.RdfInfo;
import org.springframework.stereotype.Component;

@Path("{sourceKey}/evidence/")
@Component
public class SparqlService {
	
	public static String virtuosoEndpoint = "http://virtuoso.ohdsi.org:8890/sparql/";
	
	@GET
	@Path("rdfinfo")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<RdfInfo> getRDFInfo() {
		
		String sparqlFile = "src/main/resources/resources/evidence/sparql/info.sparql";
		final List<RdfInfo> infoOnRdf = new ArrayList<RdfInfo>();
		Query query  = QueryFactory.read(sparqlFile);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(virtuosoEndpoint, query);
		ResultSet results = qexec.execSelect();
		//ResultSetFormatter.outputAsJSON(System.out, results);
		while(results.hasNext())
		{
 	    
			RdfInfo info = new RdfInfo();
			QuerySolution result=results.next();
			info.sourceDocument = result.get("sourceDocument").toString();
			infoOnRdf.add(info);
 	    
		}
		qexec.close();
		return infoOnRdf;	  
	}
	
	
}
