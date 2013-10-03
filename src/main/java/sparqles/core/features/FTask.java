package sparqles.core.features;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.utils.LogHandler;

import sparqles.core.SPARQLESProperties;
import sparqles.core.Endpoint;
import sparqles.core.EndpointResult;
import sparqles.core.EndpointTask;


public class FTask extends EndpointTask<FResult>{
	
	private static final Logger log = LoggerFactory.getLogger(FTask.class);
	
    String query;
    PrintStream out;

    Exception query_exc;
	private SpecificFTask[] _tasks;
	

    public FTask(Endpoint ep, SpecificFTask ... tasks) {
		super(ep);
		_tasks = tasks;
		Object [] s = {ep.getUri().toString(), tasks.length, SPARQLESProperties.getFTASK_WAITTIME()};
		LogHandler.init(log,"{} with {} tasks and a waittime of {} ms", s);
		
    }

    @Override
	public FResult process(EndpointResult epr) {
    	FResult res = new FResult();
		res.setEndpointResult(epr);
		LogHandler.run(log, "{}",epr.getEndpoint().getUri().toString());
		
		Map<CharSequence, FSingleResult> results = new HashMap<CharSequence, FSingleResult>(_tasks.length);
		
		int failures=0;
		for(SpecificFTask sp: _tasks){
			System.out.println(sp.name());
			LogHandler.run(log, "{} [{}]",epr.getEndpoint().getUri().toString(), sp.name());
			
			FRun run = sp.get(epr);
			run.setFileManager(_fm);
			FSingleResult pres = run.execute();

			results.put(sp.name(), pres);
			
			if(pres.getRun().getException()!=null){
				failures++;
			}
			try {
				Thread.sleep(SPARQLESProperties.getFTASK_WAITTIME());
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
