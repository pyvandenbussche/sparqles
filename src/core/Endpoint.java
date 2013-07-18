package core;

import java.io.IOException;
import java.net.URI;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.util.Utf8;

import utils.AvroUtils;

/**
 * This class describes an endpoint 
 * @author UmbrichJ
 *
 */
public class Endpoint implements AvroSerialize{

	static private Schema SCHEMA;
	static{
		try {
			SCHEMA = AvroUtils.parseSchema(Endpoint.class.getResourceAsStream("Endpoint.avsc"));
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private final URI _uri;

	public Endpoint(final URI endpointURI) {
		_uri = endpointURI;
	}

	public URI getEndpointURI() {
		return _uri;
	}
	
	public GenericData.Record serialize() {
		GenericData.Record record = new GenericData.Record(Endpoint.SCHEMA);
		record.put("uri", new Utf8(_uri.toString()));
		return record;
	}
	
	
	
	@Override
	public String toString() {
		return _uri.toString();
	}

}
