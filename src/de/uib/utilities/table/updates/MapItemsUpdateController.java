/*
 * MapItemsUpdateController.java
 *
 * By uib, www.uib.de, 2009
 * Author: Rupert Röder
 * 
 */

package de.uib.utilities.table.updates;

import java.util.*;
import de.uib.utilities.*;
import de.uib.utilities.thread.WaitCursor;
import de.uib.utilities.logging.*;
import de.uib.utilities.table.*;
import de.uib.utilities.table.gui.*;
import de.uib.utilities.table.provider.*;

public class MapItemsUpdateController implements de.uib.utilities.table.updates.UpdateController
{
	GenTableModel tablemodel;
	PanelGenEditTable panel;
	MapBasedUpdater updater;
	TableUpdateCollection updateCollection;
	
	public MapItemsUpdateController(
		PanelGenEditTable panel,
		GenTableModel model,
		MapBasedUpdater updater,
		TableUpdateCollection updateCollection
		)
	{
		this.panel = panel;
		this.tablemodel = model;
		this.updater = updater;
		this.updateCollection = updateCollection;
	}
	

	public boolean saveChanges()
	{
		//System.out.println (" ------- update controller called ");
		logging.info(this, "saveChanges");
		
		WaitCursor waitCursor = new WaitCursor( ); //licencesFrame, licencesFrame.getCursor() );
		
		boolean success = true; //true until failure
		
		Vector successfullInsertsWithNewKeys = new Vector<MapBasedTableEditItem>();
		
		Iterator iter = updateCollection.iterator();
		
		String lastKeyValue = new String("");
		
		boolean goOn = true;
		
		while (iter.hasNext() && success)
		{
			MapBasedTableEditItem updateItem = (MapBasedTableEditItem) iter.next();
			if ( 
				updateItem.getSource() == this.tablemodel
				)
			{
				logging.debug(this, " handling updateItem "  +  updateItem);
				
				if (updateItem instanceof MapDeliveryItem)
				{
					String result = updater.sendUpdate( updateItem.getRowAsMap() );
					
					success = (result != null);
					if (success && updateItem.keyChanged())
					{
						successfullInsertsWithNewKeys.add(updateItem);
						//lastKeyValue = updateItem.getKeyColumnStringValue();
						lastKeyValue = result;
					}
				}
				
				else if (updateItem instanceof MapDeleteItem)
				{
					success = updater.sendDelete( updateItem.getRowAsMap() );
				}
				
				else
				{
					logging.error("update item type not supported" );
					success = false;
				}
			}
		}
		
		if (success)
		{
			// we remove the update items
			// (either all or no one)
			iter = updateCollection.iterator();
			while (iter.hasNext())
			{
				MapBasedTableEditItem updateItem = (MapBasedTableEditItem) iter.next();
				if ( 
					updateItem.getSource() == tablemodel
					)
				{
					iter.remove();
					// this can be safely done according to an Iterator guarantee
				}
			}
			
			
			logging.info(this, " we start with the new data set, reload request  " + tablemodel.isReloadRequested() );
			tablemodel.startWithCurrentData();
			tablemodel.reset();
			
			logging.info(this, "saveChanges lastKeyValue " + lastKeyValue);
			panel.moveToKeyValue(lastKeyValue);
			
			
			waitCursor.stop();
		}	
		else
		{
			if (successfullInsertsWithNewKeys.size() > 0)
			{
				// we have to remove all update items ...
				while (iter.hasNext())
				{
					MapBasedTableEditItem updateItem = (MapBasedTableEditItem) iter.next();
					if ( 
						updateItem.getSource() == tablemodel
						)
					{
						iter.remove();
						// this can be safely done according to an Iterator guarantee
					}
				}
				
				
				// ... and look what we have in the database
				tablemodel.requestReload();
				tablemodel.reset();
				success = true; // we have valid data again, even if not the expected ones
			}
				
			waitCursor.stop();
			logging.checkErrorList(null);
			
		}
			
		
		return success; 
	}
	
	public boolean cancelChanges()
	{
		
		Iterator iter = updateCollection.iterator();
		while (iter.hasNext())
		{
			MapBasedTableEditItem updateItem = (MapBasedTableEditItem) iter.next();
			if ( 
				updateItem.getSource() == tablemodel
				)
			{
				iter.remove();
				// this can be safely done according to an Iterator guarantee
			}
		}
		
		
		tablemodel.invalidate();
		tablemodel.reset();
			
			
		return true;
	}
}
