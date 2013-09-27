package avro;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.BufferedBinaryEncoder;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.util.Utf8;
import org.junit.Before;
import org.junit.Test;

import sparqles.utils.AvroUtils;
import sparqles.utils.DateFormater;

import sparqles.core.Endpoint;

public class AvroTESTS {

	 private Schema a,b,c, ep, epres, dres;
	

     @Before
     public void setUp() throws Exception {

//             a = AvroUtils.parseSchema(new File("test/avro/A.avsc"));
//             b = AvroUtils.parseSchema(new File("test/avro/B.avsc"));
//             c = AvroUtils.parseSchema(new File("test/avro/test/C.avsc"));
     		
             ep = AvroUtils.parseSchema(new File("test/avro/Endpoint.avsc"));
             epres = AvroUtils.parseSchema(new File("test/avro/EndpointResult.avsc"));
             dres = AvroUtils.parseSchema(new File("test/avro/test/DResultGET.avsc"));

     }

     @Test
     public void testSimpleInheritance() throws Exception{
    	 //new Utf8("Doctor Who")
    	 GenericRecord ar = new GenericData.Record(ep);
    	 GenericRecord br = new GenericData.Record(epres);
    	 GenericRecord cr = new GenericData.Record(dres);
    	 
    	 ar.put("uri", "http::");
    	 br.put("endpoint", ar);
    	 br.put("date", DateFormater.getDataAsString(DateFormater.ISO8601));
    	 cr.put("endpointResult", br);
    	 System.out.println(ar);
    	 System.out.println(br);
    	 System.out.println(cr);
    	 
    	 
    	 
    	 
    	 
     }
}