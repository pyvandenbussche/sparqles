package sparqles.core;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.data.Json;
import org.junit.Test;

public class IndexTEST {

	@Test
	public void test() {
		Index idx = new Index();
		idx.setLastUpdate(System.currentTimeMillis());
		List<Availability> a = new ArrayList<Availability>();
		idx.setAvailability(a);
		List<Object> m = new ArrayList<Object>();
		String[] o = {"test","test"};
		m.add(o);
		a.add(new Availability("key", m));
		System.out.println(idx);
	}

}
