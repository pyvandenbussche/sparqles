package core.discovery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.QueryManager;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.RDFNode;

import core.Endpoint;

public class DGetSelfVoidRun  extends DRun<VoidResult>{
	private static final Logger log = LoggerFactory.getLogger(DGetSelfVoidRun.class);

	public DGetSelfVoidRun(Endpoint ep) {
		super(ep);

	}

	private final static String voidStore="http://void.rkbexplorer.com/sparql/";
	private final static String query = "" +
			"PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
//			"PREFIX rdfs:     <http://www.w3.org/2000/01/rdf-schema#>\n"+
//			"PREFIX owl:      <http://www.w3.org/2002/07/owl#>\n"+
//			"PREFIX xsd:      <http://www.w3.org/2001/XMLSchema#>\n"+
//			"PREFIX foaf:     <http://xmlns.com/foaf/0.1/>\n"+
//			"PREFIX dcterms:  <http://purl.org/dc/terms/>\n"+
//			"PREFIX scovo:    <http://purl.org/NET/scovo#>\n"+
			"PREFIX void:     <http://rdfs.org/ns/void#>\n"+
//			"PREFIX akt:      <http://www.aktors.org/ontology/portal#>\n"+
			"SELECT DISTINCT * \n"+
			"WHERE {\n"+ 
			"?ds a void:Dataset .\n"+ 
			"?ds void:sparqlEndpoint %%s .\n"+
			"?ds ?p ?o .\n"+
			"}";

	public VoidResult execute() {

		VoidResult res = new VoidResult();
		String queryString = query.replaceAll("%%s", "<"+_ep.getUri()+">");
		
		// initializing queryExecution factory with remote service.
		// **this actually was the main problem I couldn't figure out.**
		QueryExecution qexec = null;
		try {
			qexec = QueryManager.getExecution(_ep, queryString);


			boolean results = false;
			//after it goes standard query execution and result processing which can
			// be found in almost any Jena/SPARQL tutorial.

			ResultSet resSet = qexec.execSelect();
			ResultSetRewindable reswind = ResultSetFactory.makeRewindable(resSet);

			
			HashSet<CharSequence> voids = new HashSet<CharSequence>();
			
			//		    FileWriter fw = new FileWriter(new File("results",benchExperiment+".sparql.tsv"),true);
			while(reswind.hasNext()){
				RDFNode dataset = reswind.next().get("ds");
				voids.add(dataset.toString());
				//		    	fw.write(endpointURI+"\t"+dataset.toString()+"\n");

			}
			log.info("Found {} results",reswind.getRowNumber());
			//		    fw.close();
			ArrayList<CharSequence> voidA = new ArrayList<CharSequence>(voids);
			res.setVoidFile(voidA);
			//		    		    
			//		    reswind.reset();
			//		    FileOutputStream fos = new FileOutputStream(new File(getLogDir(), URLEncoder.encode(endpointURI, "UTF-8")+"-rdfxml.nq"));
			//		    ResultSetFormatter.outputAsXML(fos, reswind);
			//		    fos.close();

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		finally {
			if(qexec!=null)qexec.close();
		}
		return res;
	}
}
