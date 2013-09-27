package sparqles.core;

import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.core.availability.ATask;
import sparqles.core.discovery.DTask;
import sparqles.core.features.FTask;
import sparqles.core.features.SpecificFTask;
import sparqles.core.performance.PTask;
import sparqles.core.performance.SpecificPTask;
import sparqles.utils.FileManager;
import sparqles.utils.MongoDBManager;

public class TaskFactory {
	private static final Logger log = LoggerFactory.getLogger(TaskFactory.class);
	
	
	public static Task create(String task, String endpoint, MongoDBManager dbm,
			FileManager fm) throws URISyntaxException {
		Endpoint ep = EndpointFactory.newEndpoint(endpoint);
		return create(task, ep, dbm, fm);
	}
	
	public static Task create(String task, Endpoint ep, MongoDBManager dbm,
			FileManager fm) {
		Task t = null;
		if(task.equalsIgnoreCase("ptask")){
			t= new PTask(ep, SpecificPTask.values());
		}else if(task.equalsIgnoreCase("atask")){
			t= new ATask(ep);
		}else if(task.equalsIgnoreCase("ftask")){
			t= new FTask(ep, SpecificFTask.values());
		}else if(task.equalsIgnoreCase("dtask")){
			t= new DTask(ep);
		}else{
			log.warn("Task {} not supported or known", task);
			return null;	
		}
		if(dbm!=null && t!=null)
			t.setDBManager(dbm);
		if(fm != null && t!=null)
			t.setFileManager(fm);
		
		return t;
	}
}