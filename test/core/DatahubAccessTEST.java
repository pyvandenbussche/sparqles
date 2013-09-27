package core;

import org.junit.Test;

import sparqles.core.EndpointManager;
import sparqles.utils.DatahubAccess;

public class DatahubAccessTEST {

	
	@Test
	public void testAccess(){
		EndpointManager em = new EndpointManager();
		DatahubAccess.checkEndpointList(em);
	}
}
