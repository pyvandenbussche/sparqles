package sparqles.core.performance;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.avro.Endpoint;
import sparqles.avro.EndpointResult;
import sparqles.avro.performance.PResult;
import sparqles.avro.performance.PSingleResult;
import sparqles.core.EndpointTask;
import sparqles.core.SPARQLESProperties;

public class PTask extends EndpointTask<PResult>{
	
	private static final Logger log = LoggerFactory.getLogger(PTask.class);
	
    String query;
    PrintStream out;

    Exception query_exc;
	private SpecificPTask[] _tasks;

    public PTask(Endpoint ep, SpecificPTask ... tasks) {
		super(ep);
		_tasks = tasks;
		
		log.info("[INIT] {} with {} tasks and {} ms wait time", this, tasks.length, SPARQLESProperties.getPTASK_WAITTIME());
    }

    @Override
	public PResult process(EndpointResult epr) {
    	PResult res = new PResult();
		res.setEndpointResult(epr);
		
    	Map<CharSequence, PSingleResult> results = new HashMap<CharSequence, PSingleResult>(_tasks.length);
		
		int failures=0;
		for(SpecificPTask sp: _tasks){
			log.debug("[exec] {}:{}", this, sp.name());
			
			PRun run = sp.get(epr.getEndpoint());
			PSingleResult pres = run.execute();

			results.put(sp.name(), pres);
			
			if(pres.getCold().getException()!=null ||pres.getWarm().getException()!=null){
				failures++;
				String cold = pres.getCold().getException().toString();
				String warm = pres.getWarm().getException().toString();
				
				log.debug("[failed] {} (cold: {}, warm: {})", this, cold, warm); 
			}
			try {
				Thread.sleep(SPARQLESProperties.getPTASK_WAITTIME());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		res.setResults(results);
		
		log.info("[executed] {} {}/{} tasks without error", this, _tasks.length-failures, _tasks.length);
		
		return res;
    }
}
