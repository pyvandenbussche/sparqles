package core;

import java.sql.SQLException;
import java.util.List;

import org.apache.avro.specific.SpecificRecordBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sparqles.core.*;

public class TaskTester {


	private FileManager fm;
	private DBManager db;

	@Before
	public void setUp() throws SQLException
	{
		db = DBManager.getInstance();
		fm = FileManager.getInstance();

	}
	
	@After
	public void tearDown() throws SQLException
	{
		db.close();
//		fm.close();

	}	

	@Test
	public void test()  {
		Endpoint ep = Endpoints.DBPEDIA;




//		PTask task = new PTask(ep, SpecificPTask.ASKO,SpecificPTask.ASKPO);
//		runTask(ep,task, true, true, PResult.class );
//		
//		FTask ftask = new FTask(ep, SpecificFTask.SPARQL11_CON,SpecificFTask.SPARQL1_ASK);
//		runTask(ep,ftask, true, true, FResult.class );
//		
//		ATask atask = new ATask(ep);
//		runTask(ep,atask, true, true, AResult.class );
//		
		DTask dtask = new DTask(ep);
		runTask(ep, dtask, true, true, DResult.class );
		
		
	}

	protected <V extends SpecificRecordBase> void runTask(Endpoint ep, Task<V> task, boolean useDB, boolean useFM,
			Class<V> cls) {
		if(useDB) task.setDBManager(db);
		if(useFM) task.setFileManager(fm);

		System.err.println("[TEST] running "+task+" against "+ep);
		V res = task.call();
		System.err.println("[TEST] _______");
		System.err.println(res);
		System.out.println("v:"+res.toString());
		if(useDB){
			List<V> r = db.getResults(ep, cls);
			System.err.println("[TEST] [DB] returned "+r.size()+" results");
			for(V rr: r){
				if(rr.toString().equals(res.toString())){
					System.err.println("[TEST]  [DB] Found our result");
				}
			}
		}
		if(useFM){
			List<V> r = fm.getResults(ep, cls);
			System.err.println("[TEST] [FM] returned "+r.size()+" results");
			for(V rr: r){
				if(rr.toString().compareTo(res.toString())==0){
					System.err.println("[TEST]  [FM] Found our result");
				}
			}
		}
		
	}

}
