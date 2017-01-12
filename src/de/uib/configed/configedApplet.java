package de.uib.configed;

import javax.swing.*;
import java.awt.Dimension;
import java.util.*;


public class configedApplet extends JApplet
{	
	configedApplet me;
	configed app;
	ConfigedMain mainController;
	JLabel myLabel;
	String hostname;
	
	
	public void setSize(final int w, final int h)
	{
		final Dimension mySize = new Dimension(w,h); 
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				
				//me.resize(mySize);
				me.setSize (mySize);
				System.out.println (" -- setting size to " +  w + ", " + h );
			}
		}
		);
	}
	
	public void setMainController(ConfigedMain main)
	{
		mainController = main;
	}
	
	public void setClient(final String clientname)
	{
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				if (me.mainController == null)
				{
					System.out.println("controller not created");
				}
				else
				{
					System.out.println("call set client " + clientname);
					me.mainController.setClient(clientname);
				}

			}
		}
		);
	}
	
	public void setTabIndex(final String index)
	{
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				if (me.mainController != null)
				{
					try 
					{
						int indx = Integer.parseInt(index);
						me.mainController.setVisualViewIndex(indx);
					}
					catch(Exception ex)
					{
						System.out.println( index + " is not an integer");
					}
				}
			}
		}
		);
	}
	
	
	public void init()
	{
		initMe();
	}
	
	
	
	private void initMe()
	{
		me = this;
		configed.isApplet = true;
		app = null;
		
		hostname = getCodeBase().getHost().toString();
		
		String documentUrl = getDocumentBase().toString();
		String urlparameters = "";
		
		final HashMap<String, String> parameterMap = new HashMap<String, String>();
		documentUrl.indexOf("?");
		int markPos = documentUrl.indexOf("?"); 
		if ( markPos >= 0 && markPos < documentUrl.length() - 1)
		{
			urlparameters = documentUrl.substring(markPos+1);
			markPos = urlparameters.indexOf("#");
			if (markPos >= 0)
				urlparameters = urlparameters.substring(0, markPos);
			
			String[] params = urlparameters.split("&");
			for (int i = 0; i< params.length; i++)
			{
				String element = params[i];
				
				String var = element;
				String value = "";
				int j = element.indexOf("="); 
				if ( j >=0)
				{
					var = element.substring(0, j);
					if (j + 1 < element.length())
						value = element.substring(j+1);
				}
				parameterMap.put(var, value);
			}
		}
		
		System.out.println("configed version " + Globals.VERSION + ", date " + Globals.VERDATE); 
		System.out.println("url parameters: " + parameterMap);
		
		if (getParameter("locale") != null)
			parameterMap.put("locale", getParameter("locale"));
		
		if (getParameter("tabno") != null)
			parameterMap.put("tabno", getParameter("tabno"));
		
		if (getParameter("clientid") != null)
			parameterMap.put("clientid", getParameter("clientid"));
		
		
		if (getParameter("groupname") != null)
			parameterMap.put("groupname", getParameter("groupname"));
			
		if (getCodeBase().getPort() > -1)
		{
			hostname = hostname + ":" + getCodeBase().getPort();
		}
		myLabel = new JLabel("please log in");
		getContentPane().add(myLabel);
		SwingUtilities.invokeLater(new Runnable(){
		//new Thread(){
			public void run(){
				//getContentPane().remove(myLabel);
				
				Integer tabNo = null;
				if (parameterMap.get("tabno") != null)
				{
					try{
						tabNo = Integer.parseInt(parameterMap.get("tabno"));
					}
					catch(Exception ex)
					{
						System.out.println(parameterMap.get("tabno") + " not an Integer");
					}
				}
				
				app = new configed (
					me, // we offer the applet as host for the application, if set to null, the main frame pops up in an separate window
					parameterMap.get("locale"),//Locale 
					hostname,  // Host
					parameterMap.get("user"),
					null, //Password
					parameterMap.get("clientid"), //selected client
					parameterMap.get("groupname"), //selected group
					tabNo, //selected tab
					null  //LoggingDirectory
				);
				
				
				//setSize(getSize().width- 1, getSize().height-1); // does not resize window
			}
		});
		//}.start();
		
		try
		{
			Thread.sleep(2000);
		}
		catch(InterruptedException ex)
		{
		}
		
		getContentPane().remove(myLabel);

		
	}
	
	public void start()
	{
	}
	
	public void stop()
	{
	}
	
}
 

