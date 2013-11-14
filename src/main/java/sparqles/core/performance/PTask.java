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
import sparqles.avro.performance.Run;
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
		
		log.info("INIT {} with {} tasks and {} ms wait time", this, tasks.length, SPARQLESProperties.getPTASK_WAITTIME());
    }

    @Override
	public PResult process(EndpointResult epr) {
    	PResult res = new PResult();
		res.setEndpointResult(epr);
		
    	Map<CharSequence, PSingleResult> results = new HashMap<CharSequence, PSingleResult>(_tasks.length);
    	
    	int consequExcept =0;
    	int failures=0;
		for(SpecificPTask sp: _tasks){
			PRun run = sp.get(epr.getEndpoint());
			
			PSingleResult pres=null;
			if(consequExcept >= _tasks.length){
				log.debug("skipping {}:{} due to {} consecutive ex ", this, sp.name());
				pres = new PSingleResult();
				
				Run r = new Run(PRun.A_FIRST_RESULT_TIMEOUT, -1, 0L, 0L, 0L, (CharSequence)("Test Aborted due to "+consequExcept+" consecutive exceptions"), PRun.EXECUTION_TIMEOUT);
				pres.setCold(r);
				pres.setWarm(r);
				pres.setQuery(run.getQuery());
			}else{
				log.debug("executing {}:{}", this, sp.name());
				pres = run.execute();
			}
			results.put(sp.name(), pres);
			
			if(pres.getCold().getException()!=null || pres.getWarm().getException()!=null){
				failures++;
				String cold = "", warm ="";
				if(pres.getCold().getException()!=null){
					cold = pres.getCold().getException().toString();
					consequExcept++;	
				}else consequExcept=0;
				if(pres.getWarm().getException()!=null){
					warm = pres.getWarm().getException().toString();
					consequExcept++;
				}else consequExcept=0;
				
				log.debug("failed {} (cold: {}, warm: {})", this, cold, warm); 
			}
			try {
				Thread.sleep(SPARQLESProperties.getPTASK_WAITTIME());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		res.setResults(results);
		
		log.info("executed {} {}/{} tasks without error", this, _tasks.length-failures, _tasks.length);
		return res;
    }
}