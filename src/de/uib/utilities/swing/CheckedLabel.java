/*
*	CheckedLabel.java
*	(c) uib 2017
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

import de.uib.utilities.Globals;
import de.uib.utilities.logging.*;

public class CheckedLabel extends JPanel
//is similar to JCheckBox but not interactive
{
	protected JLabel selectedLabel;
	protected JLabel unselectedLabel;
	protected JLabel textLabel;
	
	
	public CheckedLabel(boolean selected)
	{
		this("", selected);
	}
	
	public CheckedLabel(String text, boolean selected)
	{
		this(text, null, null, selected);
	}
	
	public CheckedLabel(Icon selectedIcon, Icon unselectedIcon, boolean selected)
	{
		this("", selectedIcon, unselectedIcon, selected);
	}
	
	
	public CheckedLabel(String text, Icon selectedIcon, Icon unselectedIcon, boolean selected)
	{
		super();
		
		textLabel = new JLabel(text);
		selectedLabel = new JLabel(selectedIcon);
		unselectedLabel = new JLabel(unselectedIcon);
		
		setLayout();
		
		setSelected(selected);
	}
	
	protected void setLayout()
	{
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
	
		
		layout.setVerticalGroup( layout.createParallelGroup()
			.addComponent(textLabel, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight)
			.addComponent(selectedLabel,  de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight)
			.addComponent(unselectedLabel, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight)
		);
		
		layout.setHorizontalGroup( layout.createSequentialGroup()
			.addComponent(textLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(5,5,5)
			.addComponent(selectedLabel,  GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(unselectedLabel,  GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		);
	}

	public void setSelected(boolean b)
	{
		selectedLabel.setVisible(b);
		unselectedLabel.setVisible(!b);
	}
	

}
		
	
	
		
		
		
		
		
		
		
		
			
	
