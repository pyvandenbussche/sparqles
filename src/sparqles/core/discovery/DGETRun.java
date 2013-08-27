package sparqles.core.discovery;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.any23.extractor.html.TurtleHTMLExtractor;
import org.apache.any23.extractor.rdf.NQuadsExtractor;
import org.apache.any23.extractor.rdf.NTriplesExtractor;
import org.apache.any23.extractor.rdf.RDFXMLExtractor;
import org.apache.any23.extractor.rdf.TurtleExtractor;
import org.apache.any23.http.AcceptHeaderBuilder;
import org.apache.any23.mime.MIMEType;
import org.apache.any23.writer.TripleHandler;
import org.apache.avro.util.Utf8;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.stats.Count;
import org.semanticweb.yars.util.CallbackSet;

import sparqles.utils.DateFormater;

import com.ontologycentral.ldspider.hooks.content.ContentHandler;
import com.ontologycentral.ldspider.http.Headers;
import com.ontologycentral.ldspider.http.Headers.Treatment;

import sparqles.core.Endpoint;





public class DGETRun extends DRun<GetResult>{
	private final static String sparqDescNS = "http://www.w3.org/ns/sparql-service-description#";
	private final static String voidNS = "http://rdfs.org/ns/void#";
	public static final String header = "application/rdf+xml, text/rdf, text/rdf+xml, application/rdf";
	 private final Resource server = new Resource("http://www.w3.org/2006/http#server");
	 private final Resource mime = new Resource("http://www.w3.org/2006/http#content-type");
	final static String httpNS = "http://www.w3.org/2006/http#";
	 private final Resource link = new Resource(httpNS+"link");
	 private final Resource respCode = new Resource(httpNS+"responseCode");
	
	public DGETRun(Endpoint endpoint) {
		super(endpoint);
	}

	public String[] getDefaultExtractorNames() {
		String[] extractorNames = { 
//				RDFaExtractor.factory.getExtractorName(),
				RDFXMLExtractor.factory.getExtractorName(),
				TurtleExtractor.factory.getExtractorName(),
				NTriplesExtractor.factory.getExtractorName(),
				NQuadsExtractor.factory.getExtractorName(),
				TurtleHTMLExtractor.NAME };
		return extractorNames;
	}
	
	@Override
	public GetResult execute() {
		GetResult res = new GetResult();
		
		
		CallbackSet cbsetC= new CallbackSet();
		CallbackSet cbsetH= new CallbackSet();
		TripleHandler headerTripleHandler = new CallbackNQuadTripleHandler(cbsetH);			
		ContentHandler ch = new ContentHandlerAny23(headerTripleHandler, Treatment.INCLUDE, getDefaultExtractorNames());
		
		//perform an HTTP Get and parse the response
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(_ep.getUri().toString());
		request.addHeader("accept", getHeaderString(ch));

		String date = DateFormater.getDataAsString(DateFormater.YYYYMMDDHHMM);

		HttpResponse response;
		try {
			response = client.execute(request);
			String type = getType(response);
			
			String status = ""+response.getStatusLine().getStatusCode();
		
			// 1) CHeck the header for information
			parseHeaders(res, _ep.uri, response);

			// 2) Check the content
			if((status.startsWith("2")||status.startsWith("3"))&& ch.canHandle(type)){
				ch.handle(new URI(_ep.getUri().toString()), type, response.getEntity().getContent(), cbsetC);
			}
			Count<Node> voidPred = new Count<Node>();
			Count<Node> spdsPred = new Count<Node>();
			for(Node[] n: cbsetC.getSet()){
				Node p = n[1];
				if(p.toString().startsWith(voidNS)){
					voidPred.add(p);
				}else if(p.toString().startsWith(sparqDescNS)){
					spdsPred.add(p);
				}
			}
			res.setVOIDterms(voidPred.getTotal());
			res.setSPARQLDESCterms(spdsPred.getTotal());
			res.setSPARQLDESCpreds(convertMap(spdsPred));
			res.setVoiDpreds(convertMap(voidPred));
		} catch (Exception e) {
			e.printStackTrace();
			res.setException(e.getMessage());
		}
		finally{
			request.abort();
		}
		return res;
	}

	private Map<CharSequence, Object> convertMap(Count<Node> c) {
		Map<CharSequence, Object> map = new HashMap<CharSequence, Object>();
		for(Entry<Node, Integer> ent: c.entrySet() ){
			map.put(ent.getKey().toString(), ent.getValue().longValue());
		}
		return map;
	}

	private void parseHeaders(GetResult res, CharSequence uri, HttpResponse response) {
		CallbackSet cbsetH= new CallbackSet();
		res.setResponseLink("missing");
		try {
			Headers.processHeaders(new URI(uri.toString()), response.getStatusLine().getStatusCode(), response.getAllHeaders(), cbsetH);
			for(Node[] n: cbsetH.getSet()){
				if(n[1].equals(server)){
					res.setResponseServer(new Utf8(n[2].toString()));
				}
				if(n[1].equals(mime)){
					res.setResponseType(new Utf8(n[2].toString()));
				}
				if(n[1].equals(link)){
					res.setResponseLink(new Utf8(n[2].toString()));
				}
				if(n[1].equals(respCode)){
					res.setResponseCode(new Utf8(n[2].toString()));
				}
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	private String getHeaderString(ContentHandler ch) {
		LinkedList<MIMEType> mimetypes = new LinkedList<MIMEType>();
		for (String s : ch.getMimeTypes())
			try {
				mimetypes.add(MIMEType.parse(s));
			} catch (IllegalArgumentException e) {
				continue;
			}
		return new AcceptHeaderBuilder(mimetypes).getAcceptHeader();
		
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