package sparqles.analytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.analytics.avro.EPView;
import sparqles.analytics.avro.EPViewPerformanceData;
import sparqles.analytics.avro.EPViewPerformanceDataValues;
import sparqles.analytics.avro.PerformanceView;
import sparqles.core.Endpoint;
import sparqles.core.analytics.avro.EPViewPerformance;
import sparqles.core.performance.PResult;
import sparqles.core.performance.PSingleResult;
import sparqles.utils.MongoDBManager;

public class PAnalyser extends Analytics<PResult> {
	private static final Logger log = LoggerFactory.getLogger(PAnalyser.class);

	
	
	public PAnalyser(MongoDBManager db) {
		super(db);
	}


	@Override
	public boolean analyse(PResult pres) {
		log.info("[Analytics] {}",pres);
		
		Endpoint ep = pres.getEndpointResult().getEndpoint();
		
		PerformanceView pview=getView(ep);
		EPView epview=getEPView(ep);
		
		SummaryStatistics askStatsCold = new SummaryStatistics();
		SummaryStatistics askStatsWarm = new SummaryStatistics();
		SummaryStatistics joinStatsCold = new SummaryStatistics();
		SummaryStatistics joinStatsWarm = new SummaryStatistics();

		//prepare eppview data
		EPViewPerformance eppview = epview.getPerformance();
		EPViewPerformanceData askCold = new EPViewPerformanceData("Cold ASK Tests","#1f77b4", new ArrayList<EPViewPerformanceDataValues>());
		EPViewPerformanceData askWarm = new EPViewPerformanceData("WARM ASK Tests","#2ca02c", new ArrayList<EPViewPerformanceDataValues>());
		EPViewPerformanceData joinCold = new EPViewPerformanceData("Cold JOIN Tests","#1f77b4", new ArrayList<EPViewPerformanceDataValues>());
		EPViewPerformanceData joinWarm = new EPViewPerformanceData("Warm JOIN Tests","#2ca02c", new ArrayList<EPViewPerformanceDataValues>());
		
		ArrayList<EPViewPerformanceData> askdata= new ArrayList<EPViewPerformanceData>();
		askdata.add(askCold);
		askdata.add(askWarm);
		ArrayList<EPViewPerformanceData> joindata= new ArrayList<EPViewPerformanceData>();
		joindata.add(joinCold);
		joindata.add(joinWarm);
		
		eppview.setAsk(askdata);
		eppview.setJoin(joindata);
				
		Map<CharSequence, PSingleResult> map = pres.getResults();
		long limit =0 ;
		
		for(Entry<CharSequence, PSingleResult> ent: map.entrySet()){
			PSingleResult res = ent.getValue();
			if(ent.getKey().toString().startsWith("ASK")){
				askStatsCold.addValue(res.getCold().getClosetime()/(double)1000);
				askStatsWarm.addValue(res.getWarm().getClosetime()/(double)1000);
				
				String key = ent.getKey().toString().replaceAll("ASK",	"").toLowerCase();
				
				
				askCold.getData().add(new EPViewPerformanceDataValues(key,res.getCold().getClosetime()/(double)1000,res.getCold().getException()));
				askWarm.getData().add(new EPViewPerformanceDataValues(key,res.getWarm().getClosetime()/(double)1000,res.getWarm().getException()));
			}else if(ent.getKey().toString().startsWith("JOIN")){
				joinStatsCold.addValue(res.getCold().getClosetime()/(double)1000);
				joinStatsWarm.addValue(res.getCold().getClosetime()/(double)1000);
		
				String key = ent.getKey().toString().replaceAll("JOIN",	"").toLowerCase();
				
				joinCold.getData().add(new EPViewPerformanceDataValues(key,res.getCold().getClosetime()/(double)1000,res.getCold().getException()));
				joinWarm.getData().add(new EPViewPerformanceDataValues(key,res.getWarm().getClosetime()/(double)1000,res.getWarm().getException()));
			}else if(ent.getKey().toString().startsWith("LIMIT")){
				int sol = res.getCold().getSolutions();
				if(Math.max(limit, sol)==sol){
					limit = sol;
				}
				sol = res.getWarm().getSolutions();
				if(Math.max(limit, sol)==sol){
					limit = sol;
				}
			}
		}
		eppview.setThreshold(limit);
		
		
		
		//Update pview data
		pview.setAskMeanCold(checkForNAN(askStatsCold.getMean()));
		pview.setAskMeanWarm(checkForNAN(askStatsWarm.getMean()));
		pview.setJoinMeanCold(checkForNAN(joinStatsCold.getMean()));
		pview.setJoinMeanWarm(checkForNAN(joinStatsWarm.getMean()));
		
		
		System.out.println(pview);
		System.out.println(epview);
		_db.update(pview);
		_db.update(epview);
		
		return true;
	}


	private Double checkForNAN(double mean) {
		if (Double.isNaN(mean)){
			return -1D;
		}
		return mean;
	}


	private PerformanceView getView(Endpoint ep) {
		PerformanceView view =null;
		List<PerformanceView> views = _db.getResults(ep,PerformanceView.class, PerformanceView.SCHEMA$);
		if(views.size()!=1){
			Log.warn("We have {} AvailabilityView, expected was 1",views.size());
		}
		if(views.size()==0){
			view = new PerformanceView();
			view.setEndpoint(ep);
			_db.insert(view);

		}else{
			view = views.get(0);
		}
		return view;
	}
}