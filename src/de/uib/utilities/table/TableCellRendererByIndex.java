package de.uib.utilities.table; 

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import de.uib.utilities.*;
import de.uib.utilities.swing.*;


public class TableCellRendererByIndex extends DefaultTableCellRenderer
{
	Map<String, String> mapOfStrings;
	Map<String, ImageIcon> mapOfImages;
	Color backgroundColor;
	
	public TableCellRendererByIndex(Map<String,String> mapOfStringValues, String imagesBase)
	{
		setMap(mapOfStringValues, imagesBase);
	}
	
	public void setMap(Map<Integer, String> map)
	{
		mapOfStrings = new HashMap<String, String>();
		for (Integer key : map.keySet())
		{
			mapOfStrings.put("" + key, map.get(key));
		}
	}
	
	public void setMap(Map<String,String> mapOfStringValues, String imagesBase)
	{
		//logging.debug(this, "setMapping " + mapOfStringValues);
		
		mapOfStrings = mapOfStringValues;
		mapOfImages = new HashMap<String, ImageIcon>();
		 //Load the item images 
		if (imagesBase != null)
		{
			Iterator iter = mapOfStrings.entrySet().iterator();
			while (iter.hasNext())
			{
				Map.Entry entry = (Map.Entry) iter.next();
				String key = (String) entry.getKey();
				String stringval = (String) entry.getValue();
				
				
				ImageIcon image = null;
				
				if (key != null && stringval != null)
				{
					String imageFileString = imagesBase + Globals.fileseparator + stringval + ".png";
					System.out.println (" image file " + imageFileString);
					
					image = createImageIcon(imageFileString);
					if (image != null) mapOfImages.put(key, image);
				}
			}
		}
		
	}
	
	
	
	public void setBackgroundColor(Color c)
	{
		backgroundColor = c;
	}
	
	
	/** Returns an ImageIcon, or null if the path was invalid. */
	protected static javax.swing.ImageIcon createImageIcon(String absolutePath) 
	{
		//System.out.println ( " ---- image path: " +  absolutePath );
		
		java.net.URL url = null;
		try{
			url = new java.net.URL(absolutePath);
		}
		catch(Exception e){
			System.out.println(" could not convert path \"" + absolutePath + "\" to URL, " + e.toString() );
		}
		
		if (url != null){
				return new javax.swing.ImageIcon(url);
		} 
		else {
				System.out.println("Couldn't find file: " + absolutePath);
				return null;
		}
	}
	
	
	public Component getTableCellRendererComponent(
	JTable table,
	Object value,            // value to display
	boolean isSelected,      // is the cell selected
	boolean hasFocus,
	int row,
	int column)
	{
		Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		
		String selectedString = "";
		ImageIcon selectedIcon = null;
		
		if (backgroundColor != null) result.setBackground (backgroundColor);
		result.setForeground (java.awt.Color.black);
		
		
		if (value != null) 
		{
			
			//System.out.println (" :1 value is " + value + " value class is " + value.getClass());
			if (mapOfStrings != null) selectedString = mapOfStrings.get("" + value);
			//System.out.println (" :1 selectedString is " + selectedString);
			//System.out.println (" :2 value is " + value + " value class is " + value.getClass());
			if (mapOfImages != null) selectedIcon = mapOfImages.get("" + value);
			
			/*
			if (value instanceof String)
			{
				try
				{
					selectedIndex = Integer.decode(((String)value).trim());
				}
				catch (Exception ex)
				{ System.out.println ("TableCellRendererByIndex " + ex); }
			}
			else
			{
				try
				{
					selectedIndex = ((Integer)value).intValue();
				}
				catch (Exception ex)
				{
					System.out.println ("TableCellRendererByIndex " + ex); 
				}
			}
			*/
			
		}
		
		//logging.debug(this, "getTableCellRendererComponent: value " + value + ", selectedString " + selectedString);
			
		
		if (result instanceof JLabel) {
			((JLabel)result).setText(selectedString);
			((JLabel)result).setIcon(selectedIcon);
			((JLabel)result).setToolTipText(selectedString);
		}
		
		CellAlternatingColorizer.colorize(result, isSelected, (row % 2 == 0) , true);
		//result.setFont(Globals.defaultFont);
		
		
		return result;
	}
}

