package sparqles.core;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sparqles.avro.Dataset;
import sparqles.avro.Endpoint;
import sparqles.avro.EndpointResult;
import sparqles.avro.availability.AResult;
import sparqles.avro.discovery.DGETInfo;
import sparqles.avro.discovery.DResult;
import sparqles.avro.discovery.QueryInfo;
import sparqles.avro.discovery.RobotsTXT;
import sparqles.avro.features.FResult;
import sparqles.avro.features.FSingleResult;
import sparqles.avro.performance.PResult;
import sparqles.avro.performance.PSingleResult;
import sparqles.utils.MongoDBManager;

public class MongoDBTest {

	EndpointResult epr = new EndpointResult(Endpoints.DBPEDIA, 1L, 2L);
	RobotsTXT r = new RobotsTXT(true, true, false, false, false, false, null);
	AResult aDummy = new AResult(epr,
			1L, true, false, null, "testDummy");
	
	FResult fDummy = new FResult(epr, new HashMap<CharSequence, FSingleResult>());
	PResult pDummy = new PResult(epr, new HashMap<CharSequence, PSingleResult>());
	DResult dDummy = new DResult(epr, r, (List) new ArrayList<DGETInfo>(), new ArrayList<QueryInfo>());
	
	protected MongoDBManager m;

	@Before
	public void setUp() throws Exception {
		SPARQLESProperties.init(new File("src/test/resources/sparqles.properties"));
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
		m.initEndpointCollection();
		m.setup();
	
		Endpoint e = Endpoints.DBPEDIA;
		
		assertTrue(m.insert(e));
		assertEquals(1, m.get(Endpoint.class, Endpoint.SCHEMA$).size());
		
		assertTrue(m.insert(e));
		assertEquals(1, m.get(Endpoint.class, Endpoint.SCHEMA$).size());
	}
	
	@Test
	public void testUpdateEP() {
		m.initEndpointCollection();
		m.setup();
	
		Endpoint e = Endpoints.DBPEDIA;
		
		
		assertTrue(m.insert(e));
		List<Endpoint> ee = m.get(Endpoint.class, Endpoint.SCHEMA$);
		assertEquals(1, ee.size());
		
//		assertTrue(m.insert(e));
//		ee = m.get(Endpoint.class, Endpoint.SCHEMA$);
//		assertEquals(1, ee.size());
		
		Endpoint edb = ee.get(0);
		assertEquals(0,  edb.getDatasets().size());
		e.getDatasets().add(new Dataset("Test","Test"));
		assertTrue(m.update(e));
		ee = m.get(Endpoint.class, Endpoint.SCHEMA$);
		edb = ee.get(0);
		assertEquals(1,  edb.getDatasets().size());
		
	}
	
//	@Test
//	public void testInsertAResult() {
//		m.setup();
//	
//		AResult e = aDummy;
//				
//		assertTrue(m.insert(e));
//		assertEquals(1, m.get(AResult.class, AResult.SCHEMA$).size());
//		
//		assertTrue(m.insert(e));
//		assertEquals(2, m.get(AResult.class, AResult.SCHEMA$).size());
//	}
	
	
}
