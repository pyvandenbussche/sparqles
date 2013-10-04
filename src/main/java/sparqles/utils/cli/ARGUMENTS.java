/**
 *
 */
package sparqles.utils.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;

/**
 * Commandline parameters
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @date Jul 19, 2011
 */
public class ARGUMENTS {

	public static final int SHORT_ARG = 0;
	public static final int LONG_ARG = 1;
	
	/*
	 * HELP
	 */
	protected static final String PARAM_HELP = "?";
	protected static final String[] PARAM_HELP1 = createParam("h","help");
	protected static final OptionGroup OPTIONGROUP_HELP = new OptionGroup();
	static{
		OPTIONGROUP_HELP.addOption(createOption("help", 0, "print help screen", PARAM_HELP, null,false));
		OPTIONGROUP_HELP.addOption(createOption("help", 0, "print help screen", PARAM_HELP1[SHORT_ARG], PARAM_HELP1[LONG_ARG],false));
	}
	
	/*
	 * GENERAL ARGUMENTS
	 */
	public static final String [] PARAM_PROP_FILE = 	createParam("p","prop");
	public static final String [] PARAM_FLAG_DEBUG = 	createParam("d","verbose");
	public static final String [] PARAM_FLAG_INIT = 	createParam("i","init");
	public static final String [] PARAM_FLAG_START = 	createParam("s","start");
	public static final String [] PARAM_FLAG_RECOMPUTE = createParam("r","recompute");
	public static final String [] PARAM_FLAG_RESCHEDULE = createParam("rs","reschedule");
		
	public static final Option OPTION_PROP_FILE = createOption("property file", 1, "SPARQLES (additional) property file", 
			PARAM_PROP_FILE[SHORT_ARG], PARAM_PROP_FILE[LONG_ARG],true);
	public static final Option OPTION_DEBUG = createOption("flag",0,"enable verbose mode",
			PARAM_FLAG_DEBUG[SHORT_ARG],PARAM_FLAG_DEBUG[LONG_ARG],false);
	public static final Option OPTION_INIT = createOption("flag",0,"init datahub list",
			PARAM_FLAG_INIT[SHORT_ARG],PARAM_FLAG_INIT[LONG_ARG],false);
	public static final Option OPTION_START = createOption("flag",0,"start the service",
			PARAM_FLAG_START[SHORT_ARG],PARAM_FLAG_START[LONG_ARG],false);
	public static final Option OPTION_RECOMPUTE  = createOption("flag",0,"recompute the analytics",
			PARAM_FLAG_RECOMPUTE[SHORT_ARG],PARAM_FLAG_RECOMPUTE[LONG_ARG],false);
	public static final Option OPTION_RESCHEDULE  = createOption("flag",0,"recompute the analytics",
			PARAM_FLAG_RESCHEDULE[SHORT_ARG],PARAM_FLAG_RESCHEDULE[LONG_ARG],false);
	

	/**
	 * @param string
	 * @param string2
	 * @return
	 */
	protected static String[] createParam(String s, String l) {
		String [] arg = new String[2];
		arg[SHORT_ARG]=s;
		arg[LONG_ARG]=l;
		return arg;
	}
	protected static Option createOption(String argName, int args, String description, String shortArgname, String longArgname, boolean mandatory){

		
		
		Option o;
		if(shortArgname!=null){
			o  = OptionBuilder.withArgName(argName)
			.withDescription(description).create(shortArgname);
		}
		else
			o  = OptionBuilder.withArgName(argName)
			.withDescription(description).create();

		if(longArgname!=null){
			o.setLongOpt(longArgname);
		}
		if(args >= 0)
			o.setArgs(args);
		
		o.setRequired(mandatory);
		return o;
	}
}
