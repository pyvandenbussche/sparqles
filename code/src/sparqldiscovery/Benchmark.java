package sparqldiscovery;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

abstract public class Benchmark {

	private File _logDir;
	private String _benchExp;

	public Benchmark(File logDir, final String benchExperiment) {
		if(!logDir.exists()) logDir.mkdirs();
		_logDir= new File(logDir,benchExperiment);
		if(!_logDir.exists()) _logDir.mkdirs();
		_benchExp = benchExperiment;
	}
	
	public File getLogDir(){
		return _logDir;
	}
	
	
	public void evaluate(){
		evaluate(-1);
	}
	
	public void evaluate(int noEPs){
		try {
			Scanner s = new Scanner(new File("res/347_endpoints_list.txt"));
			FileWriter fw = new FileWriter(new File("results/"+_benchExp+".csv"));
			int c = 0;
			while(s.hasNextLine()){
				c++;
				if(noEPs==-1 || c<= noEPs){
					String line =s.nextLine();
					String [] tt = line.split("\t");
					String res = "0";
					String err= "";
					try{
						System.err.println("Processing "+tt[0]);
						if(benchmark(tt[0])) res = "1";
					}catch(Exception e){
						err += e.getClass().getSimpleName()+":"+e.getMessage();
					}
					fw.write(tt[0]+","+res+","+err+"\n");
					fw.flush();
				}
				else{
					break;
				}
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	abstract boolean benchmark( final String endpointURI) throws Exception;
}
