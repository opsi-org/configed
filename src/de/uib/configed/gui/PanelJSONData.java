package de.uib.configed.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import de.uib.utilities.*;
import org.json.*;

public class PanelJSONData extends JPanel
{
       JSONObject jO;
       JEditorPane editPane;
       JScrollPane jScrollPane; 

      public PanelJSONData ()
      {
          buildPanel();
      }
      
    protected void buildPanel()
    {
        setLayout(new BorderLayout());
        editPane = new JEditorPane();
        editPane.setContentType ("text/html");
        editPane.setEditable(false);
        editPane.setPreferredSize(new Dimension (600, 400));
        
        jScrollPane = new JScrollPane(editPane);
        jScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        add (  jScrollPane, BorderLayout.CENTER );
        
    }
    
   public void insertHTMLTable (String s)
   {
     insertHTMLTable (s, "");
   }
    
   public void insertHTMLTable (String s, String title)
   {
      final String baseRG = "f8f0f0";
      final String header =   "<HTML>\n <head>\n  <title>" + title + "</title>\n  </head>\n"
       + "<body style=\"background-color: " +  baseRG  + hexNoForColorizing (1) + "\">\n";
       
      final String footer =  "\n</body>\n</html>";
       
     
     editPane.setText ( header + s + footer);
     editPane.setCaretPosition(0);
   }
   
   public void setText (String s, String title)
   {   
       String myTitle = "";
       //String[] lines = (String[]) filterJSONString (giveTestJSONString()).toArray(new String[]{});
       if (title != null)
         myTitle = title;
      
       if (s != null)
       {    
         String[] lines = (String[]) filterJSONString (s).toArray(new String[]{});         
         editPane.setText (buildHTMLRepresentation(    lines ,  title) );
       }
       else
         editPane.setText("");
        
   }

   private static String hexNoForColorizing (int indentationCount)
   {
       switch (indentationCount)
       {
          case 0 : return "aa";
          case 1:  return "cc";
          case 2: return "ff";
          default: return "ff";
       }
   }
   
   private static String giveTestJSONString()
   {
          StringBuffer buff = new StringBuffer ();
         buff.append ("{"); 
         buff.append ("\"cpuModelName\": \"AMD-K6(tm) 3D processor\",");
         buff.append ("\"cpuSpeed\": \"267.272 MHz\",");
         buff.append ("\"ramSize\": \"127216 kB\",");
         buff.append ("\"hddPartition\":"); 
                    buff.append ("["); 
                    buff.append ("\"/dev/hda1   *      0+    254     255-  2048256    6  FAT16\",");
                    buff.append ("\"/dev/hda2        255     636     382   3068415    6  FAT16\",");
                    buff.append ("\"/dev/hda3          0       -       0         0    0  Empty\",");
                    buff.append ("\"/dev/hda4          0       -       0         0    0  Empty\"");
                    buff.append ("],");
         buff.append ("\"pciDevice\":"); 
                    buff.append ("[ ");
                    buff.append ("\"00:00.0 Host bridge: Silicon Integrated Systems [SiS] 5591/5592 Host (rev 02)\",");
                    buff.append ("\"00:00.1 IDE interface: Silicon Integrated Systems [SiS] 5513 [IDE] (rev d0) (prog-if 8a [Master SecP PriP])\",");
                    buff.append ("\"00:01.0 ISA bridge: Silicon Integrated Systems [SiS] 85C503/5513 (rev 01)\",");
                    buff.append ("\"00:02.0 PCI bridge: Silicon Integrated Systems [SiS] 5591/5592 AGP (prog-if 00 [Normal decode])\"");
                    buff.append ("]");
         buff.append ("}");

        return buff.toString();
   }
    
    
    public static ArrayList  filterJSONString (String jS)
    {
        ArrayList al = new ArrayList();
  
        try
        {
          JSONObject jO = new JSONObject ( jS );
          String[] lines = jO.toString(5).split ("\n");
        
           {
            if (lines != null && lines.length>1)
            {  
                for (int i = 0;  i< lines.length; i++)
                {
                   //filtering null lines, lines with only {}]
                   if (lines[i] != null)
                   {
                       String line = lines[i].trim();
                       //System.out.println (">>" + line);
                       if (!line.equals("{") &&  !line.equals("}")
                       && !line.equals("],"))
                      {
                          if (  lines[i].substring(lines[i].length()-3).equals (": [")  )
                              al.add (lines[i].substring(0, lines[i].length()-2));
                         else
                             al.add (lines[i]);
                     }
                   }  
                }
            }
          }  
        }
        catch (JSONException jex)
        { 
        }
        
        return al;
   }      
        

    
   public static String buildHTMLRepresentation ( String[] lines , String title)
   {
         final String baseRG = "eeee"; 
        
          StringBuffer buf = new StringBuffer (
            "<HTML>\n"
       + "<head>\n"
       + "<title>" + title + "</title>\n"
       +"</head>\n"
       + "<body style=\"background-color: " +  baseRG  + hexNoForColorizing (1) + "\">\n");
       
       int indentFound = 0;
       int indentSet = 0;
       int indentationCount = 0;
       final int  indentationPlus = 30;
       int indentationPx = 0;
       
       // with tables since style-positioning does not work
       buf.append ("<table border=\"0\">\n"); // table formatting
       
       
       if (lines != null)
       {    
           for (int i = 0; i<lines.length; i++)
           {
              // System.out.println ("line " + i  + lines[i]);
              String realLine = lines[i].trim();
              if (!realLine.equals(""))
                  {
                  indentFound = lines[i].indexOf ( realLine.charAt(0) );
                  if (indentFound > indentSet)
                  {
                      buf.append ("<tr>"); //table formatting
                      buf.append("<td  width=\"" + indentationPx +"\">&nbsp;</td><td>");  //table formatting
                      
                      // new table as element
                      
                      indentationCount++;
                      indentationPx = indentationPx + indentationPlus;
                      //buf.append ("<div style=\"position:absolute; left: " + indentationPx+ "px\">");  buf.append("\n");
                      buf.append("<div style=\"background-color: " +  baseRG + hexNoForColorizing (indentationCount) + "\">");
                      
                      buf.append ("\n<table border=\"0\">\n"); // table formatting
                      
                      buf.append ("<tr style=\"background-color: " +  baseRG + hexNoForColorizing (indentationCount) + "\">"); //table formatting
                      buf.append("<td width=\"" + indentationPx +"\">&nbsp;</td><td nowrap>");  //table formatting
                      
                      buf.append (lines[i]);
                      //buf.append ("</div>");
                      buf.append ("</td></tr>\n");    // table formatting
                      
                      indentSet = indentFound;
                  }
                  else if (indentFound < indentSet)
                  {
                    //buf.append("</div>");  buf.append("\n");
                    indentationCount--;
                    indentationPx = indentationPx  - indentationPlus;
                    
                    //buf.append("<div style=\"background-color: eeee" + hexNoForColorizing (indentationCount) + "\">");
                    buf.append ("</td></tr></table>\n");  //table formatting
                    buf.append ("<tr style=\"background-color: " +  baseRG + hexNoForColorizing (indentationCount) + "\">"); //table formatting
                    buf.append("<td width=\"" + indentationPx +"\">&nbsp;</td><td nowrap>");  //table formatting
                    buf.append (lines[i]);
                    buf.append ("</td></tr>\n");    // table formatting 
                    
                    //buf.append ("</div>");
                    indentSet = indentFound;
                  }
                  
                  else 
                  {
                    //buf.append("<div style=\"background-color: eeee" + hexNoForColorizing (indentationCount) + "\">");
                    buf.append ("<tr style=\"background-color: " +  baseRG + hexNoForColorizing (indentationCount) + "\">"); //table formatting
                    buf.append("<td width=\"" + indentationPx +"\">&nbsp;</td><td nowrap>");  //table formatting
                    buf.append  (lines[i]);
                    buf.append ("</td></tr>\n");    // table formatting
                    //buf.append ("</div>");
                  }
                 
              }
           }
       }
       buf.append ("\n</body>\n</html>");
       //System.out.println (buf.toString());
       return buf.toString();
   }
      
   private static void  specialformat (String jsonLinedString)
   {
       String[] lines = (String[]) filterJSONString (giveTestJSONString()).toArray(new String[]{});
       buildHTMLRepresentation (lines, "hallo");
   }
      
   public static void main (String[] args)
    {
        String jS = giveTestJSONString();
        try
        {
           JSONObject jO = new JSONObject (jS);
           //System.out.println ("my JSONObject \n" + jO.toString(5));
           specialformat (jO.toString(5));
        }
        catch (Exception ex)
        {
           System.out.println ("Exception occured " + ex.toString());    
        }  
    }

}

