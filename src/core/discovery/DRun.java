package core.discovery;

import core.Endpoint;

public abstract class DRun<T> {

	protected Endpoint _ep;

	public DRun(Endpoint ep) {
		_ep = ep;
	}
	
	abstract public T execute();

}