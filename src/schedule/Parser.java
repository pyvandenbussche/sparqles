package schedule;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Parser {
	private static final Logger log = LoggerFactory.getLogger(Parser.class);
	
	
	public Parser() {
		
	}
	
	public void parse(File f){
		Scanner s;
		try {
			s = new Scanner(f);
			while(s.hasNextLine()){
				parseLine(s.nextLine());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	public Object parseLine(String line){
		String [] t = line.trim().split(" ");
		if(t.length != 6){
			log.warn("Skipping {}, not correct number of tokens, found {}",line, t.length);
			return null;
		}
		try {
			URI endpoint = new URI(t[4]);
		} catch (URISyntaxException e) {
			log.warn("Skipping {}, endpoint not valid , {}",line, e.getMessage());
			return null;
		}
		String task = t[5];
		
		
		
		
		return null;
		
		
	}
}
