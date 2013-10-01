package sparqles.analytics;

import org.apache.avro.specific.SpecificRecordBase;



/**
 * Interface for all Analytics tasks.
 * @author umbrichj
 *
 * @param <V> either one of {@link AResult}, {@link DResult}, 
 * {@link FResult} or {@link PResult}
 */
public interface Analytics<V extends SpecificRecordBase> {
	
	/**
	 * 
	 * @param result - the result to analyse
	 * @return true in case of success, false otherwise
	 */
	boolean analyse(V result);


}
