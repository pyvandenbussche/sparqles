package com.pyv.endpointStatus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.pyv.endpointStatus.objects.EndpointResult;

//http://datahub.io/api/2/search/resource?format=api/sparql&all_fields=1&limit=1000

public class DatahubAccess {
	
	 public static void main(String[] args) {
		 checkEndpointList();}
	
	/**
	 * This class fetch the SPARQL endpoint list from datahub using the datahub API 
	 **/
	private static void checkEndpointList(){
		/* LOAD PROPERTIES */
	 	try {
			Properties prop = new Properties();
			String fileName = "endpointStatus.config";
			InputStream is = new FileInputStream(fileName);
			prop.load(is);
			String endpoints_list_store=  prop.getProperty("ENDPOINTS_LIST_STORE");
		
			File file = new File(endpoints_list_store);
			if(!file.exists())file.createNewFile();
			
			
			// Get datasets with SPARQL endpoint
			List<EndpointResult>  results = new ArrayList<EndpointResult>();
//			getEndpointDetails("dcc6715c-bf94-4a89-bbf3-35933da795a5"); //dbpedia
			 
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet getRequest = new HttpGet("http://datahub.io/api/2/search/resource?format=api/sparql&all_fields=1&limit=1000");
			HttpResponse response = httpClient.execute(getRequest);
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
				   + response.getStatusLine().getStatusCode());
			}
	 
			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
	 
			StringBuilder datasetIdList = new StringBuilder();
//			System.out.println("Output from Server .... \n");
			String line = "";
			while ((line = br.readLine()) != null) {
				datasetIdList.append(line);
			}
			JSONObject json = (JSONObject) JSONSerializer.toJSON( datasetIdList.toString() );        
	        JSONArray resources = json.getJSONArray("results");
	        for (int i = 0; i < resources.size(); i++) {
	        	
	        	// for each dataset (if not already existing with same value, fetch dataset information)
	        	JSONObject resource = (JSONObject)resources.get(i);
	        	String endpointURL = resource.getString("url");
	        	String datasetId = resource.getString("package_id");
	        	if(endpointURL.trim().length()>0){
		        	boolean alreadyExisting=false;
		        	for (int j = 0; j < results.size(); j++) {
						if(results.get(j).getPackageUuid().equals(datasetId) && results.get(j).getEndpointURL().equals(endpointURL)){
							alreadyExisting=true;
							break;
						}
					}
		        	if(!alreadyExisting) results.add(getEndpointDetails(datasetId,endpointURL));
	        	}
	        	
			}
//			Pattern pattern = Pattern.compile("\".+?\"");
//			Matcher matcher = pattern.matcher(datasetIdList.toString());
//			// Check all occurrence
//		    while (matcher.find()) {
////		      System.out.print("Start index: " + matcher.start());
////		      System.out.print(" End index: " + matcher.end() + " ");
//		    	String datasetId = matcher.group().subSequence(1, matcher.group().length()-1).toString();
//		    	results.add(getEndpointDetails(datasetId));
////		      	System.out.println(datasetId);
//		    }
	 
			httpClient.getConnectionManager().shutdown();
			
			
			//sort the list
			Collections.sort(results);
			
			
			//write down csv file
			StringBuilder st = new StringBuilder();
			for (int i = 0; i < results.size(); i++) {
				st.append(results.get(i).toCSV()+"\n");
			}
			
		     file.delete();
		     file.createNewFile();
	//	     System.out.println(st.toString());
		     // output         
		     BufferedWriter out = new BufferedWriter(new FileWriter(file));
			 out.write(st.toString());
			 out.close();
	 
	 	} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		} 
	}
	
	private static EndpointResult getEndpointDetails(String datasetId,String endpointURL){
		try {
			
			 
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet getRequest = new HttpGet("http://datahub.io/api/2/rest/dataset/"+datasetId);
			HttpResponse response = httpClient.execute(getRequest);
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
				   + response.getStatusLine().getStatusCode());
			}
	 
			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
	 
			StringBuilder datasetIdList = new StringBuilder();
//			System.out.println("Output from Server .... \n");
			String line = "";
			while ((line = br.readLine()) != null) {
				datasetIdList.append(line);
			}
			
			JSONObject json = (JSONObject) JSONSerializer.toJSON( datasetIdList.toString() );        
	        String ckan_url = json.getString( "ckan_url" );
	        String title = json.getString("title").trim();
	        String name = json.getString("name");
	        EndpointResult result = new EndpointResult();
	        result.setPackageUuid(datasetId);
			result.setPackageId(name);
	        result.setDatasetURI(ckan_url);
			result.setEndpointURL(endpointURL);
			result.setDatasetTitle(title);
	        
	        
	        System.out.println(datasetId+"\t"+ckan_url+"\t"+title+"\t"+name+"\t"+endpointURL);
//	        JSONObject pilot = json.getJSONObject("pilot");
	        
	 
			httpClient.getConnectionManager().shutdown();
			return result;
	 
		  } catch (ClientProtocolException e) {
	 
			e.printStackTrace();
	 
		  } catch (IOException e) {
	 
			e.printStackTrace();
		  }
		return null;
	}
	
	public static List<EndpointResult> getEndpointList(){
		List<EndpointResult> results = new ArrayList<EndpointResult>();
		/* LOAD PROPERTIES */
	 	try {
			Properties prop = new Properties();
			String fileName = "endpointStatus.config";
			InputStream is = new FileInputStream(fileName);
			prop.load(is);
			String endpoints_list_store=  prop.getProperty("ENDPOINTS_LIST_STORE");
		
			File file = new File(endpoints_list_store);
			// input
		     FileInputStream fis  = new FileInputStream(file);
		     BufferedReader in = new BufferedReader
		         (new InputStreamReader(fis));
	
		     String thisLine="";
		     while ((thisLine = in.readLine()) != null) {
		    	 EndpointResult res = new EndpointResult();
		    	 System.out.println(thisLine);
		    	 res.fromCSV(thisLine);
		    	 results.add(res);
		     }
		     in.close();
	//	     System.out.println(st.toString());     
		    
	 	} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		return results;
	}
}
