package org.ohdsi.webapi.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.webapi.evidence.CommandList;
import org.ohdsi.webapi.evidence.LinkoutData;
import org.ohdsi.webapi.evidence.RdfInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;


@Path("evidence/")
@Component
public class SparqlService {
	
	@Autowired
	  private Environment env;
	
	
	
	@GET
	  @Path("")
	  @Produces(MediaType.APPLICATION_JSON)
	  public Collection<CommandList> getAllCommand() throws JSONException {
		
		List<CommandList> infoOnCommand = new ArrayList<CommandList>();
	    	
	    	CommandList command1 = new CommandList();
	    	command1.Param = "rdfinfo";
	    	command1.Example = "rdfinfo";
	    	command1.Description = "List the basic infomation about the RDF endpoint.";
	    	infoOnCommand.add(command1);
	    	CommandList command2 = new CommandList();
	    	command2.Param = "linkoutdata/{linkout}";
	    	command2.Example = "linkoutdata/http%3A%252F%252Fdbmi-icode-01.dbmi.pitt.edu%252Fl%252Findex.php%3Fid%3Dsplicer-237164";
	    	command2.Description = "List all the linkout data of certain drug.";
	    	infoOnCommand.add(command2);
	    	CommandList command3 = new CommandList();
	    	command3.Param = "?";
	    	command3.Example = "?";
	    	command3.Description = "List all available commands with the prefix of 'WebAPI/evidence/'";
	    	infoOnCommand.add(command3);
	    
	    return infoOnCommand;
	  }
	
	@GET
	  @Path("rdfinfo")
	  @Produces(MediaType.APPLICATION_JSON)
	  public Collection<RdfInfo> getInfo() throws JSONException {
		
		String query = ResourceHelper.GetResourceAsString("/resources/evidence/sparql/info.sparql");
		String uriQuery = null;
		String sparqlEndpoint = this.env.getRequiredProperty("sparql.endpoint");
		query = sparqlEndpoint + query;
		try {
			uriQuery = URIUtil.encodeQuery(query);
		} catch (URIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		uriQuery = uriQuery + "&format=application%2Fsparql-results%2Bjson";
	    List<RdfInfo> infoOnSources = new ArrayList<RdfInfo>();
	    JSONArray lineItems = readJSONFeed(uriQuery);
	    for (int i = 0; i < lineItems.length(); ++i) {
	        JSONObject tempItem = lineItems.getJSONObject(i);
	        JSONObject tempSource = tempItem.getJSONObject("sourceDocument");
	        String source = tempSource.getString("value");
	        RdfInfo info = new RdfInfo();
	        info.sourceDocument = source;
	        infoOnSources.add(info);
	    }
	    
	    return infoOnSources;
	  }
	
	
	@GET
	  @Path("linkoutdata/{linkout}")
	  @Produces(MediaType.APPLICATION_JSON)
	  public Collection<LinkoutData> getLinkout(@PathParam("linkout") String linkout) throws JSONException, IOException {
		
		String expandedURL = URIUtil.decode(linkout);
		expandedURL = expandUrl(expandedURL);
		expandedURL = URIUtil.decode(expandedURL);;
		expandedURL = URIUtil.encodeQuery(expandedURL);
	    List<LinkoutData> infoOnLinkout = new ArrayList<LinkoutData>();
	    JSONArray lineItems = readJSONFeed(expandedURL);
	    for (int i = 0; i < lineItems.length(); ++i) {
	        JSONObject tempItem = lineItems.getJSONObject(i);
	        JSONObject tempSource = tempItem.getJSONObject("an");
	        String source = tempSource.getString("value");
	        LinkoutData info = new LinkoutData();
	        info.an = source;
	        tempSource = tempItem.getJSONObject("body");
	        source = tempSource.getString("value");
	        info.body = source;
	        tempSource = tempItem.getJSONObject("target");
	        source = tempSource.getString("value");
	        info.target = source;
	        tempSource = tempItem.getJSONObject("sourceURL");
	        source = tempSource.getString("value");
	        info.sourceURL = source;
	        tempSource = tempItem.getJSONObject("selector");
	        source = tempSource.getString("value");
	        info.selector = source;
	        tempSource = tempItem.getJSONObject("spl");
	        source = tempSource.getString("value");
	        info.spl = source;
	        tempSource = tempItem.getJSONObject("text");
	        source = tempSource.getString("value");
	        info.text = source;
	        infoOnLinkout.add(info);
	    }
	    
	    return infoOnLinkout;
	  }
	
	//expand URL from short URL
	public String expandUrl(String shortenedUrl) throws IOException {
        URL url = new URL(shortenedUrl);    
        // open connection
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY); 
        
        // stop following browser redirect
        httpURLConnection.setInstanceFollowRedirects(false);
         
        // extract location header containing the actual destination URL
        String expandedURL = httpURLConnection.getHeaderField("Location");
        httpURLConnection.disconnect();
        //System.out.println("EXPAND: "+expandedURL);
        return expandedURL;
    }
	
	//get and parse JSON function
	public JSONArray readJSONFeed(String URL) throws JSONException {
	    StringBuilder stringBuilder = new StringBuilder();
		HttpClient httpClient = new DefaultHttpClient();
	    HttpGet httpGet = new HttpGet(URL);

	    try {

	        HttpResponse response = httpClient.execute(httpGet);
	        StatusLine statusLine = response.getStatusLine();
	        int statusCode = statusLine.getStatusCode();

	        if (statusCode == 200) {

	            HttpEntity entity = response.getEntity();
	            InputStream inputStream = entity.getContent();
	            BufferedReader reader = new BufferedReader(
	                    new InputStreamReader(inputStream));
	            String line;
	            while ((line = reader.readLine()) != null) {
	                stringBuilder.append(line);
	            }
	            inputStream.close();

	        }
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    JSONObject jsonObj = new JSONObject(stringBuilder.toString());
	    JSONArray lineItems = jsonObj.getJSONObject("results").getJSONArray("bindings");
	    return lineItems;
	}
}
