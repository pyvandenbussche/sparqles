package core.performance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;



import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.util.FmtUtils;

import core.Endpoint;
import core.Task;
import core.discovery.DResultGET;

public class PTask extends Task<PResult>
{
    private static final long FIRST_RESULT_TIMEOUT = 60 * 1000;
    private static final long EXECUTION_TIMEOUT = 15 * 60 * 1000;

    String testId;
    String query;
    PrintStream out;

    Exception query_exc;

    public PTask(Endpoint ep, File logDir,String query, String testId) {
		super(ep,PResult.class);
//		_logDir = logDir;
	    this.testId = testId;
        this.query = query;
    }

    @Override
	protected PResult process(PResult res) {
    	
    	res.setTestID(testId);
    	res.setQuery(query);
    	res.setFirstResTOut(FIRST_RESULT_TIMEOUT);
        res.setExecTOut(EXECUTION_TIMEOUT);
       
        
	    long b4 = 0;
        long cnxion = 0;
        int sols = 0;
        try
        {
            Query query = QueryFactory.create(this.query);

            
            
            b4 = System.currentTimeMillis();
            
            Query q = QueryFactory.create(query.toString());
            QueryExecution qexec = QueryExecutionFactory.sparqlService(_ep.getEndpointURI().toString(), q);
            qexec.setTimeout(FIRST_RESULT_TIMEOUT, EXECUTION_TIMEOUT);
            cnxion = System.currentTimeMillis();

            if (q.isSelectType())
            {
                ResultSet results = qexec.execSelect();
                while (results.hasNext())
                {
                    QuerySolution qs = results.nextSolution();
                    out.println(toString(qs, sols == 0));
                    sols++;
                }
            }
            else if (q.isAskType())
            {
                boolean result = qexec.execAsk();
                out.println(result);
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
                    out.println(triples.next());
                    sols++;
                }
            }
            else if (q.isConstructType())
            {
                Iterator<Triple> triples = qexec.execConstructTriples();
                while (triples.hasNext())
                {
                    out.println(triples.next());
                    sols++;
                }
            }
            else
            {
                throw new UnsupportedOperationException(
                        "What query is this? (Not SELECT|ASK|DESCRIBE|CONSTRUCT). "
                                + query);
            }

            long iter = System.currentTimeMillis();
            qexec.close();
            long close = System.currentTimeMillis();

            res.setSolutions(sols);
            res.setInitTime((cnxion - b4));
            res.setExecTime((iter - b4));
            res.setCloseTime((close - b4));
            
            System.out.println(this.testId + "\t" + _ep.getEndpointURI() + "\t" + sols
                    + "\t" + (cnxion - b4) + "\t" + (iter - b4) + "\t"
                    + (close - b4));
        }
        catch (Exception e)
        {
        	res.recordError(e);
            query_exc = e;
            System.out.println(this.testId + "\t" + _ep.getEndpointURI() + "\t" + sols
                    + "\t" + (cnxion - b4) + "\t"
                    + (System.currentTimeMillis() - b4) + "\tException\t"
                    + Utils.removeNewlines(e.getMessage()));
        }
        return res;
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

    /**
     * Close the output file
     * 
     * @throws IOException
     */
    public void close() throws IOException
    {
        out.close();
    }

    public Exception getQueryException()
    {
        return query_exc;
    }

	
}
