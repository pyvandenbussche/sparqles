package sparqles.core;

import org.apache.avro.specific.SpecificRecordBase;

import sparqles.utils.MongoDBManager;

public interface Task<V extends SpecificRecordBase> {
	
	public void execute();
	
	public void setDBManager(MongoDBManager dbm);
}
