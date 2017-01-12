package de.uib.configed.guidata;

import de.uib.utilities.StringvaluedObject;
import de.uib.utilities.logging.*;
import java.awt.Color;
import de.uib.configed.Globals;

public class ValueMerger extends StringvaluedObject
{
   boolean onlyPartiallyExisting;
   boolean havingCommonValue;
   
   public static Color noCommonValueTextcolor = Globals.backgroundGrey;
   public static Color noCommonValueBackcolor = Globals.backgroundGrey;
   public static Color noCommonKeyTextcolor = Globals.backBlue;
   public static Color noCommonKeyBackcolor = Globals.backBlue;
   
   public final static String NO_COMMON_KEY = "NO COMMON KEY";
   public final static String NO_COMMON_VALUE = "NO COMMON VALUE";
   
   // building the merger:
   
   
   public ValueMerger(String value)
   {
     super(value);
     //System.out.println ("=========== creating ValueMerger with value " + value);
     
     this.onlyPartiallyExisting =false;
     this.havingCommonValue = true;
   }
   
   public void setOnlyPartiallyExisting ()
   {
        onlyPartiallyExisting = true;
   }
   
   public void setHavingNoCommonValue ()
   {
        havingCommonValue = false;
   }
   
   
   
   public void merge (String val) 
   {
      //System.out.println ("------------ merging " + val + " to value " + value);
      if (!value.equals(val))
      {
         havingCommonValue = false;
         value = NO_COMMON_VALUE;
      }
   }
   
   // change the merger
   
   public void setValue (String val)
   {
       if (! onlyPartiallyExisting)
       {  
         value = val;
         havingCommonValue = true;
       }
   }
   
   // evaluating the merger:
   
   public boolean isOnlyPartiallyExisting()
   {
      return onlyPartiallyExisting;
   }
   
   public boolean hasCommonValue()
   {
      return havingCommonValue;
   }
   
   public String getValue()
   {
       if (onlyPartiallyExisting)
         return NO_COMMON_KEY;
      else if (!havingCommonValue)
        return NO_COMMON_VALUE;
      else
        return value;
   }
   
   public Color getTextColor ()
   {
      if (onlyPartiallyExisting)
     {
        return noCommonKeyTextcolor;
     }  
     else if (!havingCommonValue)
     {
       return noCommonValueTextcolor;
     }
     else
       return Color.BLACK;
   }
   
   public Color getBackgroundColor ()
   {
      if (onlyPartiallyExisting)
     {
        return noCommonKeyBackcolor;
     }  
     else if (!havingCommonValue)
     {
       return noCommonValueBackcolor;
     }
     else
       return Color.BLACK;
   }
   
   
}
