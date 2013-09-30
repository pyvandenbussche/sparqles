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
	private static int LAST_HOUR=0;
	private static int LAST_24HOURS=1;
	private static int LAST_7DAYS=2;
	private static int LAST_31DAYS=3;
	private static int THIS_WEEK=4;


	public AAnalyser(MongoDBManager db) {
		_db = db;
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
		log.info("Start date: {}",now.getTime());
		
		Endpoint ep = ares.getEndpointResult().getEndpoint();
		Calendar [] dates = getDates(ares.getEndpointResult().getStart());

		//for availability main view
		AvailabilityView aview=getView(ep);
		EPView epview=getEPView(ep);

		
//		System.err.println("AView (before)="+aview);
//		System.err.println("EPView (before)="+epview);
		
		log.info("Query for {}< - >={}", dates[LAST_31DAYS].getTime(), now.getTime());
		List<AResult> results = _db.getResultsSince(ep, AResult.class, AResult.SCHEMA$,  dates[LAST_31DAYS].getTimeInMillis(), now.getTimeInMillis());

		
		SummaryStatistics last24HoursStats = new SummaryStatistics();
		SummaryStatistics last7DaysStats = new SummaryStatistics();
		SummaryStatistics last31DaysStats = new SummaryStatistics();
		SummaryStatistics thisWeekStats = new SummaryStatistics();

		long last = ares.getEndpointResult().getStart();
		boolean upNow=ares.getIsAvailable();
		for(AResult res: results){
			long start = res.getEndpointResult().getStart();
			Calendar next = Calendar.getInstance();
			next.setTimeInMillis(start);
			System.out.println(now.getTime() +" Vs. "+next.getTime());
			
			if(start > dates[LAST_24HOURS].getTimeInMillis()){
				update(last7DaysStats,res);
				log.info("  {} -24h-> {}",next.getTime(), dates[LAST_24HOURS].getTime());
			}

			if(start > dates[LAST_7DAYS].getTimeInMillis()){
				update(last7DaysStats,res);
				log.info("  {} -7d-> {}",next.getTime(), dates[LAST_7DAYS].getTime());
			}
			if(start > dates[LAST_31DAYS].getTimeInMillis()){
				update(last31DaysStats,res);
				log.info("  {} -31d-> {}",next.getTime(), dates[LAST_31DAYS].getTime());
			}
			if(start > dates[THIS_WEEK].getTimeInMillis()){
				update(thisWeekStats,res);
				log.info("  {} -week-> {}",next.getTime(), dates[THIS_WEEK].getTime());
			}
		}

		EPViewAvailability epav = epview.getAvailability();

		double last24HouerMean = 0;

		if(!Double.isNaN(last24HoursStats.getMean()))
			last24HouerMean=last24HoursStats.getMean(); 

		epav.setUptimeLast24h(last24HouerMean);
		aview.setUptimeLast24h(last24HouerMean);

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
		if(upNow)
			mean+=1;

		epav.setTestRuns(runs+1);
		epav.setUptimeOverall(mean/(double)(runs+1));

		log.info("  [AView] {}", aview);
		log.info("  [EPView] {}", epview);

		boolean succ=false;
		if( runs ==0){
			succ=_db.insert(aview);
			succ=_db.insert(epview);
		}
		else{
			succ=_db.update(aview);
			succ=_db.update(epview);
		}
		System.err.println("AView (after)="+aview);
		System.err.println("EPView (after)="+epview);
		
		return succ;
		}catch(Exception e){
			log.warn("[EXEC] {}",e);
			
		}
		return false;
		
	}




	private Calendar[] getDates(long time) {
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(time);
		
		Calendar lastHour = (Calendar) now.clone();
		//	lastHour.add(Calendar.HOUR, -1);
		lastHour.add(Calendar.MINUTE, -2);
		
		Calendar last24Hour = (Calendar) now.clone();
//		last24Hour.add(Calendar.HOUR, -24);
		last24Hour.add(Calendar.MINUTE, -6);

		Calendar last7Days = (Calendar) now.clone();
		//	last7Days.add(Calendar.DAY_OF_YEAR, -7);
		last7Days.add(Calendar.MINUTE, -12);


		Calendar last31Days = (Calendar) now.clone();
		//	last31Days.add(Calendar.DAY_OF_YEAR, -31);
		last31Days.add(Calendar.MINUTE, -18);


		Calendar thisweek = Calendar.getInstance();
		//	thisweek.set(Calendar.YEAR, now.get(Calendar.YEAR));
		//	thisweek.set(Calendar.WEEK_OF_YEAR, now.get(Calendar.WEEK_OF_YEAR));
		thisweek.set(Calendar.YEAR, now.get(Calendar.YEAR));
		thisweek.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR));
		thisweek.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
		thisweek.set(Calendar.MINUTE, (now.get(Calendar.MINUTE)/10)*10);
		
		

		Calendar [] c = new Calendar[5];
		c[LAST_HOUR]=lastHour;
		c[LAST_24HOURS]=last24Hour;
		c[LAST_7DAYS]= last7Days;
		c[LAST_31DAYS] = last31Days;
		c[THIS_WEEK] = thisweek;
		System.out.println("[DATES] from "+now.getTime()+" last1h:"+lastHour.getTime()+" last24h:"+last24Hour.getTime());
		System.out.println(thisweek.getTime());
		return c;
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
			Log.warn("We have {} results, expected was 1",views.size());
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
			Log.warn("We have {} results, expected was 1",views.size());
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



}
