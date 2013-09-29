package sparqles.analytics;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.mortbay.log.Log;


import sparqles.analytics.avro.AvailabilityView;
import sparqles.analytics.avro.EPView;
import sparqles.analytics.avro.EPViewAvailability;
import sparqles.analytics.avro.EPViewAvailabilityData;
import sparqles.core.Endpoint;
import sparqles.core.availability.AResult;

import sparqles.utils.MongoDBManager;

public class AAnalyser {


	private MongoDBManager _db;
	private static int LAST_24HOURS=0;
	private static int LAST_HOUR=1;
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
	public void analyse(AResult ares) {

		Endpoint ep = ares.getEndpointResult().getEndpoint();
		//for availability main view
		AvailabilityView aview=getView(ep);
		Calendar [] dates = getDates();

		List<AResult> results = _db.getResultsSince(ep, AResult.class, AResult.SCHEMA$, dates[LAST_31DAYS].getTimeInMillis());

		EPView epview=getEPView(ep);


		SummaryStatistics last24HoursStats = new SummaryStatistics();
		SummaryStatistics last7DaysStats = new SummaryStatistics();
		SummaryStatistics last31DaysStats = new SummaryStatistics();
		SummaryStatistics thisWeekStats = new SummaryStatistics();
		

		long last = ares.getEndpointResult().getStart();
		boolean upNow=ares.getIsAvailable();
		for(AResult res: results){
			long start = res.getEndpointResult().getStart();
			
			if(start > dates[LAST_24HOURS].getTimeInMillis()){
				update(last7DaysStats,res);
			}

			if(start > dates[LAST_7DAYS].getTimeInMillis()){
				update(last7DaysStats,res);
			}
			if(start > dates[LAST_31DAYS].getTimeInMillis()){
				update(last31DaysStats,res);
			}
			if(start > dates[THIS_WEEK].getTimeInMillis()){
				update(thisWeekStats,res);
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
		System.out.println((CharSequence) (""+dates[THIS_WEEK].getTimeInMillis())+","+ thisweek);
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
		
		
		
		_db.insert(aview);
		_db.insert(epview);
	}



private long getLastWeek(EPView epview) {
	long last =0;
	for(CharSequence l:	epview.getAvailability().getData().getValues().keySet()){
		long s = Long.valueOf(l.toString());
		if(Math.max(last,s )!= last){
			last = s;
		}
	}
	return last;

}

private Calendar[] getDates() {



	Calendar now = Calendar.getInstance();
	now.setTimeInMillis(1380470280144L);
	//		now.set(Calendar.YEAR, 2013);
	//		now.set(Calendar.MONTH, Calendar.AUGUST);
	//		now.set(Calendar.DAY_OF_MONTH, 29);
	//		now.set(Calendar.HOUR, 17);	

	Calendar last24Hour = (Calendar) now.clone();
	last24Hour.add(Calendar.HOUR, -24);

	Calendar lastHour = (Calendar) now.clone();
	lastHour.add(Calendar.HOUR, -1);

	Calendar last7Days = (Calendar) now.clone();
	last7Days.add(Calendar.DAY_OF_YEAR, -7);

	Calendar last31Days = (Calendar) now.clone();
	last31Days.add(Calendar.DAY_OF_YEAR, -31);


	Calendar thisweek = Calendar.getInstance();
	thisweek.set(Calendar.YEAR, now.get(Calendar.YEAR));
	thisweek.set(Calendar.WEEK_OF_YEAR, now.get(Calendar.WEEK_OF_YEAR));


	Calendar [] c = {last24Hour, lastHour, last7Days, last31Days, thisweek};
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
