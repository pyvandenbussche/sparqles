package sparqles.analytics;

import java.util.Comparator;



import sparqles.avro.Dataset;
import sparqles.avro.Endpoint;

public class EndpointComparator implements Comparator<Endpoint> {

	@Override
	public int compare(Endpoint o1, Endpoint o2) {
		int diff = o1.getUri().toString().compareToIgnoreCase(o2.getUri().toString());
		
//		if(diff == 0)
//			diff= o1.getDatasets().size()- o2.getDatasets().size();
		
//		if(diff == 0){
//			for(Dataset d: o1.getDatasets()){
//				if(!o2.getDatasets().contains(d)){
//					return -1;
//				}
//			}
//		}
		return diff;
	}

}
