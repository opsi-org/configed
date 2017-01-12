package de.uib.utilities.table;

import javax.swing.*;

public class DefaultListNewCellOptions
{
	public java.util.List getPossibleValues()
	{
		return new ArrayList();
	}
	
	public int getSelectionMode()
	{
		return ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
	}
		
	public boolean isEditable()
	{
		return true;
	}
}
	
	

