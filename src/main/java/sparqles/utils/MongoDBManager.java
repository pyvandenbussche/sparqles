package sparqles.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.avro.analytics.AvailabilityView;
import sparqles.avro.analytics.DiscoverabilityView;
import sparqles.avro.analytics.EPView;
import sparqles.avro.analytics.Index;
import sparqles.avro.analytics.InteroperabilityView;
import sparqles.avro.analytics.PerformanceView;
import sparqles.avro.Endpoint;

import sparqles.core.SPARQLESProperties;
import sparqles.avro.availability.AResult;
import sparqles.avro.core.Robots;
import sparqles.avro.discovery.DResult;
import sparqles.avro.features.FResult;
import sparqles.avro.performance.PResult;
import sparqles.avro.schedule.Schedule;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.MongoException.DuplicateKey;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;



public class MongoDBManager {
	private static final Logger log = LoggerFactory.getLogger(MongoDBManager.class);

	private MongoClient client;
	private DB db;


	private final static String RESULT_KEY="endpointResult.endpoint.uri";
	private final static String VIEW_KEY="endpoint.uri";
	private final static String EP_KEY="uri";
	
	private final static String COLL_SCHED="schedule";

	private final static String COLL_ROBOTS="robots";
	private final static String COLL_AVAIL="atasks";
	private final static String COLL_PERF="ptasks";
	private final static String COLL_DISC="dtasks";
	private final static String COLL_FEAT="ftasks";
	private final static String COLL_ENDS="endpoints";
	private final static String COLL_INDEX="index";

	private final static String COLL_AVAIL_AGG="atasks_agg";
	private final static String COLL_PERF_AGG="ptasks_agg";
	private final static String COLL_DISC_AGG="dtasks_agg";
	private final static String COLL_FEAT_AGG="ftasks_agg";
	private final static String COLL_EP_VIEW="epview";

	private static Map<Class, String[]> obj2col = new HashMap<Class, String[]>();
	static {
		obj2col.put(DResult.class, new String[]{COLL_DISC, RESULT_KEY});
		obj2col.put(AResult.class, new String[]{COLL_AVAIL, RESULT_KEY});
		obj2col.put(PResult.class, new String[]{COLL_PERF, RESULT_KEY});
		obj2col.put(FResult.class, new String[]{COLL_FEAT, RESULT_KEY});
		obj2col.put(Endpoint.class, new String[]{COLL_ENDS, EP_KEY});
		obj2col.put(Robots.class, new String[]{COLL_ROBOTS, VIEW_KEY});
		obj2col.put(Schedule.class, new String[]{COLL_SCHED, RESULT_KEY});

		obj2col.put(AvailabilityView.class, new String[]{COLL_AVAIL_AGG, VIEW_KEY});
		obj2col.put(EPView.class, new String[]{COLL_EP_VIEW, VIEW_KEY});
		obj2col.put(Index.class, new String[]{COLL_INDEX, VIEW_KEY});
		obj2col.put(PerformanceView.class, new String[]{COLL_PERF_AGG, VIEW_KEY});
		obj2col.put(InteroperabilityView.class, new String[]{COLL_FEAT_AGG, VIEW_KEY});
		obj2col.put(DiscoverabilityView.class, new String[]{COLL_DISC_AGG, VIEW_KEY} );
	}
	
	
	public MongoDBManager()  {
		setup();
	}

	public boolean isRunning() {
		if(client == null || db == null) return false;
		return true;
	}

	public void setup()  {
		try {
			client = new MongoClient( SPARQLESProperties.getDB_HOST() , SPARQLESProperties.getDB_PORT() );
			log.info("[INIT] MongoDB {} ", client);
			db = client.getDB(SPARQLESProperties.getDB_NAME() );
		} catch (UnknownHostException e) {
			log.error("Coulld not connect to MongoDB instance, {}", ExceptionHandler.toString(e));
		}	
		try{	
			String []cols = {COLL_AVAIL_AGG, COLL_PERF_AGG, COLL_DISC_AGG, COLL_FEAT_AGG, COLL_FEAT_AGG, COLL_EP_VIEW, COLL_INDEX,COLL_SCHED};
			for(String col: cols){
				DBCollection c = db.getCollection(col);
				if(c.getIndexInfo().size()==0)
					c.ensureIndex(new BasicDBObject("endpoint.uri", 1), new BasicDBObject("unique", true));	
			}
//			
			DBCollection c = db.getCollection(COLL_ENDS);
			DBObject d = new BasicDBObject("uri", 1);
			if(c.getIndexInfo().size()==0)
				c.ensureIndex(d, new BasicDBObject("unique", true));
		} catch (Exception e) {
			log.error("Exception while creating indices for MongoDB collections, {}", ExceptionHandler.toString(e));
		}	
	}
	
	
	public void initEndpointCollection() {
		DBCollection c = db.getCollection(COLL_ENDS);
		c.drop();
	}

	public void initScheduleCollection() {
		DBCollection c = db.getCollection(COLL_SCHED);
		c.drop();
	}

	public void initAggregateCollections() {
		String []cols = {COLL_AVAIL_AGG, COLL_PERF_AGG, COLL_DISC_AGG, COLL_FEAT_AGG, COLL_FEAT_AGG};
		for(String col: cols){
			DBCollection c = db.getCollection(col);
			c.drop();
			c.ensureIndex(new BasicDBObject("endpoint.uri", 1), new BasicDBObject("unique", true));	
		}
	}

	public <V extends SpecificRecordBase> boolean insert(Collection<V> results) {
		boolean res = true;

		for(V v: results){
			res = res && insert(v);
		}
		return res;
	}
	

	public <V extends SpecificRecordBase> boolean insert(V res) {
		String[] v = obj2col.get(res.getClass());
		if(v != null && v[0] != null)
			return insert(v[0], res, res.getSchema() );
		else{
			log.error("Collection for {} unknown", res.getClass());
		}
		return false;
	}

	private boolean insert(String collName, Object e, Schema schema){
		DBCollection c = db.getCollection(collName);
		String s = e.toString();
		if(s.contains("uri") && s.contains("datasets"))
			s=s.substring(s.indexOf("uri"), s.indexOf("datasets")).replaceAll("\"", "").trim().replaceAll("\\\\/", "/");
		try{
	
			DBObject dbObject = getObject(e, schema);
			WriteResult wr = c.insert(dbObject,WriteConcern.ACKNOWLEDGED);
			if(wr.getError()!=null){
				log.info("INSERT ERROR {}:{} #>{}",collName, s, wr.getError());
				log.debug("INSERT ERROR {}:{} #>{}",collName,e.toString(), wr.getError());
				return false;
			}else{
				log.info("INSERT SUCCESS {}:{}", collName, s);
				log.debug("INSERT SUCCESS {}:{}", collName, e.toString());
			}
			return true;
		}catch(DuplicateKey ex){
			log.error("INSERT DUPLICATE uri key for {} ({})",e,ExceptionHandler.toString(ex));
			return true;
		}catch(MongoException ex){
			log.error("INSERT MongoDB Exception: {}: {}", e, ExceptionHandler.toString(ex));
		}catch(Exception ex){
			log.error("INSERT Exception: {} {}", e, ExceptionHandler.toString(ex));
		}
		return false;
	}

	public <V extends SpecificRecordBase> boolean update(V res){
		
		
		if(res instanceof AvailabilityView) return update(COLL_AVAIL_AGG, ((AvailabilityView) res).getEndpoint(),res, res.getSchema(),VIEW_KEY );
		if(res instanceof PerformanceView) return update(COLL_PERF_AGG, ((PerformanceView) res).getEndpoint(),res, res.getSchema(),VIEW_KEY );
		if(res instanceof InteroperabilityView) return update(COLL_FEAT_AGG, ((InteroperabilityView) res).getEndpoint(),res, res.getSchema(),VIEW_KEY );		if(res instanceof DiscoverabilityView) return update(COLL_DISC_AGG, ((DiscoverabilityView) res).getEndpoint(),res, res.getSchema(),VIEW_KEY );

		if(res instanceof Endpoint) return update(COLL_ENDS, ((Endpoint) res), res, res.getSchema(),EP_KEY );
		if(res instanceof EPView) return update(COLL_EP_VIEW, ((EPView) res).getEndpoint(), res, res.getSchema(),VIEW_KEY );
		if(res instanceof Index) return update(COLL_INDEX, ((Index) res).getEndpoint(), res, res.getSchema(),VIEW_KEY );
		return false;
	}

	private boolean update(String collName, Endpoint ep, Object e, Schema schema, String key){
		DBCollection c = db.getCollection(collName);
		try{
			DBObject dbObject = getObject(e, schema);
			BasicDBObject q = new BasicDBObject();
			q.append(key, ep.getUri().toString());

			WriteResult wr = c.update(q, dbObject);
			if(wr.getError()!=null){
				log.info("UPDATE ERROR {}:{} #>{}",collName, ep.getUri(), wr.getError());
				log.debug("UPDATE ERROR {}:{} #>{}",collName,e.toString(), wr.getError());
				return false;
			}else{
				log.info("UPDATE SUCCESS {}:{}", collName, ep.getUri());
				log.debug("UPDATE SUCCESS {}:{}", collName, e.toString());
			}

			return true;
		}catch(DuplicateKey ex){
			log.error("INSERT DUPLICATE uri key for {} ({})",ep.getUri(), ExceptionHandler.toString(ex));
			return true;
		}catch(MongoException ex){
			log.error("INSERT MongoDB Exception: {}: {}", ep.getUri(), ExceptionHandler.toString(ex));
		}catch(Exception ex){
			log.error("INSERT Exception: {} {}", ep.getUri(), ExceptionHandler.toString(ex));
		}
		return false;
	}



	public <V extends SpecificRecordBase> List<V> get(Class<V> cls, Schema schema) {
		return getResults(null, cls, schema);
	}


	public Endpoint getEndpoint(Endpoint ep) {
		List<Endpoint> res = scan(ep, COLL_ENDS, Endpoint.class, Endpoint.SCHEMA$, EP_KEY);
		if(res.size()!=1){
			log.error("Received {} results for {}; expected one result ", res.size(), ep.getUri());
		}
		if(res.size()==0) return null;
		return res.get(0);
	}

	public <T> List<T> getResults(Endpoint ep, Class<T> cls, Schema schema) {
		String[] v = obj2col.get(cls);
		
		if(v != null && v[0] != null&& v[1] != null)
			return scan(ep,v[0], cls,schema, v[1]);
		else{
			log.warn("Collection for {} unknown", cls);
		}
		return new ArrayList<T>();
	}

	public <T extends SpecificRecordBase> List<T> getResultsSince(Endpoint ep, Class<T> cls,
			Schema schema, long since) {
	
		ArrayList<T> reslist = new ArrayList<T>();	
	
		DBCollection c  = db.getCollection(COLL_AVAIL);
		DBCursor curs = null;
		try{
		if(ep==null){
			curs = c.find();
		}else{
	
			DBObject q =
					QueryBuilder.start().and(
							QueryBuilder.start(RESULT_KEY).is(ep.getUri().toString()).get(),
							QueryBuilder.start("endpointResult.start").greaterThan(since).get()).get();
			log.info("[EXEC] {}",q);
			curs = c.find(q);
		}
	
		while(curs.hasNext()){
			DBObject o = curs.next();
			SpecificDatumReader r = new SpecificDatumReader<T>(cls);
			JsonDecoder d;
			try {
				d = DecoderFactory.get().jsonDecoder(schema, o.toString());
				T t =(T) r.read(null, d);
				reslist.add(t);
			} catch (IOException e) {
				log.error("GET RESULT Exception: {} {}", ep.getUri(), ExceptionHandler.toString(e));
			}
		}
		}finally{
			if(curs!=null)
				curs.close();
		}
		return reslist;
	
	}

	public <T extends SpecificRecordBase> List<T> getResultsSince(Endpoint ep, Class<T> cls,
			Schema schema, long from, long to) {
		ArrayList<T> reslist = new ArrayList<T>();	
	
		DBCollection c  = db.getCollection(COLL_AVAIL);
		DBCursor curs = null;
		try{
		if(ep==null){
			curs = c.find();
		}else{
	
			DBObject q =
					QueryBuilder.start().and(
							QueryBuilder.start(RESULT_KEY).is(ep.getUri().toString()).get(),
							QueryBuilder.start("endpointResult.start").greaterThan(from).get(),
							QueryBuilder.start("endpointResult.start").lessThanEquals(to).get()).get();
			log.info("[EXEC] {}",q);
			curs = c.find(q);
		}
	
		while(curs.hasNext()){
			DBObject o = curs.next();
			SpecificDatumReader r = new SpecificDatumReader<T>(cls);
			JsonDecoder d;
			try {
				d = DecoderFactory.get().jsonDecoder(schema, o.toString());
				T t =(T) r.read(null, d);
				reslist.add(t);
			} catch (IOException e) {
				log.error("GET RESULT SINCE Exception: {} {}", ep.getUri(), ExceptionHandler.toString(e));
			}
		}
		}finally{
			if(curs!=null)
				curs.close();
		}
		return reslist;
	}

	private DBObject getObject(Object o, Schema s){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonEncoder e;
		try {
			e = EncoderFactory.get().jsonEncoder(s, baos);
			SpecificDatumWriter w = new SpecificDatumWriter(o.getClass());
			w.write(o, e);
			e.flush();
			DBObject dbObject = (DBObject)JSON.parse(baos.toString());
			return dbObject;

		} catch (IOException e1) {
			log.error("GET OBJECT Exception: {} {}", o.getClass(), ExceptionHandler.toString(e1));
		}
		return null;
	}

	private <T> List<T> scan(Endpoint ep,String colName, Class<T> cls, Schema schema, String key) {
		ArrayList<T> reslist = new ArrayList<T>();	

		DBCollection c  = db.getCollection(colName);
		DBCursor curs = null;
		try{
			if(ep==null){
				curs = c.find();
			}else{
				BasicDBObject q = new BasicDBObject();
				q.append(key, ep.getUri().toString());
				curs = c.find(q);
			}

			while(curs.hasNext()){
				DBObject o = curs.next();
				SpecificDatumReader r = new SpecificDatumReader<T>(cls);
				JsonDecoder d;
				try {
					d = DecoderFactory.get().jsonDecoder(schema, o.toString());
					T t =(T) r.read(null, d);
					reslist.add(t);
				} catch (IOException e) {
					log.error("SCAN Exception: {}:{}:{} {}", ep.getUri(),colName, cls, ExceptionHandler.toString(e));
				}
			}
		}finally{
			if(curs!=null)
				curs.close();
		}

		return reslist;
	}

	public boolean cleanup(Endpoint ep) {
		//remove endpoint
		boolean res = true;
		res = res && remove(ep, Endpoint.class);
		res = res && remove(ep, AvailabilityView.class);
		res = res && remove(ep, InteroperabilityView.class);
		res = res && remove(ep, DiscoverabilityView.class);
		res = res && remove(ep, PerformanceView.class);
		return res;
	}
	
	
	
	public boolean remove(Endpoint ep, Class cls) {
		String[] v = obj2col.get(cls);

		DBCollection c  = db.getCollection(v[0]);
		try{
			BasicDBObject q = new BasicDBObject();
			q.append(v[1], ep.getUri().toString());
			
			
			WriteResult wr = c.remove(q,WriteConcern.ACKNOWLEDGED);
			if(wr.getError()!=null){
				log.info("REMOVE ERROR {}:{} #>{}",v[0], ep.getUri(), wr.getError());
				return false;
			}else{
				log.info("REMOVE SUCCESS {}:{}", v[0], ep.getUri());
			}
			return true;
		}catch(DuplicateKey ex){
			log.error("REMOVE DUPLICATE uri key for {} ({})",ep.getUri(), ExceptionHandler.toString(ex));
			return true;
		}catch(MongoException ex){
			log.error("REMOVE MongoDB Exception: {}: {}", ep.getUri(), ExceptionHandler.toString(ex));
		}catch(Exception ex){
			log.error("REMOVE Exception: {} {}", ep.getUri(), ExceptionHandler.toString(ex));
		}
		return false;
	}

	public boolean close(){
		client.close();
		return true;
	}


}
