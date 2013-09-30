package sparqles.analytics;

import org.apache.avro.specific.SpecificRecordBase;


public interface Analytics<V extends SpecificRecordBase> {
	
	public boolean analyse(V ep);


}
