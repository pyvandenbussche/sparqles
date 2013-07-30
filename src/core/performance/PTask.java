package core.performance;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.ENDSProperties;
import core.Endpoint;
import core.EndpointResult;
import core.Task;


public class PTask extends Task<PResult>{
	
	private static final Logger log = LoggerFactory.getLogger(PTask.class);
	
    String query;
    PrintStream out;

    Exception query_exc;
	private SpecificPTask[] _tasks;
	

    public PTask(Endpoint ep, SpecificPTask ... tasks) {
		super(ep);
		_tasks = tasks;
		Object [] s = {ep.getUri().toString(), tasks.length, ENDSProperties.PTASK_WAITTIME};
		log.debug("Init for {} with {} tasks and a waittime of {} ms", s);
    }

    @Override
	public PResult process(EndpointResult epr) {
    	PResult res = new PResult();
		res.setEndpointResult(epr);
    	
		log.debug("[RUN] {}", epr.getEndpoint().getUri().toString());
		Map<CharSequence, PSingleResult> results = new HashMap<CharSequence, PSingleResult>(_tasks.length);
		
		int failures=0;
		for(SpecificPTask sp: _tasks){
			log.debug("[RUN] {} [{}]", epr.getEndpoint(), sp.name());
			PRun run = sp.get(epr.getEndpoint());
			
			PSingleResult pres = run.execute();

			results.put(sp.name(), pres);
			
			if(pres.getCold().getException()!=null ||pres.getWarm().getException()!=null){
				failures++;
			}
			try {
				Thread.sleep(ENDSProperties.PTASK_WAITTIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		res.setResults(results);
		if(failures==0)
			log.info("[SUCCESS] [SELECT] {}", epr.getEndpoint());
		else{
			Object [] s = { epr.getEndpoint().getUri().toString(), failures, _tasks.length}; 
			log.error("[RUN] {}: {}/{} failures",s);
		}
		return res;
    }
}
