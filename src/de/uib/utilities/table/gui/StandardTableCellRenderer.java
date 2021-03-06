package de.uib.utilities.table.gui; 

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import de.uib.utilities.swing.CellAlternatingColorizer;
import de.uib.utilities.logging.*;
import de.uib.configed.Globals;

public class StandardTableCellRenderer extends DefaultTableCellRenderer
{
	
	protected String tooltipPrefix =  null;
	protected String separator =  ": ";
	
	protected int FILL_LENGTH = 20;
	
	public StandardTableCellRenderer()
	{
		super();
	}
	
	public StandardTableCellRenderer(String tooltipPrefix)
	{
		this();
		this.tooltipPrefix = tooltipPrefix;
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
		
		if (c == null || !(c instanceof JComponent))
			return c;
		
		JComponent  jc = (JComponent) c;
		CellAlternatingColorizer.colorize(jc, isSelected, (row % 2 == 0), (column % 2 == 0), true);
		
		if (jc instanceof JLabel)
		{
			String tooltipText = null;
			if (tooltipPrefix != null && !tooltipPrefix.equals(""))
				tooltipText = Globals.fillStringToLength(tooltipPrefix + separator +  value + " ", FILL_LENGTH);
			else
				tooltipText = Globals.fillStringToLength(value + " ", FILL_LENGTH);
			
			((JLabel)jc).setToolTipText(tooltipText);
		}
		return jc;
	}
	
}
		
