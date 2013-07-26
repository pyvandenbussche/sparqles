package core;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

import core.performance.PResult;
import core.performance.PTask;
import core.performance.SpecificPTask;

public class TaskTester {

	@Test
	public void test() throws SQLException {
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
