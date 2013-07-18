package core;

import org.apache.avro.generic.GenericData;

public interface AvroSerialize {
	
	public GenericData.Record serialize();
}
