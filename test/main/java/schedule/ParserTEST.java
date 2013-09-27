package schedule;

import java.util.Date;

import org.junit.Test;


public class ParserTEST {

	
	@Test
	public void testParseLine() throws Exception {
		
		
		Scheduler s = new Scheduler(10);
		s.init();
		SchedulerParser p = new SchedulerParser(s);
		
		
		String l = "0 0/1 * 1/1 * ? http://example.org/ PTask";
		p.parseLine(l);
		
	}
}
