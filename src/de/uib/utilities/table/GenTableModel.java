/*
 *
 * 	uib, www.uib.de, 2008-2013
 * 
 *	author Rupert Röder, Martina Hammel
 *
 */

package de.uib.utilities.table;

import de.uib.utilities.*;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.event.*;
import java.util.*;

import de.uib.utilities.table.provider.*;
import de.uib.utilities.table.updates.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.table.*;


public class GenTableModel extends AbstractTableModel
			implements TableModelFunctions

{

	protected int rowsLength;
	protected int colsLength;
	protected Vector<String> columnNames;
	protected Vector<String> classNames;
	protected Vector<Vector<Object>> rows;

	protected Vector<Integer> addedRows;
	// rows which are added and not yet saved
	protected Vector<Integer> updatedRows;
	// rows which are updated and not yet saved

	protected Vector<Integer> finalCols;
	// columns for which the values can only be entered and changed as long as the row is not saved

	protected boolean[] colEditable;
	// columns which are editable in principle (but they may be final)

	protected int keyCol = -1;
	protected TableUpdateCollection updates;
	protected String tableName;
	protected boolean modelDataValid;
	protected boolean modelStructureValid;

	protected TableProvider tableProvider;
	protected TableUpdateItemFactory itemFactory;
	protected int saveUpdatesSize;

	protected final ChainedTableModelFilter chainedFilter;
	protected final TableModelFilter emptyFilter;
	private TableModelFilter workingFilter;
	public final static String DEFAULT_FILTER_NAME = "default";


	//maps for TableModelFunctions
	protected KeyRepresenter<Integer> keyRepresenter;
	protected Map<TableModelFunctions.PairOfInt, Map<Object, java.util.List<Object>>>  functions;
	protected Map<Integer, RowStringMap> primarykey2Rowmap;
	protected Map<Integer, String> primarykeyTranslation;
	protected Mapping<Integer, String> primarykeyRepresentation;
	protected Map<TableModelFunctions.PairOfInt, Map<Integer, Mapping<Integer, String>>>  xFunctions;



	public GenTableModel (
	    de.uib.utilities.table.updates.TableUpdateItemFactory itemFactory,
	    de.uib.utilities.table.provider.TableProvider dataProvider,
	    int keyCol,
	    int[] finalColumns,
	    TableModelListener l,
	    de.uib.utilities.table.updates.TableUpdateCollection updates
	)
	{
		this.tableName = tableName;
		this.keyCol = keyCol;
		this.updates = updates;
		this.tableProvider = dataProvider;
		this.itemFactory = itemFactory;

		initColumns();
		rows = (Vector<Vector<Object>>) dataProvider.getRows();

		addedRows = new Vector<Integer>();
		updatedRows = new Vector<Integer>();

		this.finalCols = new Vector<Integer>();
		if (finalColumns == null)
		{
			if (keyCol > -1)
				this.finalCols.add(keyCol);
		}
		else
		{
			for (int i = 0; i < finalColumns.length; i++)
				this.finalCols.add(finalColumns[i]);
		}


		modelDataValid = false;
		modelStructureValid = true;


		if (rows == null)
			rowsLength = 0;
		else
			rowsLength = rows.size();

		if (l != null)
			addTableModelListener(l);

		chainedFilter = new ChainedTableModelFilter();
		emptyFilter = new TableModelFilter();
		setFilter( chainedFilter );


	}

	public GenTableModel (
	    de.uib.utilities.table.updates.TableUpdateItemFactory itemFactory,
	    de.uib.utilities.table.provider.TableProvider dataProvider,
	    int keyCol,
	    TableModelListener l,
	    de.uib.utilities.table.updates.TableUpdateCollection updates)
	{
		this(itemFactory, dataProvider, keyCol, null, l, updates);
	}

	public void clear()
	{
		colsLength = 0;
		rowsLength = 0;
		colEditable = new boolean[0];
		if (columnNames != null) columnNames.clear();
		if (classNames != null) classNames.clear();
		if (rows != null) rows.clear();
		clearUpdates();
		fireTableStructureChanged();
	}


	protected void initColumns()
	{
		columnNames = tableProvider.getColumnNames();
		logging.debug(this, "initColumns " + columnNames);
		/*
		Vector<String> columnNames1 = tableProvider.getColumnNames();
		logging.debug(this, "columnNames: " + columnNames1);

		if (columnNames1 != null)
		{
			columnNames = new Vector<String>();
			for (int i = 0; i < columnNames1.size(); i++)
				columnNames.add(columnNames1.get(i).toLowerCase());
	}
		*/

		classNames = tableProvider.getClassNames();

		if (columnNames == null)
			colsLength = 0;
		else
			colsLength = columnNames.size();

		colEditable = new boolean[colsLength];
	}

	public Vector<String> getColumnNames()
	{
		columnNames = tableProvider.getColumnNames();
		colsLength = columnNames.size();
		return columnNames;
	}

	public Vector<String> getClassNames()
	{
		classNames = tableProvider.getClassNames();
		return classNames;
	}

	public int getKeyCol()
	{
		return keyCol;
	}

	public void setKeyCol(int keyCol)
	{
		this.keyCol = keyCol;
	}

	public Vector<Integer> getFinalCols()
	{
		return finalCols;
	}

	public void invalidate()
	{
		tableProvider.requestReturnToOriginal(); //is needed
		modelDataValid = false;
		requestRefreshDerivedMaps();
	}

	
	public boolean isReloadRequested()
	{
		return !modelDataValid;
	}
	
	public void requestReload()
	{
		modelDataValid = false;
		requestRefreshDerivedMaps();
		tableProvider.requestReloadRows();
	}

	public void structureChanged()
	{
		tableProvider.structureChanged();
		modelStructureValid = false;
		requestReload();
	}

	public void startWithCurrentData()
	{
		tableProvider.setWorkingCopyAsNewOriginalRows();
		invalidate();
	}

	public void removeUpdates()
	{
		int newSize = updates.size();
		for (int i = newSize - 1; i >= saveUpdatesSize; i--)
		{
			//logging.debug(this, " remove i = " + i);
			updates.remove(i);
		}
		updatedRows.clear();
		invalidate();
	}


	public void setFilter(TableModelFilter filter)
	{
		workingFilter = filter;
	}

	/**
		define new filter by a TableModelFilterCondition
	*/
	public void setFilterCondition(TableModelFilterCondition cond)
	{
		clearFilter();
		chainedFilter.add(DEFAULT_FILTER_NAME, new TableModelFilter(cond));
	}


	public void clearFilter()
	{
		chainedFilter.clear();
	}

	public GenTableModel chainFilter(String filterName, TableModelFilter filter)
	{
		chainedFilter.add(filterName, filter);
		return this; //for chaining in notation
	}

	public Set<Object> getExistingKeys()
	{
		int keycol = getKeyCol();

		if (keycol < 0)
			return null;

		TreeSet<Object> result = new TreeSet<Object>();
		for (int row = 0; row < getRowCount(); row++)
		{
			if (getValueAt(row, keycol) != null)
				result.add(getValueAt(row, keycol));
		}

		return result;
	}


	protected void produceRows()
	{
		logging.info(this, " ---  produce rows");
		rows = tableProvider.getRows();
		//logging.debug(this, ":: " + rows);
		logging.debug(this, "produceRows(): count  " + rows.size() );

		rowsLength = rows.size();

		getColumnNames(); //update columns and class names from tableProvider

		logging.debug(this, " ---  rows produced, columnNames: " + columnNames);

		logging.debug(this, " --- using workingfilter  " + workingFilter );



		if (workingFilter != null && workingFilter.isInUse())
		{
			Vector<Vector<Object>> filteredRows = new Vector<Vector<Object>>();

			for (Vector<Object> row : rows)
			{
				if (workingFilter.test(row))
					filteredRows.add(row);
			}
			rows = filteredRows;
			rowsLength= rows.size();
			//logging.debug(this, " ---  filtered rows produced : " + rows);
		}


	}

	protected void refresh()
	{
		if (!modelDataValid)
			//perhaps the action should not depend on this condition,
			//since the necessary requests are already sent to the
			//table provider
		{

			produceRows(); //rows = tableProvider.getRows();
			//logging.debug(this, " rows.size() " + rows.size());

			if (!modelStructureValid)
			{
				initColumns();
				fireTableStructureChanged() ;
				modelStructureValid = true;
			}

			fireTableDataChanged();
			modelDataValid = true;

		}
	}

	public boolean isUsingFilter(String name)
	{
		return chainedFilter.getElement(name).isInUse();
	}

	public void setUsingFilter(String name, boolean newValue)
	{
		logging.debug(this, "setUsingFilter " + name + " to " + newValue);
		if (isUsingFilter(name) != newValue)
		{
			invalidate();
			chainedFilter.getElement(name).setInUse(newValue);
			reset();
		}
	}

	public void toggleFilter(String name)
	{
		setUsingFilter(name, !isUsingFilter(name));
	}

	private void clearUpdates()
	{
		addedRows.clear();
		updatedRows.clear();
		if (updates == null)
			saveUpdatesSize = 0;
		else
			saveUpdatesSize = updates.size();
	}

	public void threadedReset(final de.uib.utilities.thread.ReadyFlag flag)
	{
		new Thread()
		{
			public void run()
			{
				reset();
				flag.ready = true;
			}
		}.start();
	}

	/**
	* sets data to the source values 
	* (if model is not valid they are recollected)
	* clears update collection
	*/
	public void reset()
	{
		logging.info(this, "reset()");
		requestRefreshDerivedMaps();
		refresh();
		clearUpdates();
	}

	public void setEditableColumns(int[] editable)
	{
		for (int i=0; i<colsLength; i++)
		{
			colEditable[i] = false;
		}

		if (editable == null)
			return;

		for (int j = 0; j < editable.length; j++)
		{
			//if (editable[j] != keyCol) // key col cannot be made editable
			colEditable[editable[j]] = true;
		}
	}

	public String getColumnName(int col)
	{
		/*
		if (col >= columnNames.size())
		{
			logging.warning(this, "not existing columnIndex " + col);
			return "";
	}
		*/
		return columnNames.get(col);
	}

	public int getColumnCount() { return colsLength; }

	public int getRowCount() { return rowsLength;  }

	public Object getValueAt(int row, int col)
	{
		return (rows.get(row)).get(col);
	}

	public Vector<Object> getRow(int row)
	{
		return rows.get(row);
	}

	public RowStringMap getRowStringMap(int row)
	{
		RowStringMap result = new RowStringMap();

		for (int col = 0; col < getColumnNames().size(); col++)
		{
			result.put(
			    getColumnName(col),
			    "" + getValueAt(row, col)
			);
		}

		//logging.info(this, " getRowMap for modelrow " + row + ": " + result);

		return result;
	}

	public RowMap<String, Object> getRowMap(int row)
	{
		RowMap<String, Object> result = new RowMap<String, Object>();

		for (int col = 0; col < getColumnNames().size(); col++)
		{
			Object value =  getValueAt(row, col);
			if (value == null)
				value = "";
			else
				value = "" + value;

			result.put(
			    getColumnName(col), value
			);
		}

		//logging.info(this, " getRowMap for modelrow " + row + ": " + result);

		return result;
	}

	public Vector<Object> getColumn(int col)
	{
		Vector<Object> result = new Vector<Object>();
		for (int row = 0; row < rowsLength; row ++)
			result.add(getValueAt(row, col));

		return result;
	}

	public Vector<String> getOrderedColumn(int col)
	{
		TreeSet<String> set = new TreeSet<String>();
		for (int row = 0; row < rowsLength; row ++)
			set.add((String) getValueAt(row, col));

		Vector<String> result = new Vector<String>(set);

		return result;
	}


	public boolean isCellEditable( int row, int col )
	{
		if (addedRows.indexOf(row) == -1 && finalCols.indexOf(col) > -1)
			// we cannot edit a key column but when it is not saved in the data backend
			return false;

		if (colEditable[col])
			return true;

		return false;
	}


	public void setValueAt(Object value, int row, int col)
	{
		//wenn row hinzugefügte zeile ist:

		//
		//insertRow aufrufen

		//sonst:

		String oldValueString = "" + (rows.get(row)).get(col);

		//logging.debug(this, " old value at " + row + ", " + col + " : " + oldValueString);
		//logging.debug(this, " new value at " + row + ", " + col + " : " + value);
		//logging.debug(this, " --------------------  value has class " + value.getClass().getName() );

		//logging.debug(this, " key column class name  " + classNames.get(keyCol) );
		//logging.debug(this, " edit column class name  " + classNames.get(col) );

		String newValueString = "" + value;

		boolean valueChanged;

		if  (

		    (	(rows.get(row)).get(col) == null
		      &&
		      (
		          value == null
		          ||
		          value.equals("")
		      )
		    )

		    ||

		    (

		        oldValueString.equals(newValueString)
		    )

		)

			valueChanged = false;

		else
		{
			valueChanged = true;
		}

		if ( valueChanged )
		{
			if (addedRows.indexOf(row) == -1)
				// we dont register updates for already registered rows, since there values are passed via the row vector
			{
				if (updatedRows.indexOf(row) == -1)
				{
					Vector oldValues = (Vector)(rows.get(row)).clone();
					//logging.debug(this, "old values in GenTableModel " + oldValues);
					rows.get(row).setElementAt(value,col);
					//logging.debug(this, " new values in GenTableModel " + rows.get(row));

					if (itemFactory == null)
						logging.info("update item factory missing");
					else if (updates == null)
						logging.info("updates not initialized");
					else
					{
						updates.add(  itemFactory.produceUpdateItem(oldValues, rows.get(row) )  );
					}

					updatedRows.add(row);
				}
			}


			if (addedRows.indexOf(row) == -1 && finalCols.indexOf(col) > -1)
				// we cannot edit a key column after it is saved in the data backend
			{
				// we should not get any more to this code, since for this condition the value is marked as not editable
				logging.debugOut(logging.LEVEL_WARNING, "key column cannot be edited after saving the data");

				javax.swing.JOptionPane.showMessageDialog(null,
				        "values in this column are fixed after saving the data",
				        "Information",
				        javax.swing.JOptionPane.OK_OPTION);

				return;
			}

			//logging.debug(this, " new values in GenTableModel " + rows.get(row));

			//logging.debug(this, "  new value " + rows.get(row).get(col));
			rows.get(row).setElementAt(value,col); // in case of an updated row we did this already
			//logging.debug(this, " set new value " + rows.get(row).get(col));
			fireTableCellUpdated(row, col);

			requestRefreshDerivedMaps();
		}
	}



	//does not set values to null, leaves instead the original value
	//if the values map produces a null
	public void updateRowValues(int row, Map<String, Object> values)
	{
		//logging.debug(this, "updateRowValues: row, values " + row + ", '" + values);
		//logging.debug(this, "updateRowValues: columnNames " + columnNames);


		for (int col = 0; col < columnNames.size(); col++)
		{

			Object val = values.get(getColumnName(col));


			if (val != null)
				setValueAt(val, row, col);
		}
	}

	public void setRow(int row, Object[] a)
	{
		int col = 0;
		if (colsLength != a.length)
			logging.info("update row values less than than row elements");

		while (col < colsLength && col < a.length)
		{
			setValueAt(a[col], row, col);
			col++;
		}
	}

	public void addRow(Vector<Object> rowV)
	{
		logging.debug(this, "--- addRow size, row " + rowV.size() + ", " + rowV);

		rows.add(rowV);
		addedRows.add(rowsLength);

		//logging.debug(this, "addRow added, last col element " + getValueAt(rowsLength, colsLength - 1));

		updates.add(itemFactory.produceInsertItem(rowV) );
		requestReload(); // we shall have to reload the data if keys are newly generated

		rowsLength++;
		try{
			fireTableRowsInserted(rowsLength-1, rowsLength-1);
		}
		catch(Exception ex)
		{
			logging.logTrace(ex);
			logging.info(this, "addRow exception " + ex + " row " + rowV);
		}

		requestRefreshDerivedMaps();
	}


	public void addRow(Object[] a)
	{
		//logging.debug(this, "----------- GenTableModel addRow()");

		//if (addedRows.size() == 0) // we add only one row up to further notice
		{
			Vector rowV = new Vector();
			for (int i=0; i < colsLength; i++)
			{
				rowV.add(null);
			}
			for (int j=0; j < a.length; j++)
			{
				//logging.debug(this, " setting column " + j + " to " + a[j]);
				rowV.set(j, a[j]);
			}

			addRow(rowV);
		}
	}

	public  Vector<Object> produceValueRowFromSomeEntries(RowMap entries)
	{
		logging.debug(this, "produceValueRowFromSomeEntries " +  entries);

		Vector<Object> result = new Vector<Object>();

		for (String col : columnNames)
		{
			//logging.debug(this, "produceValueRowFromSomeEntries " + col + ": " + entries.get(col));
			
			/*
			if (col.equals("APO_PLZ"))
			{
				result.add(null);
				continue;
			}
			*/

			if (entries.get(col) != null)
				result.add(entries.get(col));
			else
				result.add(null);
		}

		return result;
	}

	private boolean checkDeletionOfAddedRow(int rowNum)
	{
		if (addedRows.indexOf(rowNum) > -1)
		{
			//logging.debug(this, "deleteRow, remove from addedRows  " + addedRows.indexOf(rowNum));
			//addedRows.remove(addedRows.indexOf(rowNum));

			//deletion of added rows is not adequately managed
			//therefore we reject it

			logging.info(this, "no deletion of added rows");

			javax.swing.JOptionPane.showMessageDialog(null,
			        "no deletion of added rows, please save or cancel editing",
			        "Information",
			        javax.swing.JOptionPane.OK_OPTION);
			return false;

		}

		return true;
	}


	public void deleteRows(int[] selection)
	{
		logging.debug(this, "deleteRows " + java.util.Arrays.toString(selection) );

		if (selection == null || selection.length == 0)
			return;

		java.util.Arrays.sort(selection);
		logging.debug(this, "deleteRows, sorted " + java.util.Arrays.toString(selection) );



		if (updates == null)
		{
			logging.info(this, "updates not initialized");
			return;
		}

		for (int i = 0; i < selection.length; i++)
		{
			if (!checkDeletionOfAddedRow(selection[i]))
				return;
		}

		//do with original row nums
		for (int i = 0; i < selection.length; i++)
		{
			Vector oldValues = (Vector)(rows.get(selection[i])).clone();
			logging.debug(this, "deleteRow values " + oldValues);
			updates.add(itemFactory.produceDeleteItem( oldValues ));

			if (updatedRows.indexOf(selection[i]) > -1)
			{
				logging.debug(this, "deleteRows, remove from updatedRows  " + updatedRows.indexOf(i));
				updatedRows.remove(updatedRows.indexOf(selection[i]));
			}
		}

		//adapt the model from the upper index downto the lower index (selection has been sorted)
		for (int i = selection.length - 1; i >= 0; i--)
		{
			rows.remove(selection[i]);
			rowsLength--;
			fireTableRowsDeleted(selection[i],selection[i] );
		}

		requestRefreshDerivedMaps();
	}



	public void deleteRow(int rowNum)
	{
		logging.debug(this, "deleteRow " + rowNum);

		if (rows.get(rowNum) == null)
		{
			logging.info(this, "delete row null ");
			return;
		}

		if (updates == null)
		{
			logging.info(this, "updates not initialized");
			return;
		}

		if (!checkDeletionOfAddedRow(rowNum))
			return;

		Vector oldValues = (Vector)(rows.get(rowNum)).clone();
		logging.debug(this, "deleteRow values " + oldValues);
		updates.add(itemFactory.produceDeleteItem( oldValues ));
		//we have to delete the source values, not the possibly changed current row


		if (updatedRows.indexOf(rowNum) > -1)
		{
			logging.debug(this, "deleteRow, remove from updatedRows  " + updatedRows.indexOf(rowNum));
			updatedRows.remove(updatedRows.indexOf(rowNum));
		}


		rows.remove(rowNum);
		rowsLength--;
		fireTableRowsDeleted(rowNum,rowNum);

		requestRefreshDerivedMaps();
		logging.debug(this, "deleted row " + oldValues);
	}



	public boolean isRowAdded(int rowNum)
	{
		return (addedRows.indexOf(rowNum) > -1);
	}

	public boolean someRowAdded(int[] rowNums)
	{
		boolean result = false;
		if (rowNums != null)
		{
			int i = 0;
			while (!result && i < rowNums.length)
			{
				if (addedRows.indexOf(rowNums[i]) > -1)
					result = true;
				i++;
			}
		}
		return result;
	}

	private Map<Object, java.util.List<Object>> buildFunction(int col1, int col2,
	        TableModelFilterCondition specialFilterCondition)
	{
		Map<Object, java.util.List<Object>> result = new HashMap<Object, java.util.List<Object>>();

		boolean saveUsingFilter = workingFilter != null && workingFilter.isInUse();

		if (specialFilterCondition != null)
		{
			// activate special filter and reproduce rows
			setFilter( new TableModelFilter(specialFilterCondition) );
			produceRows();
		}

		else if (saveUsingFilter)
			// we filtered but do not want to do it now
		{
			// turn off filter and reproduce rows
			setFilter( emptyFilter );
			produceRows();
		}


		for (int row = 0; row < getRowCount(); row++)
		{
			Object val1 = getValueAt(row, col1);

			java.util.List<Object> associatedValues = result.get(val1);

			if (associatedValues == null)
			{
				associatedValues = new ArrayList<Object>();
				result.put(val1, associatedValues);
			}

			Object val2 = getValueAt(row, col2);
			if (associatedValues.indexOf (val2) == -1)
				associatedValues.add(val2);
		}


		setFilter( chainedFilter );

		if (
		    specialFilterCondition != null
		    ||
		    saveUsingFilter
		)

		{
			//we changed filtering and have to reproduce the rows
			produceRows();
		}

		return result;
	}

	public boolean existsStringValueInCol(String value, int col)
	{
		boolean found = false;
		int i = 0;
		while (!found && i < getRowCount())
		{
			String compValue = "" + getValueAt(i, col);

			if (compValue.equals(value))
				found = true;
			else
				i++;
			//logging.debug(this, "value  " +  value + " (class) " + value.getClass()
			//+  " =?" + found + " "  +  compValue + " (class) " + compValue.getClass() );
		}
		return found;
	}


	public boolean existsValueInCol(Object value, int col)
	{
		boolean found = false;
		int i = 0;
		while (!found && i < getRowCount())
		{
			Object compValue = getValueAt(i, col);

			if (compValue.equals(value))
				found = true;
			else
				i++;

			//logging.debug(this, "value  " +  value + " (class) " + value.getClass()
			//+  " =?" + found + " "  +  compValue + " (class) " + compValue.getClass() );
		}
		return found;
	}


	//interface TableModelFunctions

	protected void requestRefreshDerivedMaps()
	{
		functions = null;
		primarykey2Rowmap = null;
		primarykeyTranslation = null;
		primarykeyRepresentation = null;
		xFunctions = null;
	}

	public Map<Object, java.util.List<Object>> getFunction( int col1, int col2 )
	{
		return getFunction(col1, col2, null);
	}

	public Map<Object, java.util.List<Object>> getFunction( int col1, int col2,
	        TableModelFilterCondition specialFilterCondition)
	{
		TableModelFunctions.PairOfInt pair = new TableModelFunctions.PairOfInt(col1, col2);

		if (functions == null)
			functions = new HashMap<TableModelFunctions.PairOfInt, Map<Object, java.util.List<Object>>>();

		java.util.Map<Object, java.util.List<Object>> function = functions.get(pair);

		if (function == null)
		{
			function = buildFunction(pair.col1, pair.col2, specialFilterCondition);
		}

		return function;
	}

	public java.util.Map<Integer, RowStringMap> getPrimarykey2Rowmap()
	{
		if (keyCol < 0)
			return null;

		if (primarykey2Rowmap != null)
			return primarykey2Rowmap;

		primarykey2Rowmap = new HashMap<Integer, RowStringMap> ();

		for (int i = 0; i < rows.size(); i++)
		{
			Integer key = Integer.valueOf((String) getValueAt(i, keyCol));
			primarykey2Rowmap.put(key, getRowStringMap(i));
		}

		return primarykey2Rowmap;
	}


	public void setKeyRepresenter(de.uib.utilities.table.KeyRepresenter kr)
	{
		keyRepresenter = kr;
	}



	public java.util.Map<Integer, String> getPrimarykeyTranslation()
	{
		if (keyCol < 0)
			return null;

		if (keyRepresenter == null)
			return null;

		if (primarykeyTranslation != null)
			return primarykeyTranslation;

		primarykeyTranslation = new java.util.HashMap<Integer, String>();

		for (int i = 0; i < rows.size(); i++)
		{
			try
			{
				Integer key = Integer.valueOf((String) getValueAt(i, keyCol));
				//logging.debug(this, "getPrimarykeyTranslation() key " + key);
				//logging.debug(this, "getPrimarykeyTranslation()  getPrimarykey2Rowmap() " + getPrimarykey2Rowmap());


				String repr =


				    primarykeyTranslation.put(
				        key,
				        keyRepresenter.represents(key, getPrimarykey2Rowmap().get(key))
				    );
			}
			catch(Exception ex)
			{
				logging.info(this, "getPrimarykeyTranslation()  " + getValueAt(i, keyCol) +" " + ex);
			}
		}

		return primarykeyTranslation;
	}

	public de.uib.utilities.Mapping<Integer, String> getPrimarykeyRepresentation()
	{
		if (getPrimarykeyTranslation() == null)
			return null;

		if (primarykeyRepresentation != null)
			return primarykeyRepresentation;

		primarykeyRepresentation = new Mapping<Integer,String>(primarykeyTranslation);

		return primarykeyRepresentation;
	}


	public java.util.Map<Integer, Mapping<Integer, String>> getID2Mapping (int col1st, int col2nd , Mapping col2ndMapping)
	{
		TableModelFunctions.PairOfInt pair = new TableModelFunctions.PairOfInt(col1st, col2nd);

		if (xFunctions == null)
			xFunctions = new HashMap<TableModelFunctions.PairOfInt, Map<Integer, Mapping<Integer, String>>>();

		java.util.Map<Object, java.util.List<Object>> function = getFunction(col1st, col2nd);

		if (function == null)
			return null;

		java.util.Map<Integer, Mapping<Integer, String>> xFunction = xFunctions.get(pair);
		if (xFunction == null)
		{
			xFunction = new HashMap<Integer, Mapping<Integer, String>>();
			for (Object key : function.keySet())
			{
				Integer keyVal = (Integer) key;
				xFunction.put(keyVal,
				              col2ndMapping.restrictedTo(	new HashSet<Object>(  (java.util.List<Object>) function.get(key) ) )
				             );
			}
		}

		return xFunction;
	}


}


