package sparqles.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

import org.apache.jena.riot.web.HttpOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;

import sparqles.core.CONSTANTS;
import sparqles.avro.Endpoint;

public class QueryManager {

	private static final Logger log = LoggerFactory.getLogger(QueryManager.class);
	
	
	
	public static String getQuery(String folder, String qFile) {
		log.info("getQuery {}, {}", folder, qFile);
		String content = null;
		Scanner scanner=null;
		if(folder.startsWith("file:")){
			File fold = new File(folder.replace("file:", ""));
			try {
				scanner = new Scanner(new File(fold,qFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}else{
			InputStream res = QueryManager.class.getClassLoader().getSystemResourceAsStream(folder+qFile);
			if(res != null)
				scanner = new Scanner(res);
		}
		if(scanner == null){
			log.warn("FAILED Could not load query file {} from {}", qFile, folder);
			return null;
		}

		if(scanner.hasNext())
			content = scanner.useDelimiter("\\Z").next();
		
		log.debug("PARSED input:{},output:{}", qFile, content);
		scanner.close();
		return substitute(content);
	}
	
	private static String substitute(String query){
		long time= System.currentTimeMillis();
    	if (query.contains("%%uri1")){
    		String url1 = "<http://nonsensical.com/1/"+time+">";
    		query = query.replace("%%uri1", url1 );
    	}
    	if (query.contains("%%uri2")){
    		String url2 = "<http://nonsensical.com/2/"+time+">";
    		query = query.replace("%%uri2", url2 );
    	}
    	if (query.contains("%%uri3")){
    		String url3 = "<http://nonsensical.com/3/"+time+">";
    		query = query.replace("%%uri3", url3 );
    	}
		return query;
	}

	public static QueryExecution getExecution(Endpoint ep, String query) throws Exception {
		return getExecution(ep.getUri().toString(), query);
	}
		public static QueryExecution getExecution(String epURL, String query) throws Exception {
		try {

			HttpOp.setUserAgent(CONSTANTS.USER_AGENT);
			log.debug("INIT QueryExecution for {} with query  {}",epURL,query.replaceAll("\n", ""));
			return QueryExecutionFactory.sparqlService(epURL, query);
		}
		catch (Exception e) {  
			throw new Exception(e.getMessage());
		}
	}
}
