package sparqles.paper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.avro.specific.SpecificDatumReader;

import sparqles.avro.analytics.EPViewDiscoverabilityData;
import sparqles.avro.discovery.DGETInfo;
import sparqles.avro.discovery.DResult;
import sparqles.avro.discovery.QueryInfo;
import sparqles.avro.performance.PResult;
import sparqles.core.discovery.DTask;
import sparqles.utils.ExceptionHandler;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.DBObject;
import com.mongodb.util.Hash;
import com.mongodb.util.JSON;

public class Test {

	public static void main(String[] args) {
		HashSet<String> aliveEPS = new HashSet<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File("epsAlive.csv")));
			String ep = null;
			while ( (ep=reader.readLine())!=null){
				//				System.out.println(ep);
				aliveEPS.add(ep.toLowerCase());
			}
			System.out.println(aliveEPS.size());
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Gson gson = new Gson();

			BufferedReader br = Files.newBufferedReader(Paths.get("dtasks.json"), StandardCharsets.UTF_8); 


			Map<String, Map<Date, DGETInfo>> wellknown = new HashMap<String, Map<Date, DGETInfo>>();
			Map<String, Map<Date, DGETInfo>> get = new HashMap<String, Map<Date, DGETInfo>>();

			Map<String, Map<Date, QueryInfo>> query = new HashMap<String, Map<Date, QueryInfo>>();

			Map<String, List<Date>> map = new HashMap<String, List<Date>>();
			Map<String, DResult[]> objectmap = new HashMap<String, DResult[]>();
			Map<String, Date[]> datemap = new HashMap<String, Date[]>();

			HashSet<String> filtered = new HashSet<String>();
			HashSet<String> all = new HashSet<String>();
			DResult res = new DResult();
			for (String line = null; (line = br.readLine()) != null;) {
				JsonElement jelement = new JsonParser().parse(line);
				JsonObject  jobject = jelement.getAsJsonObject();
				DBObject dbObject = (DBObject)JSON.parse(line);
				SpecificDatumReader r = new SpecificDatumReader<DResult>(DResult.class);
				JsonDecoder dec;
				try {
					dec = DecoderFactory.get().jsonDecoder(res.getSchema(), dbObject.toString());
					DResult t =(DResult) r.read(null, dec);


					String uri = t.getEndpointResult().getEndpoint().getUri().toString().toLowerCase();
					all.add(uri);
					if(!aliveEPS.contains(uri)){
						filtered.add(uri);
						if(uri.contains("http://eurostat.linked-statistics.org"))
							System.out.println(uri);
						continue;
					}
					

					Date cur = new Date(t.getEndpointResult().getStart());
					

					if(t.getQueryInfo().size()>1) System.out.println(t);
					QueryInfo qi = t.getQueryInfo().get(0);
					Map<Date, QueryInfo> m = query.get(uri);
					if (m == null){
						m = new HashMap<Date, QueryInfo>();
						query.put(uri, m);
					}
					m.put(cur, qi);

					for(DGETInfo d: t.getDescriptionFiles()){
						if(uri.equals("http://eurostat.linked-statistics.org/sparql")){
							System.out.println("");
						}
						if(d.getOperation().toString().equalsIgnoreCase("EPURL")){
							Map<Date, DGETInfo> a = get.get(uri);
							if (a == null){
								a = new HashMap<Date, DGETInfo>();
								get.put(uri, a);
							}
							a.put(cur, d);
						}else if(d.getOperation().toString().equalsIgnoreCase("wellknown")){
							Map<Date, DGETInfo> a = wellknown.get(uri);
							if (a == null){
								a = new HashMap<Date, DGETInfo>();
								wellknown.put(uri, a);
							}
							a.put(cur, d);
						}
					}
					//					List<Date> d = map.get(uri);
					//					if(d==null){
					//						d = new ArrayList<Date>(); map.put(uri, d);
					//					}
					//					d.add(cur);
					//
					//					DResult[] dobj = objectmap.get(uri);
					//					if(dobj==null){
					//						dobj = new DResult[2];objectmap.put(uri, dobj);
					//					}
					//					Date[] datem = datemap.get(uri);
					//					if(datem==null){
					//						datem = new Date[2];datemap.put(uri, datem);
					//					}
					//
					//					if(datem[0] == null || datem[0].getTime()>cur.getTime()){
					//						datem[0] = cur;
					//						dobj[0]= t;
					//					}
					//					if(datem[1] == null || datem[1].getTime()<cur.getTime()){
					//						datem[1] = cur;
					//						dobj[1]= t;
					//					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.out.println("ALL: "+all.size());
			System.out.println("filtered: "+filtered.size());
			all.removeAll(filtered);
			System.out.println(all.size());
			aliveEPS.removeAll(all);
			System.out.println(aliveEPS);
			System.out.println("query: "+query.size());
			System.out.println("get: "+get.size());
			System.out.println("well: "+wellknown.size());
//			System.exit(0);

			computeServerNames(get);
//
//			System.out.println("\n ### WellKnown ###\n");
			computeWellKnown(wellknown,"wellknown");
//			
//			System.out.println("\n ### GET ###\n");
			computeWellKnown(get,"get");
//			System.out.println("\n ### QUERY ###\n");
			computeQueryResults(query);



			//			
			////			for(String ep: map.keySet())
			////				System.out.println(ep+" "+map.get(ep));
			////			System.exit(0);
			//			
			//			System.out.println("____");
			//			int total=0;
			//			int diff=0;
			//			int serverNameChanged=0,serverNamesWithoutError=0;
			//
			//			
			//			
			//			int[][] voidCount = {{0,0},{0,0},{0,0}};
			//			int[][] sdcount = {{0,0},{0,0},{0,0}};
			//			
			//			Map<String, Map<String, Integer>> [] respCodes = new Map[2];
			////			Map<String, Map<String, Integer>> [] servers = new Map[2];
			//			
			//			Map<String, Integer> serverNameChangeMap = new HashMap<String, Integer>();
			//			
			//			Map<String, Integer[]> [] epsServers = new Map[2];
			//			
			//			FileWriter fw = new FileWriter("discoverability.csv");
			//			
			//			fw.write("ep,server0,server1\n");
			//			
			//			Map<String, Integer> ops = new HashMap<String, Integer>();
			//			ops.put("EPURL",0);ops.put("wellknown",1);
			//			
			//			
			//			for(String uri: map.keySet()){
			//				String s=uri;
			//				total++;
			//				if(datemap.get(uri)[0].getTime() == datemap.get(uri)[1].getTime()){
			////					System.out.println("EP:"+uri+" missing two snapshots");
			////					System.out.println("EP:"+uri+" "+datemap.get(uri)[0]+"  "+datemap.get(uri)[1]);
			//					continue;
			//				}
			//				diff++;
			//				String[] serverNames = new String[2];
			//				
			//				for(int i=0; i<2;i++){
			//					if(respCodes[i] == null)
			//						respCodes[i] = new HashMap<String,Map<String, Integer>>();
			//					
			//					if(servers[i] == null)
			//						servers[i] = new HashMap<String,Map<String, Integer>>();
			//					
			//					if(epsServers[i]==null)
			//						epsServers[i]= new HashMap<String, Integer[]>();
			//						
			//					DResult pres = objectmap.get(uri)[i];
			//					
			//					
			//					System.out.println(pres.getQueryInfo());
			//					
			//					boolean voiddesc = false,sddesc=false;
			//					boolean cvoid= false, csd=false;
			//					
			//					for(DGETInfo info : pres.getDescriptionFiles()){
			//						String op = info.getOperation().toString();
			//						
			//						//update resp code dist
			//						Map<String, Integer> r = respCodes[i].get(op);
			//						if(r==null){
			//							r = new HashMap<String, Integer>();
			//							respCodes[i].put(op,r);
			//						}
			//						String respCode=null;
			//						if(info.getResponseCode() == null) respCode ="null";
			//						else respCode = info.getResponseCode().toString();
			//						
			//						Integer c = r.get(respCode);
			//						if (c==null) c=0;
			//						r.put(respCode, c+1);
			//						
			//						
			//						Map<String, Integer> ser = servers[i].get(op);
			//						if(ser==null){
			//							ser = new HashMap<String, Integer>();
			//							servers[i].put(op,ser);
			//						}
			//						String serverName ="error";
			//						if(info.getResponseCode()!=null){
			//							serverName = info.getResponseServer().toString();
			//						}
			//						if(op.equalsIgnoreCase("EPURL"))
			//							serverNames[i]=serverName;
			//						c = ser.get(serverName);
			//						if (c==null) c=0;
			//						ser.put(serverName, c+1);
			//						if(op.equalsIgnoreCase("sitemap.xml_link")) continue;
			//						if(info.getSPARQLDESCpreds().size() >0 ){
			//							csd=true;
			//							sdcount[ops.get(op)][i]+=1;
			//						}
			//						if(info.getVoiDpreds().size() >0 ){
			//							cvoid=true;
			//							System.out.println(op+" "+ops.get(op));
			//							voidCount[ops.get(op)][i]+=1;
			//						}
			//					}
			//					if(csd){
			//						sdcount[2][i]+=1;
			//					}
			//					if(cvoid ){
			//						voidCount[2][i]+=1;
			//					}
			//				}
			//				if(serverNames[0] !="error" & serverNames[1] != "error"){
			//					serverNamesWithoutError++;
			//					if(!serverNames[0].equalsIgnoreCase(serverNames[1])){
			//						Integer c= serverNameChangeMap.get(serverNames[0]+"->"+serverNames[1]);
			//						if(c==null)c=0;
			//						serverNameChangeMap.put(serverNames[0]+"->"+serverNames[1], c+1);
			//						serverNameChanged++;
			//						System.out.println(uri+" "+Arrays.toString(serverNames));
			//						System.out.println("Before");
			//						DResult pres = objectmap.get(uri)[0];
			//						for(DGETInfo info : pres.getDescriptionFiles())
			//							System.out.println(info);
			//						System.out.println("after");
			//						pres = objectmap.get(uri)[1];
			//						for(DGETInfo info : pres.getDescriptionFiles())
			//							System.out.println(info);
			//						System.out.println("_______");
			//						
			//					}
			//				}
			//			}
			//			System.out.println("Summary");
			//			System.out.println("Total endpoints: "+total);
			//			System.out.println(" Considered endpoints: "+diff);
			//			System.out.println(" serverNamesWithoutError: "+serverNamesWithoutError);
			//			System.out.println(" serverNameChanged: "+serverNameChanged);
			//			
			//			for(int i=0; i<2;i++){
			//				System.out.println("i="+i);
			//				System.out.println("\t"+servers[i].size()+" Servers");
			//				System.out.println("    Response codes");
			//				for(Entry<String, Map<String, Integer>> ent: respCodes[i].entrySet()){
			//					System.out.println("    "+ent.getKey());
			//					for(Entry<String, Integer> ent1: ent.getValue().entrySet()){
			//						System.out.println("\t"+ent1.getKey()+" "+ent1.getValue());
			//					}
			//				}
			//			}
			//			for(int i=0; i<2;i++){
			//				System.out.println("i="+i);
			//				System.out.println("\t"+servers[i].size()+" Servers");
			//				System.out.println("    Server");
			//				for(Entry<String, Map<String, Integer>> ent: servers[i].entrySet()){
			//					System.out.println("    "+ent.getKey());
			//					for(Entry<String, Integer> ent1: ent.getValue().entrySet()){
			//						System.out.println("\t"+ent1.getKey()+" "+ent1.getValue());
			//					}
			//				}
			//			}
			//			for(Entry<String, Integer> ent: serverNameChangeMap.entrySet()){
			//				System.out.println(ent.getKey()+" "+ent.getValue());
			//			}
			//			
			//			
			//			System.out.println("VOID 0"+voidCount[0][0]+" "+voidCount[0][1]);
			//			System.out.println("VOID 1"+voidCount[1][0]+" "+voidCount[1][1]);
			//			System.out.println("VOID 2"+voidCount[2][0]+" "+voidCount[2][1]);
			//			System.out.println();
			//			System.out.println("SD 0"+sdcount[0][0]+" "+sdcount[0][1]);
			//			System.out.println("SD 1"+sdcount[1][0]+" "+sdcount[1][1]);
			//			System.out.println("SD 2"+sdcount[2][0]+" "+sdcount[2][1]);

		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

	private static void computeQueryResults(
			Map<String, Map<Date, QueryInfo>> query) throws IOException {
		int removed=0;
		int added =0,newdiscoverd=0;
		Map<String, Integer[]> respCodes = new HashMap<String, Integer[]>();
		FileWriter fw = new FileWriter(new File("query_results.tsv"));
		fw.write("Endpoint\tnumber of voiD datasets 2013\tnumber of voiD datasets 2015\n");
		for(Entry<String, Map<Date, QueryInfo>> ent: query.entrySet()){
			Integer [] resp = new Integer[2];
			if(ent.getKey().equals("http://eurostat.linked-statistics.org/sparql")){
				System.out.println();
			}
			for(Entry<Date, QueryInfo> v: ent.getValue().entrySet()){
				if(1900+v.getKey().getYear()==2013 ){
					if(v.getValue().getException()==null){
						if(resp[0]==null || resp[0]<v.getValue().getResults().size()) 
							resp[0]=v.getValue().getResults().size();
					}if(v.getValue().getException()!=null){
						resp[0]=-1;
					}
				}
				if(1900+v.getKey().getYear()==2015 ){
					if(v.getValue().getException()==null){
						if(resp[1]==null || resp[1]<v.getValue().getResults().size()) 
							resp[1]=v.getValue().getResults().size();
					}if(v.getValue().getException() != null){
						resp[1] = -1;
					}
				}
			}
			fw.write(ent.getKey()+"\t"+resp[0]+"\t"+resp[1]+"\n");
			if(resp[0]!=null || resp[1] !=null ){
				if(resp[0]!=null && resp[1]==null)
					System.out.println("Exceptions: "+ent.getKey()+" "+resp[0] +" "+ resp[1]);
				else if(resp[0]==null && resp[1]!=null){
					System.out.println("-Exceptions: "+ent.getKey()+" "+resp[0] +" "+ resp[1]);
					if(resp[1]>0)
						newdiscoverd++;
				}
				else if(resp[0]==0 &&resp[1]!=0){
					System.out.println("ADDED: "+ent.getKey()+" "+resp[0] +" "+ resp[1]);
					added++;
					
				}else if(resp[0]!=0 &&resp[1]==0){
					System.out.println("REMOVED: "+ent.getKey()+" "+resp[0] +" "+ resp[1]);
					removed++;
				}
				
//				if(resp[0]!=null && resp[1] !=null && ){
					
//				}
			}
		}
		fw.close();
		System.out.println("removed: "+removed);
		System.out.println("addedd: "+added);
		System.out.println("newdiscoverd: "+newdiscoverd);
		
	}

	private static void computeWellKnown(
			Map<String, Map<Date, DGETInfo>> wellknown, String name) throws IOException {
		Map<String, Boolean[]> voidResults = new HashMap<String, Boolean[]>();
		Map<String, Boolean[]> sdResults = new HashMap<String, Boolean[]>();
		Map<String, String[]> respCodes = new HashMap<String, String[]>();
		
		System.out.println("#######NAME: "+name);
		Map<String, Integer> resp2013 = new HashMap<String, Integer>();
		Map<String, Integer> resp2015 = new HashMap<String, Integer>();
		Map<String, Integer> respChanges = new HashMap<String, Integer>();

		
		FileWriter fw = new FileWriter(new File(name+"_details.tsv"));
		fw.write("EP\trespcode 2013\tvoid 2013\tsd 2013\trespcode 2015\tvoid 2015\tsd 2015\n");
		
		for(Entry<String, Map<Date, DGETInfo>> ent: wellknown.entrySet()){
			if(ent.getKey().contains("http://data.lenka.no/")){
				System.out.println("");
			}
			Boolean [] vd = new Boolean[2];
			Boolean [] sd = new Boolean[2];
			String [] resp = new String[2];
			for(Entry<Date, DGETInfo> v: ent.getValue().entrySet()){
				if(1900+v.getKey().getYear()==2013 ){
					if(v.getValue().getResponseCode()!=null && v.getValue().getResponseCode().toString().equals("200")){
						sd[0]= v.getValue().getSPARQLDESCpreds().size()>0;
						vd[0]= v.getValue().getVoiDpreds().size()>0;
						resp[0]=v.getValue().getResponseCode().toString();
					}
					if( resp[0] == null ){
						if(v.getValue().getResponseCode() !=null)
							resp[0]=v.getValue().getResponseCode().toString();
					}
				}
				if(1900+v.getKey().getYear()==2015 ){
					if(v.getValue().getResponseCode()!=null &&  v.getValue().getResponseCode().toString().equals("200")){
						sd[1]= v.getValue().getSPARQLDESCpreds().size()>0;
						vd[1]= v.getValue().getVoiDpreds().size()>0;
						resp[1]=v.getValue().getResponseCode().toString();
					}
					if( resp[1] == null ){
						if(v.getValue().getResponseCode()!=null)
							resp[1]=v.getValue().getResponseCode().toString();
						else if(v.getValue().getException()!=null){
							resp[1]="-1";
						}
					}
				}
			}
			fw.write(ent.getKey()+"\t"+resp[0]+"\t"+vd[0]+"\t"+sd[0]+"\t"+resp[1]+"\t"+vd[1]+"\t"+sd[1]+"\n");
			voidResults.put(ent.getKey(), vd);
			sdResults.put(ent.getKey(), sd);
			
			respCodes.put(ent.getKey(), resp);
			
			if(resp[0]!=null && resp[1]!=null){
				if(!resp[0].equalsIgnoreCase(resp[1])){
					String s= resp[0]+"->"+resp[1]+" VOID "+vd[0]+" "+vd[1]+" SD "+sd[0]+" "+sd[1];
					Integer c = respChanges.get(s);
					if(c==null) c=0;
					respChanges.put(s,c+1);
				
					if(resp[1]!=null && resp[1].equals("200")&& vd[1]!=null && vd[1])
						System.out.println("Void added: "+ent.getKey()+" "+resp[0]+" "+resp[1]);
					if(resp[0]!=null && resp[0].equals("200") && resp[1]!=null && !resp[1].equals("200") && vd[0]!=null && vd[0])
						System.out.println("Void removed: "+ent.getKey()+" "+resp[0]+" "+resp[1]);
					if(resp[1]!=null && resp[1].equals("200")&& sd[1]!=null && sd[1])
						System.out.println("SD added: "+ent.getKey()+" "+resp[0]+" "+resp[1]);
					if(resp[0]!=null && resp[0].equals("200") && resp[1]!=null && !resp[1].equals("200") && sd[0]!=null && sd[0])
						System.out.println("SD removed: "+ent.getKey()+" "+resp[0]+" "+resp[1]);
				}
				if(resp[1].equals("200")&&resp[0].equals("200")){
					if(vd[0]!=null && vd[1]!=null && vd[0]!= vd[1]){
						String s= resp[0]+"->"+resp[1]+" VOID "+vd[0]+" "+vd[1]+" SD "+sd[0]+" "+sd[1];
						if(vd[0]){
							System.out.println("VOID REMOVED: "+ent.getKey()+" "+s);
//							for(Entry<Date, DGETInfo> v: ent.getValue().entrySet()){
//								System.out.println(v.getKey()+" "+v.getValue());
//							}
						}
						else
							System.out.println("VOID ADDED: "+ent.getKey()+" "+s);
					}
					if(sd[0]!=null && sd[1]!=null && sd[0]!= sd[1]){
						String s= resp[0]+"->"+resp[1]+" VOID "+vd[0]+" "+vd[1]+" SD "+sd[0]+" "+sd[1];
						if(sd[0]){
							System.out.println("SD REMOVED: "+ent.getKey()+" "+s);
							for(Entry<Date, DGETInfo> v: ent.getValue().entrySet()){
								System.out.println(v.getKey()+" "+v.getValue());
							}
						}	
						else
							System.out.println("SD ADDED: "+ent.getKey()+" "+s);
					}
					
				}
				
			}
			String r = resp[0]; 
			if( r== null )
				r="error";
			Integer c = resp2013.get(r);
			if(c==null) c=0;
			resp2013.put(r,c+1);

			r = resp[1]; 
			if( r== null )
				r="error";
			c = resp2015.get(r);
			if(c==null) c=0;
			resp2015.put(r,c+1);
			
			
		}
		fw.close();
		
		System.out.println("ResponseCodes");

		

		System.out.println("\n----2013");
		for(Entry<String, Integer> ent: resp2013.entrySet())
			System.out.println(ent.getKey()+" "+ent.getValue());
		System.out.println("\n----2015");
		for(Entry<String, Integer> ent: resp2015.entrySet())
			System.out.println(ent.getKey()+" "+ent.getValue());
		System.out.println("-------");
		for(Entry<String, Integer> ent: respChanges.entrySet())
			System.out.println(ent.getKey()+" "+ent.getValue());
		
		
		//VOID
		int non=0,error=0;
		int y2013=0, y2015=0;
		Map<String, Integer> voidChange = new HashMap<String, Integer>();
		for(Entry<String, Boolean[]>ent: voidResults.entrySet()){
			if(ent.getValue()[0]==null || ent.getValue()[1]==null){
				non++;
				if(ent.getValue()[0]==null) y2013++;
				if(ent.getValue()[1]==null) y2015++;
			}
			else if(ent.getValue()[0] != ent.getValue()[1]){
				String s = ent.getValue()[0]+"->"+ent.getValue()[1];
				Integer c = voidChange.get(s);
				System.out.println("VOID: "+ent.getKey());
				if(c==null)c=0;
				voidChange.put(s, c+1);
			}
		}
		
		System.out.println("Void: "+voidResults.size()+" none:"+non+" error:"+error);
		System.out.println("Valid 200 responses "+(voidResults.size()-non));
		System.out.println("2013: "+y2013+" y2015:" +y2015);
		System.out.println("Changes: "+voidChange.size());
		for(Entry<String, Integer> ent: voidChange.entrySet()){
			System.out.println(ent.getKey()+" "+ent.getValue());
		}
		non=0;
		Map<String, Integer> sdChange = new HashMap<String, Integer>();
		for(Entry<String, Boolean[]>ent: sdResults.entrySet()){
			if(ent.getValue()[0]==null || ent.getValue()[1]==null){
				non++;
			}
			else if(ent.getValue()[0] != ent.getValue()[1]){
				System.out.println("SD: "+ent.getKey());
				String s = ent.getValue()[0]+"->"+ent.getValue()[1];
				Integer c = sdChange.get(s);
				if(c==null)c=0;
				sdChange.put(s, c+1);
			}
		}
		System.out.println("SD: "+sdResults.size()+" none:"+non+" error:"+error);
		System.out.println(sdResults.size()-non);
		System.out.println("Changes: "+sdChange.size());
		for(Entry<String, Integer> ent: sdChange.entrySet()){
			System.out.println(ent.getKey()+" "+ent.getValue());
		}

	}

	private static void computeServerNames(Map<String, Map<Date, DGETInfo>> get) throws IOException {
		Map<String, String[]> servers = new HashMap<String, String[]>();
		//EP Server
		FileWriter f = new FileWriter(new File("server_names_detail.tsv"));
		f.write("Endpoint\tservername 2013\t servername 2015\n");
		for(Entry<String, Map<Date, DGETInfo>> ent: get.entrySet()){
			String[] snames = new String[2];

			for(Entry<Date, DGETInfo> v: ent.getValue().entrySet()){
				String server = "error";
//				System.out.println(v.getValue().getResponseServer());
				if(v.getValue().getResponseServer() != null)
					server = v.getValue().getResponseServer().toString();
				else{
					System.out.println(v.getValue());
					System.out.println(v.getValue().getResponseCode());
				}
				if(1900+v.getKey().getYear()==2013){
					if( snames[0] == null || ( !server.equalsIgnoreCase("error") && (snames[0].equalsIgnoreCase("error") || snames[0].equalsIgnoreCase("missing"))))
						snames[0]=server;

				}else if(1900+v.getKey().getYear()==2015){
					if(snames[1]==null|| ( !server.equalsIgnoreCase("error") && (snames[1].equalsIgnoreCase("error") || snames[1].equalsIgnoreCase("missing"))))
						snames[1]=server;
				}
			}
//			System.out.println(Arrays.toString(snames));
			servers.put(ent.getKey(), snames);
			f.write(ent.getKey()+"\t"+snames[0]+"\t"+snames[1]+"\n");
		}
		f.close();

		Map<String, Integer> sn2013 = new HashMap<String, Integer>();
		Map<String, Integer> sn2015 = new HashMap<String, Integer>();
		
		int non=0,error=0;
		Map<String, Integer> serverChange = new HashMap<String, Integer>();
		for(Entry<String, String[]>ent: servers.entrySet()){
			if(ent.getValue()[0]==null || ent.getValue()[1]==null){
				non++;
								System.out.println(ent.getKey()+" "+Arrays.toString(ent.getValue()));
			}
			else if(!ent.getValue()[0].equalsIgnoreCase(ent.getValue()[1])){
				String s = ent.getValue()[0]+"->"+ent.getValue()[1];
				Integer c = serverChange.get(s);
				if(c==null)c=0;
				serverChange.put(s, c+1);
			}
			if(ent.getValue()[0]!=null && ent.getValue()[1]!=null){
			String sn13="null";
			String sn15="null";
			if(ent.getValue()[0]!=null)
				sn13 = ent.getValue()[0];
			if(ent.getValue()[1]!=null)
				sn15 = ent.getValue()[1];
			Integer c = sn2013.get(sn13);
			Integer c1 = sn2015.get(sn15);
			if(c==null)c=0;
			if(c1==null)c1=0;
			sn2013.put(sn13,c+1);
			sn2015.put(sn15,c1+1);
			}
		}
		
		System.out.println("Servers: "+servers.size()+" none:"+non+" error:"+error);
		System.out.println(servers.size()-non);
		System.out.println("Changes: "+serverChange.size());
		for(Entry<String, Integer> ent: serverChange.entrySet()){
			System.out.println(ent.getKey()+" "+ent.getValue());
		}
		FileWriter fw;
		try {
			fw = new FileWriter(new File("server_names.tsv"));
			fw.write("#server\t2013\t2015\n");
			for(String sn : sn2013.keySet()){
				int c=0;
				if(sn2015.containsKey(sn)){
					c = sn2015.remove(sn);
				}
				fw.write(sn+"\t"+sn2013.get(sn)+"\t"+c+"\n");
			}
			for(String sn : sn2015.keySet()){
				int c=0;
				if(sn2013.containsKey(sn)){
					c = sn2013.remove(sn);
				}
				fw.write(sn+"\t"+c+"\t"+sn2015.get(sn)+"\n");
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	

	}
}