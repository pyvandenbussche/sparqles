package sparqles.analytics;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sparqles.core.SPARQLESProperties;
import sparqles.utils.MongoDBManager;

public class IndexViewAnalyticsTEST {
	
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
		
		
		IndexViewAnalytics a = new IndexViewAnalytics();
		a.setDBManager(m);
		a.call();
	}

}
