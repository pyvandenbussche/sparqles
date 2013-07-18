package sparqldiscovery;

import java.io.File;

public class LinkHeaderTEST {

	public LinkHeaderTEST() {

	}

	public static void main(String[] args) {
		File f= new File("results");
		LinkHeader get = new LinkHeader(f);
		
		get.evaluate(20);
	}
}
