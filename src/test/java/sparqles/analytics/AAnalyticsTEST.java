package sparqles.analytics;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sparqles.core.SPARQLESProperties;
import sparqles.core.Endpoint;
import sparqles.core.EndpointFactory;
import sparqles.utils.MongoDBManager;


public class AAnalyticsTEST {

	
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
	public void test() throws URISyntaxException {
		
		AAnalyser a = new AAnalyser(m);
		
		Endpoint ep = EndpointFactory.newEndpoint("http://dbpedia.org/sparql");
		System.out.println("Analyse");
		a.analyse(ep);
		
		
	}

}
