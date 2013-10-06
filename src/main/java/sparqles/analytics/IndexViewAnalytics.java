package sparqles.analytics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.analytics.avro.AvailabilityIndex;
import sparqles.analytics.avro.EPView;
import sparqles.analytics.avro.EPViewPerformanceData;
import sparqles.analytics.avro.EPViewPerformanceDataValues;
import sparqles.analytics.avro.Index;
import sparqles.analytics.avro.IndexViewInterData;
import sparqles.analytics.avro.IndexViewPerformanceData;
import sparqles.analytics.avro.IndexViewPerformanceDataValues;
import sparqles.analytics.avro.PerformanceView;
import sparqles.core.CONSTANTS;
import sparqles.core.Task;
import sparqles.core.analytics.avro.IndexViewInteroperability;
import sparqles.core.analytics.avro.IndexViewPerformance;
import sparqles.utils.MongoDBManager;

public class IndexViewAnalytics implements Task<Index>{

	private static final Logger log = LoggerFactory.getLogger(IndexViewAnalytics.class);
	
	private MongoDBManager _dbm;

	@Override
	public Index call() throws Exception {
		
		//get the index view
		Collection<Index> idxs = _dbm.get(Index.class, Index.SCHEMA$);
		Index idx=null;
		if(idxs.size()==0){
			idx = createIndex();
			_dbm.insert(idx);
		}else if(idxs.size()>1){
//			log.warn("Too many results");
		}else
			idx = idxs.iterator().next();
		
		//get epview
		Collection<EPView> epviews = _dbm.get(EPView.class, EPView.SCHEMA$);
		
		log.info("Found {} idx views and {} epviews", idxs.size(), epviews.size());
		Map< String, SimpleHistogram> weekHist = new HashMap<String, SimpleHistogram>();
		SummaryStatistics askCold = new SummaryStatistics();
		SummaryStatistics askWarm = new SummaryStatistics();
		SummaryStatistics joinCold = new SummaryStatistics();
		SummaryStatistics joinWarm = new SummaryStatistics();
		for(EPView epv: epviews){
			//update availability
			for(Entry<CharSequence, Double> values: epv.getAvailability().getData().getValues().entrySet()){
				update(values, weekHist);
			}
			//update performance
			update(epv.getPerformance().getAsk(), askCold, askWarm);
			update(epv.getPerformance().getJoin(), joinCold, joinWarm);
		}
		List<AvailabilityIndex> aidxs = idx.getAvailability();

		
		//update availability stats
		for(Entry<String, SimpleHistogram> week: weekHist.entrySet()){
			SimpleHistogram sh = week.getValue();
			
			int total = sh.sampleSize;
			
			for(AvailabilityIndex aidx: aidxs){
				int value =0;
				if(aidx.getKey().equals("[0;5]"))
					value = sh.bin[0];
				if(aidx.getKey().equals("]5;90]"))
					value = sh.bin[1];
				if(aidx.getKey().equals("]90;95]"))
					value = sh.bin[2];
				if(aidx.getKey().equals("]95;100]"))
					value = sh.bin[3];
		
				aidx.getValues().put(week.getKey(), value/(double)total);
			}
		}
		
		//update performance stats
		ArrayList<IndexViewPerformanceData> data = new ArrayList<IndexViewPerformanceData>();
		List<IndexViewPerformanceDataValues> l = new ArrayList<IndexViewPerformanceDataValues>();
		IndexViewPerformanceData cold = new IndexViewPerformanceData("Cold Tests", "#1f77b4",l);
		l = new ArrayList<IndexViewPerformanceDataValues>();
		IndexViewPerformanceData warm = new IndexViewPerformanceData("Warm Tests", "#2ca02c",l);
		data.add(cold);
		data.add(warm);
		
		cold.getData().add(new IndexViewPerformanceDataValues("Average ASK", askCold.getMean()));
		cold.getData().add(new IndexViewPerformanceDataValues("Average JOIN", joinCold.getMean()));
		
		warm.getData().add(new IndexViewPerformanceDataValues("Average ASK", askWarm.getMean()));
		warm.getData().add(new IndexViewPerformanceDataValues("Average JOIN", joinWarm.getMean()));
		
		
		idx.getPerformance().setThreshold(-1L);
		idx.getPerformance().setData(data);
		
		
		log.info("Updated view {}", idx);
		_dbm.update(idx);
		
		return idx;
	}

	private void update(List<EPViewPerformanceData> results,
			SummaryStatistics cold, SummaryStatistics warm) {
		for(EPViewPerformanceData pdata : results){
			if(pdata.getKey().toString().contains("Cold")){
				for(EPViewPerformanceDataValues v : pdata.getData()){
					cold.addValue(v.getValue());
				}
			}
			if(pdata.getKey().toString().contains("WARM")){
				for(EPViewPerformanceDataValues v : pdata.getData()){
					warm.addValue(v.getValue());
				}
			}
		}
	}

	

	private Index createIndex() {
		Index idx = new Index();
		idx.setEndpoint(CONSTANTS.SPARQLES);
		
		
		
		
		AvailabilityIndex aidx = new AvailabilityIndex("[0;5]",new HashMap<CharSequence, Double>());
		List<AvailabilityIndex> aidxs = new ArrayList<AvailabilityIndex>();
		
		aidxs.add(aidx);
		
		aidx = new AvailabilityIndex("]5;90]",new HashMap<CharSequence, Double>());
		aidxs.add(aidx);
		
		aidx = new AvailabilityIndex("]90;95]",new HashMap<CharSequence, Double>());
		aidxs.add(aidx);
		
		aidx = new AvailabilityIndex("]95;100]",new HashMap<CharSequence, Double>());
		aidxs.add(aidx);
		
		idx.setAvailability(aidxs);
		
		IndexViewPerformance idxp = new IndexViewPerformance();
		idxp.setThreshold(-1L);
		idxp.setData(new ArrayList<IndexViewPerformanceData>());
		idx.setPerformance(idxp);
		
		
		IndexViewInteroperability idxi = new IndexViewInteroperability();
		idxi.setData(new ArrayList<IndexViewInterData>());
		idx.setInteroperability(idxi);
		
		
		return idx;
	}

	private void update(Entry<CharSequence, Double> values,
			Map<String, SimpleHistogram> weekHist) {
		String key = values.getKey().toString();
		SimpleHistogram sh = weekHist.get(key);
		if(sh==null){ sh = new SimpleHistogram();
		weekHist.put(key, sh);
		}
		sh.add(values.getValue());
	}

	@Override
	public void setDBManager(MongoDBManager dbm) {
		_dbm = dbm;
	}
	
	
	
	class SimpleHistogram{
		
		int sampleSize=0;
		int [] bin= {0,0,0,0};
		
		
		public void add(Double d){
			if( d <= 0.05)
				bin[0]++;
			else if( 0.05 < d  && d<= 0.9)
				bin[1]++;
			else if( 0.9 < d && d <= 0.95)
				bin[2]++;
			else if( 0.95 < d && d <= 1)
				bin[3]++;
			
			sampleSize++;
		}
		
	}



	
}
