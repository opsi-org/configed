package de.uib.utilities.table.gui;

/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2011 uib.de
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

public class BooleanIconTableCellRenderer extends StandardTableCellRenderer
{
	Icon trueIcon;
	Icon falseIcon;
	
	public BooleanIconTableCellRenderer(Icon trueIcon)
	{
		this( trueIcon, null);
	}
	
	public BooleanIconTableCellRenderer(Icon trueIcon, Icon falseIcon)
	{
		super();
		this.trueIcon = trueIcon;
		this.falseIcon = falseIcon;
	}
	
	public Component getTableCellRendererComponent(
		JTable table,
		Object value,            // value to display
		boolean isSelected,      // is the cell selected
		boolean hasFocus,
		int row,
		int column)
	{
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		
		if (c == null || !(c instanceof JLabel) )
			return c;
		
		JLabel label = (JLabel) c;
		
		if (value != null && !(value instanceof Boolean))
			return c;
		
		label.setText("");
		label.setIcon(null);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		
		if (value == null)
			return c;
		
		//value != null && value instanceof Boolean
		if ( (Boolean) value )
		{
			if (trueIcon != null)
				label.setIcon(trueIcon);
		}
		else
		{
			if (falseIcon != null)
				label.setIcon(falseIcon);
		}
		
		return c;
			
	}
	
}		

