package core.availability.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DatasetAnalytics implements Serializable{
	private String datasetURI=null;
	private String datasetTitle=null;
	private String datasetId = null;
	private String endpointURL = null;
	private Integer nbTotalTests=-1;
	private Integer nbTotalTestsTrue=-1;
	private List<AnalyticsPerWeek> analytics = new ArrayList<AnalyticsPerWeek>(50);
	
	
	public String getDatasetURI() {
		return datasetURI;
	}
	public void setDatasetURI(String datasetURI) {
		this.datasetURI = datasetURI;
	}
	public Integer getNbTotalTests() {
		return nbTotalTests;
	}
	public void setNbTotalTests(Integer nbTotalTests) {
		this.nbTotalTests = nbTotalTests;
	}
	public Integer getNbTotalTestsTrue() {
		return nbTotalTestsTrue;
	}
	public void setNbTotalTestsTrue(Integer nbTotalTestsTrue) {
		this.nbTotalTestsTrue = nbTotalTestsTrue;
	}
	public List<AnalyticsPerWeek> getAnalytics() {
		return analytics;
	}
	
	public String getEndpointURL() {
		return endpointURL;
	}
	public void setEndpointURL(String endpointURL) {
		this.endpointURL = endpointURL;
	}
	public String getDatasetTitle() {
		return datasetTitle;
	}
	public void setDatasetTitle(String datasetTitle) {
		this.datasetTitle = datasetTitle;
	}
	public String getDatasetId() {
		return datasetId;
	}
	public void setDatasetId(String datasetId) {
		this.datasetId = datasetId;
	}
	public AnalyticsPerWeek getAnalyticsWithDate(String date) {
		for(int i =0; i< analytics.size(); i++){
			if(analytics.get(i).getDate().equals(date))return analytics.get(i);
		}
		return null;
	}
	
	public void setAnalytics(List<AnalyticsPerWeek> analytics) {
		this.analytics = analytics;
	}
	
	public double getRatio(){
		return Double.parseDouble(""+nbTotalTestsTrue)/Double.parseDouble(""+nbTotalTests);
	}
}


