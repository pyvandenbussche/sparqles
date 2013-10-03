package sparqles.core;

import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.analytics.AAnalyser;
import sparqles.analytics.Analytics;
import sparqles.analytics.IndexViewAnalytics;
import sparqles.core.availability.ATask;
import sparqles.core.discovery.DTask;
import sparqles.core.features.FTask;
import sparqles.core.features.SpecificFTask;
import sparqles.core.performance.PTask;
import sparqles.core.performance.SpecificPTask;
import sparqles.utils.FileManager;
import sparqles.utils.MongoDBManager;
import static sparqles.core.CONSTANTS.*;

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
		Analytics a = null;
		if(task.equalsIgnoreCase(PTASK)){
			t= new PTask(ep, SpecificPTask.values());
		}else if(task.equalsIgnoreCase(ATASK)){
			t= new ATask(ep);
			a = new AAnalyser(dbm);
		}else if(task.equalsIgnoreCase(FTASK)){
			t= new FTask(ep, SpecificFTask.values());
		}else if(task.equalsIgnoreCase(DTASK)){
			t= new DTask(ep);
		}else  if(task.equalsIgnoreCase(ITASK)){
			t = new IndexViewAnalytics();
		}
		else{
			log.warn("Task {} not supported or known", task);
			return null;	
		}
		if(dbm!=null && t!=null)
			t.setDBManager(dbm);
		if(fm != null && t!=null && t instanceof EndpointTask)
			((EndpointTask)t).setFileManager(fm);
		
		if(t instanceof EndpointTask)
			((EndpointTask)t).setAnalytics(a);
		
		return t;
	}
}