package utils.cli;



import java.io.IOException;

import org.junit.Test;

import sparqles.core.Main;



public class SPARQLESCLITEST {

	@Test
	public void test() {
		final String [] args = {
				"SPARQLES"
				,"-p","test/resources/ends.properties"
				,"-i"
//				,"-s"
				};
		
		try {
			Thread t = new Thread(){
				@Override
				public void run() {
					try {
						Main.main(args);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
			};
			t.start();
			Thread.sleep(120000);
			
			System.exit(0);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}
}