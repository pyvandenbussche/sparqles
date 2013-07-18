package core.discovery;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericData.Record;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.stats.Count;

import utils.AvroUtils;

import core.AvroSerialize;
import core.Endpoint;
import core.EndpointResult;

public class DResultGET extends EndpointResult  {




	private final static String SPARQLDESCNS = "http://www.w3.org/ns/sparql-service-description#";
	private final static String VOIDNS = "http://rdfs.org/ns/void#";



	private int sparqldescNS=0, voidNS=0;
	private final Count<Node> voidPred = new Count<Node>();
	private final Count<Node> spdsPred = new Count<Node>();
	private String _status;
	private String _type;

	public void handlePredicate(Node p) {
		if(p.toString().startsWith(SPARQLDESCNS)){
			sparqldescNS++;
			spdsPred.add(p);
		}
		if(p.toString().startsWith(VOIDNS)){
			voidNS++;
			voidPred.add(p);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("DResultGET:\n");
		sb.append(super.toString());
		sb.append("  ___________\n");
		sb.append("  status:\t").append(_status).append("\n");
		sb.append("  type:\t").append(_type).append("\n");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		spdsPred.printOrderedStats(ps);
		sb.append("  SPARQLDESCterms:\t").append(sparqldescNS).append("\n");
		sb.append(baos.toString()).append("\n");

		baos = new ByteArrayOutputStream();
		ps = new PrintStream(baos);
		voidPred.printOrderedStats(ps);
		sb.append("  VOIDterms:\t").append(voidNS).append("\n");
		sb.append(baos.toString()).append("\n");

		return sb.toString();
	}

	public void responseCode(String status) {
		_status = status;

	}

	public void responseType(String type) {
		_type = type;
	}

	@Override
	public Record serialize() {
		GenericData.Record record = new GenericData.Record(DResultGET.SCHEMA);

		record.put("endpointResult", super.serialize());
		record.put("SPARQLDESCterms" , sparqldescNS);
		record.put("VOIDterms" , voidNS);
		record.put("SPARQLDESCpreds" , serialisableMap(spdsPred));
		record.put("voiDpreds" , serialisableMap(voidPred));

		return record;
	}

	private Map<String,Integer> serialisableMap(Count<Node> c ){
		Map<String,Integer> m = new HashMap<String, Integer>();
		for(Entry<Node,Integer> ent: c.entrySet()){
			m.put(ent.getKey().toString(), ent.getValue());
		}

		return m;
	}
}