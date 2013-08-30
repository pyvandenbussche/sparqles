package sparqles.utils;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

import sparqles.utils.cli.SPARQLES;

public class SPARQLESDaemon implements Daemon{

	private SPARQLES sparqles;

	@Override
	public void init(DaemonContext arg0) throws DaemonInitException, Exception {
		sparqles = new SPARQLES();
		sparqles.init(arg0.getArguments());
		
	}
	
	
	@Override
	public void destroy() {
		sparqles = null;
		
	}

	

	@Override
	public void start() throws Exception {
		sparqles.start();
		
	}

	@Override
	public void stop() throws Exception {
		sparqles.stop();
		
	}

}
