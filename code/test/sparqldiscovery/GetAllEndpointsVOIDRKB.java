package sparqldiscovery;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class GetAllEndpointsVOIDRKB {



	private static final String queryString = "" +
			"PREFIX void:     <http://rdfs.org/ns/void#>\n" +
			"SELECT DISTINCT ?endpoint WHERE { ?ds a void:Dataset . ?ds void:sparqlEndpoint ?endpoint }";
	private static final String sparqlEP = "http://void.rkbexplorer.com/sparql/";
	
	public static void main(String[] args) {
		
		Query query = QueryFactory.create(queryString);
		// initializing queryExecution factory with remote service.
		// **this actually was the main problem I couldn't figure out.**
		QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEP, query);

		//after it goes standard query execution and result processing which can
		// be found in almost any Jena/SPARQL tutorial.
		try {
		    ResultSet results = qexec.execSelect();
		    FileWriter fw = new FileWriter(new File("res/rkbx-sparqlEp.26.20.2012.txt"));
		    for (; results.hasNext();) {
		    	QuerySolution qs = results.next();
		    	System.out.println(qs.get("endpoint"));
		    	fw.write(qs.get("endpoint")+"\n");
		    }
		    fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
		   qexec.close();
		}
	}
}
