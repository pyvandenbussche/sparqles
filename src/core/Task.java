package core;

import java.util.concurrent.Callable;

import core.discovery.DResultGET;

public abstract class Task<V extends EndpointResult> implements Callable<V>{
 
	protected final Endpoint _ep;
	private Class<V> _cls;
	
	public Task(Endpoint ep, Class<V> cls){
		_ep = ep;
		_cls=cls;
	}
	
	
	@Override
	public V call() throws Exception {
		V v = _cls.newInstance();
		v.setEndpoint(_ep);
		v.setTask(this);
		v.start();
		
		process(v);		
		
		v.end();
		return v;
	}
	
	abstract protected V process(V res);
}
