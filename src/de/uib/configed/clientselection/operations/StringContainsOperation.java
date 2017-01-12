package de.uib.configed.clientselection.operations;

import de.uib.configed.clientselection.*;

public class StringContainsOperation extends SelectOperation
{   
    public StringContainsOperation( SelectElement element )
    {
        super(element);
    }
    
    @Override
    public SelectData.DataType getDataType()
    {
        return SelectData.DataType.TextType;
    }
    
    @Override
    public String getOperationString()
    {
        return "contains";
    }
}