package core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.rowset.serial.SerialBlob;

import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.availability.AResult;
import core.discovery.DResult;
import core.performance.PResult;



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
				String task = name.substring(name.lastIndexOf("."));

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
		Log.info("Writing {}", result);

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
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

	}


	private File createFile(Endpoint ep, String task) {
		try {
			return new File(rootFolder, URLEncoder.encode(ep.getUri().toString(), "UTF-8"));
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
}
