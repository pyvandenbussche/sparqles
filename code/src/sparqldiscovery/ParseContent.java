package sparqldiscovery;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;

import org.apache.any23.extractor.rdf.RDFXMLExtractor;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.ParseException;
import org.semanticweb.yars.stats.Count;
import org.semanticweb.yars2.rdfxml.RDFXMLParser;

public class ParseContent {

	private final static String sparqDescNS = "http://www.w3.org/ns/sparql-service-description#";
	private final static String voidNS = "http://rdfs.org/ns/void#";
	
	
	public static void main(String[] args) throws FileNotFoundException {
		File folder = new File("results/httpget");
		int  count =0,match=0,voidocc=0;
		Count<Node> voidPred = new Count<Node>();
		Count<Node> spdsPred = new Count<Node>();
		for(File f: folder.listFiles(new FilenameFilter() {
			
			public boolean accept(File dir, String name) {
				return name.endsWith(".dat");
			}
		})){
			Scanner s = new Scanner(f);
			count++;
			boolean spdesc=false, voidDesc=false;
			while(s.hasNextLine()){
				String line = s.nextLine();
				if(line.contains(sparqDescNS)){
					spdesc=true;
					System.err.println(line);
				}
				if(line.contains(voidNS)){
					voidDesc=true;
					System.err.println(line);
				}
			}
			if(spdesc) match++;
			if(voidDesc) voidocc++;
			RDFXMLParser p;
			try {
				p = new  RDFXMLParser(new FileInputStream(f),f.toURI().toString());
				HashSet<Node> voidPredss = new HashSet<Node>();
				HashSet<Node> spedPredss = new HashSet<Node>();
				while(p.hasNext()){
					Node[]nn= p.next();
					if(nn[1].toString().startsWith(voidNS)){
						voidPredss.add(nn[1]);
					}else if(nn[1].toString().startsWith(sparqDescNS)){
						spedPredss.add(nn[1]);
					}
				}
				for(Node n: voidPredss) voidPred.add(n);
				for(Node n: spedPredss) spdsPred.add(n);
				
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("SPARQL.1.1 Description: "+match);
		System.out.println("Void Description: "+voidocc);
		System.out.println("Total files:"+ count);
		System.out.println("__________________________________");
		voidPred.printOrderedStats();
		System.out.println("__________________________________");
		spdsPred.printOrderedStats();
	}
}