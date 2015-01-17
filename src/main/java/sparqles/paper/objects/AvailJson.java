package sparqles.paper.objects;

import java.util.Date;

public class AvailJson {
	private boolean isAvailable = false;
	private EndpointResult endpointResult;
	
	public AvailJson(){}
	
	public boolean isAvailable() {
		return isAvailable;
	}
	public void setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}
	public EndpointResult getEndpointResult() {
		return endpointResult;
	}
	public void setEndpointResult(EndpointResult endpointResult) {
		this.endpointResult = endpointResult;
	}
	
	public String getSPARQLURI(){
		if(endpointResult!=null&& endpointResult.getEndpoint()!=null)return endpointResult.getEndpoint().getUri();
		return null;
	}
	
	public Date getStartDate(){
		if(endpointResult!=null)return new Date(endpointResult.getStart());
		return null;
	}
	public Date getEndDate(){
		if(endpointResult!=null)return new Date(endpointResult.getEnd());
		return null;
	}

	public class EndpointResult{
		private long start;
		private long end; 
		private Endpoint endpoint;
		
		public long getStart() {
			return start;
		}

		public void setStart(long start) {
			this.start = start;
		}

		public long getEnd() {
			return end;
		}

		public void setEnd(long end) {
			this.end = end;
		}

		public Endpoint getEndpoint() {
			return endpoint;
		}

		public void setEndpoint(Endpoint endpoint) {
			this.endpoint = endpoint;
		}

		public EndpointResult(){}
	}
	
	public class Endpoint{
		private String uri;
		
		public Endpoint(){}

		public String getUri() {
			return uri;
		}

		public void setUri(String uri) {
			this.uri = uri;
		}
	}
}


