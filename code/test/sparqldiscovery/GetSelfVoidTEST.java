package sparqldiscovery;

import java.io.File;

public class GetSelfVoidTEST {

	public GetSelfVoidTEST() {

	}

	public static void main(String[] args) {
		File f= new File("results");
		GetSelfVoid get = new GetSelfVoid(f);
		get.evaluate();
	}
}