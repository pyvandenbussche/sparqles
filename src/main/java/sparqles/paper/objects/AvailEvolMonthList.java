package sparqles.paper.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AvailEvolMonthList {
//	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
//	private static DecimalFormat df = new DecimalFormat("0.####",new DecimalFormatSymbols(Locale.US));
	private List<AvailEvolMonth> months = new ArrayList<AvailEvolMonth>();
	
	public void addEp(String date, float avail){
		AvailEvolMonth record = new AvailEvolMonth(date);
		int indexMonth = months.indexOf(record);
		if(indexMonth>-1){//means it already exists
			record = months.get(indexMonth);
		}
		if(avail<=0.05)record.addZero_five();
		else if(avail>0.05 && avail<=0.75)record.addFive_seventyFive();
		else if(avail>0.75 && avail<=0.95)record.addSeventyFive_nintyFive();
		else if(avail>0.95 && avail<=0.99)record.addNintyFive_nintyNine();
		else if(avail>0.99)record.addNintyNine_hundred();
		if(indexMonth<0){//means it does not exist yet
			months.add(record);			
		}
	}
	
	public String csvPrint(){
		StringBuilder sb = new StringBuilder();
		sb.append("month\t0-5\t5-75\t75-95\t95-99\t99-100"+System.getProperty("line.separator"));
		Collections.sort(months);
		for (AvailEvolMonth month : months) {
			sb.append(month.getDate()
					+"\t"+month.getZero_five()
					+"\t"+(month.getZero_five()+month.getFive_seventyFive())
					+"\t"+(month.getZero_five()+month.getFive_seventyFive()+month.getSeventyFive_nintyFive())
					+"\t"+(month.getZero_five()+month.getFive_seventyFive()+month.getSeventyFive_nintyFive()+month.getNintyFive_nintyNine())
					+"\t"+(month.getZero_five()+month.getFive_seventyFive()+month.getSeventyFive_nintyFive()+month.getNintyFive_nintyNine()+month.getNintyNine_hundred())		
					+System.getProperty("line.separator"));
		}
		return sb.toString();
	}
	
	private class AvailEvolMonth implements Comparable<AvailEvolMonth>{
		String date=null; //yyyy-MM
		long zero_five=0;
		long five_seventyFive=0;
		long seventyFive_nintyFive=0;
		long nintyFive_nintyNine=0;
		long nintyNine_hundred=0;
		long nbTotal=0;
		
		public AvailEvolMonth(String date){this.date=date;}
		
		public String getDate() {
			return date;
		}
		
		public long getZero_five() {
			return zero_five;
		}
		public void addZero_five() {
			this.zero_five++;
			this.nbTotal++;
		}
		public long getFive_seventyFive() {
			return five_seventyFive;
		}
		public void addFive_seventyFive() {
			this.five_seventyFive++;
			this.nbTotal++;
		}
		public long getSeventyFive_nintyFive() {
			return seventyFive_nintyFive;
		}
		public void addSeventyFive_nintyFive() {
			this.seventyFive_nintyFive++;
			this.nbTotal++;
		}
		public long getNintyFive_nintyNine() {
			return nintyFive_nintyNine;
		}
		public void addNintyFive_nintyNine() {
			this.nintyFive_nintyNine++;
			this.nbTotal++;
		}
		public long getNintyNine_hundred() {
			return nintyNine_hundred;
		}
		public void addNintyNine_hundred() {
			this.nintyNine_hundred++;
			this.nbTotal++;
		}
		public long getNbTotal() {
			return nbTotal;
		}
		@Override
	    public boolean equals(Object object)
	    {
	        if (object != null && object instanceof AvailEvolMonth)return ((AvailEvolMonth)object).getDate().equals(this.date);
	        return false;
	    }

		@Override
		public int compareTo(AvailEvolMonth arg0) {
			// TODO Auto-generated method stub
			return this.getDate().compareTo(arg0.getDate());
		}
	}
}
