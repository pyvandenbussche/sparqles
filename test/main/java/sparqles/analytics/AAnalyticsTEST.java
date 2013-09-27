package sparqles.analytics;

import static org.junit.Assert.*;

import java.net.URISyntaxException;
import java.util.Properties;

import org.junit.Test;

import sparqles.core.SPARQLESProperties;
import sparqles.core.Endpoint;
import sparqles.core.EndpointFactory;
import sparqles.utils.DBManager;

public class AAnalyticsTEST {

	private DBManager _db;

	
	
	@Test
	public void test() throws URISyntaxException {
		Properties prop = new Properties();
		prop.setProperty("db.driver", "org.h2.Driver");
		prop.setProperty("db.url","jdbc:h2:./test.run/db.h2");
		SPARQLESProperties.init(prop);
		
		_db = new DBManager();
		
		AAnalyser a = new AAnalyser(_db);
		
		Endpoint ep = EndpointFactory.newEndpoint("http://dbpedia.org/sparql");
		System.out.println("Analyse");
		a.analyse(ep);
		
		_db.close();
	}

}
