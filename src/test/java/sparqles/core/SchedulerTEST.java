package sparqles.core;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sparqles.schedule.Scheduler;
import sparqles.schedule.iter.CronBasedIterator;

public class SchedulerTEST {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		
		try {
			CronBasedIterator iter = new CronBasedIterator(Scheduler.CRON_EVERY_HOUR);
			System.out.println("Now: "+new Date(System.currentTimeMillis()));
			System.out.println("Next: "+ iter.next());
			System.out.println("Next: "+ iter.next());
			System.out.println("Next: "+ iter.next());
			System.out.println("Next: "+ iter.next());
			System.out.println("Next: "+ iter.next());
			
			iter = new CronBasedIterator(Scheduler.CRON_EVERY_ONETEN);
			System.out.println("Now: "+new Date(System.currentTimeMillis()));
			System.out.println("Next: "+ iter.next());
			System.out.println("Next: "+ iter.next());
			System.out.println("Next: "+ iter.next());
			System.out.println("Next: "+ iter.next());
			System.out.println("Next: "+ iter.next());
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
	}

}
