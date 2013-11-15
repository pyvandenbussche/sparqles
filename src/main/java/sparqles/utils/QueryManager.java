package sparqles.utils;

import java.io.File;
import java.io.FileNotFoundException;
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
		String content;
		Scanner scanner=null;
		if(folder.startsWith("file:")){
			File fold = new File(folder.replace("file:", ""));
			try {
				scanner = new Scanner(new File(fold,qFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}else{
			scanner = new Scanner(QueryManager.class.getClassLoader().getSystemResourceAsStream(folder+qFile));
		}
		if(scanner == null){
			log.warn("FAILED Could not load query file {} from {}", qFile, folder);
			return null;
		}


		content = scanner.useDelimiter("\\Z").next();
		log.debug("PARSED input:{},output:{}", qFile, content);
		scanner.close();
		return content;
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
