package de.uib.configed.gui.productpage;

/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2013 uib.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 */

import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.configed.productgroup.*;
import de.uib.opsidatamodel.datachanges.*;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.table.*;

import java.io.File;

import de.uib.utilities.swing.*;
import de.uib.utilities.table.gui.*;
import de.uib.utilities.datapanel.*;
import de.uib.utilities.logging.*;
import de.uib.configed.guidata.*;
import de.uib.configed.gui.helper.*;
import de.uib.opsidatamodel.productstate.*;


public class PanelGroupedProductSettings extends PanelProductSettings
{
	
	//State reducedTo1stSelection
	//List reductionList
	
	
	de.uib.configed.productgroup.GroupPanel groupPanel;
	
	JMenuItemFormatted popupMarkHits;
	
	public PanelGroupedProductSettings(String title, ConfigedMain mainController, LinkedHashMap<String, Boolean> productDisplayFields,
		boolean packageGroupsVisible)
	{
		super(title, mainController, productDisplayFields);
		init();
	}
	
	@Override
	protected void producePopupMenu(final Map<String, Boolean> checkColumns)
	{
		
		super.producePopupMenu( checkColumns );
		/*
		popup.addSeparator();
		popupMarkHits = new JMenuItemFormatted();
		
		popupMarkHits.setText( configed.getResourceValue("PanelGroupedProductSettings.markAllFoundItems") );
		popupMarkHits.setFont(Globals.defaultFont);
		popupMarkHits.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				groupPanel.markAllSearchResults();
			}
		});
		
		popup.add(popupMarkHits);
		*/
	}
		
		
	public PanelGroupedProductSettings(String title, ConfigedMain mainController, LinkedHashMap<String, Boolean> productDisplayFields)
	{
		this(title, mainController, productDisplayFields, true);
	}
	
	protected void activatePacketSelectionHandling(boolean b)
	{
		if (b)
			tableProducts.getSelectionModel().addListSelectionListener(groupPanel);
		else
			tableProducts.getSelectionModel().removeListSelectionListener(groupPanel);
	}
	
	public void setSearchFields(java.util.List<String> fieldList)
	{
		groupPanel.setSearchFields(fieldList);
	}
	
			
	@Override
	protected void initTopPane()
	{
		if (tableProducts == null)
		{
			logging.info(this, " tableProducts == null ");
			System.exit(0);
		}
		topPane = new GroupPanel(this, mainController, tableProducts);
		topPane.setVisible(true);
		groupPanel = (GroupPanel) topPane;
	}
	
	@Override
	protected void init()
	{
		super.init();
	
		activatePacketSelectionHandling(true);
		tableProducts.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	
		
	public void setGroupsData(
		final Map<String, Map<String, String>> data,
		final Map<String, Set<String>> productGroupMembers
		)
	{
		groupPanel.setGroupsData(data, productGroupMembers);
		showAll();
	}
	
	public void setTableModel (InstallationStateTableModelFiltered istm)
	{
		super.setTableModel(istm);
	}
	
	
	public void clearSelection()
	{
		tableProducts.clearSelection();
	}
	
	@Override
	public void setSelection(Set selectedIDs)
	{
		activatePacketSelectionHandling(false);
		clearSelection();
		if (selectedIDs != null)
		{
			if (selectedIDs.size() == 0 && tableProducts.getRowCount() > 0)
			{
				tableProducts.addRowSelectionInterval(0, 0);
				//show first product if no product given
				logging.info(this, "setSelection 0");
			}
			else
			{
				for (int row = 0; row < tableProducts.getRowCount(); row++)
				{
					Object productId = tableProducts.getValueAt(row, 0);
					if (selectedIDs.contains(productId))
						tableProducts.addRowSelectionInterval(row, row);
				}
			}
		}
		activatePacketSelectionHandling(true);
		groupPanel.findGroup(selectedIDs);
	}
	
	@Override
	public Set<String> getSelectedIDs()
	{
		HashSet<String> result = new HashSet();
		
		int[] selection = tableProducts.getSelectedRows();
		
		
		for (int i = 0; i < selection.length; i++)
		{
			result.add((String) tableProducts.getValueAt(selection[i], 0));
		}
		
		return result;
	}
	
	public void reduceToSet(Set<String> filter)
	{
		activatePacketSelectionHandling(false);
		//Set<String> testSet = new HashSet<String>();
		//testSet.add("jedit");
		InstallationStateTableModelFiltered tModel 
			= (InstallationStateTableModelFiltered) tableProducts.getModel();
		tModel.setFilterFrom( filter  );
		
		logging.info(this, "reduceToSet  " + filter);
		logging.info(this, "reduceToSet GuiIsFiltered " + groupPanel.getGuiIsFiltered());
		
		groupPanel.setGuiIsFiltered( 
				filter != null && !filter.isEmpty() 
				);
			
		tableProducts.revalidate();
		activatePacketSelectionHandling(true);
	}
	
	public void reduceToSelected()
	{
		Set<String> selection = getSelectedIDs();
		logging.debug(this, "reduceToSelected: selectedIds  " + selection );
		reduceToSet( selection );
		setSelection(selection);
	}
	
	public void noSelection()
	{
		InstallationStateTableModelFiltered tModel 
			= (InstallationStateTableModelFiltered) tableProducts.getModel();
		
		activatePacketSelectionHandling(false);
		((InstallationStateTableModelFiltered) tModel).setFilterFrom((Set) null);
		tableProducts.revalidate();
		activatePacketSelectionHandling(true);
	}
	
	public void showAll()
	{
		Set<String> selection = getSelectedIDs();
		noSelection();
		setSelection(selection); 
		
	}
	
	
}

