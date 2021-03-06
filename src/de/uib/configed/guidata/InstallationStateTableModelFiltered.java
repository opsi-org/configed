package de.uib.configed.guidata;

import de.uib.configed.configed;
import de.uib.configed.Globals;
import de.uib.configed.ConfigedMain;
import de.uib.utilities.logging.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class InstallationStateTableModelFiltered 
	extends InstallationStateTableModel
	
{
		private int[] filter;
		//filter is a function  
		//row --> somerow (from super.table)
		
		//it holds
		//product(row) = product(somerow)
		
		private int[] filterInverse;
		
		
		private de.uib.configed.SavedStates.SessionSaveSet filterSaver;
		
		
		public InstallationStateTableModelFiltered (	String[] selectedClients, ConfigedMain main, 
						Map<String, Map<String, Map<String, String>>> collectChangedStates,
						List listOfInstallableProducts,
						Map statesAndActions,
						Map possibleActions,
						Map<String, Map<String, Object>> productGlobalInfos, 
						List<String> displayColumns,
						de.uib.configed.SavedStates.SessionSaveSet filterSaver
						)
		{
			super(
						selectedClients,  main, 
						collectChangedStates,
						listOfInstallableProducts,
						statesAndActions,
						possibleActions,
						productGlobalInfos, 
						displayColumns
						);
			//test setFilter(new int[]{4,8,9});
			this.filterSaver = filterSaver;
		}
		
		private void saveFilterSet(Set<String> filterSet)
		{
			/*
			Set<String> testSet = new HashSet<String>();
			testSet.add("jedit");
			*/
			filterSaver.serialize(filterSet);
			//filterSaver.serialize(testSet);
			logging.info(this,  "saveFilterSet " + filterSet);
		}
		
		
		public void resetFilter()
		{
			Set<String> filterSaved = (Set<String>)filterSaver.deserialize();
			if (filterSaved == null
				|| filterSaved.isEmpty())
			{
				setFilterFrom((Set) null);
			}	
			else
			{
				Set<String> products_only_in_filterset = new HashSet<String>(filterSaved);
				products_only_in_filterset.removeAll( tsProductNames );
				filterSaved.removeAll(products_only_in_filterset);
				//A - (A - B) is the intersection
					
				setFilterFrom(filterSaved);
				logging.debug(this,  "resetFilter " + filterSaved);
			}
				
			
				
		}
		
		public void setFilterFrom(Set<String> ids)
		{
			saveFilterSet(ids);
			
			if (ids == null)
			{
				setFilter((int[]) null);
			}
			else
			{
				filter = new int[ids.size()];
				int i = 0;
				
				String[] products = new String[ids.size()];
				for (String id : ids)
				{
					products[i] = id ;
					i++;
				}
				
				for (i = 0; i < ids.size(); i++)
				{
					filter[i] = productsV.indexOf(products[i]);
				}	
				
				setFilter(filter);
			}
		}
		
		private void setFilter(int[] filter)
		{
			logging.info(this, "setFilter " + logging.getStrings(filter));
			this.filter = filter;
			
			
			if (filter == null)
				filterInverse = null;
			else
			{
				filterInverse = new int[super.getRowCount()];
				for (int j = 0;j<super.getRowCount(); j++)
				{
					filterInverse[j] = -1;
				}
				for (int i = 0; i < filter.length; i++)
				{
					filterInverse[filter[i]] = i;
				}
				
				logging.debug(this, "setFilter: filter, filterInverse " + logging.getStrings(filter) + ", " + logging.getStrings(filterInverse));
			}
			
			fireTableDataChanged();
		}
		
		private int originRow(int i)
		{
			if (filter == null)
				return i;
			
			/*
			if filter[i] > super.getRowCount()-1)
				logging.warning("invalid filter");
			*/
			if (i >= filter.length)
			{
				logging.info(this, "originRow, error cannot evaluate filter; i, filter.length " + i + ", " + filter.length);
				return i;
			}
				
			return filter[i];
		}
		
		
		@Override
		public int getRowFromID(String id)
		{
			int superRow = super.getRowFromID(id);
			if (filterInverse == null)
				return superRow;
			
			return filterInverse[ superRow ];
		}
			
		
		// table model
		public int getRowCount()
		{
			if (filter == null)
				return super.getRowCount();
			else
				return filter.length;
		}
		
		
		public Object getValueAt(int row, int displayCol)
		{
			return super.getValueAt(originRow(row), displayCol);
		}
		
		public boolean isCellEditable(int row, int col)
		{
			return super.isCellEditable(originRow(row), col);
		}
		
		public void setValueAt(Object value, int row, int col)
		{
			super.changeValueAt(value, 
				originRow(row), 
				col);
			fireTableCellUpdated(row, col);
		}
		
		//ComboBoxModeller
		public  ComboBoxModel getComboBoxModel (int row,  int column)
		{
			return super.getComboBoxModel (originRow(row),  column);
		}
	
		
}