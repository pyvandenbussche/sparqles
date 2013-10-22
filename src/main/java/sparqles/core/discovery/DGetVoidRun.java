package sparqles.core.discovery;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.utils.LogFormater;
import sparqles.utils.QueryManager;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.RDFNode;

import sparqles.core.Endpoint;
import sparqles.core.EndpointFactory;

public class DGetVoidRun  extends DRun<VoidResult>{
	private static final Logger log = LoggerFactory.getLogger(DGetVoidRun.class);
	private boolean _self;
	private final static String voidStore="http://void.rkbexplorer.com/sparql/";
	private static Endpoint VOIDSTORE=null;

	
	public DGetVoidRun(Endpoint ep, boolean self) {
		super(ep);
		_self = self;
		if(VOIDSTORE==null)
			try {
				VOIDSTORE = EndpointFactory.newEndpoint(new URI(voidStore));
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	private final static String query = "" +
			"PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
			"PREFIX void:     <http://rdfs.org/ns/void#>\n"+
			"SELECT DISTINCT * \n"+
			"WHERE {\n"+ 
			"?ds a void:Dataset .\n"+ 
			"?ds void:sparqlEndpoint %%s .\n"+
			"?ds ?p ?o .\n"+
			"}";

	public VoidResult execute() {

		VoidResult res = new VoidResult();
		String queryString = query.replaceAll("%%s", "<"+_ep.getUri()+">");
		
		ArrayList<CharSequence> voidA = new ArrayList<CharSequence>();
		res.setVoidFile(voidA);
		
		// initializing queryExecution factory with remote service.
		// **this actually was the main problem I couldn't figure out.**
		QueryExecution qexec = null;
		try {
			if(_self)
				qexec = QueryManager.getExecution(_ep, queryString);
			else
				qexec = QueryManager.getExecution(VOIDSTORE, queryString);


			boolean results = false;
			//after it goes standard query execution and result processing which can
			// be found in almost any Jena/SPARQL tutorial.

			ResultSet resSet = qexec.execSelect();
			ResultSetRewindable reswind = ResultSetFactory.makeRewindable(resSet);
			
			while(reswind.hasNext()){
				RDFNode dataset = reswind.next().get("ds");
				voidA.add(dataset.toString());
			}
			log.info("Found {} results",reswind.getRowNumber());
		} catch (Exception e1) {
			res.setException(LogFormater.toString(e1));
		}
		finally {
			if(qexec!=null)qexec.close();
		}
		return res;
	}
}
