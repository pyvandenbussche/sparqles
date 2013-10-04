package sparqles.analytics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.analytics.avro.AvailabilityIndex;
import sparqles.analytics.avro.EPView;
import sparqles.analytics.avro.Index;
import sparqles.core.CONSTANTS;
import sparqles.core.Task;
import sparqles.utils.MongoDBManager;
import sparqles.utils.cli.SPARQLES;

public class IndexViewAnalytics implements Task<Index>{

	private static final Logger log = LoggerFactory.getLogger(IndexViewAnalytics.class);
	
	private MongoDBManager _dbm;

	@Override
	public void execute() {
		Collection<Index> idxs = _dbm.get(Index.class, Index.SCHEMA$);
		Collection<EPView> epviews = _dbm.get(EPView.class, EPView.SCHEMA$);
		
		
		log.info("Found {} idx views and {} epviews", idxs.size(), epviews.size());
		
		
		Map< String, SimpleHistogram> weekHist = new HashMap<String, SimpleHistogram>();
		for(EPView epv: epviews){
			for(Entry<CharSequence, Double> values: epv.getAvailability().getData().getValues().entrySet()){
				update(values, weekHist);
			}
		}
		System.out.println("Updated values for epviews");
		
		Index idx=null;
		if(idxs.size()==0){
			idx = createIndex();
			_dbm.insert(idx);
		}else if(idxs.size()>1){
//			log.warn("Too many results");
		}else
			idx = idxs.iterator().next();
		
		List<AvailabilityIndex> aidxs = idx.getAvailability();

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
		
		log.info("Updated view {}", idx);
		_dbm.update(idx);
		
	}

	private Index createIndex() {
		Index idx = new Index();
		idx.setEndpoint(CONSTANTS.SPARQLES);
		List<AvailabilityIndex> aidxs = new ArrayList<AvailabilityIndex>();
		
		
		AvailabilityIndex aidx = new AvailabilityIndex("[0;5]",new HashMap<CharSequence, Double>());
		aidxs.add(aidx);
		
		aidx = new AvailabilityIndex("]5;90]",new HashMap<CharSequence, Double>());
		aidxs.add(aidx);
		
		aidx = new AvailabilityIndex("]90;95]",new HashMap<CharSequence, Double>());
		aidxs.add(aidx);
		
		aidx = new AvailabilityIndex("]95;100]",new HashMap<CharSequence, Double>());
		aidxs.add(aidx);
		
		idx.setAvailability(aidxs);
		
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
