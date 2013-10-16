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
			String key = ent.getKey().toString();
			Run run = ent.getValue().getRun();
		
			String q = SpecificFTask.valueOf(key).toString().toLowerCase();
			if(key.contains("SPARQL1_")) {
				q = q.replaceAll("sparql10/", "").replace(".rq", "");
				EPViewInteroperabilityData t = new EPViewInteroperabilityData(q, false, run.getException());

				if(run.getException() == null){
					SPARQL1++;
					t.setValue(true);
				}
				sparql1Feat.add(t);
			}
			else if(key.contains("SPARQL11_")) {
				q = q.replaceAll("sparql11/", "").replace(".rq", "");
				EPViewInteroperabilityData t = new EPViewInteroperabilityData(q, false, run.getException());

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

		fview.setLastUpdate(pres.getEndpointResult().getEnd());

		_db.update(fview);
		_db.update(epview);
		return true;
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