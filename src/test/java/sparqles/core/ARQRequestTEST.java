package sparqles.core;

import static org.junit.Assert.*;

import org.apache.jena.riot.web.HttpOp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;

public class ARQRequestTEST {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		
		
		QueryExecution ex = QueryExecutionFactory.sparqlService("http://localhost:8000/sparql", "SELECT * WHERE { ?s ?p ?o . }");
		HttpOp.setUserAgent(CONSTANTS.USER_AGENT);
		ResultSet res = ex.execSelect();
		while (res.hasNext()){
			System.out.println("nextS");
		}
	
	}

}
