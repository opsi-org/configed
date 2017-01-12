package de.uib.configed.gui;

import de.uib.utilities.swing.FEditList;
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
    protected List<String> result;
    private DefaultListModel model;
    WaitCursor waitCursor;
    CommitTask commitTask;

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
        
        buttonAdd.setVisible(false);
        extraField.setVisible(false);
        buttonRemove.addActionListener( new RemoveListener() );
        buttonRemove.setToolTipText(de.uib.configed.configed.getResourceValue("SavedSearchesDialog.RemoveButtonTooltip"));
    }
    
    @Override
	protected void initComponents()
	{	
		super.initComponents();
		
		buttonCommit.setDisplay(
			de.uib.configed.configed.getResourceValue("SavedSearchesDialog.ExecuteButtonTooltip"),
			"images/execute.png",
			"images/execute_over.png",
			null,
			"images/execute_disabled.png"
			);
		
		buttonCancel.setDisplay(
			de.uib.configed.configed.getResourceValue("SavedSearchesDialog.CancelButtonTooltip"),
			"images/cancel.png", 
			"images/cancel_over.png", 
			null, 
			"images/cancel_disabled.png");
			
		
	}
    
    
	
	//======================
	//interface ListSelectionListener
	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		super.valueChanged(e);
		java.util.List selectedList = getSelectedList();
		buttonCommit.setEnabled(selectedList.size() > 0);
	}
	//=====
	
    @Override
    public Object getValue()
    {
        return result;
    }
    
    
    private class CommitTask extends SwingWorker<java.util.List<String>, String[]>{
    		@Override
    		public java.util.List<String> doInBackground(){
    			java.util.List<String> selected = getSelectedList();
    			logging.debug(this, "selected " + selected);
			if( !selected.isEmpty() )
			{
				publish(new String[0]);
				manager.loadSearch( selected.get(0) );
				result = manager.selectClients();
				logging.debug(this, "result of selection " + result);
			}
    			return result;
    		}
    		@Override
    		public void done(){
    			
			buttonCommit.setEnabled(true);
			buttonCancel.setEnabled(true);
			waitCursor.stop();
    		}
    		
    		@Override
		protected void process(java.util.List<String[]> chunks)
		{
			buttonCommit.setEnabled(false);
			buttonCancel.setEnabled(false);
		}
    }
    
    protected java.util.List<String> produceResult()
    {
    		result = null;;
    		
    		try{
    			result = commitTask.get();
    			logging.debug(this, "result of selection " + result);
			
    		}
    		catch(InterruptedException ex)
    		{
    			logging.debug(this, ex.toString());
    		}
    		catch(java.util.concurrent.ExecutionException ex)
    		{
    			logging.debug(this, ex.toString());
    		}
    		
    		return result;
    		
    	
    }
    		
    
    @Override
    protected void commit()
    {
        result=null;
       
	
		waitCursor = new WaitCursor( this, getCursor(), "SavedSearchesDialog_mit_worker.commit" );
		commitTask = new CommitTask();
		commitTask.execute();
		
        
		/*
		try
		{
        	
			List<String> selected = getSelectedList();
			if( !selected.isEmpty() )
			{
				manager.loadSearch( selected.get(0) );
				result = manager.selectClients();
			}
			//resetModel();
			super.commit();
		}
		finally
		{
			buttonCommit.setEnabled(true);
			buttonCancel.setEnabled(true);
			waitCursor.stop();
		}
		*/
    }
    
    @Override
    protected void cancel()
    {
        result = null;
        resetModel();
        super.cancel();
    }
    
    //overwrite to implement persistency
    protected void removeSavedSearch(String name)
    {
    	  	manager.removeSearch( name );
    }
    
    public void resetModel()
    {
    	logging.debug(this, "resetModel");
        model.removeAllElements();
        java.util.TreeSet<String> list = new java.util.TreeSet<String>( manager.getSavedSearches() );
        for( String ele: list )
            model.addElement( ele );
        logging.debug(this, "resetModel, new model " + model);
    }
    
    private class RemoveListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
        		int index = visibleList.getSelectedIndex();
        		logging.debug(this, "actionPerformed remove, list index " + index);
            
            if( index == -1 )
                return;
            //logging.debug( this, "we have list size " + model.getSize());
            logging.debug( this, "remove entry at " + index );
            
            removeSavedSearch((String) model.get(index) );
            model.remove( index );
        }
    }
}