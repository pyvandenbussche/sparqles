package org.deri.sparql.perf.test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;

import org.deri.sparql.perf.Utils;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.util.FmtUtils;

public class PerformanceTest extends Thread
{
    private static final long FIRST_RESULT_TIMEOUT = 60 * 1000;
    private static final long EXECUTION_TIMEOUT = 15 * 60 * 1000;

    String testId;
    String endpoint;
    String query;
    PrintStream out;

    Exception query_exc;

    public PerformanceTest(String testId, String endpoint, String query,
            String output) throws IOException
    {
        this.testId = testId;
        this.endpoint = endpoint;
        this.query = query;
        this.out = new PrintStream(new FileOutputStream(output));
    }

    /**
     * Call for threaded execution.
     */
    public void run()
    {
        execute();
    }

    /**
     * Call for non-threaded execution.
     */
    public void execute()
    {
        long b4 = 0;
        long cnxion = 0;
        int sols = 0;
        try
        {
            Query query = QueryFactory.create(this.query);

            b4 = System.currentTimeMillis();

            Query q = QueryFactory.create(query.toString());
            QueryExecution qexec = QueryExecutionFactory.sparqlService(
                    endpoint, q);
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

            System.out.println(this.testId + "\t" + this.endpoint + "\t" + sols
                    + "\t" + (cnxion - b4) + "\t" + (iter - b4) + "\t"
                    + (close - b4));
        }
        catch (Exception e)
        {
            query_exc = e;
            System.out.println(this.testId + "\t" + this.endpoint + "\t" + sols
                    + "\t" + (cnxion - b4) + "\t"
                    + (System.currentTimeMillis() - b4) + "\tException\t"
                    + Utils.removeNewlines(e.getMessage()));
        }
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
