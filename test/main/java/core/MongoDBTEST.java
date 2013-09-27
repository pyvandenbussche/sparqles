package core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.junit.Test;

import sparqles.core.Dataset;
import sparqles.core.SPARQLESProperties;
import sparqles.core.Endpoint;
import sparqles.core.EndpointFactory;
import sparqles.core.EndpointResult;
import sparqles.core.availability.AResult;
import sparqles.utils.MongoDBManager;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

public class MongoDBTEST {

	@Test
	public void test() throws URISyntaxException, IOException {
		
		SPARQLESProperties.init(new File("src/test/resources/ends.properties"));
		
		
		MongoDBManager m = new MongoDBManager();
		
		
		AResult f = new AResult();
		
		Endpoint e = EndpointFactory.newEndpoint("http://test.com/");
		
		ArrayList<Dataset> d = new ArrayList<Dataset>();
		Dataset dd = new Dataset();
		dd.setLabel("Test");
		dd.setUri("test");
		d.add(dd);
		e.setDatasets(d);
		
		
		m.insert(e);
		
		
		Endpoint e1 = EndpointFactory.newEndpoint("http://test.com/e1");
		
		ArrayList<Dataset> d1 = new ArrayList<Dataset>();
		Dataset dd1 = new Dataset();
		dd1.setLabel("Test1");
		dd1.setUri("test1");
		d1.add(dd);
		e1.setDatasets(d1);
		
		m.insert(e1);
		
		
		System.out.println("_____________________");
		for(Endpoint epe: m.get(Endpoint.class, Endpoint.SCHEMA$)){
			System.out.println(epe);
		}System.out.println("_____________________");
		System.out.println("_____________________");
		
		
		
		System.out.println(e);
		EndpointResult er = new EndpointResult();
		er.setEndpoint(e);
		er.setEnd(System.currentTimeMillis());
		er.setStart(System.currentTimeMillis());
		
		f.setIsAvailable(true);
		f.setIsPrivate(false);
		f.setExplaination("All is good");
		f.setEndpointResult(er);
		System.out.println(f);
		
		
		
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
	    DatumWriter<AResult> writer = new SpecificDatumWriter<AResult>(f.getSchema());
	    Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);
	    writer.write( f, encoder);
	    encoder.flush();
	    out.close();
	    
	    System.out.println("___________________");
	    System.out.println("Endpoint "+e);
	    System.out.println("Endpoint-Schema "+e.getSchema());
	    System.out.println();
	    System.out.println("ER:"+er);
	    System.out.println("ER-Schema"+er.getSchema());
	    System.out.println("ER-EP-Schema"+er.getEndpoint().getSchema());
	    
	    System.out.println("A:"+f);
	    System.out.println("A-Schema"+f.getSchema());
	    System.out.println("A-ER-Schema"+f.getEndpointResult().getSchema());
	    System.out.println("A-ER-EPSchema"+f.getEndpointResult().getEndpoint().getSchema());
	    
	    System.out.println("Schema:" +f.getSchema());
	    SpecificDatumReader<AResult> reader = new SpecificDatumReader<AResult>(f.getSchema());
	    Decoder decoder = DecoderFactory.get().binaryDecoder(out.toByteArray(), null);
	    AResult result = reader.read(null, decoder);
	    
		System.out.println("Deser"+result);
		System.exit(0);
		
		
		
		
		System.out.println("______RES_______");
		List<AResult> res = m.getResults(e, AResult.class, AResult.SCHEMA$);
		for(AResult r: res){
			System.out.println(r);
		}
		
		
		MongoClient mongoClient = new MongoClient( "localhost" , 27017 );

		DB db = mongoClient.getDB( "test" );
		
		Set<String> colls = db.getCollectionNames();
		System.out.println("Collections");
		for (String s : colls) {
		    System.out.println(s);
		}
		
		System.out.println("DBs");
		for (String s : mongoClient.getDatabaseNames()) {
		   System.out.println(s);
		}
		
		
		
		DBCollection c = db.getCollection("test");
		
		BasicDBObject doc = new BasicDBObject("name", "MongoDB").
                append("type", "database").
                append("count", 1).
                append("info", new BasicDBObject("x", 203).append("y", 102));

		c.insert(doc);
		System.out.println(c.getCount());
		
		DBCursor cursor = c.find();
		try {
		   while(cursor.hasNext()) {
		       System.out.println(cursor.next());
		   }
		} finally {
		   cursor.close();
		}
		
		BasicDBObject query = new BasicDBObject("name", "MongoDB");

		cursor = c.find(query);
System.out.println("_______________");
		try {
		   while(cursor.hasNext()) {
		       System.out.println(cursor.next());
		   }
		} finally {
		   cursor.close();
		}
	}
}
