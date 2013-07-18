package sparqldiscovery;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.deri.any23.Any23;
import org.deri.any23.extractor.*;
import org.deri.any23.extractor.html.TurtleHTMLExtractor;
import org.deri.any23.extractor.rdf.NQuadsExtractor;
import org.deri.any23.extractor.rdf.NTriplesExtractor;
import org.deri.any23.extractor.rdf.RDFXMLExtractor;
import org.deri.any23.extractor.rdf.TurtleExtractor;
import org.deri.any23.extractor.rdfa.RDFaExtractor;
import org.deri.any23.filter.*;
import org.deri.any23.vocab.XHTML;
import org.deri.any23.writer.TripleHandler;
import org.deri.any23.writer.TripleHandlerException;
import org.deri.any23.http.HTTPClient;
import org.deri.any23.source.DocumentSource;
import org.deri.any23.source.HTTPDocumentSource;
import org.deri.any23.writer.NQuadsWriter;
import org.deri.any23.writer.TripleHandler;
import org.deri.any23.writer.TripleHandlerException;
import org.semanticweb.yars2.rdfxml.RDFXMLParser;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.ontologycentral.ldspider.http.ConnectionManager;

public class GetSelfVoid  extends Benchmark{

	private final static String benchExperiment="sparqlselfvoid";
	
	public GetSelfVoid(File logDir) {
		super(logDir,benchExperiment);
	}
	
	private final static String voidStore="http://void.rkbexplorer.com/sparql/";
	private final static String query = "" +
			"PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
			"PREFIX rdfs:     <http://www.w3.org/2000/01/rdf-schema#>\n"+
			"PREFIX owl:      <http://www.w3.org/2002/07/owl#>\n"+
			"PREFIX xsd:      <http://www.w3.org/2001/XMLSchema#>\n"+
			"PREFIX foaf:     <http://xmlns.com/foaf/0.1/>\n"+
			"PREFIX dcterms:  <http://purl.org/dc/terms/>\n"+
			"PREFIX scovo:    <http://purl.org/NET/scovo#>\n"+
			"PREFIX void:     <http://rdfs.org/ns/void#>\n"+
			"PREFIX akt:      <http://www.aktors.org/ontology/portal#>\n"+
			"SELECT DISTINCT * \n"+
			"WHERE {\n"+ 
			"?ds a void:Dataset .\n"+ 
			"?ds void:sparqlEndpoint %%s .\n"+
			"?ds ?p ?o .\n"+
			"}";
	
	boolean benchmark(String endpointURI)throws Exception{
		Thread.sleep(5000);
		String queryString = query.replaceAll("%%s", "<"+endpointURI+">");
		Query query = QueryFactory.create(queryString);
		System.out.println(query);
		// initializing queryExecution factory with remote service.
		// **this actually was the main problem I couldn't figure out.**
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointURI, query);

		boolean results = false;
		//after it goes standard query execution and result processing which can
		// be found in almost any Jena/SPARQL tutorial.
		try {
		    ResultSet res = qexec.execSelect();
		    ResultSetRewindable reswind = ResultSetFactory.makeRewindable(res);
		    
		    FileWriter fw = new FileWriter(new File("results",benchExperiment+".sparql.tsv"),true);
		    while(reswind.hasNext()){
		    	RDFNode dataset = reswind.next().get("ds");
		    	fw.write(endpointURI+"\t"+dataset.toString()+"\n");
		    	
		    }
		    fw.close();
		    		    
		    reswind.reset();
		    FileOutputStream fos = new FileOutputStream(new File(getLogDir(), URLEncoder.encode(endpointURI, "UTF-8")+"-rdfxml.nq"));
		    ResultSetFormatter.outputAsXML(fos, reswind);
		    fos.close();
		    
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
		   qexec.close();
		}
		return results;
	}
			
}
