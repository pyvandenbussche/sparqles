package core;

import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericData.Record;

import core.discovery.DResultGET;

import utils.AvroUtils;
import utils.DateFormater;

public abstract class EndpointResult implements AvroSerialize {

	
	static protected Schema SCHEMA;
	static private Schema SCHEMA_EPR;
	static{
		try {
			SCHEMA_EPR = AvroUtils.parseSchema(EndpointResult.class.getResourceAsStream("EndpointResult.avsc"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Endpoint _ep;
	private Exception _exc;
	private long _start;
	private long _end;
	private Date _date;
	

	public EndpointResult() {
		try {
			System.out.println("Here" );
			SCHEMA = AvroUtils.parseSchema(DResultGET.class.getResourceAsStream(this.getClass().getSimpleName()+".avsc"));
			System.out.println(SCHEMA);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("This "+this.getClass().getSimpleName());
	}
	
	public void setEndpoint(Endpoint ep){
		_ep =ep;
	}
	
	
	public void recordError(Exception exc ){
		_exc = exc;
	}
	
	public void start() {
		_start= System.currentTimeMillis();
		_date = GregorianCalendar.getInstance().getTime();
	}
	
	public void end() {
		_end= System.currentTimeMillis();
	}
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("  EP-URI:\t").append(_ep).append("\n");
		sb.append("  ISO8601:\t").append(DateFormater.getDataAsString(DateFormater.ISO8601)).append("\n");
//		sb.append("  start:").append(_start).append("\n");
//		sb.append("  end:").append(_end).append("\n");
		sb.append("  elapsed(ms):\t").append((_end-_start)).append("\n");
		return sb.toString();
	}
	
	
	public GenericData.Record serialize() {
		GenericData.Record record = new GenericData.Record(EndpointResult.SCHEMA_EPR);
		record.put("endpoint", _ep.serialize());
		record.put("date", DateFormater.getDataAsString(DateFormater.ISO8601));
		record.put("start", _start);
		record.put("end", _end);
		
		return record;
	}


	
	
}
