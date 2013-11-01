package sparqles.core;


import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sparqles.avro.Endpoint;


import sparqles.avro.schedule.Schedule;
import sparqles.core.availability.ATask;
import sparqles.schedule.Scheduler;
import sparqles.utils.MongoDBManager;

public class AvailabilityTEST {

	private MongoDBManager m;


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
	public void testSingle() throws Exception {
		
		ATask a = new ATask(Endpoints.DBPEDIA);
		a.setDBManager(m);
		
		a.call();
	}
	
	@Test
	public void test() {

		Endpoint ep = Endpoints.DBPEDIA;
		
		m.initEndpointCollection();
		m.initAggregateCollections();
		m.insert(ep);
		
		Schedule sc = new Schedule();
		sc.setEndpoint(ep);
		sc.setATask("0 0/2 * 1/1 * ? *");
		m.insert(sc);
		
		Scheduler s = new Scheduler();
		
		s.useDB(m);
		s.init(m);
		
		try {
			Thread.sleep(60*60*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		s.close();
		m.close();
		
	}
}
