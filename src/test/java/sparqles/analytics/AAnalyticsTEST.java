package sparqles.analytics;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sparqles.core.SPARQLESProperties;
import sparqles.avro.Endpoint;
import sparqles.avro.availability.AResult;
import sparqles.core.EndpointFactory;
import sparqles.utils.MongoDBManager;


public class AAnalyticsTEST {

	
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
	public void test() throws URISyntaxException {
		m.initAggregateCollections();
		AAnalyser a = new AAnalyser(m);
		
		Endpoint ep = EndpointFactory.newEndpoint("http://dbpedia.org/sparql");
		System.out.println("Analyse");
		
		TreeSet<AResult> res = new TreeSet<AResult>(new Comparator<AResult>() {
			public int compare(AResult o1, AResult o2) {
				int diff =o1.getEndpointResult().getStart().compareTo(o2.getEndpointResult().getStart()); 
				return diff;
			}
		});

		List<AResult> epRes = m.getResults(ep, AResult.class, AResult.SCHEMA$);
		System.out.println("Results: "+epRes.size());
		for(AResult epres: epRes){
			res.add(epres);
			System.out.println(new Date(epres.getEndpointResult().getStart()));
		}
		
//		if(_onlyLast&&epRes.size()!=0){
//			a.analyse(res.last());
//		}else{
			for(AResult ares: res){
				a.analyse(ares);
			}
//		}
//		log.info("ANALYSE AVAILABILITY {} and {}",ep, epRes.size());
		
	}

}
