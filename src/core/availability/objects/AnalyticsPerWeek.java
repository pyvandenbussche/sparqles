package core.availability.objects;

import java.io.Serializable;

public class AnalyticsPerWeek implements Serializable {
	private String date=null;
	private Integer nbTests=-1;
	private Integer nbTestsTrue=-1;
	
	
	
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public Integer getNbTests() {
		return nbTests;
	}
	public void setNbTests(Integer nbTests) {
		this.nbTests = nbTests;
	}
	public Integer getNbTestsTrue() {
		return nbTestsTrue;
	}
	public void setNbTestsTrue(Integer nbTestsTrue) {
		this.nbTestsTrue = nbTestsTrue;
	}
}
