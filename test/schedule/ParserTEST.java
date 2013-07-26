package schedule;

import java.util.Date;

import org.junit.Test;

public class ParserTEST {

	
	@Test
	public void testParseLine() throws Exception {
		
		
		Parser p = new Parser();
		
		
		String l = "* * * * http://example.org/ PTask";
		p.parseLine(l);
		CronExpression c = new CronExpression("0 0 12 * * ?");
		System.out.println(c.getExpressionSummary());
		
		Date d = new Date();
		System.out.println("Now: "+d);
		System.out.println(c.getNextValidTimeAfter(new Date()));
	}
}
