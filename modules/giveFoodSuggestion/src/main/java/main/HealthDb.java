public class HealthDb
{
	private String serverName;
	private int port;
	private String user;
	private String password;
	private String databaseName;
	
	private MysqlDataSource
	
	
	MysqlDataSource ds = new MysqlDataSource();
		
		ds.setServerName("45.56.85.191");
		ds.setPort(3306);
		ds.setUser("kimjongun");
		ds.setPassword("KimJongUnIsGreat");
		ds.setDatabaseName("HealthDB");
