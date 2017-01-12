package de.uib.utilities.swing.list;

/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2010 uib.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 */


import java.awt.*; 
import javax.swing.*;
import de.uib.configed.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.swing.*;



public class StandardListCellRenderer extends DefaultListCellRenderer
{     
	
	protected String tooltipPrefix =  "";
	
	protected int FILL_LENGTH = 20;
	
	
	public StandardListCellRenderer()
	{
		super();
	}
	
	
	public StandardListCellRenderer(String tooltipPrefix)
	{
		super();
		this.tooltipPrefix = tooltipPrefix;
	}
	
	public Component getListCellRendererComponent(
	JList list,
	Object value,            // value to display
	int index,               // cell index
	boolean isSelected,      // is the cell selected
	boolean cellHasFocus)    // the list and the cell have the focus
	{
		Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		if (c == null || !(c instanceof JComponent))
			return c;
		
		JComponent  jc = (JComponent) c;
		CellAlternatingColorizer.colorize(jc, isSelected, (index % 2 == 0) , true);
		jc.setFont(Globals.defaultFont);
		
		if (jc instanceof JLabel)
		{
			((JLabel)jc).setToolTipText(Globals.fillStringToLength(tooltipPrefix + " " + value + " ", FILL_LENGTH));
		}
		
		return jc;
	}
}
