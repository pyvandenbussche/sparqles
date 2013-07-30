package core;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.util.FmtUtils;

import core.availability.AResult;
import core.discovery.DResult;
import core.performance.PResult;


/**
 * (De)Serialises avro results
 * @author Juergen Umbrich <jueumb@gmail.com>
 *
 */
public class FileManager {
	private static final Logger log = LoggerFactory.getLogger(FileManager.class);
	private HashMap<String, Map<String, File>> eptask;

	File rootFolder = new File("./ondisk");

	public FileManager() {
		init();
	}


	private void init() {
		eptask = new HashMap<String, Map<String,File>>();

		if(!rootFolder.exists()) rootFolder.mkdirs();
		if(rootFolder.isFile()){ log.warn("The specified folder {} is not a directory", rootFolder);
			return;
		}
		for( File f: rootFolder.listFiles()){
			String name = f.getName().replace(".avro", "");
			try {
				String ep = URLDecoder.decode(name.substring(0, name.lastIndexOf(".")),  "UTF-8");
				String task = name.substring(name.lastIndexOf(".")+1);

				put(ep,task,f);


			} catch (UnsupportedEncodingException e) {
				log.warn("UnsupportedEncodingException: {} for {}", e.getMessage(),f );
			}
		}
	}


	private void put(String ep, String task, File f) {
		Map<String, File> map =eptask.get(ep);
		if(map==null){
			map = new HashMap<String, File>();
			eptask.put(ep, map);
		}
		if(map.containsKey(task))
			log.warn("Duplicate entry for {}", f );
		map.put(task, f);
	}


	public <V> List<V> readResults(String endpointURI,Class<V> cls){
		try {
			return readResults(EndpointFactory.newEndpoint(new URI(endpointURI)),cls);
		} catch (URISyntaxException e) {
			Object[] t = {e.getClass().getSimpleName(), e.getMessage(), cls.getSimpleName(), endpointURI};
			log.error("{}:{} during deserialisation of {} results for {}", t);
			return new ArrayList<V>();
		}
	}
	
	public <V> List<V> readResults(Endpoint ep,Class<V> cls){
		List<V> l = new ArrayList<V>();
		File f = getFile(ep, cls.getSimpleName());
		DatumReader<V> reader = new SpecificDatumReader<V>(cls);
		try {
			DataFileReader<V> dfr = new DataFileReader<V>(f, reader);
			while(dfr.hasNext()){
				l.add(dfr.next());
			}
		} catch (IOException e) {
			Object[] t = {e.getClass().getSimpleName(), e.getMessage(), cls.getSimpleName(),ep.getUri().toString()};
			log.error("{}:{} during deserialisation of {} results for {}", t);
		}
		Object[] t = {l.size(), cls.getSimpleName(), ep.getUri().toString()};
		log.info("Deserialised {} {} results for {}",t);
		return l;
	}
	
	public <V extends SpecificRecordBase> boolean writeResult(V res) {
		if(res instanceof DResult) return writeResult((DResult)res);
		if(res instanceof AResult) return writeResult((AResult)res);
		if(res instanceof PResult) return writeResult((PResult)res);
		return true;
	}

	public boolean writeResult(DResult res) {
		return writeResult(res.getEndpointResult().getEndpoint(), res.getClass().getSimpleName(), (SpecificRecordBase)res);
	}
	public boolean writeResult(AResult res) {
		return writeResult(res.getEndpointResult().getEndpoint(), res.getClass().getSimpleName(), (SpecificRecordBase)res);
	}
	public boolean writeResult(PResult res) {
		return writeResult(res.getEndpointResult().getEndpoint(), res.getClass().getSimpleName(), (SpecificRecordBase) res);
	}

	public<V extends SpecificRecordBase> boolean writeResult(Endpoint ep, String task, V  result){
		log.debug("[STORE] {}", result);

		try {
			DatumWriter<V> d = new SpecificDatumWriter<V>((Class<V>) result.getClass());
			DataFileWriter<V> dfw = new DataFileWriter<V>(d);

			File f = getFile(ep,task);
			
			if(f==null){
				f = createFile(ep,task);
				put(ep.getUri().toString(),task,f);
				dfw.create(result.getSchema(), f);
			}else{
				dfw = dfw.appendTo(f);
			}
			dfw.append(result);
			dfw.close();
			log.debug("[STORED] {}", result);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			log.warn("[STORE] {}", e);
			return false;
		}

	}


	private File createFile(Endpoint ep, String task) {
		try {
			return new File(rootFolder, URLEncoder.encode(ep.getUri().toString(), "UTF-8")+"."+task+".avro");
		} catch (UnsupportedEncodingException e) {
			log.warn("UnsupportedEncodingException: {} for {}", e.getMessage(), ep.getUri().toString() );
		}
		return null;
	}


	private File getFile(Endpoint ep, String task) {
		Map<String, File> map = eptask.get(ep.getUri().toString());
		if(map==null){
			return null;
		}
		return map.get(task);
	}


	public static int writeSPARQLResults(ResultSet results, String queryFile,
			Endpoint ep, Long start) {
		
		PrintWriter out = getPARQLResultPrintStream(ep, queryFile, start);
		int sols=0;
		while (results.hasNext())
        {
            QuerySolution qs = results.nextSolution();
            out.println(toString(qs, sols == 0));
            sols++;
        }
		out.close();
		return sols;
	}
	
	public static int writeSPARQLResults(Iterator<Triple> triples,
			String _queryFile, Endpoint _ep, Long start) {
		PrintWriter out = getPARQLResultPrintStream(ep, queryFile, start);
		int sols=0;
		while (triples.hasNext())
        {
            out.println(triples.next());
            sols++;
        }
		out.close();
		return sols;
	}
	
	private static PrintWriter getPARQLResultPrintStream(Endpoint ep,
			String queryFile, Long start) {
		// TODO Auto-generated method stub
		return null;
	}



	
	
	private static String toString(QuerySolution qs, boolean first)
    {
        StringBuffer vars = new StringBuffer();
        StringBuffer sb = new StringBuffer();
        Iterator<String> varns = qs.varNames();
        while (varns.hasNext())
        {
            String varName = varns.next();
            if (first)
            {
                vars.append(varName + "\t");
            }
            sb.append(FmtUtils.stringForObject(qs.get(varName)) + "\t");
        }

        if (first)
            return vars.toString() + "\n" + sb.toString();
        return sb.toString();
    }


	
}
