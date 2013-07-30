package core.availability;

import java.util.List;

import org.junit.Test;

import com.hp.hpl.jena.util.FileManager;

import core.DBManager;
import core.Endpoint;
import core.Endpoints;

public class ATaskTEST {

	@Test
	public void testATask() throws Exception {
		
		Endpoint ep = Endpoints.DBPEDIA;
		
		
		DBManager db = new DBManager();
		core.FileManager fm = new core.FileManager();
		
		ATask task = new ATask(ep);
		task.setDBManager(db);
		task.setFileManager(fm);
		
		AResult res = task.call();
		
		
		List<AResult> r = db.getResults(ep, AResult.class);
		for(AResult rr: r){
			System.out.println("From db:"+rr);
		}
		
		r = fm.readResults(ep, AResult.class);
		for(AResult rr: r){
			System.out.println("From file:"+rr);
		}
		
		
	}
}
