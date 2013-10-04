package sparqles.utils.cli;

import java.io.File;
import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.analytics.AAnalyserInit;
import sparqles.core.Endpoint;
import sparqles.core.SPARQLESProperties;
import sparqles.schedule.Schedule;
import sparqles.schedule.Scheduler;
import sparqles.utils.DatahubAccess;
import sparqles.utils.DateFormater;
import sparqles.utils.MongoDBManager;

/**
 * Main CLI class for the SPARQL Endpoint status program 
 * @author UmbrichJ
 *
 */
public class SPARQLES extends CLIObject{
	private static final Logger log = LoggerFactory.getLogger(SPARQLES.class);
	private Scheduler scheduler;
	private MongoDBManager dbm;
	
	@Override
	public String getDescription() {
		return "Start and control SPARQLES";
	}

	@Override
	protected void addOptions(Options opts) {
		opts.addOption(ARGUMENTS.OPTION_PROP_FILE);
		opts.addOption(ARGUMENTS.OPTION_INIT);
		opts.addOption(ARGUMENTS.OPTION_START);
		opts.addOption(ARGUMENTS.OPTION_RECOMPUTE);
		opts.addOption(ARGUMENTS.OPTION_RESCHEDULE);
	}

	@Override
	protected void execute(CommandLine cmd) {

		
		parseCMD(cmd);
				
		//reinitialise datahub 
		if( CLIObject.hasOption(cmd, ARGUMENTS.PARAM_FLAG_INIT)){
			//check the endpoint list
			Collection<Endpoint> eps = DatahubAccess.checkEndpointList();
			dbm.initEndpointCollection();
			dbm.insert(eps);			 
		}
		if( CLIObject.hasOption(cmd, ARGUMENTS.PARAM_FLAG_RESCHEDULE)){
			Collection<Schedule> epss = Scheduler.createDefaultSchedule(dbm);
			dbm.initScheduleCollection();
			dbm.insert(epss);
		}
		if( CLIObject.hasOption(cmd, ARGUMENTS.PARAM_FLAG_RECOMPUTE)){
			recomputeAnalytics();
		}
		
		if( CLIObject.hasOption(cmd, ARGUMENTS.PARAM_FLAG_START)){
			start();
		}
		
		Runtime.getRuntime().addShutdownHook (new ShutdownThread(this));
	}

	private void recomputeAnalytics() {
		dbm.initAggregateCollections();
		AAnalyserInit a = new AAnalyserInit(dbm);
		a.run();
	}

	private void start() {
//		epm.init(dbm);
		scheduler.init(dbm);
		try {
			long start = System.currentTimeMillis();
			while (true) {
				log.info("Running since {}", DateFormater.formatInterval(System.currentTimeMillis()-start));
				Thread.sleep (1800000);
			}
		}catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private void parseCMD(CommandLine cmd) {
		//load the Properties
		if( CLIObject.hasOption(cmd, ARGUMENTS.PARAM_PROP_FILE)){
			File propFile = new File(CLIObject.getOptionValue(cmd, ARGUMENTS.PARAM_PROP_FILE));
			if(propFile.exists()){
				log.info("Reading properties from {}",propFile);
				SPARQLESProperties.init(propFile);
			}else{
				log.warn("Specified property file ({}) does not exist", propFile);
			}
		}

		setup(true,true);
	}

	public void init(String[] arguments) {
		CommandLine cmd =  verifyArgs(arguments);
		parseCMD(cmd);
	}
	
	private void setup(boolean useDB, boolean useFM) {
		//Init the scheduler
		scheduler = new Scheduler();
		
		if(useDB){
			dbm = new MongoDBManager();
			scheduler.useDB(dbm);
		}
		scheduler.useFileManager(useFM);
	}

	public void stop() {
		log.info("[START] [SHUTDOWN] Shutting down the system");
		scheduler.close();
		log.info("[SUCCESS] [SHUTDOWN] Everything closed normally");
	}
	
	class ShutdownThread extends Thread{
		private SPARQLES _s;
		public ShutdownThread(SPARQLES s) {
			_s=s;
		}
		@Override
		public void run() {
			_s.stop();
		}
	}
}