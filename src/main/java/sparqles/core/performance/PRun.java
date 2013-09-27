package sparqles.core.performance;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import sparqles.utils.LogHandler;
import sparqles.utils.QueryManager;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.util.FmtUtils;

import sparqles.core.ENDSProperties;
import sparqles.core.Endpoint;

public class PRun {

	
	private static final Logger log = LoggerFactory.getLogger(PRun.class);
	
	 private static final long FIRST_RESULT_TIMEOUT = 60 * 1000;
	 private static final long EXECUTION_TIMEOUT = 15 * 60 * 1000;

	private String _query;
	private String _queryFile;
	private Endpoint _ep;
		
	public PRun(Endpoint ep, Object object, String queryFile) {
		_queryFile = queryFile;
		
		_query = QueryManager.getQuery(ENDSProperties.getPTASK_QUERIES(),queryFile);
		_ep = ep;
	}


//	private String getQuery(String qFile) {
//		String content;
//		try {
//			content = new Scanner(new File("WebContent/WEB-INF/resources/ptask/"+qFile)).useDelimiter("\\Z").next();
//			log.debug("Parsed from {} query {}", qFile, content);
//			return content;
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//		return "";
//		
//	}


	public PSingleResult execute() {
		PSingleResult result = new PSingleResult();
		
		result.setQuery(_query);
    	
		 // we need to run a test alwasy two times
		LogHandler.run(log, " [COLD] {} over {}", _ep.getUri(),_queryFile);
        result.setCold(run());
        
        try {
			Thread.sleep(ENDSProperties.getPTASK_WAITTIME());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        LogHandler.run(log, " [WARM] {} over {}", _ep.getUri(),_queryFile);
        result.setWarm(run());
		
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
            
            qexec.setTimeout(FIRST_RESULT_TIMEOUT, EXECUTION_TIMEOUT);
            cnxion = System.currentTimeMillis();

            if (q.isSelectType())
            {
                ResultSet results = qexec.execSelect();
                while (results.hasNext())
                {
                    QuerySolution qs = results.nextSolution();
//                    out.println(toString(qs, sols == 0));
                    sols++;
                }
            }
            else if (q.isAskType())
            {
                boolean result = qexec.execAsk();
//                out.println(result);
                if (result)
                    sols = 1;
                else
                    sols = -1;
            }
            else if (q.isDescribeType())
            {
                Iterator<Triple> triples = qexec.execDescribeTriples();
                while (triples.hasNext())
                {
//                    out.println(triples.next());
                    sols++;
                }
            }
            else if (q.isConstructType())
            {
                Iterator<Triple> triples = qexec.execConstructTriples();
                while (triples.hasNext())
                {
//                    out.println(triples.next());
                    sols++;
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
        	LogHandler.warn(log, "", e);
//        	log.error("[RUN] {}: {}", _ep.getUri().toString(), e.getClass().getSimpleName()+" "+e.getMessage());
        	r.setException(LogHandler.toString(e));
//        	res.recordError(e);
//            query_exc = e;
            System.out.println(_ep.getUri() + "\t" + sols
                    + "\t" + (cnxion - b4) + "\t"
                    + (System.currentTimeMillis() - b4) + "\tException\t"
                    + Utils.removeNewlines(e.getMessage()));
            
//            return false;
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

//    /**
//     * Close the output file
//     * 
//     * @throws IOException
//     */
//    public void close() throws IOException
//    {
//        out.close();
//    }
//
//    public Exception getQueryException()
//    {
//        return query_exc;
//    }

}
