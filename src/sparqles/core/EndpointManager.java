package sparqles.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.schedule.Schedule;
import sparqles.utils.LogHandler;
import sparqles.utils.MongoDBManager;

/**
 * This class manages the list of endpoints and their scheduling
 * @author UmbrichJ
 *
 */
public class EndpointManager {

	private static final Logger log = LoggerFactory.getLogger(EndpointManager.class);

	private Map<String, Endpoint> _epMap = null;
	private Map<String, String[]> _epScheduleMap = null;

	private final static Integer ATASK_ID=0;
	private final static Integer PTASK_ID=1;
	private final static Integer DTASK_ID=2;
	private final static Integer FTASK_ID=3;

	public void init(MongoDBManager dbm) {

		LogHandler.init(log, "Endpointmanager");
		//read endpoint list
		loadEndpointMap(dbm);

		//read schedule list
		loadEndpointScheduleMap(dbm);
	}

//	public  Map<String, Endpoint> getEndpointMap() {
//		if(_epMap == null){
//			loadEndpointMap();
//		}
//		return _epMap;
//	}
//
	public  Map<String, String[]> getEndpointScheduleMap() {
		return _epScheduleMap;
	}

	private void loadEndpointScheduleMap(MongoDBManager dbm) {
		_epScheduleMap = new HashMap<String, String[]>();
		
		Collection<Schedule> s = dbm.get(Schedule.class, Schedule.SCHEMA$);
		for(Schedule sd: s){
			Endpoint ep = sd.getEndpoint();
			updateSchedule(ep.getUri().toString(), "ATask", sd.getATask().toString());
			updateSchedule(ep.getUri().toString(), "PTask", sd.getPTask().toString());
			updateSchedule(ep.getUri().toString(), "FTask", sd.getFTask().toString());
			updateSchedule(ep.getUri().toString(), "DTask", sd.getDTask().toString());
		}
		
//		
//		
//		Scanner s = null;
//		if( ENDSProperties.getSCHEDULE_CRON().startsWith("file:")){
//			try {
//				File f= new File(ENDSProperties.getSCHEDULE_CRON().replace("file:", ""));
//				if(f.exists()){
//					s = new Scanner(f);
//				}
//			} catch (FileNotFoundException e) {
//				LogHandler.warn(log, "loading scheduler map", e);
////				log.warn("FileNotFoundException for scheduler map, msg:{}",ENDSProperties.getSCHEDULE_CRON());
//			}
//		}else{
//			s = new Scanner(ClassLoader.getSystemResourceAsStream(ENDSProperties.getSCHEDULE_CRON()));
//		}
//		if(s!=null){
//			while(s.hasNextLine()){
//				parseLine(s.nextLine().trim());
//			}
//		}
	}

	private void parseLine(String line) {
		if(line.startsWith("#") || line.length()==0) return;
		String [] t = line.trim().split(" ");
		if(t.length < 8 || t.length > 9){
			log.warn("Skipping {}, not correct number of tokens, found {}",line, t.length);
			return;
		}else{
			String ep =t[t.length-2].trim().toLowerCase();

			if(!_epMap.containsKey(ep)){
				log.warn("Endpoint {} is not registered in the endpoint map",ep);
//				return;
			}
			String task = t[t.length-1];
			String cron = toString(t);
			updateSchedule(ep, task, cron);
		}
	}

	private Integer getTaskID(String task){
		if(task.equalsIgnoreCase("ATask")){
			return ATASK_ID;
		}else if(task.equalsIgnoreCase("PTask")){
			return PTASK_ID;
		}else if(task.equalsIgnoreCase("DTask")){
			return DTASK_ID;
		}else if(task.equalsIgnoreCase("FTask")){
			return FTASK_ID;
		}else{
			log.warn("Task {} is not known and we cannot assing an ID",task);
		}
		return null;
	}

	public String getTask(Integer id){
		if(id == ATASK_ID) return "ATask";
		else if(id == PTASK_ID) return "PTask";
		else if(id == FTASK_ID)  return "FTask";
		else if(id == DTASK_ID) return "DTask";
		return null;
	}

	/**
	 * Converts the cron schedule from the string array into a string 
	 * @param t
	 * @return
	 */
	private String toString(String[] t) {
		StringBuilder sb = new StringBuilder();
		int stop = 6;
		if(t.length==9) stop = 7;
		for( int i =0; i < stop; i++){
			sb.append(t[i]).append(" ");
		}
		return sb.toString().trim();
	}

	/**
	 * Load the list of endpoint from file
	 * @param dbm 
	 * @return The endpoint map
	 */
	private  void loadEndpointMap(MongoDBManager dbm) {
		_epMap = new HashMap<String, Endpoint>();
		
		Collection<Endpoint> eps = dbm.get(Endpoint.class, Endpoint.SCHEMA$);
		
		for(Endpoint ep: eps){
			_epMap.put(ep.getUri().toString(), ep);
		}
	}
//		
//		Scanner s = null;
//		
//		
//		
//		
//		
//		
//		
//		
//		
//		if( ENDSProperties.getENDPOINT_LIST().startsWith("file:")){
//			try {
//				File f= new File(ENDSProperties.getENDPOINT_LIST().replace("file:", ""));
//				if(f.exists()){
//					s = new Scanner(f);
//				}
//			} catch (FileNotFoundException e) {
//				LogHandler.warn(log, "loading endpoint map", e);
//
//			}
//		}else{
//			s = new Scanner(ClassLoader.getSystemResourceAsStream(ENDSProperties.getENDPOINT_LIST()));
//		}
//		if(s!=null){
//			while(s.hasNextLine()){
//				String line = s.nextLine().trim().toLowerCase();
//				if(line.length()>0 && line.startsWith("http://")){
//					try {
//						Endpoint ep = EndpointFactory.newEndpoint(line);
//						_epMap.put(line, ep);
//					} catch (URISyntaxException e) {
//						LogHandler.warn(log, "loading scheduler map", e);
//					}
//				}
//			}}
//	}

	public void updateSchedule(String ep, String task, String cron) {

		Object[] a = {task,ep, cron};
		log.info("Scheduling {} for {} with {}", a);
		String [] map = _epScheduleMap.get(ep);
		if(map==null){
			map= new String[4];
			_epScheduleMap.put(ep, map);
		}

		Integer id = getTaskID(task);
		if(id != null){
			if(map[id] != null){
				Object[] tt = {task,map[id], cron};
				log.warn("Entry exists for {} (old:{}, new:{})",tt);
			}else 
				map[id]=cron; 
		}
	}

	public void close() {
//		storeMap(_epMap);
//		storeSchedule(_epScheduleMap);

	}
	private void storeSchedule(Map<String, String[]> results) {
		PrintWriter pw=null;
		if(ENDSProperties.getSCHEDULE_CRON().startsWith("file:")){
			File f= new File(ENDSProperties.getSCHEDULE_CRON().replace("file:", ""));
			try {
				pw = new PrintWriter(f);
			} catch (FileNotFoundException e) {
				LogHandler.warn(log, "storing schedule list", e);
			}
		}else{
			log.info("Not storing endpoint list, since file is in classpath");
		}
		if(pw!=null){
			for(Entry<String, String[]>ent: results.entrySet()){
				for(int i=0; i< ent.getValue().length; i++){
					if(ent.getValue()[i]!=null){
						pw.println(ent.getValue()[i]+" "+ent.getKey()+" "+getTask(i));
					}
				}
			}
			pw.close();
		}
	}

//	private  void storeMap(Map<String, Endpoint> results) {
//		PrintWriter pw=null;
//		if(ENDSProperties.getENDPOINT_LIST().startsWith("file:")){
//			File f= new File(ENDSProperties.getENDPOINT_LIST().replace("file:", ""));
//			try {
//				pw = new PrintWriter(f);
//			} catch (FileNotFoundException e) {
//				LogHandler.warn(log, "storing endpoint list", e);
////				log.warn("Did not store endpoint list: {}:{}", e.getClass().getSimpleName(), e.getMessage());
//			}
//		}else{
//			log.info("Not storing endpoint list, since file is in classpath");
//		}
//		if(pw!=null){
//			for(String ep: results.keySet()){
//				pw.println(ep);
//			}
//			pw.close();
//		}
//	}

	public void updateEndpointDB() {
		// TODO Auto-generated method stub
		
	}

	
}


