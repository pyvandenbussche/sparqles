package utils.cli;

import java.io.File;
import java.sql.Date;

import javax.swing.DefaultButtonModel;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.util.FileManager;

import schedule.Scheduler;
import utils.DatahubAccess;
import utils.DateFormater;

import core.DBManager;
import core.ENDSProperties;
import core.EndpointManager;






/**
 * Main CLI class for the SPARQL Endpoint status program 
 * @author UmbrichJ
 *
 */
public class SPARQLES extends CLIObject{
	private static final Logger log = LoggerFactory.getLogger(SPARQLES.class);


	@Override
	public String getDescription() {
		return "Start and control SPARQLES";
	}

	@Override
	protected void addOptions(Options opts) {
		opts.addOption(ARGUMENTS.OPTION_PROP_FILE);
		opts.addOption(ARGUMENTS.OPTION_INIT);
		opts.addOption(ARGUMENTS.OPTION_START);
	}

	@Override
	protected void execute(CommandLine cmd) {

		//load the Properties
		if( CLIObject.hasOption(cmd, ARGUMENTS.PARAM_PROP_FILE)){
			File propFile = new File(CLIObject.getOptionValue(cmd, ARGUMENTS.PARAM_PROP_FILE));
			if(propFile.exists()){
				ENDSProperties.init(propFile);
			}else{
				log.warn("Specified property file ({}) does not exist", propFile);
			}
		}

		// Init the endpoint manager
		final EndpointManager epm = new EndpointManager();
		epm.init();

		//Init the scheduler
		final Scheduler s = new Scheduler();
		s.useDB(true);
		s.useFileManager(true);

		//reinitialise datahub 
		if( CLIObject.hasOption(cmd, ARGUMENTS.PARAM_INIT)){
			//check the endpoint list
			DatahubAccess.checkEndpointList(epm);
			//create schedule
			s.createDefaultSchedule(epm);
		}
		s.init(epm);

		Thread runtimeHookThread = new Thread() {
			public void run() {
				log.info("[START] [SHUTDOWN] Shutting down the system");
				s.close();
				epm.close();
				log.info("[SUCCESS] [SHUTDOWN] Everything closed normally");
			}};
		Runtime.getRuntime().addShutdownHook (runtimeHookThread);
		
		if( CLIObject.hasOption(cmd, ARGUMENTS.PARAM_START)){
			try {
				long start = System.currentTimeMillis();
				while (true) {
					log.info("Running since {}", DateFormater.getDataAsString(DateFormater.ISO8601, new Date(start)));
					Thread.sleep (1800000);
				}
			}catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
}