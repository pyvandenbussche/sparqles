package com.pyv.endpointStatus;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.n3.N3Writer;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.rio.trig.TriGWriter;
import org.openrdf.rio.trix.TriXWriter;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.sail.memory.MemoryStore;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.pyv.endpointStatus.objects.AnalyticsPerWeek;
import com.pyv.endpointStatus.objects.DatasetAnalytics;


public class EndpointStatusFiller {
	
	
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
	private String OUTPUT_ARCHIVES_PATH = null;
	private String HOMEPAGE = null;
	private static DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+01:00'");
	private static DateFormat sdf2 = new SimpleDateFormat("yyyy-MM");
	private RepositoryConnection memCon = null;
	
	 public static void main(String[] args) {new EndpointStatusFiller();}
	 public EndpointStatusFiller() {
		 
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
					OUTPUT_ARCHIVES_PATH= prop.getProperty("OUTPUT_ARCHIVES_PATH");
					HOMEPAGE= prop.getProperty("HOMEPAGE");
					
					Repository memRepository = new SailRepository(new MemoryStore());
					memRepository.initialize();
					memCon = memRepository.getConnection();
			 	
					
					
				 	Calendar begin = Calendar.getInstance();
					 begin.set(2012, 2, 24);  
					 begin.set(Calendar.HOUR_OF_DAY,0);
					 begin.set(Calendar.MINUTE,0);
					 begin.set(Calendar.SECOND, 0);
					 
					 Calendar end = Calendar.getInstance();
					 end.set(2012, 7, 23);
					 begin.set(Calendar.HOUR_OF_DAY,0);
					 begin.set(Calendar.MINUTE,0);
					 begin.set(Calendar.SECOND, 0);
				 	generate(begin,end);
				 	
				 	
				 	
				 	end.add(Calendar.MONTH, -1);
					String filePath = OUTPUT_ARCHIVES_PATH+"archive_"+sdf2.format(begin.getTime())+"_to_"+sdf2.format(end.getTime())+".n3";
					 end.add(Calendar.MONTH, +1);
					File file = new File(filePath);
					if(file.exists())file.delete();
					file.createNewFile();
					WriteRepositoryToFile(file, memCon, RDFFormat.N3);
				 	
				 	memCon.close();
			 	
			 	} catch (FileNotFoundException e2) {
					e2.printStackTrace();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
	    }
	
	 
	 
	 private void generate(Calendar begin, Calendar end){
		 
		 
			try {
				 
				HTTPRepository rep = new HTTPRepository(TRIPLE_STORE_URL);
				rep.initialize();
				RepositoryConnection con = rep.getConnection();
				
				
				
				 
				/* fetch list datasets 1 week before begin => listBefore */
				List<String> listBefore =  new ArrayList<String>();
				StringBuilder query = new StringBuilder();
				query.append("SELECT distinct ?dataset \n");
				query.append("WHERE{ ");
				query.append("?dataset a void:Dataset. \n");
				query.append("?dataset ends:status ?status. ");
				query.append("?status dcterms:date ?statusDate. \n");
				query.append("FILTER ( ?statusDate < \""+sdf.format(begin.getTime())+"\"^^xsd:dateTime ) \n");
//					 System.out.print(sdf.format(begin.getTime())+"\t");
				begin.add(Calendar.WEEK_OF_YEAR, -1);
				query.append("FILTER ( ?statusDate > \""+sdf.format(begin.getTime())+"\"^^xsd:dateTime ) \n");		 
				query.append("}");
				begin.add(Calendar.WEEK_OF_YEAR, 1);
				TupleQueryResult resultTuple = con.prepareTupleQuery(QueryLanguage.SPARQL, EndpointStatusUtil.PREFIXES+ query.toString()).evaluate();	
				while (resultTuple.hasNext()) {
					BindingSet bindingSet = resultTuple.next();
					String datasetURI= bindingSet.getBinding("dataset").getValue().toString();
					listBefore.add(datasetURI);
				}
				
		 
				/* fetch list datasets 1 week after begin => listAfter */
				List<String> listAfter =  new ArrayList<String>();
				query = new StringBuilder();
				query.append("SELECT distinct ?dataset  \n");
				query.append("WHERE{ ");
				query.append("?dataset a void:Dataset. \n");
				query.append("?dataset ends:status ?status. ");
				query.append("?status dcterms:date ?statusDate. \n");
				query.append("FILTER ( ?statusDate > \""+sdf.format(end.getTime())+"\"^^xsd:dateTime ) \n");
//					 System.out.print(sdf.format(begin.getTime())+"\t");
				end.add(Calendar.WEEK_OF_YEAR, 1);
				query.append("FILTER ( ?statusDate < \""+sdf.format(end.getTime())+"\"^^xsd:dateTime ) \n");		 
				query.append("}");
				end.add(Calendar.WEEK_OF_YEAR, -1);
				resultTuple = con.prepareTupleQuery(QueryLanguage.SPARQL, EndpointStatusUtil.PREFIXES+ query.toString()).evaluate();	
				while (resultTuple.hasNext()) {
					BindingSet bindingSet = resultTuple.next();
					String datasetURI= bindingSet.getBinding("dataset").getValue().toString();
					listAfter.add(datasetURI);
				}
		 
				
				
				/* list datasets added =>listAdded*/
				listAfter.removeAll(listBefore);
				List<String> listAdded = new ArrayList<String>();
				listAdded.addAll(listAfter);
//				for (int i = 0; i < listAfter.size(); i++) {
//					System.out.println(listAfter.get(i));
//				}
		 
				
				 
				int cptJesus=0;
				int cptCrash=0;
				int cptNormal=0;
				int cptAlwaysDown=0;
				
				/* ############# LISTBEFORE ################## */
				System.out.println("########### PROCESSING LIST BEFORE ##############");
				 /* calculate the number of days between the 2 dates */
				long diffDays= nbDaysBetweenCalendars(begin,end);
				/* for each dataset from listBefore, calculate %avail the week before begin and the week after end */
				List<DatasetAnalytics> datasetsAnalyticsBefore =StatPerEndpointPerWeek(listBefore,begin,-1,con);
				List<DatasetAnalytics> datasetsAnalyticsAfter =StatPerEndpointPerWeek(listBefore,end,1,con);
				
				for (int i = 0; i < datasetsAnalyticsBefore.size(); i++) {
//				for (int i = 0; cptNormal < 20; i++) {
					DatasetAnalytics datasetAnalytics = datasetsAnalyticsBefore.get(i);
					String datasetURI = datasetAnalytics.getDatasetURI();
					double availBefore = ((Double.parseDouble(""+datasetAnalytics.getAnalytics().get(0).getNbTestsTrue())/Double.parseDouble(""+datasetAnalytics.getAnalytics().get(0).getNbTests())));
					
					
					/* case begin avail >0% */
					if(availBefore>0){
						for (int j = 0; j < datasetsAnalyticsAfter.size(); j++) {
							if(datasetsAnalyticsAfter.get(j).getDatasetURI().equals(datasetURI)){
								DatasetAnalytics datasetAnalyticsAfter = datasetsAnalyticsAfter.get(i);
								double availAfter = ((Double.parseDouble(""+datasetAnalyticsAfter.getAnalytics().get(0).getNbTestsTrue())/Double.parseDouble(""+datasetAnalyticsAfter.getAnalytics().get(0).getNbTests())));
							
								/* case end avail >0% : normal case*/
								if(availAfter>0){
									cptNormal++;
									normalGenStrategy(datasetURI,availBefore,availAfter,begin,end,diffDays,false,con);
								
								}
								
								/* case end avail ==0% : crash down one day*/
								else{
									cptCrash++;
									crashGenStrategy(datasetURI,availBefore,availAfter,begin,end,diffDays,false,con);
								}
								break;
							}
						}
					}
					
					/* case begin avail==0% */
					else{
						for (int j = 0; j < datasetsAnalyticsAfter.size(); j++) {
							if(datasetsAnalyticsAfter.get(j).getDatasetURI().equals(datasetURI)){
								DatasetAnalytics datasetAnalyticsAfter = datasetsAnalyticsAfter.get(i);
								double availAfter = ((Double.parseDouble(""+datasetAnalyticsAfter.getAnalytics().get(0).getNbTestsTrue())/Double.parseDouble(""+datasetAnalyticsAfter.getAnalytics().get(0).getNbTests())));
							
								/* case end avail >0% : jesus one day*/
								if(availAfter>0){
									cptJesus++;
									jesusGenStrategy(datasetURI,availBefore,availAfter,begin,end,diffDays,false,con);
								}
								
								/* case end avail ==0% : always down*/
								else{
									cptAlwaysDown++;
									allDownGenStrategy(datasetURI,availBefore,availAfter,begin,end,diffDays,false,con);
								}
								break;
							}
						}
					}
					
				}
				
				
				System.out.println("cptJesus \t"+cptJesus);
				System.out.println("cptCrash \t"+cptCrash);
				System.out.println("cptNormal \t"+cptNormal);
				System.out.println("cptAlwaysDown \t"+cptAlwaysDown);
				
				
				
				
				
				
				/* ############# LISTADDED ################## */
				System.out.println("\n########### PROCESSING LIST ADDED ##############");
				/* for each dataset from listAdded, calculate %avail the week week after end */
				List<DatasetAnalytics> datasetsAnalyticsAdded =StatPerEndpointPerWeek(listAdded,end,1,con);
				for (int i = 0; i < datasetsAnalyticsAdded.size(); i++) {
					DatasetAnalytics datasetAnalytics = datasetsAnalyticsAdded.get(i);
					String datasetURI = datasetAnalytics.getDatasetURI();
					
					double availAfter = ((Double.parseDouble(""+datasetAnalytics.getAnalytics().get(0).getNbTestsTrue())/Double.parseDouble(""+datasetAnalytics.getAnalytics().get(0).getNbTests())));
					cptJesus++;
					jesusGenStrategy(datasetURI,0,availAfter,begin,end,diffDays,true,con);
				}
				
				 con.close();
			} catch (Exception e) {
					e.printStackTrace();
			}
			 
		}
	 
	 
	 private void normalGenStrategy(String datasetURI, double availBefore,double availAfter,Calendar begin, Calendar end, long diffDays, boolean isAdded,RepositoryConnection con){
		 try {
			//store the begin date so we can reinitialize the value afterwards
			int tempYear = begin.get(Calendar.YEAR);
			int tempMonth = begin.get(Calendar.MONTH);
			int tempDay = begin.get(Calendar.DAY_OF_MONTH);
			/*
			 * STRATEGY:
			 * 
			 * If availbefore<1 or availAfter<1  get 1 error type (descr) after end
			 * 
			 * get 50 resp time before begin and 50 after end -> rand(min,max) 
			 * 
			 */
			String errorDescr = "";
			if (availAfter < 1)
				errorDescr = fetchErrorDescr(datasetURI, end, 1, con);
			else if (availBefore < 1)
				errorDescr = fetchErrorDescr(datasetURI, begin, -1, con);
//			Integer[] respTimeRange = fetchRespTimeRange(datasetURI, end, 1,con);
			/* each hour generate a result */
			while (begin.before(end)) {

				//add variance on seconds
				int timeVariance = getRandomInteger(62, 186);
				begin.add(Calendar.SECOND, timeVariance);

				ValueFactory vf = memCon.getValueFactory();
				BNode statusNode = vf.createBNode();
				memCon.add(vf.createStatement(vf.createURI(datasetURI),vf.createURI("http://labs.mondeca.com/vocab/endpointStatus#status"),statusNode));
				memCon.add(vf.createStatement(statusNode,vf.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),vf.createURI("http://labs.mondeca.com/vocab/endpointStatus#EndpointStatus")));
				memCon.add(vf.createStatement(statusNode,vf.createURI("http://purl.org/dc/terms/date"),vf.createLiteral(sdf.format(begin.getTime()), vf.createURI("http://www.w3.org/2001/XMLSchema#dateTime"))));
				boolean isAvailable = getRandomBoolean(availBefore,availAfter,nbDaysBetweenCalendars(begin, end),diffDays);
				if(isAvailable){
					memCon.add(vf.createStatement(statusNode,vf.createURI("http://labs.mondeca.com/vocab/endpointStatus#statusIsAvailable"),vf.createLiteral("true", vf.createURI("http://www.w3.org/2001/XMLSchema#boolean"))));
					memCon.add(vf.createStatement(statusNode,vf.createURI("http://purl.org/dc/terms/description"),vf.createLiteral("Endpoint is operating normally")));
//					memCon.add(vf.createStatement(statusNode,vf.createURI("http://labs.mondeca.com/vocab/endpointStatus#responseTime"),vf.createLiteral(getRandomInteger(respTimeRange[0],respTimeRange[1])+"", vf.createURI("http://www.w3.org/2001/XMLSchema#integer"))));
				}
				else{
					memCon.add(vf.createStatement(statusNode,vf.createURI("http://labs.mondeca.com/vocab/endpointStatus#statusIsAvailable"),vf.createLiteral("false", vf.createURI("http://www.w3.org/2001/XMLSchema#boolean"))));
					memCon.add(vf.createStatement(statusNode,vf.createURI("http://purl.org/dc/terms/description"),vf.createLiteral(errorDescr)));
//					memCon.add(vf.createStatement(statusNode,vf.createURI("http://labs.mondeca.com/vocab/endpointStatus#responseTime"),vf.createLiteral(getRandomInteger(respTimeRange[0],respTimeRange[1])+"", vf.createURI("http://www.w3.org/2001/XMLSchema#integer"))));
				}

				begin.add(Calendar.SECOND, -timeVariance);
				begin.add(Calendar.HOUR, 1);
			}
			begin.set(Calendar.YEAR, tempYear);
			begin.set(Calendar.MONTH, tempMonth);
			begin.set(Calendar.DAY_OF_MONTH, tempDay);
		} catch (Exception e) {
			e.printStackTrace();
		}
	 }
	 
	 private void crashGenStrategy(String datasetURI, double availBefore,double availAfter,Calendar begin, Calendar end, long diffDays, boolean isAdded,RepositoryConnection con){
		 try {
				//store the begin date so we can reinitialize the value afterwards
				int tempYear = begin.get(Calendar.YEAR);
				int tempMonth = begin.get(Calendar.MONTH);
				int tempDay = begin.get(Calendar.DAY_OF_MONTH);
				
				Calendar temp = Calendar.getInstance();
				temp.set(Calendar.YEAR, tempYear);
				temp.set(Calendar.MONTH, tempMonth);
				temp.set(Calendar.DAY_OF_MONTH, tempDay);
				
				/*
				 * STRATEGY:
				 * end date randomly between begin and and
				 * If availbefore<1 or availAfter<1  get 1 error type (descr) after end
				 * 
				 * get 50 resp time before begin and 50 after end -> rand(min,max) 
				 * 
				 */
				temp.add(Calendar.DAY_OF_YEAR, getRandomInteger(1,(int)diffDays));
				
				
				String errorDescr = "";
				if (availAfter < 1)
					errorDescr = fetchErrorDescr(datasetURI, end, 1, con);
				else if (availBefore < 1)
					errorDescr = fetchErrorDescr(datasetURI, begin, -1, con);
//				Integer[] respTimeRange = fetchRespTimeRange(datasetURI, end, 1,con);
				/* each hour generate a result */
				while (begin.before(temp)) {

					//add variance on seconds
					int timeVariance = getRandomInteger(62, 186);
					begin.add(Calendar.SECOND, timeVariance);

					ValueFactory vf = memCon.getValueFactory();
					BNode statusNode = vf.createBNode();
					memCon.add(vf.createStatement(vf.createURI(datasetURI),vf.createURI("http://labs.mondeca.com/vocab/endpointStatus#status"),statusNode));
					memCon.add(vf.createStatement(statusNode,vf.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),vf.createURI("http://labs.mondeca.com/vocab/endpointStatus#EndpointStatus")));
					memCon.add(vf.createStatement(statusNode,vf.createURI("http://purl.org/dc/terms/date"),vf.createLiteral(sdf.format(begin.getTime()), vf.createURI("http://www.w3.org/2001/XMLSchema#dateTime"))));
					boolean isAvailable = getRandomBoolean(availBefore,availBefore,nbDaysBetweenCalendars(begin, temp),diffDays);
					if(isAvailable){
						memCon.add(vf.createStatement(statusNode,vf.createURI("http://labs.mondeca.com/vocab/endpointStatus#statusIsAvailable"),vf.createLiteral("true", vf.createURI("http://www.w3.org/2001/XMLSchema#boolean"))));
						memCon.add(vf.createStatement(statusNode,vf.createURI("http://purl.org/dc/terms/description"),vf.createLiteral("Endpoint is operating normally")));
//						memCon.add(vf.createStatement(statusNode,vf.createURI("http://labs.mondeca.com/vocab/endpointStatus#responseTime"),vf.createLiteral(getRandomInteger(respTimeRange[0],respTimeRange[1])+"", vf.createURI("http://www.w3.org/2001/XMLSchema#integer"))));
					}
					else{
						memCon.add(vf.createStatement(statusNode,vf.createURI("http://labs.mondeca.com/vocab/endpointStatus#statusIsAvailable"),vf.createLiteral("false", vf.createURI("http://www.w3.org/2001/XMLSchema#boolean"))));
						memCon.add(vf.createStatement(statusNode,vf.createURI("http://purl.org/dc/terms/description"),vf.createLiteral(errorDescr)));
					}

					begin.add(Calendar.SECOND, -timeVariance);
					begin.add(Calendar.HOUR, 1);
				}
				
				while (begin.before(end)) {

					//add variance on seconds
					int timeVariance = getRandomInteger(62, 186);
					begin.add(Calendar.SECOND, timeVariance);

					ValueFactory vf = memCon.getValueFactory();
					BNode statusNode = vf.createBNode();
					memCon.add(vf.createStatement(vf.createURI(datasetURI),vf.createURI("http://labs.mondeca.com/vocab/endpointStatus#status"),statusNode));
					memCon.add(vf.createStatement(statusNode,vf.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),vf.createURI("http://labs.mondeca.com/vocab/endpointStatus#EndpointStatus")));
					memCon.add(vf.createStatement(statusNode,vf.createURI("http://purl.org/dc/terms/date"),vf.createLiteral(sdf.format(begin.getTime()), vf.createURI("http://www.w3.org/2001/XMLSchema#dateTime"))));
					memCon.add(vf.createStatement(statusNode,vf.createURI("http://labs.mondeca.com/vocab/endpointStatus#statusIsAvailable"),vf.createLiteral("false", vf.createURI("http://www.w3.org/2001/XMLSchema#boolean"))));
					memCon.add(vf.createStatement(statusNode,vf.createURI("http://purl.org/dc/terms/description"),vf.createLiteral(errorDescr)));

					begin.add(Calendar.SECOND, -timeVariance);
					begin.add(Calendar.HOUR, 1);
				}
				begin.set(Calendar.YEAR, tempYear);
				begin.set(Calendar.MONTH, tempMonth);
				begin.set(Calendar.DAY_OF_MONTH, tempDay);
			} catch (Exception e) {
				e.printStackTrace();
			}
		 }
	 
	 private void jesusGenStrategy(String datasetURI, double availBefore,double availAfter,Calendar begin, Calendar end, long diffDays, boolean isAdded,RepositoryConnection con){
		 try {
				//store the begin date so we can reinitialize the value afterwards
				int tempYear = begin.get(Calendar.YEAR);
				int tempMonth = begin.get(Calendar.MONTH);
				int tempDay = begin.get(Calendar.DAY_OF_MONTH);
				
				Calendar temp = Calendar.getInstance();
				temp.set(Calendar.YEAR, tempYear);
				temp.set(Calendar.MONTH, tempMonth);
				temp.set(Calendar.DAY_OF_MONTH, tempDay);
				int nbDaysResurection=getRandomInteger(1,(int)diffDays);
				temp.add(Calendar.DAY_OF_YEAR, nbDaysResurection);
				
				
				String errorDescr = "";
				if (availAfter < 1)
					errorDescr = fetchErrorDescr(datasetURI, end, 1, con);
				else if (availBefore < 1)
					errorDescr = fetchErrorDescr(datasetURI, begin, -1, con);
//				Integer[] respTimeRange = fetchRespTimeRange(datasetURI, end, 1,con);
				/* each hour generate a result */
				
				
				if(!isAdded){
					while (begin.before(temp)) {

						//add variance on seconds
						int timeVariance = getRandomInteger(62, 186);
						begin.add(Calendar.SECOND, timeVariance);

						ValueFactory vf = memCon.getValueFactory();
						BNode statusNode = vf.createBNode();
						memCon.add(vf.createStatement(vf.createURI(datasetURI),vf.createURI("http://labs.mondeca.com/vocab/endpointStatus#status"),statusNode));
						memCon.add(vf.createStatement(statusNode,vf.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),vf.createURI("http://labs.mondeca.com/vocab/endpointStatus#EndpointStatus")));
						memCon.add(vf.createStatement(statusNode,vf.createURI("http://purl.org/dc/terms/date"),vf.createLiteral(sdf.format(begin.getTime()), vf.createURI("http://www.w3.org/2001/XMLSchema#dateTime"))));
						memCon.add(vf.createStatement(statusNode,vf.createURI("http://labs.mondeca.com/vocab/endpointStatus#statusIsAvailable"),vf.createLiteral("false", vf.createURI("http://www.w3.org/2001/XMLSchema#boolean"))));
						memCon.add(vf.createStatement(statusNode,vf.createURI("http://purl.org/dc/terms/description"),vf.createLiteral(errorDescr)));

						begin.add(Calendar.SECOND, -timeVariance);
						begin.add(Calendar.HOUR, 1);
					}
				}
				else{
					begin.add(Calendar.DAY_OF_YEAR, nbDaysResurection);
				}
				while (begin.before(end)) {

					//add variance on seconds
					int timeVariance = getRandomInteger(62, 186);
					begin.add(Calendar.SECOND, timeVariance);

					ValueFactory vf = memCon.getValueFactory();
					BNode statusNode = vf.createBNode();
					memCon.add(vf.createStatement(vf.createURI(datasetURI),vf.createURI("http://labs.mondeca.com/vocab/endpointStatus#status"),statusNode));
					memCon.add(vf.createStatement(statusNode,vf.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),vf.createURI("http://labs.mondeca.com/vocab/endpointStatus#EndpointStatus")));
					memCon.add(vf.createStatement(statusNode,vf.createURI("http://purl.org/dc/terms/date"),vf.createLiteral(sdf.format(begin.getTime()), vf.createURI("http://www.w3.org/2001/XMLSchema#dateTime"))));
					boolean isAvailable = getRandomBoolean(availAfter,availAfter,nbDaysBetweenCalendars(begin, end),diffDays);
					if(isAvailable){
						memCon.add(vf.createStatement(statusNode,vf.createURI("http://labs.mondeca.com/vocab/endpointStatus#statusIsAvailable"),vf.createLiteral("true", vf.createURI("http://www.w3.org/2001/XMLSchema#boolean"))));
						memCon.add(vf.createStatement(statusNode,vf.createURI("http://purl.org/dc/terms/description"),vf.createLiteral("Endpoint is operating normally")));
//						memCon.add(vf.createStatement(statusNode,vf.createURI("http://labs.mondeca.com/vocab/endpointStatus#responseTime"),vf.createLiteral(getRandomInteger(respTimeRange[0],respTimeRange[1])+"", vf.createURI("http://www.w3.org/2001/XMLSchema#integer"))));
					}
					else{
						memCon.add(vf.createStatement(statusNode,vf.createURI("http://labs.mondeca.com/vocab/endpointStatus#statusIsAvailable"),vf.createLiteral("false", vf.createURI("http://www.w3.org/2001/XMLSchema#boolean"))));
						memCon.add(vf.createStatement(statusNode,vf.createURI("http://purl.org/dc/terms/description"),vf.createLiteral(errorDescr)));
					}

					begin.add(Calendar.SECOND, -timeVariance);
					begin.add(Calendar.HOUR, 1);
				}
				begin.set(Calendar.YEAR, tempYear);
				begin.set(Calendar.MONTH, tempMonth);
				begin.set(Calendar.DAY_OF_MONTH, tempDay);
			} catch (Exception e) {
				e.printStackTrace();
			}
		 }
	 
	 private void allDownGenStrategy(String datasetURI, double availBefore,double availAfter,Calendar begin, Calendar end, long diffDays, boolean isAdded,RepositoryConnection con){
		 try {
				//store the begin date so we can reinitialize the value afterwards
				int tempYear = begin.get(Calendar.YEAR);
				int tempMonth = begin.get(Calendar.MONTH);
				int tempDay = begin.get(Calendar.DAY_OF_MONTH);
				
				
				
				String errorDescr = "";
				if (availAfter < 1)
					errorDescr = fetchErrorDescr(datasetURI, end, 1, con);
				else if (availBefore < 1)
					errorDescr = fetchErrorDescr(datasetURI, begin, -1, con);
//				Integer[] respTimeRange = fetchRespTimeRange(datasetURI, end, 1,con);
				/* each hour generate a result */
				
				
					while (begin.before(end)) {

						//add variance on seconds
						int timeVariance = getRandomInteger(62, 186);
						begin.add(Calendar.SECOND, timeVariance);

						ValueFactory vf = memCon.getValueFactory();
						BNode statusNode = vf.createBNode();
						memCon.add(vf.createStatement(vf.createURI(datasetURI),vf.createURI("http://labs.mondeca.com/vocab/endpointStatus#status"),statusNode));
						memCon.add(vf.createStatement(statusNode,vf.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),vf.createURI("http://labs.mondeca.com/vocab/endpointStatus#EndpointStatus")));
						memCon.add(vf.createStatement(statusNode,vf.createURI("http://purl.org/dc/terms/date"),vf.createLiteral(sdf.format(begin.getTime()), vf.createURI("http://www.w3.org/2001/XMLSchema#dateTime"))));
						memCon.add(vf.createStatement(statusNode,vf.createURI("http://labs.mondeca.com/vocab/endpointStatus#statusIsAvailable"),vf.createLiteral("false", vf.createURI("http://www.w3.org/2001/XMLSchema#boolean"))));
						memCon.add(vf.createStatement(statusNode,vf.createURI("http://purl.org/dc/terms/description"),vf.createLiteral(errorDescr)));

						begin.add(Calendar.SECOND, -timeVariance);
						begin.add(Calendar.HOUR, 1);
					}
				begin.set(Calendar.YEAR, tempYear);
				begin.set(Calendar.MONTH, tempMonth);
				begin.set(Calendar.DAY_OF_MONTH, tempDay);
			} catch (Exception e) {
				e.printStackTrace();
			}
		 }
	 
	 
	 private List<DatasetAnalytics> StatPerEndpointPerWeek(List<String> datasets, Calendar date, int weekAddOrRemove, RepositoryConnection con){
		 try {
				
				List<DatasetAnalytics> datasetsAnalytics = new ArrayList<DatasetAnalytics>(400);
				
				
				for (int i = 0; i < datasets.size(); i++) {
					String datasetURI = datasets.get(i);
				
					//fetch dataset Info
					 String queryDatasets="SELECT distinct ?title ?identifier ?endpoint \n" +
						"WHERE{ <"+datasetURI+"> a void:Dataset.\n" +	
						"	   OPTIONAL{<"+datasetURI+"> <http://purl.org/dc/terms/title> ?title.} "+ 
						"	   OPTIONAL{<"+datasetURI+"> <http://purl.org/dc/terms/identifier> ?identifier.} "+
						"	   OPTIONAL{<"+datasetURI+"> void:sparqlEndpoint ?endpoint.} "+
						"} LIMIT 1 ";
					 TupleQueryResult resultsDatasets = con.prepareTupleQuery(QueryLanguage.SPARQL, EndpointStatusUtil.PREFIXES+ queryDatasets).evaluate();
					 for ( ; resultsDatasets.hasNext() ; ){
						 
							BindingSet bindDataset = resultsDatasets.next() ;
							DatasetAnalytics da = new DatasetAnalytics();
							if(bindDataset.getValue("endpoint")!=null){
								String endpoint = bindDataset.getValue("endpoint").stringValue().toString();
								da.setEndpointURL(endpoint);
							}
							if(bindDataset.getValue("title")!=null){
								String title = ((Literal)bindDataset.getValue("title")).stringValue().toString();
								da.setDatasetTitle(title);
							}
							if(bindDataset.getValue("title")!=null){
								String identifier = ((Literal)bindDataset.getValue("identifier")).stringValue().toString();
								da.setDatasetId(identifier);
							}
							
							da.setDatasetURI(datasetURI);
							
							
							
							
							 int nbTestsTot=0;
							 int nbTestsTrueTot=0;
//							 System.out.println(datasetURI);
								 int nbTestsWeek=0;
								 int nbTestsTrueWeek=0;
								 String query="SELECT ?availability \n" +
									"WHERE{ " +
										"<"+datasetURI+"> a void:Dataset. \n" +
										"<"+datasetURI+"> ends:status ?status. " +
										"?status dcterms:date ?statusDate. \n" +
										"?status ends:statusIsAvailable ?availability. \n";
								 
								 if(weekAddOrRemove<0)date.add(Calendar.WEEK_OF_YEAR, weekAddOrRemove);
								 query+="FILTER ( ?statusDate > \""+sdf.format(date.getTime())+"\"^^xsd:dateTime ) \n";
	//							 System.out.print(sdf.format(begin.getTime())+"\t");
								 if(weekAddOrRemove>0)date.add(Calendar.WEEK_OF_YEAR, weekAddOrRemove);
								 else date.add(Calendar.WEEK_OF_YEAR, -(weekAddOrRemove));
								 	query+="FILTER ( ?statusDate < \""+sdf.format(date.getTime())+"\"^^xsd:dateTime ) \n"+			 
									"}";
								 if(weekAddOrRemove>0)date.add(Calendar.WEEK_OF_YEAR, -(weekAddOrRemove));
								 	
//								 	System.out.println(query);
								 
								 	TupleQueryResult results = con.prepareTupleQuery(QueryLanguage.SPARQL, EndpointStatusUtil.PREFIXES+ query).evaluate();
								 	
								 	for ( ; results.hasNext() ; ){
										BindingSet bind = results.next() ;
										nbTestsWeek++;
										nbTestsTot++;
										if(((Literal)bind.getValue("availability")).booleanValue()==true){
											nbTestsTrueTot++;
											nbTestsTrueWeek++;
										}
									}
								 	AnalyticsPerWeek a = new AnalyticsPerWeek();
								 	a.setDate(date.get(Calendar.YEAR)+"_"+date.get(Calendar.MONTH)+"_"+date.get(Calendar.DAY_OF_MONTH));
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
				}
					
				 
				return datasetsAnalytics;
				 		
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
	 }
	 
	 private String fetchErrorDescr(String datasetURI, Calendar date,int weekAddOrRemove, RepositoryConnection con){
		 
		 	
//		 	System.out.println(query);
		 
		 	try {
				StringBuilder query = new StringBuilder();
				query.append("SELECT ?descr \n");
				query.append("WHERE{ ");
				query.append("<"+ datasetURI+ "> a void:Dataset. \n");
				query.append("<"+ datasetURI+ "> ends:status ?status. ");
				query.append("?status dcterms:date ?statusDate. \n");
				query.append("?status ends:statusIsAvailable \"false\"^^xsd:boolean. \n");
				if (weekAddOrRemove < 0)
					date.add(Calendar.WEEK_OF_YEAR, weekAddOrRemove);
				query.append("FILTER ( ?statusDate > \""+ sdf.format(date.getTime()) + "\"^^xsd:dateTime ) \n");
				//							 System.out.print(sdf.format(begin.getTime())+"\t");
				if (weekAddOrRemove > 0)
					date.add(Calendar.WEEK_OF_YEAR, weekAddOrRemove);
				else
					date.add(Calendar.WEEK_OF_YEAR, -(weekAddOrRemove));
				query.append("FILTER ( ?statusDate < \""+ sdf.format(date.getTime()) + "\"^^xsd:dateTime ) \n");
				query.append("?status dcterms:description ?descr. \n" + "}LIMIT 1");
				if (weekAddOrRemove > 0)
					date.add(Calendar.WEEK_OF_YEAR, -(weekAddOrRemove));
				TupleQueryResult results = con.prepareTupleQuery(QueryLanguage.SPARQL,EndpointStatusUtil.PREFIXES + query.toString()).evaluate();
				for (; results.hasNext();) {
					BindingSet bind = results.next();
					return ((Literal) bind.getValue("descr")).stringValue();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "";
	 }
	 
	 private Integer[] fetchRespTimeRange(String datasetURI, Calendar date,int weekAddOrRemove, RepositoryConnection con){
		 
		 Integer[] out = new Integer[2];
		 out[0]=19;
		 out[1]=8769;
//		 	System.out.println(query);
		 
		 	try {
				StringBuilder query = new StringBuilder();
				query.append("SELECT (MIN(?respTime) as ?avgRespTime) \n");
				query.append("WHERE{ ");
				query.append("<"+ datasetURI+ "> a void:Dataset. \n");
				query.append("<"+ datasetURI+ "> ends:status ?status. ");
				query.append("?status dcterms:date ?statusDate. \n");
				query.append("?status ends:responseTime ?respTime. \n");
				if (weekAddOrRemove < 0)
					date.add(Calendar.WEEK_OF_YEAR, weekAddOrRemove);
				query.append("FILTER ( ?statusDate > \""+ sdf.format(date.getTime()) + "\"^^xsd:dateTime ) \n");
				//							 System.out.print(sdf.format(begin.getTime())+"\t");
				if (weekAddOrRemove > 0)
					date.add(Calendar.WEEK_OF_YEAR, weekAddOrRemove);
				else
					date.add(Calendar.WEEK_OF_YEAR, -(weekAddOrRemove));
				query.append("FILTER ( ?statusDate < \""+ sdf.format(date.getTime()) + "\"^^xsd:dateTime ) \n");
				query.append("}");
				if (weekAddOrRemove > 0)
					date.add(Calendar.WEEK_OF_YEAR, -(weekAddOrRemove));
				TupleQueryResult results = con.prepareTupleQuery(QueryLanguage.SPARQL,EndpointStatusUtil.PREFIXES + query.toString()).evaluate();
				for (; results.hasNext();) {
					BindingSet bind = results.next();
					out[0] = Integer.parseInt(((Literal) bind.getValue("avgRespTime")).stringValue());
				}
				
				
				query = new StringBuilder();
				query.append("SELECT (MAX(?respTime) as ?avgRespTime) \n");
				query.append("WHERE{ ");
				query.append("<"+ datasetURI+ "> a void:Dataset. \n");
				query.append("<"+ datasetURI+ "> ends:status ?status. ");
				query.append("?status dcterms:date ?statusDate. \n");
				query.append("?status ends:responseTime ?respTime. \n");
				if (weekAddOrRemove < 0)
					date.add(Calendar.WEEK_OF_YEAR, weekAddOrRemove);
				query.append("FILTER ( ?statusDate > \""+ sdf.format(date.getTime()) + "\"^^xsd:dateTime ) \n");
				//							 System.out.print(sdf.format(begin.getTime())+"\t");
				if (weekAddOrRemove > 0)
					date.add(Calendar.WEEK_OF_YEAR, weekAddOrRemove);
				else
					date.add(Calendar.WEEK_OF_YEAR, -(weekAddOrRemove));
				query.append("FILTER ( ?statusDate < \""+ sdf.format(date.getTime()) + "\"^^xsd:dateTime ) \n");
				query.append("}");
				if (weekAddOrRemove > 0)
					date.add(Calendar.WEEK_OF_YEAR, -(weekAddOrRemove));
				TupleQueryResult results2 = con.prepareTupleQuery(QueryLanguage.SPARQL,EndpointStatusUtil.PREFIXES + query.toString()).evaluate();
				for (; results2.hasNext();) {
					BindingSet bind = results2.next();
					out[1] = Integer.parseInt(((Literal) bind.getValue("avgRespTime")).stringValue());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return out;
	 }
	
	 
	 private long nbDaysBetweenCalendars(Calendar begin, Calendar after){
		 long milliseconds1 = begin.getTimeInMillis();
		    long milliseconds2 = after.getTimeInMillis();
		    long diff = milliseconds2 - milliseconds1;
		    long diffDays = diff / (24 * 60 * 60 * 1000);
//		    System.out.println("\nDays between days = "+diffDays);
		    return diffDays;
	 }
	 
	 
	 private static int getRandomInteger(int aStart, int aEnd){
		    if ( aStart > aEnd ) {
		      throw new IllegalArgumentException("Start cannot exceed End.");
		    }
		    //get the range, casting to long to avoid overflow problems
		    long range = (long)aEnd - (long)aStart + 1;
		    // compute a fraction of the range, 0 <= frac < range
		    long fraction = (long)(range * Math.random());
		    return (int)(fraction + aStart);    
	 }
	 
	 private static boolean getRandomBoolean(double availBefore, double availAfter,double daysRemainingIndex, double diffDays){
		 double availMoy = ( (availBefore * (daysRemainingIndex/diffDays)) + (availAfter * (1-(daysRemainingIndex/diffDays))) );
//		 System.out.println(availMoy);
//		 System.out.println(Math.random() < availMoy);
		 return (Math.random() < availMoy);
	 }
	 
	 public static void WriteRepositoryToFile(File file ,RepositoryConnection con, RDFFormat format){
			try {
				
				if(file.exists())file.delete();
				file.createNewFile();
				FileOutputStream fileOut = new FileOutputStream(file);
				RDFWriter writer=null;
				if(format==null)writer = new RDFXMLWriter(fileOut);
				else if(format.equals(RDFFormat.RDFXML))writer = new RDFXMLWriter(fileOut);
				else if(format.equals(RDFFormat.N3))writer = new N3Writer(fileOut);
				else if(format.equals(RDFFormat.NTRIPLES))writer = new NTriplesWriter(fileOut);
				else if(format.equals(RDFFormat.TURTLE))writer = new TurtleWriter(fileOut);
				else if(format.equals(RDFFormat.TRIX))writer = new TriXWriter(fileOut);
				else if(format.equals(RDFFormat.TRIG))writer = new TriGWriter(fileOut);
//							rdfwriter.setBaseURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
				 
				con.export(writer, ((Resource)null) );
				fileOut.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
}

