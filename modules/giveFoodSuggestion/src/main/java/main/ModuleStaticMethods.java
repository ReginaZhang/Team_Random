package main;


import java.util.HashMap;
import java.sql.*;

import com.mysql.jdbc.jdbc2.optional.*;

public class ModuleStaticMethods {
	
	public static HashMap<String, String> computeBmi(double height, double weight) {
		
		double bmi = weight/(height*height);
		String status = bmi < 18.5 ? "underweight" : (bmi <= 24.9 ? "normal" : (bmi <= 29.9 ? "overweight" : "obese"));
				
		HashMap<String, String> bmiInfo = new HashMap<String, String>();
		bmiInfo.put("bmi", Double.toString(bmi));
		bmiInfo.put("status", status);
		return  bmiInfo;
		
	}
	
	public static Connection connectToDb() throws SQLException {
		
		MysqlDataSource ds = new MysqlDataSource();
		
		ds.setServerName("45.56.85.191");
		ds.setPort(3306);
		ds.setUser("kimjongun");
		ds.setPassword("KimJongUnIsGreat");
		ds.setDatabaseName("HealthDB");
		
		return ds.getConnection();

	}
	
	public static ResultSet selectUser(Connection conn, int id) throws SQLException {
		
		PreparedStatement stmt = conn.prepareStatement("SELECT * FROM User WHERE UserId=?");
		stmt.setInt(1, id);
		
		ResultSet rs = stmt.executeQuery();
		return rs; 
		
	}
	
	private static ResultSet executeQuery(Connection conn, String query) throws Exception {
		Statement stmt = conn.createStatement();
		
		ResultSet rs;
		rs = stmt.executeQuery(query);

		return rs;
	}

}
