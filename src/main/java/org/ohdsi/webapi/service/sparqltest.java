package org.ohdsi.webapi.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

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
import org.ohdsi.webapi.helper.ResourceHelper;

public class sparqltest {
	public static void main(String[] args) throws JSONException{
		String query = ResourceHelper.GetResourceAsString("/resources/evidence/sparql/info.sparql");
		String uriQuery = null;
		query = "http://virtuoso.ohdsi.org:8890/sparql?default-graph-uri=&query=" + query + "";
		try {
			uriQuery = URIUtil.encodeQuery(query);
		} catch (URIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		uriQuery = uriQuery + "&format=application%2Fsparql-results%2Bjson";
		readJSONFeed(uriQuery);
		//System.out.println(result1);
	}
	
	private static void readJSONFeed(String URL) throws JSONException {
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

	        } else {
	            //Log.d("JSON", "Failed to download file");
	        }
	    } catch (Exception e) {
	        //Log.d("readJSONFeed", e.getLocalizedMessage());
	    }
	    JSONObject jsonObj = new JSONObject(stringBuilder.toString());
	    JSONArray lineItems = jsonObj.getJSONObject("results").getJSONArray("bindings");
	    for (int i = 0; i < lineItems.length(); ++i) {
	        JSONObject tempItem = lineItems.getJSONObject(i);
	        JSONObject tempSource = tempItem.getJSONObject("sourceDocument");
	        String source = tempSource.getString("value");
	        System.out.println(source);
	    }
	}
}
