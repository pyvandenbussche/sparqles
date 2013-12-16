package sparqles.utils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.mortbay.log.Log;

import sparqles.core.CONSTANTS;



public class ConnectionManager {

    private DefaultHttpClient _client;
	private IdleConnectionMonitorThread _monitor;

	

    
    public ConnectionManager(String proxyHost, int proxyPort, String puser, String ppassword, int connections) {
    	// general setup
    	SchemeRegistry supportedSchemes = new SchemeRegistry();

    	// Register the "http" and "https" protocol schemes, they are
    	// required by the default operator to look up socket factories.
    	supportedSchemes.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
    	supportedSchemes.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

    	// prepare parameters
    	HttpParams params = new BasicHttpParams();
    	HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
    	HttpProtocolParams.setContentCharset(params, "UTF-8");
    	HttpProtocolParams.setUseExpectContinue(params, true);
    	
    	HttpClientParams.setRedirecting(params, true);

    	//connection params 
    	params.setParameter(CoreConnectionPNames.SO_TIMEOUT, CONSTANTS.SOCKET_TIMEOUT);
//    	params.setParameter(CoreConnectionPNames.TCP_NODELAY, true);
    	params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONSTANTS.CONNECTION_TIMEOUT);

    	ConnManagerParams.setMaxTotalConnections(params, connections);
    	ClientConnectionManager cm = new ThreadSafeClientConnManager(params, supportedSchemes);
    	
    	_client = new DefaultHttpClient(cm, params);
    	

    	// check if we have a proxy
    	if (proxyHost != null) {
    		HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
    		_client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
    		
    		if (puser != null) {
    			_client.getCredentialsProvider().setCredentials(
    					new AuthScope(proxyHost, proxyPort),
    					new UsernamePasswordCredentials(puser, new String(ppassword))); 
    		}
    	}
    	_monitor = new IdleConnectionMonitorThread(cm);
    	_monitor.start();
 	}
    
   
    public void shutdown() {
    	_client.getConnectionManager().shutdown();
    }

    public HttpResponse connect(HttpGet get) throws ClientProtocolException, IOException {
    	return _client.execute(get);
    }
    
    static class IdleConnectionMonitorThread extends Thread {
        
        private final ClientConnectionManager connMgr;
        private volatile boolean shutdown;
        
        public IdleConnectionMonitorThread(ClientConnectionManager connMgr) {
            super();
            this.connMgr = connMgr;
        }

        @Override
        public void run() {
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(5000);
                        // Close expired connections
                        connMgr.closeExpiredConnections();
                        // Optionally, close connections
                        // that have been idle longer than 30 sec
                        connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
                        Log.info("Cleaning up expired and idle connections");
                    }
                }
            } catch (InterruptedException ex) {
                // terminate
            }
        }
        
        public void shutdown() {
            shutdown = true;
            synchronized (this) {
                notifyAll();
            }
        }
        
    }
}