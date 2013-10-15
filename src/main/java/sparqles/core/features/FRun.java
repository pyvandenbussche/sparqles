package sparqles.core.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.core.Endpoint;
import sparqles.core.SPARQLESProperties;

public class FRun extends TaskRun {

	
	private static final Logger log = LoggerFactory.getLogger(FRun.class);
	
	
	
	
		
	public FRun(Endpoint ep, String queryFile ) {
		this(ep, queryFile, System.currentTimeMillis());
	}


	public FRun(Endpoint ep, String queryFile, Long start) {
		super( ep, queryFile, SPARQLESProperties.getFTASK_QUERIES(),start,log);
		
	}

	public FSingleResult execute() {
		FSingleResult result = new FSingleResult();
		
		result.setQuery(_query);
    	
		log.debug("[RUN] {} over {}", _queryFile, _ep.getUri());

		result.setRun(run());
        
        return result;
	}





	
}
