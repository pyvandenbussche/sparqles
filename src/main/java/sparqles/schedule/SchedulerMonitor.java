package sparqles.schedule;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparqles.utils.ExceptionHandler;

public class SchedulerMonitor extends Thread {

	private static final Logger log = LoggerFactory.getLogger(SchedulerMonitor.class);

	private boolean run = true;
	private final Long SLEEP_TIME= 10 * 60 * 1000L;
	
	private final ConcurrentLinkedQueue<ScheduledFuture> future = new ConcurrentLinkedQueue<ScheduledFuture>();
	private final ConcurrentLinkedQueue<ScheduledFuture> afuture = new ConcurrentLinkedQueue<ScheduledFuture>();
	
	
	public void run() {
	
		while(run){
			log.info("CHECK queue: {} availability, {} others", afuture.size(), future.size());
			int tasks = future.size(), atasks = future.size();
			
			int tasksDone = 0, atasksDone = 0;
			for(ScheduledFuture<?> f: future){
				if(f.isDone()){
					future.remove(f);
					tasksDone++;
				}
			}
			
			for(ScheduledFuture<?> f: afuture){
				if(f.isDone()){
					afuture.remove(f);
					atasksDone++;
				}
			}
			
			log.info("STATS availability: {} done, {} scheduled; other: {} done, {} scheduled", atasksDone, afuture.size(), tasksDone, future.size());
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
				log.error("{}", ExceptionHandler.logAndtoString(e));
				
			}
		}
	}
	
	public void halt(){
		run = false;
	}

	public void submit(ScheduledFuture<?> schedule) {
		log.debug("Received new future object: {}", schedule);
		future.add(schedule);
	}

	public void submitA(ScheduledFuture<?> schedule) {
		log.debug("Received new future object [A]: {}", schedule);
		afuture.add(schedule);
	}
}