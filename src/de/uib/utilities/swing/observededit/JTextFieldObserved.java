/*
 * JTextFieldObserved .java
 * JTextField which has a listener for any changes of content
 * //Copyright:  Copyright (c) 2011-2012
 * @author roeder
 */
 
package de.uib.utilities.swing.observededit;

import javax.swing.*;
import de.uib.utilities.observer.*;
import de.uib.utilities.*;
import de.uib.utilities.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

public class JTextFieldObserved extends JTextField
		implements KeyListener
{
	protected String startText = "";
	
	protected DataEditListener editListener;
	
	public JTextFieldObserved()
	{
		this("", null);
	}
	
	
	public JTextFieldObserved(DataEditListener editListener)
	{
		super("");
		setEditListener(editListener);
	}
	
	public JTextFieldObserved(ObservableSubject editingNotifier)
	{
		this("", editingNotifier);
		
	}
	
	public JTextFieldObserved(String s, ObservableSubject editingNotifier)
	{
		super(s);
		setObservable(editingNotifier);
		//new PopupMenuFieldEdit(this); no observed event 
	}
	
	public void setObservable(ObservableSubject editingNotifier)
	{
		if (editingNotifier == null)
			return;
		
		editListener = new DataEditListener(editingNotifier, this);
		getDocument().addDocumentListener(editListener);
		addKeyListener(editListener);
		addKeyListener(this);
		
	}
	
	public void setEditListener(DataEditListener editListener)
	{
		this.editListener = editListener;
		getDocument().addDocumentListener(editListener);
		addKeyListener(editListener);
		addKeyListener(this);
	}
	
	public void setTextObserved(String s)
	{
		boolean saveWithFocusCheck = editListener.isWithFocusCheck();
		editListener.setWithFocusCheck(false);
		//logging.debug(this, "setTextObserved set text " + s);
		//logging.debug(this, "setTextObserved focus check " + editListener.isWithFocusCheck());
		
		setText(s);
		//logging.debug(this, "setTextObserved text set " + s); 
		editListener.setWithFocusCheck(saveWithFocusCheck);
	}
	
	@Override
	public void setText(String s)
	{
		//logging.debug(this, "setText " + s);
		startText = s;
		super.setText(s);
		setCaretPosition(0);
		//this.setToolTipText(s);
	}
	
	

	//KeyListener
	public void keyPressed(KeyEvent e)
	{
		//logging.info(this, "key event " + e);
		
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
		{
			//logging.debug(this, "escape");
			setText(startText);
			setCaretPosition(startText.length());
		}
		else if (e.getKeyCode() == KeyEvent.VK_ENTER)
		{
			//transferFocus();
		}
			
	}
	public void keyReleased(KeyEvent e){}
	public void keyTyped(KeyEvent e){}
	
}
