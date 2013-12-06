package sparqles.core.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import sparqles.avro.discovery.DResult;
import sparqles.avro.discovery.RobotsTXT;

public class SitemapHandler extends DefaultHandler {
	private static final Logger log = LoggerFactory.getLogger(SitemapHandler.class);
	
	boolean sparqlEP = false, datasetURI=false;
	private RobotsTXT _rtxt;
	private DResult _result;
	private String _epURI;
	
	public SitemapHandler(RobotsTXT rtxt, DResult result, String epURI) {
		_rtxt = rtxt;
		_result = result;
		_epURI = epURI;
	}

	
	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		System.out.println("Prefix: "+prefix +" uri: "+ uri);
	}


	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		sparqlEP = uri.equalsIgnoreCase("http://sw.deri.org/2007/07/sitemapextension/scschema.xsd") && localName.equalsIgnoreCase("sparqlEndpointLocation");
		datasetURI = uri.equalsIgnoreCase("http://sw.deri.org/2007/07/sitemapextension/scschema.xsd") && localName.equalsIgnoreCase("datasetURI");
		
//		if(sparqlEP || datasetURI)
//			System.out.println("Start: "+uri+ " lname:"+localName+" qName:"+qName + " attr:"+ attributes);
		if(sparqlEP)
			_rtxt.setSitemapXMLSPARQL(true);
	}
	
	
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if(sparqlEP && uri.equalsIgnoreCase("http://sw.deri.org/2007/07/sitemapextension/scschema.xsd") && localName.equalsIgnoreCase("sparqlEndpointLocation")){
//			System.out.println("end: "+uri + " lname:"+localName+" qName:"+qName );
			sparqlEP = false;
			
			
		}if(datasetURI && uri.equalsIgnoreCase("http://sw.deri.org/2007/07/sitemapextension/scschema.xsd") && localName.equalsIgnoreCase("datasetURI")){
//			System.out.println("end: "+uri + " lname:"+localName+" qName:"+qName );
			datasetURI = false;
		}
				
		}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if(sparqlEP){
			String ep = new String(new String(ch, start, length));
			
			
			boolean match = _epURI.equals(ep);
			log.debug("sparqlEP : " + new String(ch, start, length) + " match:"+match);
			//check if the URL matches
			_rtxt.setSitemapXMLSPARQLMatch(match);
		}
		if(datasetURI){
			//TODO look if that is a voiD file, might be very expensive
			log.debug("datasetURI : " + new String(ch, start, length));
		}
		
	}
}
