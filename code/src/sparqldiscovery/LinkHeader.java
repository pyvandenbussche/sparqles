package sparqldiscovery;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;

public class LinkHeader extends Benchmark {

	
	private final static String benchExperiment="lrdd";
	
	
	public LinkHeader(File logDir) {
		super(logDir,benchExperiment);
		
	}

	
	/**
	 * Perfom a HTTPGet operation (accept: application/rdf+xml) on the endpoint URI and store the header
	 */
	boolean benchmark(final String endpointURI) throws Exception{
		//perform an HTTP Get and parse the response
		
		
		
		HttpHead request = new HttpHead(endpointURI);
		
		List<URI> descriptorTriples = new ArrayList<URI>();
		HttpClient client = new DefaultHttpClient();
		try {
			HttpResponse response = client.execute(request);
			if (checkRespCode(response.getStatusLine().getStatusCode())) {
				// http://test.openxrd.org/header/simple
				// Link: <http://test.openxrd.org/header/simple;about>;
				// rel="describedby"; type="application/xrd+xml"
				
				org.apache.http.Header[] headers = response.getHeaders("Link");
				if(headers != null){
					for (org.apache.http.Header header : headers) {
						String value = header.getValue();
						if (value.contains("rel") && value.contains("describedby")	&& value.contains("<")) {
							URI base = request.getURI();
							String url = value.substring(value.indexOf("<") + 1,value.lastIndexOf(">"));
							URI descriptorURI = new URL(base.toURL(),url).toURI();
						 
							descriptorTriples.add(descriptorURI);
						}
					}
				}
				if(descriptorTriples.size()!=0){
					FileWriter fw = new FileWriter(new File(getLogDir(), URLEncoder.encode(endpointURI,"UTF-8")+"_uris.dat"));
					for(URI u: descriptorTriples){
						fw.write(u+"\n");
					}
					fw.close();
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}finally{
			request.abort();
		}	
		
		return descriptorTriples.size() != 0;
	}
	
	private boolean checkRespCode(int respCode) {
		String s = new String("" + respCode);
		return (s.startsWith("2") || s.startsWith("3"));
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
