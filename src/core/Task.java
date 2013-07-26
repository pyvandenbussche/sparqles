package core;

import java.util.concurrent.Callable;

import org.apache.avro.specific.SpecificRecordBase;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Task<V extends SpecificRecordBase> implements Callable<V> {
	private static final Logger logger = LoggerFactory.getLogger(Task.class);
	
	private EndpointResult _epr;
	private String _id;
	private DBManager _dbm;

	private FileManager _fm;
	
	public Task(Endpoint ep) {
		_epr = new EndpointResult();
		_epr.setEndpoint(ep);
		_id = this.getClass().getSimpleName()+"("+_epr.getEndpoint().getUri()+")";
	}
	
	public void setDBManager(DBManager dbm){
		_dbm = dbm;
	}
	public void setFileManager(FileManager fm) {
		_fm =fm;
	}
	
	
	
	@Override
	public V call(){
		
		_epr.setStart(System.currentTimeMillis());
		V v= process(_epr);
		_epr.setEnd(System.currentTimeMillis());
	
		
		if(_dbm != null){
			if( !_dbm.insertResult(v)){
				Log.warn("Could not store record to DB");
			}
			
		}
		if(_fm != null){
			if( !_fm.writeResult(v)){
				Log.warn("Could not store record to file");
			}
			
		}
		
		return v;
	}
	
	
	abstract public V process(EndpointResult epr);
	
	@Override
	public String toString() {
		return _id; 
	}
}