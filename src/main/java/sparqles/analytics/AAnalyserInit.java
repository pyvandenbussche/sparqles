package sparqles.analytics;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.apache.avro.specific.SpecificRecordBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.core.Endpoint;
import sparqles.core.availability.AResult;
import sparqles.core.features.FResult;
import sparqles.core.performance.PResult;
import sparqles.utils.MongoDBManager;
import sparqles.utils.cli.SPARQLES;

public class AAnalyserInit {

	private static final Logger log = LoggerFactory.getLogger(AAnalyserInit.class);

	private MongoDBManager _db;

	private boolean _onlyLast;

	public AAnalyserInit(MongoDBManager db) {
		this(db, false);
	}

	public AAnalyserInit(MongoDBManager db, boolean onlyLast) {
		_db = db;
		_onlyLast = onlyLast;
	}

	/**
	 * Computes the aggregated statistics for the Availability task
	 * @param ep
	 */
	public void run() {

		List<Endpoint> eps =_db.get(Endpoint.class, Endpoint.SCHEMA$);
		AAnalyser a = new AAnalyser(_db);
		PAnalyser p = new PAnalyser(_db);
		//		DAnalyser d = new DAnalyser(_db);
		FAnalyser f = new FAnalyser(_db);

		for (Endpoint ep: eps) {
			log.info("[ANALYSE] {}",ep);

			availability(ep,a);
			//			discoverability(ep,d);
			interoperability(ep,f);
			performance(ep,p);
		}
	}



	private void performance(Endpoint ep, PAnalyser p) {
		TreeSet<PResult> res = new TreeSet<PResult>(new Comparator<PResult>() {
			public int compare(PResult o1, PResult o2) {
				int diff =o1.getEndpointResult().getStart().compareTo(o2.getEndpointResult().getStart()); 
				return diff;
			}
		});

		List<PResult> epRes = _db.getResults(ep, PResult.class, PResult.SCHEMA$);
		for(PResult epres: epRes){
			res.add(epres);
		}
		if(_onlyLast){
			p.analyse(res.last());
		}else{
			for(PResult ares: res){
				p.analyse(ares);
			}
		}
		log.info("[ANALYSE] [PERFORMANCE] {} and {}",ep, epRes.size());
	}

	private void interoperability(Endpoint ep, FAnalyser f) {
		TreeSet<FResult> res = new TreeSet<FResult>(new Comparator<FResult>() {
			public int compare(FResult o1, FResult o2) {
				int diff =o1.getEndpointResult().getStart().compareTo(o2.getEndpointResult().getStart()); 
				return diff;
			}
		});

		List<FResult> epRes = _db.getResults(ep, FResult.class, FResult.SCHEMA$);
		for(FResult epres: epRes){
			res.add(epres);
		}
		if(_onlyLast){
			f.analyse(res.last());
		}else{
			for(FResult ares: res){
				f.analyse(ares);
			}
		}
		log.info("[ANALYSE] [INTEROPERABILITY] {} and {}",ep, epRes.size());

	}

	private void availability(Endpoint ep, AAnalyser a) {

		TreeSet<AResult> res = new TreeSet<AResult>(new Comparator<AResult>() {
			public int compare(AResult o1, AResult o2) {
				int diff =o1.getEndpointResult().getStart().compareTo(o2.getEndpointResult().getStart()); 
				return diff;
			}
		});

		List<AResult> epRes = _db.getResults(ep, AResult.class, AResult.SCHEMA$);
		for(AResult epres: epRes){
			res.add(epres);
		}
		if(_onlyLast){
			a.analyse(res.last());
		}else{
			for(AResult ares: res){
				a.analyse(ares);
			}
		}
		log.info("[ANALYSE] [AVAILABILITY] {} and {}",ep, epRes.size());

	}
}