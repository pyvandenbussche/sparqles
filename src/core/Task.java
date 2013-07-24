package core;

import java.util.concurrent.Callable;

public abstract class Task<V> implements Callable<V> {

	
	private EndpointResult _epr;


	public Task(Endpoint ep) {
		_epr = new EndpointResult();
		_epr.setEndpoint(ep);
	}
	
	@Override
	public V call() throws Exception {
		
		
		_epr.setStart(System.currentTimeMillis());
				
		V v= process(_epr);
		_epr.setEnd(System.currentTimeMillis());
		
		return v;
	}
	
	
	
	
	abstract public V process(EndpointResult epr);
	
}