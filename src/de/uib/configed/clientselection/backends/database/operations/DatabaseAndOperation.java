package de.uib.configed.clientselection.backends.database.operations;

import java.util.*;
import de.uib.configed.clientselection.operations.*;
import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.backends.database.*;

public class DatabaseAndOperation extends AndOperation
{
    public DatabaseAndOperation( List<SelectOperation> operations )
    {
        super( operations );
    }
    
    public boolean doesMatch( Client client )
    {
        DatabaseClient dClient = (DatabaseClient) client;
        if( !dClient.isOnOtherTable() )
        {
            return super.doesMatch(client);
        }
        
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("(");
        Iterator<SelectOperation> iterator = getChildOperations().iterator();
        while( iterator.hasNext() )
        {
            iterator.next().doesMatch( client );
            sqlBuilder.append( dClient.getSql() );
            if( iterator.hasNext() )
                sqlBuilder.append( " AND " );
        }
        sqlBuilder.append(")");
        dClient.setSql( sqlBuilder.toString() );
        return false;
    }
}