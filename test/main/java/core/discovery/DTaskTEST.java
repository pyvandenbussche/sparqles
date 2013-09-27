package core.discovery;

import java.util.List;

import org.junit.Test;

import core.DBManager;
import core.Endpoint;
import core.Endpoints;
import core.FileManager;
import core.performance.PResult;
import core.performance.PTask;
import core.performance.SpecificPTask;

public class DTaskTEST {

	@Test
	public void test() {
		try {
			
			Endpoint ep = Endpoints.DBPEDIA;
			
			DBManager db = new DBManager();
			FileManager fm = new FileManager();
			
			DTask task = new DTask(ep);
			task.setDBManager(db);
			task.setFileManager(fm);
			
			task.call();
				
			List<DResult> r = db.getResults(ep, DResult.class);
			for(DResult rr: r){
				System.out.println(rr);
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
