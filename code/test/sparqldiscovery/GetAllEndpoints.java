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

public class GetAllEndpoints {

	private static final String queryString = "" +
			"SELECT DISTINCT ?dataset ?endpoint ?title ?identifier  WHERE {"+
			"?dataset <http://www.w3.org/ns/dcat#distribution> ?distribution."+
			"?distribution <http://purl.org/dc/terms/format> [<http://www.w3.org/1999/02/22-rdf-syntax-ns#value> \"api/sparql\"]."+
			"?distribution <http://www.w3.org/ns/dcat#accessURL> ?endpoint."+
			"?dataset <http://purl.org/dc/terms/title> ?title."+
			"?dataset <http://purl.org/dc/terms/identifier> ?identifier."+
			"} ORDER BY ?title";
	private static final String sparqlEP = "http://linkeddata.openlinksw.com/sparql";
	
	public static void main(String[] args) {
		
		Query query = QueryFactory.create(queryString);
		// initializing queryExecution factory with remote service.
		// **this actually was the main problem I couldn't figure out.**
		QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEP, query);

		//after it goes standard query execution and result processing which can
		// be found in almost any Jena/SPARQL tutorial.
		try {
		    ResultSet results = qexec.execSelect();
		    FileWriter fw = new FileWriter(new File("res/sparqlEp.23.20.2012.txt"));
		    for (; results.hasNext();) {
		    	QuerySolution qs = results.next();
		    	System.out.println(qs.get("endpoint"));
		    	fw.write(qs.get("identifier")+"\t"+qs.get("endpoint")+"\n");
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
