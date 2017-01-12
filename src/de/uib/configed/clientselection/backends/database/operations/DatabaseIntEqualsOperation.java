package de.uib.configed.clientselection.backends.database.operations;

import de.uib.configed.clientselection.operations.*;
import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.backends.database.*;

public class DatabaseIntEqualsOperation extends IntEqualsOperation
{
    private int compareInteger;
    private String elementName;
    private String table;
    
    public DatabaseIntEqualsOperation( String elementName, int compareInteger, String table )
    {
        this.compareInteger = compareInteger;
        this.elementName = elementName;
        this.table = table;
    }
    
    public boolean doesMatch( Client client )
    {
        DatabaseClient dClient = (DatabaseClient) client;
        if( dClient.isOnOtherTable() )
        {
            dClient.setSql( table+"."+elementName+" = "+String.valueOf(compareInteger) );
            dClient.setTable( table );
            return false;
        }
        
        String sql = "SELECT hostId FROM HOST WHERE HOST.hostId = '" + dClient.getId() + "' AND HOST."+elementName+" = "+String.valueOf(compareInteger)+";";
        return DbConnect2.checkForExistence(sql);
    }
}