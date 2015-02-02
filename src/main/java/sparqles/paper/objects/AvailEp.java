package sparqles.paper.objects;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AvailEp {
	private String epURI=null;
	private List<AvailEpMonthRecord> records = new ArrayList<AvailEpMonthRecord>();
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
	private static DecimalFormat df = new DecimalFormat("0.####",new DecimalFormatSymbols(Locale.US));
	
	public AvailEp(String epURI){this.epURI=epURI;}
	public String getEpURI() {
		return epURI;
	}
	public void setEpURI(String epURI) {
		this.epURI = epURI;
	}
	public List<AvailEpMonthRecord> getRecords() {
		return records;
	}
	public void setRecords(List<AvailEpMonthRecord> records) {
		this.records = records;
	}
	public void addResult(Date date, boolean isAvailable){
		AvailEpMonthRecord record = new AvailEpMonthRecord(sdf.format(date));
		int indexMonth = records.indexOf(record);
		if(indexMonth>-1){//means it already exists
			records.get(indexMonth).addTest(isAvailable);		
		}
		else{
			record.addTest(isAvailable);
			records.add(record);
		}
		
	}
	public List<String[]> getAvailPerMonth(){
		List<String[]> availPerMonthList = new ArrayList<>();
		for (int i = 0; i < records.size(); i++) {
			AvailEpMonthRecord record = records.get(i);
			String[] availPerMonth = new String[]{record.getDate(), df.format(record.getNbSuccessTest()/record.getNbTests())};
			availPerMonthList.add(availPerMonth);
		}
		return availPerMonthList;
	} 
	
	@Override
    public boolean equals(Object object)
    {
        if (object != null && object instanceof AvailEp)return ((AvailEp)object).getEpURI().equals(this.epURI);
        return false;
    }
	
	public void prettyPrint(){
		System.out.println(epURI);
		for (AvailEpMonthRecord record : records) {
			System.out.println("\t["+record.getDate()+"]\t"+df.format(record.getNbSuccessTest()/record.getNbTests()));
		}		
	}
	public void uriPrint(){
		System.out.println(epURI);
	}
	public boolean isAlive(int monthWindow){
		int nbMonthAlive=0;
		Date today = new Date();
		
		for (int i = 0; i < monthWindow; i++) {
			int indexMonth = records.indexOf(new AvailEpMonthRecord(sdf.format(today)));
			if(indexMonth>-1){//means it already exists
				AvailEpMonthRecord record = records.get(indexMonth);	
				if(record.getNbSuccessTest()/record.getNbTests()>0) nbMonthAlive++;
			}
			Calendar c = Calendar.getInstance(); 
			c.setTime(today); 
			c.add(Calendar.MONTH, -1);
			today = c.getTime();
		}
		
		return (nbMonthAlive>0);//will return false for endpoints not present in the window
	}
	
	
	public class AvailEpMonthRecord{
		String date=null; //yyyy-MM
		double nbTests=0;
		double nbSuccessTest=0;
		
		public AvailEpMonthRecord(String date){this.date=date;}
		
		public void addTest(boolean isAvailable){
			nbTests++;
			if(isAvailable)nbSuccessTest++;			
		}

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public double getNbTests() {
			return nbTests;
		}

		public void setNbTests(int nbTests) {
			this.nbTests = nbTests;
		}

		public double getNbSuccessTest() {
			return nbSuccessTest;
		}

		public void setNbSuccessTest(int nbSuccessTest) {
			this.nbSuccessTest = nbSuccessTest;
		}
		
		@Override
	    public boolean equals(Object object)
	    {
	        if (object != null && object instanceof AvailEpMonthRecord)return ((AvailEpMonthRecord)object).getDate().equals(this.date);
	        return false;
	    }
	}
}
