package sparqles.analytics;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import sparqles.core.Endpoint;
import sparqles.core.availability.AResult;
import sparqles.utils.MongoDBManager;

public class AAnalyserInit {


	private MongoDBManager _db;

	public AAnalyserInit(MongoDBManager db) {
		_db = db;
	}

	/**
	 * Computes the aggregated statistics for the Availability task
	 * @param ep
	 */



	public void run() {

		List<Endpoint> eps =_db.get(Endpoint.class, Endpoint.SCHEMA$);
		AAnalyser a = new AAnalyser(_db);
		
		
		for(Endpoint ep: eps){
			System.out.println(ep);
			TreeSet<AResult> res = new TreeSet<AResult>(new Comparator<AResult>() {

				@Override
				public int compare(AResult o1, AResult o2) {
					int diff =o1.getEndpointResult().getStart().compareTo(o2.getEndpointResult().getStart()); 
					return diff;
				}
			});
			List<AResult> epRes = _db.getResults(ep, AResult.class, AResult.SCHEMA$);
			for(AResult epres: epRes){
				res.add(epres);
			}
			for(AResult ares: res.descendingSet()){
				System.out.println(ares);
				a.analyse(ares);
			}	
		}
		
	}
}
