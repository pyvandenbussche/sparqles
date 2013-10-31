package sparqles.core;

import java.io.File;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sparqles.avro.Endpoint;
import sparqles.avro.discovery.DResult;
import sparqles.avro.performance.PResult;
import sparqles.utils.MongoDBManager;

public class DiscoverabilityTEST {

	
	private MongoDBManager m;


	@Before
	public void setUp() throws Exception {
		SPARQLESProperties.init(new File("src/test/resources/ends.properties"));
		m = new MongoDBManager();
		
		
	}

	@After
	public void tearDown() throws Exception {
		m.close();
	}
	
	
	@Test
	public void test() throws Exception {
		Endpoint ep = Endpoints.DBPEDIA;
			
		test(ep);
		
		
	}
	
	
	private void test(Endpoint ep) throws Exception {
		Task<DResult> t = TaskFactory.create(CONSTANTS.DTASK, ep, m, null);
		DResult res = t.call();
		System.out.println(res);
		m.insert(res);
	}

	@Test
	public void testGroup() throws Exception {
		Endpoint [] eps = {Endpoints.DBPEDIA,Endpoints.AEMET};
		for(Endpoint ep: eps){
			test(ep);
		}
	}
}