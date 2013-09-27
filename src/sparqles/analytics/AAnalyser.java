package sparqles.analytics;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import sparqles.core.Endpoint;
import sparqles.core.availability.AResult;
import sparqles.utils.DBManager;

public class AAnalyser {

	
	private DBManager _db;


	public AAnalyser(DBManager db) {
		_db = db;
	}
	
	
	public void analyse(Endpoint ep) {
		
		
		Calendar now = Calendar.getInstance();
		now.set(Calendar.YEAR, 2013);
		now.set(Calendar.MONTH, Calendar.AUGUST);
		now.set(Calendar.DAY_OF_MONTH, 29);
		now.set(Calendar.HOUR, 17);				
		 
		
//		Thu Aug 29 17:00:00 BST 2013
		Calendar lastHour = (Calendar) now.clone();
		lastHour.add(Calendar.HOUR, -1);
		List<AResult> last = _db.getResultsSince(ep, AResult.class, lastHour.getTimeInMillis());
		System.out.println("===== LAST ====");
		boolean upNow=false;
		if(last.size()!=1) System.err.println("Hmm we should only have one result");
		for( AResult a : last){
			System.out.println(a);
			upNow= a.getIsAvailable();
//			System.out.println(new Date(a.getEndpointResult().getStart()));
		}
		
		
		Calendar lastDay = (Calendar) now.clone();
		lastDay.add(Calendar.DAY_OF_MONTH, -1);
		List<AResult> lastday = _db.getResultsSince(ep, AResult.class, lastDay.getTimeInMillis());
		
		System.out.println("===== LAST DAY ====");
		int online=0, offline=0;
		for( AResult a : lastday){
//			System.out.println(a);
			if(a.getIsAvailable()) online++;
			else offline++;
		}
		double lastDayRate = (online)/(double)(online+offline);
		
		
		Calendar lastWeek = (Calendar) now.clone();
		lastWeek.add(Calendar.DAY_OF_MONTH, -7);
		
		List<AResult> lastweek = _db.getResultsSince(ep, AResult.class, lastWeek.getTimeInMillis());
		
		System.out.println("===== LAST WEEK====");
		online=0; offline=0;
		for( AResult a : lastweek){
//			System.out.println(a);
//			System.out.println(new Date(a.getEndpointResult().getStart()));
			if(a.getIsAvailable()) online++;
			else offline++;
		}
		double lastWeekRate = (online)/(double)(online+offline);
		
		
		System.out.println("upNow:"+upNow+" uptimeLast24h:"+lastDayRate+" uptimeLast7d:"+lastWeekRate);

	}
	
}
