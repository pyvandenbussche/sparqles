package core;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DBManager {

	
	public static final String CREATE_RESULT="CREATE TABLE IF NOT EXISTS results (Endpoint VARCHAR(256), Task VARCHAR(256), Result CLOB, Date TIMESTAMP,  UNIQUE(Endpoint, Date));";
	
	private Connection con;
	
	public DBManager() throws SQLException {
		setup();
	}
	 
	
	

	private void setup() throws SQLException {
		try {
			Class.forName("org.h2.Driver");
			con = DriverManager.getConnection("jdbc:h2:./testdb.h2");
				
			con.createStatement().execute(CREATE_RESULT);
	
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	
	
	public boolean insertResult(EndpointResult result){
		try {
			PreparedStatement prep = con.prepareStatement("INSERT INTO results (Endpoint, Task, Result, Date) VALUES (?,?,?,?)");
			
			prep.setString(1, result.getEndpoint().getEndpointURI().toString());
			prep.setString(2, result.getTask().getClass().getSimpleName());
			prep.setString(3, result.serialize().toString());
			prep.setTimestamp(4, new java.sql.Timestamp(result.getDate().getTime()));
			
			System.out.println(prep);
			int insertedRecordsCount = prep.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
//	
//	
//	public List<EndpointResult> getResults(Endpoint ep){
//		ArrayList<EndpointResult> reslist = new ArrayList<EndpointResult>();
//		try {
//			Statement st = con.createStatement();
//			ResultSet res = st.executeQuery("SELECT * FROM results WHERE Endpoint="+ep.getEndpointURI().toString());
//			while(res.next()){
//				System.out.println(res.getString(3));
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	
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
}
