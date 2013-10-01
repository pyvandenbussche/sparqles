package sparqles.analytics;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.analytics.avro.AvailabilityView;
import sparqles.analytics.avro.EPView;
import sparqles.analytics.avro.EPViewAvailability;
import sparqles.analytics.avro.EPViewAvailabilityData;
import sparqles.core.Endpoint;
import sparqles.core.availability.AResult;
import sparqles.utils.MongoDBManager;

public class AAnalyser implements Analytics<AResult> {
	private static final Logger log = LoggerFactory.getLogger(AAnalyser.class);

	private MongoDBManager _db;

	public static DateCalculator _dates = new DateCalculator();
	public final static int LAST_HOUR=0;
	public final static int LAST_24HOURS=1;
	public final static int LAST_7DAYS=2;
	public final static int LAST_31DAYS=3;
	public final static int THIS_WEEK=4;


	public AAnalyser(MongoDBManager db) {
		_db = db;
		
	}

	static void setDateCalculator(DateCalculator calc){
		_dates = calc;
	}


	/**
	 * Computes the aggregated statistics for the Availability task
	 * @param ep
	 */
	public boolean analyse(AResult ares) {
		try{
		log.info("[ANALYSE] {}", ares);

		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(ares.getEndpointResult().getStart());
		log.debug("Start date: {}",now.getTime());
		
		Endpoint ep = ares.getEndpointResult().getEndpoint();
		Calendar [] dates = _dates.getDates(ares.getEndpointResult().getStart());

		//get the views
		AvailabilityView aview=getView(ep);
		EPView epview=getEPView(ep);

		
		// query mongodb for all AResults in the last 31 days 
		log.debug("Query for {}< - >={}", dates[LAST_31DAYS].getTime(), now.getTime());
		List<AResult> results = _db.getResultsSince(ep, AResult.class, AResult.SCHEMA$,  dates[LAST_31DAYS].getTimeInMillis(), now.getTimeInMillis());

		
		SummaryStatistics last24HoursStats = new SummaryStatistics();
		SummaryStatistics last7DaysStats = new SummaryStatistics();
		SummaryStatistics last31DaysStats = new SummaryStatistics();
		SummaryStatistics thisWeekStats = new SummaryStatistics();

//		long last = ares.getEndpointResult().getStart();
		
		for(AResult res: results){
			long start = res.getEndpointResult().getStart();
			Calendar next = Calendar.getInstance();
			next.setTimeInMillis(start);
			
			if(start > dates[LAST_24HOURS].getTimeInMillis()){
				update(last7DaysStats,res);
				log.debug("  {} -24h-> {}",next.getTime(), dates[LAST_24HOURS].getTime());
			}

			if(start > dates[LAST_7DAYS].getTimeInMillis()){
				update(last7DaysStats,res);
				log.debug("  {} -7d-> {}",next.getTime(), dates[LAST_7DAYS].getTime());
			}
			if(start > dates[LAST_31DAYS].getTimeInMillis()){
				update(last31DaysStats,res);
				log.debug("  {} -31d-> {}",next.getTime(), dates[LAST_31DAYS].getTime());
			}
			if(start > dates[THIS_WEEK].getTimeInMillis()){
				update(thisWeekStats,res);
				log.debug("  {} -week-> {}",next.getTime(), dates[THIS_WEEK].getTime());
			}
		}

		// Update the views 
		EPViewAvailability epav = epview.getAvailability();

		double last24HouerMean = 0;
		if(!Double.isNaN(last24HoursStats.getMean()))
			last24HouerMean=last24HoursStats.getMean(); 
		epav.setUptimeLast24h(last24HouerMean);
		aview.setUptimeLast24h(last24HouerMean);

		boolean upNow=ares.getIsAvailable();
		aview.setUpNow(upNow);
		epav.setUpNow(upNow);		

		double last7dayMean = 0;
		if(!Double.isNaN(last7DaysStats.getMean()))
			last7dayMean=last7DaysStats.getMean(); 
		aview.setUptimeLast7d(last7dayMean);
		epav.setUptimeLast7d(last7dayMean);

		double thisweek = 0D;
		if(!Double.isNaN(thisWeekStats.getMean())){
			thisweek = thisWeekStats.getMean();
		}
		epav.getData().getValues().put((CharSequence) (""+dates[THIS_WEEK].getTimeInMillis()), thisweek);
		epav.getData().setKey("Availability");

		double last31dayMean = 0;
		if(!Double.isNaN(last31DaysStats.getMean()))
			last31dayMean=last31DaysStats.getMean(); 
		epav.setUptimeLast31d(last31dayMean);

		int runs = epav.getTestRuns();
		Double mean = epav.getUptimeOverall();
		if(mean==null) mean=0D;
		SummaryStatistics s = new SummaryStatistics();
		if(upNow) mean+=1;
		epav.setTestRuns(runs+1);
		epav.setUptimeOverall(mean/(double)(runs+1));

		log.debug("  [AView] {}", aview);
		log.debug("  [EPView] {}", epview);

		boolean succ=false;
		if( runs ==0){
			succ=_db.insert(aview);
			succ=_db.insert(epview);
		}
		else{
			succ=_db.update(aview);
			succ=_db.update(epview);
		}
//		System.err.println("AView (after)="+aview);
//		System.err.println("EPView (after)="+epview);
		
		return succ;
		}catch(Exception e){
			log.warn("[EXEC] {}",e);
			
		}
		return false;
		
	}




	

	private void update(SummaryStatistics stats, AResult res) {
		if(res.getIsAvailable()){
			stats.addValue(1);
		}else
			stats.addValue(0);
	}

	private AvailabilityView getView(Endpoint ep) {
		AvailabilityView view =null;
		List<AvailabilityView> views = _db.getResults(ep,AvailabilityView.class, AvailabilityView.SCHEMA$);
		if(views.size()!=1){
			Log.warn("We have {} AvailabilityView, expected was 1",views.size());
		}
		if(views.size()==0){
			view = new AvailabilityView();
			view.setEndpoint(ep);

		}else{
			view = views.get(0);
		}
		return view;
	}

	private EPView getEPView(Endpoint ep) {
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
			data.setValues(new HashMap<CharSequence, Double>());
		}else{
			view = views.get(0);
		}
		return view;
	}

	
	
	
//	private Calendar[] getDates(long time) {
//		Calendar now = Calendar.getInstance();
//		now.setTimeInMillis(time);
//		
//		Calendar lastHour = (Calendar) now.clone();
//		lastHour.add(Calendar.HOUR, -1);
//		//testing
//		//lastHour.add(Calendar.MINUTE, -2);
//		
//		Calendar last24Hour = (Calendar) now.clone();
////		last24Hour.add(Calendar.HOUR, -24);
//		last24Hour.add(Calendar.MINUTE, -6);
//
//		Calendar last7Days = (Calendar) now.clone();
//		//	last7Days.add(Calendar.DAY_OF_YEAR, -7);
//		last7Days.add(Calendar.MINUTE, -12);
//
//
//		Calendar last31Days = (Calendar) now.clone();
//		//	last31Days.add(Calendar.DAY_OF_YEAR, -31);
//		last31Days.add(Calendar.MINUTE, -18);
//
//
//		Calendar thisweek = Calendar.getInstance();
//		//	thisweek.set(Calendar.YEAR, now.get(Calendar.YEAR));
//		//	thisweek.set(Calendar.WEEK_OF_YEAR, now.get(Calendar.WEEK_OF_YEAR));
//		thisweek.set(Calendar.YEAR, now.get(Calendar.YEAR));
//		thisweek.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR));
//		thisweek.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
//		thisweek.set(Calendar.MINUTE, (now.get(Calendar.MINUTE)/10)*10);
//		
//		
//
//		Calendar [] c = new Calendar[5];
//		c[LAST_HOUR]=lastHour;
//		c[LAST_24HOURS]=last24Hour;
//		c[LAST_7DAYS]= last7Days;
//		c[LAST_31DAYS] = last31Days;
//		c[THIS_WEEK] = thisweek;
////		System.out.println("[DATES] from "+now.getTime()+" last1h:"+lastHour.getTime()+" last24h:"+last24Hour.getTime());
////		System.out.println(thisweek.getTime());
//		return c;
//	}


}

/**
 * Calculates the necessary date timestamps for the analysis
 * @author umbrichj
 *
 */
class DateCalculator{
	Calendar[] getDates(long time) {
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(time);
		
		Calendar lastHour = (Calendar) now.clone();
		lastHour.add(Calendar.HOUR, -1);
		
		Calendar last24Hour = (Calendar) now.clone();
		last24Hour.add(Calendar.HOUR, -24);

		Calendar last7Days = (Calendar) now.clone();
		last7Days.add(Calendar.DAY_OF_YEAR, -7);


		Calendar last31Days = (Calendar) now.clone();
		last31Days.add(Calendar.DAY_OF_YEAR, -31);
		

		Calendar thisweek = Calendar.getInstance();
			thisweek.set(Calendar.YEAR, now.get(Calendar.YEAR));
			thisweek.set(Calendar.WEEK_OF_YEAR, now.get(Calendar.WEEK_OF_YEAR));

		Calendar [] c = new Calendar[5];
		c[AAnalyser.LAST_HOUR]=lastHour;
		c[AAnalyser.LAST_24HOURS]=last24Hour;
		c[AAnalyser.LAST_7DAYS]= last7Days;
		c[AAnalyser.LAST_31DAYS] = last31Days;
		c[AAnalyser.THIS_WEEK] = thisweek;
		return c;
	}
}