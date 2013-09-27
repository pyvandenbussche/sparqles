package core.performance;

import java.util.List;

import org.junit.Test;



import core.DBManager;
import core.Endpoint;
import core.Endpoints;
import core.FileManager;


public class PTaskTEST {

	@Test
	public void testATask() throws Exception {
		
		Endpoint ep = Endpoints.DBPEDIA;
		
		DBManager db = new DBManager();
		FileManager fm = new FileManager();
		
		
		PTask task = new PTask(ep, null,SpecificPTask.ASKO,SpecificPTask.ASKPO);
		task.setDBManager(db);
		task.setFileManager(fm);
		
		task.call();
			
		List<PResult> r = db.getResults(ep, PResult.class);
		for(PResult rr: r){
			System.out.println(rr);
		}
	}
}
