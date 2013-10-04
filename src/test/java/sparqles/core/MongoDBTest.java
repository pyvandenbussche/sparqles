package sparqles.core;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sparqles.core.availability.AResult;
import sparqles.core.availability.ATask;
import sparqles.utils.MongoDBManager;

public class MongoDBTest {

	protected MongoDBManager m;

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
	public void testConnection() {
		MongoDBManager m = new MongoDBManager();
		assertTrue(m.isRunning());
	}
	
	@Test
	public void testInsertEP() {
		Endpoint e = Endpoints.DBPEDIA;
		m.initEndpointCollection();
		assertTrue(m.insert(e));
		assertEquals(1, m.get(Endpoint.class, Endpoint.SCHEMA$).size());
		
		assertTrue(m.insert(e));
		assertEquals(1, m.get(Endpoint.class, Endpoint.SCHEMA$).size());
	}
	
	@Test
	public void testGetSince(){
		Endpoint e = Endpoints.DBPEDIA;
		List<AResult> res = m.getResultsSince(e, AResult.class, AResult.SCHEMA$, 1380458400001L);
		for(AResult r : res){
			System.out.println(r);
		}
	}
}
