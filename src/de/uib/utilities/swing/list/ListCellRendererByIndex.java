package de.uib.utilities.swing.list;

/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2011 uib.de
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
import java.util.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.swing.*;
import de.uib.configed.Globals;





public class ListCellRendererByIndex extends StandardListCellRenderer
{     
	
	protected Map<String, String> mapOfStrings;
	protected Map<String, String> mapOfTooltips;
	protected Map<String, ImageIcon> mapOfImages;
	final static int imageDefaultWidth = 30;
	protected boolean showOnlyIcon = false;
	
	
	public ListCellRendererByIndex(
		Map<String,String> mapOfStringValues,
		Map<String, String> mapOfDescriptions,
		String imagesBase, int imageWidth,
		boolean showOnlyIcon, String tooltipPrefix)
	
	{
		super(tooltipPrefix);
		this.showOnlyIcon = showOnlyIcon;
		mapOfStrings = mapOfStringValues;
		mapOfTooltips = mapOfDescriptions;
		mapOfImages = new HashMap<String,ImageIcon>();
		
		 //Load the item values 
		{
			Iterator iter = mapOfStrings.entrySet().iterator();
			while (iter.hasNext())
			{
				Map.Entry entry = (Map.Entry) iter.next();
				String key = (String) entry.getKey();
				String stringval = (String) entry.getValue();
				
				ImageIcon image = null;
				
				if (imagesBase != null)
				{
					if (key != null && stringval != null)
					{
						String imageFileString = imagesBase + "/" + key+ ".png";
						//logging.info(this, "key " + key + ",  image file " + imageFileString);
						
						image = de.uib.configed.Globals.createImageIcon(imageFileString, stringval);
						//logging.info(this, "image found " + (image != null));
						
						if (image == null)
						// try with gif
						{
							imageFileString = imagesBase + "/" + stringval + ".gif";
							//logging.info(this, " image file " + imageFileString);
						
							image = de.uib.configed.Globals.createImageIcon(imageFileString, stringval);
							//logging.info(this, "image found " + (image != null));
						}
						
						if (image != null) mapOfImages.put(key, image);
					}
				}
			}
		}
		
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
		
		if (value == null)
		{
			return c;
		}
		
		
		String tooltip = mapOfTooltips.get(value);
		if (tooltip == null || tooltip.equals(""))
			tooltip = mapOfStrings.get(value);
		
		JComponent  jc = (JComponent) c;
		
		if (jc instanceof JLabel)
		{
			((JLabel)jc).setToolTipText(Globals.fillStringToLength(tooltipPrefix 
				+ " " + tooltip + " ", FILL_LENGTH));
		}
		
		return jc;
		
	}
}
