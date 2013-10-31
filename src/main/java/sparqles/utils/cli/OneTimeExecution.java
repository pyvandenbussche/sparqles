package sparqles.utils.cli;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.avro.specific.SpecificRecordBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.avro.Endpoint;
import sparqles.core.Task;
import sparqles.core.TaskFactory;
import sparqles.utils.FileManager;
import sparqles.utils.MongoDBManager;

public class OneTimeExecution<T extends SpecificRecordBase> {
	private static final Logger log = LoggerFactory.getLogger(OneTimeExecution.class);
	private MongoDBManager dbm;
	private FileManager fm;
	
	
	public OneTimeExecution(MongoDBManager dbm, FileManager fm) {
		this.dbm = dbm;
		this.fm = fm;
	}

	public void run(String task) {
		Collection<Endpoint> eps = dbm.get(Endpoint.class, Endpoint.SCHEMA$);
		

		ExecutorService executor = Executors.newFixedThreadPool(100);
	    
	    List<Callable<T>> todo = new ArrayList<Callable<T>>(eps.size());

	  
	    
	    for(Endpoint ep: eps){
			Task<T> t = TaskFactory.create(task, ep, dbm, fm);
			log.info("OneTimeSchedule {}", ep);
			todo.add(t);
//			compService.submit(t);
		}
	    
	    
	    try {
			List<Future<T>> all= executor.invokeAll(todo);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
//	    
//	    Future<T> f= null;
//	    try {
//	    	while((f = compService.poll()) != null){
//	    		while(!f.isDone()){
//	    			Thread.sleep(500);
//	    			log.debug("Waiting unitl task {} is done", f.get());
//	    		}
//				log.info("Task for {} completed", f.get());
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	    log.info("All tasks are done");
	    executor.shutdown();
	}
}