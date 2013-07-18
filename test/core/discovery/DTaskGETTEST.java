package core.discovery;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import core.Endpoint;

public class DTaskGETTEST {

	@Test
	public void test() {
		try {
			Endpoint ep = new Endpoint(new URI("http://aemet.linkeddata.es/sparql"));
			DTaskGET dget= new DTaskGET(ep, new File("."));
			DResultGET res = dget.call();
			
			System.out.println(res);
			System.out.println(res.serialize());
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
