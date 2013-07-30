package core.availability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.QueryManager;

import com.hp.hpl.jena.query.QueryExecution;

import core.Endpoint;
import core.EndpointResult;
import core.Task;




public class ATask extends Task<AResult>{
	
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
		log.debug("[RUN] {}", epr.getEndpoint().getUri().toString());
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
				log.info("[SUCCESS] [ASK] {}", epr.getEndpoint());
				return result;
			}
			else{
				return testSelect(epr);
			}
		} catch (InterruptedException e) {
			result.setException("InterruptedException: "+e.getMessage());
			log.error("[RUN] {}: {}", epr.getEndpoint().getUri().toString(), e.getClass().getSimpleName()+" "+e.getMessage());
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
			boolean response = qe.execAsk();
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
				log.info("[SUCCESS] [SELECT] {}", epr.getEndpoint());
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

			result.setExplaination("SPARQL Endpoint is unavailable. "+failureExplanation);
			//	    		System.out.println("Thread: " + result.getPackageId()+"\tFALSE"+"\t"+(System.currentTimeMillis()-start));
			log.error("[RUN] {}: {}", epr.getEndpoint().getUri().toString(), "SPARQL Endpoint is unavailable. "+failureExplanation);
			return result;
		}
	}

	
}
