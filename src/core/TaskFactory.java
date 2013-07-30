package core;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.availability.ATask;
import core.discovery.DTask;
import core.performance.PTask;
import core.performance.SpecificPTask;

public class TaskFactory {
	private static final Logger log = LoggerFactory.getLogger(TaskFactory.class);
	public static Task create(String task, URI endpoint) {
		Endpoint ep = EndpointFactory.newEndpoint(endpoint);
		if(task.equalsIgnoreCase("ptask")){
			return new PTask(ep, SpecificPTask.values());
		}else if(task.equalsIgnoreCase("atask")){
			return new ATask(ep);
		}else if(task.equalsIgnoreCase("ftask")){
			return new ATask(ep);
		}else if(task.equalsIgnoreCase("dtask")){
			return new DTask(ep);
		}else{
			log.warn("Task {} not supported or known", task);
			return null;	
		}
		
	}

}
