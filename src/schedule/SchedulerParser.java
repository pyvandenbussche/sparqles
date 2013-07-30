package schedule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import schedule.iter.CronBasedIterator;

import core.Endpoint;
import core.TaskFactory;

public class SchedulerParser {
	private static final Logger log = LoggerFactory.getLogger(SchedulerParser.class);
	private Scheduler _scheduler;


	public SchedulerParser(Scheduler scheduler) {
		_scheduler = scheduler;
	}

	public void parse(InputStream inputStream){
		Scanner s = new Scanner(inputStream);
		while(s.hasNextLine()){
			
			parseLine(s.nextLine());
		}

	}

	public void parseLine(String line){
		if(line.trim().startsWith("#") || line.trim().length()==0) return;
		
		log.debug("[PARSE] {}", line);
		
		String [] t = line.trim().split(" ");
		if(t.length < 8 || t.length > 9){
			log.warn("Skipping {}, not correct number of tokens, found {}",line, t.length);
			return;
		}
		try {
			URI endpoint = new URI(t[t.length-2]);
			String task = t[t.length-1];
		
			_scheduler.schedule(TaskFactory.create(task, endpoint), new CronBasedIterator(toString(t)));
		} catch (URISyntaxException e) {
			log.warn("Skipping {}, endpoint not valid , {}",line, e.getMessage());
			return;
		} catch (ParseException e) {
			log.warn("Skipping {}, cron expression not valid , {}",line, e.getMessage());
		}
	}

	private String toString(String[] t) {
		StringBuilder sb = new StringBuilder();
		int stop = 6;
		if(t.length==9) stop = 7;
		for( int i =0; i < stop; i++){
			sb.append(t[i]).append(" ");
		}
		return sb.toString();
	}
}
