package sparqldiscovery;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.mime.Header;
import org.apache.http.impl.client.DefaultHttpClient;
import org.semanticweb.yars.util.CallbackNxBufferedWriter;

public class GetResponse extends Benchmark {

	
	private final static String benchExperiment="httpget";
	private ContentHandlerAny23 any23;
	private String _any23Header;
	
	
	public GetResponse(File logDir) {
		super(logDir,benchExperiment);
		any23 = new ContentHandlerAny23(ContentHandlerAny23.getDefaultExtractorNames());
		_any23Header = any23.composeAcceptHeader(any23.getMimeTypes());
	}

	
	/**
	 * Perfom a HTTPGet operation (accept: application/rdf+xml) on the endpoint URI and store the header
	 */
	boolean benchmark(final String endpointURI) throws Exception{
		//perform an HTTP Get and parse the response
		
		
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(endpointURI);
		System.out.println(_any23Header);
		request.addHeader("accept", _any23Header);
		HttpResponse response;
		Boolean res=false;
//		Count<String> statuse = Count()<String>();
		try {
//			
			response = client.execute(request);
			String type = getType(response);
			String status = ""+response.getStatusLine().getStatusCode();
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(getLogDir(), URLEncoder.encode(endpointURI,"UTF-8")+"-header.nq")));
			CallbackNxBufferedWriter cb = new  CallbackNxBufferedWriter(bw);
			Headers.processHeaders(new URI(endpointURI), response.getStatusLine().getStatusCode(), response.getAllHeaders(), cb);
			bw.close();
//		
			System.err.println("  "+endpointURI+" "+type+" "+status);
					
//           
			if(status.startsWith("2")||status.startsWith("3")){
				FileWriter fw = new FileWriter(new File(getLogDir(), URLEncoder.encode(endpointURI,"UTF-8")+"-response.dat"));
					BufferedReader rd = new BufferedReader
                			(new InputStreamReader(response.getEntity().getContent()));
                	String line = "";
                	while ((line = rd.readLine()) != null) {
                		fw.write(line+"\n");
                	}
                	fw.close();
                	res = true;     
			}
//			
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
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
