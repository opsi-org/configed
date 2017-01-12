package de.uib.configed.gui;

import de.uib.configed.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import de.uib.utilities.*;
import de.uib.utilities.swing.*;

public class Frame1_Infodialog extends JDialog implements ActionListener, KeyListener
{
 JPanel infoPanel; 
 VerticalPositioner textPanel;
 JButton button1 = new JButton();
 JTextField label1 = new JTextField();
 JLabel labelVersion = new JLabel();
 JLabel label3 = new JLabel();
 JLabel label4 = new JLabel();
 BorderLayout borderLayout1 = new BorderLayout();
 BorderLayout borderLayout2 = new BorderLayout();
 FlowLayout flowLayout1 = new FlowLayout();
 String product = de.uib.configed.Globals.APPNAME;
 String version = "2.0";
 public Frame1_Infodialog(Frame parent)
 {
  super(parent);
  enableEvents(AWTEvent.WINDOW_EVENT_MASK);
  try
  {
   jbInit();
  }
  catch(Exception e)
  {
   e.printStackTrace();
  }
  pack();
 }
 /**Initialisierung der Komponenten*/
 private void jbInit() throws Exception
 {
        infoPanel = new JPanel(new BorderLayout());
        this.setTitle(de.uib.configed.Globals.APPNAME);
        setResizable(false);
        label1.setText("Version " + de.uib.configed.Globals.VERSION + " " + de.uib.configed.Globals.VERDATE + " " + de.uib.configed.Globals.VERHASHTAG);
        label1.setEditable(false);
        label1.setBackground(de.uib.configed.Globals.backgroundLightGrey); 
        label1.setFont(de.uib.configed.Globals.defaultFont);
        label3.setText(de.uib.configed.Globals.COPYRIGHT1);
        label3.setFont(de.uib.configed.Globals.defaultFont);
        label4.setText(de.uib.configed.Globals.COPYRIGHT2);
        label4.setFont(de.uib.configed.Globals.defaultFont);
        textPanel = new VerticalPositioner (
            new   CenterPositioner (label1),
            new   CenterPositioner (label3),
            new   CenterPositioner (label4));
        button1.setText("ok");
        button1.addActionListener(this);
				button1.addKeyListener(this);
        //infoPanel.add ( new   CenterPositioner ( new JLabel (product) ), BorderLayout.NORTH);
        infoPanel. add (textPanel, BorderLayout.CENTER);
        infoPanel.add(new   CenterPositioner (button1), BorderLayout.SOUTH); 
        this.getContentPane().add(infoPanel);

 }
 /**Überschrieben, so dass eine Beendigung beim Schließen des Fensters möglich ist.*/
 protected void processWindowEvent(WindowEvent e)
 {
  if (e.getID() == WindowEvent.WINDOW_CLOSING)
  {
   cancel();
  }
  super.processWindowEvent(e);
 }
 /**Dialog schließen*/
 void cancel()
 {
  dispose();
 }
 /**Dialog bei Schalter-Ereignis schließen*/
 public void actionPerformed(ActionEvent e)
 {
  if (e.getSource() == button1)
  {
   cancel();
  }
 }
 
 // KeyListener

  public void keyPressed (KeyEvent e)
  {
			 if (e.getKeyCode() == KeyEvent.VK_ENTER)
			{
				if (e.getSource() == button1)
				{ 
					cancel();
				}
			}
  }

  public void keyReleased (KeyEvent e)
  {
  }

  public void keyTyped (KeyEvent e)  
	{
	}
}