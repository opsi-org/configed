package de.uib.utilities;

public class StringvaluedObject
{

   protected String value;
   
   public StringvaluedObject (String val)
   {
     value = val;
   }
   
   public void setValue (String val)
   {
     value=val;
   }
    
   public String getValue()
   {
      //System.out.println ("--------------- delivering  " + value);
      return value;
   }
   
   public String toString()
   {
      return getValue();
   }
   
}
  

