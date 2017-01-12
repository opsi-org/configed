package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.operations.*;
import de.uib.configed.clientselection.backends.opsidatamodel.*;
import de.uib.utilities.logging.logging;
import java.util.*;

public class OpsiSoftwareEqualsOperation extends OpsiDataStringEqualsOperation implements ExecutableOperation
{
    
    
    public OpsiSoftwareEqualsOperation( String key, String data, SelectElement element )
    {
        super(OpsiDataClient.SOFTWARE_MAP,key, data, element);
    }
    
    @Override
    protected boolean checkData(final String realData )
    {
    	//logging.debug(this, "checkData data:" + realData);
    		
    	String rData = realData.toLowerCase();
    	
    	if( dataSplitted == null ) //simple case: no '*'
        {
        	//logging.debug(this, "checkData, comparing the real data " + rData + " to the requested data " + data);
        	return rData.equals(data) ;
        }
        else if( dataSplitted.length == 0 ) //the only chars are  '*'
        {
        	//logging.debug(this, "checkData,  dataSplitted.length == 0" );
            if( rData.length() > 0 )
                return true;
            else
                return false;
        }
        else
        {
        	//logging.debug(this, "checkData comparing to dataSplitted " + Arrays.toString(dataSplitted));

            if( !startsWith )
                if( !rData.startsWith(dataSplitted[0]) )
                    return false;
            int index=0;
            int i=0;
            while( i<dataSplitted.length && index>=0 )
            {
                //logging.debug( this,  "checkData " + dataSplitted[i] );
                if( !dataSplitted[i].isEmpty() )
                {
                    index=rData.indexOf( dataSplitted[i], index );
                    if( index >= 0 )
                        index+=dataSplitted[i].length();
                }
                //logging.debug( this,  "checkData " + String.valueOf(index) );
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
  
