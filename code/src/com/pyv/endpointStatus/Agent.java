package com.pyv.endpointStatus;

import java.util.concurrent.Callable;

import com.pyv.endpointStatus.objects.EndpointResult;

public class Agent implements Callable<EndpointResult>{
	private EndpointResult result = null;
	
	   public Agent(EndpointResult result) {
	      this.result=result;	      
	   }
	   
	   public EndpointResult call() throws Exception {
		 	long start = System.currentTimeMillis();
//		 	String responseTime="";
       	try {
       		boolean response = EndpointStatusUtil.executeAsk(result.getEndpointURL());
       		if(response){
        		result.setResponseTime(""+(System.currentTimeMillis()-start));
        		
        		if((System.currentTimeMillis()-start)>20000){
        			result.setAvailable(false);
	        		result.setExplaination("SPARQL Endpoint is timeout");
        		}
        		else{
	        		result.setAvailable(response);
	        		result.setExplaination("Endpoint is operating normally");
        		}
//       			System.out.println("Thread: " + result.getPackageId()+"\tTRUE"+"\t"+responseTime);
       			return result;
       		}
       		else{
       			return testSelect();
       		}
        } catch (InterruptedException e) {
        	return result;        
       	}catch (Exception e) {
       		return testSelect();
       	}
	 }
	 private EndpointResult testSelect(){
		 long start = System.currentTimeMillis();
		 try{
				
    		boolean response = EndpointStatusUtil.executeAsk2(result.getEndpointURL());
	        	if(response) {
	        		
	        		result.setResponseTime(""+(System.currentTimeMillis()-start));
	        		if((System.currentTimeMillis()-start)>20000){
	        			result.setAvailable(false);
		        		result.setExplaination("SPARQL Endpoint is timeout");
	        		}
	        		else{
	        			result.setAvailable(response);
	        			result.setExplaination("Endpoint is operating normally");
	        		}
//	        		System.out.println("Thread: " + result.getPackageId()+"\tTRUE (SEL)"+"\t"+responseTime);
	        		return result;
				}
	        	else{
	        		result.setAvailable(response);
//	        		System.out.println("Thread: " + result.getPackageId()+"\tFALSE"+"\t"+responseTime);
	        		return result;
	        	}
			}catch (Exception e1) {
				result.setAvailable(false);
//				result.setResponseTime(""+(System.currentTimeMillis()-start));
				String failureExplanation="";
	    		failureExplanation=e1.getMessage().replaceAll("rethrew: ", "");
	    		failureExplanation=failureExplanation.replaceAll("Failed when initializing the StAX parsing engine", "SPARQL protocol not respected");
	    		failureExplanation=failureExplanation.replaceAll("java.net.UnknownHostException:", "Unknown host:");
	    		failureExplanation=failureExplanation.replaceAll("HttpException:", "HTTP error");
	    		if(failureExplanation.contains("401 Authorization Required"))result.setPrivate(true);
	    		
	    		result.setExplaination("SPARQL Endpoint is unavailable. "+failureExplanation);
//	    		System.out.println("Thread: " + result.getPackageId()+"\tFALSE"+"\t"+(System.currentTimeMillis()-start));
	    		return result;
			}
	 }
}
