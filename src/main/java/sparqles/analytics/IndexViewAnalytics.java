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
import sparqles.analytics.avro.EPViewAvailabilityDataPoint;
import sparqles.analytics.avro.EPViewInteroperabilityData;
import sparqles.analytics.avro.EPViewPerformanceData;
import sparqles.analytics.avro.EPViewPerformanceDataValues;
import sparqles.analytics.avro.Index;
import sparqles.analytics.avro.IndexAvailabilityDataPoint;
import sparqles.analytics.avro.IndexViewDiscoverabilityData;
import sparqles.analytics.avro.IndexViewDiscoverabilityDataValues;
import sparqles.analytics.avro.IndexViewInterData;
import sparqles.analytics.avro.IndexViewInterDataValues;
import sparqles.analytics.avro.IndexViewPerformanceData;
import sparqles.analytics.avro.IndexViewPerformanceDataValues;
import sparqles.core.CONSTANTS;
import sparqles.core.Task;
import sparqles.core.analytics.avro.EPViewDiscoverability;
import sparqles.core.analytics.avro.EPViewInteroperability;
import sparqles.core.analytics.avro.EPViewPerformance;
import sparqles.core.analytics.avro.IndexViewDiscoverability;
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

		SimpleHistogram [] interStats = new SimpleHistogram[6];
		Arrays.fill(interStats, new SimpleHistogram());

		

		Count [] discoStats = {	new Count<String>(),
				new Count<String>()};

		
		
		//iterate over all epviews and analyse them
		for(EPView epv: epviews){
			System.err.println(epv);
			//analyse availability
			analyseAvailability(epv.getAvailability(), weekHist);

			//analyse performance
			analysePerformance(epv.getPerformance(), perfStats );

			//analyse interoperability
			analyseInteroperability(epv.getInteroperability(), interStats);
			
			//analyse interoperability
			analyseDiscoverability(epv.getDiscoverability(), discoStats);
		}

		//update the index view
		updateAvailabilityStats(idx, weekHist);

		//update performance stats
		updatePerformanceStats(idx, perfStats);

		//update interoperability stats
		updateInteroperability(idx, interStats);
		
		updateDiscoverability(idx, discoStats);


		log.info("Updated view {}", idx);
		_dbm.update(idx);

		return idx;
	}

	private void analyseDiscoverability(EPViewDiscoverability discoverability,
			Count<String>[] discoStats) {
		discoStats[1].add(discoverability.getServerName().toString());
			
		if(discoverability.getSDDescription().size()!=0){
			discoStats[0].add("sd");
		}
		if(discoverability.getVoIDDescription().size()!=0){
			discoStats[0].add("void");
		}
		if( discoverability.getSDDescription().size()==0 &&
			discoverability.getVoIDDescription().size()==0)
			discoStats[0].add("no");
	}

	private void updateDiscoverability(Index idx, Count[] object) {
		
		IndexViewDiscoverability iv = idx.getDiscoverability();
		Count<String> server = object[1];
		
		List<IndexViewDiscoverabilityData> l = new ArrayList<IndexViewDiscoverabilityData>();
		List<IndexViewDiscoverabilityDataValues> lv = new ArrayList<IndexViewDiscoverabilityDataValues>();
		for(String k: server.keySet()){
			lv.add(
			new IndexViewDiscoverabilityDataValues(k, server.get(k)/(double)server.getTotal()));
		}
		l.add(new IndexViewDiscoverabilityData("Server Names", lv));
		iv.setServerName(l);
		
		Count<String> stats = object[0];
		int v =stats.get("no");
		iv.setNoDescription(v/(double)stats.getTotal());
		v =stats.get("sd");
		iv.setSDDescription(v/(double)stats.getTotal());
		v = stats.get("void");
		iv.setVoIDDescription(v/(double)stats.getTotal());
		
		
		
	}

	private void updateInteroperability(Index idx, SimpleHistogram[] interStats) {
		IndexViewInteroperability iv = idx.getInteroperability();

		List<IndexViewInterData> v = new ArrayList<IndexViewInterData>();
		iv.setData(v);
		
		v.add(updateSPARQL1(interStats));
		v.add(updateSPARQL11(interStats));
	}

	private IndexViewInterData updateSPARQL11(SimpleHistogram[] interStats) {
		IndexViewInterData ivd = new IndexViewInterData();
		ivd.setColor("#2ca02c");
		ivd.setKey("SPARQL 1.1");
		
		ArrayList<IndexViewInterDataValues> v = new ArrayList<IndexViewInterDataValues>();
		//sparql1 mod
		double perc = interStats[sparql11_agg].bin[3]/
				(double)interStats[sparql11_agg].sampleSize;
		v.add( new IndexViewInterDataValues(
				"Aggregate",perc) );

		//sparql1 com
		perc = interStats[sparql11_filter].bin[3]/
				(double)interStats[sparql11_filter].sampleSize;
		v.add( new IndexViewInterDataValues(
				"Filter",perc) );

		//sparql1 graph
		perc = interStats[sparql11_other].bin[3]/
				(double)interStats[sparql11_other].sampleSize;
		v.add( new IndexViewInterDataValues(
				"Other",perc) );
		
		ivd.setData(v);
		
		return ivd;

	}

	private IndexViewInterData updateSPARQL1(SimpleHistogram[] interStats) {
		IndexViewInterData ivd = new IndexViewInterData();
		ivd.setColor("#1f77b4");
		ivd.setKey("SPARQL 1.0");
		
		ArrayList<IndexViewInterDataValues> v = new ArrayList<IndexViewInterDataValues>();
		//sparql1 mod
		double perc = interStats[sparql1_solMods].bin[3]/
				(double)interStats[sparql1_solMods].sampleSize;
		v.add( new IndexViewInterDataValues(
				"Solution Modifiers",perc) );

		//sparql1 com
		perc = interStats[sparql1_com].bin[3]/
				(double)interStats[sparql1_com].sampleSize;
		v.add( new IndexViewInterDataValues(
				"Common Operators and Filters",perc) );

		//sparql1 graph
		perc = interStats[sparql1_graph].bin[3]/
				(double)interStats[sparql1_graph].sampleSize;
		v.add( new IndexViewInterDataValues(
				"Graph and other",perc) );
		
		ivd.setData(v);
		
		return ivd;

	}

	private void analyseInteroperability(
			EPViewInteroperability interoperability,
			SimpleHistogram[] interStats) {
		boolean [] all  = new boolean[6];
		Arrays.fill(all, true);

	
		for(EPViewInteroperabilityData d : interoperability.getSPARQL1Features()){
			System.out.println("1:"+d.getLabel());
			String l = d.getLabel().toString();

			boolean bv= d.getValue();
			/*
			SEL[.]*ORDERBY*OFFSET
			SEL[.]*ORDERBY-ASC
			SEL[.]*ORDERBY-DESC
			SEL[.]*ORDERBY
			SEL-DISTINCT[.]
			SEL-REDUCED[.]
			 */
			if( l.contains("orderby") || l.contains("distinct") ||
					l.contains("reduced") )
			{
				all[sparql1_solMods] =all[sparql1_solMods] && bv;	
			}		

			/*
			SEL[.]
			SEL[JOIN]
			SEL[OPT]
			SEL[UNION]
			-- matches fil --
			SEL[FIL(!BOUND)]
			SEL[FIL(BLANK)]
			SEL[FIL(BOOL)]
			SEL[FIL(IRI)]
			SEL[FIL(NUM)]
			SEL[FIL(REGEX)]
			SEL[FIL(REGEX-i)]
			SEL[FIL(STR)]
			SEL[BNODE]  -> bnode
			SEL[EMPTY]  -> empty
			 */
			else if( l.contains("fil") || l.contains("bnode") ||
					l.contains("empty") || l.contains("union") ||
					l.contains("opt") || l.contains("join") ||
					l.contains("sel[.]") )
			{

				all[sparql1_com] =all[sparql1_com]&& bv;

			}
			/*
			SEL[FROM]
			SEL[GRAPH]
			SEL[GRAPH;JOIN]
			SEL[GRAPH;UNION]
			CON[.]
			CON[JOIN]
			CON[OPT]
			ASK[.]
			 */
			else if( l.contains("graph") || l.contains("con") ||
					l.contains("ask") || l.contains("from") )
			{
				all[sparql1_graph] =all[sparql1_graph]&& bv;
			}
			else{
				log.info("Could not match {}",l);
			}
		}
		for(EPViewInteroperabilityData d : interoperability.getSPARQL11Features()){
			System.out.println("11"+ d.getLabel());
			String l = d.getLabel().toString();
			boolean bv = d.getValue();
			/*
			Aggregate
			SEL[AVG]*GROUPBY
			SEL[AVG]
			SEL[COUNT]*GROUPBY
			SEL[MAX]
			SEL[MIN]
			SEL[MINUS]
			SEL[SUM]
			 */
			if( l.contains("avg") || l.contains("count") ||
					l.contains("max") || l.contains("min") ||
					l.contains("minus") || l.contains("sum") )
			{
				all[sparql11_agg] =all[sparql11_agg] && bv;	
			}	
			/*
			Filter
			SEL[FIL(!EXISTS)]
			SEL[FIL(ABS)]
			SEL[FIL(CONTAINS)]
			SEL[FIL(EXISTS)]
			SEL[FIL(START)]
			 */
			else if( l.contains("fil") || l.contains("distinct") ||
					l.contains("reduced") )
			{
				all[sparql11_filter] =all[sparql11_filter] && bv;	
			}	
			/*
			Other
			ASK[FIL(!IN)]
			CON-[.]
			SEL[BIND]
			SEL[PATHS]
			SEL[SERVICE]
			SEL[SUBQ]
			SEL[SUBQ;GRAPH]
			SEL[VALUES]
			 */	
			else if( l.contains("ask") || l.contains("con") ||
					l.contains("bind") || l.contains("paths") || 
					l.contains("service") || l.contains("subq") ||
					l.contains("values") )
			{
				all[sparql11_other] =all[sparql11_other] && bv;	
			}	
			else{
				log.info("Could not match {}",l);
			}
		}
		
		boolean update = false;
		for(int i=0; i < all.length;i++){
			update= update || all[i];
		}
		
		if(update){
			for(int i=0; i < all.length;i++){
				if(all[i]) interStats[i].add(1D);
				else interStats[i].add(0D);
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
		for(EPViewAvailabilityDataPoint value: availability.getData().getValues()){
			update(value, weekHist);
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
		
		double v =  (perfStats[askCold].getN()==0) ? -1D : perfStats[askCold].getMean(); 
		cold.getData().add(new IndexViewPerformanceDataValues("Average ASK", v));
		v =  (perfStats[joinCold].getN()==0) ? -1D : perfStats[joinCold].getMean(); 
		cold.getData().add(new IndexViewPerformanceDataValues("Average JOIN", v));

		v =  (perfStats[askWarm].getN()==0) ? -1D : perfStats[askWarm].getMean(); 
		warm.getData().add(new IndexViewPerformanceDataValues("Average ASK", v));
		v =  (perfStats[joinWarm].getN()==0) ? -1D : perfStats[joinWarm].getMean(); 
		warm.getData().add(new IndexViewPerformanceDataValues("Average JOIN", v));

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

				
				
				aidx.getValues().add(new IndexAvailabilityDataPoint(week.getKey(), value/(double)total));
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




		AvailabilityIndex aidx = new AvailabilityIndex("[0;5]", new ArrayList<IndexAvailabilityDataPoint>());
		List<AvailabilityIndex> aidxs = new ArrayList<AvailabilityIndex>();

		aidxs.add(aidx);

		aidx = new AvailabilityIndex("]5;90]",new ArrayList<IndexAvailabilityDataPoint>());
		aidxs.add(aidx);

		aidx = new AvailabilityIndex("]90;95]",new ArrayList<IndexAvailabilityDataPoint>());
		aidxs.add(aidx);

		aidx = new AvailabilityIndex("]95;100]",new ArrayList<IndexAvailabilityDataPoint>());
		aidxs.add(aidx);

		idx.setAvailability(aidxs);

		IndexViewPerformance idxp = new IndexViewPerformance();
		idxp.setThreshold(-1L);
		idxp.setData(new ArrayList<IndexViewPerformanceData>());
		idx.setPerformance(idxp);

		IndexViewInteroperability idxi = new IndexViewInteroperability();
		idxi.setData(new ArrayList<IndexViewInterData>());
		idx.setInteroperability(idxi);

		IndexViewDiscoverability idxd = new IndexViewDiscoverability();
		idxd.setNoDescription(-1D);
		idxd.setSDDescription(-1D);
		idxd.setVoIDDescription(-1D);
		idxd.setServerName(new ArrayList<IndexViewDiscoverabilityData>());
		idx.setDiscoverability(idxd);
		
		return idx;
	}

	private void update(EPViewAvailabilityDataPoint value,
			Map<String, SimpleHistogram> weekHist) {
		String key = ""+value.getX();
		SimpleHistogram sh = weekHist.get(key);
		if(sh==null){ sh = new SimpleHistogram();
			weekHist.put(key, sh);
		}
		sh.add(value.getY());
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
	
	class Count<T> extends HashMap<T, Integer>{
		int sampleSize=0;
		
		public void add(T t){
			if(this.containsKey(t)){
				this.put(t, this.get(t)+1);
			}else
				this.put(t, 1);
			
			sampleSize++;
		}

		public double getTotal() {
			// TODO Auto-generated method stub
			return sampleSize;
		}
	}
}
