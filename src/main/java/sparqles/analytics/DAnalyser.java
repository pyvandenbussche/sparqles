package sparqles.analytics;

import java.util.ArrayList;
import java.util.List;

import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.avro.Endpoint;
import sparqles.avro.analytics.DiscoverabilityView;
import sparqles.avro.analytics.EPView;
import sparqles.avro.analytics.EPViewDiscoverability;
import sparqles.avro.analytics.EPViewDiscoverabilityData;
import sparqles.avro.discovery.DGETInfo;
import sparqles.avro.discovery.DResult;
import sparqles.avro.discovery.QueryInfo;
import sparqles.core.discovery.DTask;
import sparqles.utils.MongoDBManager;

public class DAnalyser extends Analytics<DResult> {
	private static final Logger log = LoggerFactory.getLogger(DAnalyser.class);



	public DAnalyser(MongoDBManager db) {
		super(db);
	}

	@Override
	public boolean analyse(DResult pres) {
		log.info("[Analytics] {}",pres);

		Endpoint ep = pres.getEndpointResult().getEndpoint();

		DiscoverabilityView dview= getView(ep);
		EPView epview=getEPView(ep);


		List<EPViewDiscoverabilityData> lvoid = new ArrayList<EPViewDiscoverabilityData>();
		List<EPViewDiscoverabilityData> lsd = new ArrayList<EPViewDiscoverabilityData>();

		String serverName = "missing";
		for(DGETInfo info : pres.getDescriptionFiles()){
			if(info.getOperation().toString().equals(DTask.EPURL)){
				if(!info.getResponseServer().toString().equalsIgnoreCase("missing")){
					serverName = info.getResponseServer().toString();
				}

				EPViewDiscoverabilityData d = new EPViewDiscoverabilityData("HTTP Get", info.getVoiDpreds().size()!=0);
				lvoid.add(d);
				
				d = new EPViewDiscoverabilityData("HTTP Get", info.getSPARQLDESCpreds().size() != 0);
				lsd.add(d);
			}
			if(info.getOperation().toString().equalsIgnoreCase("wellknown")){
				if(!info.getResponseServer().toString().equalsIgnoreCase("missing")){
					serverName = info.getResponseServer().toString();
				}
				
				EPViewDiscoverabilityData d = new EPViewDiscoverabilityData("/.well-known/void", info.getVoiDpreds().size()!=0);
				lvoid.add(d);
				
				d = new EPViewDiscoverabilityData("/.well-known/void", info.getSPARQLDESCpreds().size() != 0);
				lsd.add(d);
			}
			if(info.getSPARQLDESCpreds().size() >0 ){
				dview.setSD(true);
			}
			if(info.getVoiDpreds().size() >0 ){
				dview.setVoID(true);
			}
		}
		log.info("Setting server name to {}",serverName);
		dview.setServerName(serverName);
		EPViewDiscoverability depview = epview.getDiscoverability();
		
		depview.setServerName(dview.getServerName());
		depview.setVoIDDescription(lvoid);
		
		for(QueryInfo info: pres.getQueryInfo()){

			if(info.getOperation().equals("query-self")){
				EPViewDiscoverabilityData d = new EPViewDiscoverabilityData("SPARQL Endpoint content", info.getResults().size()!=0);
				lvoid.add(d);
			}
		}

		//		
		//		

		
		//		d = new EPViewDiscoverabilityData("HTTP Get", pres.getGetResult().getSPARQLDESCterms()!=0);
		//		l.add(d);
		//		d = new EPViewDiscoverabilityData("SPARQL Endpoint content", pres.getVoidResult().getSPARQLFile().size()!=0);
		//		l.add(d);


		//		depview.setSDDescription(l);

		dview.setLastUpdate(pres.getEndpointResult().getEnd());

		_db.update(dview);
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


	private DiscoverabilityView getView(Endpoint ep) {
		DiscoverabilityView view =null;
		List<DiscoverabilityView> views = new ArrayList<DiscoverabilityView>();
		if (_db  != null){
			views = _db.getResults(ep,DiscoverabilityView.class, DiscoverabilityView.SCHEMA$);
		}
		if(views.size()!=1){
			Log.warn("We have {} FeatureView, expected was 1",views.size());
		}
		if(views.size()==0){
			view = new DiscoverabilityView();
			view.setEndpoint(ep);
			view.setSD(false);
			view.setVoID(false);
			view.setServerName("missing");
			view.setLastUpdate(-1L);
			if (_db  != null)
				_db.insert(view);
		}else{
			view = views.get(0);
		}
		return view;
	}
}