package de.uib.utilities.table.gui;

import javax.swing.JTable;
import javax.swing.table.*;
import de.uib.configed.Globals;
import de.uib.utilities.swing.CellAlternatingColorizer;


public class ColorTableCellRenderer extends DefaultTableCellRenderer 
{
	public ColorTableCellRenderer() {
		super();
	}
	
	public java.awt.Component getTableCellRendererComponent(
		JTable table, Object value, boolean isSelected,
		boolean hasFocus, int row, int column) 
	{
		java.awt.Component cell = super.getTableCellRendererComponent
			(table, value, isSelected, hasFocus, row, column);
			
		CellAlternatingColorizer.colorize(cell, isSelected, (row % 2 == 0), /*(column % 2 == 0),*/ true);
		
		return cell;
		
	}
}
