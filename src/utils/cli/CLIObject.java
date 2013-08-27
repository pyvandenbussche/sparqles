package utils.cli;

import java.util.Arrays;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public abstract class CLIObject {
	
	private static final Logger log = LoggerFactory.getLogger(CLIObject.class);
	private Options _opts;

	public Options getOptions(){
		return _opts;
	}
	
	public abstract String getDescription();

	public String getCommand(){
		return this.getClass().getSimpleName();
	}
	public CLIObject() {
		init();
	}

	
		
	protected void init() {
		_opts = new Options();
		_opts.addOptionGroup(ARGUMENTS.OPTIONGROUP_HELP);
		addOptions(_opts);
	}

	
	
	
	/**
	 * add all Option(Groups) to this object
	 * Note: The help flag is set automatically ("?")
	 * @param opts
	 */
	abstract protected void addOptions(Options opts);

	public void run(String[] args) {
		log.info("[START] [ARGS] "+Arrays.toString(args));
		CommandLine cmd = verifyArgs(args);
		
		long start = System.currentTimeMillis();
		execute(cmd);
		long end = System.currentTimeMillis();
		log.info("[END] ("+(end-start)+" ms)");
	}

	abstract protected void execute(CommandLine cmd);

	protected CommandLine verifyArgs(String[] args) {
		init();

		CommandLineParser parser = new GnuParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(_opts, args);
		} catch (org.apache.commons.cli.ParseException e) {
			log.info("ERROR: "+e.getClass().getSimpleName()+" : "+e.getMessage()+" args={"+Arrays.toString(args)+"}");
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(this.getClass().getSimpleName(), _opts ,true);
			System.exit(-1);
		}
		if(cmd!=null && (cmd.hasOption(ARGUMENTS.PARAM_HELP)||cmd.hasOption(ARGUMENTS.PARAM_HELP1[ARGUMENTS.SHORT_ARG])||cmd.hasOption(ARGUMENTS.PARAM_HELP1[ARGUMENTS.LONG_ARG]))){
			log.info("Here is a help (args length "+cmd.getArgList().size()+"): ");
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(this.getClass().getSimpleName(), _opts ,true);
			System.exit(-1);
		}
		return cmd;
	}

	/**
	 * @param cmd
	 * @param paramSparqlQuery
	 * @return
	 */
	public static String getOptionValue(CommandLine cmd,
			String[] param) {
		if(cmd.hasOption(param[ARGUMENTS.SHORT_ARG]))
			return cmd.getOptionValue(param[ARGUMENTS.SHORT_ARG]);
		if(cmd.hasOption(param[ARGUMENTS.LONG_ARG]))
			return cmd.getOptionValue(param[ARGUMENTS.LONG_ARG]);
		return null;
	}

	/**
	 * @param cmd
	 * @param paramProxyPort
	 * @param object
	 * @return
	 */
	public static String getOptionValue(CommandLine cmd,
			String[] param, String defaultValue) {
		String s = getOptionValue(cmd, param);
		if(s==null) return defaultValue;
		return s;
	}

	public static String[] getOptionValues(CommandLine cmd,
			String[] param) {
		if(cmd.hasOption(param[ARGUMENTS.SHORT_ARG]))
			return cmd.getOptionValues(param[ARGUMENTS.SHORT_ARG]);
		if(cmd.hasOption(param[ARGUMENTS.LONG_ARG]))
			return cmd.getOptionValues(param[ARGUMENTS.LONG_ARG]);
		return null;
	}
	
	/**
	 * @param paramDebug
	 * @return
	 */
	public static boolean hasOption(CommandLine cmd, String[] param) {
		if(cmd.hasOption(param[ARGUMENTS.SHORT_ARG]))
			return true;
		if(cmd.hasOption(param[ARGUMENTS.LONG_ARG]))
			return true;
		
		return false;
	}
	
	public static boolean hasOption(CommandLine cmd, String param) {
		return cmd.hasOption(param);
	}
}