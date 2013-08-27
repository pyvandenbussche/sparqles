package sparqles.core.performance;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.utils.LogHandler;

import sparqles.core.ENDSProperties;
import sparqles.core.Endpoint;
import sparqles.core.EndpointResult;
import sparqles.core.Task;


public class PTask extends Task<PResult>{
	
	private static final Logger log = LoggerFactory.getLogger(PTask.class);
	
    String query;
    PrintStream out;

    Exception query_exc;
	private SpecificPTask[] _tasks;
	

    public PTask(Endpoint ep, SpecificPTask ... tasks) {
		super(ep);
		_tasks = tasks;
		Object [] s = {ep.getUri().toString(), tasks.length, ENDSProperties.getPTASK_WAITTIME()};
		LogHandler.init(log,"{} with {} tasks and a waittime of {} ms", s);
    }

    @Override
	public PResult process(EndpointResult epr) {
    	PResult res = new PResult();
		res.setEndpointResult(epr);
    	LogHandler.run(log, "{}",epr.getEndpoint().getUri().toString());
		
    	
    	Map<CharSequence, PSingleResult> results = new HashMap<CharSequence, PSingleResult>(_tasks.length);
		
		int failures=0;
		for(SpecificPTask sp: _tasks){
			LogHandler.run(log, "{} {}",epr.getEndpoint().getUri().toString(),sp.name());
			PRun run = sp.get(epr.getEndpoint());
			
			PSingleResult pres = run.execute();

			results.put(sp.name(), pres);
			
			if(pres.getCold().getException()!=null ||pres.getWarm().getException()!=null){
				failures++;
			}
			try {
				Thread.sleep(ENDSProperties.getPTASK_WAITTIME());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		res.setResults(results);
		if(failures==0)
			LogHandler.debugSuccess(log, "{}", epr.getEndpoint());
		else{
			LogHandler.debugERROR(log, "{}: {}/{}",  epr.getEndpoint().getUri().toString(), failures, _tasks.length);
		}
		return res;
    }
}
