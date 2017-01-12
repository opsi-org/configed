package de.uib.configed.clientselection.backends.database.operations;

import java.util.*;
import de.uib.configed.clientselection.operations.*;
import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.backends.database.*;

public class DatabaseNotOperation extends NotOperation
{
    public DatabaseNotOperation( SelectOperation operation )
    {
        super( operation );
    }
    
    public boolean doesMatch( Client client )
    {
        DatabaseClient dClient = (DatabaseClient) client;
        if( !dClient.isOnOtherTable() )
        {
            return super.doesMatch(client);
        }
        
        String sql="NOT (";
        getChildOperations().get(0).doesMatch(client);
        sql += dClient.getSql();
        sql += ")";
        dClient.setSql( sql );
        return false;
    }
}