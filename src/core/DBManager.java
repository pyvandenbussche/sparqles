package core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.rowset.serial.SerialBlob;

import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.availability.AResult;
import core.performance.PResult;



public class DBManager {
	private static final Logger logger = LoggerFactory.getLogger(DBManager.class);
	
	public static final String CREATE_RESULT="CREATE TABLE IF NOT EXISTS results (Endpoint VARCHAR(256), Task VARCHAR(256), Result BLOB, Date TIMESTAMP,  UNIQUE(Endpoint, Date));";
	
	private Connection con;
	
	public DBManager() throws SQLException {
		setup();
	}
	 
	
	

	private void setup() throws SQLException {
		try {
			Class.forName("org.h2.Driver");
			con = DriverManager.getConnection("jdbc:h2:./testdbh2");
				
			con.createStatement().execute(CREATE_RESULT);
	
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public void insertResult(AResult res) {
		insertResult(res.getEndpointResult().getEndpoint().getUri().toString(), res.getClass().getSimpleName(), res, res.getEndpointResult().getStart() );
	}
	public void insertResult(PResult res) {
		insertResult(res.getEndpointResult().getEndpoint().getUri().toString(), res.getClass().getSimpleName(), res, res.getEndpointResult().getStart() );
	}
	
	public boolean insertResult(String epURI, String task, Object  result, Long timestamp){
		Log.info("Inserting {}", result);
		
		try {
			PreparedStatement prep = con.prepareStatement("INSERT INTO results (Endpoint, Task, Result, Date) VALUES (?,?,?,?)");
			
			prep.setString(1, epURI);
			prep.setString(2, task);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Encoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
            SpecificDatumWriter writer = new SpecificDatumWriter(result.getClass());
			writer.write(result, encoder);
			encoder.flush();
			
			prep.setBlob(3, new SerialBlob(outputStream.toByteArray()));
			prep.setTimestamp(4, new java.sql.Timestamp(timestamp));
			
			Log.info("Execute {}", prep);
			int insertedRecordsCount = prep.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean insertEndpoint(Endpoint ep){
		return true;
	}
	
	public void stop() throws SQLException{
		con.close();
	}
	
	public void debug(){
		try {
			Statement st = con.createStatement();
			ResultSet res = st.executeQuery("SELECT * FROM results ");
			while(res.next()){
				System.out.println(res.getString(3));
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public <T> List<T> getResults(Endpoint ep, Class<T> cls) {
		ArrayList<T> reslist = new ArrayList<T>();
		try {
			
			Statement st = con.createStatement();
			String query ="SELECT * FROM results WHERE Endpoint='"+ep.getUri().toString()+"' AND Task='"+cls.getSimpleName()+"';";
			Log.info("Querying {}", query);
			
			ResultSet res = st.executeQuery(query);
			while(res.next()){
				System.out.println("res3: "+res.getString(3));
				Decoder decoder = DecoderFactory.get().binaryDecoder(res.getBinaryStream(3), null);
				DatumReader<T> dr = new SpecificDatumReader<T>(cls);
				
				T t = dr.read(null, decoder);
				reslist.add(t);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return reslist;
	}
}
