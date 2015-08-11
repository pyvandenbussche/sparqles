package sparqles.analytics;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import sparqles.paper.objects.AvailEp;
import sparqles.paper.objects.AvailEpFromList;
import sparqles.paper.objects.AvailEvolMonthList;
import sparqles.paper.objects.AvailJson;

import com.google.gson.Gson;

public class AEvol  {
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new AEvol(args);
	}
	
	public AEvol(String[] args) {
		try {
			Gson gson = new Gson();
			
			//check if there is any stat to run or if it is up to date
			//open connection to mongodb aEvol collection
			
			
			
			//read the list of endpoints
			String json = readUrl("http://sparqles.ai.wu.ac.at/api/endpoint/list");
			AvailEpFromList[] epArray = gson.fromJson(json, AvailEpFromList[].class);
			List<String> epList = new ArrayList<>();
			for (int i = 0; i < epArray.length; i++) {
				epList.add(epArray[i].getUri());
//				System.out.println(epArray[i].getUri());
			}
			
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private static String readUrl(String urlString) throws Exception {
	    BufferedReader reader = null;
	    try {
	        URL url = new URL(urlString);
	        reader = new BufferedReader(new InputStreamReader(url.openStream()));
	        StringBuffer buffer = new StringBuffer();
	        int read;
	        char[] chars = new char[1024];
	        while ((read = reader.read(chars)) != -1)
	            buffer.append(chars, 0, read); 

	        return buffer.toString();
	    } finally {
	        if (reader != null)
	            reader.close();
	    }
	}
	
}
