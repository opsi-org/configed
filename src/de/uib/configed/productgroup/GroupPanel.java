package de.uib.configed.productgroup;

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
 
 
 /* test
 
 * Wechsel zu einer anderen, vorhandenen Produktgruppe bewirkt:
 - Änderung des Inhalts des "Speichern unter"-Feldes zu dem Namen der Gruppe
 - Aktivieren des Delete-Buttons
 - Disablen des Speichern- und Cancel-Buttons
 
 
 
 */
 

import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.configed.gui.productpage.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.table.*;

import de.uib.utilities.swing.*;
import de.uib.utilities.table.gui.*;
import de.uib.utilities.logging.*;

import de.uib.configed.guidata.*;

public class GroupPanel extends JPanel
	implements 
	ListSelectionListener		// for associated table  
	, ActionListener				// for internal buttons
	, ItemListener 				// for combos
{
	JComboBoxToolTip groupsCombo;
	class JComboBoxToolTipX extends JComboBoxToolTip 
		{
			public void fireActionEvent()
			//make public
			{
				//logging.debug(this, "fireActionEvent()");
				super.fireActionEvent();
			}
		}
		
	protected de.uib.utilities.table.gui.SearchPane searchPane;
	protected JTable tableProducts;
	
		
	
	//JComboBoxToolTipX saveNameEditor;

	JTextField saveNameEditor;
	
	
	protected de.uib.configed.gui.IconButton buttonCommit;
	protected de.uib.configed.gui.IconButton buttonCancel;
	protected de.uib.configed.gui.IconButton buttonFilter;
	protected de.uib.configed.gui.IconButton buttonEditDialog;
	protected de.uib.configed.gui.IconButton buttonDelete;
	
	protected JLabel labelSave;
	
	
	final static String NO_GROUP_ID =configed.getResourceValue("GroupPanel.NO_GROUP_ID");
	final static String SAVE_GROUP_ID = configed.getResourceValue("GroupPanel.SAVE_GROUP_ID");
	final static String NO_GROUP_DESCRIPTION = configed.getResourceValue("GroupPanel.NO_GROUP_DESCRIPTION");
	final static String EMPTIED_GROUPID = "";
	final static String TEXT_SAVE = configed.getResourceValue("GroupPanel.TEXT_SAVE");
	final static String TEXT_DELETE = configed.getResourceValue("GroupPanel.TEXT_DELETE");
	
	
	protected Map<String,  Map<String, String>> theData;
	
	
	PanelGroupedProductSettings associate;
	JPanel panelEdit;
	protected Set<String> selectedIDs;
	
	protected DefaultComboBoxModel comboModel;
	
	protected LinkedHashMap<String, String> namesAndDescriptions;
	protected LinkedHashMap<String, String> namesAndDescriptionsSave;
	protected MapOfProductGroups productGroupMembers;
	protected int editIndex;
	protected String showKey;
	protected String editedKey;
	protected JTextField groupsEditField;
	protected JTextField descriptionField;
	protected boolean dataChanged = false;
	protected boolean groupEditing = false;
	protected boolean deleted = false;
	
	private final int minFieldWidth = 30;
	private final int maxComboWidth = 200;
	
	abstract class MyDocumentListener implements DocumentListener
	{
		
		protected boolean enabled = true;
		abstract public void doAction();
		
		public void changedUpdate(DocumentEvent e)
		{
			if (enabled) doAction();
		}
		public void insertUpdate(DocumentEvent e)
		{
			if (enabled) doAction();
		}
		public void removeUpdate(DocumentEvent e)
		{
			if (enabled) doAction();
		}
		
		public void setEnabled(boolean b)
		{
			enabled = b;
		}
	}
	
	MyDocumentListener descriptionFieldListener;
	MyDocumentListener groupsEditFieldListener;
	
	protected ConfigedMain mainController;
	
	public GroupPanel(PanelGroupedProductSettings associate, ConfigedMain mainController, JTable table)
	{
		this.associate = associate;
		this.mainController  = mainController;
		this.tableProducts = table;
		
		initData();
		
		initComponents();
	}
	
	public void setSearchFields(java.util.List<String> fieldList)
	{
		searchPane.setSearchFields(fieldList);
	}
	
	public void markAllSearchResults()
	{
		searchPane.markAll();
	}
	
	public void setGuiIsFiltered(boolean b)
	{
		logging.debug(this, "setGuiIsFiltered " + b);
		buttonFilter.setActivated(!b);
	}
	
	public boolean getGuiIsFiltered()
	{
		return !buttonFilter.isActivated();
	}
	
	
	protected void enterExistingGroup()
	{
		logging.debug(this, "enterExistingGroup");
		
		saveNameEditorShallFollow();
		

		
		if (getGuiIsFiltered() )
		{
			setGuiIsFiltered(false);
			associate.noSelection();
		}	
		
		setMembers();
		
		setDataChanged(false);
		setDeleted(false);
		
		isDeleteLegal();
	}
	
	protected void enterEditGroup()
	{
		//logging.debug(this, "enterEditGroup , ");
		descriptionFieldListener.setEnabled(false);
		
		//String currentKey = (String) saveNameEditor.getSelectedItem();
		String currentKey =  groupsEditField.getText(); //saveNameEditor.getText();
		
		//logging.debug(this, "enterEditGroup , currentKey " + currentKey); 
		
		if (namesAndDescriptionsSave.get(currentKey) != null)
		{
			descriptionField.setText(namesAndDescriptionsSave.get(currentKey));
		}
		descriptionFieldListener.setEnabled(true);
		
		if (
			(!currentKey.equals(SAVE_GROUP_ID)
				&& !currentKey.equals((String)groupsCombo.getSelectedItem())
			)
			)
		{
			setDataChanged(true);
		}
		
		isSaveLegal();
		isDeleteLegal();
	}
	
	
	private boolean membersChanged()
	{
		if (productGroupMembers == null || saveNameEditor == null)
			return false;
		
		selectedIDs = associate.getSelectedIDs();
		
		
		//String currentKey = (String) saveNameEditor.getSelectedItem();
		String currentKey = saveNameEditor.getText();
		
		if (currentKey == null || currentKey.equals(""))
			return false;
		
		boolean result = false;
		
		//logging.debug(this, "membersChanged, currentKey " + currentKey);
	
		if (namesAndDescriptions.get(currentKey) != null)
		//case we have an old key
		{
			if (productGroupMembers.get(currentKey) == null
				|| 
				((Set)productGroupMembers.get(currentKey)).size() == 0
				)
			//there were no products assigned
			{
					if (selectedIDs.size() > 0)
					//but now there are some
						result = true;
			}
			else
			//there were products assigned
			{
				if (
				!productGroupMembers.get(currentKey).equals(
					selectedIDs
					)
				)				//but they are different
				result = true;
			}
		}
		else
		//we have no old key
		{
			if (selectedIDs.size() > 0)
				result = true;
		}
		
		//logging.debug(this, "membersChanged  " + result);
		
		return result;
	}
	
	
	private void setItemWithoutListener(String key)
	{
		groupsCombo.removeItemListener(this);
		groupsCombo.setSelectedItem(key);
		groupsCombo.addItemListener(this);
		
		isDeleteLegal();
	}
	
	
	public void findGroup(Set<String> set)
	{
		Iterator iterNames = namesAndDescriptions.keySet().iterator();
		boolean theSetFound = false;
		
		if (set !=  null)
		{
			TreeSetBuddy checkSet = new TreeSetBuddy(set);
			
			while (!theSetFound && iterNames.hasNext())
			{
				
				String name = (String) iterNames.next();
				//logging.debug(this, "findGroup():  name " + name); 
				//logging.debug(this, "findGroup():  productGroupMembers.get(name) " +  productGroupMembers.get(name));
				//logging.debug(this, "findGroup():  compare to  " +  set);
					
				
				if  (
						productGroupMembers.get(name) != null 
						&&
						productGroupMembers.get(name).equals(checkSet)
					)
				{
					//avoid selection events in groupsCombo
					setItemWithoutListener(name);
					theSetFound = true;
				}
			}
		}
		if (!theSetFound)
			setItemWithoutListener(NO_GROUP_ID);
	}
	
	private void updateAssociations()
	{
		//editedKey = namesAndDescriptions.getKeyAt(groupsCombo.getSelectedIndex());
		if  (membersChanged())
		{
			setDataChanged(true);
		}
		
		
		
		if (namesAndDescriptions == null)
			return;
		//logging.debug(this, "updateAssociations: namesAndDescriptions.keySet() " + namesAndDescriptions.keySet() ); 
		
	
		isSaveLegal();
		//isDeleteLegal();  not needed since a change in associations does not concern save name 
	
		findGroup(associate.getSelectedIDs());
		
	}
	
	
	private boolean isDescriptionChanged()
	{
		boolean result = false;
		//String currentKey = (String) saveNameEditor.getSelectedItem();
		String currentKey = saveNameEditor.getText();
		
		if (namesAndDescriptions.get(currentKey) == null) //current key did not exist
			result = true;
		
		else
		{
			String oldDescription = namesAndDescriptions.get(currentKey);
			if (!oldDescription.equals(descriptionField.getText()))
				result = true;
		}
		
		//logging.debug(this, "isDescriptionChanged "  + result);
			
		return result;
	}
	
	protected void updateDescription()
	{
		if (isDescriptionChanged())
		{
			setDataChanged(true);
		}
	}
	
	
	protected void updateKey()
	{
		
	}
	
	protected void initData()
	{
		searchPane = new SearchPane(tableProducts, true);
		//searchPane.setSearchFields(new Integer[]{0, 1, 2, 3, 4});
		
		groupsCombo = new JComboBoxToolTip();
		groupsCombo.setEditable(false);
		groupsCombo.setMaximumRowCount(30);
		
		//saveNameEditor = new JComboBoxToolTipX();
		saveNameEditor = new JTextField("");
					
		saveNameEditor.setEditable(true);
		saveNameEditor.setToolTipText( 
			de.uib.configed.configed.getResourceValue("GroupPanel.GroupnameTooltip")
			);
		//setGroupsData(null, null);
		//Set<String> oldSet = groupPanel.getLastSelectedIDs();
		setMembers();
		setGroupEditing(false);
	}
	
	
	protected void initComponents()
	{
		
		buttonCommit = new de.uib.configed.gui.IconButton( 
			de.uib.configed.configed.getResourceValue("GroupPanel.SaveButtonTooltip") , //desc 
			"images/apply.png", //inactive
			"images/apply_over.png",  //over
			"images/apply_disabled.png", //active
			true);	//setEnabled
		buttonCommit.addActionListener(this);
		buttonCommit.setPreferredSize(de.uib.utilities.Globals.newSmallButton);
		
		buttonCancel = new de.uib.configed.gui.IconButton( 
			de.uib.configed.configed.getResourceValue("GroupPanel.CancelButtonTooltip") , 
			"images/cancel.png", 
			"images/cancel_over.png", 
			"images/cancel_disabled.png");
		buttonCancel.addActionListener(this);
		buttonCancel.setPreferredSize(de.uib.utilities.Globals.newSmallButton);
		
		buttonDelete = new de.uib.configed.gui.IconButton( 
			de.uib.configed.configed.getResourceValue("GroupPanel.DeleteButtonTooltip"), 
			"images/edit-delete.png", 
			"images/edit-delete_over.png", 
			"images/edit-delete_disabled.png");
		buttonDelete.addActionListener(this);
		buttonDelete.setPreferredSize(de.uib.utilities.Globals.newSmallButton);
		
		buttonFilter = new de.uib.configed.gui.IconButton( 
			de.uib.configed.configed.getResourceValue("GroupPanel.FilterButtonTooltip") ,
			"images/view-filter_disabled-32.png", 
			"images/view-filter_over-32.png",
			" ",
			true);
		buttonFilter.setToolTips(
			de.uib.configed.configed.getResourceValue("GroupPanel.FilterButtonTooltipActive"),
			de.uib.configed.configed.getResourceValue("GroupPanel.FilterButtonTooltipInactive")
		); 
		buttonFilter.addActionListener(this);
		buttonFilter.setPreferredSize(de.uib.utilities.Globals.newSmallButton);
		
		
		buttonEditDialog = new de.uib.configed.gui.IconButton( 
			de.uib.configed.configed.getResourceValue("GroupPanel.EditButtonTooltip") , 
			"images/packagegroup_save.png", 
			"images/packagegroup_save_over.png", 
			"images/packagegroup_save_disabled.png");
		//buttonEditDialog.setPreferredSize(Globals.buttonDimension);
		buttonEditDialog.setToolTips(
			de.uib.configed.configed.getResourceValue("GroupPanel.EditButtonTooltipInactive"),
			de.uib.configed.configed.getResourceValue("GroupPanel.EditButtonTooltipActive")
		); 
		buttonEditDialog.addActionListener(this);
		buttonEditDialog.setPreferredSize(de.uib.utilities.Globals.newSmallButton);
		
		JLabel labelSelectedGroup = new JLabel( configed.getResourceValue("GroupPanel.selectgroup.label") );
		
		labelSelectedGroup.setFont(de.uib.utilities.Globals.defaultFont);
				
		//groupsEditField = (JTextField) (saveNameEditor.getEditor().getEditorComponent());
		groupsEditField = saveNameEditor;
		groupsEditField.getCaret().setBlinkRate(0);
		groupsEditField.setBackground(Globals.backgroundLightGrey);
		
		groupsEditFieldListener = 
			new MyDocumentListener(){
				@Override
				public void doAction()
				{
					//logging.debug(this, "comboBox item edited, enabled listener " + enabled);
					//updateKey();
					enterEditGroup();
				}
			};
		
		groupsEditField.getDocument().addDocumentListener(groupsEditFieldListener);
				
		groupsCombo.addItemListener(this);
		
		
		groupsEditField.addFocusListener(new FocusAdapter(){
				public void focusGained(FocusEvent e)
				{
					logging.debug(this, "focus gained on groupsEditField, groupediting");
					setGroupEditing(true);
				}
			}
		);
		
		
		groupsCombo.setPreferredSize(Globals.buttonDimension);
		saveNameEditor.setPreferredSize(Globals.buttonDimension);
		groupsEditField.setBackground(Globals.backgroundLightGrey);
		//saveNameEditor.addItemListener(this);
		
		labelSave = new JLabel();
		labelSave.setText(TEXT_SAVE);
		labelSave.setFont(Globals.defaultFontStandardBold);
		
		descriptionField = new JTextField("");
		descriptionField.setPreferredSize(Globals.buttonDimension);
		descriptionField.setFont(Globals.defaultFont);
		descriptionField.setBackground(Globals.backgroundLightGrey);
		descriptionField.getCaret().setBlinkRate(0);
	
		descriptionFieldListener = 
			new MyDocumentListener(){
				@Override
				public void doAction()
				{
					logging.debug(this, "description changed, setgroupediting");
					updateDescription();
				}
			};
		descriptionField.getDocument().addDocumentListener(descriptionFieldListener);
		
		
		
		panelEdit = new JPanel();
		
		panelEdit.setBackground(Globals.backgroundWhite);
			
		GroupLayout layoutPanelEdit =  new GroupLayout(panelEdit);
		panelEdit.setLayout(layoutPanelEdit);
		
		layoutPanelEdit.setVerticalGroup(
			layoutPanelEdit.createSequentialGroup()
				.addGap(Globals.vGapSize/2, Globals.vGapSize/2, Globals.vGapSize/2)
				.addGroup(layoutPanelEdit.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(labelSave, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
				.addGap(1, 1, 2)
				.addGroup(layoutPanelEdit.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(saveNameEditor, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(descriptionField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(buttonDelete, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(buttonCommit, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(buttonCancel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
				.addGap(Globals.vGapSize/2, Globals.vGapSize/2, Globals.vGapSize/2)
			)
		;
		
		
		layoutPanelEdit.setHorizontalGroup(
			layoutPanelEdit .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layoutPanelEdit.createSequentialGroup()
					.addGap(Globals.gapSize, Globals.gapSize, Globals.gapSize)
					.addComponent(labelSave, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
				.addGroup(layoutPanelEdit.createSequentialGroup()
					.addGap(Globals.gapSize, Globals.gapSize, Globals.gapSize)
					.addComponent(saveNameEditor, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, maxComboWidth)
					.addGap(Globals.minGapSize/2, Globals.minGapSize/2, Globals.gapSize/2)
					.addComponent(descriptionField, minFieldWidth, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addGap(Globals.minGapSize, Globals.minGapSize, Globals.gapSize)
					.addComponent(buttonDelete, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(Globals.minGapSize/2, Globals.minGapSize/2, Globals.gapSize/2)
					.addComponent(buttonCommit, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(Globals.minGapSize/2, Globals.minGapSize/2, Globals.gapSize/2)
					.addComponent(buttonCancel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(Globals.gapSize, Globals.gapSize, Globals.gapSize)
				)
			)
		;
		
		setGroupEditing(false);
		
		/*
		Containership csPanelEdit = new Containership(panelEdit);
		csPanelEdit.doForAllContainedCompis("setOpaque", new Object[]{Boolean.TRUE});
		csPanelEdit.doForAllContainedCompis("setBackground", new Object[]{Globals.backgroundWhite});
		
		*/
		
		
		panelEdit.setBorder( Globals.createPanelBorder() );
		
	
		GroupLayout layoutMain = new GroupLayout(this);
		this.setLayout(layoutMain);
		
		layoutMain.setVerticalGroup(
			layoutMain.createSequentialGroup()
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
				.addGroup(layoutMain.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(searchPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
				.addGroup(layoutMain.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(labelSelectedGroup, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(groupsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(buttonFilter, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(buttonEditDialog, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
				.addGap(Globals.vGapSize/2, Globals.vGapSize/2, Globals.vGapSize/2)
				.addComponent(panelEdit,   GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
		;
		
		layoutMain.setHorizontalGroup(
			layoutMain .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layoutMain.createSequentialGroup()
					//.addGap(Globals.gapSize, Globals.gapSize, Globals.gapSize)
					.addComponent(searchPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					)
				.addGroup(layoutMain.createSequentialGroup()
					.addGap(Globals.gapSize, Globals.gapSize, Globals.gapSize)
					.addComponent(buttonFilter, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(Globals.gapSize, Globals.gapSize, Short.MAX_VALUE)
					.addComponent(labelSelectedGroup, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(Globals.minGapSize, Globals.minGapSize, Globals.gapSize)
					.addComponent(groupsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, maxComboWidth)
					.addGap(Globals.minGapSize, Globals.minGapSize, Globals.gapSize)
					.addComponent(buttonEditDialog, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(Globals.gapSize, Globals.gapSize, Globals.gapSize)
				)
				.addComponent(panelEdit,   80, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
			)
		;
		
	}
	
	
	protected boolean save()
	{
		boolean result = false;
		//logging.debug(this, "save: !");
		
		if (deleted)
		{
			String removeGroupID = groupsEditField.getText(); //(String) groupsCombo.getSelectedItem();
			//logging.debug(this, "save: delete group " + removeGroupID);
			theData.remove(removeGroupID);
			
			if (mainController.deleteGroup(removeGroupID))
			{
				result = true;
				setInternalGroupsData();
			}
			
		}
		else
		{
			String newGroupID = groupsEditField.getText();//(String) saveNameEditor.getSelectedItem();
			String newDescription = descriptionField.getText();
			Set selectedProducts = associate.getSelectedIDs();
		
			logging.debug(this, "save: set groupname, description, assigned_products " 
				+ newGroupID + ", " 
				+ newDescription + ", "
				+ selectedProducts
			);
			
			
				//logging.debug(this, "save: newGroupID " + newGroupID);
				
				if (mainController.setProductGroup(newGroupID, newDescription, selectedProducts))
				{
					result = true;
					
					//modify internal model
					HashMap group = new HashMap<String, String>();
					group.put("description", newDescription);
					theData.put(newGroupID, group);
					
					productGroupMembers.put(newGroupID, new TreeSetBuddy(selectedProducts));
					
					setInternalGroupsData();
				}
			
		}
		
		return result;
	}
	
	protected void setMembers()
	{
		//logging.debug(this, "setMembers, productGroupMembers " + productGroupMembers); 
		
		if (productGroupMembers == null
			|| groupsCombo == null
		)
			//|| productGroupMembers.get((String) groupsCombo.getSelectedItem()) == null)
		{
			associate.clearSelection();
			return;
		}
		
	
		logging.debug(this, "group members " 
			+ productGroupMembers.get((String) groupsCombo.getSelectedItem()));
	
		
		associate.setSelection(
			(Set)
			productGroupMembers.get((String) groupsCombo.getSelectedItem())
		);
	}
	
	
	protected void setInternalGroupsData()
	{
		//logging.debug(this, "setInternalGroupsData: data " + theData);
		
		namesAndDescriptionsSave = new LinkedHashMap();
		namesAndDescriptionsSave.put(SAVE_GROUP_ID, NO_GROUP_DESCRIPTION);
		for (String id : new TreeSet<String>(theData.keySet()))
		{
			//logging.debug(this, "id " + id + ":  " + theData.get(id).get("description"));  
			namesAndDescriptionsSave.put(id, theData.get(id).get("description"));
		}
		//saveNameEditor.setValues(namesAndDescriptionsSave);
		
		
		namesAndDescriptions = new LinkedHashMap();
		namesAndDescriptions.put(NO_GROUP_ID, "");
		for (String id : new TreeSet<String>(theData.keySet()))
		{
			//logging.debug(this, "id " + id + ":  " + theData.get(id).get("description"));  
			namesAndDescriptions.put(id, theData.get(id).get("description"));
		}
		groupsCombo.setValues(namesAndDescriptions);
		comboModel  = (DefaultComboBoxModel) groupsCombo.getModel();
		
		
		
		//for reentry
		clearChanges();
		
	}
		
	
	public void setGroupsData(
		final Map<String, Map<String, String>> data,
		final Map<String, Set<String>> productGroupMembers)
	{
		logging.debug(this, "setGroupsData " + data);
		setGroupEditing(false);
		
		this.productGroupMembers = new MapOfProductGroups(productGroupMembers);
		
		
		if (data != null)
			theData = data;
		else 
			theData = new HashMap();
		
		setInternalGroupsData();
		
		//buttonFilter.setActivated(true);
		setGuiIsFiltered(false);
	}
	
	private void setGroupEditing(boolean b)
	{
		//logging.debug(this, "setGroupEditing " + b);
		groupEditing = b;
		if (panelEdit != null)
		{
			panelEdit.setVisible(b);
			buttonEditDialog.setActivated(!b);
		}
			
	}
	
	
	
	// ActionListener interface
	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		if (e.getSource() == buttonCommit)
		{
			commit();
		}
		
		else if (e.getSource() == buttonCancel)
		{
			//System.out.println (" -------- buttonCancel " + e);
			cancel();
		}
		
		else if (e.getSource() == buttonDelete)
		{
			setDeleted(true);
			setDataChanged(true);
		}
		
		else if (e.getSource() == buttonEditDialog)
		{
			setGroupEditing(!panelEdit.isVisible());
		}
		
		else if (e.getSource() == buttonFilter)
		{
			if ( !getGuiIsFiltered())
				//buttonFilter.isActivated())
			{
				logging.debug(this, "associate.reduceToSelected()");
				associate.reduceToSelected();
				setGuiIsFiltered(true);
				buttonFilter.setNewImage(
					"images/view-filter_over-32.png",
					"images/view-filter_disabled-32.png");
			}
			else
			{
				setGuiIsFiltered(false);
				//buttonFilter.setActivated(true);
				logging.debug(this, "associate.showAll()");
				associate.showAll();
				buttonFilter.setNewImage(
					"images/view-filter_disabled-32.png",
					"images/view-filter_over-32.png");
			}
		}
	}
	
	//ListSelectionListener 
	public void valueChanged(ListSelectionEvent e)
	{
		//logging.debug(this, "-----------------ListSelectionListener valueChanged, source " + e.getSource());
		//Ignore extra messages.
		if (e.getValueIsAdjusting()) return;
		
		//assumed to be list selection model of product table
			updateAssociations();
	}
	
	//ItemListener
	public void itemStateChanged(ItemEvent e)
	{
		//logging.debug(this, "itemStateChanged ");
		if  (e.getStateChange() == ItemEvent.SELECTED)
		{
			if  (e.getSource() == groupsCombo)
				enterExistingGroup();
			
			else if  (e.getSource() == saveNameEditor)
				enterEditGroup();
				 
		}
	}
	
	
	private void saveNameEditorShallFollow()
	{
		int comboIndex = groupsCombo.getSelectedIndex();
		//logging.debug(this, "saveNameEditorShallFollow(): comboIndex " + comboIndex);
		/*
		if (comboIndex !=  saveNameEditor.getSelectedIndex())
		//to avoid loops
		{
			saveNameEditor.setSelectedIndex(comboIndex);
		}
		*/
		if (comboIndex == 0 || comboIndex == -1)
			saveNameEditor.setText(SAVE_GROUP_ID);
		else
			saveNameEditor.setText("" + groupsCombo.getSelectedItem());
	}
		
	
	//handling data changes
	private void clearChanges()
	{
		//reset internal components 
		if (saveNameEditor != null)
		{
			//saveNameEditor.setValues(namesAndDescriptionsSave);
			
			saveNameEditorShallFollow();
		}
		
		setDataChanged(false);
		setDeleted(false);
		if (buttonDelete != null)
			buttonDelete.setEnabled(false);
		
		setMembers();
		
	}
	
	public void setDataChanged(boolean b)
	{
		//logging.debug(this, "setDataChanged " + b);
		dataChanged = b;
		if (buttonCommit != null) buttonCommit.setEnabled(b);
		if (buttonCancel != null) buttonCancel.setEnabled(b);
	}
	
	
	protected boolean isSaveLegal()
	{
		String proposedName = groupsEditField.getText();
		
		//logging.debug(this, "isSaveLegal: proposedName >" + proposedName + "<");

		boolean result = true;
		
		if (proposedName == null)
			result = false;
		
		if (result)
		{
			boolean forbidden = 
			proposedName.equals(SAVE_GROUP_ID)
			||
			proposedName.equals("")
			//||
			//proposedName.indexOf(' ') >= 0
			;
	
			result = !forbidden;
		}		
		
		buttonCommit.setEnabled(result);
		
		return result;
	}
	
	protected boolean isDeleteLegal()
	{
		boolean result = false;
		
		if (groupsCombo != null)
			result = (groupsCombo.getSelectedIndex() > 0);
		
		//result = result && groupsEditField.getText().equals((String)groupsCombo.getSelectedItem()); 
		
		buttonDelete.setEnabled(result);
		
		return result;
	}
	
	
	protected void setDeleted(boolean b)
	{
		if (saveNameEditor != null && descriptionField != null)
		{
			saveNameEditor.setEnabled(!b);
			saveNameEditor.setEditable(!b);
			descriptionField.setEnabled(!b);
			descriptionField.setEditable(!b);
			buttonDelete.setEnabled(!b);
			
			if (b)
				labelSave.setText(TEXT_DELETE );
			else
				labelSave.setText(TEXT_SAVE);
			
			deleted = b;
		
		}
	}
		
	public void commit()
	{
		logging.debug(this, "commit");
		String newGroupID = groupsEditField.getText();//(String) saveNameEditor.getSelectedItem();
		if (save())
		{
			clearChanges();
			groupsCombo.setSelectedItem(newGroupID);
			enterExistingGroup();
		}
		
	}
	
	public void cancel()
	{
		clearChanges();
	}
				
	
}
	
	
