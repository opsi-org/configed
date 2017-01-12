package de.uib.utilities.swing;

import de.uib.configed.Globals;
import de.uib.utilities.logging.*;


public class CellAlternatingColorizer
{
	public static void colorize ( java.awt.Component cell, boolean isSelected, boolean isEven, boolean textColoring)
	{
		if (textColoring) cell.setForeground( Globals.lightBlack );
		
		/*
		if (isSelected) 
		{
			cell.setBackground( Globals.defaultTableSelectedRowDark );
		}
		*/
		//logging.debug("CellAlternatingcolorize selection, even  " + isSelected + ", " + isEven); 
		
		if (isSelected) 
		{
			if (isEven)
				cell.setBackground( Globals.defaultTableSelectedRowDark);
			else
				cell.setBackground( Globals.defaultTableSelectedRowBright);
		}
	
		else
		{
			if (isEven)
			{
				cell.setBackground( Globals.defaultTableCellBgColor2 );
			}
			else
			{
				cell.setBackground( Globals.defaultTableCellBgColor1 );
			}
		}
		
	}
	
	
	public static void colorizeSecret( java.awt.Component cell )
	{
		cell.setBackground( Globals.defaultTableSelectedRowBright);
		cell.setForeground( Globals.defaultTableSelectedRowBright);
	}
		
		
	
	public static void colorize ( java.awt.Component cell, boolean isSelected, boolean rowEven, boolean colEven,  boolean textColoring)
	{
		
		if (textColoring) cell.setForeground( Globals.lightBlack );
		
		if (isSelected) 
		{
			if (rowEven)
				cell.setBackground( Globals.defaultTableSelectedRowDark);
			else
				cell.setBackground( Globals.defaultTableSelectedRowBright);
				
		}
	
		else
		{
			if (rowEven && colEven) // 0,0 
			{
				cell.setBackground( Globals.defaultTableCellBgColor00 );
			}
			else if (rowEven && !colEven) //0,1
			{
				cell.setBackground( Globals.defaultTableCellBgColor01 );
			}
			else if (!rowEven && colEven) //1,0
			{
				cell.setBackground( Globals.defaultTableCellBgColor10 );
			}
			else
			{
				cell.setBackground( Globals.defaultTableCellBgColor11 );
			}
		}
				
	}
}
