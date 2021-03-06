package de.uib.utilities.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import de.uib.configed.*;
import de.uib.utilities.logging.*;

public class JTextEditorField extends javax.swing.JTextField
	implements  KeyListener
{
	String lastSetS = null;
	
	public JTextEditorField(String s)
	{
		super(s);
		addKeyListener(this);
	}
	
	public JTextEditorField(Document doc, String text, int columns)
	{
		super(doc, text, columns);
		addKeyListener(this);
	}
	
	public void setText(String s)
	{
		logging.debug(this, "setText " + s);
		if (s == null)
			lastSetS = "";
		else
			lastSetS = s;
		super.setText(s);
	}
	
	
	public boolean isChangedText()
	{
		if (lastSetS == null && getText() == null)
			return false;
		
		if (lastSetS == null && getText() != null)
			return true;
		
		return !lastSetS.equals( getText() );
	}
		
	
	//KeyListener
	public void keyTyped(KeyEvent e)
	{
		
	}
	
	public void  keyPressed(KeyEvent e)
	{
		//logging.debug(this, "keyPressed  code " + e.getKeyCode()  + " char " + e.getKeyChar());
		//logging.debug(this, "keyPressed  KeyEvent.VK_ESCAPE " + KeyEvent.VK_ESCAPE);
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
		{
			setText(lastSetS);
		}
	}
	
	public  void  keyReleased(KeyEvent e){}
	
}
	
	
