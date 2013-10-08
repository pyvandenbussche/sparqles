package sparqles.analytics;

import java.util.ArrayList;
import java.util.Arrays;
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
import sparqles.analytics.avro.EPViewAvailability;
import sparqles.analytics.avro.EPViewInteroperabilityData;
import sparqles.analytics.avro.EPViewPerformanceData;
import sparqles.analytics.avro.EPViewPerformanceDataValues;
import sparqles.analytics.avro.Index;
import sparqles.analytics.avro.IndexViewInterData;
import sparqles.analytics.avro.IndexViewPerformanceData;
import sparqles.analytics.avro.IndexViewPerformanceDataValues;
import sparqles.analytics.avro.PerformanceView;
import sparqles.core.CONSTANTS;
import sparqles.core.Task;
import sparqles.core.analytics.avro.EPViewInteroperability;
import sparqles.core.analytics.avro.EPViewPerformance;
import sparqles.core.analytics.avro.IndexViewInteroperability;
import sparqles.core.analytics.avro.IndexViewPerformance;
import sparqles.utils.MongoDBManager;

public class IndexViewAnalytics implements Task<Index>{

	private static final Logger log = LoggerFactory.getLogger(IndexViewAnalytics.class);
	
	private MongoDBManager _dbm;
	final int askCold=0, askWarm=1, joinCold=2, joinWarm=3;
	final int sparql1_solMods=0, sparql1_com=1, sparql1_graph=2,
			  sparql11_agg=3, sparql11_filter=4, sparql11_other=5;
	
	
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
		
		
		//Prepare aggregated analytics
		Map< String, SimpleHistogram> weekHist = new HashMap<String, SimpleHistogram>();
		
		SummaryStatistics [] perfStats = {new SummaryStatistics(),
				new SummaryStatistics(), new SummaryStatistics(),
				new SummaryStatistics()};
		
		SummaryStatistics [] interStats = {new SummaryStatistics(),
				new SummaryStatistics(), new SummaryStatistics(),
				new SummaryStatistics(), new SummaryStatistics(),
				new SummaryStatistics()};
		
		//iterate over all epviews and analyse them
		for(EPView epv: epviews){
			System.err.println(epv);
			//analyse availability
			analyseAvailability(epv.getAvailability(), weekHist);
			
			//analyse performance
			analysePerformance(epv.getPerformance(), perfStats );
			
			//analyse interoperability
			analyseInteroperability(epv.getInteroperability(), interStats);
		}
		
		//update the index view
		updateAvailabilityStats(idx, weekHist);
		
		//update performance stats
		updatePerformanceStats(idx, perfStats);
		
		//update interoperability stats
		
		
		
		log.info("Updated view {}", idx);
		_dbm.update(idx);
		
		return idx;
	}

	private void analyseInteroperability(
			EPViewInteroperability interoperability,
			SummaryStatistics[] interStats) {
		
		boolean [] all  = new boolean[6];
		Arrays.fill(all, false);
				
		
		for(EPViewInteroperabilityData d : interoperability.getSPARQL1Features()){
			System.out.println("1:"+d.getLabel());
			String l = d.getLabel().toString();
			if(l.contains("fil") &&	l.contains("bnode") && 
				l.contains("empty")){
				all[sparql1_com] =all[sparql1_com]&& d.getValue();
			}
			else if(l.contains("ask") &&	l.contains("graph") && 
					l.contains("con[")&& l.contains("sel[from]")){
					all[sparql1_graph] =all[sparql1_graph]&& d.getValue();
				}
			else{
				all[sparql1_solMods] =all[sparql1_solMods]&& d.getValue();
			}
		}
		for(EPViewInteroperabilityData d : interoperability.getSPARQL11Features()){
			System.out.println("11"+ d.getLabel());
			String l = d.getLabel().toString();
			
			else if(l.contains("sel[fil")){
					all[sparql11_filter] =all[sparql11_filter]&& d.getValue();
				}
		}
	}

	private void analysePerformance(EPViewPerformance performance,
			SummaryStatistics[] perfStats) {
		update(performance.getAsk(), perfStats[askCold], perfStats[askWarm]);
		update(performance.getJoin(), perfStats[joinCold], perfStats[joinWarm]);
	}

	private void analyseAvailability(EPViewAvailability availability,
			Map<String, SimpleHistogram> weekHist) {
		for(Entry<CharSequence, Double> values: availability.getData().getValues().entrySet()){
			update(values, weekHist);
		}
	}

	private void updatePerformanceStats(Index idx, SummaryStatistics[] perfStats) {
		ArrayList<IndexViewPerformanceData> data = new ArrayList<IndexViewPerformanceData>();
		List<IndexViewPerformanceDataValues> l = new ArrayList<IndexViewPerformanceDataValues>();
		IndexViewPerformanceData cold = new IndexViewPerformanceData("Cold Tests", "#1f77b4",l);
		l = new ArrayList<IndexViewPerformanceDataValues>();
		IndexViewPerformanceData warm = new IndexViewPerformanceData("Warm Tests", "#2ca02c",l);
		data.add(cold);
		data.add(warm);
		
		cold.getData().add(new IndexViewPerformanceDataValues("Average ASK", perfStats[askCold].getMean()));
		cold.getData().add(new IndexViewPerformanceDataValues("Average JOIN", perfStats[joinCold].getMean()));
		
		warm.getData().add(new IndexViewPerformanceDataValues("Average ASK", perfStats[askWarm].getMean()));
		warm.getData().add(new IndexViewPerformanceDataValues("Average JOIN", perfStats[joinWarm].getMean()));
		
		idx.getPerformance().setThreshold(-1L);
		idx.getPerformance().setData(data);
		
	}

	private void updateAvailabilityStats(Index idx,
			Map<String, SimpleHistogram> weekHist) {
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
