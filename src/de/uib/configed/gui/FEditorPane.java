package de.uib.configed.gui;

/**
 * FEditorPane
 * Copyright:     Copyright (c) 2001-2005, 2016
 * Organisation:  uib
 * @author Rupert Röder
 * @version
 */
import de.uib.configed.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class FEditorPane extends FGeneralDialog
{

	JEditorPane editPane = new JEditorPane();

	public FEditorPane(Frame owner, String title, boolean modal, Object[] buttonList, int preferredWidth, int preferredHeight)
	{
		super (owner, title, modal, buttonList, preferredWidth, preferredHeight);
		init();
	}


	private void init()
	{
		editPane.setOpaque(true);
		//editPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		editPane.setBackground(Globals.backgroundGrey);
		editPane.setText("          ");
		editPane.setEditable(false);
		editPane.setFont(new java.awt.Font("Dialog", 0, 10));

		scrollpane.getViewport().add(editPane, null);

		editPane.addKeyListener(this);

		pack();

	}
	
	public boolean setPage(String url)
	{
		
		/* tip from stackoverflow for a ssl connection:
		
		Extend JEditorPane to override the getStream() method.
		
		Inside that method, you can open a URLConnection. Test whether it is an HttpsURLConnection. If it is, initialize your own SSLContext with a custom X509TrustManager that doesn't perform any checks. Get the context's SSLSocketFactory and set it as the socket factory for the connection. Then return the InputStream from the connection.
		
		This will defeat any attempts by the runtime to protect the user from a spoof site serving up malware. If that's really what you want…
		*/
		
		boolean result = true;
		editPane.setEditable(false);
		try
		{
			editPane.setPage(url);
		}
		catch(java.io.IOException ioe) 
		{
			result = false;
			editPane.setContentType("text/html");
			editPane.setText("<html>Could not load " + url + "<br>" + ioe +"</html>");
		}
		
		return result;
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

		editPane.setContentType("text/html");
		editPane.setText ( header + s + footer);
		editPane.setCaretPosition(0);
	}


	// KeyListener



	public void keyReleased (KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_SHIFT)
		{ shiftPressed = false;
			//System.out.println ("shift released");
		}

		if (e.getKeyCode () == KeyEvent.VK_TAB && !shiftPressed)
		{
			if (e.getSource() == editPane)
			{jButton1.requestFocus();}
		}

		if (e.getKeyCode () == KeyEvent.VK_TAB && shiftPressed)
		{
			if (e.getSource() == jButton1)
			{editPane.requestFocus();}
		}
	}



}
