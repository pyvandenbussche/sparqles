package core.performance;

public class Utils {
	public static String removeNewlines(String in){
		if(in == null) return null;
		return in.replaceAll("\r", "  ").replaceAll("\n", "  ");
	}
}
