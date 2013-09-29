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

public class EPViewTEST {

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
		
		
	}
}
