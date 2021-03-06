package de.uib.configed.gui;

import de.uib.utilities.swing.*;
import de.uib.configed.clientselection.*;
import de.uib.utilities.thread.WaitCursor;
import de.uib.utilities.logging.logging;
import de.uib.configed.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;

public class SavedSearchesDialog extends FEditList
{
	private SelectionManager manager;
	private List<String> result;
	private DefaultListModel model;
	private java.util.List<Object> saveSelValue;

	public SavedSearchesDialog()
	{
		setTitle(configed.getResourceValue("SavedSearchesDialog.title") + " (" + Globals.APPNAME + ")" );
		setModal(false);
		setLeaveOnCommit(false);
		manager = new SelectionManager(null);
		result = new LinkedList<String>();

		model = new DefaultListModel();

		setEditable(true);
		setListModel( model );
		resetModel();

		buttonAdd.setVisible(true);
		buttonRemove.setVisible(false);
		extraField.setVisible(false);

	}


	public void start()
	{
		buttonAdd.setEnabled(true);
		//reloadAction();
		resetModel();
	}

	@Override
	protected void createComponents()
	{
		super.createComponents();
		
		//redefine buttonCommit
		buttonCommit.setToolTipText(
			de.uib.configed.configed.getResourceValue("SavedSearchesDialog.ExecuteButtonTooltip") 
			);
		buttonCommit.setIcon(Globals.createImageIcon("images/execute.png", ""));
		buttonCommit.setSelectedIcon(Globals.createImageIcon("images/execute_over.png", ""));
		buttonCommit.setDisabledIcon(Globals.createImageIcon("images/execute_disabled.png", ""));
		buttonCommit.setPreferredSize(new java.awt.Dimension(buttonWidth, de.uib.utilities.Globals.buttonHeight));
		
		
		
		buttonCancel.setToolTipText( 
			de.uib.configed.configed.getResourceValue("SavedSearchesDialog.CancelButtonTooltip"));
		
	}

	@Override
	protected void initComponents()
	{
		super.initComponents();
		

		buttonRemove.addActionListener( new ActionListener(){
			                                public void actionPerformed( ActionEvent e )
			                                {
				                                logging.debug(this, "actionPerformed");
				                                //does not find the selected entry
				                                removeSelectedEntry();
			                                }
		                                }
		                              );
		buttonRemove.setToolTipText(de.uib.configed.configed.getResourceValue("SavedSearchesDialog.RemoveButtonTooltip"));

		buttonAdd.setEnabled(true);
		buttonAdd.addActionListener( new ActionListener(){
			                             public void actionPerformed( ActionEvent e )
			                             {
				                             logging.debug(this, "actionPerformed on buttonAdd ");
				                             addElement();
			                             }
		                             }
		                           );

		JMenuItem reload = new JMenuItemFormatted();
		//reload.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0)); does not find itscontext
		reload.setText(configed.getResourceValue("ConfigedMain.reloadTable"));
		reload.setIcon(de.uib.configed.Globals.createImageIcon("images/reload16.png", ""));
		reload.setFont(Globals.defaultFont);
		reload.addActionListener(new ActionListener()
		                         {
			                         public void actionPerformed(ActionEvent e)
			                         {
				                         logging.debug(this, "------------- reload action");
				                         reloadAction();
			                         }
		                         }
		                        );
		popup.add(reload);

		JMenuItem remove = new JMenuItemFormatted();
		//remove.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0)); does not find itscontext
		remove.setText(de.uib.configed.configed.getResourceValue("SavedSearchesDialog.RemoveButtonTooltip"));
		//remove.setIcon(de.uib.configed.Globals.createImageIcon("images/remove16.png", ""));
		remove.setFont(Globals.defaultFont);
		remove.addActionListener(new ActionListener()
		                         {
			                         public void actionPerformed(ActionEvent e)
			                         {
				                         logging.debug(this, "------------- remove action");
				                         removeSelectedEntry();
			                         }
		                         }
		                        );
		popup.add(remove);

		JMenuItem edit = new JMenuItemFormatted();
		edit.setText(de.uib.configed.configed.getResourceValue("SavedSearchesDialog.EditSearchMenu"));
		edit.setFont(Globals.defaultFont);
		edit.addActionListener( new ActionListener()
		                        {
			                        public void actionPerformed(ActionEvent e)
			                        {
				                        editSearch( visibleList.getSelectedValue().toString() );
			                        }
		                        }
		                      );
		popup.add(edit);


	}

	@Override public void setVisible(boolean b)
	{
		logging.debug(this, "setVisible " + b);
		super.setVisible(b);
	}

	@Override
	public void setDataChanged(boolean b)
	{
		boolean active = buttonCommit.isEnabled();
		super.setDataChanged(b);
		buttonCommit.setEnabled(active);
	}




	//======================
	//interface ListSelectionListener
	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		logging.debug(this, "SavedSearchesDialog ListSelectionListener valueChanged " + e);

		super.valueChanged(e);

		if (e.getValueIsAdjusting())
			return;

		buttonCommit.setEnabled(getSelectedList().size() > 0);
	}
	//=====

	@Override
	public Object getValue()
	{
		return result;
	}
	
	@Override
	protected void commit()
	{
		result=null;
		//logging.info(this, " SavedSearchesDialog.commit");

		buttonCommit.setEnabled(false);
		buttonCancel.setEnabled(false);

		WaitCursor waitCursor = new WaitCursor( this, "SavedSearchesDialog.commit" );

		try
		{

			List<String> selected = getSelectedList();
			if( !selected.isEmpty() )
			{
				manager.loadSearch( selected.get(0) );

				//test:
				//manager.setSearch(de.uib.opsidatamodel.SavedSearches.SEARCHfailedByTimeTestS);
				result = manager.selectClients();
			}
			super.commit();
		}
		finally
		{
			buttonCommit.setEnabled(true);
			buttonCancel.setEnabled(true);
			waitCursor.stop();
		}
	}

	@Override
	protected void cancel()
	{
		result = null;
		//resetModel();
		super.cancel();
	}

	private void removeSelectedEntry()
	{
		int index = visibleList.getSelectedIndex();
		logging.debug(this, "remove selected Entry, list index " + index);

		if( index == -1 )
			return;
		//logging.debug( this, "we have list size " + model.getSize());
		logging.debug( this, "remove entry at " + index );

		removeSavedSearch((String) model.get(index) );
		model.remove( index );
	}

	//overwrite to implement persistency
	protected void removeSavedSearch(String name)
	{
		manager.removeSearch( name );
	}

	//overwrite to implement
	protected void reloadAction()
	{
	}

	//overwrite to implement
	protected void addElement()
	{
	}

	//overwrite to implement
	protected void editSearch( String name )
	{
	}


	public void resetModel()
	{
		logging.debug(this, "resetModel");
		model.removeAllElements();

		de.uib.opsidatamodel.SavedSearches savedSearches = manager.getSavedSearches();
		java.util.TreeSet<String> nameSet = new java.util.TreeSet<String>(manager.getSavedSearchesNames());
		Map<String, String> valueMap = new HashMap<String, String>();
		Map<String, String> descMap = new HashMap<String, String>();

		for( String ele: nameSet )
		{
			model.addElement( ele );
			valueMap.put(ele, ele);
			descMap.put(ele, savedSearches.get(ele).getDescription());
		}

		setCellRenderer(	new de.uib.utilities.swing.list.ListCellRendererByIndex(
		                     valueMap, descMap, null, -1, false, "")
		               );

		initSelection();

	}

	//======================
	//interface MouseListener
	@Override
	public void mousePressed(MouseEvent e)
	{
	}

	@Override
	public void  mouseClicked(MouseEvent e)
	{
		//logging.info(this, "mouseClicked " + e);
		if (e.getClickCount() > 1)
		{
			commit();
		}

	}
	@Override
	public void  mouseEntered(MouseEvent e)
	{
	}

	@Override
	public void  mouseExited(MouseEvent e)
	{
	}

	public void  mouseReleased(MouseEvent e)
	{}
	//======================



}