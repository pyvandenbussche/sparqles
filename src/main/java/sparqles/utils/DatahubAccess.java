package sparqles.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
//import org.apache.http.conn.HttpClientConnectionManager;
//import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.core.CONSTANTS;
import sparqles.core.Dataset;
import sparqles.core.Endpoint;
import sparqles.core.EndpointFactory;

//http://datahub.io/api/2/search/resource?format=api/sparql&all_fields=1&limit=1000

public class DatahubAccess {
	private static final Logger log = LoggerFactory.getLogger(DatahubAccess.class);



	/**
	 * This class fetch the SPARQL endpoint list from datahub using the datahub API 
	 * @param epm 
	 **/
	public static Collection<Endpoint> checkEndpointList(){
		Map<String, Endpoint> results = new HashMap<String, Endpoint>();
		/* LOAD PROPERTIES */
		try {
			
			
			
			
			HttpClient httpclient = new DefaultHttpClient();
//			HttpClientConnectionManager connMrg = new BasicHttpClientConnectionManager();
//			CloseableHttpClient httpClient = HttpClients.custom()
//			        .setConnectionManager(connMrg)
//			        .build();
			
			HttpGet getRequest = new HttpGet("http://datahub.io/api/2/search/resource?format=api/sparql&all_fields=1&limit=1000");
			getRequest.addHeader("User-Agent", CONSTANTS.USER_AGENT);
			
			
			HttpResponse response = httpclient.execute(getRequest);
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}
			String respString = EntityUtils.toString(response.getEntity());
//			response.close();
			
			JsonFactory factory = new JsonFactory();
			ObjectMapper mapper = new ObjectMapper(factory);
			JsonNode rootNode = mapper.readTree(respString);  

			JsonNode res = rootNode.get("results");
			log.info("We found {} datasets",res.size());
			Iterator<JsonNode> iter = res.getElements();
			int c=1;
			
			
			Map<String,Set<String>> map = new HashMap<String, Set<String>>();
			while(iter.hasNext()){
				JsonNode node = iter.next();
				String endpointURL = node.findPath("url").getTextValue().trim();
				String datasetId = node.findPath("package_id").getTextValue().trim();
				
				Set<String> s = map.get(endpointURL);
				if(s==null){
					s= new HashSet<String>();
					map.put(endpointURL, s);
				}
				s.add(datasetId);
			}
			for(Entry<String,Set<String>> ent: map.entrySet()){
				String endpointURL = ent.getKey(); 

				if(endpointURL.length()==0) continue;
				
				Endpoint ep = results.get(endpointURL);
				if(ep == null){
					try {
						ep = EndpointFactory.newEndpoint(new URI(endpointURL));
						List<Dataset> l = new ArrayList<Dataset>();
						ep.setDatasets(l);
						results.put(endpointURL, ep);
					} catch (URISyntaxException e) {
						log.warn("URISyntaxException:{}",e.getMessage());
					}
				}
				if(ent.getValue().size()!=0){
					for(String ds : ent.getValue()){
						ep = checkForDataset(ep,ds,httpclient );
						log.info("Found dataset information for {}", ep);
					}
				}else{
					System.err.println("This should not happend for ep"+ep);
				}
				if(c==49){
					System.out.println("ASDASD");
				}
				log.info("[GET] [{}] {}",c++,ep);
			}
//			httpClient.getConnectionManager().shutdown();
		} catch (Exception e2) {
			log.warn("[EXEC] {}",e2);
			e2.printStackTrace();
		} 
		log.info("Found {} endpoints",results.size());
		
		return results.values();
	}

	private static Endpoint checkForDataset(Endpoint ep, String datasetId, HttpClient httpClient){
		log.debug("[GET] dataset info for {} and {}", datasetId,ep);
		HttpGet getRequest = null;
		try {
			getRequest = new HttpGet("http://datahub.io/api/2/rest/dataset/"+datasetId);
			getRequest.addHeader("User-Agent", CONSTANTS.USER_AGENT);
			HttpResponse response = httpClient.execute(getRequest);
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}
			String respString = EntityUtils.toString(response.getEntity());
//			response.close();
			
			JsonFactory factory = new JsonFactory();
			ObjectMapper mapper = new ObjectMapper(factory);
			JsonNode rootNode = mapper.readTree(respString); 

//			System.out.println(rootNode);
			String ckan_url = rootNode.findPath("ckan_url").getTextValue();
			List<JsonNode> titles  = rootNode.findValues("title");
			String title = null;
			for(JsonNode s : titles){
//				System.out.println(s);
				if(!s.toString().contains("Linking Open"))
					title = s.asText();
				
			}
					

			Dataset d = new Dataset();
			d.setLabel(title);
			d.setUri(ckan_url);
			List<Dataset> l =  ep.getDatasets();
			l.add(d);
//			ep.setDatasets(l);

			return ep;

		} catch (Exception e) {
			log.warn("[EXEC] {}",e);
		} 
		return ep;
	}
}
