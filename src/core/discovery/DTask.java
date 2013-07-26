package core.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Endpoint;
import core.EndpointResult;
import core.Task;
import core.performance.PTask;

/**
 * DTaskGET: This task inspects the header and content after a HTTP GET on the endpoint URI. 
 * 
 * We perform the following checks:
 * 1) if the header contains any information about meta data for this endpoint
 * 2) if the endpoint returns any RDF content 
 * 3) if 2 succeeds, then check for vocabulary terms from either voiD or SPARQL1.1
 * @author UmbrichJ
 *
 */
public class DTask extends Task<DResult> {
	private static final Logger log = LoggerFactory.getLogger(DTask.class);
	
	public DTask(Endpoint ep) {
		super(ep);
	}
	
	@Override
	public DResult process(EndpointResult epr) {
		DResult result = new DResult();
		result.setEndpointResult(epr);
		
		result.setGetResult(SpecificDTask.newGetRun(epr.getEndpoint()).execute());
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		e.printStackTrace();
		}
		result.setVoidResult(SpecificDTask.newSelfVoidRun(epr.getEndpoint()).execute());
		
		return result;
	}
}