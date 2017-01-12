package de.uib.configed.clientselection.operations;

import de.uib.configed.clientselection.*;

public class DateGreaterThanOperation extends SelectOperation
{   
    public DateGreaterThanOperation( SelectElement element )
    {
        super(element);
    }
    
    @Override
    public SelectData.DataType getDataType()
    {
        return SelectData.DataType.DateType;
    }
    
    @Override
    public String getOperationString()
    {
        return ">";
    }
}