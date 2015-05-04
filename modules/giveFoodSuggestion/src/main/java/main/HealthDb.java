package main;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class HealthDb
{
	private final String serverName;
	private final int port;
	private final String user;
	private final String password;
	private final String schemaName;
	
	private final MysqlDataSource ds;
	
	private final HashMap<String, HashMap<String, String>> TableSet;
	
	public HealthDb() throws SQLException {
		
		this.serverName = "45.56.85.191";
		this.port = 3306;
		this.user = "kimjongun";
		this.password = "KimJongUnIsGreat";
		this.schemaName = "HealthDB";
		
		ds = new MysqlDataSource();
		ds.setServerName(serverName);
		ds.setPort(port);
		ds.setUser(user);
		ds.setPassword(password);
		ds.setDatabaseName(schemaName);
		
		System.out.println("Establishing connection to database");
		Connection conn = ds.getConnection();	
		System.out.println("Connection Established");
		
		TableSet = new HashMap<String, HashMap<String, String>>();
		ResultSet rs = conn.createStatement().executeQuery("show table status;");
		ArrayList<String> tables = new ArrayList<String>();
		
		while (rs.next()) {
			tables.add(rs.getString(1));
		}
		
		HashMap<String, String> thisTable;
		for (String table: tables) {
			thisTable = new HashMap<String, String>();
			ResultSet fields = conn.createStatement().executeQuery("describe " + table);
			while(fields.next()) {
				thisTable.put(fields.getString(1), fields.getString(2));
			}
			TableSet.put(table, thisTable);
		}
		System.out.println("Database metadata obtained");
		System.out.println("Start-up successful");
				
	}
	
	public HealthDb(String serverName, int port, String user, String password, String schemaName) throws SQLException {
		
		this.serverName = serverName;
		this.port = port;
		this.user = user;
		this.password = password;
		this.schemaName = schemaName;
		
		ds = new MysqlDataSource();
		ds.setServerName(serverName);
		ds.setPort(port);
		ds.setUser(user);
		ds.setPassword(password);
		ds.setDatabaseName(schemaName);
		
		System.out.println("Establishing connection to database");
		Connection conn = ds.getConnection();
		System.out.println("Connection Established");
		
		TableSet = new HashMap<String, HashMap<String, String>>();
		ResultSet rs = conn.createStatement().executeQuery("show table status;");
		ArrayList<String> tables = new ArrayList<String>();
		
		while (rs.next()) {
			tables.add(rs.getString(1));
		}
		
		HashMap<String, String> thisTable;
		for (String table: tables) {
			thisTable = new HashMap<String, String>();
			ResultSet fields = conn.createStatement().executeQuery("describe " + table);
			while(fields.next()) {
				thisTable.put(fields.getString(1), fields.getString(2));
			}
			TableSet.put(table, thisTable);
		}
		System.out.println("Database metadata obtained");
		System.out.println("Start-up successful");
		
	}

	
	public ResultSet executeQuery(String tableName, String field, String value) {
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection conn = null;
		
		try {
			conn = ds.getConnection();
			
			String type = TableSet.get(tableName).get(field);
			String wildcardQuery = "SELECT * FROM " + tableName + " WHERE " + field + " LIKE ?";
			String equalQuery = "SELECT * FROM " + tableName + " WHERE " + field + " = ?";
						
			if(type.contains("double")) {
				stmt = conn.prepareStatement(equalQuery);
				stmt.setDouble(1, Double.parseDouble(value));
			} else if(type.contains("int")) {
				stmt = conn.prepareStatement(equalQuery);
				stmt.setInt(1, Integer.parseInt(value));
			} else if(type.contains("varchar") || type.contains("enum")) {
				stmt = conn.prepareStatement(wildcardQuery);
				stmt.setString(1, "%"+ value +"%");
			}
				
			rs = stmt.executeQuery();
			rs.next();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return rs;
		
	}
	
	public void executeInsert(String tableName, HashMap<String, String> fieldValuePair) {
		
		HashMap<String, String> thisTable = this.TableSet.get(tableName.trim());
		
		PreparedStatement stmt = null;
		Connection conn = null;
		String sql = "INSERT INTO " + tableName + " (";
		
		String[] ks = fieldValuePair.keySet().toArray(new String[fieldValuePair.size()]);
		for (int i=0; i<fieldValuePair.size()-1; i++) {
			sql += ks[i];
			sql += ", ";
		}
		sql+=ks[fieldValuePair.size()-1];
		sql+=") VALUES(";
		
		for (int i=0; i<fieldValuePair.size()-1; i++) {
			sql += "?, ";
		}
		sql+="?);";
		
		try {
			conn = ds.getConnection();
			stmt = conn.prepareStatement(sql);
			
			int paraIndex = 1;
			for (String field: fieldValuePair.keySet()) {
				String type = thisTable.get(field);
				
				if(type.contains("double")) {
					stmt.setDouble(paraIndex, Double.parseDouble(fieldValuePair.get(field)));
				} else if(type.contains("int")) {
					stmt.setInt(paraIndex, Integer.parseInt(fieldValuePair.get(field)));
				} else if(type.contains("varchar") || type.contains("enum")) {
					stmt.setString(paraIndex, fieldValuePair.get(field));
				} else if(type.contains("date")) {
					stmt.setDate(paraIndex, Date.valueOf(fieldValuePair.get(field)));
				}
			
				paraIndex++;
			}
				stmt.execute();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	public void executeDelete(String tableName, HashMap<String, String> fieldValuePair) {
		
		PreparedStatement stmt = null;
		Connection conn = null;
		String sql = "DELETE FROM " + tableName + " WHERE ";

		String[] fields = fieldValuePair.keySet().toArray(new String[fieldValuePair.size()]);
		
		for (int i=0; i<fieldValuePair.size()-1; i++) {
			sql = sql + fields[i] + "=? AND ";
		}
		
		sql = sql + fields[fields.length-1] + "=?;";
		
		System.out.println(sql);
		
		try {
			conn = ds.getConnection();
			stmt = conn.prepareStatement(sql);
			
			int paraIndex = 1;
			for (String field: fieldValuePair.keySet()) {
				String type = this.TableSet.get(tableName.trim()).get(field);

				if(type.contains("double")) {
					stmt.setDouble(paraIndex, Double.parseDouble(fieldValuePair.get(field)));
				} else if(type.contains("int")) {
					stmt.setInt(paraIndex, Integer.parseInt(fieldValuePair.get(field)));
				} else if(type.contains("varchar") || type.contains("enum")) {
					stmt.setString(paraIndex, fieldValuePair.get(field));
				}
				
				paraIndex++;
			}
		
			stmt.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public HashMap<String, HashMap<String, String>> getTableSet() {
		return (HashMap<String, HashMap<String, String>>)TableSet.clone();
	}
	
}
