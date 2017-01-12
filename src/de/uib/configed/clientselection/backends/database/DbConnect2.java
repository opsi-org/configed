package de.uib.configed.clientselection.backends.database;

import java.sql.*;
import de.uib.utilities.logging.logging;

public class DbConnect2
{
    private final String driver = "com.mysql.jdbc.Driver";
    private final String url = "jdbc:mysql://localhost/opsi";
    private final String user = "opsi";
    private final String password = "opsi";
    private static Connection con = null;
    
    private DbConnect2()
    {
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
            con = DriverManager.getConnection(url, user, password);
            con.setAutoCommit(true);
        }
        catch( SQLException e ) {
            logging.error( this, e.getMessage(), e );
            con = null;
        }
    }
    
    public static Connection getConnection()
    {
        if( con == null )
            new DbConnect2();
        return con;
    }
    
    public static void closeConnection()
    {
        try {
            con.close();
        }
        catch( SQLException e ) {
            logging.error( "DbConnect2: " + e.getMessage(), e );
        }
        con = null;
    }
    
    public static boolean checkForExistence( String sql )
    {
        logging.debug( "DbConnect2: "+ sql );
        try {
            ResultSet reply = getConnection().createStatement().executeQuery(sql);
            if( reply.next() )
                return true;
        }
        catch( Exception e ){
            logging.error( "DbConnect2: " + e.getMessage(), e );
        }
        return false;
    }
}