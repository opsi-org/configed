package de.uib.utilities;

import java.util.concurrent.atomic.*;
import java.awt.*;
import java.util.Vector;
import de.uib.utilities.logging.*;
import de.uib.configed.Globals;
import de.uib.utilities.swing.ActivityPanel;

public class WaitCursor
{

	static Vector<WaitCursor> instances = new Vector<WaitCursor> ();
	static AtomicInteger objectCounting = new AtomicInteger();
	//static int objectCounting = 0;
	int objectNo; 
	
	boolean ready = false;

	Cursor saveCursor;
	Component c;
	String callLocation;

	public WaitCursor()
	{
		this(null, new Cursor(Cursor.DEFAULT_CURSOR));
	}

	public WaitCursor(Component c_calling)
	{
		this(c_calling, new Cursor(Cursor.DEFAULT_CURSOR));
	}
	
	public WaitCursor(Component c_calling, String callLocation)
	{
		this(c_calling, new Cursor(Cursor.DEFAULT_CURSOR), callLocation);
	}

	public WaitCursor(Component c_calling, Cursor saveCursor)
	{
		this(c_calling, saveCursor, "(not specified)");
	}
	
	public WaitCursor(Component c_calling, Cursor saveCursor, String callLocation)
	{
		//instances.add(this);
		//objectCounting++;
		objectNo = objectCounting.addAndGet(1);
		//objectNo = objectCounting;
		
		
		this.saveCursor = saveCursor;
		this.callLocation = callLocation;


		if (c_calling == null)
		{
			try
			{
				c = Globals.mainContainer;
			}
			catch(Exception ex)
			{
				logging.info(this, "retrieveBasePane " + ex);
				c = null;
			}
		}
		else
			c = c_calling;

		logging.info(this, "adding instance " + objectNo + "-- call location at (" + callLocation + ") on component " + c );
		/*
		if (c instanceof javax.swing.JTextField)
		{
			logging.info(this, "adding instance " + objectNo + " on textfield " + ((javax.swing.JTextField) c).getText()); 
		}
		*/

		final Component cX = c;
		

		new Thread(){
			public void run()
			{
				if (!ready & cX != null)
				{
					logging.info(this, " set wait cursor  on " + cX );
					
					cX.setCursor(new Cursor(Cursor.WAIT_CURSOR));
					
					//does not get the component for event queue reasons
				}
				
				ActivityPanel.setActing(true);
				
				/*
				try{
					javax.swing.SwingUtilities.invokeAndWait(new Thread(){
							public void run()
							{
								ActivityPanel.setActing(true);
							}
						}
					);
				}
				catch(Exception ex)
				{
					logging.info(this, "WaitCursor SwingUtilities.invokeAndWait thread " + ex);
				}
				*/
				
				//logging.debug(this, " cursor carrying component " + c);
				while (!ready)
				{
					try
					{
						Thread.sleep (200);
						//logging.debug(this, "running wait cursor thread ");
					}
					catch (InterruptedException ex)
					{}
				}
			}
		}.start();

	}

	public void stop()
	{
		logging.info(this, " stop wait cursor " + objectNo + ", was located at (" + callLocation + ")");
		ready = true;
		
		if (c != null) c.setCursor(saveCursor);
		/*	
		
			try{
				javax.swing.SwingUtilities.invokeLater(new Thread(){
						public void run()
						{
							instances.remove(this);
							logging.debug(this, "removing instance " + objectNo);
							ActivityPanel.setActing(false);
						}
					}
				);
			}
			catch(Exception ex)
			{
				logging.info(this, "WaitCursor SwingUtilities.invokeAndWait thread " + ex);
			}
		
		*/
		//instances.remove(this);
		objectCounting.decrementAndGet(); 
		logging.debug(this, "removing instance " + objectNo);
		if (objectCounting.get() == 0)
		//if (instances.size() == 0)
		{
			logging.debug(this, "seemed to be last living instance");
			ActivityPanel.setActing(false);
		}
		else
		{
			logging.info(this, " stopped wait cursor " 
				+ " instance " + objectNo + ", " 
				+ " still active  " + objectCounting  
				//+ getInstancesNumbers()
				);
		}
			
	}
	
	public  boolean isStopped()
	{
		return ready;
	}
	
	public static void stopAll()
	{
		/*
		Vector<WaitCursor> instancesCopy = new Vector<WaitCursor>();
		for (WaitCursor instance : instances)
		{
			instancesCopy.add(instance);
		}
		for (WaitCursor instance : instancesCopy)
		{
			instance.stop();
		}
		*/
	}
	
	private int getObjectNo()
	{
		return objectNo;
	}
	
	private static String getInstancesNumbers()
	{
		StringBuffer listing  = new StringBuffer("[ ");
		for (WaitCursor inst : instances)
		{
			listing.append(inst.getObjectNo());
			listing.append(" ");
		}
		listing.append("]");
		
		return listing.toString();
	}
}

