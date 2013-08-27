package sparqles.schedule;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.schedule.iter.CronBasedIterator;
import sparqles.schedule.iter.ScheduleIterator;
import sparqles.utils.LogHandler;
import sparqles.core.DBManager;
import sparqles.core.ENDSProperties;
import sparqles.core.Endpoint;
import sparqles.core.EndpointManager;
import sparqles.core.FileManager;
import sparqles.core.Task;
import sparqles.core.TaskFactory;

public class Scheduler {
	
	private static final Logger log = LoggerFactory.getLogger(Scheduler.class);
	
	private final static String CRON_EVERY_HOUR="0 0 0/1 1/1 * ? *";
	private final static String CRON_EVERY_ONETEN="0 30 1/12 1/1 * ? *";
	private final static String CRON_EVERY_DAY_AT_715="0 15 7 1/1 * ? *";
	private final static String CRON_EVERY_WED_THU_AT_410="0 10 4 ? * WED,THU *";
	
	private final static String CRON_EVERY_SUN_AT_310="0 10 3 ? * SUN *";
	private final static String CRON_FIRST_SAT_AT_MONTH_AT_TWO="0 0 2 ? 1/1 SAT#1 *";
	private final static String CRON_EVERY_FIVE_MINUTES="0 0/5 * 1/1 * ? *";
	
	private final static Map<String,String> taskSchedule = new HashMap<String,String>();
	static{
		taskSchedule.put("ATask", CRON_EVERY_HOUR);
//		taskSchedule.put("PTask", CRON_EVERY_SUN_AT_310);
//		taskSchedule.put("FTask", CRON_FIRST_SAT_AT_MONTH_AT_TWO);
		
		taskSchedule.put("PTask", CRON_EVERY_ONETEN);
		taskSchedule.put("FTask", CRON_EVERY_DAY_AT_715);
		taskSchedule.put("DTask", CRON_EVERY_WED_THU_AT_410);
	}
	
	private final ScheduledExecutorService SERVICE;

	private FileManager _fm;
	private DBManager _dbm;
	
	public Scheduler(){
		this(ENDSProperties.getTASK_THREADS());
	}
	
	public Scheduler(int threads){
		SERVICE = Executors.newScheduledThreadPool(threads);
		log.info("[INIT] Scheduler with {} threads",threads);
	}
	
	public void init(EndpointManager epm) {
		sparqles.utils.LogHandler.init(log," Scheduling tasks for {} endpoints", epm.getEndpointScheduleMap().size());
		for(Entry<String, String[]> ent: epm.getEndpointScheduleMap().entrySet()){
			String [] cronSchedules = ent.getValue();
			for(int i=0; i < cronSchedules.length;i++){
				if(cronSchedules[i]!=null){
					try {
						schedule(TaskFactory.create(epm.getTask(i),ent.getKey(), _dbm, _fm ), new CronBasedIterator(cronSchedules[i]));
					} catch (Exception e) {
						LogHandler.warn(log,"",e);
					}
				}
			}
		}
	}

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
//            	log.debug("[EXEC] {}",schedulerTask.getClass().getSimpleName());
				schedulerTask.call();
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

	public void createDefaultSchedule(EndpointManager epm) {
		for(Entry<String, Endpoint> ep: epm.getEndpointMap().entrySet()){
			for(Entry<String,String> ent:taskSchedule.entrySet()){
				epm.updateSchedule(ep.getKey(), ent.getKey(), ent.getValue());
			}
		}
	}

	public void close() {
		log.info("Shutting down scheduler service");
		List<Runnable> tasks = SERVICE.shutdownNow();
		log.info("{} Tasks were scheduled after the shutdown command", tasks.size());
		
		if(_dbm != null){
			_dbm.close();
		}
		
		
	}

	public void useDB(boolean b) {
		if(b){
			_dbm = new DBManager();
		}else{
			if(_dbm != null){
				_dbm.close();
			}
			_dbm = null;
		}
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