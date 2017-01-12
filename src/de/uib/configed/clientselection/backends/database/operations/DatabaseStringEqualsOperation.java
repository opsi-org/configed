package de.uib.configed.clientselection.backends.database.operations;

import de.uib.configed.clientselection.operations.*;
import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.backends.database.*;

public class DatabaseStringEqualsOperation extends StringEqualsOperation
{
    private String compareString;
    private String elementName;
    private String table;
    private String equalOperator;
    
    public DatabaseStringEqualsOperation( String elementName, String compareString, String table )
    {
        this.compareString = compareString;
        this.elementName = elementName;
        this.table = table;
        if( compareString.contains("*") )
        {
            this.equalOperator="like";
            this.compareString = compareString.replaceAll( "\\*", "%" );
        }
        else
        {
            this.equalOperator="=";
        }
    }
    
    public boolean doesMatch( Client client )
    {
        DatabaseClient dClient = (DatabaseClient) client;
        if( dClient.isOnOtherTable() )
        {
            dClient.setSql( table+"."+elementName+" "+equalOperator+" '"+compareString+"'" );
            dClient.setTable( table );
            return false;
        }
        
        String sql = "SELECT hostId FROM HOST WHERE HOST.hostId = '" + dClient.getId() + "' AND HOST."+elementName+" "+equalOperator+" '"+compareString+"';";
        return DbConnect2.checkForExistence(sql);
    }
}