package de.uib.utilities.swing.timeedit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.text.DateFormat;
import java.sql.Timestamp;
import de.uib.utilities.*;

public class TimeEditField extends JTextField
	implements 
	MouseListener,
	KeyListener
{
	protected FEditDate fDateEditor;
	private DateFormat dateFormat;
	
	public TimeEditField()
	{
		this(null);
	}
	
	public TimeEditField(String s)
	{
		super(s);
		super.setEditable(true);
		addMouseListener(this);
		addKeyListener(this);
		dateFormat = DateFormat.getDateInstance(Globals.dateFormatStylePattern);//DateFormat.LONG);
		
		
	}
	
	public String getText()
	{
		String s = super.getText();
		//logging.debug(this, "we have time " + s);
		
		if (s == null || s.equals(""))
			return s;
		
		try 
		{
			java.util.Date d = dateFormat.parse(s);
			java.sql.Timestamp ts = new Timestamp(d.getTime());
			s = ts.toString();
			//logging.debug(this, "constructed time " + s);
		}
		catch (Exception ex)
		{
			logging.debug(this, "Time format: " + ex);
		}
		
		return s;
	}
		
	
	public void setText(String dateTimeString)
	{
	
		String s = dateTimeString;
		
		if (s != null && !s.equals("") && s.indexOf(' ') == -1)
			//supplement hours etc, if not supplied
				s = s + " 00:00:00";
				
		try
		{
			java.util.Date d = java.sql.Timestamp.valueOf(dateTimeString);
			s = dateFormat.format(d);
		}
		catch (Exception ex)
		{
			//logging.debug(" time format exception: " + ex);
		}			
		
		//logging.debug(this, " we are setting " + s);
		
		super.setText(s);
		
	}
	
	public void constructFDateEditor(String title)
	{
		fDateEditor = new FEditDate("", false);
		fDateEditor.setTitle("("+Globals.SHORT_APPNAME + ") " + title);
		fDateEditor.setCaller(this);
		fDateEditor.setModal(true);
	}
	
	protected void activateEditor()
	{
		logging.debug(this, "activateEditor");
		fDateEditor.setStartText(getText());
		fDateEditor.init(new Dimension(190, 180));
		fDateEditor.setVisible(true);
	}
	
	//MouseListener
	public void  mouseClicked(MouseEvent ev)
	{
		//logging.debug(this, "mouse event on textfield");
		if ( isEnabled() && (ev.getClickCount() > 1 || fDateEditor.isVisible()) )
		{
			activateEditor();
		}
	}
	public void  mouseEntered(MouseEvent e){} 
	public void  mouseExited(MouseEvent e){} 
	public void  mousePressed(MouseEvent e){} 
	public void  mouseReleased(MouseEvent e){}
	
	
	//KeyListener
	public void keyPressed(KeyEvent e)
	{
		if ( isEnabled() &&
			(
				e.getKeyCode() == KeyEvent.VK_SPACE 
			||	e.getKeyCode() == KeyEvent.VK_BACK_SPACE
			||	e.getKeyCode() == KeyEvent.VK_DELETE
			)
		)	
			
		{
			activateEditor();
		}
	}
	
	public void keyReleased(KeyEvent e)
	{}
    public void keyTyped(KeyEvent e)
	{}
	
	

}

