package org.deri.sparql.perf;

public class Utils {
	public static String removeNewlines(String in){
		if(in == null) return null;
		return in.replaceAll("\r", "  ").replaceAll("\n", "  ");
	}
}
