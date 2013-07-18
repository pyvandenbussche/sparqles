package sparqldiscovery;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.stats.Count;

public class ParseHeaders {
	static private final Resource server = new Resource("http://www.w3.org/2006/http#server");
	static private final Resource mime = new Resource("http://www.w3.org/2006/http#content-type");
	static private final Resource link = new Resource(Headers.httpNS+"link");
	static private final Resource respCode = new Resource(Headers.httpNS+"responseCode");
	public static void main(String[] args) {
		File folder = new File("results/httpget");
		Count<Node> servers = new Count<Node>();
		Count<Node> mimes = new Count<Node>();
		Count<Node> mimes200 = new Count<Node>();
		Count<Node> links = new Count<Node>();
		Count<Node> resp = new Count<Node>();
		int total =1;
		for(File f: folder.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				return name.endsWith(".nq");
			}
		})){
			System.err.println("Parsing "+f);
			NxParser nxp;
			try {
				total++;
				nxp = new NxParser(new FileInputStream(f));
				String str_resp="";
				Node mimetype=null;
				while(nxp.hasNext()){
					Node [] n = nxp.next();
					if(n[1].equals(server)){
						servers.add(n[2]);
					}
					if(n[1].equals(mime)){
						mimes.add(n[2]);
						mimetype= n[2];
					}
					if(n[1].equals(link)){
						links.add(n[2]);
					}
					if(n[1].equals(respCode)){
						resp.add(n[2]);
						str_resp= n[2].toString();
					}
				}
				if(str_resp.startsWith("2")){
					mimes200.add(mimetype);
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Total parsed files "+total);
		System.out.println("_________________________________");
		Count<String> rdfstores = new Count<String>();
		for(Node n: servers.keySet()){
			String stn = n.toString();
			if(stn.indexOf("/")!=-1){
				rdfstores.add(stn.substring(0, stn.indexOf("/")),servers.get(n));
			}else
			{
				rdfstores.add(stn,servers.get(n));
			}
		}
		
		Count<String> cleanedmimes = new Count<String>();
		
		for(Node n: mimes.keySet()){
			String stn = n.toString();
			if(stn.indexOf(";")!=-1){
				cleanedmimes.add(stn.substring(0, stn.indexOf(";")),mimes.get(n));
			}else{
				cleanedmimes.add(stn,mimes.get(n));
			}
		}
		Count<String> cleanedmimes200 = new Count<String>();
		for(Node n: mimes200.keySet()){
			String stn = n.toString();
			if(stn.indexOf(";")!=-1){
				cleanedmimes200.add(stn.substring(0, stn.indexOf(";")),mimes200.get(n));
			}else{
				cleanedmimes200.add(stn,mimes200.get(n));
			}
		}
		
		
		servers.printOrderedStats(System.out);
		System.out.println("_________________________________");
		toLatexTable(rdfstores,System.out);
//		rdfstores.printOrderedStats(System.out);
		System.out.println("_________________________________");
		toLatexTable(cleanedmimes,System.out);
//		mimes.printOrderedStats(System.out);
		System.out.println("_________________________________");
		links.printOrderedStats();
		
		System.out.println("_____________"+resp.getTotal()+"___________________");
		toLatexTable(resp,System.out);
		
		System.out.println("_____________"+cleanedmimes200.getTotal()+"___________________");
		toLatexTable(cleanedmimes200,System.out);
	}
	
	
	public static void toLatexTable(Count c, PrintStream out){
		StringBuilder sb = new StringBuilder();
		sb.append("\\begin{table}\n")
		.append("\\centering\n")
		.append("\\begin{tabular}{lr}\n")
		.append("\\toprule\n")
		.append("\\textsc{Server}&\\textsc{count}\\\\\n")
		.append("\\midrule\n");
		Iterator<Map.Entry<Object, Integer>> it = c.getOccurrenceOrderedEntries().iterator();
		
		while(it.hasNext()) {
			Map.Entry<Object, Integer> e = it.next();
			sb.append(e.getKey().toString() + "&" + e.getValue()+"\\\\\n");
		}
		sb.append("\\bottomrule\n");
		sb.append("\\end{tabular}\n");
		sb.append("\\caption{\\label{tab:} .}\n");
		sb.append("\\end{table}\n");
		out.println(sb.toString());
	}
}
