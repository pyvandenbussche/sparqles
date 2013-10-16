package sparqles.core.discovery;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.avro.util.Utf8;
import org.apache.http.Header;
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

import sparqles.core.Endpoint;
import sparqles.utils.DateFormater;

import com.hp.hpl.jena.graph.Triple;





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
		request.addHeader("accept", "application/rdf+xml, application/x-turtle, application/rdf+n3, application/xml, text/turtle, text/rdf, text/plain;q=0.1");

		HttpResponse resp;
		try {
			resp = client.execute(request);
			String type = getType(resp);

			String status = ""+resp.getStatusLine().getStatusCode();
			res.setResponseCode(status);
			Header [] header = resp.getAllHeaders();
			// 1) CHeck the header for information

			parseHeaders(res, _ep, header);
			HashMap<CharSequence, Object> voidPred = new HashMap<CharSequence, Object>();
			HashMap<CharSequence, Object> spdsPred = new HashMap<CharSequence, Object>();
			if(status.startsWith("2")){
				String content = EntityUtils.toString(resp.getEntity());	

				PipedRDFIterator<Triple> iter = new PipedRDFIterator<Triple>();
				final PipedRDFStream<Triple> inputStream = new PipedTriplesStream(iter);
				ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes());
				RDFDataMgr.parse(inputStream, bais,_ep.getUri().toString(), getLangFromType(type));


				


				while(iter.hasNext()){
					Triple t = iter.next();
					String pred  = t.getPredicate().toString();
					System.out.println(t);
					if(pred.startsWith(sparqDescNS)){
						update(pred.replace(sparqDescNS, ""), spdsPred );
					}else if(pred.startsWith(voidNS)){
						update( pred.replace(voidNS, ""), voidPred );
					}
				}


				

			}  
			res.setVOIDterms( voidPred.size());
			res.setSPARQLDESCterms( spdsPred.size());
			res.setSPARQLDESCpreds(spdsPred);
			res.setVoiDpreds(voidPred);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return res;
	}


	private void update(CharSequence key, Map<CharSequence, Object> map){
		if(map.containsKey(key))
			map.put(key, ((Integer)map.get(key))+1);
		else
			map.put(key,1);	
	}

	private Lang getLangFromType(String type) {
		if(type.contains("application/x-turtle")||type.contains("text/turtle"))
			return Lang.TTL;
		if(type.contains("application/rdf+xml")||type.contains("application/xml"))
			return Lang.RDFXML;
		if(type.contains("text/plain"))
			return Lang.NTRIPLES;
		if(type.contains("text/rdf+n3"))
			return Lang.N3;

		return Lang.RDFXML;
	}



	private void parseHeaders(GetResult res, Endpoint _ep, Header[] header) {
		//		CallbackSet cbsetH= new CallbackSet();
		res.setResponseLink("missing");

		for (int i = 0; i < header.length; i++) {
			String name = header[i].getName();
			if(name.equals("Content-Type")){
				res.setResponseType(new Utf8(header[i].getValue()));
			}
			if(name.equals("Server")){
				res.setResponseServer(parseServer(header[i].getValue()));
			}
			if(name.equals("Link")){
				res.setResponseLink(parseServer(header[i].getValue()));
			}

			//			System.out.println(header[i]);
		}





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

	private CharSequence parseServer(String value) {
		String server= value.trim();
		if(server.contains("/"))
			server = server.substring(0,server.indexOf("/"));

		return new Utf8(server);
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