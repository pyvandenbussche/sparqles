package sparqles.core;

import java.util.concurrent.Callable;

import org.apache.avro.specific.SpecificRecordBase;

import sparqles.utils.MongoDBManager;

/**
 * A Task is a {@link Callable} connected to the database.
 * @author umbrichj
 *
 * @param <V> - return type restricted to AVRO objects
 */
public interface Task<V extends SpecificRecordBase>  extends Callable<V> {
	
//	public void execute();
	
	/**
	 * Set the MongoDBManager.
	 * @param dbm
	 */
	void setDBManager(MongoDBManager dbm);
}
