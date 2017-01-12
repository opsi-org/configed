package de.uib.utilities.table;

import java.util.*;
import javax.swing.*;


public class DefaultListCellOptions 
implements ListCellOptions
{
	 java.util.List possibleValues;
	 java.util.List defaultValues;
	 int selectionMode;
	 boolean editable;
	 
	 public DefaultListCellOptions()
	 {
	 	 possibleValues = new ArrayList();
	 	 defaultValues = new ArrayList();
	 	 selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
	 	 editable = true;
	 }
	 
	 public DefaultListCellOptions(
	 	 java.util.List possibleValues,
	 	 java.util.List defaultValues,
	 	 int selectionMode,
	 	 boolean editable)
	{
		this.possibleValues = possibleValues;
	 	this.defaultValues = defaultValues;
	 	this.selectionMode = selectionMode;
	 	this.editable = editable;
	}
	
	
	public static ListCellOptions getNewBooleanListCellOptions()
	{
		 java.util.List possibleValues = new ArrayList();
		 possibleValues.add(true);
		 possibleValues.add(false);
		 java.util.List defaultValues = new ArrayList();
		 defaultValues.add(false);
		 boolean editable = false;
		 return  
		 	new DefaultListCellOptions(
				possibleValues, 
				defaultValues, 
				ListSelectionModel.SINGLE_SELECTION,
				editable
			);
	}
	
	public static ListCellOptions getNewEmptyListCellOptions()
	{
		 java.util.List possibleValues = new ArrayList();
		 boolean editable = true;
		 return  
		 	new DefaultListCellOptions(
				possibleValues, 
				null, //defaultValues, 
				ListSelectionModel.SINGLE_SELECTION,
				editable
			);
	}
	
	public static ListCellOptions getNewEmptyListCellOptionsMultiSelection()
	{
		 java.util.List possibleValues = new ArrayList();
		 boolean editable = true;
		 return  
		 	new DefaultListCellOptions(
				possibleValues, 
				null, //defaultValues, 
				ListSelectionModel.MULTIPLE_INTERVAL_SELECTION,
				editable
			);
	}
	
	
	public java.util.List getPossibleValues()
	{
		return possibleValues;
	}
	
	public java.util.List getDefaultValues()
	{
		return defaultValues;
	}
	
	public void setDefaultValues(java.util.List values)
	{
		defaultValues = values;
	}
	
	public int getSelectionMode()
	{
		return selectionMode;
	}
		
	public boolean isEditable()
	{
		return editable;
	}
	
	@Override
	public String toString()
	{
		return "DefaultListCellOptions,  possibleValues: " + possibleValues 
		+ "; defaultValues: " + defaultValues 
		+ "; selectionMode: " + selectionMode
		+ "; editable: " + editable;
	}
	
}
	
	

