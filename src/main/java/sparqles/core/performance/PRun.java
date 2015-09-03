package sparqles.core.performance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.avro.Endpoint;
import sparqles.avro.performance.PSingleResult;
import sparqles.core.SPARQLESProperties;
import sparqles.core.interoperability.TaskRun;

public class PRun extends TaskRun{

	
	private static final Logger log = LoggerFactory.getLogger(PRun.class);
	

	
	
	public PRun(Endpoint ep, String queryFile ) {
		this(ep, queryFile, System.currentTimeMillis());
	}

	public PRun(Endpoint ep, String queryFile, Long start) {
		super( ep, queryFile,SPARQLESProperties.getPTASK_QUERIES(), start,log);
	}
	
	public PSingleResult execute() {
		PSingleResult result = new PSingleResult();
		
		result.setQuery(_query);
    	
		log.debug("RUN COLD {} over {}", _queryFile, _ep.getUri());
        result.setCold(run());
        
        try {
			Thread.sleep(SPARQLESProperties.getPTASK_WAITTIME());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        log.debug("RUN WARM {} over {}", _queryFile, _ep.getUri());
        result.setWarm(run());
		
        return result;
	}
}