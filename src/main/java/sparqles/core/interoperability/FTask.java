package sparqles.core.interoperability;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;

import sparqles.avro.Endpoint;
import sparqles.avro.EndpointResult;
import sparqles.avro.features.FResult;
import sparqles.avro.features.FSingleResult;
import sparqles.avro.performance.PSingleResult;
import sparqles.avro.performance.Run;
import sparqles.core.EndpointTask;
import sparqles.core.SPARQLESProperties;
import sparqles.core.performance.PRun;


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
		log.info("INIT {} with {} tasks and {} ms wait time", this, tasks.length, SPARQLESProperties.getPTASK_WAITTIME());
    }

    @Override
	public FResult process(EndpointResult epr) {
    	FResult res = new FResult();
		res.setEndpointResult(epr);
		
		Map<CharSequence, FSingleResult> results = new HashMap<CharSequence, FSingleResult>(_tasks.length);
		
		int failures=0, consequExcept=0;
		for(SpecificFTask sp: _tasks){
			log.debug("execute {}:{}", this, sp.name());
			
			FRun run = sp.get(epr);
			FSingleResult fres = null;
			if(consequExcept >= _tasks.length){
				log.debug("skipping {}:{} due to {} consecutive ex ", this, sp.name());
				fres = new FSingleResult();
				
				Run r = new Run(PRun.A_FIRST_RESULT_TIMEOUT, -1, 0L, 0L, 0L, (CharSequence)("Test Aborted due to "+consequExcept+" consecutive exceptions"), PRun.EXECUTION_TIMEOUT);
				fres.setRun(r);
				fres.setQuery(run.getQuery());
			}else{
				log.debug("executing {}:{}", this, sp.name());
				fres = run.execute();
			}
			
			

			results.put(sp.name(), fres);
			
			if(fres.getRun().getException()!=null){
				failures++;
				
				String exec = fres.getRun().getException().toString();
				if(exec.contains("QueryExceptionHTTP")){
					consequExcept++;
				}else{
					consequExcept=0;
				}
				
				log.debug("failed {} exec: {}", this, exec);
			}
			try {
				Thread.sleep(SPARQLESProperties.getFTASK_WAITTIME());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		res.setResults(results);
		log.info("executed {} {}/{} tasks without error", this, _tasks.length-failures, _tasks.length);
		
		return res;
    }
}
