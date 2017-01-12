package de.uib.utilities.swing;

import java.awt.*;
import javax.swing.*;

public class HorizontalPositioner extends JPanel
{
   
    public HorizontalPositioner (JComponent leftC,  JComponent rightC)
    {
        setLayout (new BorderLayout());
       add(leftC, BorderLayout.WEST);
       add(rightC, BorderLayout.CENTER);
    }
    
    public HorizontalPositioner (JComponent leftC,  JComponent centerC, JComponent rightC)
    {
        setLayout (new BorderLayout());
       add(leftC, BorderLayout.WEST);
       add( new  CenterPositioner(centerC), BorderLayout.CENTER);
       add(rightC, BorderLayout.EAST);
    }
    
    
}


