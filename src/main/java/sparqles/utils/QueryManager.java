package sparqles.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;

import sparqles.core.Endpoint;

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
			scanner = new Scanner(ClassLoader.getSystemResourceAsStream(folder+qFile));
		}
		if(scanner == null){
			log.warn("Could not load query file {} from {}", qFile, folder);
			return null;
		}


		content = scanner.useDelimiter("\\Z").next();
		log.debug("Parsed from {} query {}", qFile, content);
		scanner.close();
		return content;
	}

	public static QueryExecution getExecution(Endpoint ep, String query) throws Exception {
		try {
			log.info("Create QueryExecution for {} with query  {}",ep.getUri(),query);
			return QueryExecutionFactory.sparqlService(ep.getUri().toString(), query);
		}
		catch (Exception e) {  
			throw new Exception(e.getMessage());
		}
	}
}
