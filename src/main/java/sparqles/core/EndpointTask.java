package sparqles.core;

import org.apache.avro.specific.SpecificRecordBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.analytics.Analytics;
import sparqles.analytics.avro.Index;
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
public abstract class EndpointTask<V extends SpecificRecordBase> implements Task<V>{
	private static final Logger log = LoggerFactory.getLogger(EndpointTask.class);
		
	private final EndpointResult _epr;
	private final String _id;

	private MongoDBManager _dbm;
	protected FileManager _fm;

	private Analytics<V> _analytics;
	
	public EndpointTask(Endpoint ep) {
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
	
	@Override
	public V call() throws Exception {
		long start = System.currentTimeMillis();
		_epr.setStart(start);
		V v = null;
		try{
			LogHandler.run(log,this.getClass().getSimpleName(), _epr.getEndpoint().getUri().toString());
			 v= process(_epr);
			long end = System.currentTimeMillis();
			
			LogHandler.success(log,this.getClass().getSimpleName(), _epr.getEndpoint().getUri().toString(), end-start);
			_epr.setEnd(end);
			
			//insert into database
			if(_dbm != null &&  !_dbm.insert(v)){
				log.warn("Could not store record to DB");
			}
			
			//write to file
			if(_fm != null &&  !_fm.writeResult(v)){
				log.warn("Could not store record to file");
			}
			if(_analytics != null && !_analytics.analyse(v)){
				log.warn("Could not analyse the task results");
			}
		}catch(Exception e){
			LogHandler.error(log,this.getClass().getSimpleName(), _epr.getEndpoint().getUri().toString(),e);
		}
		_epr.setEnd(System.currentTimeMillis());
		return v;
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