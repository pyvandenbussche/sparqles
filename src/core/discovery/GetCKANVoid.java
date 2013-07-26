package core.discovery;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class GetCKANVoid extends DRun<T>{

	private final static String benchExperiment = "ckanvoid";
	
	
	public GetCKANVoid(File logDir, String benchExperiment) {
		super(logDir, benchExperiment);
	}
	
	private static final String queryString = "" +
			"SELECT DISTINCT ?endpoint ?void\n"+
			"WHERE { \n"+
			"?ds <http://www.w3.org/ns/dcat#distribution> ?distribution.\n"+
			"?distribution <http://purl.org/dc/terms/format> [<http://www.w3.org/1999/02/22-rdf-syntax-ns#value> \"api/sparql\"].\n"+
			"?distribution <http://www.w3.org/ns/dcat#accessURL> ?endpoint.\n"+
			"?ds <http://purl.org/dc/terms/title> ?title.\n"+
			"?ds<http://purl.org/dc/terms/identifier> ?identifier.\n"+

			"\n"+
			"?ds <http://www.w3.org/ns/dcat#distribution> ?s.\n"+
			"?ds <http://purl.org/dc/terms/identifier> ?identifier.\n"+
			"?s <http://www.w3.org/ns/dcat#accessURL> ?void .\n"+
			"?s <http://purl.org/dc/terms/format> ?f .\n"+
			"?f <http://www.w3.org/1999/02/22-rdf-syntax-ns#value> \"meta/void\" .\n"+
			"}\n"+
			"";
	private static final String sparqlEP = "http://linkeddata.openlinksw.com/sparql";
	
	
	//we have sometimes two void description for the same endpoint
		
	
	public static void main(String[] args) {
//		GetCKANVoid ck = new GetCKANVoid(new File("results"), benchExperiment);
		Query query = QueryFactory.create(queryString);
		// initializing queryExecution factory with remote service.
		// **this actually was the main problem I couldn't figure out.**
		QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEP, query);

		//after it goes standard query execution and result processing which can
		// be found in almost any Jena/SPARQL tutorial.
		int count =0;
	    int voidcount = 0;
	    
		try {
		    ResultSet results = qexec.execSelect();
		    FileWriter fw = new FileWriter(new File("results/sparqlckan.sparql.tsv"));
		    for (; results.hasNext();) {
		    	count++;
		    	QuerySolution qs = results.next();
		    	if(qs.get("void") != null) voidcount++;
		    	fw.write(qs.get("endpoint")+"\t"+qs.get("void")+"\n");
		    }
		    fw.close();
		    
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
		   qexec.close();
		}
		System.out.println(voidcount+"/"+count);
	}
}
