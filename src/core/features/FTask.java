package core.features;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.net.SyslogAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.ENDSProperties;
import core.Endpoint;
import core.EndpointResult;
import core.Task;


public class FTask extends Task<FResult>{
	
	private static final Logger log = LoggerFactory.getLogger(FTask.class);
	
    String query;
    PrintStream out;

    Exception query_exc;
	private SpecificFTask[] _tasks;
	

    public FTask(Endpoint ep, SpecificFTask ... tasks) {
		super(ep);
		_tasks = tasks;
		Object [] s = {ep.getUri().toString(), tasks.length, ENDSProperties.FTASK_WAITTIME};
		log.debug("Init for {} with {} tasks and a waittime of {} ms", s);
    }

    @Override
	public FResult process(EndpointResult epr) {
    	FResult res = new FResult();
		res.setEndpointResult(epr);
    	
		log.debug("[RUN] {}", epr.getEndpoint().getUri().toString());
		Map<CharSequence, FSingleResult> results = new HashMap<CharSequence, FSingleResult>(_tasks.length);
		
		int failures=0;
		for(SpecificFTask sp: _tasks){
			System.out.println(sp.name());
			log.debug("[RUN] {} [{}]", epr.getEndpoint(), sp.name());
			FRun run = sp.get(epr);
			
			FSingleResult pres = run.execute();

			results.put(sp.name(), pres);
			
			if(pres.getRun().getException()!=null){
				failures++;
			}
			try {
				Thread.sleep(ENDSProperties.FTASK_WAITTIME);
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
