package sparqles.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SPARQLESProperties {
	private static final Logger log = LoggerFactory.getLogger(SPARQLESProperties.class);

		private static String DATA_DIR = "./data";
	
	private static String SCHEDULE_CRON;
	
	public static String getSCHEDULE_CRON() {
		return SCHEDULE_CRON;
	}


	private static String PTASK_QUERIES;

	private static Integer SPARQL_WAITTIME=5000;
	private static Integer PTASK_WAITTIME=SPARQL_WAITTIME;
	private static Integer FTASK_WAITTIME=SPARQL_WAITTIME;
	private static String FTASK_QUERIES;

	private static Integer TASK_THREADS=10;

	private static String ENDPOINT_LIST;

	private static String DB_HOST = "localhost";
	private static int DB_PORT=27017;

	private static String DB_NAME="sparqles";
	
	public static String getDATA_DIR() {
		return DATA_DIR;
	}
	
	public static String getDB_HOST() {
		return DB_HOST;
	}
	public static String getDB_NAME() {
		return DB_NAME;
	}

	public static int getDB_PORT() {
		return DB_PORT;
	}

	public static String getPTASK_QUERIES() {
		return PTASK_QUERIES;
	}

	public static Integer getSPARQL_WAITTIME() {
		return SPARQL_WAITTIME;
	}

	public static Integer getPTASK_WAITTIME() {
		return PTASK_WAITTIME;
	}

	public static Integer getFTASK_WAITTIME() {
		return FTASK_WAITTIME;
	}

	public static String getFTASK_QUERIES() {
		return FTASK_QUERIES;
	}

	public static Integer getTASK_THREADS() {
		return TASK_THREADS;
	}

	public static String getENDPOINT_LIST() {
		return ENDPOINT_LIST;
	}

	public static void init(File propFile){
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(propFile));
			init(props);
		} catch (IOException e) {
			log.error("Could not load properties from file: {}",propFile);
		}
	}


	public static void init(Properties props) {
		


		DATA_DIR = props.getProperty("data.dir", DATA_DIR);

		DB_HOST = props.getProperty("db.host",DB_HOST);
		DB_NAME = props.getProperty("db.name",DB_NAME);
		DB_PORT = Integer.valueOf(props.getProperty("db.port",""+DB_PORT));

		FTASK_QUERIES = (props.getProperty("ftask.queries"));
		PTASK_QUERIES = (props.getProperty("ptask.queries"));
		
		TASK_THREADS = Integer.valueOf(props.getProperty("task.threads",""+TASK_THREADS));
		
		ENDPOINT_LIST = (props.getProperty("endpoint.list"));

		SPARQL_WAITTIME = Integer.valueOf(props.getProperty("waittime", ""+SPARQL_WAITTIME));
		PTASK_WAITTIME = Integer.valueOf(props.getProperty("ptask.waittime", ""+SPARQL_WAITTIME));
		FTASK_WAITTIME = Integer.valueOf(props.getProperty("ftask.waittime", ""+SPARQL_WAITTIME));

		SCHEDULE_CRON = props.getProperty("schedule.cron");
		
		Object[] t = {DATA_DIR, DB_HOST, DB_PORT};
		log.debug("[LOAD] properties: {}",props );

	}
}
