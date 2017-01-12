/*
 * FEditList.java
 * 
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2008-2015 uib.de
 *
 * This program is free software; you may redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License, version AGPLv3, as published by the Free Software Foundation
 *
 *
 * @author roeder
 */


package de.uib.utilities.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import de.uib.utilities.*;
import de.uib.utilities.swing.list.*;
import de.uib.utilities.logging.*;
import java.util.*;
import de.uib.configed.Globals;


public class FEditList extends FEditObject
			implements ListSelectionListener,
			ActionListener, KeyListener,
			MouseListener
{
	private javax.swing.JScrollPane scrollpane;
	protected javax.swing.JList visibleList;

	private JTextComponent tracker;

	private ListModel initialModel;
	private java.util.List<Object> initiallySelected;

	protected Object selValue = "";

	protected JPopupMenu popup;

	private CellEditor celleditor;

	public FEditList()
	{
		this(null);
	}

	public FEditList(JTextComponent tracker)
	{
		this(tracker, null);
	}

	public FEditList(JTextComponent tracker, CellEditor celleditor)
	{
		super("");
		this.tracker = tracker;
		this.celleditor = celleditor;

		visibleList = new JList(); //new String[]{"a","b","c"});
		visibleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		visibleList.addListSelectionListener(this);
		visibleList.setCellRenderer(new StandardListCellRenderer());

		visibleList.addMouseListener(this);

		scrollpane = new javax.swing.JScrollPane();
		scrollpane.setViewportView(visibleList);
		editingArea.add(scrollpane, BorderLayout.CENTER);
		editable = false;

		popup = new JPopupMenu();
		visibleList.setComponentPopupMenu(popup);
	}

	@Override
	protected void initComponents()
	{
		super.initComponents();
		visibleList.addKeyListener(this);

	}

	public void setCellRenderer(ListCellRenderer render)
	{
		visibleList.setCellRenderer(render);
	}

	public void setListModel(ListModel model)
	{
		//logging.debug(this, "setListModel " + model);
		visibleList.setModel(model);
		initialModel = model;
	}

	public void setSelectionMode(int selectionMode)
	{
		//logging.debug(this, "setSelectionMode " + selectionMode);
		visibleList.setSelectionMode(selectionMode);
	}


	public void setEditable(boolean b)
	{
		//logging.debug(this, "setEditable " + b);
		super.setEditable(b);
		extraField.setVisible(b);
		buttonRemove.setVisible(b);
		buttonAdd.setVisible(b);
	}

	protected Object getValueAt(Point location)
	{
		return visibleList.getModel().getElementAt(
		           visibleList.locationToIndex( location )
		       );
	}

	protected void setExtraFieldToListValueAt(Point location)
	{
		String txt =  "" + getValueAt( location );
		//visibleList.getModel().getElementAt( visibleList.locationToIndex( location ) );
		//logging.debug(this, " location has text " + txt);
		extraField.setText( txt );
		extraFieldChanged(false);
	}

	protected void addSelectedValues(java.util.List<Object> toSelect)
	{
		if (toSelect == null)
			return;

		ListModel model = visibleList.getModel();

		for (int i = 0; i<model.getSize(); i++)
		{
			Object element = model.getElementAt(i);
			//logging.debug(this, "addSelectedValues, check element " + element + " no " + i);

			if (toSelect.contains(element))
			{
				//logging.debug(this, "setSelectedValues, adding " + element);
				visibleList.addSelectionInterval(i,i);
			}
		}

		visibleList.ensureIndexIsVisible(visibleList.getMaxSelectionIndex());
	}

	public void initSelection()
	{
		visibleList.setSelectedIndex(0);
	}

	public void setSelectedValues(java.util.List<Object> toSelect)
	{
		//logging.debug(this, "setSelectedValues " + toSelect);
		initiallySelected = toSelect;

		visibleList.clearSelection();
		//logging.debug(this, "setSelectedValues , selection cleared");
		addSelectedValues(toSelect);
		//logging.debug(this, "setSelectedValues , selected values added");
	}

	public void setSelectedValue(Object ob)
	{
		//logging.debug(this, "setSelectedValue " + ob);
		visibleList.setSelectedValue(ob, true);
	}

	private void addElementFromExtraField(Object element)
	{
		//logging.debug(this, "requested addElementFromExtraField >" + element + "<");
		addElement(element);
		
		//extraField.removeActionListener(this); 
		// extraField.setText(""); //an empty list value will always be created by what ever event
		//extraField.addActionListener(this);
		extraFieldChanged(false);
	}

	protected void addElement(Object element)
	{
		//logging.debug(this, "requested addElement >" + element + "<");
		ListModel limo = visibleList.getModel();
		if (limo instanceof DefaultListModel)
		{
			if (! ((DefaultListModel) limo).contains(element) )
			{
				((DefaultListModel) limo).addElement(element);
				java.util.List list = new ArrayList(); //getSelectedList();
				list.add(element);
				addSelectedValues(list);
			}
		}
		else
		{
			logging.error(this, "for element adding required instance of DefaultListModel");
		}

	}

	@Override
	public Object getValue()
	{
		return getSelectedList();
	}

	private void setTracker(Object s)
	{
		if (tracker != null)
			tracker.setText("" + s);
	}

	@Override
	public void setStartValue(Object s)
	{
		//logging.debug(this, "setStartValue " + s);
		super.setStartValue(s);
		setTracker(s);
	}

	@Override
	protected void cancel()
	{
		setListModel(initialModel);
		setSelectedValues(initiallySelected);

		super.cancel();

		if (celleditor != null)
			celleditor.stopCellEditing();
	}

	@Override
	protected void commit()
	{
		logging.info(this, "FEditList.commit");

		if (Globals.forbidEditingTargetSpecific())
		{
			logging.info(this, "commit: forbidden");
			cancel();
		}
		else
		{
			if (celleditor != null)
				celleditor.stopCellEditing();

			if (leaveOnCommit) leave();
		}
	}


	//======================
	// interface ActionListener
	@Override
	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		super.actionPerformed(e);

		if (e.getSource() == buttonAdd)
		{
			addElementFromExtraField(extraField.getText());
		}
		else if (e.getSource() == buttonRemove)
		{
			visibleList.clearSelection();
		}
	}
	//======================

	//======================
	// interface KeyListener
	@Override
	public void keyPressed (KeyEvent e)
	{
		super.keyPressed(e);

		if (e.getSource() == buttonAdd)
		{
			addElementFromExtraField(extraField.getText());
		}
		else if 	(e.getSource() == buttonRemove)
		{
			visibleList.clearSelection();
		}
	}
	@Override
	public void keyTyped (KeyEvent e)
	{
	}
	@Override
	public void keyReleased (KeyEvent e)
	{
	}
	//======================


	//======================
	//interface MouseListener
	public void mousePressed(MouseEvent e)
	{
	}

	public void  mouseClicked(MouseEvent e)
	{
		if (e.getClickCount() > 1)
			setExtraFieldToListValueAt( e.getPoint() );

	}

	public void  mouseEntered(MouseEvent e)
	{
	}

	public void  mouseExited(MouseEvent e)
	{
	}

	public void  mouseReleased(MouseEvent e)
	{}
	//======================


	@Override
	public boolean init()
	{
		setStartValue(selValue);
		return super.init();
	}


	public java.util.List getSelectedList()
	{
		java.util.List result = new ArrayList();

		ListModel model = visibleList.getModel();

		for (int i = 0; i<model.getSize(); i++)
		{
			Object element = model.getElementAt(i);
			if (visibleList.isSelectedIndex(i))
				result.add(element);
		}

		return result;
	}

	//======================
	//interface ListSelectionListener
	public void valueChanged(ListSelectionEvent e)
	{
		java.util.List selectedList = getSelectedList();
		//logging.debug(this, "FEditList valueChanged ,  selected " + selectedList);
		setDataChanged(true);

		setTracker(selectedList);

		buttonRemove.setEnabled(selectedList.size() > 0);
	}
	//======================


}


