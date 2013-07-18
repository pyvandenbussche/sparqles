package com.pyv.endpointStatus;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.ResultSet;


public class  EndpointStatusUtil {
	
	/* CONFIGURATION */
//	public static String CKAN_SPARQL_ENDPOINT = "http://semantic.ckan.net/sparql";
//	public static String TRIPLE_STORE_URL = "http://localhost:8280/openrdf-sesame/repositories/testSansInf";
//	public static String TEMPLATE_HTML_PAGE_PATH = "file:C:/Users/Pierre-Yves/Desktop/templates/template.html";
//	public static String TEMPLATE_DETAILS_HTML_PAGE_PATH = "file:C:/Users/Pierre-Yves/Desktop/templates/template_details.html";
//	public static String OUTPUT_HTML_PAGE_PATH = "C:/Users/Pierre-Yves/Desktop/";
//	public static String OUTPUT_RSS_PAGE_PATH = "C:/Users/Pierre-Yves/Desktop/feeds/";
//	public static String OUTPUT_RSS_OVERALL_FEED_NAME = "overall";
//	public static String OUTPUT_DETAILS_HTML_PAGE_PATH = "C:/Users/Pierre-Yves/Desktop/details/";
//	public static String OUTPUT_HTML_PAGE_NAME = "index.html";
//	public static String HOMEPAGE = "http://www.aredefinir.fr/";
	
	/* NAMESPACES */
	public static String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	public static String VOID_DATASET = "http://rdfs.org/ns/void#Dataset";
	public static String DC_TITLE = "http://purl.org/dc/terms/title";
	
//	public static String DC_DESCRIPTION = "http://purl.org/dc/terms/description";
//	public static String REVIEW_RATING = "http://purl.org/stuff/rev#rating";
	public static String DC_IDENTIFIER = "http://purl.org/dc/terms/identifier";
	public static String VOID_SPARQL_ENDPOINT = "http://rdfs.org/ns/void#sparqlEndpoint";
	
	
//	public static String EX_STATUS = "http://mondeca/ex#status";
//	public static String EX_STATUS_DATE = "http://mondeca/ex#statusDate";
//	public static String EX_STATUS_IS_AVAILABLE = "http://mondeca/ex#statusIsAvailable";
//	public static String EX_STATUS_EXPLANATION = "http://mondeca/ex#statusExplanation";
//	public static String EX_STATUS_RESPONSETIME = "http://mondeca/ex#statusResponseTime";
	public static String EX_ENDPOINT_STATUS = "http://labs.mondeca.com/vocab/endpointStatus#EndpointStatus";
	public static String EX_STATUS = "http://labs.mondeca.com/vocab/endpointStatus#status";
	public static String EX_STATUS_DATE = "http://purl.org/dc/terms/date";
	public static String EX_STATUS_IS_AVAILABLE = "http://labs.mondeca.com/vocab/endpointStatus#statusIsAvailable";
	public static String EX_STATUS_EXPLANATION = "http://purl.org/dc/terms/description";
	public static String EX_STATUS_RESPONSETIME = "http://labs.mondeca.com/vocab/endpointStatus#statusResponseTime";
	
	public static String XSD_DATETIME = "http://www.w3.org/2001/XMLSchema#dateTime";
	public static String XSD_BOOLEAN = "http://www.w3.org/2001/XMLSchema#boolean";
	public static String XSD_INTEGER = "http://www.w3.org/2001/XMLSchema#integer";
	
	public static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'+01:00'";
	public static final DateFormat DATE_FORMAT_RSS = new SimpleDateFormat("EEE, d MMM yyyy HH:mm Z", Locale.US);
	
	protected static String PREFIXES = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"+
	"PREFIX xsd:<http://www.w3.org/2001/XMLSchema#> \n"+
	"PREFIX dc:<http://purl.org/dc/elements/1.1/> \n"+
	"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> \n"+
	"PREFIX owl:<http://www.w3.org/2002/07/owl#> \n"+
	"PREFIX skos:<http://www.w3.org/TR/skos-reference/#> \n"+
	"PREFIX void:<http://rdfs.org/ns/void#> \n"+
	"PREFIX ends:<http://labs.mondeca.com/vocab/endpointStatus#> \n"+
	"PREFIX dcterms:<http://purl.org/dc/terms/> \n"+
	"PREFIX foaf:<http://xmlns.com/foaf/0.1/> \n";
	
	public static HTTPRepository connectToRepository(String serverURL)throws RepositoryException{
		try {
			HTTPRepository rep = new HTTPRepository(serverURL);
			rep.initialize();
			System.out.println("connection successfull on: "+serverURL);
			return rep;
		} catch (RepositoryException e) {
			System.out.println("RdfRepositoryServiceImpl.connectToRepository()"+e.getMessage());
			throw new RepositoryException("Connection to SPARQL Endpoint : "+ serverURL+" is impossible.\n Please check your URL.");
		}
	}
	public static String encode(String s)
	 {
		 String[] ENTITIES = {">",
				 "&gt;",
				 "<",
				 "&lt;",
				 "&",
				 "&amp;",
				 "\"",
				 "&quot;",
				 "'",
				 "&#039;",
				 "\\",
				 "&#092;",
				 "\u00a9",
				 "&copy;",
				 "\u00ae",
				 "&reg;"};
		 
		 Hashtable entityTableEncode = new Hashtable(ENTITIES.length);
		 for (int i = 0; i < ENTITIES.length; i += 2)
		 {
		 if (!entityTableEncode.containsKey(ENTITIES[i]))
		 {
		     entityTableEncode.put(ENTITIES[i], ENTITIES[i + 1]);
		           }
		   	        }
		 
		 if (s == null){
			 return "";
		 }
		 StringBuffer sb = new StringBuffer(s.length() * 2);
		 char ch;
		 for (int i = 0; i < s.length(); ++i)
		 {
			 ch = s.charAt(i);
			 if ((ch >= 63 && ch <= 90) || (ch >= 97 && ch <= 122) || (ch == ' '))
			 {
				 sb.append(ch);
			 }
			 else if (ch == '\n')
			 {
				 sb.append("\n");
			 }
			 else
			 {
				 String chEnc = (String) entityTableEncode.get(String.valueOf(ch));
				 if (chEnc != null)
				 {
					 sb.append(chEnc);
				 }
				 else{
	     // 	Not 7 Bit use the unicode system
	                  	sb.append("&#");
	                  	sb.append(new Integer(ch).toString());
	                  	sb.append(';');
				 }
	   	      }
		  }
		        	return sb.toString();
	}
	
	public static boolean executeAsk2(String endpointURL) throws Exception {
		QueryExecution qe= null;
        try {
				String query= "SELECT ?s WHERE{?s ?p ?o} LIMIT 1";
					qe= QueryExecutionFactory.sparqlService(endpointURL, query);
					return qe.execSelect().hasNext();
        }
        catch (Exception e) {  
        	throw new Exception(e.getMessage());
        }
        finally {
        	if(qe!=null)qe.close();
        }
    }
	public static boolean executeAsk(String endpointURL) throws Exception {
		QueryExecution qe= null;
        try {
				String query= "ASK WHERE{?s ?p ?o}";
					qe= QueryExecutionFactory.sparqlService(endpointURL, query);
					return qe.execAsk();
        }
        catch (Exception e) {  
        	throw new Exception(e.getMessage());
        }
        finally {
        	if(qe!=null)qe.close();
        }
    }
	
	public static Repository LoadRepositoryFromFile(File file){
		try {
			Repository rdfRepository = new SailRepository(new MemoryStore());
			rdfRepository.initialize();
			
			parseRDF(file, "http://www.w3.org/1999/02/22-rdf-syntax-ns#", rdfRepository.getConnection(), null);
			System.out.println("Loading file in repository: "+file+" .............  done");
			return rdfRepository;
		} catch (Exception e) {
			System.out.println("LoadRepositoryFromFile()"+e.getMessage());
			return null;
		}
	}
	public static void parseRDF(File input,String defaultNamespace,RepositoryConnection connection,RDFFormat format)throws Exception {
		if(format != null) {
			try {
				// a format is given, parse with it.
				//System.out.println("parsing RDF input with default namespace '"+defaultNamespace+"' and format '"+format+"'...");
				connection.add(input, defaultNamespace, format);
				connection.close();
			} catch (Exception e) {
				throw new Exception(e);
			}			
		} else {
			// no format, try everything we can
			List<RDFFormat> formats = Arrays.asList(new RDFFormat[] {
					RDFFormat.RDFXML,
					RDFFormat.N3,
					RDFFormat.NTRIPLES,
					RDFFormat.TURTLE,
					RDFFormat.TRIG,
					RDFFormat.TRIX
			});
			
			for (Iterator<RDFFormat> i = formats.iterator(); i.hasNext();) {
				RDFFormat f = (RDFFormat) i.next();
				try {
					parseRDF(input, defaultNamespace, connection, f);
					// as soon as one is OK, break out of the loop.
					break;
				} catch (Exception e) {
					System.out.println("Cannot parse RDF input as '"+f.getName()+"'");
				}
			}
		}
	}
	
	public static ResultSet executeQuery(String query, String endpointURL) throws Exception {
		QueryExecution qe= null;
        try {
			qe= QueryExecutionFactory.sparqlService(endpointURL, PREFIXES+query);
			ResultSet results = qe.execSelect();
			return results;
        }
        catch (QueryParseException e) { 
        	System.out.println("Query parse exception: "+e.getMessage()); 
        	throw new Exception(e.getMessage());
        }
        catch (Exception e) { 
        	System.out.println(endpointURL+" may be DOWN :"+e.getMessage()); 
        	throw new Exception(endpointURL+" may be DOWN :"+e.getMessage());
        }
        finally {
        	if(qe!=null)qe.close();
        }
    }
}
