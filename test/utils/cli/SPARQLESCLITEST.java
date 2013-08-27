package utils.cli;



import java.io.IOException;

import org.junit.Test;

import core.Main;

public class SPARQLESCLITEST {

	@Test
	public void test() {
		final String [] args = {
				"SPARQLES",
				"-p","test/resources/ends.properties",
				"-s"};
		
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
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}