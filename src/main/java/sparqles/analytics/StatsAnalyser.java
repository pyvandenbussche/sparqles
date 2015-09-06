package sparqles.analytics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;

import sparqles.avro.Endpoint;
import sparqles.avro.EndpointResult;
import sparqles.avro.analytics.Index;
import sparqles.avro.availability.AResult;
import sparqles.avro.discovery.DResult;
import sparqles.avro.features.FResult;
import sparqles.avro.performance.PResult;
import sparqles.core.SPARQLESProperties;
import sparqles.core.Task;
import sparqles.utils.MongoDBManager;

public class StatsAnalyser  implements Task<Index> {
	static SimpleDateFormat yearweek = new SimpleDateFormat("YYYY-'W'ww");
	
	private MongoDBManager _dbm;
	@Override
	public Index call() throws Exception {
		
		analyse_p();
		analyse_a();
		analyse_f();
		analyse_d();
		
		return null;
		
				
		
		
	}

	private void analyse_a() {
		System.out.println("Analysing a");
		TreeMap<String, Map<String, Integer>> epWeekCount = new TreeMap<String, Map<String,Integer>>();
		
		TreeSet<String>weeks = new TreeSet<>();
		Iterator<AResult> iter =  _dbm.getIterator(AResult.class, AResult.SCHEMA$);
		int count=0;
		while(iter.hasNext()){
			count++;
			AResult p = iter.next();
			
			update(epWeekCount, weeks, p.getEndpointResult());
			
			if (count % 1000==0){System.out.println(count);}
		}
		
		printStats(epWeekCount, weeks, "a_runs.csv");
	}
	
	private void update(TreeMap<String, Map<String, Integer>> epWeekCount,
			
			TreeSet<String> weeks, EndpointResult epr) {
		Date cur = trim(new Date(epr.getStart() ), 0);
		String ep= epr.getEndpoint().getUri().toString();
		
		String yyyyww= yearweek.format(cur);
		weeks.add(yyyyww);
		
		
		Map<String, Integer> weekCount = epWeekCount.get(ep);
		if (weekCount == null){
			weekCount = new TreeMap<String, Integer>();
			epWeekCount.put(ep, weekCount);
		}
		Integer c = weekCount.get(yyyyww);
		if(c==null){c=0;}
		weekCount.put(yyyyww,c+1);
		
		
		
	}

	private void analyse_p() {
		
		System.out.println("Analysing p");
		TreeMap<String, Map<String, Integer>> epWeekCount = new TreeMap<String, Map<String,Integer>>();
		TreeSet<String>weeks = new TreeSet<>();
		Iterator<PResult> iter =  _dbm.getIterator(PResult.class, PResult.SCHEMA$);
		int count=0;
		while(iter.hasNext()){
			count++;
			PResult p = iter.next();
			
			update(epWeekCount, weeks, p.getEndpointResult());
			
			if (count % 10000==0){System.out.println(count);}
		}
		
		printStats(epWeekCount, weeks, "p_runs.csv");
	}
	
	private void analyse_d() {
		System.out.println("Analysing d");
		TreeMap<String, Map<String, Integer>> epWeekCount = new TreeMap<String, Map<String,Integer>>();
		TreeSet<String>weeks = new TreeSet<>();
		Iterator<DResult> iter =  _dbm.getIterator(DResult.class, DResult.SCHEMA$);
		int count=0;
		while(iter.hasNext()){
			count++;
			DResult p = iter.next();
			
			update(epWeekCount, weeks, p.getEndpointResult());
			
			if (count % 10000==0){System.out.println(count);}
		}
		
		printStats(epWeekCount, weeks, "d_runs.csv");
	}
	private void analyse_f() {
		System.out.println("Analysing f");
		TreeMap<String, Map<String, Integer>> epWeekCount = new TreeMap<String, Map<String,Integer>>();
		TreeSet<String>weeks = new TreeSet<>();
		Iterator<FResult> iter =  _dbm.getIterator(FResult.class, FResult.SCHEMA$);
		int count=0;
		while(iter.hasNext()){
			count++;
			FResult p = iter.next();
			
			update(epWeekCount, weeks, p.getEndpointResult());
			
			if (count % 10000==0){System.out.println(count);}
		}
		
		printStats(epWeekCount, weeks, "f_runs.csv");
	}

	private void printStats(TreeMap<String, Map<String, Integer>> epWeekCount,
			TreeSet<String> weeks, String fName) {
		System.out.println("Printing stats to "+fName);
		PrintWriter fw;
		try {
			fw = new PrintWriter(new File(fName));
			fw.print("#ep, empty, min, mean, max");
			for(String yyyyww: weeks){
				fw.print(","+yyyyww);
			}
			fw.println();
			SummaryStatistics s = new SummaryStatistics();
			
			for (Entry<String, Map<String, Integer>> ent: epWeekCount.entrySet()){
				fw.print(ent.getKey());
				int empty=0;
				
				for(String yyyyww: weeks){
					if(ent.getValue().containsKey(yyyyww)){
						s.addValue(ent.getValue().get(yyyyww));
					}else{s.addValue(0); empty++;}
				}
				fw.print(" ,"+empty);
				fw.print(" ,"+s.getMin());
				fw.print(" ,"+s.getMean());
				fw.print(" ,"+s.getMax());
				
				for(String yyyyww: weeks){
					if(ent.getValue().containsKey(yyyyww)){
						fw.print(" ,"+ent.getValue().get(yyyyww));
					}else{
						fw.print(" ,0");
					}
				}
				fw.println();
			}
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void setDBManager(MongoDBManager dbm) {
		_dbm = dbm;
	}
	
	public static Date trim(Date date, int hours) {
		final GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY,hours);
        return calendar.getTime();
    }
	
	
	public static void main(String[] args) {
		SPARQLESProperties.init(new File("src/test/resources/sparqles.properties"));
		MongoDBManager m = new MongoDBManager();
		
		StatsAnalyser s = new StatsAnalyser();
		s.setDBManager(m);
		
		try {
			s.call();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
