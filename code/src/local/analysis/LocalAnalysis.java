package local.analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

public class LocalAnalysis {
	public static EndpointResultRangeFilter[] THRESHOLDS = new EndpointResultRangeFilter[]{
		new EndpointResultRangeFilter(0),
		new EndpointResultRangeFilter(500),
		new EndpointResultRangeFilter(1000),
		new EndpointResultRangeFilter(1500),
		new EndpointResultRangeFilter(5000),
		new EndpointResultRangeFilter(10000),
		new EndpointResultRangeFilter(20000),
		new EndpointResultRangeFilter(40000),
		new EndpointResultRangeFilter(50000),
		new EndpointResultRangeFilter(100000),
		new EndpointResultRangeFilter(1,499),
		new EndpointResultRangeFilter(501,999),
		new EndpointResultRangeFilter(1001,1499),
		new EndpointResultRangeFilter(1501,4999),
		new EndpointResultRangeFilter(5001,9999),
		new EndpointResultRangeFilter(10001,19999),
		new EndpointResultRangeFilter(20001,39999),
		new EndpointResultRangeFilter(40001,49999),
		new EndpointResultRangeFilter(50001,99999),
		new EndpointResultRangeFilter(100002),
	};
	
	
	public static String ENDPOINTS = "C:\\Users\\aidhog\\Documents\\Research\\papers\\epmonitoring\\data\\endpoints-340.txt";
	public static String INPUT_DIR = "C:\\Users\\aidhog\\Documents\\Research\\papers\\epmonitoring\\data\\perf-results\\bak\\";
	public static String FILTER_DIR = "C:\\Users\\aidhog\\Documents\\Research\\papers\\epmonitoring\\data\\perf-results\\340-perf\\results\\";
	public static String SUFFIX = ".log";
	public static String EXCEPTION = "Exception";

	static final Logger _log = Logger.getLogger(LocalAnalysis.class.getName());

	public static void main(String[] args) throws IOException{
		Set<String> endpoints = null;
		if(ENDPOINTS!=null){
			endpoints = loadEndpoints(ENDPOINTS);
			_log.info("Loaded "+endpoints.size()+" active endpoints.");
		}
		
//		Map<String,Map<String,EndpointResult>> file2ep2result = new HashMap<String,Map<String,EndpointResult>>();
//		Map<String,Map<String,EndpointResult>> ep2file2result = new HashMap<String,Map<String,EndpointResult>>();
//
//		load(INPUT_DIR, file2ep2result, ep2file2result, endpoints);
//		
//		for(Map.Entry<String,Map<String,EndpointResult>> e:file2ep2result.entrySet()){
//			_log.info("File: "+e.getKey()+" records: "+e.getValue().size());
//		}
		
		filter(INPUT_DIR,endpoints,FILTER_DIR);
		
//		printThresholdInfo(ep2file2result,THRESHOLDS);
//		printLimitSummary(ep2file2result);
//		printLimitCDF(ep2file2result);
//		printLimitFilterCDF(ep2file2result);
//		printAskCDF(ep2file2result);
//		printJoinCDF(ep2file2result);
//		printJoinFilterCDF(ep2file2result);
//		
//		printLimitTimeCDF(ep2file2result);
//		printFilteredLimitTimeCDF(ep2file2result);
//		printSumColdAskCDF(ep2file2result);
//		printSumColdJoinCDF(ep2file2result);
	}
	
	static void printThresholdInfo(Map<String,Map<String,EndpointResult>> ep2file2result, EndpointResultRangeFilter[] thresholds){
		Map<String,Map<String,EndpointResult>> limit_e2f2r = projectRegexTests(ep2file2result, "limit100001");
		
		Map<String,Count<String>> test2dist = new HashMap<String,Count<String>>();
		
		
		for(Map.Entry<String,Map<String,EndpointResult>> e2f2r: limit_e2f2r.entrySet()){
			for(Map.Entry<String,EndpointResult> f2r : e2f2r.getValue().entrySet()){
				Count<String> dist = test2dist.get(f2r.getKey());
				if(dist==null){
					dist = new Count<String>();
					test2dist.put(f2r.getKey(),dist);
				}
				
				for(EndpointResultRangeFilter rf: thresholds){
					if(rf.passesFilter(f2r.getValue())){
						dist.add(rf.toString());
					}
				}
			}
		}
		
		for(Map.Entry<String,Count<String>> t2d:test2dist.entrySet()){
			System.out.println(t2d.getKey());
			System.out.println("interval\tcount");
			for(EndpointResultRangeFilter rf: thresholds){
				Integer count = t2d.getValue().get(rf.toString());
				if(count==null) count = 0;
				System.out.println(rf.toString()+"\t"+count);
			}
		}
	}
	
	static void printJoinCDF(Map<String,Map<String,EndpointResult>> ep2file2result){
		Map<String,Map<String,EndpointResult>> limit_e2f2r = projectRegexTests(ep2file2result, "join");
		Map<String,Map<String,EndpointResult>> f_limit_e2f2r = filterEndpointsAllTests(limit_e2f2r, new EndpointExceptionFilter());
		f_limit_e2f2r = filterEndpointsAllTests(f_limit_e2f2r, new EndpointResultRangeFilter(1,1000));
		
		_log.info("Map : "+f_limit_e2f2r.size());
		printCDF(f_limit_e2f2r, new MillisecondPerResult());
	}
	
	static void printJoinFilterCDF(Map<String,Map<String,EndpointResult>> ep2file2result){
		Map<String,Map<String,EndpointResult>> limit_e2f2r = projectRegexTests(ep2file2result, "join");
		Map<String,Map<String,EndpointResult>> f_limit_e2f2r = filterEndpointsAllTests(limit_e2f2r, new EndpointExceptionFilter());
		f_limit_e2f2r = filterEndpointsAllTests(f_limit_e2f2r, new EndpointResultRangeFilter(990,1000));
		
		_log.info("Map : "+f_limit_e2f2r.size());
		printCDF(f_limit_e2f2r, new MillisecondPerResult());
	}
	
	static void printJoinTimeCDF(Map<String,Map<String,EndpointResult>> ep2file2result){
		Map<String,Map<String,EndpointResult>> limit_e2f2r = projectRegexTests(ep2file2result, "join");
		Map<String,Map<String,EndpointResult>> f_limit_e2f2r = filterEndpointsAllTests(limit_e2f2r, new EndpointExceptionFilter());
		f_limit_e2f2r = filterEndpointsAllTests(f_limit_e2f2r, new EndpointResultRangeFilter(1,1000));
		
		_log.info("Map : "+f_limit_e2f2r.size());
		printCDF(f_limit_e2f2r, new Time());
	}
	
	static void printSumColdJoinCDF(Map<String,Map<String,EndpointResult>> ep2file2result){
		Map<String,Map<String,EndpointResult>> joins_cold_e2f2r = projectRegexTests(ep2file2result, "join");
		joins_cold_e2f2r = projectRegexTests(joins_cold_e2f2r, "warm", true);
		Map<String,Map<String,EndpointResult>> f_joins_cold_e2f2r = filterEndpointsAllTests(joins_cold_e2f2r, new EndpointExceptionFilter());
		f_joins_cold_e2f2r = filterEndpointsAllTests(f_joins_cold_e2f2r, new EndpointResultRangeFilter(1000,1000));
		
		Map<String,Map<String,EndpointResult>> sum = sumTests(f_joins_cold_e2f2r);
		
		_log.info("Map : "+sum.size());
		printCDF(sum, new MillisecondPerResult());
	}
	
	static void printLimitCDF(Map<String,Map<String,EndpointResult>> ep2file2result){
		Map<String,Map<String,EndpointResult>> limit_e2f2r = projectRegexTests(ep2file2result, "limit100001");
		Map<String,Map<String,EndpointResult>> f_limit_e2f2r = filterEndpointsAllTests(limit_e2f2r, new EndpointExceptionFilter());
		f_limit_e2f2r = filterEndpointsAllTests(f_limit_e2f2r, new EndpointResultRangeFilter(1,Integer.MAX_VALUE));
		
		_log.info("Map : "+f_limit_e2f2r.size());
		printCDF(f_limit_e2f2r, new MillisecondPerResult());
	}
	
	static void printLimitFilterCDF(Map<String,Map<String,EndpointResult>> ep2file2result){
		Map<String,Map<String,EndpointResult>> limit_e2f2r = projectRegexTests(ep2file2result, "limit100001");
		Map<String,Map<String,EndpointResult>> f_limit_e2f2r = filterEndpointsAllTests(limit_e2f2r, new EndpointExceptionFilter());
		f_limit_e2f2r = filterEndpointsAllTests(f_limit_e2f2r, new EndpointResultRangeFilter(99001,100002));
		
		_log.info("Map : "+f_limit_e2f2r.size());
		printCDF(f_limit_e2f2r, new MillisecondPerResult());
	}
	
	static void printLimitTimeCDF(Map<String,Map<String,EndpointResult>> ep2file2result){
		Map<String,Map<String,EndpointResult>> limit_e2f2r = projectRegexTests(ep2file2result, "limit100001");
		limit_e2f2r = projectRegexTests(limit_e2f2r, "warm", true);
		Map<String,Map<String,EndpointResult>> f_limit_e2f2r = filterEndpointsAllTests(limit_e2f2r, new EndpointExceptionFilter());
		f_limit_e2f2r = filterEndpointsAllTests(f_limit_e2f2r, new EndpointResultRangeFilter(1,Integer.MAX_VALUE));
		
		_log.info("Map : "+f_limit_e2f2r.size());
		printCDF(f_limit_e2f2r, new Time());
	}
	
	static void printFilteredLimitTimeCDF(Map<String,Map<String,EndpointResult>> ep2file2result){
		Map<String,Map<String,EndpointResult>> limit_e2f2r = projectRegexTests(ep2file2result, "limit100001");
		limit_e2f2r = projectRegexTests(limit_e2f2r, "warm", true);
		Map<String,Map<String,EndpointResult>> f_limit_e2f2r = filterEndpointsAllTests(limit_e2f2r, new EndpointExceptionFilter());
		f_limit_e2f2r = filterEndpointsAllTests(f_limit_e2f2r, new EndpointResultRangeFilter(99000,100002));
		
		_log.info("Map : "+f_limit_e2f2r.size());
		printCDF(f_limit_e2f2r, new Time());
	}
	
	static void printSumColdAskCDF(Map<String,Map<String,EndpointResult>> ep2file2result){
		Map<String,Map<String,EndpointResult>> ask_cold_e2f2r = projectRegexTests(ep2file2result, "ask");
		ask_cold_e2f2r = projectRegexTests(ask_cold_e2f2r, "warm", true);
		Map<String,Map<String,EndpointResult>> f_ask_cold_e2f2r = filterEndpointsAllTests(ask_cold_e2f2r, new EndpointExceptionFilter());
		f_ask_cold_e2f2r = filterEndpointsAllTests(f_ask_cold_e2f2r, new EndpointResultRangeFilter(-1,-1));
		
		Map<String,Map<String,EndpointResult>> sum = sumTests(f_ask_cold_e2f2r);
		
		_log.info("Map : "+sum.size());
		printCDF(sum, new MillisecondPerResult());
	}
	
	static void printAskCDF(Map<String,Map<String,EndpointResult>> ep2file2result){
		Map<String,Map<String,EndpointResult>> ask_e2f2r = projectRegexTests(ep2file2result, "ask");
		Map<String,Map<String,EndpointResult>> f_ask_e2f2r = filterEndpointsAllTests(ask_e2f2r, new EndpointExceptionFilter());
		f_ask_e2f2r = filterEndpointsAllTests(f_ask_e2f2r, new EndpointResultRangeFilter(-1,-1));
		
		_log.info("Map : "+f_ask_e2f2r.size());
		printCDF(f_ask_e2f2r, new MillisecondPerResult());
	}
	
	static void printCDF(Map<String,Map<String,EndpointResult>> ep2file2result, EndpointMetric em){
		if(!ep2file2result.isEmpty()){
			Set<String> endpoints = ep2file2result.keySet();
			Set<String>	tests = ep2file2result.values().iterator().next().keySet();
			TreeSet<String> temp = new TreeSet<String>();
			temp.addAll(tests);
			tests = temp;
			
			String[] testsArray = new String[tests.size()];
			tests.toArray(testsArray);
			
			_log.info("Endpoints : "+endpoints.size());
			_log.info("Tests : "+tests.size());
			
			ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>(tests.size());
			for(int i=0; i<tests.size(); i++){
				data.add(new ArrayList<Double>());
			}
			System.out.println();
			
			
			for(String ep:endpoints){
				Map<String,EndpointResult> f2r = ep2file2result.get(ep);
				if(f2r==null){
					throw new RuntimeException("No result for endpoint "+ep+" in any test.");
				}
				for(int i=0; i<testsArray.length; i++){
					EndpointResult er = f2r.get(testsArray[i]);
					if(er!=null){
						data.get(i).add(em.compute(er));
					} else{
						throw new RuntimeException("No result for endpoint "+ep+" in test "+testsArray[i]);
					}
				}
			}
			
			int size = data.get(0).size();
			
			for(ArrayList<Double> o:data){
				if(o.size()!=size){
					throw new RuntimeException("Jagged data for CDF.");
				}
				Collections.sort(o);
			}
			
			double incr = (double)1/(double)size;
			
			for(String t:testsArray){
				System.out.print(t+"\t");
			}
			System.out.println("cdf");
			
			for(int i=0; i<size; i++){
				for(ArrayList<Double> o:data){
					System.out.print(o.get(i)+"\t");
				}
				System.out.println(((double)(i+1)*incr));
			}
			System.out.println();
		}
	}
	
	static void printLimitSummary(Map<String,Map<String,EndpointResult>> ep2file2result){
		Map<String,Map<String,EndpointResult>> limit_e2f2r = projectRegexTests(ep2file2result, "limit");
		limit_e2f2r = projectRegexTests(limit_e2f2r, "warm");
		Map<String,Map<String,EndpointResult>> f_limit_e2f2r = filterEndpointsAllTests(limit_e2f2r, new EndpointExceptionFilter());
		f_limit_e2f2r = filterEndpointsAllTests(f_limit_e2f2r, new EndpointFullResultFilter());
		
		_log.info("Map : "+f_limit_e2f2r.size());
		
		if(!f_limit_e2f2r.isEmpty()){
			Set<String> endpoints = f_limit_e2f2r.keySet();
			Set<String>	tests = f_limit_e2f2r.values().iterator().next().keySet();
			
			_log.info("Endpoints : "+endpoints.size());
			_log.info("Tests : "+tests.size());
			
			
			for(String t:tests){
				System.out.print("\t"+t);
			}
			System.out.println();
			
			for(String ep:endpoints){
				System.out.print(ep);
				Map<String,EndpointResult> f2r = f_limit_e2f2r.get(ep);
				if(f2r==null){
					throw new RuntimeException("No result for endpoint "+ep+" in any test.");
				}
				for(String t:tests){
					EndpointResult er = f2r.get(t);
					if(er!=null){
						System.out.print("\t"+Long.toString(er.exec));
					} else{
						throw new RuntimeException("No result for endpoint "+ep+" in test "+t);
					}
				}
				System.out.println();
			}
			
		}
	}
	
	static Map<String,Map<String,EndpointResult>> sumTests(Map<String,Map<String,EndpointResult>> ep2file2result){
		Map<String,Map<String,EndpointResult>> sum = new HashMap<String,Map<String,EndpointResult>>();
		
		for(Map.Entry<String,Map<String,EndpointResult>> e2f2r : ep2file2result.entrySet()){
			int results = 0;
			long exec = 0;
			long total = 0;
			long b4 = 0;
			String excep = null;
			
			for(Map.Entry<String, EndpointResult> f2r : e2f2r.getValue().entrySet()){
				results += f2r.getValue().results;
				exec += f2r.getValue().exec;
				total += f2r.getValue().total;
				b4 += f2r.getValue().b4;
				excep = f2r.getValue().exception;
			}

			EndpointResult er = new EndpointResult("n/a", "sum", e2f2r.getKey(), results, b4, exec);
			er.setException(excep);
			er.setTotal(total);

			Map<String,EndpointResult> summap = new HashMap<String,EndpointResult>();
			summap.put(er.log,er);
			sum.put(er.endpoint, summap);
		}
		
		return sum;
	}
	
	static Map<String,Map<String,EndpointResult>> projectRegexTests(Map<String,Map<String,EndpointResult>> ep2file2result, String regex){
		return projectRegexTests(ep2file2result, regex, false);
	}
	
	
	static Map<String,Map<String,EndpointResult>> projectRegexTests(Map<String,Map<String,EndpointResult>> ep2file2result, String regex, boolean negate){
		Map<String,Map<String,EndpointResult>> ep2file2result_f = new HashMap<String,Map<String,EndpointResult>>(); 
		
		for(Map.Entry<String,Map<String,EndpointResult>> e: ep2file2result.entrySet()){
			Map<String,EndpointResult> file2result = new HashMap<String,EndpointResult>();
			for(Map.Entry<String, EndpointResult> f:e.getValue().entrySet()){
				if(negate ^ f.getKey().contains(regex)){
					file2result.put(f.getKey(), f.getValue());
				}
			}
			if(!file2result.isEmpty()){
				ep2file2result_f.put(e.getKey(),file2result);
			}
		}
		
		return ep2file2result_f;
	}
	
	static Map<String,Map<String,EndpointResult>> projectTests(Map<String,Map<String,EndpointResult>> ep2file2result, String[] tests){
		Map<String,Map<String,EndpointResult>> ep2file2result_p = new HashMap<String,Map<String,EndpointResult>>(); 
		
		HashSet<String> project = new HashSet<String>();
		for(String test:tests)
			project.add(test);
		
		for(Map.Entry<String,Map<String,EndpointResult>> e: ep2file2result.entrySet()){
			Map<String,EndpointResult> file2result = new HashMap<String,EndpointResult>();
			file2result.putAll(e.getValue());
			retainKeys(file2result,project);
			if(!file2result.isEmpty()){
				ep2file2result_p.put(e.getKey(),file2result);
			}
		}
		
		return ep2file2result_p;
	}
	
	static <E> void removeKeys(Map<E,?> map, Collection<E> remove){
		for(E r:remove){
			map.remove(r);
		}
	}
	
	static <E> void retainKeys(Map<E,?> map, Set<E> retain){
		ArrayList<E> remove = new ArrayList<E>();
		for(E key:map.keySet()){
			if(!retain.contains(key))
				remove.add(key);
		}
		removeKeys(map,remove);
	}
	
	static Map<String,Map<String,EndpointResult>> filterEndpointsAllTests(Map<String,Map<String,EndpointResult>> ep2file2result, EndpointFilter ef){
		Map<String,Map<String,EndpointResult>> ep2file2result_f = new HashMap<String,Map<String,EndpointResult>>();
		for(Map.Entry<String,Map<String,EndpointResult>> e: ep2file2result.entrySet()){
			boolean filter = false;
			for(Map.Entry<String, EndpointResult> f: e.getValue().entrySet()){
				if(!ef.passesFilter(f.getValue())){
					filter = true;
					break;
				}
			}
			
			if(!filter){
				ep2file2result_f.put(e.getKey(), e.getValue());
			}
		}
		return ep2file2result_f;
	}
	
	static void filter(String in_dir_name, Set<String> endpoints, String out_dir_name) throws IOException{
		File in_dir = new File(in_dir_name);
		if(!in_dir.exists() || !in_dir.isDirectory()){
			throw new RuntimeException(in_dir_name+" is not a directory");
		}
		
		File out_dir = new File(out_dir_name);
		if(!out_dir.exists()){
			out_dir.mkdirs();
		} else if(!out_dir.isDirectory()){
			throw new RuntimeException(out_dir_name+" is not a directory");
		}

		File[] files = in_dir.listFiles();

		for(File f:files){
			if(f.getName().endsWith(SUFFIX)){
				_log.info("Filtering "+f);
				String output_fn = out_dir_name+"/"+f.getName();
				File output_file = new File(output_fn);
				int written = filterFile(f,endpoints,output_file);
				_log.info("...done. Wrote "+written+" to "+output_fn);
			}
		}
	}
	
	static void load(String dir_name, Map<String,Map<String,EndpointResult>> file2ep2result, Map<String,Map<String,EndpointResult>> ep2file2result, Set<String> endpoints) throws IOException{
		File dir = new File(dir_name);
		if(!dir.exists() || !dir.isDirectory()){
			throw new RuntimeException(dir_name+" is not a directory");
		}

		File[] files = dir.listFiles();

		for(File f:files){
			if(f.getName().endsWith(SUFFIX)){
				_log.info("Loading "+f);
				ArrayList<EndpointResult> results = parseResult(f, endpoints);
				_log.info("... loaded "+results.size()+" from "+f);

				Map<String,EndpointResult> ep2result = new HashMap<String,EndpointResult>();
				file2ep2result.put(f.getName(), ep2result);

				for(EndpointResult er:results){
					if(ep2result.put(er.endpoint, er)!=null){
						_log.warning("++Multiple entries for "+er.endpoint+" in "+f);
					}

					Map<String,EndpointResult> file2result =  ep2file2result.get(er.endpoint);
					if(file2result==null){
						file2result = new HashMap<String,EndpointResult>();
						ep2file2result.put(er.endpoint, file2result);
					}
					if(file2result.put(f.getName(), er)!=null){
						_log.warning("**Multiple entries for "+er.endpoint+" in "+f);
					}
				}
			}
		}
	}


	static ArrayList<EndpointResult> parseResult(File file, Set<String> endpoints) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(file));
		ArrayList<EndpointResult> results = new ArrayList<EndpointResult>();
		String line = null;

		String fname = file.getName();

		while((line = br.readLine())!=null){
			line = line.trim();
			if(!line.isEmpty()){
				String[] tuple = line.split("\t");
				try{
					if(endpoints==null || endpoints.contains(tuple[1])){
						EndpointResult er = new EndpointResult(fname, tuple[0], tuple[1], Integer.parseInt(tuple[2]), Long.parseLong(tuple[3]), Long.parseLong(tuple[4]));
						if(tuple[5].equals(EXCEPTION)){
							er.setException(tuple[6]);
						} else{
							er.setTotal(Long.parseLong(tuple[5]));
						}
						results.add(er);
					}
				} catch(Exception e){
					_log.warning("Error parsing "+line);
				}
			}
		}
		br.close();
		return results;
	}
	
	static int filterFile(File input, Set<String> endpoints, File output) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(input));
		String line = null;
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(output));
		
		int wrote = 0;
		
		while((line = br.readLine())!=null){
			line = line.trim();
			if(!line.isEmpty()){
				String[] tuple = line.split("\t");
				try{
					if(endpoints==null || endpoints.contains(tuple[1])){
						bw.write(line+"\n");
						wrote++;
					}
				} catch(Exception e){
					_log.warning("Error parsing "+line);
				}
			}
		}
		
		br.close();
		bw.close();
		
		return wrote;
	}
	
	static Set<String> loadEndpoints(String endpoints) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(endpoints));
		Set<String> eps = new HashSet<String>();
		String line = null;

		while((line = br.readLine())!=null){
			line = line.trim();
			if(!line.isEmpty()){
				eps.add(line);
			}
		}
		br.close();

		return eps;
	}
	
	public static class MillisecondPerResult implements EndpointMetric {
		public double compute(EndpointResult er){
			return (double) er.exec / (double) er.results; 
		}
	}
	
	public static class Time implements EndpointMetric {
		public double compute(EndpointResult er){
			return (double) er.exec; 
		}
	}
	
	public static interface EndpointMetric {
		public double compute(EndpointResult er);
	}

	public static class EndpointExceptionFilter implements EndpointFilter {
		public boolean passesFilter(EndpointResult er){
			return er.exception==null;
		}
	}
	
	public static class EndpointFullResultFilter implements EndpointFilter {
		public static final double RESULTS_MET = 0.99;
		public static final int[] LIMITS = new int[]{ 100001, 50000, 25000, 12500, 6250, 3125};
		
		public boolean passesFilter(EndpointResult er){
			for(int limit:LIMITS){
				if(er.test.contains(Integer.toString(limit))){
					if((double) er.results > (double)limit*RESULTS_MET)
						return true;
					return false;
				}
			}
			throw new RuntimeException("Unrecognised test "+er.test);
		}
	}
	
	public static class EndpointResultRangeFilter implements EndpointFilter {
		int minIncl;
		int maxIncl;
		
		public EndpointResultRangeFilter(int equals){
			this(equals, equals);
		}
		
		public EndpointResultRangeFilter(int minIncl, int maxIncl){
			this.minIncl = minIncl;
			this.maxIncl = maxIncl;
		}
		
		public boolean passesFilter(EndpointResult er){
			return (er.results >= minIncl ) && (er.results <= maxIncl);
		}
		
		public String toString(){
			if(minIncl==maxIncl)
				return Integer.toString(minIncl);
			else return +minIncl+"--"+maxIncl;
		}
	}

	
	public static interface EndpointFilter {
		public boolean passesFilter(EndpointResult er);
	}
	
	public static class EndpointResult {

		public EndpointResult(String log, String test, String endpoint, int results, long b4, long exec){
			this.log = log;
			this.test = test;
			this.endpoint = endpoint;
			this.results = results;
			this.b4 = b4;
			this.exec = exec;
		}

		public void setException(String exception){
			this.exception = exception;
		}

		public void setTotal(long total){
			this.total = total;
		}
		
		public String toString(){
			if(exception!=null)
				return log+"\t"+test+"\t"+endpoint+"\t"+results+"\t"+b4+"\t"+exec+"\t"+EXCEPTION+"\t"+exception;
			return log+"\t"+test+"\t"+endpoint+"\t"+results+"\t"+b4+"\t"+exec+"\t"+total;
		}

		String log;
		String test;
		String endpoint;
		int results;
		long b4;
		long exec;
		long total;
		String exception;
	}
}
