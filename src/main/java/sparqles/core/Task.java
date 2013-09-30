package sparqles.core;

import java.util.concurrent.Callable;

import org.apache.avro.specific.SpecificRecordBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.analytics.Analytics;
import sparqles.utils.FileManager;
import sparqles.utils.LogHandler;
import sparqles.utils.MongoDBManager;

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

	private MongoDBManager _dbm;
	protected FileManager _fm;

	private Analytics _analytics;
	
	public Task(Endpoint ep) {
		_epr = new EndpointResult();
		_epr.setEndpoint(ep);
		_id = this.getClass().getSimpleName()+"("+_epr.getEndpoint().getUri()+")";
	}
	
	public void setDBManager(MongoDBManager dbm){
		_dbm = dbm;
	}
	public void setFileManager(FileManager fm) {
		_fm =fm;
	}
	
	public V call(){
		long start = System.currentTimeMillis();
		_epr.setStart(start);
		try{
			LogHandler.run(log,this.getClass().getSimpleName(), _epr.getEndpoint().getUri().toString());
			V v= process(_epr);
			
			//insert into database
			if(_dbm != null &&  !_dbm.insert(v)){
				log.warn("Could not store record to DB");
			}
			
			//write to file
			if(_fm != null &&  !_fm.writeResult(v)){
				log.warn("Could not store record to file");
			}
			long end = System.currentTimeMillis();
			
			LogHandler.success(log,this.getClass().getSimpleName(), _epr.getEndpoint().getUri().toString(), end-start);
			_epr.setEnd(end);
			return v;
		}catch(Exception e){
			LogHandler.error(log,this.getClass().getSimpleName(), _epr.getEndpoint().getUri().toString(),e);
		}
		
		_epr.setEnd(System.currentTimeMillis());
		return null;
	}
	
	
	abstract public V process(EndpointResult epr);
	
	@Override
	public String toString() {
		return _id; 
	}

	public void setAnalytics(Analytics a) {
		_analytics = a;
		
	}
}