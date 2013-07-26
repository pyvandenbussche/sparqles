package core.performance;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Endpoint;
import core.EndpointResult;
import core.FileManager;
import core.Task;


public class PTask extends Task<PResult>{
	
	private static final Logger log = LoggerFactory.getLogger(PTask.class);
	
    String query;
    PrintStream out;

    Exception query_exc;
	private SpecificPTask[] _runs;
	

    public PTask(Endpoint ep, File logDir,SpecificPTask ... runs) {
		super(ep);
		_runs = runs;
    }

    @Override
	public PResult process(EndpointResult epr) {
    	PResult res = new PResult();
		res.setEndpointResult(epr);
    	
		
		Map<CharSequence, PSingleResult> results = new HashMap<CharSequence, PSingleResult>(_runs.length);
		for(SpecificPTask sp: _runs){
			log.debug("Running {} for {}",sp.name(), epr.getEndpoint());
			PRun run = sp.get(epr.getEndpoint());
			
			PSingleResult pres = run.execute();
//			run.close();
			results.put(sp.name(), pres);
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		res.setResults(results);
    return res;
    }

	

    
	

	
}
