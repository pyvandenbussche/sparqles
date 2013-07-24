package core.discovery;




import java.io.File;
import java.util.LinkedList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.any23.extractor.html.TurtleHTMLExtractor;
import org.apache.any23.extractor.rdf.NQuadsExtractor;
import org.apache.any23.extractor.rdf.NTriplesExtractor;
import org.apache.any23.extractor.rdf.RDFXMLExtractor;
import org.apache.any23.extractor.rdf.TurtleExtractor;
import org.apache.any23.extractor.rdfa.RDFaExtractor;
import org.apache.any23.http.AcceptHeaderBuilder;
import org.apache.any23.mime.MIMEType;
import org.apache.any23.writer.TripleHandler;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.util.CallbackSet;

import utils.DateFormater;

import com.ontologycentral.ldspider.hooks.content.ContentHandler;
import com.ontologycentral.ldspider.http.Headers;
import com.ontologycentral.ldspider.http.Headers.Treatment;

import core.Endpoint;
import core.Task;

/**
 * DTaskGET: This task inspects the header and content after a HTTP GET on the endpoint URI. 
 * 
 * We perform the following checks:
 * 1) if the header contains any information about meta data for this endpoint
 * 2) if the endpoint returns any RDF content 
 * 3) if 2 succeeds, then check for vocabulary terms from either voiD or SPARQL1.1
 * @author UmbrichJ
 *
 */
public class DTaskGET extends Task<DResultGET> {


	/** We should use more content types here **/
	public static String header = "application/rdf+xml, text/rdf, text/rdf+xml, application/rdf";
	private File _logDir;

	
	public DTaskGET(Endpoint ep, File logDir) {
		super(ep,DResultGET.class);
		_logDir = logDir;
	}

	public static String[] getDefaultExtractorNames() {
		String[] extractorNames = { 
//				RDFaExtractor.factory.getExtractorName(),
				RDFXMLExtractor.factory.getExtractorName(),
				TurtleExtractor.factory.getExtractorName(),
				NTriplesExtractor.factory.getExtractorName(),
				NQuadsExtractor.factory.getExtractorName(),
				TurtleHTMLExtractor.NAME };
		return extractorNames;
	}
	/**
	 * 
	 */
	@Override
	protected DResultGET process(DResultGET res) {
		
		
		
		CallbackSet cbsetH= new CallbackSet();
		CallbackSet cbsetC= new CallbackSet();
		TripleHandler headerTripleHandler = new CallbackNQuadTripleHandler(cbsetH);
		ContentHandler ch = new ContentHandlerAny23(headerTripleHandler, Treatment.DUMP, getDefaultExtractorNames());
				
		LinkedList<MIMEType> mimetypes = new LinkedList<MIMEType>();
		for (String s : ch.getMimeTypes())
			try {
				mimetypes.add(MIMEType.parse(s));
			} catch (IllegalArgumentException e) {
				continue;
			}
		header = new AcceptHeaderBuilder(mimetypes).getAcceptHeader();
		System.out.println(header);
	
		//perform an HTTP Get and parse the response
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(_ep.getUri());
		request.addHeader("accept", header);

		String date = DateFormater.getDataAsString(DateFormater.YYYYMMDDHHMM);
		
		HttpResponse response;
		try {
			response = client.execute(request);
			String type = getType(response);
			String status = ""+response.getStatusLine().getStatusCode();
			res.responseType(type);	
			res.responseCode(status);
			
			// 1) CHeck the header for information
			Headers.processHeaders(_ep.getEndpointURI(), response.getStatusLine().getStatusCode(), response.getAllHeaders(), cbsetH);
			for(Node[] n: cbsetH.getSet()){
				System.out.println(Nodes.toN3(n));
			}

			// 2) Check the content
			if((status.startsWith("2")||status.startsWith("3"))&& ch.canHandle(type)){
				ch.handle(_ep.getEndpointURI(), type, response.getEntity().getContent(), cbsetC);
			}
			
			for(Node[] n: cbsetC.getSet()){
				Node p = n[1];
				res.handlePredicate(p);
			}
		} catch (Exception e) {
			e.printStackTrace();
			res.recordError(e);
		}
		finally{
			request.abort();
		}
		return res;
	}

	private String getType(HttpResponse response) {
		String type ="";
		org.apache.http.Header ct = response.getFirstHeader("Content-Type");
        if (ct != null) {
                type = response.getFirstHeader("Content-Type").getValue();
        }
		return type;
	}


}