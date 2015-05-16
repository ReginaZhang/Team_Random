package main;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
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
	
	public HealthDb(String serverName, int port, String user, String password, String schemaName) throws SQLException {
		
		this.serverName = serverName;
		this.port = port;
		this.user = user;
		this.password = password;
		this.schemaName = schemaName;
		
		ds = new MysqlDataSource();
		ds.setServerName(this.serverName);
		ds.setPort(this.port);
		ds.setUser(this.user);
		ds.setPassword(this.password);
		ds.setDatabaseName(this.schemaName);
		
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
	
	public HealthDb() throws SQLException {
		
		this("45.56.85.191", 3306, "kimjongun", "KimJongUnIsGreat", "HealthDB");
				
	}
	
	public ResultSet executeQuery(String tableName, String field, String value) throws SQLException {
		
	PreparedStatement stmt = null;
	ResultSet rs = null;
	Connection conn = null;
	
	
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
		
	return rs;
		
	}

	
	public ResultSet executeQuery(String tableName, HashMap<String, String> fieldValuePair) {
		
		HashMap<String, String> thisTable = this.TableSet.get(tableName.trim());
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection conn = null;
		
		try {
			conn = ds.getConnection();
			
			String sql = "SELECT * FROM " + tableName + " WHERE ";
			String[] ks = fieldValuePair.keySet().toArray(new String[fieldValuePair.size()]);
			
			for (int i=0; i<fieldValuePair.size(); i++) {
				
				sql += ks[i];
				String type = thisTable.get(ks[i]);
				if (fieldValuePair.get(ks[i]) == null) {
					sql += " is null";
				} else if (type == "varchar") {
					sql += " like %?%";
				} else {
					sql += " = ?";
				}
				
				if (i!=fieldValuePair.size()-1) {
					sql+=" and ";
				} else {
					sql+=";";
				}
				
			}
			
			stmt = conn.prepareStatement(sql);
			
			int index = 1;
			for (String field: ks) {
				String type = thisTable.get(field);
				
				if (fieldValuePair.get(field) != null) {
						
					if(type.contains("double")) {					
						stmt.setDouble(index, Double.parseDouble(fieldValuePair.get(field)));
					} else if(type.contains("int")) {
						stmt.setInt(index, Integer.parseInt(fieldValuePair.get(field)));
					} else if(type.contains("varchar") || type.contains("enum")) {
						stmt.setString(index, fieldValuePair.get(field));
					}
					
				}
				
				index++;
				
			}
				
			rs = stmt.executeQuery();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return rs;
		
	}
	
	public void executeUpdate(String tableName, HashMap<String, String> conditionPair, HashMap<String, String> newValuePair)
			throws SQLException {
		
		HashMap<String, String> thisTable = this.TableSet.get(tableName.trim());
		
		PreparedStatement stmt = null;
		Connection conn = null;
		String sql = "UPDATE " + tableName + " SET ";
		
		String[] nks = newValuePair.keySet().toArray(new String[newValuePair.size()]);
		String[] cks = conditionPair.keySet().toArray(new String[conditionPair.size()]);
		
		for (int i=0; i<newValuePair.size(); i++) {
			sql += nks[i] + " = ";
			
			if (newValuePair.get(nks[i]) == null) {
				sql += "null";
			} else if (newValuePair.get(nks[i]).equals("now()") && thisTable.get(nks[i]).contains("date")) {
				sql += "now()";
			} else {
				sql += "?";
			}
			
			if (i!=newValuePair.size() - 1) {
				sql += ", ";
			} else {
				sql+=" WHERE ";
			}
			
		}
		
		for (int i=0; i<conditionPair.size(); i++) {
			sql += cks[i];
			
			if (conditionPair.get(cks[i]) == null) {
				sql += " is null";
			} else {
				sql += " = ?";
			}
			
			if (i!=conditionPair.size() - 1) {
				sql += " AND ";
			} else {
				sql+=";";
			}
			
		}
		
		System.out.println(sql);
		
		conn = ds.getConnection();
		stmt = conn.prepareStatement(sql);
		
		int paraIndex = 1;
		for (String field: nks) {
			
			if (newValuePair.get(field) == null || (newValuePair.get(field).equals("now()") && thisTable.get(field).contains("date"))) {
				
			} else {
				String type = thisTable.get(field);
				
				if(type.contains("double")) {
					stmt.setDouble(paraIndex, Double.parseDouble(newValuePair.get(field)));
				} else if(type.contains("int")) {
					stmt.setInt(paraIndex, Integer.parseInt(newValuePair.get(field)));
				} else if(type.contains("varchar") || type.contains("enum")) {
					stmt.setString(paraIndex, newValuePair.get(field));
				} else if(type.contains("date")) {
					stmt.setDate(paraIndex, Date.valueOf(newValuePair.get(field)));
				}
			
				paraIndex++;
			}
			
		}
		
		for (String field: cks) {
			
			if (conditionPair.get(field) == null) {
				
			} else {
				String type = thisTable.get(field);
				
				if(type.contains("double")) {
					stmt.setDouble(paraIndex, Double.parseDouble(conditionPair.get(field)));
				} else if(type.contains("int")) {
					stmt.setInt(paraIndex, Integer.parseInt(conditionPair.get(field)));
				} else if(type.contains("varchar") || type.contains("enum")) {
					stmt.setString(paraIndex, conditionPair.get(field));
				} else if(type.contains("date")) {
					stmt.setDate(paraIndex, Date.valueOf(conditionPair.get(field)));
				}
			
				paraIndex++;
			}
			
		}
		
		stmt.execute();
		
	}

	public int executeInsert(String tableName, HashMap<String, String> fieldValuePair) {
		
		HashMap<String, String> thisTable = this.TableSet.get(tableName.trim());
		
		PreparedStatement stmt = null;
		Connection conn = null;
		String sql = "INSERT INTO " + tableName + " (";
		
		String[] ks = fieldValuePair.keySet().toArray(new String[fieldValuePair.size()]);
		for (int i=0; i<fieldValuePair.size(); i++) {
			sql += ks[i];
			
			if (i!=fieldValuePair.size() - 1) {
				sql += ", ";
			} else {
				sql+=") VALUES(";
			}
		}
		
		for (int i=0; i<fieldValuePair.size()-1; i++) {
			sql += "?, ";
		}
		sql+="?);";
		
		System.out.println(sql);
		
		try {
			conn = ds.getConnection();
			stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			
			int paraIndex = 1;
			for (String field: ks) {
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
		
		return getGeneratedId(stmt);
		
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
			for (String field: fields) {
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
	
	public String getServerName() {
		return serverName;
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<String, HashMap<String, String>> getTableSet() {
		return (HashMap<String, HashMap<String, String>>)TableSet.clone();
	}
	
	public int getGeneratedId(Statement stmt) {
		
		int id = -1;
		
		try {
			ResultSet rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				id = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return id;
		
	}
	
	
	
	
}
