package de.uib.utilities.swing.timeedit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*; 
import java.text.*;
import java.sql.Timestamp;

import de.uib.utilities.*;
import de.uib.utilities.swing.*;
import de.uib.utilities.observer.*;
import de.uib.utilities.swing.observededit.*;


public class CheckedTimeField extends JTextFieldObserved
	implements 
	MouseListener,
	KeyListener,
	DocumentListener
{
	private DateFormat dateFormatLong;
	private DateFormat dateFormatShort;
	private DateFormat dateFormatSimple;
	
	private String initialS; 
	
	protected boolean withTime = false;
	
	private Font normalFont;
	private Color normalColor;
	private Font markedFont;
	private Color markedColor = Color.RED;
	
	public CheckedTimeField()
	{
		this("");
	}
	
	
	public CheckedTimeField(String s)
	{
		this(s, (ObservableSubject) null);
		addKeyListener(this);
	}
	
	//only for compatiblity of signature with TimeEditField
	public CheckedTimeField(String s, String title)
	{
		this(s);
	}
	
	//only for compatiblity of signature with TimeEditField
	public CheckedTimeField(String s, String title, ObservableSubject editingNotifier)
	{
		this(s, editingNotifier);
	}
	
	public CheckedTimeField(String s, ObservableSubject editingNotifier)
	{
		super(s, editingNotifier);
		
		setEditable(true);
		normalColor = getForeground();
		setBackground(Globals.backNimbusLight);
		getDocument().addDocumentListener(this);
		dateFormatLong = DateFormat.getDateInstance( DateFormat.LONG );
		//dateFormatLong.setLenient(false);
		dateFormatShort = DateFormat.getDateInstance( DateFormat.SHORT );
		dateFormatShort.setLenient(false); // do not allow e.g. month 13
		dateFormatSimple = new SimpleDateFormat("dd.MM.yyyy");
		dateFormatSimple.setLenient(false);
		
		//addKeyListener(this); in superclass
 
	}
	
	@Override
	public void copy()
	{
		super.copy();
		indicateFormatDefect();
	}
	
	@Override
	public void cut()
	{
		super.cut();
		indicateFormatDefect();
	}
	
	@Override
	public void paste()
	{
		super.paste();
		indicateFormatDefect();
	}
	
	protected void mark()
	{
		setForeground(markedColor);
	}
	
	protected void unmark()
	{
		setForeground(normalColor);
	}
	
	protected java.util.Date checkDate(String s) throws IllegalArgumentException
	{         
		//logging.debug(this, "checkDate,  parse >>" + s + "<<" );
		
		if (
			s == null || s.equals("")
			)
			return null;
		
		ParsePosition ppos = new ParsePosition(0);
		java.util.Date d = null;
		
		
		//first try
		d = dateFormatLong.parse(s, ppos);
		
		//logging.debug(this, " parse pos " + ppos);
		
		if (d  == null)
		{
			//second try
			ppos = new ParsePosition(0);
			d = dateFormatShort.parse(s, ppos);
			//logging.debug(this, " parse pos " + ppos);
		}
		
		if (d == null)
			throw new IllegalArgumentException("Kein gültiges Datum gefunden");
		
		if (s != null && ppos.getIndex() < s.length())
			throw new IllegalArgumentException("Datumstring enthält nicht interpretierbare Zeichen ");
		
		return d;
	}
	
	protected void indicateFormatDefect()
	{
		try
		{
			String s = getDocument().getText(0, getDocument().getLength());
			checkDate(s);
			unmark();
		}
		catch (Exception ex)
		{
			mark();
		}
		
	}
	
	
	public String getText()
	{
		String s = super.getText();
		
		if (s == null || s.equals(""))
			return s;
		
		try 
		{
			java.util.Date d = checkDate(s);
			java.sql.Timestamp ts = new Timestamp(d.getTime());
			s = ts.toString();
			//setText(s);
			//logging.debug(this, "constructed time " + s);
		}
		catch (Exception ex)
		{
			//logging.debug(this, "Time format: " + ex);
			return "";
		}
		
		return s;
	}
		
	
	@Override
	public void setText(String dateTimeString)
	{
	
		String s = dateTimeString;
		initialS = s;
		
		
		if (s != null && !s.equals("") && s.indexOf(' ') == -1)
			//supplement hours etc, if not supplied
				s = s + " 00:00:00";
				
		try
		{
			java.util.Date d = java.sql.Timestamp.valueOf(dateTimeString);
			s = dateFormatSimple.format(d);
			//logging.debug(this, " formatted : " + s);
		}
		catch (Exception ex)
		{
			//logging.debug(" time format exception: " + ex);
		}			
		
		//logging.debug(this, "setText: we are setting " + s);
		
		super.setText(s);
		
	}
	
	protected void cancel()
	{
		setText(initialS);
	}
	
	
	//MouseListener
	public void  mouseClicked(MouseEvent ev)
	{
	}
	public void  mouseEntered(MouseEvent e){} 
	public void  mouseExited(MouseEvent e){} 
	public void  mousePressed(MouseEvent e){} 
	public void  mouseReleased(MouseEvent e){}
	
	
	//KeyListener
	public void keyPressed(KeyEvent e)
	{
		//logging.info(this, "key event " + e);
		
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
		{
			cancel();
		}
			
		else if (e.getKeyCode() == KeyEvent.VK_ENTER)
		{
			//transferFocus();
		}
			
	}
	
	public void keyReleased(KeyEvent e)
	{}
    public void keyTyped(KeyEvent e)
	{}
	
	
	//DocumentListener
	public void  changedUpdate(DocumentEvent e) 
	{
		indicateFormatDefect();
	}
	
    public void  insertUpdate(DocumentEvent e)
    {
    		indicateFormatDefect();
	}
	
    public void  removeUpdate(DocumentEvent e)
    {
    		indicateFormatDefect();
	}

}

