package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.operations.*;
import de.uib.configed.clientselection.backends.opsidatamodel.*;
import de.uib.utilities.logging.logging;
import java.util.*;

public class OpsiDataStringEqualsOperation extends StringEqualsOperation implements ExecutableOperation
{
    protected String map;
    protected String key;
    protected String data;
    protected String[] dataSplitted=null;
    protected boolean startsWith;
    protected boolean endsWith;
    
    
    public OpsiDataStringEqualsOperation( String map, String key, String data, SelectElement element )
    {
        super(element);
    		logging.debug(this, "OpsiDataStringEqualsOperation maptype, key, data: " + map + ", " + key + ", " + data );
        this.map = map;
        this.key = key;
        this.data = data.toLowerCase();
        if( data.contains("*") )
        {
            dataSplitted = (this.data).split("\\*");
            logging.debug( this, "OpsiDataStringEqualsOperation " + String.valueOf(dataSplitted.length) );
        }
        startsWith = data.startsWith("*");
        endsWith = data.endsWith("*");
            
    }
    
    public boolean doesMatch( Client client )
    {
        OpsiDataClient oClient = (OpsiDataClient) client;
        //logging.debug(this, "doesMatch client " + oClient);
        //logging.debug(this, "doesMatch interesting map, key  "   + map + ", " + key);
        Map realMap = oClient.getMap( map );
        //logging.debug( this, "doesMatch " + realMap.toString() );
        if( !realMap.containsKey(key) || realMap.get(key) == null )
        {
            //logging.debug(this, "key '" + key + "' not found!");
            return false;
        }
        
        String realData = realMap.get(key).toString().toLowerCase();
        //logging.debug( this, "doesMatch realData " + realData);
        return checkData( realData );
    }
    
    
    
    protected boolean checkData(final String realData )
    {
    	 //logging.debug(this, "checkData " + realData + " " +data);
    		
    	String rData = realData.toLowerCase();
    	
        if( dataSplitted == null )
        {
            //logging.debug(this, "checkData, comparing to data " + data);
            if( rData.equals(data) )
                return true;
            return false;
        }
        else if( dataSplitted.length == 0 )
        {
            //logging.debug(this, "checkData,  dataSplitted.length == 0" );
            if( realData.length() > 0 )
                return true;
            else
                return false;
        }
        else
        {
            if( !startsWith )
                if( !rData.startsWith(dataSplitted[0]) )
                    return false;
            int index=0;
            int i=0;
            while( i<dataSplitted.length && index>=0 )
            {
                //logging.debug( this, dataSplitted[i] );
                if( !dataSplitted[i].isEmpty() )
                {
                    index=rData.indexOf( dataSplitted[i], index );
                    if( index >= 0 )
                        index+=dataSplitted[i].length();
                }
                //logging.debug( this, String.valueOf(index) );
                i++;
            }
            if( index<0 )
                return false;
            if( !endsWith )
                if( !rData.endsWith( dataSplitted[dataSplitted.length-1] ) )
                    return false;
            return true;
        }
    }
}
