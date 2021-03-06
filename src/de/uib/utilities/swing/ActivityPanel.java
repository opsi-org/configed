package de.uib.utilities.swing;

import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import de.uib.utilities.*;
import de.uib.utilities.thread.WaitCursor;
import de.uib.utilities.logging.*;


public class ActivityPanel extends JPanel
	implements Runnable
{
	Color[] colors; 
	
	Thread colorSwitching;
	
	/**sets width = 30 pixel*/
	public static int w = 30;
	/**sets height = a0 pixel*/
	public static int h = 10;
	
	private final int noOfParts = 4; 
	/**an arraylist for panels*/
	ArrayList<JPanel> partPanels = new ArrayList<JPanel>();
	
	public static int sleepingMS = 750;
	
	/**inactive status is -1*/
	public static int inactive = -1;
	/**a blueGrey LineBorder*/
	public static javax.swing.border.LineBorder lineBorderActive;
	/**a blackLightBlue LineBorder*/
	public static javax.swing.border.LineBorder lineBorderInactive; 
	
	/**
	* call the "initGui" method
	*/
	public ActivityPanel()
	{
		initGui();
	}
	
	/** acting status default is false*/
	private static boolean acting = false;
	
	/**
	*
	* Sets the state of the panals with background and border color
	* @param i number of selected panel of arraylist
	*/
	private void setState(int i)
	
	{
		for (int j = 0; j<partPanels.size(); j++)
		{
			setBorder(lineBorderActive);
			partPanels.get(j).setBackground(colors[0]);
			if (i == inactive)
			{
				setBorder(lineBorderInactive);
				partPanels.get(j).setBackground(Globals.backLightBlue);
			}
			else
			{
				setBorder(lineBorderActive);
				partPanels.get(j).setBackground(Globals.backNimbus);
			}
			
			
			if (j == i)
			{
				partPanels.get(j).setBackground(colors[1]);
			}
			
			
			
		}
		try
		{
			//logging.info(this, "paintImmediately i " + i);
			//if (SwingUtilities.isEventDispatchThread())
			{
				//logging.info(this, "event dispatch thread");
				paintImmediately(0,0,w,h); //class cast exceptions mit  sleepingMS = 50 if not event dispatch thread
			}
			//else
				//logging.info(this, "not event dispatch thread");
					
			
			//repaint();
		}
		catch(Exception strange)
		{
			logging.warning(this, "strange exception " + strange);
			setState(inactive);
			
		}
	}
		
	/**
	* Sets global variable "acting" with value of b
	* @param b acting status (true or false)
	*/
	public static void setActing(boolean b)
	{
		acting = b;
	}
	
	//runnable
	/**
	* endless loop
	*
	*
	*/
	public void run()
	{
		int i = 0;
		boolean finalizing = false;
		boolean forward = true;
		while (true)
		{
			try
			{
				//logging.info(this, "sleep " + sleepingMS);
				Thread.sleep(sleepingMS);
				
			}
			catch(InterruptedException ignore) {}
			
			if (acting)
			{
				finalizing = true;
				
				if (i == noOfParts-1)
				{
					forward  = false;
				}
				else if (i == 0)
				{
					forward = true;
				}
				
				setState(i);
				if (forward)
					i++;
				else
					i--;
			}
			else if (finalizing)
			{
				finalizing = false;
				forward = true;
				i = inactive;
				setState(i);
				i = 0;
				try
				{
					Thread.sleep(2*sleepingMS);
				}
				catch(InterruptedException ignore) {}
			}
		}
		
	}
	
	/*
	@Override public void paint(Graphics g
					{
						try{
							super.paintBorder(g);
						}
						catch(java.lang.ClassCastException ex)
						{
							logging.info(this, "the well known exception " + ex);
						}
					}
	*/
	
	protected void initGui()
	{
		lineBorderInactive = new javax.swing.border.LineBorder(Globals.backLightBlue, 1, true);
		lineBorderActive = new javax.swing.border.LineBorder(Globals.blueGrey, 1, true);
		logging.debug(this, "starting");
		setOpaque(true);
		setBorder(lineBorderInactive);
		colors = new Color[2];
		colors[1] =  Globals.opsiLogoBlue; //Globals.backgroundLightGrey;
		colors[0] = Globals.opsiLogoLightBlue;
		setPreferredSize(new Dimension(w, h));
		
		partPanels = new ArrayList<JPanel>();
		
		for (int j = 0; j < noOfParts; j++)
		{
			partPanels.add(new JPanel(){
					
					
					@Override
					public void paint(Graphics g)
					{
						try{
							super.paint(g);
						}
						catch(java.lang.ClassCastException ex)
						{
							setActing(false);
							logging.warning(this, "the ugly well known exception " + ex);
							try
							{
								Thread.sleep(1000);
							}
							catch(Exception x){};
							WaitCursor.stopAll();
						}
					}
					
				}
			);
		
					
			partPanels.get(j).setOpaque(true);
		}
		
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		
		GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
		for (int j = 0; j < noOfParts; j++) 
			hGroup.addComponent(partPanels.get(j), w/noOfParts, w/noOfParts, w/noOfParts);
		layout.setHorizontalGroup(hGroup);
		
		
		GroupLayout.ParallelGroup vGroup = layout.createParallelGroup();
		for (int j = 0; j < noOfParts; j++) 
			vGroup.addComponent(partPanels.get(j), h-2, h-2, h-2);
		layout.setVerticalGroup(vGroup);
	}
	
	public static void main(String[] args)
	{
		JFrame frame = new JFrame();
		frame.setSize(new Dimension(100, 40));
		ActivityPanel panel = new ActivityPanel();
		frame.getContentPane().add(panel);
		frame.setVisible(true);
		panel.run();
	}
}


				
