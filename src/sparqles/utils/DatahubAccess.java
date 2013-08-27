package sparqles.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.core.ENDSProperties;
import sparqles.core.Endpoint;
import sparqles.core.EndpointFactory;
import sparqles.core.EndpointManager;

//http://datahub.io/api/2/search/resource?format=api/sparql&all_fields=1&limit=1000

public class DatahubAccess {
	private static final Logger log = LoggerFactory.getLogger(DatahubAccess.class);


	
	/**
	 * This class fetch the SPARQL endpoint list from datahub using the datahub API 
	 * @param epm 
	 **/
	public static Collection<Endpoint> checkEndpointList(EndpointManager epm){
		Map<String, Endpoint> results = epm.getEndpointMap();
		/* LOAD PROPERTIES */
		try {

			HttpClient httpClient = new DefaultHttpClient();
			HttpGet getRequest = new HttpGet("http://datahub.io/api/2/search/resource?format=api/sparql&all_fields=1&limit=1000");
			HttpResponse response = httpClient.execute(getRequest);
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}
			String respString = EntityUtils.toString(response.getEntity());
//			System.out.println(respString);

			JsonFactory factory = new JsonFactory();
			ObjectMapper mapper = new ObjectMapper(factory);
			JsonNode rootNode = mapper.readTree(respString);  


			JsonNode res = rootNode.get("results");
//			System.out.println(res instanceof ArrayNode);
			Iterator<JsonNode> iter = res.getElements();
			//Iterator<Entry<String, JsonNode>> iter = rootNode.get("results").getFields();
			while(iter.hasNext()){
				String endpointURL = iter.next().findPath("url").getTextValue();
				if(endpointURL.trim().length()!=0){
					Endpoint ep;
					try {
						ep = EndpointFactory.newEndpoint(new URI(endpointURL));
						results.put(endpointURL, ep);
//						System.out.println(ep);
					} catch (URISyntaxException e) {
						log.warn("URISyntaxException:{}",e.getMessage());
					}
				}
			}
			log.info("Found {} endpoints",results.size());
			

			//			JSONObject json = (JSONObject) JSONSerializer.toJSON( respString);        
			//	        JSONArray resources = json.getJSONArray("results");
			//	        for (int i = 0; i < resources.size(); i++) {
			//	        	
			//	        	// for each dataset (if not already existing with same value, fetch dataset information)
			//	        	JSONObject resource = (JSONObject)resources.get(i);
			//	        	String endpointURL = resource.getString("url");
			//	        	String datasetId = resource.getString("package_id");
			//	        	if(endpointURL.trim().length()>0 && !results.containsKey(endpointURL)){
			//	        		Endpoint ep;
			//					try {
			//						ep = EndpointFactory.newEndpoint(new URI(endpointURL));
			//						results.put(endpointURL, ep);
			//					} catch (URISyntaxException e) {
			//						e.printStackTrace();
			//					}
			////		        	boolean alreadyExisting=false;
			////		        	for (int j = 0; j < results.size(); j++) {
			////						if(results.get(j).getPackageUuid().equals(datasetId) && results.get(j).getEndpointURL().equals(endpointURL)){
			////							alreadyExisting=true;
			////							break;
			////						}
			////					}
			////		        	if(!alreadyExisting) results.add(getEndpointDetails(datasetId,endpointURL));
			//	        	}
			//	        	
			//			}
			httpClient.getConnectionManager().shutdown();

//			storeMap(results);
			//write down csv file
			//			StringBuilder st = new StringBuilder();
			//			for (Entry<String, Endpoint> ent : results.entrySet()) {
			//				st.append(ent.getValue()+"\n");
			//			}
			////		    System.out.println(st.toString());

		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		} 

		return results.values();
	}
	
	//	
	//	private static EndpointResult getEndpointDetails(String datasetId,String endpointURL){
	//		try {
	//			
	//			 
	//			HttpClient httpClient = new DefaultHttpClient();
	//			HttpGet getRequest = new HttpGet("http://datahub.io/api/2/rest/dataset/"+datasetId);
	//			HttpResponse response = httpClient.execute(getRequest);
	//			if (response.getStatusLine().getStatusCode() != 200) {
	//				throw new RuntimeException("Failed : HTTP error code : "
	//				   + response.getStatusLine().getStatusCode());
	//			}
	//	 
	//			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
	//	 
	//			StringBuilder datasetIdList = new StringBuilder();
	////			System.out.println("Output from Server .... \n");
	//			String line = "";
	//			while ((line = br.readLine()) != null) {
	//				datasetIdList.append(line);
	//			}
	//			
	//			JSONObject json = (JSONObject) JSONSerializer.toJSON( datasetIdList.toString() );        
	//	        String ckan_url = json.getString( "ckan_url" );
	//	        String title = json.getString("title").trim();
	//	        String name = json.getString("name");
	//	        EndpointResult result = new EndpointResult();
	//	        result.setPackageUuid(datasetId);
	//			result.setPackageId(name);
	//	        result.setDatasetURI(ckan_url);
	//			result.setEndpointURL(endpointURL);
	//			result.setDatasetTitle(title);
	//	        
	//	        
	//	        System.out.println(datasetId+"\t"+ckan_url+"\t"+title+"\t"+name+"\t"+endpointURL);
	////	        JSONObject pilot = json.getJSONObject("pilot");
	//	        
	//	 
	//			httpClient.getConnectionManager().shutdown();
	//			return result;
	//	 
	//		  } catch (ClientProtocolException e) {
	//	 
	//			e.printStackTrace();
	//	 
	//		  } catch (IOException e) {
	//	 
	//			e.printStackTrace();
	//		  }
	//		return null;
	//	}
	//	
	//	public static List<EndpointResult> getEndpointList(){
	//		List<EndpointResult> results = new ArrayList<EndpointResult>();
	//		/* LOAD PROPERTIES */
	//	 	try {
	//			Properties prop = new Properties();
	//			String fileName = "endpointStatus.config";
	//			InputStream is = new FileInputStream(fileName);
	//			prop.load(is);
	//			String endpoints_list_store=  prop.getProperty("ENDPOINTS_LIST_STORE");
	//		
	//			File file = new File(endpoints_list_store);
	//			// input
	//		     FileInputStream fis  = new FileInputStream(file);
	//		     BufferedReader in = new BufferedReader
	//		         (new InputStreamReader(fis));
	//	
	//		     String thisLine="";
	//		     while ((thisLine = in.readLine()) != null) {
	//		    	 EndpointResult res = new EndpointResult();
	//		    	 System.out.println(thisLine);
	//		    	 res.fromCSV(thisLine);
	//		    	 results.add(res);
	//		     }
	//		     in.close();
	//	//	     System.out.println(st.toString());     
	//		    
	//	 	} catch (FileNotFoundException e2) {
	//			e2.printStackTrace();
	//		} catch (IOException e2) {
	//			e2.printStackTrace();
	//		}
	//		return results;
	//	}
	
}
