package sparqles.analytics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import sparqles.avro.Endpoint;
import sparqles.avro.analytics.AvailabilityIndex;
import sparqles.avro.analytics.EPView;
import sparqles.avro.analytics.EPViewAvailability;
import sparqles.avro.analytics.EPViewAvailabilityDataPoint;
import sparqles.avro.analytics.EPViewDiscoverability;
import sparqles.avro.analytics.EPViewDiscoverabilityData;
import sparqles.avro.analytics.EPViewInteroperability;
import sparqles.avro.analytics.EPViewInteroperabilityData;
import sparqles.avro.analytics.EPViewPerformance;
import sparqles.avro.analytics.EPViewPerformanceData;
import sparqles.avro.analytics.EPViewPerformanceDataValues;
import sparqles.avro.analytics.Index;
import sparqles.avro.analytics.IndexAvailabilityDataPoint;
import sparqles.avro.analytics.IndexViewDiscoverability;
import sparqles.avro.analytics.IndexViewDiscoverabilityData;
import sparqles.avro.analytics.IndexViewDiscoverabilityDataValues;
import sparqles.avro.analytics.IndexViewInterData;
import sparqles.avro.analytics.IndexViewInterDataValues;
import sparqles.avro.analytics.IndexViewInteroperability;
import sparqles.avro.analytics.IndexViewPerformance;
import sparqles.avro.analytics.IndexViewPerformanceData;
import sparqles.avro.analytics.IndexViewPerformanceDataValues;
import sparqles.avro.schedule.Schedule;
import sparqles.core.CONSTANTS;
import sparqles.core.Task;
import sparqles.schedule.Scheduler;
import sparqles.utils.DatahubAccess;
import sparqles.utils.MongoDBManager;

public class RefreshDataHubTask implements Task<Index>{

	private static final Logger log = LoggerFactory.getLogger(RefreshDataHubTask.class);
	private MongoDBManager _dbm;
	private Scheduler _s;
	

	@Override
	public Index call() throws Exception {
		log.info("[EXECUTE] updating ckan catalog" );
				
		Collection<Endpoint> datahub = DatahubAccess.checkEndpointList();
		Collection<Endpoint>  db = _dbm.get(Endpoint.class, Endpoint.SCHEMA$);
		
//		TreeSet<Endpoint> ckan = new TreeSet<Endpoint>(new EndpointComparator());
//		
//		
//		
//		
//		int newEPs = 0, upEPs=0;
//		for(Endpoint ep: list){
//			
//			Endpoint epDB = _dbm.getEndpoint(ep);
//			if(epDB==null){
//				//new endpoint, 
//				//add it to the db, create store and register the schedule
//				newEPs++;
//				_dbm.insert(ep);
//				Schedule sch = _s.defaultSchedule(ep);
//				_dbm.insert(sch);
//				_s.initSchedule(sch);
//				
//			}else{
//				if(ep.getDatasets().size() != epDB.getDatasets().size()){
//					_dbm.update(ep);
//					upEPs++;
//				}
//			}
//		}
//		log.info("[CKAN] received {} endpoints, new inserts {}, updates {}",list.size(), newEPs, upEPs);
		return null;
	}


	@Override
	public void setDBManager(MongoDBManager dbm) {
		_dbm = dbm;
		
	}


	public void setScheduler(Scheduler scheduler) {
		_s = scheduler;
		
	}
}