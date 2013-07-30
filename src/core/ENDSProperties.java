package core;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ENDSProperties {
	private static final Logger log = LoggerFactory.getLogger(ENDSProperties.class);
	
	public static final String DB_DRIVER;
	public static final String DB_URL;

	public static final String DATA_DIR;

	public static Integer SPARQL_WAITTIME;
	public static Integer PTASK_WAITTIME;
	public static Integer FTASK_WAITTIME;



	static {
		Properties props = new Properties();
		try {
			props.load(ClassLoader.getSystemResourceAsStream("ends.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		DATA_DIR = props.getProperty("data.dir", "./data");
		DB_DRIVER = props.getProperty("db.driver","org.h2.Driver");
		DB_URL = props.getProperty("db.url","jdbc:h2:./db.h2");
		
		
		 SPARQL_WAITTIME = Integer.valueOf(props.getProperty("waittime", "5000"));
		 PTASK_WAITTIME = Integer.valueOf(props.getProperty("ptask.waittime", ""+SPARQL_WAITTIME));
		 FTASK_WAITTIME = Integer.valueOf(props.getProperty("ftask.waittime", ""+SPARQL_WAITTIME));
		 
		 
		Object[] t = {DATA_DIR, DB_DRIVER, DB_URL};
		log.debug("Setup for this instance is: data:{}, db_drv:{}, db_url:{}",t );
	}
}
