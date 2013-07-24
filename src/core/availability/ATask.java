package core.availability;

import utils.QueryManager;

import com.hp.hpl.jena.query.QueryExecution;

import core.Endpoint;
import core.EndpointResult;
import core.Task;




public class ATask extends Task<AResult>{
	private final static String ASKQUERY = "ASK WHERE{?s ?p ?o}";
	private final static String SELECTQUERY= "SELECT ?s WHERE{?s ?p ?o} LIMIT 1";
	
	public ATask(Endpoint ep) {
		super(ep);
	}
	
	@Override
	public AResult process(EndpointResult epr) {
		AResult result = new AResult();
		result.setEndpointResult(epr);
		
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
				//       			System.out.println("Thread: " + result.getPackageId()+"\tTRUE"+"\t"+responseTime);
				return result;
			}
			else{
				return testSelect(epr);
			}
		} catch (InterruptedException e) {
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
			return result;
		}
	}

	
}
