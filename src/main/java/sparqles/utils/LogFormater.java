package sparqles.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;

public class LogFormater {

	
	//used for initalisation processes
	public static final String INIT ="[INIT]";
	
	
	
	
	
	
	
	
	
	/*
	 * Exception handling
	 */
	public static void warn(Logger log, String msg, Exception e) {
		Object []t= {msg, e.getClass().getSimpleName(), e.getMessage()};
		log.warn("[EXEP] {} ({}:{})",t);
	}

	public static String toString(Exception e){
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}
	
	
	public static void run(Logger log, String msg, Object ... o) {
		log.debug("[EXEC] "+msg, o);
	}

	
	public static void init(Logger log, String msg, Object ... o) {
		log.info("[INIT] "+msg, o);
	}

	
	/*
	 * This is for the general task logging
	 */
	/**
	 * 
	 * @param log
	 * @param task
	 * @param ep
	 */
	public static void run(Logger log, String task, String ep) {
		log.info("[EXEC] {} {}", task, ep);
	}
	public static void success(Logger log, String task, String ep, Long time) {
		log.info("[SUCC] {} {} #> in {} ms", task, ep, time);
	}
	public static void error(Logger log, String task, String ep, Exception e) {
		log.warn("[FAILED] {} {} #> {}: {}", task,ep, e.getClass().getSimpleName(), e.getMessage());
	}

	public static void debugSuccess(Logger log, String string, Object ...  o) {
		log.debug("[SUCC] "+string, o);
	}

	public static void debugERROR(Logger log, String msg, Object ... o) {
		log.debug(msg, o);
		
	}

	
}