package sparqles.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;

public class LogFormater {
	

	public static String toString(Exception e){
		return e.getClass().getSimpleName()+" msg:"+e.getMessage()+" cause:"+e.getCause();
	}
	
	public static String toFullString(Exception e){
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}
}