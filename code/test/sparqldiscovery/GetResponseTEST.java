package sparqldiscovery;

import java.io.File;

public class GetResponseTEST {

	public GetResponseTEST() {

	}

	public static void main(String[] args) {
		File f= new File("results");
		GetResponse get = new GetResponse(f);
		
		get.evaluate();
	}
}
