package sparqles.core;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.avro.specific.SpecificRecordBase;

import sparqles.utils.MongoDBManager;

public interface Task<V extends SpecificRecordBase>  extends Callable<V>{
	
//	public void execute();
	
	public void setDBManager(MongoDBManager dbm);
}
