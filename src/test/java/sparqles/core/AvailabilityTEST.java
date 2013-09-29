package sparqles.core;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sparqles.core.availability.AResult;
import sparqles.core.availability.ATask;
import sparqles.schedule.Schedule;
import sparqles.schedule.Scheduler;
import sparqles.utils.MongoDBManager;

public class AvailabilityTEST {

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
	public void test() {

		Endpoint ep = Endpoints.DBPEDIA;
		
		
		m.initEndpointCollection();
		m.insert(ep);
		
		
		Schedule sc = new Schedule();
		sc.setEndpoint(ep);
		sc.setATask("0 0/2 * 1/1 * ? *");
		m.insert(sc);
		
		Scheduler s = new Scheduler();
		EndpointManager epm = new  EndpointManager();
		
		epm.init(m);
		s.useDB(m);
		s.init(epm);
		
		try {
			Thread.sleep(30*60*60*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		s.close();
		m.close();
		
	}
}
