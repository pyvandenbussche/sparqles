package sparqles.core.discovery;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.avro.util.Utf8;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.riot.lang.PipedTriplesStream;
import org.osjava.norbert.NoRobotClient;
import org.osjava.norbert.NoRobotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.RDFNode;


import sparqles.avro.Endpoint;
import sparqles.avro.EndpointResult;
import sparqles.avro.core.Robots;
import sparqles.avro.discovery.DGETInfo;
import sparqles.avro.discovery.DResult;
import sparqles.avro.discovery.QueryInfo;
import sparqles.avro.discovery.RobotsTXT;
import sparqles.core.CONSTANTS;
import sparqles.core.EndpointTask;
import sparqles.utils.ConnectionManager;
import sparqles.utils.ExceptionHandler;
import sparqles.utils.QueryManager;

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
public class DTask extends EndpointTask<DResult> {
	private static final Logger log = LoggerFactory.getLogger(DTask.class);
	private final static ConnectionManager cm = new ConnectionManager(null, 0, null, null, 50);
	private final static String sparqDescNS = "http://www.w3.org/ns/sparql-service-description#";
	private final static String voidNS = "http://rdfs.org/ns/void#";
	public static final String header = "application/rdf+xml, text/rdf, text/rdf+xml, application/rdf";

	private final static String query = "" +
			"PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
			"PREFIX void:     <http://rdfs.org/ns/void#>\n"+
			"SELECT DISTINCT * \n"+
			"WHERE {\n"+ 
			"?ds a void:Dataset .\n"+ 
			"?ds void:sparqlEndpoint %%s .\n"+
			"?ds ?p ?o .\n"+
			"}";
	
	public DTask(Endpoint ep) {
		super(ep);
	}

	@Override
	public DResult process(EndpointResult epr) {
		DResult result = new DResult();
		result.setEndpointResult(epr);
		log.debug("execute {}", _epURI);
		
		result.setDescriptionFiles((List)new ArrayList<DGETInfo>());

		int failures=0;

		//RobotsTXT run
		log.debug("execute {} {}","robots", _epURI);
		RobotsTXT rtxt = new RobotsTXT(false,false,false,false,false,false,"");

		result.setRobotsTXT(rtxt);
		
		//get list of existing robots.txt
		List<Robots> r = _dbm.getResults(_ep, Robots.class, Robots.SCHEMA$);
		//check everytime for updated robots.txts
		Robots rob = fetchRobotsTXT();

		if(r.size()==0){
			//first robots.txt test, insert into DB
			_dbm.insert(rob);
		}else{
			if(rob.getRespCode().toString().startsWith("5")){
				//there was a server error, try to get the last stored robots.txt
				if(r.size()==1){ rob = r.get(0);}
			}else{
				//update robots txt
				_dbm.update(rob);
			}
		}
		if(rob.getRespCode()==200)
			rtxt.setHasRobotsTXT(true);

		boolean isRobotsAllowed = checkRobotsTxt(rob);
		rtxt.setAllowedByRobotsTXT(isRobotsAllowed);

		log.debug("execute {} {}","sitemap", _epURI);
		//discovery void and sparqles via semantic sitemap.xml
		//http://vocab.deri.ie/void/guide#sec_5_2_Discovery_via_sitemaps
		parseSitemapXML(rob,rtxt,result);

		//inspect HTTP Get
		//ok we checked the robots.txt, now we do a http get on the sparql URL
		log.debug("execute {} {}","httpget", _epURI);
		try {
			URI epURL = new URI(_ep.getUri().toString());
			DGETInfo info = checkForVoid(epURL.toString(), "EPURL");
			result.getDescriptionFiles().add(info);
		} catch (Exception e) {
			log.debug("[EXC] HTTP GET "+_epURI, ExceptionHandler.logAndtoString(e, true));
		}
		log.debug("execute {} {}","well-known", _epURI);
		try{
			//well-known location
			URI epURL = new URI(_ep.getUri().toString());
			URL wellknown = new URI(epURL.getScheme(), epURL
					.getAuthority(), "/.well-known/void", null, null)
					.toURL();
			DGETInfo info = checkForVoid(wellknown.toString(), "wellknown");
			result.getDescriptionFiles().add(info);
		} catch (Exception e) {
			log.debug("[EXC] HTTP well known "+_epURI, e);
		}
		
		List<QueryInfo> queryInfos = new ArrayList<QueryInfo>();
		result.setQueryInfo(queryInfos);
		
		log.debug("execute {} {}","query-self", _epURI);
		//maybe the endpoint has data about itself
		queryInfos.add(query(_ep.getUri().toString()));
		
		log.info("executed {}", this);

		return result;
	}

	private QueryInfo query(String epURL) {
		QueryInfo info = new QueryInfo();
		info.setURL(epURL);
		
		String queryString = query.replaceAll("%%s", "<"+_ep.getUri()+">");
		
		HashSet<String> voidAset= new HashSet<String>();
		
		ArrayList<CharSequence> voidA = new ArrayList<CharSequence>();
		info.setResults(voidA);
		
		// initializing queryExecution factory with remote service.
		QueryExecution qexec = null;
		try {
			qexec = QueryManager.getExecution(epURL, queryString);
		
			boolean results = false;
			
			ResultSet resSet = qexec.execSelect();
			ResultSetRewindable reswind = ResultSetFactory.makeRewindable(resSet);
			
			while(reswind.hasNext()){
				RDFNode dataset = reswind.next().get("ds");
				voidAset.add(dataset.toString());
			}
			
			voidA.addAll(voidAset);
			log.info("Found {} results",reswind.getRowNumber());
		} catch (Exception e1) {
			info.setException(ExceptionHandler.logAndtoString(e1));
			log.debug("[EXEC] SPARQL query to "+epURL+" for "+_epURI, e1);
		}
		finally {
			if(qexec!=null)qexec.close();
		}
		return info;
		
	}

	//	<sc:datasetURI> for void
	//sc:sparqlEndpointLocation
	/**
	 * Find information about sitemap.xml in robots.txt by parsing the robots.txt content 
	 * for the "Sitemap:" value. 
	 * Next, retrieve the sitemap.xml and look for sc:sparqlEndpointLocation
	 * @param rob
	 * @param rtxt
	 * @param result
	 */
	private void parseSitemapXML(Robots rob,RobotsTXT rtxt, DResult result) {
		String robotsContent = rob.getContent().toString();
		
		URL sitemapURL =null;
		if(robotsContent!=null){
			BufferedReader bufReader = new BufferedReader(new StringReader(robotsContent));
			String line=null;
			try {
				while( (line=bufReader.readLine()) != null ){
					if(line.trim().startsWith("Sitemap")){

						sitemapURL = new URL(line.substring(line.indexOf(":")+1).trim());
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(sitemapURL!=null){
			rtxt.setSitemapXML(true);
			HttpGet get=null;
			try{    
				get = new HttpGet(sitemapURL.toURI());
				HttpResponse resp = cm.connect(get);

				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				dbFactory.setNamespaceAware(true);
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				
				String conent = EntityUtils.toString(resp.getEntity());
				//	System.out.println(conent);
				Document doc = dBuilder.parse(new ByteArrayInputStream(conent.getBytes()));
				doc.getDocumentElement().normalize();

				NodeList nodeList = doc.getElementsByTagNameNS("http://sw.deri.org/2007/07/sitemapextension/scschema.xsd", "sparqlEndpointLocation");
				rtxt.setSitemapXMLSPARQL(nodeList.getLength()!=0);
				for (int temp = 0; temp < nodeList.getLength(); temp++) {
					Node nNode = nodeList.item(temp);
//					System.out.println(nNode.getTextContent());
					if(_ep.getUri().toString().equalsIgnoreCase(nNode.getTextContent())){
						rtxt.setSitemapXMLSPARQLMatch(true);
					}
				}
				nodeList = doc.getElementsByTagNameNS("http://sw.deri.org/2007/07/sitemapextension/scschema.xsd", "datasetURI");
				for (int temp = 0; temp < nodeList.getLength(); temp++) {
					Node nNode = nodeList.item(temp);
					DGETInfo info = checkForVoid(nNode.getTextContent(), "sitemap.xml_link");
					result.getDescriptionFiles().add(info);
				}
			} catch (Exception e) {
				log.debug("[EXEC] Sitemap for "+_epURI, e);
				rtxt.setException(ExceptionHandler.logAndtoString(e));
			}finally{
				if(get != null){
					get.releaseConnection();
				}
			}
		}
	}

	private DGETInfo checkForVoid(String url, String operation) {
		DGETInfo info = new DGETInfo();
		info.setOperation(operation);
		info.setURL(url);
		
		HashMap<CharSequence, Object> voidPred = new HashMap<CharSequence, Object>();
		HashMap<CharSequence, Object> spdsPred = new HashMap<CharSequence, Object>();
		info.setSPARQLDESCpreds( spdsPred );
		info.setVoiDpreds( voidPred );
		
		HttpGet request = new HttpGet(url);
		request.addHeader("accept", "application/rdf+xml, application/x-turtle, application/rdf+n3, application/xml, text/turtle, text/rdf, text/plain;q=0.1");
		request.addHeader("User-Agent", CONSTANTS.USER_AGENT);
		log.info("GET {}",request);
		HttpResponse resp;
		try {
			resp = cm.connect(request);
			
			String type = getType(resp);

			String status = ""+resp.getStatusLine().getStatusCode();
			info.setResponseCode(status);
			
			Header [] header = resp.getAllHeaders();
			
			// 1) CHeck the header for information
			parseHeaders(info,  header);
			if(status.startsWith("2")){
				String content = EntityUtils.toString(resp.getEntity());	
				info.setContent(content);
				PipedRDFIterator<Triple> iter = new PipedRDFIterator<Triple>();
				final PipedRDFStream<Triple> inputStream = new PipedTriplesStream(iter);
				ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes());
				RDFDataMgr.parse(inputStream, bais,url, getLangFromType(type));

				while(iter.hasNext()){
					Triple t = iter.next();
					String pred  = t.getPredicate().toString();
					if(pred.startsWith(sparqDescNS)){
						update(pred.replace(sparqDescNS, ""), spdsPred );
					}else if(pred.startsWith(voidNS)){
						update( pred.replace(voidNS, ""), voidPred );
					}
				}
			}
		}catch(Exception e ){
			log.warn("failed checking for VOID "+url+" for "+_epURI, ExceptionHandler.logAndtoString(e,true));
			info.setException(ExceptionHandler.logAndtoString(e));
		}finally{
			request.releaseConnection();
		}
		return info;
	}

	private Robots fetchRobotsTXT() {
		Robots rob = new Robots();
		rob.setEndpoint(_ep);
		rob.setRespCode(-1);
		rob.setContent("");
		URI robotsOnHost;
		URI host = null;
		try {
			host = new URI(_ep.getUri().toString());
			robotsOnHost = new URI(host.getScheme(), host.getAuthority(), "/robots.txt", null, null);
		} catch (URISyntaxException e) {
			log.debug("[EXEC] ROBOTS for "+ _epURI,e);
			rob.setException(ExceptionHandler.logAndtoString(e));
			return rob;
		}

		HttpGet hget = new HttpGet(robotsOnHost);
		HttpResponse hres;
		try {
			hres = cm.connect(hget);
			HttpEntity hen = hres.getEntity();

			int status = hres.getStatusLine().getStatusCode();
			rob.setRespCode(status);
			if (status == 200) {
				if (hen != null) {

					String content = EntityUtils.toString(hen);
					rob.setContent(content);
				}
			}	
			hget.abort();
		} catch (Exception e1) {
			log.debug("[EXEC] ROBOTS for "+ _epURI,e1);
			rob.setException(ExceptionHandler.logAndtoString(e1));
		}finally{
			hget.releaseConnection();
		}
		return rob;
	}

	private boolean checkRobotsTxt(Robots rob) {
		NoRobotClient _nrc = new NoRobotClient(CONSTANTS.USER_AGENT);
		URI host;
		try {
			host = new URI(rob.getEndpoint().getUri().toString());
			try {
				if (!((host.getPath() == null || host.getPath().equals(
						""))
						&& host.getQuery() == null && host
						.getFragment() == null))
					// If the URI host comes for whatever reason with
					// path, query, or fragment, strip it.
					_nrc.parse(
							rob.getContent().toString(),
							(new URI(host.getScheme(), host
									.getAuthority(), null, null, null))
									.toURL());
				else
					_nrc.parse(rob.getContent().toString(), host.toURL());

			} catch (NoRobotException e) {
				log.debug("no robots.txt for " + host);
				return true;
			} 
			return _nrc.isUrlAllowed(host.toURL());

		} catch (Exception e1) {
			log.warn("failed checking for ROBOTS PARSE for "+ _epURI,ExceptionHandler.logAndtoString(e1, true));
		}
		return true;
	}
	
	private void parseHeaders(DGETInfo info,  Header[] header) {
		for (int i = 0; i < header.length; i++) {
			String name = header[i].getName();
			if(name.equals("Content-Type")){
				info.setResponseType(new Utf8(header[i].getValue()));
			}
			if(name.equals("Server")){
				info.setResponseServer(parseServer(header[i].getValue()));
			}
			if(name.equals("Link")){
				info.setResponseLink(parseServer(header[i].getValue()));
			}
		}
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

	private CharSequence parseServer(String value) {
		String server= value.trim();
		if(server.contains("/"))
			server = server.substring(0,server.indexOf("/"));

		if(server.contains("("))
			server = server.substring(0,server.indexOf("("));

		return new Utf8(server);
	}

	private void update(CharSequence key, Map<CharSequence, Object> map){
		if(map.containsKey(key))
			map.put(key, ((Integer)map.get(key))+1);
		else
			map.put(key,1);	
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