package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;

import core.Endpoint;
import core.performance.PRun;




public class QueryManager {

	private static final Logger log = LoggerFactory.getLogger(QueryManager.class);
	
	
	
	public static QueryExecution getExecution(Endpoint ep, String query) throws Exception {
		try {
			log.info("Create QueryExecution for {} with query  {}",ep.getUri(),query);
			return QueryExecutionFactory.sparqlService(ep.getUri().toString(), query);
		}
        catch (Exception e) {  
        	throw new Exception(e.getMessage());
        }
    }
}
