package schedule;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Scheduler {
	private final ScheduledExecutorService SERVICE;
	
	Scheduler(int threads){
		SERVICE = Executors.newScheduledThreadPool(1);
	}
	
	
	public void schedule(){
		
	}
}
