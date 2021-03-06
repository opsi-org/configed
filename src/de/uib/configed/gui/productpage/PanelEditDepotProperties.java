package de.uib.configed.gui.productpage;

/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2014, 2016 uib.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 */

import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.configed.type.*;
import de.uib.opsidatamodel.datachanges.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.event.*;
import javax.swing.table.*;
import org.jdesktop.swingx.*;
import de.uib.utilities.swing.*;
import de.uib.utilities.swing.list.*;
import de.uib.utilities.table.*;
import de.uib.utilities.table.gui.*;
import de.uib.utilities.table.updates.*;
import de.uib.utilities.datapanel.*;
import de.uib.utilities.logging.*;

import de.uib.configed.guidata.*;
import de.uib.configed.gui.helper.*;
import de.uib.opsidatamodel.productstate.*;

public class PanelEditDepotProperties extends DefaultPanelEditProperties 
	implements 
	ListSelectionListener, ActionListener, 
	MouseListener, KeyListener
{
	private javax.swing.JLabel jLabelEditDepotProductProperties;
	private IconButton buttonSetValuesFromPackage;
	
	java.util.List<String> depots;
	private JList<String> listDepots;
	java.util.List<String> listSelectedDepots;
	private JPanel panelDepots;
	private JButton buttonSelectWithEqualProperties;
	private JButton buttonSelectAll;
	JPopupMenu popupDepot = new JPopupMenu();
	
	protected final Map<String,Object> emptyVisualData = new HashMap<String,Object>();
	
	
	public PanelEditDepotProperties( ConfigedMain mainController, 
		de.uib.utilities.datapanel.AbstractEditMapPanel productPropertiesPanel
		)
	{
		super(mainController, productPropertiesPanel);
		initComponents();
	}
	
	protected void initComponents()
	{
		super.initComponents();
		
		panelDepots = new JPanel();
		//panelDepots.setBorder( Globals.createPanelBorder() );
		//panelDepots.setBackground(Globals.backgroundLightGrey);
		//panelDepots.setOpaque(true);
		
		Containership csPanelDepots = new Containership(panelDepots);
		
		depots = new ArrayList<String>();
		listDepots = new JList<String>(); //new String[]{"a","b","c"});
		listDepots.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		listDepots.addListSelectionListener(this);
		listDepots.addMouseListener(this);
		listDepots.addKeyListener(this);
		
		
		
		listDepots.setCellRenderer(new StandardListCellRenderer());
		
		listSelectedDepots = new ArrayList<String>();
		
		
		JScrollPane scrollpaneDepots = new javax.swing.JScrollPane();
		scrollpaneDepots.setViewportView(listDepots);
		
		popupDepot = new JPopupMenu();
		listDepots.setComponentPopupMenu(popupDepot);
		
		buttonSelectWithEqualProperties = new JButton("",
			Globals.createImageIcon("images/equalplus.png", ""));
			
		buttonSelectWithEqualProperties.setToolTipText(
			configed.getResourceValue("ProductInfoPane.buttonSelectAllWithEqualProperties") 
			);
		Globals.formatButtonSmallText( buttonSelectWithEqualProperties );
		buttonSelectWithEqualProperties.addActionListener(this);
		
		buttonSelectAll = new JButton("", 
			Globals.createImageIcon("images/plusplus.png", ""));
		buttonSelectAll.setToolTipText(
			configed.getResourceValue("ProductInfoPane.buttonSelectAll") 
			);
		Globals.formatButtonSmallText( buttonSelectAll );
		buttonSelectAll.addActionListener(this);
		
		
		GroupLayout layoutPanelDepots = new GroupLayout(panelDepots);
		panelDepots.setLayout( layoutPanelDepots );
		
		layoutPanelDepots.setVerticalGroup(
			layoutPanelDepots.createSequentialGroup()
				.addGroup(layoutPanelDepots.createParallelGroup()
					.addComponent(scrollpaneDepots, GroupLayout.Alignment.LEADING, 30, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addComponent(buttonSelectWithEqualProperties,  GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(buttonSelectAll,  GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					//.addGap(10, 20, 20)
				)
			)
		;
		
		layoutPanelDepots.setHorizontalGroup(
			layoutPanelDepots.createSequentialGroup()
				.addComponent(scrollpaneDepots, 50, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(buttonSelectWithEqualProperties,  Globals.squareButtonWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(buttonSelectAll,  Globals.squareButtonWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(10, 20, 50)
			)
		;
		csPanelDepots.doForAllContainedCompis("setBackground", new Object[]{Globals.backgroundWhite});
		buttonSelectAll.setForeground(Globals.blue);
		buttonSelectWithEqualProperties.setForeground(Globals.blue);
		//buttonSelectAll.setBackground(Globals.backLightBlue);
		//buttonSelectWithEqualProperties.setBackground(Globals.backLightBlue);
		
		
		//jLabelProductProperties = new JLabel ( configed.getResourceValue("ProductInfoPane.jLabelProductProperties") );
		//jLabelProductProperties.setFont ( Globals.defaultFontBig );
		
		jLabelEditDepotProductProperties =  new JLabel ( configed.getResourceValue("ProductInfoPane.jLabelEditDepotProductProperties") );
		jLabelEditDepotProductProperties.setFont ( Globals.defaultFont );
		
		
		
		buttonSetValuesFromPackage = new IconButton( 
		configed.getResourceValue("ProductInfoPane.buttonSetValuesFromPackage") ,
			"images/reset_network_defaults.png", 
			"images/reset_network_defaults_over.png",
			" ", 
			true);
		
		buttonSetValuesFromPackage.setPreferredSize(new Dimension(15, 30));
		
		buttonSetValuesFromPackage.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					
					productPropertiesPanel.resetDefaults();
					
					//productPropertiesPanel.setValues(productpropertiesDefaultsMap);
					
					
					/*  // test
					System.out.println(" action performed on buttonSetValuesFromPackage ");
					HashMap testdata = new HashMap();
					testdata.put("test", "3");
					testdata.put("loop", "200");
					testdata.put("addressbook_name", "2x");
					
					productPropertiesPanel.setValues(testdata);
					*/
					
				}
		});
		
		
		JPanel panelTop = new JPanel();
		javax.swing.GroupLayout layoutEditProperties = new javax.swing.GroupLayout(panelTop);
		panelTop.setLayout(layoutEditProperties);
		
		layoutEditProperties.setHorizontalGroup(
			layoutEditProperties.createSequentialGroup()
				//.addGap(hGapSize, hGapSize, hGapSize)
				.addGroup(
					layoutEditProperties.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(layoutEditProperties.createSequentialGroup()
							//.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
							//.addComponent(jLabelProductProperties, minHSize, prefHSize, Short.MAX_VALUE)
							.addGroup(layoutEditProperties.createParallelGroup()
								.addComponent(panelDepots, minHSize, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
								.addGroup(layoutEditProperties.createSequentialGroup()
									.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
									.addComponent(jLabelEditDepotProductProperties, minHSize, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
									.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
									)
								)
							.addGap(hGapSize, hGapSize, hGapSize)
							.addComponent(buttonSetValuesFromPackage,  20, 20, 20)
						)
						//.addComponent(productPropertiesPanel, minHSize, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				)
				.addGap(0, hGapSize, hGapSize)
			)
		;
		
		layoutEditProperties.setVerticalGroup(
			layoutEditProperties.createSequentialGroup()
				.addGap(minGapVSize, vGapSize, vGapSize)
				.addGroup(layoutEditProperties.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
					//.addComponent(jLabelProductProperties, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGroup(layoutEditProperties.createSequentialGroup()
						.addComponent(jLabelEditDepotProductProperties, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
						.addComponent(panelDepots, minHSize, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					)
					.addComponent(buttonSetValuesFromPackage,  GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
				)
				.addGap(minGapVSize, minGapVSize, minGapVSize)
				//.addComponent(productPropertiesPanel, minTableVSize, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
			)
		;
		
		
		/*
		GroupLayout layoutAll = new GroupLayout(this);
		setLayout(layoutAll);
		
		layoutAll.setVerticalGroup(layoutAll.createSequentialGroup()
			.addComponent(panelTop, minHSize, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
			.addComponent(productPropertiesPanel, minHSize, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
			)
		;
		
		layoutAll.setHorizontalGroup(layoutAll.createParallelGroup()
			.addComponent(panelTop, minTableVSize, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
			.addComponent(productPropertiesPanel, minTableVSize, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
			)
		;
		*/
		
		
		JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitter.setResizeWeight(0.3);
		splitter.setTopComponent(panelTop);
		splitter.setBottomComponent(productPropertiesPanel);
		
		add(splitter);
		
		
		GroupLayout layoutAll = new GroupLayout(this);
		setLayout(layoutAll);
		
		layoutAll.setVerticalGroup(layoutAll.createSequentialGroup()
			.addComponent(splitter, minHSize, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
			)
		;
		
		layoutAll.setHorizontalGroup(layoutAll.createParallelGroup()
			.addComponent(splitter, minTableVSize, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
			)
		;
		
		
	}
	
	public void clearDepotListData()
	{
		setDepotListData(new ArrayList<String>(), "", "");
	}
	
	public void setDepotListData(java.util.List<String> depots, String selectedDepot, String productEdited)
	{
		
		logging.debug(this, "setDepotListData");
		/*
		depots, selected, product, selectedDepots " 
			+ depots +
			", " + selectedDepot + ", " + productEdited
			+ ", " + listSelectedDepots);
		*/
		
		this.depots = depots;
		this.productEdited = productEdited;
		listDepots.setListData(new Vector(depots));
		//listDepots.setModel(new DefaultComboBoxModel(new Vector(depots)));
		//listDepots.setSelectedValue(selectedDepot, true);
		
		resetSelectedDepots(depots);
			
		//listDepots.setSelectionInterval(0, listDepots.getModel().getSize()-1);
		
	}
	
	//Interface ListSelectionListener
	public void valueChanged(ListSelectionEvent e)
	{
		if (e.getValueIsAdjusting())
			return;
		
		
		/*
		if (listDepots.hasFocus())
			listSelectedDepots = listDepots.getSelectedValuesList();
		*/
		//logging.info(this," ListSelectionListener selected " +  listDepots.getSelectedValuesList() );
		//logging.info(this," ListSelectionListener productEdited " + productEdited); 
		
		
		Map<String, Object> visualData  = mergeProperties(
			mainController.getPersistenceController().getDepot2product2properties(),
			listDepots.getSelectedValuesList(), 
			productEdited
		);

		if (visualData == null) //no properties
		{
			//produce empty map
			visualData = emptyVisualData; 
		}
		
		/*
		if (listDepots.getSelectedValuesList().size() == 0)
		{
			visualDats = emptyVisualDt
		*/
		
		if (listDepots.getSelectedValuesList().size() != 0)
		{
			productPropertiesPanel.setEditableMap(
				
				visualData,
				
				mainController.getPersistenceController()
					.getProductPropertyOptionsMap(
						listDepots.getSelectedValuesList().get(0), 
						productEdited)
				);
			
			//list of all property maps
			java.util.List<ConfigName2ConfigValue> storableProperties = new ArrayList<ConfigName2ConfigValue>();
			for (String depot : listDepots.getSelectedValuesList())
			{
				Map<String, ConfigName2ConfigValue> product2properties =
					mainController.getPersistenceController().getDepot2product2properties().get(depot);
					
				if (product2properties == null)
				{
					logging.info(this, " product2properties null for depot " + depot);
				}
				else if (product2properties .get(productEdited) == null)
				{
					logging.info(this, " product2properties null for depot, product " + depot + ", " + productEdited);
				}
				else	
					storableProperties.add(product2properties .get(productEdited));
					
				;
				/*
				logging.info(this, "storeData for depot " + depot + ": " + 
					mainController.getPersistenceController()
					.getDepot2product2properties().get(depot).get(productEdited)
					)
				;
				*/
				
			}
			productPropertiesPanel.setStoreData(storableProperties);
			
			//updateCollection (the real updates)
			ProductpropertiesUpdateCollection depotProductpropertiesUpdateCollection 
			= new ProductpropertiesUpdateCollection(
					mainController, 
					mainController.getPersistenceController(),
					listDepots.getSelectedValuesList(),
					productEdited)
			;
			productPropertiesPanel.setUpdateCollection( 
					depotProductpropertiesUpdateCollection
					)
			;
			mainController.addToGlobalUpdateCollection(
					depotProductpropertiesUpdateCollection
					)
			;
			
		}
		
		//logging.info(this,"visualdata set " +  visualData );
	}
	
	
	private Map<String, Object> mergeProperties( 
		Map<String, Map<String, ConfigName2ConfigValue>> depot2product2properties,
		java.util.List<String> depots,
		String productId)
	{
		
		
		Map<String, Object> result = new HashMap<String, Object>();
		
		if (depots == null || depots.size() == 0)
			return result;
		
		Map<String, ConfigName2ConfigValue> propertiesDepot0 = depot2product2properties.get(depots.get(0));
		
		//logging.info(this, "mergeProperties depot0 " + depots.get(0) );
		//logging.info(this, "mergeProperties, properties for key " + productId + " : " + propertiesDepot0.get(productId));
		
		
		
		if (depots.size() == 1)
		{
			if (propertiesDepot0 == null ||  propertiesDepot0.get(productId) == null)
			{
				//ready
			}
			else 
			{
				result =  propertiesDepot0.get(productId);
			}
		}
		else
		{
			int n = 0;
			
			while ( n < depots.size()
					&&
					(
					depot2product2properties.get(depots.get(n)) == null ||  
					depot2product2properties.get(depots.get(n)).get(productId) == null)
					)
			{
				n++;
			}
			
			if ( n == depots.size())
			{
				//ready
			}
			else
			{
				//create start mergers
				ConfigName2ConfigValue properties = depot2product2properties.get(depots.get(n)).get(productId);
					
				for (String key : properties.keySet())
				{
					java.util.List value = (java.util.List) properties.get(key) ;
					result.put(key, new ListMerger(value));
				}
				
				//merge the other depots
				for (int i = 1; i< depots.size(); i++)
				{
					properties = depot2product2properties.get(depots.get(i)).get(productId);
					
					//logging.info(this, "mergeProperties depot  " + depots.get(i) );
					//logging.info(this, "mergeProperties, properties for key " + productId + " : " + properties);
					
					if (properties == null)
					{
						logging.info(this, "mergeProperties, product on depot ha not properties " + productId + " on " + depots.get(i));
						continue;
					}
					
					for (String key : properties.keySet())
					{
						java.util.List value = (java.util.List) properties.get(key) ;
						if (result.get(key) == null)
						// we need a new property. it is not common
						{
							ListMerger merger = new ListMerger ( value );
							//logging.debug(this, " new property, merger " + merger);
							merger.setHavingNoCommonValue();
							result.put (key, merger);
						}
						else
						{
							ListMerger merger = (ListMerger) result.get(key);
							result.put(key, merger.merge(value));
						}
						
						
					}
				}
			}
		}
		
		return result;
		
	}
	
	
	private void saveSelectedDepots()
	{
		logging.debug(this, "saveSelectedDepots");
		listSelectedDepots = listDepots.getSelectedValuesList();
	}
	
	private void resetSelectedDepots(java.util.List<String> baseList)
	{
		logging.debug(this, "resetSelectedDepots");
	
		listDepots.setValueIsAdjusting(true);
		
		if (listSelectedDepots == null || listSelectedDepots.size() == 0)
		{
			//mark all
			listDepots.setSelectionInterval(0, listDepots.getModel().getSize()-1);
		}
		else
		{
			int[] selection = new int[listSelectedDepots.size()];
			int n = 0;
			for (int j = 0; j < baseList.size(); j++) 
			{
				String depot = baseList.get(j);
				if (listSelectedDepots.indexOf(depot) > -1)
				{
					selection[n] = j;
					n++;
				}
			}
			logging.debug(this, "resetSelectedDepots, n, selection is " + n + ", -- " + logging.getStrings(selection));
			
			for (int i = 0; i < n; i++)
			{
				listDepots.getSelectionModel().addSelectionInterval(selection[i], selection[i]);
			}
		}
		listDepots.setValueIsAdjusting(false);
	
	}
	
	//KeyListener
	public void keyPressed(KeyEvent e)
	{
		//logging.info(this, "keyPressed " + e);
		if (e.getSource() == listDepots)
		{
			saveSelectedDepots();
		}
	}
	public void  keyReleased(KeyEvent e){} 
	public void  keyTyped(KeyEvent e){}
	
	//MouseListener
	public void mouseClicked(MouseEvent e)
	{
		logging.info(this, "mouseClicked " + e);
		if (e.getSource() == listDepots)
		{
			saveSelectedDepots();
		}
			
	}
	public void mouseEntered(MouseEvent e)
	{
	}
	public void  mouseExited(MouseEvent e)
	{
	}
	public void  mousePressed(MouseEvent e)
	{
	}
	public void  mouseReleased(MouseEvent e)
	{
	}

	//ActionListener
	public void actionPerformed( ActionEvent e)
	{
		logging.debug(this, "actionPerformed " + e);
		
		if (e.getSource() == buttonSelectWithEqualProperties)
		{
			selectDepotsWithEqualProperties();
			saveSelectedDepots();
		}	
		else if (e.getSource() == buttonSelectAll)
		{
			listDepots.setSelectionInterval(0, listDepots.getModel().getSize()-1);
			saveSelectedDepots();
		}
		
		
	}	
	
	private void selectDepotsWithEqualProperties()
	{
		String selectedDepot0 = (String) listDepots.getSelectedValue();
		
		if (selectedDepot0 == null || selectedDepot0.equals(""))
			return;
		
		ConfigName2ConfigValue properties0 = mainController.getPersistenceController()
				.getDefaultProductProperties(selectedDepot0).get(productEdited);
		
				
		//if (properties0 == null)
		//	return;
				
		int startDepotIndex = listDepots.getSelectedIndex();
		listDepots.setSelectionInterval(startDepotIndex, startDepotIndex);
		
		for (int i = 0; i < listDepots.getModel().getSize(); i++)
		{
			String compareDepot = listDepots.getModel().getElementAt(i);
			
			if (compareDepot.equals(selectedDepot0))
				continue;
			
			ConfigName2ConfigValue compareProperties = mainController.getPersistenceController()
				.getDefaultProductProperties(compareDepot).get(productEdited); 
			
			//logging.info(this, "compare " + properties0 + "          to \n "  + compareProperties);
			
			if ((properties0 == null && compareProperties == null) ||properties0.equals(compareProperties))
			{ 	
				//logging.info(this, "equal");
				listDepots.addSelectionInterval(i, i);
			}
		}
		
	}
		
		
}
