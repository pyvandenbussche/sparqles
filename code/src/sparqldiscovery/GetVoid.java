package sparqldiscovery;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.deri.any23.Any23;
import org.deri.any23.extractor.ExtractionException;
import org.deri.any23.extractor.ExtractorFactory;
import org.deri.any23.extractor.ExtractorRegistry;
import org.deri.any23.extractor.html.TurtleHTMLExtractor;
import org.deri.any23.extractor.rdf.*;
import org.deri.any23.filter.IgnoreTitlesOfEmptyDocuments;
import org.deri.any23.http.HTTPClient;
import org.deri.any23.mime.MIMEType;
import org.deri.any23.source.DocumentSource;
import org.deri.any23.source.HTTPDocumentSource;
import org.deri.any23.writer.NQuadsWriter;
import org.deri.any23.writer.TripleHandler;
import org.deri.any23.writer.TripleHandlerException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class GetVoid {

	protected static String benchExperiment="sparqlvoid";
//	private HTTPClient httpClient;
	private Any23 runner;
	private HTTPClient httpClient;
	
	public GetVoid(File logDir) {
		this(logDir,benchExperiment);
	}
	
	public GetVoid(File logDir, final String benchExperiment) {
		String[] extractorNames = {
				RDFXMLExtractor.factory.getExtractorName(),
				TurtleExtractor.factory.getExtractorName(),
				NTriplesExtractor.factory.getExtractorName(),
				NQuadsExtractor.factory.getExtractorName(),
				TurtleHTMLExtractor.NAME,
							};
		runner = new Any23(extractorNames);
		runner.setHTTPUserAgent("uccderifuji");
		try {
			httpClient = runner.getHTTPClient();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private final static String voidStore="http://void.rkbexplorer.com/sparql/";
	private final static String query = "" +
			"PREFIX void:     <http://rdfs.org/ns/void#>\n"+
			"SELECT DISTINCT ?ep ?ds\n"+
			"WHERE {\n"+ 
			"?ds a void:Dataset .\n"+ 
			"?ds void:sparqlEndpoint ?ep .\n"+
			"?ds ?p ?o .\n"+
			"}";
	
	public static void main(String[] args) throws IOException {
		File out = new File("results/"+benchExperiment);
		Query q = QueryFactory.create(query);
		System.out.println(q);
		
		
		QueryExecution qexec = QueryExecutionFactory.sparqlService(voidStore, query);

	    ResultSet res = qexec.execSelect();
	    ResultSetRewindable reswind = ResultSetFactory.makeRewindable(res);
		    
	    FileWriter fw = new FileWriter(new File("results/"+benchExperiment+".sparql.tsv"));
	    while(reswind.hasNext()){
	    	QuerySolution qs =reswind.next();
	    	fw.write(qs.get("?ep")+"\t"+qs.get("?ds")+"\n");
	    }
	    fw.close();
	}
}