package sparqles.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionHandler {
	
	private static final Logger log = LoggerFactory.getLogger(ExceptionHandler.class);
	private static final AtomicInteger excID = new AtomicInteger();
	private static final AtomicLong excCounter = new AtomicLong();
	
	private static final HashMap<Class, Integer> exceptionID = new HashMap<Class, Integer>();
	

	public static String logAndtoString(Exception e){
		return logAndtoString(e, false);
	}
	
	
	public static String logAndtoString(Exception e, boolean withExceptionID) {

		
		String id = ExceptionHandler.getExceptionID(e);
		ExceptionHandler.log(id,e);

		StringBuilder sb = new StringBuilder();
		if(withExceptionID){
			sb.append(id).append("> ");
		}
		sb.append(e.getClass().getSimpleName())
		.append(" msg:").append(e.getMessage())
		.append(" cause:").append(e.getCause()).toString();
		
		return sb.toString();

	}
	
	
	
	public static String toFullString(Exception e){
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}

	private static String getExceptionID(Exception e) {
//		System.out.println(exceptionID);
		
		Integer id = exceptionID.get(e.getClass());
		if(id ==null){
			id = excID.getAndIncrement();
			exceptionID.put(e.getClass(), id);
			log.info("New Exception ID: {} -> {}", id, e.getClass());
		}
		return "EXC@"+id+"#"+excCounter.getAndIncrement();
	}

	private static void log(String id, Exception e) {
		log.info(id,e);
	}

}