package de.uib.utilities.table;

import javax.swing.*;

public interface ListCellOptions
{
	public java.util.List getPossibleValues();
	public java.util.List getDefaultValues();
	public void setDefaultValues(java.util.List values);
	public int getSelectionMode();
	public boolean isEditable();
}
	

