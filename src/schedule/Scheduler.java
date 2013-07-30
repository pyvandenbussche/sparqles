package schedule;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import schedule.iter.ScheduleIterator;

import core.Task;

public class Scheduler {
	
	private static final Logger log = LoggerFactory.getLogger(Scheduler.class);
	
	private final ScheduledExecutorService SERVICE;
	
	public Scheduler(int threads){
		SERVICE = Executors.newScheduledThreadPool(threads);
		log.info("[INIT] Scheduler with {} threads",threads);
	}
	
	public void init() {
		SchedulerParser p = new SchedulerParser(this);
		p.parse(ClassLoader.getSystemResourceAsStream("schedule.cron"));
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
            	log.debug("[EXEC] {}",schedulerTask.getClass().getSimpleName());
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
}
