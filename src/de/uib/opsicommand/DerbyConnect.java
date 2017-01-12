package de.uib.opsicommand;

import java.sql.*;
import java.io.File;

import de.uib.opsidatamodel.productstate.ProductState;
import de.uib.utilities.logging.logging;

public class DerbyConnect
{
    
	final String framework = "derbyclient";
	//final String driver = "org.apache.derby.jdbc.ClientDriver";
	final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	//public final String serverDefault = "localhost:1527";
	
	
	//protected String url = "jdbc:derby:derbyDB;create=true";
	
	final String derbyURL = "jdbc:derby:";
	
	
	
	private static Connection con;
    
	
	private DerbyConnect(String databasePath, String server, boolean renew)
	{
		logging.info("creating DerbyConnect driver " + driver  + ", server " + server + ", renew" + renew );
			
		int i = server.indexOf(":"); 
		
		if ( i == - 1)
			server = server + "_" + JSONthroughHTTP.defaultPort;
		
		else
			server = server.substring(0, i) + "_" + server.substring(i+1);
		
		String url = derbyURL + databasePath + File.separator + server  + File.separator + "configed" + ";create=true";
		
		logging.info("url " + url);
		try {
			Class.forName( driver ).newInstance();
			//new Driver();
		}
		//         catch ( ClassNotFoundException e ) {
		//             e.printStackTrace();
		//             System.exit(1);
		//         }
		catch( Exception e ) {
			e.printStackTrace();
			return;
		}
		
		try {
			con = DriverManager.getConnection(url);
			con.setAutoCommit(true);
		}
		catch( SQLException e ) {
			logging.error( this, e.getMessage() );
			//e.printStackTrace();
		}
		
		TableProductOnClient tableProductOnClient = new TableProductOnClient();
		tableProductOnClient.renew(renew); 
		tableProductOnClient.create();
		
		//System.exit(0);
	}
	
		
	public static Connection getConnection(String databasePath, String server, boolean resync)
	{
		if( con == null )
			new DerbyConnect(databasePath, server, resync);
		return con;
	}
	
	
	public static Connection getConnection()
	{
		return con;
	}
	
	
	
	public static void closeConnection()
	{
		try {
			con.close();
		}
		catch( SQLException e ) {
			logging.error( "DerbyConnect: " + e.getMessage() );
			e.printStackTrace();
		}
		con = null;
	}
	
	public static boolean checkForExistence( String sql )
	{
		logging.debug( "DerbyConnect: "+ sql );
		try {
			ResultSet reply = getConnection().createStatement().executeQuery(sql);
			if( reply.next() )
				return true;
		}
		catch( Exception e ){
			logging.error( "DerbyConnect: " + e.getMessage() );
			e.printStackTrace();
		}
		return false;
	}
		
}