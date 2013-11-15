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
import sparqles.utils.ExceptionHandler;
import sparqles.utils.FileManager;
import sparqles.utils.MongoDBManager;
import sparqles.core.EndpointTask;
import sparqles.core.SPARQLESProperties;
import sparqles.analytics.RefreshDataHubTask;
import sparqles.avro.Endpoint;
import sparqles.avro.schedule.Schedule;
import sparqles.core.Task;
import sparqles.core.TaskFactory;
import sparqles.core.availability.ATask;
import static sparqles.core.CONSTANTS.*;

public class Scheduler {

	private static final Logger log = LoggerFactory.getLogger(Scheduler.class);

	public final static String CRON_EVERY_HOUR="0 0 0/1 1/1 * ? *";
	public final static String CRON_EVERY_ONETEN="0 30 1 1/1 * ? *";
	private final static String CRON_EVERY_DAY_AT_715="0 15 7 1/1 * ? *";
	private final static String CRON_EVERY_DAY_AT_215="0 15 2 1/1 * ? *";
	private final static String CRON_EVERY_MON_WED_FRI_SUN_THU_AT_410="0 10 4 ? * WED,THU *";

	private final static String CRON_EVERY_SUN_AT_310="0 10 3 ? * SUN *";
	private final static String CRON_EVERY_SUN_AT_2330="0 30 23 ? * SUN *";
	private final static String CRON_EVERY_SAT_AT_310="0 10 3 ? * SAT *";
	private final static String CRON_FIRST_SAT_AT_MONTH_AT_TWO="0 0 2 ? 1/1 SAT#1 *";
	private final static String CRON_EVERY_FIVE_MINUTES="0 0/5 * 1/1 * ? *";


	/**
	 * The default schedules for various tasks
	 * http://www.cronmaker.com/
	 */
	private final static Map<String,String> taskSchedule = new HashMap<String,String>();
	static{
		taskSchedule.put(ATASK, CRON_EVERY_HOUR);
		taskSchedule.put(PTASK, CRON_EVERY_ONETEN);
		taskSchedule.put(FTASK, CRON_EVERY_SUN_AT_310);
		taskSchedule.put(DTASK, CRON_EVERY_SAT_AT_310);
		taskSchedule.put(ITASK, CRON_EVERY_DAY_AT_715);
		taskSchedule.put(ETASK, CRON_EVERY_DAY_AT_215);
	}

	private final ScheduledExecutorService SERVICE, ASERVICE;
	private FileManager _fm;
	private MongoDBManager _dbm;

	private SchedulerMonitor _monitor;

	public Scheduler(){
		this(SPARQLESProperties.getTASK_THREADS());
	}

	public Scheduler(int threads){
		int athreads = (int)( threads * 0.3);
		int tthreads= threads - athreads;
		SERVICE = Executors.newScheduledThreadPool(tthreads);
		ASERVICE = Executors.newScheduledThreadPool(athreads);
				
		_monitor = new SchedulerMonitor();
		_monitor.start();
		log.info("INIT Scheduler with {} athreads and {} threads",athreads, tthreads);
	}

	/**
	 * Initial the scheduler with the schedules from the underlying DB.
	 * @param db
	 */
	public void init(MongoDBManager db) {

		Collection<Schedule> schedules = db.get(Schedule.class, Schedule.SCHEMA$);
		log.info("Scheduling tasks for {} endpoints", schedules.size());

		for(Schedule sd: schedules){
			initSchedule(sd);
			
			
		}
	}

	public void initSchedule(Schedule sd) {
		Endpoint ep = sd.getEndpoint();
		
		
		try {
			if(sd.getATask()!=null){
				schedule(TaskFactory.create(ATASK,ep, _dbm, _fm ), new CronBasedIterator(sd.getATask().toString()));
			}
			if(sd.getPTask()!=null){
				schedule(TaskFactory.create(PTASK,ep, _dbm, _fm ), new CronBasedIterator(sd.getPTask().toString()));
			}				
			if(sd.getFTask()!=null){
				schedule(TaskFactory.create(FTASK,ep, _dbm, _fm ), new CronBasedIterator(sd.getFTask().toString()));
			}			
			if(sd.getDTask()!=null){
				schedule(TaskFactory.create(DTASK,ep, _dbm, _fm ), new CronBasedIterator(sd.getDTask().toString()));
			}
			if(sd.getITask()!=null){
				schedule(TaskFactory.create(ITASK,ep, _dbm, _fm ), new CronBasedIterator(sd.getITask().toString()));
			}
			if(sd.getETask()!=null){
				RefreshDataHubTask task = (RefreshDataHubTask) TaskFactory.create(ETASK,ep, _dbm, _fm );
				task.setScheduler(this);
				schedule(task, new CronBasedIterator(sd.getITask().toString()));
			}
		} catch (ParseException e) {
			log.warn("EXEC ParseException: {} for {}", ep.getUri(),ExceptionHandler.logAndtoString(e,true));
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
				schedulerTask.call();
				reschedule(schedulerTask, iterator);
			} catch (Exception e) {
				log.error("Exception: {} {}", schedulerTask, ExceptionHandler.logAndtoString(e,true));
			}
		}
	}

	public void schedule(Task task, ScheduleIterator iter){
		Date time = iter.next();
		long startTime = time.getTime() - System.currentTimeMillis();

		SchedulerTimerTask t = new SchedulerTimerTask(task,iter);

		if(task instanceof ATask)
			_monitor.submitA(ASERVICE.schedule(t, startTime, TimeUnit.MILLISECONDS));
		else 
			_monitor.submit(SERVICE.schedule(t, startTime, TimeUnit.MILLISECONDS));
		
		log.info("SCHEDULED {} next:'{}' ",task, time);
		log.debug("SCHEDULED {} next:'{}' policy:'{}'",task, time, iter);
	}


	public void shutdown(){
		SERVICE.shutdown();
	}

	private void reschedule(Task task,
			ScheduleIterator iter) {
		Date time = iter.next();
		if(time.getTime() <  System.currentTimeMillis()){
			log.error("PAST stop scheduling task, next date is in the past!");
			return;
		}
		if(task instanceof EndpointTask){
			EndpointTask t = (EndpointTask) task;
			Endpoint ep = _dbm.getEndpoint(t.getEndpoint());
			if(ep == null){
				log.warn("Endpoint {} was removed from DB, stop schedulingl", ep);
				return;
			}
			t.setEndpoint(ep);
		}
		schedule(task, iter);
		
		Object [] s = {task, time, iter};
		log.info("RESCHEDULED {} next:'{}' policy:'{}'",s);
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
		s.setETask(taskSchedule.get(ETASK));
		l.add(s);
		
		return l;
	}

	/**
	 *  Returns the default schedule element for the endpoints
	 * @param ep
	 * @return
	 */
	public static Schedule defaultSchedule(Endpoint ep) {
		Schedule s = new Schedule();
		s.setEndpoint(ep);

		s.setATask(taskSchedule.get(ATASK));
		s.setPTask(taskSchedule.get(PTASK));
		s.setFTask(taskSchedule.get(FTASK));
//		s.setDTask(taskSchedule.get(DTASK));
//		s.setITask(taskSchedule.get(ITASK));

		return s;
	}

	public void close() {
		log.info("Shutting down scheduler service");
		List<Runnable> tasks = SERVICE.shutdownNow();
		log.info("{} Tasks were scheduled after the shutdown command", tasks.size());
		_monitor.halt();
		if(_dbm != null){
			_dbm.close();
		}
	}

	public void useDB(MongoDBManager dbm) {
		_dbm = dbm;
	}

	public void useFileManager(FileManager fm) {
		_fm = fm;
	}

	public void delSchedule(Endpoint ep) {
		// TODO Auto-generated method stub
		
	}
}