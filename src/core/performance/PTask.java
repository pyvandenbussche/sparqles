package core.performance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.util.FmtUtils;

import core.DBManager;
import core.Endpoint;
import core.EndpointResult;
import core.Task;
import core.availability.AResult;
import core.discovery.DResultGET;

public class PTask extends Task<PResult>{
	
	private static final Logger log = LoggerFactory.getLogger(PTask.class);
	
    private static final long FIRST_RESULT_TIMEOUT = 60 * 1000;
    private static final long EXECUTION_TIMEOUT = 15 * 60 * 1000;

    String testId;
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
