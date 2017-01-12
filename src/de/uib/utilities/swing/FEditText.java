/*
 * FEditText.java
 *
 * (c) uib 2009-2010
 */

package de.uib.utilities.swing;
/**
 *
 * @author roeder
 */
 
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import de.uib.configed.Globals;
import de.uib.utilities.logging.*;

 
public class FEditText extends FEdit
	implements DocumentListener
{
	private javax.swing.JScrollPane scrollpane;
    private javax.swing.JTextArea textarea;
    
    private boolean singleLine;
	
    
    public FEditText(String initialText, String hint)
	{
		super(initialText, hint);
		initFEditText();
		setSingleLine(false);
	}
    
	public FEditText(String initialText)
	{
		super(initialText);
		initFEditText();
    }
    
    protected void initFEditText()
    {	
		scrollpane = new javax.swing.JScrollPane();
		textarea = new javax.swing.JTextArea();
		scrollpane.setViewportView(textarea);
		editingArea.add(scrollpane, BorderLayout.CENTER);
		textarea.setEditable(true);
		textarea.addKeyListener(this);
		textarea.getDocument().addDocumentListener(this);
		setStartText(this.initialText);
	}

	public void setSingleLine(boolean b)
	{
		singleLine = b;
		textarea.setLineWrap(!singleLine);
		textarea.setWrapStyleWord(!singleLine);
	}
	
	public void setStartText(String s)
	{
		super.setStartText(s);
		textarea.setText(s);
	}
	
	public String getText()
	{
		textarea.setText(textarea.getText().replaceAll("\t",""));
		if (singleLine) textarea.setText(textarea.getText().replaceAll("\n","")); 
		initialText = textarea.getText(); //set new initial text for use in processWindowEvent
		return initialText;
	}
	
	public void select(int selectionStart, int selectionEnd)
	{
		textarea.select(selectionStart, selectionEnd);
	}
	
	@Override
	public void keyPressed (KeyEvent e)
	{
		if (e.getSource() == textarea)
		{
			//logging.debug(this, " key event on textarea " + e);
			
			if (
			(e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK 
			&&  e.getKeyCode() == KeyEvent.VK_TAB
			)
				buttonCommit.requestFocusInWindow();
			
			else if ((e.getKeyCode() == KeyEvent.VK_ENTER) && singleLine)
				commit();
		}
		
		super.keyPressed(e);
	}
		
	
	// DocumentListener interface
	public void changedUpdate(DocumentEvent e)
	{
		//logging.debug(this, "changedUpdate");
		setDataChanged(true);
	}
    public void insertUpdate(DocumentEvent e)
	{
		//logging.debug(this, "insertUpdate");
		/*
		//catch tabs and in case returns
		try
		{
			String newPiece = e.getDocument().getText(e.getOffset(), e.getLength());
			logging.debug(this, " --------->" + newPiece + "<");
			if ( newPiece.equals ("\t") )
			{
				//logging.debug(this, "tab");
				buttonCommit.requestFocus();
			}
			
		}
		catch(javax.swing.text.BadLocationException ex)
		{
		}
		*/
		setDataChanged(true);
	}
    public void removeUpdate(DocumentEvent e)
	{
		//logging.debug(this, "removeUpdate");
		setDataChanged(true);
	}

}
