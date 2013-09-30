package sparqles.analytics;

import org.apache.avro.specific.SpecificRecordBase;

public interface Analytics<V> {
	
	public <V extends SpecificRecordBase> void analyse(V ep);

}
