package core.discovery;

import core.Endpoint;

public class SpecificDTask {

	public static DRun<GetResult> newGetRun(Endpoint endpoint) {
		return new DGETRun(endpoint);
	}

	public static DRun<VoidResult> newSelfVoidRun(Endpoint endpoint) {
		return new DGetSelfVoidRun(endpoint);
	}




	
}
