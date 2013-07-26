package schedule;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Date;

import org.apache.avro.specific.SpecificRecordBase;
import org.junit.Test;

import schedule.iter.CronBasedIterator;
import schedule.iter.DailyIterator;
import schedule.iter.HourlyIterator;
import schedule.iter.SecondlyIterator;

import core.Endpoint;
import core.EndpointResult;
import core.Endpoints;
import core.Task;

public class SchedulerTEST {

	@Test
	public void test() {
		
//		
		
		DailyIterator d = new DailyIterator(2, 2, 2);
		iter(d, 10);
		
		HourlyIterator h = new HourlyIterator(15, 20);
		iter(h,10);
		
		
		String c = "10 * * * * ?";
		CronBasedIterator i;
		try {
			i = new CronBasedIterator(c);
			iter(i,10);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Scheduler s = new Scheduler(1);
		try {
			s.schedule(new DummyTask(Endpoints.DBPEDIA), new CronBasedIterator(c));
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			Thread.sleep(600000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	
	private void iter(ScheduleIterator iter, int runs){
		System.err.println("##--\n#"+iter.getClass().getSimpleName());
		System.err.println("##--");
		for( int c =0; c< runs; c++){
			System.err.println(iter.next());
		}
	}
	
	public static class DummyTask extends Task{
		public DummyTask(Endpoint ep) {
			super(ep);
		}

		@Override
		public SpecificRecordBase process(EndpointResult epr) {
			System.out.println("EXEC: "+new Date());
			return null;
		}
	}
}
