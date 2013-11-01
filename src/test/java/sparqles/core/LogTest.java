package sparqles.core;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.avro.specific.SpecificRecordBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class LogTest {
	private static final Logger log = LoggerFactory.getLogger(LogTest.class);
	
	
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		
		
		
		System.out.println("Test");
		log.debug("DEBUG");
		log.info("INFO");
		log.warn("WARN");
		log.error("ERROR");
		
		org.apache.log4j.Logger log4j = org.apache.log4j.Logger.getLogger(getClass());
		log4j.info("LOG4j info");
	}

}
