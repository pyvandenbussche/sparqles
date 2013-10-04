package sparqles.schedule;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.schedule.iter.CronBasedIterator;
import sparqles.schedule.iter.ScheduleIterator;
import sparqles.utils.FileManager;
import sparqles.utils.MongoDBManager;
import sparqles.core.SPARQLESProperties;
import sparqles.core.Endpoint;
import sparqles.core.Task;
import sparqles.core.TaskFactory;
import static sparqles.core.CONSTANTS.*;

public class Scheduler {
	
	private static final Logger log = LoggerFactory.getLogger(Scheduler.class);
	
	private final static String CRON_EVERY_HOUR="0 0 0/1 1/1 * ? *";
	private final static String CRON_EVERY_ONETEN="0 30 1/12 1/1 * ? *";
	private final static String CRON_EVERY_DAY_AT_715="0 15 7 1/1 * ? *";
	private final static String CRON_EVERY_WED_THU_AT_410="0 10 4 ? * WED,THU *";
	
	private final static String CRON_EVERY_SUN_AT_310="0 10 3 ? * SUN *";
	private final static String CRON_EVERY_SUN_AT_2330="0 30 23 ? * SUN *";
	private final static String CRON_EVERY_SAT_AT_310="0 10 3 ? * SAT *";
	private final static String CRON_FIRST_SAT_AT_MONTH_AT_TWO="0 0 2 ? 1/1 SAT#1 *";
	private final static String CRON_EVERY_FIVE_MINUTES="0 0/5 * 1/1 * ? *";
	

	/**
	 * The default schedules for various tasks
	 */
	private final static Map<String,String> taskSchedule = new HashMap<String,String>();
	static{
		taskSchedule.put(ATASK, CRON_EVERY_HOUR);
		taskSchedule.put(PTASK, CRON_EVERY_ONETEN);
		taskSchedule.put(FTASK, CRON_EVERY_SUN_AT_310);
		taskSchedule.put(DTASK, CRON_EVERY_SAT_AT_310);
		taskSchedule.put(ITASK, CRON_EVERY_SUN_AT_2330);
	}
	
	
	private final ScheduledExecutorService SERVICE;

	private FileManager _fm;
	private MongoDBManager _dbm;
	
	public Scheduler(){
		this(SPARQLESProperties.getTASK_THREADS());
	}
	
	public Scheduler(int threads){
		SERVICE = Executors.newScheduledThreadPool(threads);
		log.info("[INIT] Scheduler with {} threads",threads);
	}
	
	/**
	 * Initial the scheduler with the schedules from the underlying DB.
	 * @param db
	 */
	public void init(MongoDBManager db) {
		
		Collection<Schedule> schedules = db.get(Schedule.class, Schedule.SCHEMA$);
		sparqles.utils.LogHandler.init(log," [Scheduling tasks for {} endpoints", schedules.size());
		
		for(Schedule sd: schedules){
			
			Endpoint ep = sd.getEndpoint();
			String task=null, schedule= null;
			
			if(sd.getATask()!=null){
				task=ATASK;
				schedule=sd.getATask().toString();
			}
			if(sd.getPTask()!=null){
				task=PTASK;
				schedule=sd.getPTask().toString();
			}				
			if(sd.getFTask()!=null){
				task=FTASK;
				schedule=sd.getFTask().toString();				
			}			
			if(sd.getDTask()!=null){
				task=DTASK;
				schedule=sd.getDTask().toString();			
			}
			if(sd.getITask()!=null){
				task=ITASK;
				schedule=sd.getITask().toString();			
			}
			try {
				schedule(TaskFactory.create(task,ep, _dbm, _fm ), new CronBasedIterator(schedule));
			} catch (ParseException e) {
				log.warn("[EXEC] ParseException: {} for {}",e.getMessage(), ep.uri);
			}
		}
	}

	/**
	 * A timer task which executes the assigned task and automatically reschedules the data
	 * @author umbrichj
	 *
	 */
	class SchedulerTimerTask implements Runnable {
		private ScheduleIterator iterator;
		private Task schedulerTask;
        public SchedulerTimerTask(Task schedulerTask,
                ScheduleIterator iterator) {
            this.schedulerTask = schedulerTask;
            this.iterator = iterator;
        }
        public void run() {
            try {
				schedulerTask.execute();
				reschedule(schedulerTask, iterator);
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
    }
	
	public void schedule(Task task, ScheduleIterator iter){
		Date time = iter.next();
		long startTime = time.getTime() - System.currentTimeMillis();
        
		SchedulerTimerTask t = new SchedulerTimerTask(task,iter);
	
		SERVICE.schedule(t, startTime, TimeUnit.MILLISECONDS);
		Object [] s = {task, time, iter};
		log.info("[SCHEDULED] {} next:'{}' policy:'{}'",s);
	}
	
	
	public void shutdown(){
		SERVICE.shutdown();
	}
	
	private void reschedule(Task task,
			ScheduleIterator iter) {
		Date time = iter.next();
		
		long startTime = time.getTime() - System.currentTimeMillis();
        SchedulerTimerTask t = new SchedulerTimerTask(task,iter);
		SERVICE.schedule(t, startTime, TimeUnit.MILLISECONDS);
		Object [] s = {task, time, iter};
		log.info("[RESCHEDULED] {} next:'{}' policy:'{}'",s);
	}

	
	/**
	 * Creates for all endpoints in the DB a default schedule
	 * @param dbm
	 * @return
	 */
	public static Collection<Schedule> createDefaultSchedule(
			MongoDBManager dbm) {
		List<Schedule> l = new ArrayList<Schedule>();
		Collection<Endpoint> eps = dbm.get(Endpoint.class, Endpoint.SCHEMA$); 
		for(Endpoint ep: eps){
			Schedule s = defaultSchedule(ep);
			l.add(s);
		}
		
		//add the analytics schedules for 
		Schedule s = new Schedule();
		s.setEndpoint(SPARQLES);
		s.setITask(taskSchedule.get(ITASK));
		l.add(s);
		
		return l;
	}
	
	/**
	 *  Returns the default schedule element
	 * @param ep
	 * @return
	 */
	private static Schedule defaultSchedule(Endpoint ep) {
		Schedule s = new Schedule();
		s.setEndpoint(ep);
		
		s.setATask(taskSchedule.get(ATASK));
		s.setPTask(taskSchedule.get(PTASK));
		s.setFTask(taskSchedule.get(FTASK));
		s.setDTask(taskSchedule.get(DTASK));
		s.setITask(taskSchedule.get(ITASK));
		
		return s;
	}

	public void close() {
		log.info("Shutting down scheduler service");
		List<Runnable> tasks = SERVICE.shutdownNow();
		log.info("{} Tasks were scheduled after the shutdown command", tasks.size());
		
		if(_dbm != null){
			_dbm.close();
		}
	}

	public void useDB(MongoDBManager dbm) {
		_dbm = dbm;
	}

	public void useFileManager(boolean b) {
		if(b){
			_fm = new FileManager();
		}else{
			if(_fm != null){
				;
			}
			_fm = null;
		}
	}
}