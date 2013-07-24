package utils;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;

import core.Endpoint;




public class QueryManager {

	
	
	
	public static QueryExecution getExecution(Endpoint ep, String query) throws Exception {
		try {
			return QueryExecutionFactory.sparqlService(ep.getUri().toString(), query);
		}
        catch (Exception e) {  
        	throw new Exception(e.getMessage());
        }
    }
}
