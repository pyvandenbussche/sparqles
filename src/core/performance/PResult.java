package core.performance;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericData.Record;

import core.EndpointResult;
import core.discovery.DResultGET;

public class PResult extends EndpointResult {

	private long _b4Time;
	private Object _frestout;
	private long _exectout;
	private int _sols;
	private long _execTime;
	private long _closeTime;
	private String _testid;
	private String _query;

	public void setFirstResTOut(long firstResTimeout) {
		_frestout = firstResTimeout;
	}

	public void setExecTOut(long executionTimeout) {
		_exectout = executionTimeout;
	}

	public void setSolutions(int sols) {
		_sols = sols;
	}

	public void setInitTime(long b4Time) {
		_b4Time=b4Time; 
	}

	public void setExecTime(long execTime) {
		_execTime = execTime;
	}

	public void setCloseTime(long closeTime) {
		_closeTime = closeTime;
	}

	public void setTestID(String testId) {
		_testid= testId;
	}

	public void setQuery(String query) {
		_query = query;
	}
	
	@Override
	public Record serialize() {
		GenericData.Record record = new GenericData.Record(DResultGET.SCHEMA);

		record.put("endpointResult", super.serialize());
		record.put("frestout", _frestout);
		record.put("solutions",_sols);
		record.put("inittime", _b4Time);
		record.put("exectime", _execTime);
		record.put("closetime", _closeTime);
		record.put("exectout", _exectout);
		record.put("testid", _testid);
		record.put("query", _query);
		return record;
		 
	}
}
