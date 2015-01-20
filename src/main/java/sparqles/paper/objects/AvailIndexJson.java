package sparqles.paper.objects;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AvailIndexJson {
	List<AvailKeyValue> availability = new ArrayList<AvailKeyValue>();
	
	
	public void addHeader(String header){
		String[] parts = header.split("\\t");
		for (int i = 1; i < parts.length; i++) {
			AvailKeyValue record = new AvailKeyValue(parts[i], i);
			availability.add(record);
		}
	}
	
	public void addValue(String value){
		try {
			String[] parts = value.split("\\t");
			if(parts.length>1){
				int previousPart=0;
				for (int i=1; i<parts.length; i++) {
					SimpleDateFormat parserSDF=new SimpleDateFormat("MMM-yy");
					final Date date =parserSDF.parse(parts[0]);
					final int currentPart = Integer.parseInt(parts[i]);
					final int finalPreviousPart = previousPart;
					for (AvailKeyValue record : availability) {
						if(record.getIndex()==i){
								record.getValues().add(new ArrayList<Long>(){{add(date.getTime());add(new Long(currentPart-finalPreviousPart));}});
							break;
						}
					}
					previousPart=Integer.parseInt(parts[i]);
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	private class AvailKeyValue{
		private String key=null;
		private int index=0;
		private List<List<Long>> values = new ArrayList<List<Long>>();
		
		public AvailKeyValue(String key, int index){this.key=key; this.index=index;}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public List<List<Long>> getValues() {
			return values;
		}

	}

}
