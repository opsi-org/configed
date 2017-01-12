package de.uib.configed.clientselection.backends.database.operations;

import de.uib.configed.clientselection.operations.*;
import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.backends.database.*;

public class DatabaseHardwareOperation extends HardwareOperation
{
    private String table;

    public DatabaseHardwareOperation( SelectOperation operation )
    {
        super( operation );
        this.table = table;
    }
    
    public boolean doesMatch( Client client )
    {
        DatabaseClient dClient = (DatabaseClient) client;
        dClient.setOnOtherTable(true);
        getChildOperations().get(0).doesMatch( client );
        String deviceTable = dClient.getTable();
        String configTable = deviceTable.replace("HARDWARE_DEVICE", "HARDWARE_CONFIG");
        String sql = "SELECT HOST.hostId FROM HOST, "+configTable+", "+deviceTable+" WHERE HOST.hostId = '" + dClient.getId() 
                    + "' AND HOST.hostId = "+configTable+".hostId AND "+configTable+".hardware_id = "+deviceTable+".hardware_id AND ";
        sql += dClient.getSql();
        dClient.setSql("");
        sql += ";";
        dClient.setOnOtherTable(false);
        
        return DbConnect2.checkForExistence( sql );
    }
}