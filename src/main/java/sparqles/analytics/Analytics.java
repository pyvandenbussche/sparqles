package sparqles.analytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.avro.specific.SpecificRecordBase;
import org.mortbay.log.Log;

import sparqles.avro.analytics.EPView;
import sparqles.avro.analytics.EPViewAvailability;
import sparqles.avro.analytics.EPViewAvailabilityData;
import sparqles.avro.analytics.EPViewAvailabilityDataPoint;
import sparqles.avro.analytics.EPViewDiscoverabilityData;
import sparqles.avro.analytics.EPViewInteroperabilityData;
import sparqles.avro.analytics.EPViewPerformanceData;
import sparqles.avro.Endpoint;
import sparqles.avro.analytics.EPViewDiscoverability;
import sparqles.avro.analytics.EPViewInteroperability;
import sparqles.avro.analytics.EPViewPerformance;
import sparqles.utils.MongoDBManager;



/**
 * Interface for all Analytics tasks.
 * @author umbrichj
 *
 * @param <V> either one of {@link AResult}, {@link DResult}, 
 * {@link FResult} or {@link PResult}
 */
public abstract class Analytics<V extends SpecificRecordBase> {
	
	
	protected final MongoDBManager _db;
	
	public Analytics(MongoDBManager db) {
		_db = db;
		
	}
	
	protected EPView getEPView(Endpoint ep) {
		EPView view =null;
		List<EPView> views = _db.getResults(ep,EPView.class, EPView.SCHEMA$);
		if(views.size()!=1){
			Log.warn("We have {} EPView, expected was 1",views.size());
		}
		if(views.size()==0){
			view = new EPView();
			view.setEndpoint(ep);
			
			EPViewAvailability av = new EPViewAvailability();
			view.setAvailability(av);
			EPViewAvailabilityData data = new EPViewAvailabilityData();
			av.setData(data);
			data.setKey("Availability");
			data.setValues(new ArrayList<EPViewAvailabilityDataPoint>());
			
			
			EPViewPerformance p = new EPViewPerformance();
			ArrayList<EPViewPerformanceData> askdata= new ArrayList<EPViewPerformanceData>();
			ArrayList<EPViewPerformanceData> joindata= new ArrayList<EPViewPerformanceData>();
			
			p.setAsk(askdata);
			p.setJoin(joindata);
			
			EPViewInteroperability iv = new EPViewInteroperability();
			iv.setSPARQL11Features(new ArrayList<EPViewInteroperabilityData>());
			iv.setSPARQL1Features(new ArrayList<EPViewInteroperabilityData>());
			
			view.setPerformance(p);
			view.setAvailability(av);
			view.setInteroperability(iv);
			
			
			EPViewDiscoverability dv = new EPViewDiscoverability("", new ArrayList<EPViewDiscoverabilityData>(),new ArrayList<EPViewDiscoverabilityData>());
			view.setDiscoverability(dv);
			
			_db.insert(view);
		}else{
			view = views.get(0);
		}
		return view;
	}
	
	/**
	 * 
	 * @param result - the result to analyse
	 * @return true in case of success, false otherwise
	 */
	public abstract boolean analyse(V result);


}
