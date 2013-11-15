package sparqles.core;

import org.apache.avro.specific.SpecificRecordBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.analytics.Analytics;
import sparqles.avro.Endpoint;
import sparqles.avro.EndpointResult;
import sparqles.utils.FileManager;
import sparqles.utils.ExceptionHandler;
import sparqles.utils.MongoDBManager;

/**
 * 
 * Abstract task definition for an endpoint
 * 
 * @author UmbrichJ
 *
 * @param <V> is one of ATask, PTask, FTask or DTask
 */
public abstract class EndpointTask<V extends SpecificRecordBase> implements Task<V>{
	private static final Logger log = LoggerFactory.getLogger(EndpointTask.class);
		
	
	private final String _id;

	protected MongoDBManager _dbm;
	protected FileManager _fm;

	private Analytics<V> _analytics;

	protected final String _task;

	protected String _epURI;

	protected Endpoint _ep;
	
	public EndpointTask(Endpoint ep) {
		_epURI = ep.getUri().toString();
		setEndpoint(ep);
		
		_task = this.getClass().getSimpleName();
		_id = _task+"("+_epURI+")";
	}
	
	public Endpoint getEndpoint() {
		return _ep;
	}
	
	public void setEndpoint(Endpoint ep) {
		if(!_epURI.equals(ep.getUri().toString()))
			log.error("Endpoint URIs do not match (was:{} is:{}", _epURI, ep.getUri());
		_ep = ep;
	}

	public void setDBManager(MongoDBManager dbm){
		_dbm = dbm;
	}
	public void setFileManager(FileManager fm) {
		_fm =fm;
	}
	
	@Override
	public V call() throws Exception {
		log.info("EXECUTE {}", this);
		
		long start = System.currentTimeMillis();
		EndpointResult epr = new EndpointResult();
		epr.setEndpoint(_ep);
		epr.setStart(start);
		boolean i_succ=true, a_succ = true, f_succ= true;
		V v = null;
		try{
			
			v = process(epr);
			long end = System.currentTimeMillis();	
			epr.setEnd(end);
			
			//insert into database
			if(_dbm != null)
				i_succ=_dbm.insert(v);
			
			//write to file
			if(_fm != null)
				f_succ = _fm.writeResult(v);
			
			//analyse the results	
			if(_analytics != null)
				a_succ=  _analytics.analyse(v);

				
			log.info("EXECUTED {} in {} ms (idx:{}, disk:{}, analysed:{})", _id, end-start, i_succ, f_succ, a_succ);
		}catch(Exception e){
			log.error("FAILED {} (idx:{}, disk:{}, analysed:{}) {}" , this, i_succ, f_succ, a_succ, ExceptionHandler.logAndtoString(e, true));
		}
		return v;
	}
	
	
	abstract public V process(EndpointResult epr);
	
	@Override
	public String toString() {
		return _id; 
	}

	/**
	 * 
	 * @param a - the analytics program for this task
	 */
	public void setAnalytics(Analytics<V> a) {
		_analytics = a;
	}
}