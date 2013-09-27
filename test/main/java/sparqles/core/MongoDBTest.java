package sparqles.core;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import sparqles.utils.MongoDBManager;

public class MongoDBTest {

	@Test
	public void test() {
		
		SPARQLESProperties.init(new File("test/main/resources/ends.properties"));
		
		MongoDBManager m = new MongoDBManager();
		assertTrue(m.isRunning());
		
	}

}
