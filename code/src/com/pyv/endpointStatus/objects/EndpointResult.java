package com.pyv.endpointStatus.objects;

public class EndpointResult {
	private String datasetURI=null;
	private String packageId=null;
	private String endpointURL=null;
	private String datasetTitle=null;
	private String responseTime=null;
	private boolean isAvailable=false;
	private boolean isPrivate=false;
	private String explaination=null;
	
	public EndpointResult(String datasetURI, String packageId,String datasetTitle, String endpointURL){
		this.datasetURI=datasetURI;
		this.packageId=packageId;
		this.datasetTitle=datasetTitle;
		this.endpointURL = endpointURL;
	}
	
	public EndpointResult(String datasetURI){
		this.datasetURI=datasetURI;
	}
	
	public EndpointResult(){
	}

	public String getDatasetURI() {
		return datasetURI;
	}

	public void setDatasetURI(String datasetURI) {
		this.datasetURI = datasetURI;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public String getPackageId() {
		return packageId;
	}

	public void setPackageId(String packageId) {
		this.packageId = packageId;
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

	public String getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(String responseTime) {
		this.responseTime = responseTime;
	}

	public boolean isAvailable() {
		return isAvailable;
	}

	public void setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}

	public String getExplaination() {
		return explaination;
	}

	public void setExplaination(String explaination) {
		this.explaination = explaination;
	}
	
}
