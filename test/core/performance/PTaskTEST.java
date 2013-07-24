package core.performance;

import java.util.List;

import org.junit.Test;

import core.DBManager;
import core.Endpoint;
import core.Endpoints;
import core.availability.AResult;
import core.availability.ATask;

public class PTaskTEST {

	@Test
	public void testATask() throws Exception {
		
		Endpoint ep = Endpoints.DBPEDIA;
		
		PTask task = new PTask(ep, null,SpecificPTask.ASKO,SpecificPTask.ASKPO);
		PResult res = task.call();
		
		
		DBManager db = new DBManager();
		db.insertResult(res);
	
		
		List<PResult> r = db.getResults(ep, PResult.class);
		for(PResult rr: r){
			System.out.println(rr);
		}
	}
}
