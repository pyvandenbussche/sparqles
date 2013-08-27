package core.features;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import utils.LogHandler;
import utils.QueryManager;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.util.FmtUtils;

import core.ENDSProperties;
import core.Endpoint;
import core.FileManager;
import core.performance.Run;

public class FRun {

	
	private static final Logger log = LoggerFactory.getLogger(FRun.class);
	
	 private static final long FIRST_RESULT_TIMEOUT = 60 * 1000;
	 private static final long EXECUTION_TIMEOUT = 15 * 60 * 1000;

	private String _query;
	private String _queryFile;
	private Endpoint _ep;

	private Long _start;

	private FileManager _fm;
		
	public FRun(Endpoint ep, String queryFile ) {
		this(ep, queryFile, System.currentTimeMillis());
	}


	public FRun(Endpoint ep, String queryFile, Long start) {
		_queryFile = queryFile;
		
		_query = QueryManager.getQuery(ENDSProperties.getFTASK_QUERIES(),queryFile);
		_ep = ep;
		_start =start;
	}

	public FSingleResult execute() {
		FSingleResult result = new FSingleResult();
		
		result.setQuery(_query);
    	
		 // we need to run a test alwasy two times
		LogHandler.run(log, "{} over {}",_queryFile, _ep.getUri());
		result.setRun(run());
        
        return result;
	}

	private Run run() {
		
		
		Run r = new Run();
		r.setFrestout(FIRST_RESULT_TIMEOUT);
        r.setExectout(EXECUTION_TIMEOUT);
        
        
	    long b4 = 0;
        long cnxion = 0;
        int sols = 0;
        try
        {
            b4 = System.currentTimeMillis();

            Query q = QueryFactory.create(this._query);
            QueryExecution qexec = QueryManager.getExecution(_ep, _query);
            
            qexec.setTimeout(FIRST_RESULT_TIMEOUT, FIRST_RESULT_TIMEOUT);
            cnxion = System.currentTimeMillis();
            LogHandler.run(log, " {} over {}", _ep.getUri(),_queryFile);
            if (q.isSelectType())
            {
            	
            	ResultSet results = qexec.execSelect();
                if(_fm!=null)
                	sols = _fm.writeSPARQLResults(results, _queryFile, _ep, _start);
                else{
                	sols = skipSPARQLResults(results, _queryFile, _ep, _start);
                }
                
            }
            else if (q.isAskType())
            {
            	boolean result = qexec.execAsk();
                if (result)
                    sols = 1;
                else
                    sols = -1;
            }
            else if (q.isDescribeType())
            {
                Iterator<Triple> triples = qexec.execDescribeTriples();
                
                if(_fm!=null)
                	sols = _fm.writeSPARQLResults(triples, _queryFile, _ep, _start);
                else{
                	sols = skipSPARQLResults(triples, _queryFile, _ep, _start);
                }

            }
            else if (q.isConstructType())
            {
            	Iterator<Triple> triples = qexec.execConstructTriples();
                if(_fm!=null)
                	sols = _fm.writeSPARQLResults(triples, _queryFile, _ep, _start);
                else{
                	sols = skipSPARQLResults(triples, _queryFile, _ep, _start);
                }
            }
            else
            {
                throw new UnsupportedOperationException(
                        "What query is this? (Not SELECT|ASK|DESCRIBE|CONSTRUCT). "
                                + q);
            }

            long iter = System.currentTimeMillis();
            qexec.close();
            long close = System.currentTimeMillis();

            r.setSolutions(sols);
            r.setInittime((cnxion - b4));
            r.setExectime((iter - b4));
            r.setClosetime((close - b4));
            
            System.out.println( _ep.getUri() + "\t" + sols
                    + "\t" + (cnxion - b4) + "\t" + (iter - b4) + "\t"
                    + (close - b4));
        }
        catch (Exception e)
        {
//        	res.recordError(e);
//            query_exc = e;
            System.out.println(_ep.getUri() + "\t" + sols
                    + "\t" + (cnxion - b4) + "\t"
                    + (System.currentTimeMillis() - b4) + "\tException\t"
                    + Utils.removeNewlines(e.getMessage()));
            
//            return false;
            LogHandler.warn(log, "", e);
      
            r.setException(LogHandler.toString(e));
        }
        
        return r;
	}

	private static String toString(QuerySolution qs, boolean first)
    {
        StringBuffer vars = new StringBuffer();
        StringBuffer sb = new StringBuffer();
        Iterator<String> varns = qs.varNames();
        while (varns.hasNext())
        {
            String varName = varns.next();
            if (first)
            {
                vars.append(varName + "\t");
            }
            sb.append(FmtUtils.stringForObject(qs.get(varName)) + "\t");
        }

        if (first)
            return vars.toString() + "\n" + sb.toString();
        return sb.toString();
    }


	public void setFileManager(FileManager fm) {
		_fm = fm;
		
	}
	
	public int skipSPARQLResults(Iterator<Triple> triples,
			String queryFile, Endpoint ep, Long start) {
		
			int sols=0;
			while (triples.hasNext())
	        {
				triples.next();
	            sols++;
	        }
			return sols;
	}
	public int skipSPARQLResults(ResultSet results, String queryFile,
			Endpoint ep, Long start) {
		
			int sols=0;
			while (results.hasNext())
	        {
	            QuerySolution qs = results.nextSolution();
	            toString(qs, sols == 0);
	            sols++;
	        }
			
			return sols;
	}
}
