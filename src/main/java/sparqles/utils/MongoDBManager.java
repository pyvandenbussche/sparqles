package sparqles.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.sql.rowset.serial.SerialBlob;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

import sparqles.core.ENDSProperties;
import sparqles.core.Endpoint;
import sparqles.core.availability.AResult;
import sparqles.core.discovery.DResult;
import sparqles.core.features.FResult;
import sparqles.core.performance.PResult;
import sparqles.schedule.Schedule;



public class MongoDBManager {
	private static final Logger log = LoggerFactory.getLogger(MongoDBManager.class);

	private MongoClient client;
	private DB db;

	private final static String COLL_SCHED="schedule";

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

	public MongoDBManager()  {
		setup();
	}

	public void initEndpointCollection() {
		DBCollection c = db.getCollection(COLL_ENDS);
		c.drop();
		c.ensureIndex(new BasicDBObject("uri", 1).append("unique", true));
	}

	public void initScheduleCollection() {
		DBCollection c = db.getCollection(COLL_SCHED);
		c.drop();
		c.ensureIndex(new BasicDBObject("endpoint.uri", 1).append("unique", true));
	}


	private void setup()  {
		try {
			client = new MongoClient( ENDSProperties.getDB_HOST() , ENDSProperties.getDB_PORT() );
			db = client.getDB( ENDSProperties.getDB_NAME() );
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public <V extends SpecificRecordBase> boolean insert(V res) {
		if(res instanceof DResult) return insert(COLL_DISC, res, res.getSchema() );
		if(res instanceof AResult) return insert(COLL_AVAIL, res, res.getSchema() );
		if(res instanceof PResult) return insert(COLL_PERF, res, res.getSchema() );
		if(res instanceof FResult) return insert(COLL_FEAT, res, res.getSchema() );
		if(res instanceof Endpoint) return insert(COLL_ENDS, res, res.getSchema() );
		if(res instanceof Schedule) return insert(COLL_SCHED, res, res.getSchema() );
		return true;
	}

//	public boolean insert(Schedule ep) {
//		return insert(COLL_SCHED, ep, ep.getSchema());
//	}
//
//	public boolean insert(Endpoint ep){
//		return insert(COLL_ENDS, ep, ep.getSchema());
//	}
	
	private boolean insert(String collName, Object e, Schema schema){
		DBCollection c = db.getCollection(collName);
		try{

			DBObject dbObject = getObject(e, schema);
			WriteResult wr = c.insert(dbObject,WriteConcern.FSYNC_SAFE);
			if(wr.getError()!=null){
				System.out.println("error");
			}else{
				log.info("[INSERT] [SUCC] {}",e.toString());
			}

			return true;
		}catch(Exception ex){
			log.warn("[EXEC] {}",ex);
		}
		return false;
	}

	public <V extends SpecificRecordBase> List<V> get(Class<V> cls, Schema schema) {
		return getResults(null, cls, schema);
	}

	
	public <T> List<T> getResults(Endpoint ep, Class<T> cls, Schema schema) {
		if(cls.getName().equals(DResult.class.getName())) return scan(ep,COLL_DISC, cls,schema);
		if(cls.getName().equals(AResult.class.getName())) return scan(ep,COLL_AVAIL,cls, schema);
		if(cls.getName().equals(PResult.class.getName())) return scan(ep,COLL_PERF, cls,schema);
		if(cls.getName().equals(FResult.class.getName())) return scan(ep,COLL_FEAT, cls,schema);
		if(cls.getName().equals(Endpoint.class.getName())) return scan(ep,COLL_ENDS, cls,schema);
		if(cls.getName().equals(Schedule.class.getName())) return scan(ep,COLL_SCHED, cls,schema);
		return null;
	}
	
	public boolean close(){
		client.close();
		return true;
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
			e1.printStackTrace();
		}
		return null;
	}

	private <T> List<T> scan(Endpoint ep,String colName, Class<T> cls, Schema schema) {
		ArrayList<T> reslist = new ArrayList<T>();	

		DBCollection c  = db.getCollection(colName);
		DBCursor curs = null;
		if(ep==null){
			curs = c.find();
		}else{
			BasicDBObject q = new BasicDBObject();
			q.append("endpoint.uri", ep.getUri().toString());
			c.find(q);
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
				e.printStackTrace();
			}
		}
		return reslist;
	}

//	public <T> List<T> getResultsByDESCDate(Endpoint ep, Class<T> cls) {
//		ArrayList<T> reslist = new ArrayList<T>();
//		try {
//
//			Statement st = con.createStatement();
//			String query ="SELECT * FROM results WHERE Endpoint='"+ep.getUri().toString()+"' AND Task='"+cls.getSimpleName()+"' ORDER BY Date DESC;";
//			log.info("[QUERY] {}", query);
//
//			ResultSet res = st.executeQuery(query);
//			while(res.next()){
//				Decoder decoder = DecoderFactory.get().binaryDecoder(res.getBinaryStream(3), null);
//				DatumReader<T> dr = new SpecificDatumReader<T>(cls);
//
//				T t = dr.read(null, decoder);
//				reslist.add(t);
//			}
//		} catch (SQLException e) {
//
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return reslist;
//	}
//
//	public <T> List<T> getResultsSince(Endpoint ep, Class<T> cls,
//			Long timestamp) {
//		ArrayList<T> reslist = new ArrayList<T>();
//		try {
//			Statement st = con.createStatement();
//			String query ="SELECT * FROM results WHERE Endpoint='"+ep.getUri().toString()+"' AND Task='"+cls.getSimpleName()+"' AND Date > '"+new java.sql.Timestamp(timestamp)+"' ORDER BY Date DESC;";
//			log.info("[QUERY] {}", query);
//
//			ResultSet res = st.executeQuery(query);
//			while(res.next()){
//				Decoder decoder = DecoderFactory.get().binaryDecoder(res.getBinaryStream(3), null);
//				DatumReader<T> dr = new SpecificDatumReader<T>(cls);
//
//				T t = dr.read(null, decoder);
//				reslist.add(t);
//			}
//		} catch (SQLException e) {
//
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return reslist;
//	}
//
//	
//
//



}
