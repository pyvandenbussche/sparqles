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
import sparqles.analytics.avro.EPViewInteroperabilityData;
import sparqles.analytics.avro.EPViewPerformanceData;
import sparqles.analytics.avro.EPViewPerformanceDataValues;
import sparqles.analytics.avro.InteroperabilityView;
import sparqles.analytics.avro.PerformanceView;
import sparqles.core.Endpoint;
import sparqles.core.analytics.avro.EPViewPerformance;
import sparqles.core.features.FResult;
import sparqles.core.features.FRun;
import sparqles.core.features.FSingleResult;
import sparqles.core.features.SpecificFTask;
import sparqles.core.performance.PResult;
import sparqles.core.performance.PSingleResult;
import sparqles.core.performance.Run;
import sparqles.utils.MongoDBManager;

public class FAnalyser extends Analytics<FResult> {
	private static final Logger log = LoggerFactory.getLogger(FAnalyser.class);

	
	
	public FAnalyser(MongoDBManager db) {
		super(db);
	}


	@Override
	public boolean analyse(FResult pres) {
		log.info("[Analytics] {}",pres);
		
		Endpoint ep = pres.getEndpointResult().getEndpoint();
		
		InteroperabilityView fview= getView(ep);
		EPView epview=getEPView(ep);
		
		List<EPViewInteroperabilityData> sparql1Feat = new ArrayList<EPViewInteroperabilityData>();
		List<EPViewInteroperabilityData> sparql11Feat = new ArrayList<EPViewInteroperabilityData>();
		int SPARQL1=0, SPARQL11=0;
		for(Entry<CharSequence, FSingleResult> ent: pres.getResults().entrySet()){
//			System.out.println(ent.getKey()+" -> "+ent.getValue());
			String key = ent.getKey().toString();
			Run run = ent.getValue().getRun();
			String q = SpecificFTask.valueOf(key).toString().toLowerCase();
			
				if(key.startsWith("SPARQL1_")) {
					q = q.replaceAll("sparql10/", "").replace(".rq", "");
					EPViewInteroperabilityData t = new EPViewInteroperabilityData(q, false,run.getException());
					
					if(run.getException()==null){
						SPARQL1++;
						t.setValue(true);
					}
					sparql1Feat.add(t);
				}
				if(key.startsWith("SPARQL11_")) {SPARQL11++;
					q = q.replaceAll("sparql11/", "").replace(".rq", "");
					EPViewInteroperabilityData t = new EPViewInteroperabilityData(q, false,run.getException());
					
					if(run.getException()==null){
						SPARQL11++;
						t.setValue(true);
					}
					
					sparql11Feat.add(t);
				}
			
		}
		fview.setNbCompliantSPARQL1Features(SPARQL1);
		fview.setNbCompliantSPARQL11Features(SPARQL11);
		epview.getInteroperability().setSPARQL1Features(sparql1Feat);
		epview.getInteroperability().setSPARQL11Features(sparql11Feat);
		System.out.println(fview);
		System.out.println(epview);
		
		_db.update(fview);
		_db.update(epview);
		return true;
		
//		SummaryStatistics askStatsCold = new SummaryStatistics();
//		SummaryStatistics askStatsWarm = new SummaryStatistics();
//		SummaryStatistics joinStatsCold = new SummaryStatistics();
//		SummaryStatistics joinStatsWarm = new SummaryStatistics();
//
//		//prepare eppview data
//		EPViewPerformance eppview = epview.getPerformance();
//		EPViewPerformanceData askCold = new EPViewPerformanceData("Cold ASK Tests","#1f77b4", new ArrayList<EPViewPerformanceDataValues>());
//		EPViewPerformanceData askWarm = new EPViewPerformanceData("WARM ASK Tests","#2ca02c", new ArrayList<EPViewPerformanceDataValues>());
//		EPViewPerformanceData joinCold = new EPViewPerformanceData("Cold JOIN Tests","#1f77b4", new ArrayList<EPViewPerformanceDataValues>());
//		EPViewPerformanceData joinWarm = new EPViewPerformanceData("Warm JOIN Tests","#2ca02c", new ArrayList<EPViewPerformanceDataValues>());
//		
//		ArrayList<EPViewPerformanceData> askdata= new ArrayList<EPViewPerformanceData>();
//		askdata.add(askCold);
//		askdata.add(askWarm);
//		ArrayList<EPViewPerformanceData> joindata= new ArrayList<EPViewPerformanceData>();
//		joindata.add(joinCold);
//		joindata.add(joinWarm);
//		
//		eppview.setAsk(askdata);
//		eppview.setJoin(joindata);
//		
//		
//		Map<CharSequence, PSingleResult> map = pres.getResults();
//		int limit =0 ;
//		
//		for(Entry<CharSequence, PSingleResult> ent: map.entrySet()){
//			PSingleResult res = ent.getValue();
//			if(ent.getKey().toString().startsWith("ASK")){
//				askStatsCold.addValue(res.getCold().getClosetime()/(double)1000);
//				askStatsWarm.addValue(res.getWarm().getClosetime()/(double)1000);
//				
//				String key = ent.getKey().toString().replaceAll("ASK",	"").toLowerCase();
//				
//				
//				askCold.getData().add(new EPViewPerformanceDataValues(key,res.getCold().getClosetime()/(double)1000));
//				askWarm.getData().add(new EPViewPerformanceDataValues(key,res.getWarm().getClosetime()/(double)1000));
//			}else if(ent.getKey().toString().startsWith("JOIN")){
//				joinStatsCold.addValue(res.getCold().getClosetime()/(double)1000);
//				joinStatsWarm.addValue(res.getCold().getClosetime()/(double)1000);
//		
//				String key = ent.getKey().toString().replaceAll("JOIN",	"").toLowerCase();
//				
//				joinCold.getData().add(new EPViewPerformanceDataValues(key,res.getCold().getClosetime()/(double)1000));
//				joinWarm.getData().add(new EPViewPerformanceDataValues(key,res.getWarm().getClosetime()/(double)1000));
//			}else if(ent.getKey().toString().startsWith("LIMIT")){
//				int sol = res.getCold().getSolutions();
//				if(Math.max(limit, sol)==sol){
//					limit = sol;
//				}
//				sol = res.getWarm().getSolutions();
//				if(Math.max(limit, sol)==sol){
//					limit = sol;
//				}
//			}
//		}
//		
//		
//		//Update pview data
//		pview.setAskMeanCold(askStatsCold.getMean());
//		pview.setAskMeanWarm(askStatsWarm.getMean());
//		pview.setJoinMeanCold(joinStatsCold.getMean());
//		pview.setJoinMeanWarm(joinStatsWarm.getMean());
//		
//		
//		System.out.println(pview);
//		System.out.println(epview);
//		_db.update(pview);
//		_db.update(epview);
//		
//		return true;
	}


	private InteroperabilityView getView(Endpoint ep) {
		InteroperabilityView view =null;
		List<InteroperabilityView> views = _db.getResults(ep,InteroperabilityView.class, InteroperabilityView.SCHEMA$);
		if(views.size()!=1){
			Log.warn("We have {} FeatureView, expected was 1",views.size());
		}
		if(views.size()==0){
			view = new InteroperabilityView();
			view.setEndpoint(ep);
			view.setNbCompliantSPARQL11Features(-1);
			view.setNbCompliantSPARQL1Features(-1);
			_db.insert(view);

		}else{
			view = views.get(0);
		}
		return view;
	}
}