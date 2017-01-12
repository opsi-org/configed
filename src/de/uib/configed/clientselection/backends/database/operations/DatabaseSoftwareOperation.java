package de.uib.configed.clientselection.backends.database.operations;

import de.uib.configed.clientselection.operations.*;
import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.backends.database.*;

public class DatabaseSoftwareOperation extends SoftwareOperation
{
    public DatabaseSoftwareOperation( SelectOperation operation )
    {
        super( operation );
    }
    
    public boolean doesMatch( Client client )
    {
        DatabaseClient dClient = (DatabaseClient) client;
        dClient.setOnOtherTable(true);
        getChildOperations().get(0).doesMatch( client );
        String sql = "SELECT hostId FROM HOST, PRODUCT_ON_CLIENT, PRODUCT WHERE HOST.hostId = '" + dClient.getId() + "' AND HOST.hostId = PRODUCT_ON_CLIENT.clientId "
                        + "AND PRODUCT_ON_CLIENT.productId = PRODUCT.productId AND ";
        sql += dClient.getSql();
        dClient.setSql("");
        sql += ";";
        dClient.setOnOtherTable(false);
        
        return DbConnect2.checkForExistence( sql );
    }
}