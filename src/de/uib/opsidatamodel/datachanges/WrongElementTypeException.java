package de.uib.opsidatamodel.datachanges;

public class WrongElementTypeException extends Exception
{
    public WrongElementTypeException()
    {
      super(getClass().getName());  
    }

    public WrongElementTypeException(String message)
    {
       super(getClass().getName() + ": " + message);  
    }
}
    
