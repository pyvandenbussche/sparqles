package sparqldiscovery;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;

import org.semanticweb.yars.stats.Count;





public class VoidNameParser {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		File f= new File("results/sparqlckan.sparql.tsv");
		Scanner s = new Scanner(f);
		int URIs = 0;
		Count<String> fileNames = new Count<String>();
		Count<String> pathNames = new Count<String>();
		while(s.hasNextLine()){
			String l = s.nextLine();
			String [] tt = l.split("\t");
			try {
				URI u = new URI(tt[1]);
//				System.out.println(u.getAuthority());
				String path = u.getPath();
				
				String folder = "";
				String file = "";
				if(path.lastIndexOf("/")>1){
					folder = path.substring(0,path.lastIndexOf("/"));
					file = path.substring(path.lastIndexOf("/")+1);
				}
				else{
					file = path.replaceAll("/", "");
					folder= "R00T";
				}
				if(file.trim().length()==0)System.out.println(l);
				fileNames.add(file);
				pathNames.add(folder);
				URIs++;
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Found a total of "+URIs+" URIs");
		System.out.println("___________FILE___________________");
		fileNames.printOrderedStats();
		System.out.println("___________PATH___________________");
		pathNames.printOrderedStats();
		System.out.println("______________________________");
		
	}

}
