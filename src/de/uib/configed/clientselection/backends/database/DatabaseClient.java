package de.uib.configed.clientselection.backends.database;

import java.util.*;
import de.uib.configed.clientselection.*;
import de.uib.utilities.logging.logging;

public class DatabaseClient implements Client
{
    private String hostId;
    private List<String> hostdb;
    private String sql;
    private String table="";
    private boolean onOtherTable;
    
    public DatabaseClient( String host )
    {
        hostId = host;
        onOtherTable = false;
    }
    
    public String getId()
    {
        return hostId;
    }
    
    public void setHostDb( List<String> db )
    {
        hostdb = db;
    }
    
    public String getHostDbAt( int index )
    {
        return hostdb.get(index);
    }
    
    public void setSql( String newSql )
    {
        sql = newSql;
    }
    
    public String getSql()
    {
        return sql;
    }
    
    public void setOnOtherTable( boolean otherTable )
    {
        logging.debug( this, "Set on other table: " + otherTable );
        onOtherTable = otherTable;
    }
    
    public boolean isOnOtherTable()
    {
        return onOtherTable;
    }
    
    public void setTable( String table )
    {
        this.table = table;
    }
    
    public String getTable()
    {
        return table;
    }
}