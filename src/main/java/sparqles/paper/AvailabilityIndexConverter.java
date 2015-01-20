package sparqles.paper;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import sparqles.paper.objects.AvailEp;
import sparqles.paper.objects.AvailEvolMonthList;
import sparqles.paper.objects.AvailIndexJson;
import sparqles.paper.objects.AvailJson;
import arq.cmdline.CmdGeneral;

import com.google.gson.Gson;

public class AvailabilityIndexConverter extends CmdGeneral  {
	private String availabilityEvoPath=null;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new AvailabilityIndexConverter(args).mainRun();
	}
	
	public AvailabilityIndexConverter(String[] args) {
		super(args);
		getUsage().startCategory("Arguments");
		getUsage().addUsage("availability-evo.dat", "absolute path for the availability-evo.dat csv file  (e.g. /home/...)");
	}
	
	@Override
    protected String getCommandName() {
		return "availability-evo.dat";
	}
	
	@Override
	protected String getSummary() {
		return getCommandName() + " availability-evo.dat (e.g. /home/...)";
	}

	@Override
	protected void processModulesAndArgs() {
		if (getPositional().size() < 1) {
			doHelp();
		}
		availabilityEvoPath = getPositionalArg(0);
	}

	@Override
	protected void exec() {
		try {
			Gson gson = new Gson();
			AvailIndexJson availIndexJson = new AvailIndexJson();
			BufferedReader br = Files.newBufferedReader(Paths.get(availabilityEvoPath), StandardCharsets.UTF_8);
			int cpt=0;
		    for (String line = null; (line = br.readLine()) != null;) {
		    	if(cpt==0){
		    		availIndexJson.addHeader(line);
		    	}
		    	else{
		    		availIndexJson.addValue(line);
		    	}
		    	cpt++;
		    }
		    System.out.println(gson.toJson(availIndexJson));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
//	private void writeFile(String content, String fileName){
//		if(!outputFolderFile.exists())outputFolderFile.mkdir();
//		FileOutputStream fop = null;
//		File file;
//	
//		try {
//	
//			file = new File(outputFolderFile.getAbsolutePath()+"/"+fileName);
//			if(file.exists())file.delete();
//			file.createNewFile();
//			
//			fop = new FileOutputStream(file);
//		
//			// get the content in bytes
//			byte[] contentInBytes = content.getBytes();
//	
//			fop.write(contentInBytes);
//			fop.flush();
//			fop.close();
//	
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				if (fop != null) {
//					fop.close();
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}
	
}
