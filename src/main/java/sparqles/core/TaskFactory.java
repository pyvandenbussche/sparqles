package sparqles.core;

import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.analytics.AAnalyser;
import sparqles.analytics.Analytics;
import sparqles.analytics.DAnalyser;
import sparqles.analytics.FAnalyser;
import sparqles.analytics.IndexViewAnalytics;
import sparqles.analytics.PAnalyser;
import sparqles.analytics.RefreshDataHubTask;
import sparqles.avro.Endpoint;
import sparqles.core.availability.ATask;
import sparqles.core.discovery.DTask;
import sparqles.core.interoperability.FTask;
import sparqles.core.interoperability.SpecificFTask;
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
			a= new PAnalyser(dbm);
		}else if(task.equalsIgnoreCase(ATASK)){
			t= new ATask(ep);
			a = new AAnalyser(dbm);
		}else if(task.equalsIgnoreCase(FTASK)){
			t= new FTask(ep, SpecificFTask.values());
			a = new FAnalyser(dbm);
		}else if(task.equalsIgnoreCase(DTASK)){
			t= new DTask(ep);
			a = new DAnalyser(dbm);
		}else  if(task.equalsIgnoreCase(ITASK)){
			t = new IndexViewAnalytics();
		}
		else  if(task.equalsIgnoreCase(ETASK)){
			t = new RefreshDataHubTask();
		}
		else{
			log.warn("Task {} not supported or known", task);
			t= null;	
		}
		if(dbm!=null && t!=null)
			t.setDBManager(dbm);
		if(fm != null && t!=null && t instanceof EndpointTask)
			((EndpointTask)t).setFileManager(fm);
		if(t instanceof EndpointTask)
			((EndpointTask)t).setAnalytics(a);
		
		if(t != null)
			log.info("Successfully create {} task for {}", task, ep.getUri());
		return t;
	}
}