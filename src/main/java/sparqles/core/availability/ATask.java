package sparqles.core.availability;

import org.apache.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.utils.LogFormater;
import sparqles.utils.QueryManager;

import com.hp.hpl.jena.query.QueryExecution;

import sparqles.avro.Endpoint;
import sparqles.avro.EndpointResult;
import sparqles.avro.availability.AResult;
import sparqles.core.EndpointTask;
import sparqles.core.interoperability.TaskRun;

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
		result.setExplanation("Endpoint is operating normally");
		log.debug("[exec] {}", epr.getEndpoint().getUri().toString());
		
		long start = System.currentTimeMillis();
		try {
			QueryExecution qe = QueryManager.getExecution(epr.getEndpoint(), ASKQUERY);
			boolean response = qe.execAsk();
			if(response){
				result.setResponseTime((System.currentTimeMillis()-start));
				if((System.currentTimeMillis()-start)>20000){
					result.setIsAvailable(false);
					result.setExplanation("SPARQL Endpoint is timeout");
				}
				else{
					result.setIsAvailable(response);
					result.setExplanation("Endpoint is operating normally");
				}
				log.debug("[executed] ask {}", epr.getEndpoint().getUri().toString());
				return result;
			}
			else{
				return testSelect(epr);
			}
		} catch (InterruptedException e) {
			result.setException(LogFormater.toString(e));
			result.setExplanation(LogFormater.toString(e));
			log.warn("[executed] ASK query for {}", epr.getEndpoint().getUri(), e);
			return result;        
		}catch (Exception e) {
			return testSelect(epr);
		}
	}

	private AResult testSelect(EndpointResult epr){
		AResult result = new AResult();
		result.setEndpointResult(epr);
		result.setExplanation("Endpoint is operating normally");
		long start = System.currentTimeMillis();
		try{
			QueryExecution qe = QueryManager.getExecution(epr.getEndpoint(), SELECTQUERY);
			qe.setTimeout(TaskRun.A_FIRST_RESULT_TIMEOUT, TaskRun.A_FIRST_RESULT_TIMEOUT);
			boolean response = qe.execSelect().hasNext();
			
			if(response) {
				result.setResponseTime((System.currentTimeMillis()-start));
				if((System.currentTimeMillis()-start)>TaskRun.A_FIRST_RESULT_TIMEOUT){
					result.setIsAvailable(false);
					result.setExplanation("SPARQL Endpoint is timeout");
				}
				else{
					result.setIsAvailable(response);
					result.setExplanation("Endpoint is operating normally");
				}
				log.debug("[executed] select {}", epr.getEndpoint().getUri().toString());
				return result;
			}
			else{
				result.setIsAvailable(response);
				log.debug("[executed] no response {}", epr.getEndpoint().getUri().toString());
				//	        		System.out.println("Thread: " + result.getPackageId()+"\tFALSE"+"\t"+responseTime);
				return result;
			}
		}catch(HttpException he){
			result.setIsAvailable(false);
			result.setException(LogFormater.toString(he));
			result.setExplanation(LogFormater.toString(he));
		}catch (Exception e1) {
			result.setIsAvailable(false);
			result.setException(LogFormater.toString(e1));
			result.setExplanation(LogFormater.toString(e1));
			if(e1.getMessage()!=null)
				if(e1.getMessage().contains("401 Authorization Required"))result.setIsPrivate(true);
//			i
//			String failureExplanation="";
//			failureExplanation=e1.getMessage().replaceAll("rethrew: ", "");
//			failureExplanation=failureExplanation.replaceAll("Failed when initializing the StAX parsing engine", "SPARQL protocol not respected");
//			failureExplanation=failureExplanation.replaceAll("java.net.UnknownHostException:", "Unknown host:");
//			failureExplanation=failureExplanation.replaceAll("HttpException:", "HTTP error");
//			if(failureExplanation.contains("401 Authorization Required"))result.setIsPrivate(true);
//			result.setException(LogFormater.toString(e1));
//			result.setExplanation("SPARQL Endpoint is unavailable. "+failureExplanation);

			log.warn("[executed] SELECT query for {}", epr.getEndpoint().getUri(), e1);
		}
		return result;
	}
}