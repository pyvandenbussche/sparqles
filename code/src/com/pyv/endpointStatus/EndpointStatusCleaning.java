package com.pyv.endpointStatus;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.openrdf.model.Literal;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;


public class EndpointStatusCleaning {
	
	
	private String CKAN_SPARQL_ENDPOINT = null;
	private String CKAN_PACKAGE_PATH = null;
	private String TRIPLE_STORE_URL= null;
	private String TEMPLATE_HTML_PAGE_PATH= null;
	private String TEMPLATE_DETAILS_HTML_PAGE_PATH = null;
	private String OUTPUT_HTML_PAGE_PATH = null;
	private String OUTPUT_STATS_AVAILABILITY = null;
	private String OUTPUT_RSS_PAGE_PATH = null;
	private String OUTPUT_RSS_OVERALL_FEED_NAME = null;
	private String OUTPUT_DETAILS_HTML_PAGE_PATH = null;
	private String OUTPUT_HTML_PAGE_NAME = null;
	private String HOMEPAGE = null;
	
	 public static void main(String[] args) {new EndpointStatusCleaning();}
	 public EndpointStatusCleaning() {
		 
		 /*INITIALISATION*/
		 	/* LOAD PROPERTIES */
			 	try {
					Properties prop = new Properties();
					String fileName = "endpointStatus.config";
					InputStream is = new FileInputStream(fileName);
					prop.load(is);
					CKAN_SPARQL_ENDPOINT= prop.getProperty("CKAN_SPARQL_ENDPOINT");
					CKAN_PACKAGE_PATH= prop.getProperty("CKAN_PACKAGE_PATH");
					TRIPLE_STORE_URL= prop.getProperty("TRIPLE_STORE_URL");
					TEMPLATE_HTML_PAGE_PATH= prop.getProperty("TEMPLATE_HTML_PAGE_PATH");
					TEMPLATE_DETAILS_HTML_PAGE_PATH= prop.getProperty("TEMPLATE_DETAILS_HTML_PAGE_PATH");
					OUTPUT_HTML_PAGE_PATH= prop.getProperty("OUTPUT_HTML_PAGE_PATH");
					OUTPUT_STATS_AVAILABILITY= prop.getProperty("OUTPUT_STATS_AVAILABILITY");
					OUTPUT_RSS_PAGE_PATH= prop.getProperty("OUTPUT_RSS_PAGE_PATH");
					OUTPUT_RSS_OVERALL_FEED_NAME= prop.getProperty("OUTPUT_RSS_OVERALL_FEED_NAME");
					OUTPUT_DETAILS_HTML_PAGE_PATH= prop.getProperty("OUTPUT_DETAILS_HTML_PAGE_PATH");
					OUTPUT_HTML_PAGE_NAME= prop.getProperty("OUTPUT_HTML_PAGE_NAME");
					HOMEPAGE= prop.getProperty("HOMEPAGE");
				} catch (FileNotFoundException e2) {
					e2.printStackTrace();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
//				cleanOldEndpointsURI();
//				cleanRemoveOldEndpointsURI();
//				cleanAddDatasetInfoWhenMissing();
//				cleanRemoveEndpointsWhenNoData();
				
				cleanRemoveWrongRespTime();
	    }
	
	 
	 
	 /*
	  *	Replace any old datasetURI "http://ckan.net/package/" || "http://thedatahub.org/package/" 
	  * with new datahub ones "http://thedatahub.org/dataset/"
	  */
	 private void cleanOldEndpointsURI(){
		 try {
			HTTPRepository rep = new HTTPRepository(TRIPLE_STORE_URL);
			rep.initialize();
			RepositoryConnection con = rep.getConnection();
				
			String query= 
				" SELECT distinct ?dataset \n"+
				" WHERE{ \n"+
				" ?dataset ends:status ?status. \n"+
				" } ORDER BY DESC(?dataset) ";
			
			/* CONNECTION AU ENDPOINT ET RECUPERATION DES RESULTATS */
			ResultSet resultSet=null;
				resultSet = EndpointStatusUtil.executeQuery(EndpointStatusUtil.PREFIXES+query,TRIPLE_STORE_URL);
				 
				while(resultSet.hasNext()){
					 QuerySolution soln = resultSet.nextSolution() ;
					 String datasetURI = soln.get("dataset").toString();
					 if(datasetURI.contains("http://ckan.net/package/")|| datasetURI.contains("http://thedatahub.org/package/")){
						 String newURI =datasetURI;
						 newURI=newURI.replaceAll("http://ckan.net/package/", "http://thedatahub.org/dataset/");
						 newURI=newURI.replaceAll("http://thedatahub.org/package/", "http://thedatahub.org/dataset/");
						 
						 System.out.println(datasetURI+"\t"+newURI);
						 String queryGroup=
								"CONSTRUCT{ " +
									"<"+datasetURI+"> ?p ?o." +
								"}\n" +
								"WHERE{ " +
									"<"+datasetURI+"> ?p ?o." +
								"}";
						 GraphQueryResult graphToRemove = con.prepareGraphQuery(QueryLanguage.SPARQL, EndpointStatusUtil.PREFIXES+ queryGroup).evaluate();
						 
						 queryGroup=
									"CONSTRUCT{ " +
										"<"+newURI+"> ?p ?o." +
									"}\n" +
									"WHERE{ " +
										"<"+datasetURI+"> ?p ?o." +
									"}";
						 GraphQueryResult graphToAdd = con.prepareGraphQuery(QueryLanguage.SPARQL, EndpointStatusUtil.PREFIXES+ queryGroup).evaluate();
						 con.remove(graphToRemove);
						 con.add(graphToAdd);

					 }					 
				}
				con.close();
				System.out.println("cleanOldEndpointsURI finished \n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	 }
	 
	 /*
	  *	Remove all datasetURI != "http://thedatahub.org/dataset/"
	  */
	 private void cleanRemoveOldEndpointsURI(){
		 try {
			HTTPRepository rep = new HTTPRepository(TRIPLE_STORE_URL);
			rep.initialize();
			RepositoryConnection con = rep.getConnection();
				
			String query= 
				" SELECT distinct ?dataset \n"+
				" WHERE{ \n"+
				" ?dataset ends:status ?status. \n"+
				" } ORDER BY DESC(?dataset) ";
			
			/* CONNECTION AU ENDPOINT ET RECUPERATION DES RESULTATS */
			ResultSet resultSet=null;
				resultSet = EndpointStatusUtil.executeQuery(EndpointStatusUtil.PREFIXES+query,TRIPLE_STORE_URL);
				 
				while(resultSet.hasNext()){
					 QuerySolution soln = resultSet.nextSolution() ;
					 String datasetURI = soln.get("dataset").toString();
					 if(!datasetURI.contains("http://thedatahub.org/dataset/")){
						 
						 System.out.println("Removing"+ datasetURI);
						 String queryGroup=
								"CONSTRUCT{ " +
									"<"+datasetURI+"> ?p ?o. ?o ?pp ?oo." +
								"}\n" +
								"WHERE{ " +
									"<"+datasetURI+"> ?p ?o. ?o ?pp ?oo." +
								"}";
						 GraphQueryResult graphToRemove = con.prepareGraphQuery(QueryLanguage.SPARQL, EndpointStatusUtil.PREFIXES+ queryGroup).evaluate();
						 con.remove(graphToRemove);
					 }					 
				}
				con.close();
				System.out.println("cleanRemoveOldEndpointsURI finished \n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	 }
	 
	 /*
	  *	Remove all datasetURI != "http://thedatahub.org/dataset/"
	  */
	 private void cleanRemoveEndpointsWhenNoData(){
		 try {
			HTTPRepository rep = new HTTPRepository(TRIPLE_STORE_URL);
			rep.initialize();
			RepositoryConnection con = rep.getConnection();
				
			String query= 
				" SELECT distinct ?dataset \n"+
				" WHERE{ \n"+
				" ?dataset a void:Dataset. \n"+
				" FILTER NOT EXISTS{ ?dataset ends:status ?status.} \n"+
				" } ORDER BY DESC(?dataset) ";
			
			/* CONNECTION AU ENDPOINT ET RECUPERATION DES RESULTATS */
			TupleQueryResult resultTuple = con.prepareTupleQuery(QueryLanguage.SPARQL, EndpointStatusUtil.PREFIXES+ query.toString()).evaluate();	
			while (resultTuple.hasNext()) {
				BindingSet bindingSet = resultTuple.next();
				String datasetURI= bindingSet.getBinding("dataset").getValue().toString();

				System.out.println("Removing "+ datasetURI);
				 String queryGroup=
						" CONSTRUCT{ " +
							"<"+datasetURI+"> ?p ?o." +
						"}\n" +
						"WHERE{ " +
							"<"+datasetURI+"> ?p ?o." +
						"}";
				 GraphQueryResult graphToRemove = con.prepareGraphQuery(QueryLanguage.SPARQL, EndpointStatusUtil.PREFIXES+ queryGroup).evaluate();
				 con.remove(graphToRemove);
				
				//for each dataset, create a new one with datahub
				
			}
			con.close();
			System.out.println("cleanRemoveOldEndpointsURI finished \n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	 }
	 
	 /*
	  *	Remove all datasetURI != "http://thedatahub.org/dataset/"
	  */
	 private void cleanRemoveWrongRespTime(){
		 try {
			HTTPRepository rep = new HTTPRepository(TRIPLE_STORE_URL);
			rep.initialize();
			RepositoryConnection con = rep.getConnection();
				
			String query= 
				" SELECT distinct ?dataset \n"+
				" WHERE{ \n"+
				" ?dataset a void:Dataset. \n"+
				" } ORDER BY DESC(?dataset) ";
			
			/* CONNECTION AU ENDPOINT ET RECUPERATION DES RESULTATS */
			TupleQueryResult resultTuple = con.prepareTupleQuery(QueryLanguage.SPARQL, EndpointStatusUtil.PREFIXES+ query.toString()).evaluate();	
			while (resultTuple.hasNext()) {
				BindingSet bindingSet = resultTuple.next();
				String datasetURI= bindingSet.getBinding("dataset").getValue().toString();

				System.out.println("Removing "+ datasetURI);
				 String queryGroup=
						" CONSTRUCT{ " +
							"?o <http://labs.mondeca.com/vocab/endpointStatus#responseTime> \"false\"^^<http://www.w3.org/2001/XMLSchema#boolean> ." +
						"}\n" +
						"WHERE{ " +
							"<"+datasetURI+"> ?p ?o. ?o <http://labs.mondeca.com/vocab/endpointStatus#responseTime> \"false\"^^<http://www.w3.org/2001/XMLSchema#boolean> ." +
						"}";
				 GraphQueryResult graphToRemove = con.prepareGraphQuery(QueryLanguage.SPARQL, EndpointStatusUtil.PREFIXES+ queryGroup).evaluate();
				 con.remove(graphToRemove);
				 
				 queryGroup=
							" CONSTRUCT{ " +
								"?o <http://labs.mondeca.com/vocab/endpointStatus#responseTime> \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean> ." +
							"}\n" +
							"WHERE{ " +
								"<"+datasetURI+"> ?p ?o. ?o <http://labs.mondeca.com/vocab/endpointStatus#responseTime> \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean> ." +
							"}";
					 graphToRemove = con.prepareGraphQuery(QueryLanguage.SPARQL, EndpointStatusUtil.PREFIXES+ queryGroup).evaluate();
					 con.remove(graphToRemove);
				
				//for each dataset, create a new one with datahub
				
			}
			con.close();
			System.out.println("cleanRemoveOldEndpointsURI finished \n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	 }
	 
	 /*
	  *	Add information on dataset URI node when missing
	  */
	 private void cleanAddDatasetInfoWhenMissing(){
		 try {
			HTTPRepository rep = new HTTPRepository(TRIPLE_STORE_URL);
			rep.initialize();
			RepositoryConnection con = rep.getConnection();
				
			
			/*
			 * datasets 
			 */
			StringBuilder query= new StringBuilder();
			query.append(" SELECT distinct ?datasetURI \n");
			query.append(" WHERE{ \n");
			query.append(" {?datasetURI ends:status ?status.} \n");
			query.append(" FILTER NOT EXISTS{ ?datasetURI rdf:type ?type.} \n");
			query.append(" } ORDER BY DESC(?datasetURI) ");
			
			TupleQueryResult resultTuple = con.prepareTupleQuery(QueryLanguage.SPARQL, EndpointStatusUtil.PREFIXES+ query.toString()).evaluate();	
			while (resultTuple.hasNext()) {
				BindingSet bindingSet = resultTuple.next();
				String datasetURI= bindingSet.getBinding("datasetURI").getValue().toString();
				System.out.println(datasetURI);
				
				
				//for each dataset, create a new one with datahub
				addEndpointInfoFromDatahub(datasetURI,con);
				
			}
			
			
			con.close();
			System.out.println("cleanAddDatasetInfoWhenMissing finished \n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	 }
	 
	 
	 private void addEndpointInfoFromDatahub(String datasetURI, RepositoryConnection endpointCon){
		 try {
			 /* 
			  * Connection to Datahub endpoint to fetch dataset infos
			  * */
				String service=  CKAN_SPARQL_ENDPOINT;
				StringBuilder query= new StringBuilder();
				query.append("SELECT DISTINCT ?endpoint ?title ?identifier  WHERE {");
				query.append("	<"+datasetURI+"> <http://www.w3.org/ns/dcat#distribution> ?distribution."); 
				query.append("	?distribution <http://purl.org/dc/terms/format> [<http://www.w3.org/1999/02/22-rdf-syntax-ns#value> \"api/sparql\"].");
				query.append("	?distribution <http://www.w3.org/ns/dcat#accessURL> ?endpoint.");
				query.append("	<"+datasetURI+"> <http://purl.org/dc/terms/title> ?title.");
				query.append("	<"+datasetURI+"> <http://purl.org/dc/terms/identifier> ?identifier.");
				query.append("} ORDER BY ?title");
				
				HTTPRepository rep = new HTTPRepository(service);
				RepositoryConnection con = rep.getConnection();
				ValueFactory vf = endpointCon.getValueFactory();
				
				TupleQueryResult resultTuple = con.prepareTupleQuery(QueryLanguage.SPARQL, EndpointStatusUtil.PREFIXES+ query.toString()).evaluate();	
				if(!resultTuple.hasNext()){
					endpointCon.add(vf.createStatement(vf.createURI(datasetURI), vf.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), vf.createURI("http://rdfs.org/ns/void#Dataset")));
				}
				while (resultTuple.hasNext()) {
					BindingSet bindingSet = resultTuple.next();
					String endpoint= bindingSet.getBinding("endpoint").getValue().toString();
					String identifier= bindingSet.getBinding("identifier").getValue().toString();
					String title= ((Literal)bindingSet.getBinding("title").getValue()).getLabel().toString();
					
					//add statements
					endpointCon.add(vf.createStatement(vf.createURI(datasetURI), vf.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), vf.createURI("http://rdfs.org/ns/void#Dataset")));
					endpointCon.add(vf.createStatement(vf.createURI(datasetURI), vf.createURI("http://rdfs.org/ns/void#sparqlEndpoint"), vf.createLiteral(endpoint)));
					endpointCon.add(vf.createStatement(vf.createURI(datasetURI), vf.createURI("http://purl.org/dc/terms/title"), vf.createLiteral(title)));
					endpointCon.add(vf.createStatement(vf.createURI(datasetURI), vf.createURI("http://purl.org/dc/terms/identifier"), vf.createLiteral(identifier)));
					System.out.println(endpoint+"\t"+identifier+"\t"+title);
					
				}
				con.close();
		 } catch (Exception e) {
			e.printStackTrace();
		}
			
	}
	
	
}

