package de.uib.configed.clientselection.backends.database.operations;

import java.util.*;
import de.uib.configed.clientselection.operations.*;
import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.backends.database.*;

public class DatabaseGroupOperation extends SelectGroupOperation
{
    public DatabaseGroupOperation( SelectOperation operation )
    {
        registerChildOperation( operation );
    }
    
    public boolean doesMatch( Client client )
    {
        DatabaseClient dClient = (DatabaseClient) client;
        dClient.setOnOtherTable(true);
        String sql="SELECT hostId FROM HOST, OBJECT_TO_GROUP where HOST.hostid = '"+client.getId()+"' and HOST.hostId = OBJECT_TO_GROUP.objectId AND ";
        getChildOperations().get(0).doesMatch(client);
        sql += dClient.getSql();
        sql += ";";
        dClient.setOnOtherTable(false);
        return DbConnect2.checkForExistence( sql );
    }
}