/* 
 *
 * (c) uib, www.uib.de, 2016
 *
 * author Rupert Röder
 */

package de.uib.configed.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import java.util.*;

import de.uib.utilities.*;
import de.uib.utilities.tree.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.swing.*;
import de.uib.utilities.table.*;
import de.uib.utilities.table.gui.*;
import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.configed.guidata.ListMerger;
import de.uib.utilities.pdf.DocumentToPdf;


public class EditMapPanelGroupedForHostConfigs extends de.uib.utilities.datapanel.EditMapPanelGrouped

// works on a map of pairs of type String - List
{
	
	
	protected JPopupMenu popup0;
	protected JPopupMenu popup1;
	
	protected JMenuItem popupItemDeleteEntry;
	
	
	
	public  EditMapPanelGroupedForHostConfigs( TableCellRenderer tableCellRenderer, 
		boolean keylistExtendible, 
		boolean keylistEditable,
		boolean reloadable,
		final de.uib.utilities.datapanel.AbstractEditMapPanel.Actor actor)
	{
		super(tableCellRenderer, keylistExtendible, keylistEditable, reloadable, actor);
			
		
		popupItemDeleteEntry = new JMenuItem( configed.getResourceValue("EditMapPanel.PopupMenu.RemoveEntry"));
			
		popupItemDeleteEntry.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e){
					deleteUser();
				}
			}
		);
		
		
		popup0 = new PopupMenuTrait(new Integer[]{
										//PopupMenuTrait.POPUP_SAVE,
										PopupMenuTrait.POPUP_RELOAD
			})
				{
					public void action(int p)
					{
						//logging.info(this, "action popup  " + p);
						
						switch(p)
						{
							case PopupMenuTrait.POPUP_RELOAD:
								reload();
								//actor.reloadData();
								break;
								
							case PopupMenuTrait.POPUP_SAVE:
								//actor.saveData();
								break;
						}
						
					}
				}
			;
			
		popup1 = new PopupMenuTrait(new Integer[]{
										//PopupMenuTrait.POPUP_SAVE,
										PopupMenuTrait.POPUP_RELOAD
			})
				{
					public void action(int p)
					{
						//logging.info(this, "action popup  " + p);
						
						switch(p)
						{
							case PopupMenuTrait.POPUP_RELOAD:
								reload();
								//actor.reloadData();
								break;
								
							case PopupMenuTrait.POPUP_SAVE:
								//actor.saveData();
								break;
						}
						
					}
				}
			;
		
		MouseListener popupListener0 = new utils.PopupMouseListener(popup0){
			@Override
			protected void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					//int selRow = tree.getRowForLocation(e.getX(), e.getY());
					TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
					//logging.info(this, " sel path " + selPath);
					//if(selRow % 2 == 0) //test
					if ( !isUserPath( selPath ) )
						super.maybeShowPopup(e);
				}
			}
		};
		tree.addMouseListener(popupListener0);
		
		
		popup1.add( popupItemDeleteEntry );
		
		MouseListener popupListener1 = new utils.PopupMouseListener(popup1){
			@Override
			protected void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
					//logging.info(this, " sel path " + selPath);
					if ( isUserPath( selPath ) ) 
						super.maybeShowPopup(e);
				}
			}
		};
		tree.addMouseListener(popupListener1);
		
		
		
		
	}
	
	private boolean isUserPath(TreePath path)
	{
		if (path.getPathCount() == 3 && path.getPathComponent(1).toString().equals( de.uib.opsidatamodel.permission.OpsiPermission.CONFIGKEY_STR_USER ) )
			return true;
		
		return false;
	}
	
	private int getUserStartIndex()
	{
		return 1;
	}
	
	
	protected void deleteUser()
	{
		//logging.info(this, "delete " + mapTableModel.getKeys());
		//logging.info(this, "delete " + mapTableModel.getData());
		javax.swing.tree.TreePath p = tree.getSelectionPath();
		
		//actor.deleteData();
		if (p != null)
		{
			logging.info(this, "deleteUser path "  + p);
			
			int startComponentI = getUserStartIndex();
			StringBuffer keyB = new StringBuffer(p.getPathComponent(startComponentI).toString());
			startComponentI++;
			for (int i =startComponentI; i < p.getPathCount(); i++)
			{
				keyB.append(".");
				keyB.append(p.getPathComponent(i).toString());
			}
			String key = keyB.toString();
			logging.info(this, "deleteUser, selected user key " + key);
			//logging.info(this, "deleteUser, selected entry " + partialPanels.get( key ).getTableModel());
			Vector<String> propertyNames = partialPanels.get( key ).getNames();
			logging.info(this, "deleteUser, property names " + propertyNames);
			for (String name : propertyNames)
			{
				((de.uib.utilities.datapanel.EditMapPanelX) partialPanels.get( key )).removeProperty( name );
			}
			
			int row = tree.getRowForPath(p); 
			
			//logging.info(this, "reloaded, return to " + row);
			tree.setExpandsSelectedPaths(true);
			tree.setSelectionInterval(row, row);
			tree.scrollRowToVisible(row);
		}
	}
	
}
