package sparqles.core.availability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.utils.LogHandler;
import sparqles.utils.QueryManager;

import com.hp.hpl.jena.query.QueryExecution;

import sparqles.core.Endpoint;
import sparqles.core.EndpointResult;
import sparqles.core.EndpointTask;

/**
 * This class performs the required task to study the availability of an endpoint. 
 * 
 * We first perform a ASK query to check if an endpoint is available.
 * If the ask query is successful but returns false, we perform a SELECT LIMIT 1 query
 * @author UmbrichJ
 *
 */
public class ATask extends EndpointTask<AResult>{
	
	/**
	 * static class logger
	 */
	private static final Logger log = LoggerFactory.getLogger(ATask.class);
	
	private final static String ASKQUERY = "ASK WHERE{?s ?p ?o}";
	private final static String SELECTQUERY= "SELECT ?s WHERE{?s ?p ?o} LIMIT 1";
	
	public ATask(Endpoint ep) {
		super(ep);
	}
	
	
	@Override
	public AResult process(EndpointResult epr) {
		AResult result = new AResult();
		result.setEndpointResult(epr);
		LogHandler.run(log, "{}", epr.getEndpoint().getUri().toString());
		
		long start = System.currentTimeMillis();
		try {
			QueryExecution qe = QueryManager.getExecution(epr.getEndpoint(), ASKQUERY);
			boolean response = qe.execAsk();
			if(response){
				result.setResponseTime((System.currentTimeMillis()-start));
				if((System.currentTimeMillis()-start)>20000){
					result.setIsAvailable(false);
					result.setExplaination("SPARQL Endpoint is timeout");
				}
				else{
					result.setIsAvailable(response);
					result.setExplaination("Endpoint is operating normally");
				}
				LogHandler.debugSuccess(log,"[ASK] {}", epr.getEndpoint().getUri().toString());
				return result;
			}
			else{
				return testSelect(epr);
			}
		} catch (InterruptedException e) {
			result.setException(LogHandler.toString(e));
			LogHandler.warn(log, "[ASK] query and "+epr.getEndpoint().getUri().toString(), e);
			return result;        
		}catch (Exception e) {
			return testSelect(epr);
		}
	}

	private AResult testSelect(EndpointResult epr){
		AResult result = new AResult();
		result.setEndpointResult(epr);
		long start = System.currentTimeMillis();
		try{
			QueryExecution qe = QueryManager.getExecution(epr.getEndpoint(), SELECTQUERY);
			boolean response = qe.execSelect().hasNext();
			
			if(response) {
				result.setResponseTime((System.currentTimeMillis()-start));
				if((System.currentTimeMillis()-start)>20000){
					result.setIsAvailable(false);
					result.setExplaination("SPARQL Endpoint is timeout");
				}
				else{
					result.setIsAvailable(response);
					result.setExplaination("Endpoint is operating normally");
				}
				LogHandler.debugSuccess(log,"[SELECT] {}", epr.getEndpoint().getUri().toString());
				return result;
			}
			else{
				result.setIsAvailable(response);
				//	        		System.out.println("Thread: " + result.getPackageId()+"\tFALSE"+"\t"+responseTime);
				return result;
			}
		}catch (Exception e1) {
			result.setIsAvailable(false);
			//				result.setResponseTime(""+(System.currentTimeMillis()-start));
			String failureExplanation="";
			failureExplanation=e1.getMessage().replaceAll("rethrew: ", "");
			failureExplanation=failureExplanation.replaceAll("Failed when initializing the StAX parsing engine", "SPARQL protocol not respected");
			failureExplanation=failureExplanation.replaceAll("java.net.UnknownHostException:", "Unknown host:");
			failureExplanation=failureExplanation.replaceAll("HttpException:", "HTTP error");
			if(failureExplanation.contains("401 Authorization Required"))result.setIsPrivate(true);
			result.setException(LogHandler.toString(e1));
			result.setExplaination("SPARQL Endpoint is unavailable. "+failureExplanation);
			//	    		System.out.println("Thread: " + result.getPackageId()+"\tFALSE"+"\t"+(System.currentTimeMillis()-start));
			LogHandler.warn(log, "[ASK] query and "+epr.getEndpoint().getUri().toString(), e1);
//			log.error("[RUN] {}: {}", epr.getEndpoint().getUri().toString(), "SPARQL Endpoint is unavailable. "+failureExplanation);
			return result;
		}
	}
}