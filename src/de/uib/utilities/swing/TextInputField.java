/*
*	TextInputField.java
*	(c) uib 2012
*	GPL licensed
*   Author Rupert Röder
*/

package de.uib.utilities.swing;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import de.uib.configed.Globals;
import de.uib.utilities.logging.*;

public class TextInputField extends JPanel
//common wrapper class for JTextField and JCombBox
{
	
	protected JTextField textfield;
	protected JComboBox combo;
	protected Vector<String> proposedValues;
	
	public enum InputType { TEXT, DATE, VALUELIST};
	
	private InputType inputType; 
	
	
	public TextInputField(String initialValue)
	{
		this(initialValue, null);
	}
	
	public TextInputField(String initialValue, Vector<String> proposedValues)
	{
		super(new BorderLayout());
		
		String initValue = initialValue;
		
		inputType = InputType.VALUELIST;
		
		if (proposedValues == null)
		{
			this.proposedValues = new Vector<String>();;
			
			if (initialValue == null)
			{
				inputType = InputType.DATE;
				initValue = "";
			}
			
			else
				inputType = InputType.TEXT;
			
		}
		
		else
		{
			this.proposedValues = proposedValues;
			proposedValues.add(0, "");
		}
		
		
		combo = new JComboBox(this.proposedValues);
		//logging.debug(this, "class of editor component "  
		//	+ combo.getEditor().getEditorComponent().getClass());
		((JTextField) combo.getEditor().getEditorComponent()).getCaret().setBlinkRate(0);
		
		
		/*
		if (inputType == InputType.DATE)
			textfield = new JFormattedTextField(de.uib.utilities.Globals.getToday());
		
		else
		*/
			
		textfield = new JTextField(initValue);
			
		textfield.getCaret().setBlinkRate(0);
		
		if (inputType == InputType.VALUELIST)
			add(combo);
		else
			add(textfield);

	}
	
	public void addValueChangeListener(
		de.uib.utilities.observer.swing.ValueChangeListener listener
		)
	{
		combo.addActionListener(listener);
		textfield.getDocument().addDocumentListener(listener);
	}
	
	public boolean isEmpty()
	{
		if (inputType == InputType.VALUELIST)
			return combo.getSelectedItem() == null || combo.getSelectedItem().toString().isEmpty();
		
		return textfield.getText().isEmpty();
	}
 
	
	public void setEditable(boolean b)
	{
		textfield.setEditable(b);
		combo.setEditable(b);
	}
	
	public void setToolTipText(String s)
	{
		textfield.setToolTipText(s);
		combo.setToolTipText(s);
	}
	
	public void setText(String s)
	{
		combo.setSelectedItem(s);
		textfield.setText(s);
	}
	
	
	public String getText()
	{
		if (inputType == InputType.VALUELIST)
			return combo.getSelectedItem().toString();
		else
			return textfield.getText();
	}
}
		
	
	
		
		
		
		
		
		
		
		
			
	
