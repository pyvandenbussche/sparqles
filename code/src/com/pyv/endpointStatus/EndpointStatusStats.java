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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.openrdf.model.Literal;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.pyv.endpointStatus.objects.AnalyticsPerWeek;
import com.pyv.endpointStatus.objects.DatasetAnalytics;

public class EndpointStatusStats {
		
	private String CKAN_SPARQL_ENDPOINT = null;
	private String CKAN_PACKAGE_PATH = null;
	private String TRIPLE_STORE_URL= null;
	private String TEMPLATE_HTML_PAGE_PATH= null;
	private String TEMPLATE_DETAILS_HTML_PAGE_PATH = null;
	private String OUTPUT_HTML_PAGE_PATH = null;
	private String OUTPUT_STATS_AVAILABILITY = null;
	private String OUTPUT_STATS_LIB_NB_ENDPOINTS = null;
	private String OUTPUT_STATS_LIB_AVAILABILITY = null;
	private String OUTPUT_RSS_PAGE_PATH = null;
	private String OUTPUT_RSS_OVERALL_FEED_NAME = null;
	private String OUTPUT_DETAILS_HTML_PAGE_PATH = null;
	private String OUTPUT_HTML_PAGE_NAME = null;
	private String HOMEPAGE = null;
	
	 public static void main(String[] args) {new EndpointStatusStats();}
	 public EndpointStatusStats() {
		 
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
					OUTPUT_STATS_LIB_NB_ENDPOINTS= prop.getProperty("OUTPUT_STATS_LIB_NB_ENDPOINTS");
					OUTPUT_STATS_LIB_AVAILABILITY= prop.getProperty("OUTPUT_STATS_LIB_AVAILABILITY");
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
				
				Calendar now = Calendar.getInstance();
				//si on est dimanche alors on fait le calcul
				if(now.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ){
					//ajoute nombre de endpoints la semaine dernière
					EvolutionWeekByWeek();
					//récupère les stats sur la dernière semaine
					List<DatasetAnalytics> datasetsAnalytics =StatPerEndpointPerWeek();
					//ajoute taux endpoints down la semaine dernière
					trueFalsePerWeek(datasetsAnalytics);
				}
				
			 /* 
			  * récupération des endpoints
			  * */
				/* REQUETE SELECT */
				String query= 
					" SELECT ?dataset \n"+
					" WHERE{ \n"+
					" ?dataset a void:Dataset. \n"+
					" } ORDER BY DESC(?dataset) ";
				
				/* CONNECTION AU ENDPOINT ET RECUPERATION DES RESULTATS */
				ResultSet resultSet=null;
				try {
					resultSet = EndpointStatusUtil.executeQuery(EndpointStatusUtil.PREFIXES+query,TRIPLE_STORE_URL);
					StringBuilder builder = new StringBuilder();
					 
					 
					while(resultSet.hasNext()){
						 QuerySolution soln = resultSet.nextSolution() ;
						 String datasetURI = soln.get("dataset").toString();
						 
						 builder.append(datasetURI+";"+getAvailability(datasetURI,1)+";"+getAvailability(datasetURI,7)+";"+getAvailability(datasetURI,31)+"\n");
						 generateLastdays_weeks_monthAvailability(datasetURI);
						 
						 
					}
					
					File newFile = new File(OUTPUT_STATS_AVAILABILITY);

					if(newFile.exists())newFile.delete();
					try {
						newFile.createNewFile();
						BufferedWriter out = new BufferedWriter(new FileWriter(newFile));
						out.write(builder.toString());
						out.close();
					} catch (Exception e) {
						System.out.println("executeEnhancement()Problem when writing new page "+e.getMessage());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
	    }
	 
	 
	 private void generateLastdays_weeks_monthAvailability(String datasetURI){
		 
		 
		 /* ECRITURE DU FICHIER HTML PAGE PRINCIPALE*/
			
	 }
	 
	 /*
	  *	pour chaque semaine, regarde le nombre total d'endpoint et construit un histogramme avec le nbre de gris, rouge, orange et vert. 
	  */
	 private void EvolutionWeekByWeek(){
		 try {
			HTTPRepository rep = new HTTPRepository(TRIPLE_STORE_URL);
			rep.initialize();
			RepositoryConnection con = rep.getConnection();
			
				
			Calendar end = Calendar.getInstance();
					
			String builder = "";
			end.set(Calendar.HOUR_OF_DAY,0);
			end.set(Calendar.MINUTE,0);
			end.set(Calendar.SECOND, 0);
			
			DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+01:00'");
			 
			 String query="SELECT (count(distinct ?dataset) as ?count) \n" +
				"WHERE{ " +
					"?dataset a void:Dataset. \n" +
					"?dataset ends:status ?status. " +
					"?status dcterms:date ?statusDate. \n" +
					"?status ends:statusIsAvailable ?availability. \n" +
					"FILTER ( ?statusDate < \""+sdf.format(end.getTime())+"\"^^xsd:dateTime ) \n";
			 end.add(Calendar.WEEK_OF_YEAR, -1);
			 	query+="FILTER ( ?statusDate > \""+sdf.format(end.getTime())+"\"^^xsd:dateTime ) \n"+			 
				"} ORDER BY ?statusDate";
			 	System.out.println(query);
			 	
			 	TupleQueryResult results = con.prepareTupleQuery(QueryLanguage.SPARQL, EndpointStatusUtil.PREFIXES+ query).evaluate();
//				 	 ResultSet resultSet2 = EndpointStatusUtil.executeQuery(EndpointStatusUtil.PREFIXES+query,TRIPLE_STORE_URL);
			 	int nbTotal=-1;
			 	for ( ; results.hasNext() ; ){
					BindingSet bind = results.next() ;
//						System.out.println(sdf.format(begin.getTime())+"\t"+((Literal)bind.getValue("count")).intValue());
					nbTotal=((Literal)bind.getValue("count")).intValue();
					
					
				    builder = "{x:new Date("+end.get(Calendar.YEAR)+","+end.get(Calendar.MONTH)+","+end.get(Calendar.DAY_OF_MONTH)+"), " +
							"y:"+nbTotal+"},";
					
					}
			 	con.close();
			 	
			
			File file = new File(OUTPUT_STATS_LIB_NB_ENDPOINTS);
			// input
		     FileInputStream fis  = new FileInputStream(file);
		     BufferedReader in = new BufferedReader
		         (new InputStreamReader(fis));

		     StringBuilder st = new StringBuilder();
		     int i =1;
		     String thisLine="";
		     while ((thisLine = in.readLine()) != null) {
		       if(thisLine.equals("];")){
		    	   st.append("\n"+builder);
		    	   st.append("\n"+thisLine);
		       }
		       // ajout de la val max
		       else if(thisLine.contains("var Ymax = ")){
		    	   System.out.println(thisLine.substring(11, thisLine.indexOf(";")));
		    	   int ymax  = Integer.parseInt(thisLine.substring(11, thisLine.indexOf(";")));
		    	   if(ymax<nbTotal){
		    		   st.append("\n"+thisLine.substring(0, 11)+nbTotal+";");
		    	   }
		    	   else{
		    		   st.append("\n"+thisLine);
		    	   }
		       }
		       else{
		    	   if(st.length()!=0) st.append("\n");
		    	   st.append(thisLine);
		       }
		     }
		     in.close();
		     file.delete();
		     file.createNewFile();
//		     System.out.println(st.toString());
		     // output         
		     BufferedWriter out = new BufferedWriter(new FileWriter(file));
			 out.write(st.toString());
			 out.close();
		     
//			 System.out.println("var Ymin = "+min+";");
//			 System.out.println("var Ymax = "+max+";");
			 		
		} catch (Exception e) {
			e.printStackTrace();
		}
	 }
	 
	 private void trueFalsePerWeek(List<DatasetAnalytics> datasetsAnalytics){
		 try {
			if(datasetsAnalytics.size()>0){
				 DatasetAnalytics d =  datasetsAnalytics.get(0);
				 String firstArray= "";
				 String secondArray= "";
				 for(int j=0; j<d.getAnalytics().size();j++){
					 String date = d.getAnalytics().get(j).getDate();
					 int cptTotal = 0;
					 int cpttrue = 0;
					 for(int i=0; i<datasetsAnalytics.size();i++){
						 DatasetAnalytics da =  datasetsAnalytics.get(i);
						 AnalyticsPerWeek apw = da.getAnalyticsWithDate(date);
						 cptTotal+=apw.getNbTests();
						 cpttrue+=apw.getNbTestsTrue();
					 }
					 String[] dateSplit = date.split("_");
					 NumberFormat f = NumberFormat.getNumberInstance();  
				        f.setMinimumFractionDigits(2);  
				        f.setMaximumFractionDigits(2);
				        firstArray+="\t{x:new Date("+dateSplit[0]+","+dateSplit[1]+","+dateSplit[2]+"), " +
								"y:"+f.format(((Double.parseDouble(""+cpttrue)/Double.parseDouble(""+cptTotal)))*100).replace(",",".")+"},\n";
				        secondArray+="\t{x:new Date("+dateSplit[0]+","+dateSplit[1]+","+dateSplit[2]+"), " +
								"y:"+f.format(100-(((Double.parseDouble(""+cpttrue)/Double.parseDouble(""+cptTotal)))*100)).replace(",",".")+"},\n";
					
				 }
				 System.out.println("var data = ["+secondArray+"\t];");
				 
				 File file = new File(OUTPUT_STATS_LIB_AVAILABILITY);
					// input
				     FileInputStream fis  = new FileInputStream(file);
				     BufferedReader in = new BufferedReader
				         (new InputStreamReader(fis));

				     StringBuilder st = new StringBuilder();
				     int i =1;
				     String thisLine="";
				     while ((thisLine = in.readLine()) != null) {
				       if(thisLine.equals("];")){
				    	   st.append("\n"+secondArray);
				    	   st.append(thisLine);
				       }
				       else{
				    	   if(st.length()!=0)st.append("\n");
				    	   st.append(thisLine);
				       }
				     }
				     in.close();
				     file.delete();
				     file.createNewFile();
//				     System.out.println(st.toString());
				     // output         
				     BufferedWriter out = new BufferedWriter(new FileWriter(file));
					 out.write(st.toString());
					 out.close();
			 }
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }
	 
	 private List<DatasetAnalytics> StatPerEndpointPerWeek(){
		 try {
				HTTPRepository rep = new HTTPRepository(TRIPLE_STORE_URL);
				rep.initialize();
				RepositoryConnection con = rep.getConnection();
				
				List<DatasetAnalytics> datasetsAnalytics = new ArrayList<DatasetAnalytics>(300);
				
				//on récupère la liste des datasets
				 String queryDatasets="SELECT distinct ?dataset ?title ?identifier ?endpoint \n" +
					"WHERE{ ?dataset a void:Dataset.\n" +	
					"	   ?dataset <http://purl.org/dc/terms/title> ?title. "+ 
					"	   ?dataset <http://purl.org/dc/terms/identifier> ?identifier. "+
					"	   ?dataset void:sparqlEndpoint ?endpoint. "+
					"} ORDER BY ?dataset";
				 TupleQueryResult resultsDatasets = con.prepareTupleQuery(QueryLanguage.SPARQL, EndpointStatusUtil.PREFIXES+ queryDatasets).evaluate();
				 for ( ; resultsDatasets.hasNext() ; ){
						BindingSet bindDataset = resultsDatasets.next() ;
						String datasetURI = bindDataset.getValue("dataset").stringValue().toString();
						String endpoint = bindDataset.getValue("endpoint").stringValue().toString();
						String title = ((Literal)bindDataset.getValue("title")).stringValue().toString();
						String identifier = ((Literal)bindDataset.getValue("identifier")).stringValue().toString();
						DatasetAnalytics da = new DatasetAnalytics();
						da.setDatasetURI(datasetURI);
						da.setDatasetTitle(title);
						da.setDatasetId(identifier);
						da.setEndpointURL(endpoint);
						
						Calendar begin = Calendar.getInstance();
						 begin.set(Calendar.HOUR_OF_DAY,0);
						 begin.set(Calendar.MINUTE,0);
						 begin.set(Calendar.SECOND, 0);
//						 int min = -1;
//						 int max = -1;
						 int nbTestsTot=0;
						 int nbTestsTrueTot=0;
						 System.out.println(datasetURI);
							 DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+01:00'");
							 int nbTestsWeek=0;
							 int nbTestsTrueWeek=0;
							 String query="SELECT ?availability \n" +
								"WHERE{ " +
									"<"+datasetURI+"> a void:Dataset. \n" +
									"<"+datasetURI+"> ends:status ?status. " +
									"?status dcterms:date ?statusDate. \n" +
									"?status ends:statusIsAvailable ?availability. \n" +
									"FILTER ( ?statusDate < \""+sdf.format(begin.getTime())+"\"^^xsd:dateTime ) \n";
//							 System.out.print(sdf.format(begin.getTime())+"\t");
							 begin.add(Calendar.WEEK_OF_YEAR, -1);
							 	query+="FILTER ( ?statusDate > \""+sdf.format(begin.getTime())+"\"^^xsd:dateTime ) \n"+			 
								"} ORDER BY ?statusDate";
							 	
//							 	System.out.println(query);
							 
							 	TupleQueryResult results = con.prepareTupleQuery(QueryLanguage.SPARQL, EndpointStatusUtil.PREFIXES+ query).evaluate();
//							 	 ResultSet resultSet2 = EndpointStatusUtil.executeQuery(EndpointStatusUtil.PREFIXES+query,TRIPLE_STORE_URL);
							 	
							 	for ( ; results.hasNext() ; ){
									BindingSet bind = results.next() ;
									nbTestsWeek++;
									nbTestsTot++;
									if(((Literal)bind.getValue("availability")).booleanValue()==true){
										nbTestsTrueTot++;
										nbTestsTrueWeek++;
									}
								}
							 	con.close();
							 	AnalyticsPerWeek a = new AnalyticsPerWeek();
							 	a.setDate(begin.get(Calendar.YEAR)+"_"+begin.get(Calendar.MONTH)+"_"+begin.get(Calendar.DAY_OF_MONTH));
							 	a.setNbTests(nbTestsWeek);
							 	a.setNbTestsTrue(nbTestsTrueWeek);
							 	
							 	da.getAnalytics().add(a);
//							 	System.out.println("{x:new Date("+begin.get(Calendar.YEAR)+","+begin.get(Calendar.MONTH)+","+begin.get(Calendar.DAY_OF_MONTH)+"), y:"+nbTestsWeek+", z:"+nbTestsTrueWeek+"},");
						 
//						 System.out.println("var nbTestsTot = "+nbTestsTot+";");
//						 System.out.println("var nbTestsTrueTot = "+nbTestsTrueTot+";");
						 da.setNbTotalTests(nbTestsTot);
						 da.setNbTotalTestsTrue(nbTestsTrueTot);
						
						 datasetsAnalytics.add(da);
				 }
					
				 
				return datasetsAnalytics;
				 		
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
	 }
	 
	 private String getAvailability(String datasetURI,int windowDays){
		 Calendar now2 = Calendar.getInstance();
		 now2.add(Calendar.DATE, -windowDays);
		 DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+01:00'");
		 String OneDayBefore= sdf.format(now2.getTime());
		 String query="SELECT DISTINCT ?statusDate ?availability " +
 			"WHERE{ " +
 				"<"+datasetURI+"> ends:status ?status. " +
 				"?status dcterms:date ?statusDate. " +
 				"?status ends:statusIsAvailable ?availability. " +
 				"FILTER ( ?statusDate > \""+OneDayBefore+"\"^^xsd:dateTime ) " +
 			"} ORDER BY ?statusDate";
		 ResultSet resultSet2=null;
		 try {
			 
			 resultSet2 = EndpointStatusUtil.executeQuery(EndpointStatusUtil.PREFIXES+query,TRIPLE_STORE_URL);
		} catch (Exception e) {
			e.printStackTrace();
		}
			int nbActive=0;
			int nbTotal=0;
			for ( ; resultSet2.hasNext() ; ){
				QuerySolution soln2 = resultSet2.nextSolution() ;
				nbTotal++;
				if(((com.hp.hpl.jena.rdf.model.Literal)soln2.get("availability")).getLexicalForm().equalsIgnoreCase("true"))nbActive++;
			}
			
			Double uptime=new Double(0);
			if(nbTotal>0){
				uptime = Double.parseDouble(""+nbActive)/Double.parseDouble(""+nbTotal)*100;
				DecimalFormat df = new DecimalFormat () ; 
				df.setMaximumFractionDigits ( 2 ) ; //arrondi à 2 chiffres apres la virgules 
				return df.format(uptime.doubleValue());//.replaceAll(",", "."));
			}
			return "0";
	 }
	 
}

