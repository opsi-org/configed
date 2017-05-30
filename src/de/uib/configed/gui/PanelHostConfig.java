package de.uib.configed.gui;

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
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import de.uib.configed.*;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.swing.*;
import de.uib.utilities.datapanel.*;
import de.uib.utilities.table.*;
import de.uib.utilities.table.gui.*;
import de.uib.opsidatamodel.*;
import de.uib.opsidatamodel.datachanges.*;

public class PanelHostConfig extends JPanel
{
	//delegate
	protected EditMapPanelGrouped editMapPanel;
	protected JLabel label;
	
	protected boolean keylistExtendible = true; 
	protected boolean entryRemovable = true;
	protected boolean reloadable = true;
	
	//static Integer classcounter = 0;
	
	public PanelHostConfig()
	{
		/*
		classcounter++;
		if (classcounter > 1)
			System.exit(0);
		*/
		buildPanel();
	}
	
	//overwrite in subclasses
	protected void reloadHostConfig()
	{
		logging.debug(this, "reloadHostConfig");
		// putUsersToPropertyclassesTreeMap();
	}
	
	
	//overwrite in subclasses
	protected void saveHostConfig()
	{
		logging.debug(this, "saveHostConfig");
	}
	
	private void putUsersToPropertyclassesTreeMap()
	{
		Map<String, Object> configs = de.uib.opsidatamodel.PersistenceControllerFactory.getPersistenceController()
							.getConfig(de.uib.opsidatamodel.PersistenceControllerFactory.getPersistenceController().getHostInfoCollections().getConfigServer());
		for (Map.Entry<String, Object> entry : configs.entrySet())
		{
    			String key = (String) entry.getKey();
    			if (key.startsWith(de.uib.opsidatamodel.PersistenceControllerFactory.getPersistenceController().KEY_USER_ROOT + "."))
			{
				String user =key.split("\\.")[1];
				logging.info(this, "putUsersToPropertyclassesTreeMap found config key " + key +"");
				if (! user.equals("{}"))
				{
					String newpropertyclass =key.split("\\.")[0] + "." + user;
					if (! de.uib.opsidatamodel.PersistenceControllerFactory.getPersistenceController().PROPERTYCLASSES_SERVER.containsKey(newpropertyclass))
					{
						logging.info(this, "putUsersToPropertyclassesTreeMap found new user " + user + " [" + newpropertyclass +"]");
						de.uib.opsidatamodel.PersistenceControllerFactory.getPersistenceController().PROPERTYCLASSES_SERVER.put(newpropertyclass, "");
					}
				}
			}
		}
	}
	protected void buildPanel()
	{
		//boolean serverEditing = (ConfigedMain.getEditingTarget() == ConfigedMain.EditingTarget.SERVER);
		label = new JLabel (configed.getResourceValue("MainFrame.jLabel_Config") );
		// putUsersToPropertyclassesTreeMap();
		de.uib.opsidatamodel.PersistenceControllerFactory.getPersistenceController().checkConfiguration();
		putUsersToPropertyclassesTreeMap();
		
		

		editMapPanel = new EditMapPanelGroupedForHostConfigs(
			new de.uib.configed.gui.helper.PropertiesTableCellRenderer(), 
			keylistExtendible, entryRemovable, reloadable,
			//serverEditing, serverEditing, true,
			//de.uib.opsidatamodel.PersistenceControllerFactory.getPersistenceController().PROPERTYCLASSES,
			new AbstractEditMapPanel.Actor()
			{
				protected void reloadData()
				{
					reloadHostConfig();
				}
				
				protected void saveData()
				{
					saveHostConfig();
				}
			}
		);
		
		
		/*
		JPanel header =new JPanel();
		
		GroupLayout headerLayout = new GroupLayout(header);
		header.setLayout(headerLayout);
		
		headerLayout.setHorizontalGroup(
			headerLayout.createSequentialGroup()
				.addGap(de.uib.utilities.Globals.hGapSize)
				.addComponent(label, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(de.uib.utilities.Globals.hGapSize)
			)
		;
	
		headerLayout.setVerticalGroup(
			headerLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(label)
			)
		;
		*/
		GroupLayout planeLayout = new GroupLayout( this );
		this.setLayout( planeLayout );
		
		planeLayout.setHorizontalGroup(
			planeLayout.createSequentialGroup()
			//.addGap(de.uib.utilities.Globals.vGapSize)
			.addGroup(planeLayout.createParallelGroup()
				//.addComponent( header, GroupLayout.Alignment.CENTER )
				.addComponent( editMapPanel )
			)
			//.addGap(de.uib.utilities.Globals.vGapSize)
		);
		
		planeLayout.setVerticalGroup(
			planeLayout.createSequentialGroup()
			//.addGap(20)
			//.addComponent( header,  GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			//.addGap(5)
			.addComponent( editMapPanel, de.uib.configed.Globals.lineHeight * 2, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE )
			//.addGap(20)
		);
	}
	
	
	
	
	public void initEditing(
		String labeltext,
		Map configVisualMap,
		Map<String,  de.uib.utilities.table.ListCellOptions> configOptions,
		Collection collectionConfigStored,
		AdditionalconfigurationUpdateCollection configurationUpdateCollection,
		boolean optionsEditable,
		TreeMap<String, String> classesMap
		)
	{
		initEditing(
		labeltext,
		configVisualMap,
		configOptions,
		collectionConfigStored,
		configurationUpdateCollection,
		optionsEditable,
		classesMap,
		null
		);
	}
	
	public void initEditing(
		String labeltext,
		Map configVisualMap,
		Map<String,  de.uib.utilities.table.ListCellOptions> configOptions,
		Collection collectionConfigStored,
		AdditionalconfigurationUpdateCollection configurationUpdateCollection,
		boolean optionsEditable,
		TreeMap<String, String> classesMap,
		EditMapPanelX.PropertyHandlerType propertyHandlerType
		)
	{
		label.setText(labeltext);
		
		//configed.getResourceValue("MainFrame.jLabel_Config")  + ":   " + hostId);
		
		
		logging.info(this, "initEditing " 
		//	+ " configVisualMap  " +  (configVisualMap)
		//	+ " configOptions  " + (configOptions)
			+ " optionsEditable "  + optionsEditable 
			);
		editMapPanel.setClasses( classesMap );
		editMapPanel.setEditableMap(
			configVisualMap, 
			configOptions); 
		editMapPanel.setStoreData(collectionConfigStored);
		editMapPanel.setUpdateCollection(configurationUpdateCollection);
		
		
		editMapPanel.setLabel(labeltext);
		
		editMapPanel.setOptionsEditable( optionsEditable );
		((EditMapPanelGrouped)editMapPanel).setPropertyHandlerType( propertyHandlerType ); // if null then defaultPropertyHandler is set
		
	}

	

	//delegated methods
	public void registerDataChangedObserver( DataChangedObserver o )
	{
		editMapPanel.registerDataChangedObserver(o);
	}
	
	protected void setEditableMap(
		Map visualdata,
		Map<String, ListCellOptions> optionsMap
		)
	{
		editMapPanel.setEditableMap(visualdata, optionsMap);
	}
		
}
