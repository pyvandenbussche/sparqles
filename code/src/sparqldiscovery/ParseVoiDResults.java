package sparqldiscovery;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;

public class ParseVoiDResults {

	
	
	public static void main(String[] args) throws FileNotFoundException {
		Scanner s = new Scanner(new File("res/347_endpoints_list.txt"));
		HashSet<String> eps = new HashSet<String>();
		while(s.hasNextLine()){
			String line =s.nextLine();
			String [] tt = line.split("\t");
			eps.add(tt[0]);
		}
		
//		File in = new File("results/sparqlvoid.sparql.tsv");
//		File in = new File("results/sparqlselfvoid.sparql.tsv");
		File in = new File("results/sparqlckan.sparql.tsv");
		s = new Scanner(in);
		HashSet<String> epsvoid = new HashSet<String>();
		while(s.hasNextLine()){
			String line =s.nextLine();
			String [] tt = line.split("\t");
			epsvoid.add(tt[0]);
		}
		int c=0;
		for(String str: epsvoid){
			if(eps.contains(str)){
				c++;
			}
		}
		System.out.println(c+"/"+epsvoid.size()+"/"+eps.size());
	}
}
