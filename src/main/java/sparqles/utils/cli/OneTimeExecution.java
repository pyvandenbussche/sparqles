package sparqles.utils.cli;

import java.util.Collection;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.avro.specific.SpecificRecordBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.core.Endpoint;
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
	    CompletionService<T> compService = new ExecutorCompletionService<T>(executor);
	    
	    for(Endpoint ep: eps){
			Task<T> t = TaskFactory.create(task, ep, dbm, fm);
			compService.submit(t);
		}
	    Future<T> f= null;
	    try {
			while((f = compService.take())!=null){
				log.info("Task for {} completed", f.get());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	    executor.shutdown();
		
	}

}
