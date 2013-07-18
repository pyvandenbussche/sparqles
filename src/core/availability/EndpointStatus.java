package core.availability;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.http.HTTPRepository;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.tidy.Tidy;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.pyv.endpointStatus.objects.EndpointResult;

	//TODO: ajouter un logger externe ?
	//TODO: mémoriser la dernière liste de CKAN au cas où le endpoitn n'est pas dispo.

public class EndpointStatus {
	
	private int cptNbActive=0;
	private Document doc;
//	private String dataset;
//	private String title;
//	private String endpointURL;
//	private String description;
//	private String identifier;
//	private String rating;
	private HTTPRepository repository;
//	private boolean isRetrieved;
//	private String failureExplanation=null;
//	private String responseTime=null;
//	private boolean hasChangedSinceLastTest=false;
	private StringBuffer rssFeedGlobal= new StringBuffer();
	
	private String CKAN_SPARQL_ENDPOINT = null;
	private String CKAN_PACKAGE_PATH = null;
	private String TRIPLE_STORE_URL= null;
	private String TEMPLATE_HTML_PAGE_PATH= null;
	private String TEMPLATE_DETAILS_HTML_PAGE_PATH = null;
	private String OUTPUT_HTML_PAGE_PATH = null;
	private String OUTPUT_RSS_PAGE_PATH = null;
	private String OUTPUT_RSS_OVERALL_FEED_NAME = null;
	private String OUTPUT_DETAILS_HTML_PAGE_PATH = null;
	private String OUTPUT_HTML_PAGE_NAME = null;
	private String OUTPUT_ARCHIVES_PATH = null;
	private String HOMEPAGE = null;
	
	 public static void main(String[] args) {new EndpointStatus();}
	 public EndpointStatus() {
		 
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
					OUTPUT_RSS_PAGE_PATH= prop.getProperty("OUTPUT_RSS_PAGE_PATH");
					OUTPUT_RSS_OVERALL_FEED_NAME= prop.getProperty("OUTPUT_RSS_OVERALL_FEED_NAME");
					OUTPUT_DETAILS_HTML_PAGE_PATH= prop.getProperty("OUTPUT_DETAILS_HTML_PAGE_PATH");
					OUTPUT_HTML_PAGE_NAME= prop.getProperty("OUTPUT_HTML_PAGE_NAME");
					OUTPUT_ARCHIVES_PATH= prop.getProperty("OUTPUT_ARCHIVES_PATH");
					HOMEPAGE= prop.getProperty("HOMEPAGE");
				} catch (FileNotFoundException e2) {
					e2.printStackTrace();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			    
			 /* CONNECTION AU TRIPLESTORE LOCAL */
				try {
					repository = EndpointStatusUtil.connectToRepository(TRIPLE_STORE_URL);
				} catch (RepositoryException e1) {
					e1.printStackTrace();
				}
				
			/* Historisation des triplets fenetre 1 mois */
//				new EndpointStatusArchiver(TRIPLE_STORE_URL,OUTPUT_ARCHIVES_PATH,1);
			
			 /* RECUPERATION DU TEMPLATE HTML */
				URL url;
				BufferedInputStream in=null;
				Tidy tidy = new Tidy();
				try {
					url = new URL(TEMPLATE_HTML_PAGE_PATH);
					in = new BufferedInputStream(url.openStream());
					tidy.setQuiet(true);
					tidy.setShowWarnings(false);
					doc = tidy.parseDOM(in, null);
				} catch (MalformedURLException e) {
					System.out.println("executeEnhancement()Page URL "+TEMPLATE_HTML_PAGE_PATH+" is not well formed");	
				} catch (IOException e) {
					System.out.println("executeEnhancement()Problem to access the URL "+TEMPLATE_HTML_PAGE_PATH);
				}
			 /* 
			  * Connection au endpoint CKAN et récupération de l'ensemble des endpoints avec leurs infos
			  * */
				String service=  CKAN_SPARQL_ENDPOINT;
				StringBuilder query= new StringBuilder();
				query.append("SELECT DISTINCT ?dataset ?endpoint ?title ?identifier  WHERE {");
				query.append("	?dataset <http://www.w3.org/ns/dcat#distribution> ?distribution."); 
				query.append("	?distribution <http://purl.org/dc/terms/format> [<http://www.w3.org/1999/02/22-rdf-syntax-ns#value> \"api/sparql\"].");
				query.append("	?distribution <http://www.w3.org/ns/dcat#accessURL> ?endpoint.");
				query.append("	?dataset <http://purl.org/dc/terms/title> ?title.");
				query.append("	?dataset <http://purl.org/dc/terms/identifier> ?identifier.");
				query.append("} ORDER BY ?title");
				
			
//				"SELECT DISTINCT ?dataset ?endpoint ?title ?identifier WHERE { "+
//				"	   ?dataset <http://www.w3.org/ns/dcat#distribution> ?distribution. "+
//				"	   ?distribution <http://purl.org/dc/terms/format> [ <http://moat-project.org/ns#taggedWithTag> [ <http://moat-project.org/ns#name> \"api/sparql\" ] ]. "+
//				"	   ?distribution <http://www.w3.org/ns/dcat#accessURL> ?endpoint. "+
//				"	   ?dataset <http://purl.org/dc/terms/title> ?title. "+ 
//				"	   ?dataset <http://purl.org/dc/terms/identifier> ?identifier. "+
//				"	 } ORDER BY ?title "; 
				
//				SELECT DISTINCT ?dataset ?endpoint ?title ?identifier WHERE { 
//					?dataset dcat:distribution ?distribution. 
//					?distribution dc:format [ moat:taggedWithTag [ moat:name "api/sparql" ] ]. 
//					?distribution dcat:accessURL ?endpoint. 
//					?dataset dc:title ?title. 
//					?dataset dc:identifier ?identifier.
//					} ORDER BY ?title
				
//				String endpoint= "http://fun.rkbexplorer.com/sparql";
//				String query="SELECT DISTINCT ?dataset ?endpoint ?title ?description ?rating ?identifier "+
//				"WHERE { "+
//					"?dataset <http://rdfs.org/ns/void#sparqlEndpoint> <"+endpoint+">. "+
//					"?dataset <http://rdfs.org/ns/void#sparqlEndpoint> ?endpoint. "+
//					"?dataset <http://purl.org/dc/terms/title> ?title. "+
//					"OPTIONAL{ ?dataset <http://purl.org/dc/terms/description> ?description. } "+
//					"OPTIONAL{ ?dataset <http://purl.org/stuff/rev#rating> ?rating. } "+
//					"?dataset <http://purl.org/dc/terms/identifier> ?identifier."+
//				"} "+
//				"ORDER BY ?title ";
				
				System.out.println(query.toString());
				
			/* CONNECTION AU ENDPOINT CKAN ET RECUPERATION DES DATASETS */
				ResultSet resultSet=null;
				try {
					resultSet = EndpointStatusUtil.executeQuery(query.toString(),service);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			/*
			 * DEBUT du flux rss global
			 */
			generateGlobalFeed_Begin();
			
			
			/*
			 * On récupère la liste des endpoints
			 */
			int nbTotal=0;
			List<AResult> results = new ArrayList<AResult>();
			for ( ; resultSet.hasNext() ; )
			{
				QuerySolution soln = resultSet.nextSolution() ;
				AResult result = new AResult();
				
				if(soln.get("identifier")!=null)
					result.setPackageId(((com.hp.hpl.jena.rdf.model.Literal)soln.get("identifier")).getLexicalForm());
				
				if(isIdentifierValid(result.getPackageId())){
					nbTotal++;
					result.setDatasetURI(soln.get("dataset").toString());
					result.setEndpointURL(soln.get("endpoint").toString());
					result.setDatasetTitle(((com.hp.hpl.jena.rdf.model.Literal)soln.get("title")).getLexicalForm());
//					System.out.print(endpointURL);
					results.add(result);
				}
			}
			
			
			/*
			 * On fait le test de dispo en multithreads
			 */
			try {
				List<Agent> agents = new ArrayList<Agent>(100);
				 ExecutorService executor = Executors.newCachedThreadPool();
				 for (int i = 0; i < results.size(); i++) {
					 agents.add(new Agent(results.get(i)));
				 }
				 executor.invokeAll(agents, 10, TimeUnit.SECONDS);
				 
				 	// This will make the executor accept no new threads
					// and finish all existing threads in the queue
					executor.shutdown();
					if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
						 executor.shutdownNow(); // Cancel currently executing tasks
						 System.out.println("Shutdown now :)");
					       // Wait a while for tasks to respond to being cancelled
					       if (!executor.awaitTermination(10, TimeUnit.SECONDS)){
					    	   for(int i=0; i<results.size(); i++){
					    		   AResult result = results.get(i);
					    		   if(result.getExplaination()==null)result.setExplaination("SPARQL Endpoint is timeout");
					    		   if(result.isAvailable())cptNbActive++;
					    		   
					    		   if(result.isPrivate())nbTotal--; // on n'affiche pas les endpoints privés
					    		   else{
						    		   addStatus(result);
						    		   boolean hasChangedSinceLastTest= addStatusToHTMLPage(i%2==0,result);
									   if(hasChangedSinceLastTest)generateFeed(result);
					    		   }
					    	   }
					       }
					}
		} catch (Exception e) {
			System.out.print("interrupted !\n");
			e.printStackTrace();
			
		}			
			
			
			System.out.println(cptNbActive+"/"+nbTotal+" endpoints");
			
			if(nbTotal>0){
				/* FIN DU FLUX RSS GENERAL */
				generateGlobalFeed_End(nbTotal);
				
				//last update time
				try {
					DecimalFormat df = new DecimalFormat () ; 
					df.setMaximumFractionDigits ( 2 ) ;
					
					DateFormat sdf = EndpointStatusUtil.DATE_FORMAT_RSS;  
					XObject tableNodes = XPathAPI.eval(doc, "//div[@id=\"last_update\"]");
					if(tableNodes.nodelist().getLength()>0){
						tableNodes.nodelist().item(0).appendChild(doc.createTextNode("Last update: "+sdf.format(Calendar.getInstance().getTime())));
					}
					tableNodes = XPathAPI.eval(doc, "//div[@id=\"endpoint_stats\"]");
					if(tableNodes.nodelist().getLength()>0){
						tableNodes.nodelist().item(0).appendChild(doc.createTextNode(cptNbActive+" / "+nbTotal+" ("+
								df.format(Double.parseDouble(""+cptNbActive)/Double.parseDouble(""+nbTotal)*100)+"%) Public SPARQL " +
										"Endpoints available / total"));
					}
				} catch (DOMException e1) {
					e1.printStackTrace();
				} catch (TransformerException e1) {
					e1.printStackTrace();
				}
				/* ECRITURE DU FICHIER HTML PAGE PRINCIPALE*/
				File newFile = new File(OUTPUT_HTML_PAGE_PATH+OUTPUT_HTML_PAGE_NAME);
				if(newFile.exists())newFile.delete();
				try {
					newFile.createNewFile();
					BufferedWriter out = new BufferedWriter(new FileWriter(newFile));
					DOMSource domSource = new DOMSource(doc);
		            StringWriter writer = new StringWriter();
		            StreamResult result = new StreamResult(writer);
		            TransformerFactory tf = TransformerFactory.newInstance();
		            Transformer transformer = tf.newTransformer();
		            transformer.transform(domSource, result);
		            writer.flush();
					out.write(writer.toString().replaceAll("<div id=\"facebook\"></div>", "<fb:like href=\"http://labs.mondeca.com/sparqlEndpointsStatus/index.html\" layout=\"button_count\" show_faces=\"false\" width=\"250\" action=\"recommend\" font=\"arial\"></fb:like>"));
					out.close();
				} catch (Exception e) {
					System.out.println("executeEnhancement()Problem when writing new page "+e.getMessage());
				}
				
				System.exit(0);
			}
	    }
	 
	 
	 private boolean isIdentifierValid(String identifier){
		 int numberDigit=0;
		 for (int i = 0; i < identifier.length(); i++) {
			 if (Character.isDigit(identifier.charAt(i))) {
				 numberDigit++;
			 }
		 }
		 return (numberDigit<6);
	 }
	
	 private void generateGlobalFeed_Begin(){
		 String now = EndpointStatusUtil.DATE_FORMAT_RSS.format(Calendar.getInstance().getTime());
		 
		// Si le fichier exsite déjà, alors ajouter juste le nouvel item. 
		 File newFile = new File(OUTPUT_RSS_PAGE_PATH+OUTPUT_RSS_OVERALL_FEED_NAME+".xml");
		 if(!newFile.exists()){
			 rssFeedGlobal.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?> \n");
			 rssFeedGlobal.append("<rss version=\"2.0\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:content=\"http://purl.org/rss/1.0/modules/content/\"> \n");
			 rssFeedGlobal.append("\t<channel>\n");
			 rssFeedGlobal.append("\t\t<title>Open SPARQL Endpoints Status</title>\n");
			 rssFeedGlobal.append("\t\t<link>"+HOMEPAGE+"</link>\n");
			 rssFeedGlobal.append("\t\t<description>Test Availability of all open SPARQL Endpoint</description>\n");
			 rssFeedGlobal.append("\t\t<dc:creator>Pierre-Yves Vandenbussche</dc:creator>\n");
			 rssFeedGlobal.append("\t\t<language>en</language>\n");
			 rssFeedGlobal.append("\t\t<webMaster>pierre-yves.vandenbussche@mondeca.com</webMaster>\n");
			 rssFeedGlobal.append("\t\t<lastBuildDate>"+now+"</lastBuildDate>\n");
			 rssFeedGlobal.append("\t\t<item>\n");
		 }
		 else rssFeedGlobal.append("<item>\n");
		 
		 rssFeedGlobal.append("\t\t\t<title>Overall open SPARQL endpoint status news</title>\n");
		 rssFeedGlobal.append("\t\t\t<description>"+EndpointStatusUtil.DATE_FORMAT_RSS.format(Calendar.getInstance().getTime())+"&lt;h2&gt;List of recent status changements:&lt;/h2&gt;&lt;ul&gt;");
	 }
	 
	 
	 
	 private void generateGlobalFeed_End(int nbResultsTotal){
		 String now = EndpointStatusUtil.DATE_FORMAT_RSS.format(Calendar.getInstance().getTime());
		 DecimalFormat df = new DecimalFormat () ; 
			df.setMaximumFractionDigits ( 2 ) ; 
		 if(!rssFeedGlobal.toString().contains("&lt;li&gt;")){
			 rssFeedGlobal.append("&lt;li&gt;No changement&lt;/li&gt;");
		 }
		 
		
		 rssFeedGlobal.append("&lt;/ul&gt;</description>\n");
		 rssFeedGlobal.append("\t\t\t<link>"+HOMEPAGE+"</link>\n");
		 rssFeedGlobal.append("\t\t\t<guid>"+HOMEPAGE+" "+now+"</guid>\n");
		 rssFeedGlobal.append("\t\t\t<dc:creator>Pierre-Yves Vandenbussche</dc:creator>\n");
		 rssFeedGlobal.append("\t\t\t<pubDate>"+now+"</pubDate>\n");
		 rssFeedGlobal.append("\t\t</item>\n");
		 
		 File newFile = new File(OUTPUT_RSS_PAGE_PATH+OUTPUT_RSS_OVERALL_FEED_NAME+".xml");
		 if(!newFile.exists()){
			 rssFeedGlobal.append("\t</channel>\n");
			 rssFeedGlobal.append("</rss>\n");
		 }
		 
		 //dans tous les cas, ajouter l'info du nombre de endpoitns dispo dans la description
		 String rssFeedGlobalString = rssFeedGlobal.toString().replaceFirst("<description>", "<description>"+cptNbActive+" / "+nbResultsTotal+" ("+
					df.format(Double.parseDouble(""+cptNbActive)/Double.parseDouble(""+nbResultsTotal)*100)+"%) endpoints available / total");
		 
		 
		 
		 /*
		  * si le fichier existe déjà, 
		  * 	-remplacer le last build date
		  * 	-ajouter le nouvel item en tete
		  * 	-si existe plus de 24 items alors virer le dernier
		  */		 
		 if(newFile.exists()){
		//lecture du fichier texte	
			String fileString="";
			try{
				InputStream ips=new FileInputStream(newFile); 
				InputStreamReader ipsr=new InputStreamReader(ips);
				BufferedReader br=new BufferedReader(ipsr);
				
				String ligne;
				while ((ligne=br.readLine())!=null){
					fileString+=ligne+"\n";
				}
				br.close(); 
			}		
			catch (Exception e){
				System.out.println("generateGlobalFeed_End: "+e.getMessage());
			}
			//remplacer le last build date
			fileString=fileString.replaceAll("<lastBuildDate>.*</lastBuildDate>", "<lastBuildDate>"+now+"</lastBuildDate>");
			
			//si existe plus de 24 items alors virer le dernier
			int index = 0;
			int start = 0;
			int end=0;
			int nbMatch=0;
			while(index <fileString.length()){
				start = fileString.indexOf("<item>",index);
				if(start>0){
					end= fileString.indexOf("</item>",index);
					if(end>0){
						nbMatch++;
						if(nbMatch>24){
							fileString=fileString.substring(0, start-3)+fileString.substring((end+7),(fileString.length()));
						}
						else{
							index=(end+7);
						}
					}
					else index=fileString.length();
				}
				else index=fileString.length();
			}
	        
	        //ajouter le nouvel item en tete
	        fileString=fileString.replaceFirst("<item>",rssFeedGlobalString +"\t\t<item>");
	        /* ECRITURE DU FICHIER XML RSS FEEDS*/
			
			newFile.delete();
			try {
				newFile.createNewFile();
				BufferedWriter out = new BufferedWriter(new FileWriter(newFile));
	            StringWriter writer = new StringWriter();
	            writer.flush();
				out.write(fileString);
				out.close();
			} catch (Exception e) {
				System.out.println("Problem when writing new page "+e.getMessage());
			}
		 }
		 else{
			 try {
					newFile.createNewFile();
					BufferedWriter out = new BufferedWriter(new FileWriter(newFile));
		            StringWriter writer = new StringWriter();
		            writer.flush();
					out.write(rssFeedGlobalString);
					out.close();
				} catch (Exception e) {
					System.out.println("Problem when writing new page "+e.getMessage());
				}
		 }
		 
		
			
		 
	 }
	 private void generateFeed(AResult result){
		 
		 
		 System.out.println("####CHANGEMENT: "+result.getDatasetTitle()+" "+ result.getExplaination());
		 
		 File newFile=null;
		 DateFormat sdf = EndpointStatusUtil.DATE_FORMAT_RSS;
		 String now = sdf.format(Calendar.getInstance().getTime());
		 StringBuffer sb = new StringBuffer();
			try {
				newFile = new File(OUTPUT_RSS_PAGE_PATH+URLEncoder.encode(result.getPackageId(),"UTF-8")+".xml");
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			if(!newFile.exists()){
				sb.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n");
				 sb.append("<rss version=\"2.0\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:content=\"http://purl.org/rss/1.0/modules/content/\">\n");
				 sb.append("\t<channel>\n");
				 sb.append("\t\t<title>"+result.getDatasetTitle()+" SPARQL Endpoint Status</title>\n");
				 sb.append("\t\t<link>"+HOMEPAGE+"</link>\n");
				 sb.append("\t\t<description>Test Availability of "+result.getEndpointURI()+" SPARQL Endpoint</description>\n");
				 sb.append("\t\t<dc:creator>Pierre-Yves Vandenbussche</dc:creator>\n");
				 sb.append("\t\t<language>en</language>\n");
				 sb.append("\t\t<webMaster>pierre-yves.vandenbussche@mondeca.com</webMaster>\n");
				 sb.append("\t\t<lastBuildDate>"+now+"</lastBuildDate>\n");
				 sb.append("\t\t<item>\n");
			}
			else sb.append("<item>\n");
		try {
			 sb.append("\t\t\t<title>"+EndpointStatusUtil.encode(result.getDatasetTitle())+" "+result.getExplaination()+"</title>\n");
			 sb.append("\t\t\t<guid>"+HOMEPAGE+"details/"+URLEncoder.encode(result.getPackageId(),"UTF-8")+" "+now+"</guid>\n");
			 sb.append("\t\t\t<description>"+sdf.format(Calendar.getInstance().getTime())+" "+EndpointStatusUtil.encode(result.getDatasetTitle())+" "+result.getExplaination()+". Endpoint URL: "+result.getEndpointURI()+"</description>\n");
			 sb.append("\t\t\t<link>"+HOMEPAGE+"details/"+URLEncoder.encode(result.getPackageId(),"UTF-8")+".html"+"</link>\n");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		 sb.append("\t\t\t<dc:creator>Pierre-Yves Vandenbussche</dc:creator>\n");
		 sb.append("\t\t\t<pubDate>"+now+"</pubDate>\n");
		 sb.append("\t\t</item>\n");
		 
		 if(!newFile.exists()){
			 sb.append("\t</channel>\n");
			 sb.append("</rss>");
		 }
		 
		 rssFeedGlobal.append("&lt;li&gt;"+EndpointStatusUtil.encode(result.getDatasetTitle())+" "+result.getExplaination()+"&lt;/li&gt;");
		 
		 if(newFile.exists()){
				//lecture du fichier texte	
					String fileString="";
					try{
						InputStream ips=new FileInputStream(newFile); 
						InputStreamReader ipsr=new InputStreamReader(ips);
						BufferedReader br=new BufferedReader(ipsr);
						
						String ligne;
						while ((ligne=br.readLine())!=null){
							fileString+=ligne+"\n";
						}
						br.close(); 
					}		
					catch (Exception e){
						System.out.println("generateFeed: "+e.getMessage());
					}
					
					//remplacer le last build date
					fileString=fileString.replaceAll("<lastBuildDate>.*</lastBuildDate>", "<lastBuildDate>"+now+"</lastBuildDate>");
					
					//si existe plus de 24 items alors virer le dernier
					int index = 0;
					int start = 0;
					int end=0;
					int nbMatch=0;
					while(index <fileString.length()){
						start = fileString.indexOf("<item>",index);
						if(start>0){
							end= fileString.indexOf("</item>",index);
							if(end>0){
								nbMatch++;
								if(nbMatch>24){
									fileString=fileString.substring(0, start-3)+fileString.substring((end+7),(fileString.length()));
								}
								else{
									index=(end+7);
								}
							}
							else index=fileString.length();
						}
						else index=fileString.length();
					}
			        
			        //ajouter le nouvel item en tete
			        fileString=fileString.replaceFirst("<item>",sb.toString() +"\t\t<item>");
			        /* ECRITURE DU FICHIER XML RSS FEEDS*/
					
					newFile.delete();
					try {
						newFile.createNewFile();
						BufferedWriter out = new BufferedWriter(new FileWriter(newFile));
			            StringWriter writer = new StringWriter();
			            writer.flush();
						out.write(fileString);
						out.close();
					} catch (Exception e) {
						System.out.println("Problem when writing new page "+e.getMessage());
					}
				 }
				 else{
					 try {
							newFile.createNewFile();
							BufferedWriter out = new BufferedWriter(new FileWriter(newFile));
				            StringWriter writer = new StringWriter();
				            writer.flush();
							out.write(sb.toString());
							out.close();
						} catch (Exception e) {
							System.out.println("Problem when writing new page "+e.getMessage());
						}
				 }
	 }
	 
	 private boolean addStatusToHTMLPage(
			 boolean isOdd,
			 AResult result){
		 boolean hasChangedSinceLastTest=false;
		 try {
			 /* GET UPTIME LAST 24h */
			 Calendar now = Calendar.getInstance();
			 now.add(Calendar.DATE, -1);
		 	 DateFormat sdf = new SimpleDateFormat(EndpointStatusUtil.DATE_FORMAT);  
			 String OneDayBefore= sdf.format(now.getTime());  
			 			 
		 	String query="SELECT DISTINCT ?statusDate ?availability ?responseTime " +
		 			"WHERE{ " +
		 				"<"+result.getDatasetURI()+"> <"+EndpointStatusUtil.EX_STATUS+"> ?status. " +
		 				"?status <"+EndpointStatusUtil.EX_STATUS_DATE+"> ?statusDate. " +
		 				"?status <"+EndpointStatusUtil.EX_STATUS_IS_AVAILABLE+"> ?availability. " +
		 				"OPTIONAL{ ?status <"+EndpointStatusUtil.EX_STATUS_RESPONSETIME+"> ?responseTime.} " +
		 				"FILTER ( ?statusDate > \""+OneDayBefore+"\"^^xsd:dateTime ) " +
		 			"} ORDER BY ?statusDate";
		 	ResultSet resultSet=null;
			try {
				resultSet = EndpointStatusUtil.executeQuery(query,TRIPLE_STORE_URL);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			List<String[]> responseTime24h = new ArrayList<String[]>();
			int nbActive=0;
			int nbTotal=0;
			Boolean avantDernierAvailability=null;
			Boolean dernierAvailability=null;
			for ( ; resultSet.hasNext() ; )
			{
				nbTotal++;
				QuerySolution soln = resultSet.nextSolution() ;
//			for(Iterator<BindingSetAjax> it=tupleQueryResultAjax.getBindingSets().iterator(); it.hasNext();){
//				BindingSetAjax b =it.next();
				String avail = ((com.hp.hpl.jena.rdf.model.Literal)soln.get("availability")).getLexicalForm();
				if(dernierAvailability!=null)avantDernierAvailability=new Boolean(dernierAvailability.booleanValue());
				dernierAvailability=new Boolean(avail.equalsIgnoreCase("true"));
				if(avail.equalsIgnoreCase("true")){
					nbActive++;
					String responseTime="100";
					if(soln.get("responseTime")!=null){
						responseTime=((com.hp.hpl.jena.rdf.model.Literal)soln.get("responseTime")).getLexicalForm();
					}
					responseTime24h.add(new String[]{((com.hp.hpl.jena.rdf.model.Literal)soln.get("statusDate")).getLexicalForm(),responseTime});
				}
				else{
					responseTime24h.add(new String[]{((com.hp.hpl.jena.rdf.model.Literal)soln.get("statusDate")).getLexicalForm(),"0"});
				}
			}
			//detect changement
			if(nbTotal>2){
				if(!avantDernierAvailability.equals(dernierAvailability)){
					hasChangedSinceLastTest=true;
				}
				else hasChangedSinceLastTest=false;
			}
			else if(nbTotal==1)hasChangedSinceLastTest=true;
			else hasChangedSinceLastTest=false;
			DecimalFormat df = new DecimalFormat () ; 
			df.setMaximumFractionDigits ( 2 ) ; //arrondi à 2 chiffres apres la virgules 
			Double uptimeOneDay=new Double(0);
			if(nbTotal>0){
//				System.out.println("tupleQueryResultAjax.getBindingSets().size():" +tupleQueryResultAjax.getBindingSets().size());
//				System.out.println("nbActive:" +nbActive);
//				System.out.println(""+Double.parseDouble(""+nbActive)/Double.parseDouble(""+tupleQueryResultAjax.getBindingSets().size())*100);
				uptimeOneDay = Double.parseDouble(""+nbActive)/Double.parseDouble(""+nbTotal)*100;
			}
			
			/* GET UPTIME LAST 7days */
			 Calendar now2 = Calendar.getInstance();
			 now2.add(Calendar.DATE, -7);
			 String SevenDayBefore= sdf.format(now2.getTime());
			 query="SELECT DISTINCT ?statusDate ?availability " +
	 			"WHERE{ " +
	 				"<"+result.getDatasetURI()+"> <"+EndpointStatusUtil.EX_STATUS+"> ?status. " +
	 				"?status <"+EndpointStatusUtil.EX_STATUS_DATE+"> ?statusDate. " +
	 				"?status <"+EndpointStatusUtil.EX_STATUS_IS_AVAILABLE+"> ?availability. " +
	 				"FILTER ( ?statusDate > \""+SevenDayBefore+"\"^^xsd:dateTime ) " +
	 			"} ORDER BY ?statusDate";
			 ResultSet resultSet2=null;
			 try {
				 
				 resultSet2 = EndpointStatusUtil.executeQuery(query,TRIPLE_STORE_URL);
			} catch (Exception e) {
				e.printStackTrace();
			}
				nbActive=0;
				
				List<String[]> availability7d = new ArrayList<String[]>();
				
				List<String[]> tempResultList = new ArrayList<String[]>();
//				System.out.println(resultSet2.hasNext());
				//read once resultset
				for ( ; resultSet2.hasNext() ; ){
					QuerySolution soln = resultSet2.nextSolution() ;
					tempResultList.add(new String[]{((com.hp.hpl.jena.rdf.model.Literal)soln.get("statusDate")).getLexicalForm(), ((com.hp.hpl.jena.rdf.model.Literal)soln.get("availability")).getLexicalForm()});
				}
				
				nbTotal=0;
				//populate tab
				for(int i=7; i>0;i--){
					 Calendar ptMax = Calendar.getInstance();
					 ptMax.add(Calendar.DATE, -i);
					 Calendar ptMin = Calendar.getInstance();
					 ptMin.add(Calendar.DATE, -(i-1));
					 int cptTotalPerDay=0;
					 int cptTruePerDay=0;
					 for (int k=0 ; k<tempResultList.size() ; k++)
						{
							try {
								Calendar statusDate = Calendar.getInstance();
								statusDate.setTime(sdf.parse(tempResultList.get(k)[0]));
								if(statusDate.compareTo(ptMax)>0 && statusDate.compareTo(ptMin)<=0){
									cptTotalPerDay++;
									nbTotal++;
									if(tempResultList.get(k)[1].equalsIgnoreCase("true")){
										cptTruePerDay++;
										nbActive++;
									}
								}
							} catch (ParseException e) {
								e.printStackTrace();
							}
					}
					 if(cptTotalPerDay>0){
						 Double uptimeDay = Double.parseDouble(""+cptTruePerDay)/Double.parseDouble(""+cptTotalPerDay)*100;
						 availability7d.add(new String[]{"Day -"+i,df.format(uptimeDay.doubleValue()).replaceAll(",", ".")});
					 }
					 else availability7d.add(new String[]{"Day -"+i,"0"});
				}
				
//				for(Iterator<BindingSetAjax> it=tupleQueryResultAjax.getBindingSets().iterator(); it.hasNext();){
//					BindingSetAjax b =it.next();
//					if(b.getBinding("availability").getValue().equalsIgnoreCase("true"))nbActive++;
//				}
				Double uptimeSevenDay=new Double(0);
				if(nbTotal>0){
					uptimeSevenDay = Double.parseDouble(""+nbActive)/Double.parseDouble(""+nbTotal)*100;
				}
				createDetailsHTMLPage(result,uptimeOneDay,uptimeSevenDay,responseTime24h,availability7d);
				
//			System.out.println("OneDayBefore: "+OneDayBefore);
//			System.out.println("uptimeOneDay: "+uptimeOneDay);
//			System.out.println("SevenDayBefore: "+SevenDayBefore);
//			System.out.println("uptimeSevenDay: "+uptimeSevenDay);
			
				
				
				XObject tableNodes = XPathAPI.eval(doc, "//table[@id=\"table_current\"]");
				if(tableNodes.nodelist().getLength()>0){
					Node tableNode = tableNodes.nodelist().item(0);
					Element tr = doc.createElement("tr");
					if(isOdd)tr.setAttribute("class", "row_odd");
						Element cellStatus = doc.createElement("td");
							Element image = doc.createElement("img");
							if(result.isAvailable()){
								if(uptimeOneDay<100)image.setAttribute("src", "./images/orange.png" );
								else image.setAttribute("src", "./images/green.png" );
							}
							else{
								if(uptimeSevenDay<1) image.setAttribute("src", "./images/gray.png" );
								else image.setAttribute("src", "./images/red.png" );
							}
							cellStatus.appendChild(image);
						tr.appendChild(cellStatus);
							Element cellTitle = doc.createElement("td");
								Element endpointdetailsLink = doc.createElement("a");
									endpointdetailsLink.setAttribute("href", "./details/"+URLEncoder.encode(result.getPackageId(),"UTF-8")+".html");
									endpointdetailsLink.appendChild(doc.createTextNode(result.getDatasetTitle()));
							cellTitle.appendChild(endpointdetailsLink);
						tr.appendChild(cellTitle);						
						Element cellUptimeOneDay = doc.createElement("td");
							cellUptimeOneDay.appendChild(doc.createTextNode(df.format(uptimeOneDay.doubleValue())+"%"));
						tr.appendChild(cellUptimeOneDay);
						Element cellUptimeSevenDay = doc.createElement("td");
						cellUptimeSevenDay.appendChild(doc.createTextNode(df.format(uptimeSevenDay.doubleValue())+"%"));
						tr.appendChild(cellUptimeSevenDay);
						
						Element cellEndpoint = doc.createElement("td");
						
							Element rssLink = doc.createElement("a");
								rssLink.setAttribute("href", "./feeds/"+URLEncoder.encode(result.getPackageId(),"UTF-8")+".xml");
								rssLink.setAttribute("target", "_blank");
									Element rssImg = doc.createElement("img");
										rssImg.setAttribute("src", "./images/rss.gif" );
										rssImg.setAttribute("title", "Subscribe to this endpoint status changements" );
										rssImg.setAttribute("class", "rssIMG" );
										rssImg.setAttribute("height", "18" );
										rssImg.setAttribute("width", "18" );
								rssLink.appendChild(rssImg);
						cellEndpoint.appendChild(rssLink);
							Element endpointLink = doc.createElement("a");
								endpointLink.setAttribute("href", result.getEndpointURI());
								endpointLink.setAttribute("target", "_blank");
									Element endpointLinkImg = doc.createElement("img");
										endpointLinkImg.setAttribute("src", "./images/endpoint.png" );
										endpointLinkImg.setAttribute("title", "Link to this endpoint URL" );
										endpointLinkImg.setAttribute("class", "endpointIMG" );
								endpointLink.appendChild(endpointLinkImg);
						cellEndpoint.appendChild(endpointLink);
							
							Element ckanLink = doc.createElement("a");
								ckanLink.setAttribute("href", CKAN_PACKAGE_PATH+result.getPackageId());
								ckanLink.setAttribute("target", "_blank");
									Element ckanLinkImg = doc.createElement("img");
										ckanLinkImg.setAttribute("src", "./images/ckan.png" );
										ckanLinkImg.setAttribute("title", "Link to CKAN endpoint information page" );
										ckanLinkImg.setAttribute("class", "endpointIMG" );
								ckanLink.appendChild(ckanLinkImg);
						cellEndpoint.appendChild(ckanLink);
						tr.appendChild(cellEndpoint);
					tableNode.appendChild(tr);
					
				}
			} catch (DOMException e) {
				System.out.println("DOMException: "+e.getMessage());
			} catch (TransformerException e) {
				System.out.println("TransformerException: "+e.getMessage());
			} catch (Exception e) {
				System.out.println("Exception: "+e.getMessage());
			}
			return hasChangedSinceLastTest;
	 }
	
	 private void createDetailsHTMLPage(AResult result, Double uptimeOneDay, Double uptimeSevenDay, List<String[]> responseTime24h,  List<String[]> availability7d){
	 Document docDetails=null;
	 /* RECUPERATION DU TEMPLATE HTML */
		URL url;
		BufferedInputStream in=null;
		Tidy tidy = new Tidy();
	 try {
			url = new URL(TEMPLATE_DETAILS_HTML_PAGE_PATH);
			in = new BufferedInputStream(url.openStream());
			tidy.setQuiet(true);
			tidy.setShowWarnings(false);
			docDetails = tidy.parseDOM(in, null);
		} catch (MalformedURLException e) {
			System.out.println("executeEnhancement()Page URL "+TEMPLATE_HTML_PAGE_PATH+" is not well formed");	
		} catch (IOException e) {
			System.out.println("executeEnhancement()Problem to access the URL "+TEMPLATE_HTML_PAGE_PATH);
		}
//		 System.out.println("Create page :"+title );
		try {
			//ajout du titre et du statut courant
			XObject tableNodes = XPathAPI.eval(docDetails, "//div[@id=\"title\"]");
			if(tableNodes.nodelist().getLength()>0){
				Node tableNode = tableNodes.nodelist().item(0);
					Element h2 = docDetails.createElement("h2");
					h2.setAttribute("style", "white-space:nowrap; font-size:18px;");
						Element image = docDetails.createElement("img");
						if(result.isAvailable()){
							if(uptimeOneDay<100)image.setAttribute("src", "../images/orange.png" );
							else image.setAttribute("src", "../images/green.png" );
						}
						else{
							if(uptimeSevenDay<1) image.setAttribute("src", "../images/gray.png" );
							else image.setAttribute("src", "../images/red.png" );
						}
					h2.appendChild(image);
					h2.appendChild(docDetails.createTextNode(result.getDatasetTitle()+" SPARQL Endpoint Details"));
				tableNode.appendChild(h2);
			}
			//ajout lien rss
			tableNodes = XPathAPI.eval(docDetails, "//div[@id=\"rss_feed\"]");
			if(tableNodes.nodelist().getLength()>0){
				Node tableNode = tableNodes.nodelist().item(0);
				Element rssLink = doc.createElement("a");
					rssLink.setAttribute("href", "../feeds/"+URLEncoder.encode(result.getPackageId(),"UTF-8")+".xml");
					rssLink.setAttribute("target", "_blank");
						Element rssImg = doc.createElement("img");
							rssImg.setAttribute("src", "../images/rss.gif" );
							rssImg.setAttribute("width", "18" );
							rssImg.setAttribute("height", "18" );
							rssImg.setAttribute("title", "Subscribe to this endpoint status changements" );
					rssLink.appendChild(rssImg);
					rssLink.appendChild(docDetails.createTextNode("Subscribe"));
				tableNode.appendChild(rssLink);
			}
			
			//ajout lien endpoint
			tableNodes = XPathAPI.eval(docDetails, "//div[@id=\"endpoint_link\"]");
			if(tableNodes.nodelist().getLength()>0){
				Node tableNode = tableNodes.nodelist().item(0);
				Element rssLink = doc.createElement("a");
					rssLink.setAttribute("href", result.getEndpointURI());
					rssLink.setAttribute("target", "_blank");
						Element rssImg = doc.createElement("img");
							rssImg.setAttribute("src", "../images/endpoint.png" );
							rssImg.setAttribute("title", "Link to this SPARQL endpoint" );
					rssLink.appendChild(rssImg);
				tableNode.appendChild(rssLink);
			}
			
			//ajout lien ckan
			tableNodes = XPathAPI.eval(docDetails, "//div[@id=\"ckan_link\"]");
			if(tableNodes.nodelist().getLength()>0){
				Node tableNode = tableNodes.nodelist().item(0);
				Element rssLink = doc.createElement("a");
					rssLink.setAttribute("href", CKAN_PACKAGE_PATH+result.getPackageId());
					rssLink.setAttribute("target", "_blank");
						Element rssImg = doc.createElement("img");
							rssImg.setAttribute("src", "../images/ckan.png" );
							rssImg.setAttribute("title", "Link to CKAN endpoint information page" );
					rssLink.appendChild(rssImg);
				tableNode.appendChild(rssLink);
			}
			
			//ajout des données pour le script charts
			tableNodes = XPathAPI.eval(docDetails, "//body//script");
			if(tableNodes.nodelist().getLength()>0){
				Node tableNode = tableNodes.nodelist().item(0);
				
				StringBuffer script= new StringBuffer();
				script.append("YUI().use('charts', function (Y){var myDataValues = [ ");
				for(int i=0; i<responseTime24h.size(); i++){
					script.append("{date:\""+responseTime24h.get(i)[0]+"\",responseTime:"+responseTime24h.get(i)[1]+"},");
				}				
				script.append("]; ");
				script.append("var myAxes = {response:{ keys:[\"responseTime\"], position:\"left\", ");
				script.append("	type:\"numeric\", labelFormat: {suffix:\"ms\", thousandsSeparator:\" \"}, ");
				script.append("	styles:{ majorTicks:{ display: \"none\" } } }, dateRange:{ keys:[\"date\"], ");
				script.append("	position:\"bottom\", type:\"category\", styles:{ majorTicks:{ display: ");
				script.append("	\"none\" }, label: { rotation:-45, margin:{top:5} } } } }; ");
				script.append("var seriesCollection = [ { type:\"combo\", xAxis:\"dateRange\", ");
				script.append("yAxis:\"response\", xKey:\"date\", yKey:\"responseTime\", xDisplayName:\"Date\", ");
				script.append("yDisplayName:\"Response Time\", line: { color: \"#ff7200\" ");
				script.append("}, marker: { fill: { color: \"#ff9f3b\" }, border: { ");
				script.append("color: \"#ff7200\", weight: 1 }, over: { width: 16, height: 16 ");
				script.append("}, width:9, height:9 } } ]; ");
				script.append("var myChart = new Y.Chart({ dataProvider:myDataValues, ");
				script.append("axes:myAxes, seriesCollection:seriesCollection, ");
				script.append("horizontalGridlines: true, verticalGridlines: true, ");
				script.append("render:\"#chart24h\" }); var myDataValues7d = [");
				for(int i=0; i<availability7d.size(); i++){
					script.append("{date:\""+availability7d.get(i)[0]+"\",availability:"+availability7d.get(i)[1]+"},");
				}
				script.append("]; ");
				script.append("var myAxes7d = { response:{ keys:[\"availability\"],  maximum:100,position:\"left\", ");
				script.append("type:\"numeric\", labelFormat: {suffix:\"%\"}, styles:{ majorTicks:{ ");
				script.append("display: \"none\" } } }, dateRange:{ keys:[\"date\"], position:\"bottom\", ");
				script.append("type:\"category\", styles:{ majorTicks:{ display: \"none\" ");
				script.append("}, label: { rotation:-45, margin:{top:5} } } } }; var seriesCollection7d ");
				script.append("= [ { type:\"combo\", xAxis:\"dateRange\", yAxis:\"response\", xKey:\"date\", ");
				script.append("yKey:\"availability\", xDisplayName:\"Date\", yDisplayName:\"Endpoint ");
				script.append("Availability\", line: { color: \"#ff7200\" }, marker: { fill: { ");
				script.append("color: \"#ff9f3b\" }, border: { color: \"#ff7200\", weight: 1 ");
				script.append("}, over: { width: 16, height: 16 }, width:9, height:9 } } ]; var ");
				script.append("myChart7D = new Y.Chart({ dataProvider:myDataValues7d, ");
				script.append("axes:myAxes7d, seriesCollection:seriesCollection7d, ");
				script.append("horizontalGridlines: true, verticalGridlines: true, ");
				script.append("render:\"#chart7d\" }); }); ");
				tableNode.appendChild(docDetails.createTextNode(script.toString()));
			}
		 } catch (Exception e1) {
			e1.printStackTrace();
		}
				
		 /* ECRITURE DU FICHIER HTML PAGE DETAILS*/
		 try {
		     //encodage	 
			File newFile = new File(OUTPUT_DETAILS_HTML_PAGE_PATH+URLEncoder.encode(result.getPackageId(),"UTF-8")+".html");
			if(newFile.exists())newFile.delete();
			try {
				newFile.createNewFile();
				BufferedWriter out = new BufferedWriter(new FileWriter(newFile));
				DOMSource domSource = new DOMSource(docDetails);
	            StringWriter writer = new StringWriter();
	            StreamResult res = new StreamResult(writer);
	            TransformerFactory tf = TransformerFactory.newInstance();
	            Transformer transformer = tf.newTransformer();
	            transformer.transform(domSource, res);
	            writer.flush();
				out.write(writer.toString());
				out.close();
			} catch (Exception e) {
				System.out.println("executeEnhancement()Problem when writing new page "+e.getMessage());
			}
		 } catch (java.io.UnsupportedEncodingException e) {
			    e.printStackTrace();
		}
	 }
			
	 /*
	  * Methode de mise à jour du status et des infos complémentaire (suppression existants et création) en base triplestore
	  */
	 private void  addStatus(AResult result)throws RepositoryException{
		 RepositoryConnection con = repository.getConnection();
		 
		 String ajout = "";
		 /* TYPE */
		 addStatementObjectURI(result.getDatasetURI(), EndpointStatusUtil.RDF_TYPE,EndpointStatusUtil.VOID_DATASET, con);
		 
		 /* ENDPOINT */
		 RepositoryResult<Statement> temp = con.getStatements(createURI(result.getDatasetURI(), con), createURI(EndpointStatusUtil.VOID_SPARQL_ENDPOINT, con), null, false, (Resource)null);
		 while(temp.hasNext()){con.remove(temp.next(), (Resource)null);}
		 addStatementObjectLiteral(result.getDatasetURI(), EndpointStatusUtil.VOID_SPARQL_ENDPOINT,result.getEndpointURI()+ajout, con);
		 
		 /* TITLE */
		 RepositoryResult<Statement> temp2 = con.getStatements(createURI(result.getDatasetURI(), con), createURI(EndpointStatusUtil.DC_TITLE, con), null, false, (Resource)null);
		 while(temp2.hasNext()){con.remove(temp2.next(), (Resource)null);}
		 addStatementObjectLiteral(result.getDatasetURI(), EndpointStatusUtil.DC_TITLE,result.getDatasetTitle()+ajout, con);
		 
		 /* IDENTIFIER */
		 RepositoryResult<Statement> temp3 = con.getStatements(createURI(result.getDatasetURI(), con), createURI(EndpointStatusUtil.DC_IDENTIFIER, con), null, false, (Resource)null);
		 while(temp3.hasNext()){con.remove(temp3.next(), (Resource)null);}
		 if(result.getPackageId()!=null && result.getPackageId().length()>0)addStatementObjectLiteral(result.getDatasetURI(), EndpointStatusUtil.DC_IDENTIFIER,result.getPackageId()+ajout, con);
		 
//		 /* DESCIRPTION */
//		 RepositoryResult<Statement> temp4 = con.getStatements(createURI(dataset, con), createURI(EndpointStatusUtil.DC_DESCRIPTION, con), null, false, (Resource)null);
//		 while(temp4.hasNext()){con.remove(temp4.next(), (Resource)null);}
//		 if(description!=null && description.length()>0)addStatementObjectLiteral(dataset, EndpointStatusUtil.DC_DESCRIPTION,description+ajout, con);
		 
		 
//		 /* RATING */
//		 RepositoryResult<Statement> temp5 = con.getStatements(createURI(dataset, con), createURI(EndpointStatusUtil.REVIEW_RATING, con), null, false, (Resource)null);
//		 while(temp5.hasNext()){con.remove(temp5.next(), (Resource)null);}
//		 if(rating!=null && rating.length()>0)addStatementObjectLiteral(dataset, EndpointStatusUtil.REVIEW_RATING,rating+ajout, con);
		 
		 BNode b =createBnode(con);
		 addStatement(result.getDatasetURI(),EndpointStatusUtil.EX_STATUS,b, con);
		 Date actuelle = new Date();
	 	 DateFormat sdf = new SimpleDateFormat(EndpointStatusUtil.DATE_FORMAT);  
		 String currentDate= sdf.format(actuelle);  
		 addStatement(b, EndpointStatusUtil.RDF_TYPE,createURI(EndpointStatusUtil.EX_ENDPOINT_STATUS,con), con);
		 addStatement(b,EndpointStatusUtil.EX_STATUS_DATE,currentDate,createURI(EndpointStatusUtil.XSD_DATETIME, con), con);
		 addStatement(b,EndpointStatusUtil.EX_STATUS_IS_AVAILABLE,""+result.isAvailable(),createURI(EndpointStatusUtil.XSD_BOOLEAN, con), con);
		 if(result.getExplaination()!=null)addStatement(b,EndpointStatusUtil.EX_STATUS_EXPLANATION,result.getExplaination(), con);
		 if(result.getResponseTime()!=null)addStatement(b,EndpointStatusUtil.EX_STATUS_RESPONSETIME,result.getResponseTime(),createURI(EndpointStatusUtil.XSD_INTEGER, con), con);
		 
		 con.close();
	 
	 }
	 
	 
		
	 	/* AIDE A LA CREATION DE TRIPLET DANS TRIPLESTORE */
	 	private static URI createURI(String uri, RepositoryConnection con){
			ValueFactory vf = con.getValueFactory();
			return vf.createURI(uri);
		}
		private static Literal createLiteral(String value, RepositoryConnection con){
			ValueFactory vf = con.getValueFactory();
			return vf.createLiteral(value);
		}
		private static Literal createLiteral(String value,URI datatype, RepositoryConnection con){
			ValueFactory vf = con.getValueFactory();
			return vf.createLiteral(value,datatype);
		}
		private static BNode createBnode(RepositoryConnection con){
			ValueFactory vf = con.getValueFactory();
			return vf.createBNode();
		}
		private static void addStatementObjectURI(String s, String p, String o, RepositoryConnection con) throws RepositoryException{
			ValueFactory myFactory = con.getValueFactory();
			Statement st = myFactory.createStatement((Resource) createURI(s, con), createURI(p, con), createURI(o, con));
			con.add(st, new Resource[0]);
		}
		private static void addStatementObjectLiteral(String s, String p, String o, RepositoryConnection con) throws RepositoryException{
			ValueFactory myFactory = con.getValueFactory();
			Statement st = myFactory.createStatement((Resource) createURI(s, con), createURI(p, con), createLiteral(o, con));
			con.add(st, new Resource[0]);
		}
		private static void addStatement(BNode s, String p, String o,URI datatype, RepositoryConnection con) throws RepositoryException{
			ValueFactory myFactory = con.getValueFactory();
			Statement st = myFactory.createStatement((Resource) s, createURI(p, con), createLiteral(o,datatype, con));
			con.add(st, new Resource[0]);
		}
		private static void addStatement(BNode s, String p, String o, RepositoryConnection con) throws RepositoryException{
			ValueFactory myFactory = con.getValueFactory();
			Statement st = myFactory.createStatement((Resource) s, createURI(p, con), createLiteral(o, con));
			con.add(st, new Resource[0]);
		}
		private static void addStatement(BNode s, String p, URI o, RepositoryConnection con) throws RepositoryException{
			ValueFactory myFactory = con.getValueFactory();
			Statement st = myFactory.createStatement((Resource) s, createURI(p, con), o);
			con.add(st, new Resource[0]);
		}
		private static void addStatement(String s, String p, BNode o, RepositoryConnection con) throws RepositoryException{
			ValueFactory myFactory = con.getValueFactory();
			Statement st = myFactory.createStatement((Resource) createURI(s, con), createURI(p, con), o);
			con.add(st, new Resource[0]);
		}
		
}

