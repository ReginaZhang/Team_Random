public class HealthDb
{
	private final String serverName = "45.56.85.191";
	private final int port = 3306;
	private final String user = "kimjongun";
	private final String password = "KimJongUnIsGreat";
	private final String schemaName = "HealthDB";
	
	private final MysqlDataSource ds;
	private final Connection conn;
	
	public HealthDb() {
		
		ds = new MysqlDataSource();
		ds.setServerName(serverName);
		ds.setPort(port);
		ds.setUser(user);
		ds.setPassword(password);
		ds.setDatabaseName(schemaName);
		
		conn = ds.getConnection();
		
	}
	
	public HealthDb(String serverName, int port, String user, String passwrod, String schemaName) {
		
		this.serverName = serverName;
		this.port = port;
		this.user = user;
		this.password = password;
		this.schemaName = schemeName;
		
		this();
		
	}
	
	public Connection getConnection() {
		return this.conn;
	}
