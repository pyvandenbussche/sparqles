package core.features;

import java.util.List;

import org.junit.Test;



import core.DBManager;
import core.Endpoint;
import core.Endpoints;
import core.FileManager;


public class FTaskTEST {

	@Test
	public void testATask() throws Exception {
		
		Endpoint ep = Endpoints.DBPEDIA;
		
		DBManager db = new DBManager();
		FileManager fm = new FileManager();
		
		
		FTask task = new FTask(ep, SpecificFTask.SPARQL11_CON,SpecificFTask.SPARQL1_ASK);
		task.setDBManager(db);
		task.setFileManager(fm);
		
		task.call();
			
		List<FResult> r = db.getResults(ep, FResult.class);
		for(FResult rr: r){
			System.out.println(rr);
		}
	}
}
