package core;

import java.util.concurrent.Callable;

import org.apache.avro.specific.SpecificRecordBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Abstract Task class
 * 
 * @author UmbrichJ
 *
 * @param <V> is one of ATask, PTask, FTask or DTask
 */
public abstract class Task<V extends SpecificRecordBase> implements Callable<V> {
	private static final Logger log = LoggerFactory.getLogger(Task.class);
		
	private final EndpointResult _epr;
	private final String _id;

	private DBManager _dbm;
	protected FileManager _fm;
	
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
		try{
			utils.LogHandler.run(log,this.getClass().getSimpleName(), _epr.getEndpoint().getUri().toString());
			V v= process(_epr);
			if(_dbm != null &&  !_dbm.insertResult(v)){
				log.warn("Could not store record to DB");
			}
			if(_fm != null &&  !_fm.writeResult(v)){
				log.warn("Could not store record to file");
			}
			utils.LogHandler.success(log,this.getClass().getSimpleName(), _epr.getEndpoint().getUri().toString());
			return v;
		}catch(Exception e){
			utils.LogHandler.error(log,this.getClass().getSimpleName(), _epr.getEndpoint().getUri().toString(),e);
		}
		_epr.setEnd(System.currentTimeMillis());
		return null;
	}
	
	
	abstract public V process(EndpointResult epr);
	
	@Override
	public String toString() {
		return _id; 
	}
}