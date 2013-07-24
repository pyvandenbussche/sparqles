package core.availability;

import java.util.List;

import org.junit.Test;

import core.DBManager;
import core.Endpoint;
import core.Endpoints;

public class ATaskTEST {

	@Test
	public void testATask() throws Exception {
		
		Endpoint ep = Endpoints.DBPEDIA;
		
		ATask task = new ATask(ep);
		AResult res = task.call();
		
		
		DBManager db = new DBManager();
		db.insertResult(res);
	
		
		List<AResult> r = db.getResults(ep, AResult.class);
		for(AResult rr: r){
			System.out.println(rr);
		}
	}
}
