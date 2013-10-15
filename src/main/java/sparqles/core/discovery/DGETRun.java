package sparqles.core.discovery;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.avro.util.Utf8;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.riot.lang.PipedTriplesStream;

import sparqles.utils.DateFormater;

import com.hp.hpl.jena.graph.Triple;
import sparqles.core.Endpoint;





public class DGETRun extends DRun<GetResult>{
	private final static String sparqDescNS = "http://www.w3.org/ns/sparql-service-description#";
	private final static String voidNS = "http://rdfs.org/ns/void#";
	public static final String header = "application/rdf+xml, text/rdf, text/rdf+xml, application/rdf";
	
	public DGETRun(Endpoint endpoint) {
		super(endpoint);
	}

	
	
	@Override
	public GetResult execute() {
		GetResult res = new GetResult();
		
		
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(_ep.getUri().toString());
		request.addHeader("accept", "application/rdf+xml, application/x-turtle, application/rdf+n3, application/xml");

		HttpResponse resp;
		try {
			resp = client.execute(request);
			String type = getType(resp);
			
			String status = ""+resp.getStatusLine().getStatusCode();
			Header [] header = resp.getAllHeaders();
			// 1) CHeck the header for information
//			parseHeaders(res, _ep, header);
			
			if(status.startsWith("2")){
				String content = EntityUtils.toString(resp.getEntity());	
				
				
				
				PipedRDFIterator<Triple> iter = new PipedRDFIterator<Triple>();
			    final PipedRDFStream<Triple> inputStream = new PipedTriplesStream(iter);
			    ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes());
			    RDFDataMgr.parse(inputStream, bais,_ep.getUri().toString(), Lang.RDFXML );
			    
			    while(iter.hasNext()){
			    	System.out.println(iter.next());
			    }
			    
			    
			    Map<CharSequence, Object> voidPred = new HashMap<CharSequence, Object>();
			    Map<CharSequence, Object> spdsPred = new HashMap<CharSequence, Object>();
				
//			    res.setVOIDterms(voidPred.getTotal());
//				res.setSPARQLDESCterms(spdsPred.getTotal());
//				res.setSPARQLDESCpreds(convertMap(spdsPred));
//				res.setVoiDpreds(convertMap(voidPred));
			}  
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//			for(Node[] n: cbsetC.getSet()){
//				Node p = n[1];
//				if(p.toString().startsWith(voidNS)){
//					voidPred.add(p);
//				}else if(p.toString().startsWith(sparqDescNS)){
//					spdsPred.add(p);
//				}
//			}
			
		
		
		
		
		
		
		
		
		
		
	     
	     
		String date = DateFormater.getDataAsString(DateFormater.YYYYMMDDHHMM);

		
		return res;
	}

	
	private void parseHeaders(GetResult res, CharSequence uri, HttpResponse response) {
//		CallbackSet cbsetH= new CallbackSet();
//		res.setResponseLink("missing");
//		try {
//			Headers.processHeaders(new URI(uri.toString()), response.getStatusLine().getStatusCode(), response.getAllHeaders(), cbsetH);
//			for(Node[] n: cbsetH.getSet()){
//				if(n[1].equals(server)){
//					res.setResponseServer(new Utf8(n[2].toString()));
//				}
//				if(n[1].equals(mime)){
//					res.setResponseType(new Utf8(n[2].toString()));
//				}
//				if(n[1].equals(link)){
//					res.setResponseLink(new Utf8(n[2].toString()));
//				}
//				if(n[1].equals(respCode)){
//					res.setResponseCode(new Utf8(n[2].toString()));
//				}
//			}
//		} catch (URISyntaxException e) {
//			e.printStackTrace();
//		}
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