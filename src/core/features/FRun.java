package core.features;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.QueryManager;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.util.FmtUtils;


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
		
	public FRun(Endpoint ep, String queryFile ) {
		this(ep, queryFile, System.currentTimeMillis());
	}


	public FRun(Endpoint ep, String queryFile, Long start) {
		_queryFile = queryFile;
		
		_query = getQuery(_queryFile);
		_ep = ep;
		_start =start;
	}


	private String getQuery(String qFile) {
		String content;
		try {
			content = new Scanner(new File("WebContent/WEB-INF/resources/ftask/"+qFile)).useDelimiter("\\Z").next();
			log.debug("Parsed from {} query {}", qFile, content);
			return content;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return "";
		
	}


	public FSingleResult execute() {
		FSingleResult result = new FSingleResult();
		
		result.setQuery(_query);
    	
		 // we need to run a test alwasy two times
		log.info("Execute {} over {}",_queryFile, _ep.getUri());
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

            if (q.isSelectType())
            {
            	log.debug("Executing {} against {}", _queryFile, _ep.getUri());
                ResultSet results = qexec.execSelect();
                
                sols = FileManager.getInstance().writeSPARQLResults(results, _queryFile, _ep, _start);
                
            }
            else if (q.isAskType())
            {
            	log.debug("Executing {} against {}", _queryFile, _ep.getUri());
                boolean result = qexec.execAsk();
//                out.println(result);
                if (result)
                    sols = 1;
                else
                    sols = -1;
            }
            else if (q.isDescribeType())
            {
            	log.debug("Executing {} against {}", _queryFile, _ep.getUri());
                Iterator<Triple> triples = qexec.execDescribeTriples();
                
                sols = FileManager.getInstance().writeSPARQLResults(triples, _queryFile, _ep, _start);
//                while (triples.hasNext())
//                {
////                    out.println(triples.next());
//                    sols++;
//                }
            }
            else if (q.isConstructType())
            {
            	log.debug("Executing {} against {}", _queryFile, _ep.getUri());
                Iterator<Triple> triples = qexec.execConstructTriples();
                sols = FileManager.getInstance().writeSPARQLResults(triples, _queryFile, _ep, _start);
                
//                while (triples.hasNext())
//                {
//                	log.debug(arg0)
//                    out.println(triples.next());
//                    sols++;
//                }
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
            
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            
            r.setException(sw.toString());
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
