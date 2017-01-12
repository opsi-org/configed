/*
 * SearchPane.java
 *
 * By uib, www.uib.de, 2011-2013
 * Author: Rupert Röder
 * 
 */

package de.uib.utilities.table.gui;

import de.uib.configed.configed;

import java.awt.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.util.*;
import java.util.regex.*;

import de.uib.utilities.swing.*;
import de.uib.utilities.table.*;
import de.uib.utilities.table.updates.*;

import de.uib.utilities.*;
import de.uib.utilities.logging.*;
//import de.uib.utilities.thread.*;
import utils.*;

public class SearchPane extends JPanel
	implements DocumentListener, KeyListener
{
	JTextField fieldSearch;
	
	private boolean searchActive = false;
	
	int blinkrate;
	JComboBox comboSearchFields;
	JComboBox comboSearchFieldsMode;
	
	JPopupMenu searchMenu;
	
	
	public static enum SearchMode {
		FULL_TEXT_SEARCHING_WITH_ALTERNATIVES,
		FULL_TEXT_SEARCHING_ONE_STRING,
		START_TEXT_SEARCHING,
		REGEX_SEARCHING
	}
		
		
	
	public final static int FULL_TEXT_SEARCH = 0;
	public final static int START_TEXT_SEARCH = 1;
	public final static int REGEX_SEARCH = 2;
	
	
	protected boolean withRegEx = true;
	protected boolean selectMode = true;
	
	private int foundrow = -1;
	//private int lastSearchTextLength = 0;
	JTable table;
	
	final Comparator comparator;
	Map<String, Mapping<Integer, String>> mappedValues; 
	
	public SearchPane(JTable table, boolean withRegEx)
	{
		comparator = Globals.getCollator();
		mappedValues = new HashMap<String, Mapping<Integer, String>>();
		this.withRegEx = withRegEx;
		initComponents();
		
		this.table = table;
	}
	
	public SearchPane(JTable table)
	{
		this(table, false);	
	}
	
	
	public void setMapping(String columnName, Mapping<Integer, String> mapping)
	{
		mappedValues.put(columnName,  mapping);
		//logging.debug(this, "mappedValues " + mappedValues);
	} 
	
	public void setSelectMode(boolean select)
	{
		this.selectMode = select;
	}
	
	
	protected void initComponents() 
	{
		setBackground(Globals.backgroundWhite);
		
		
		JLabel labelSearch = new JLabel(  configed.getResourceValue("SearchPane.search") );
		labelSearch.setFont(Globals.defaultFont);
		
		//JLabel labelSearch = new JLabel("Suche");
		//labelSearch.setFont(Globals.defaultFontStandardBold);
		//labelSearch.setPreferredSize(Globals.labelDimension);
		
		fieldSearch = new JTextField("");
		//fieldSearch.setPreferredSize(Globals.textfieldDimension);
		fieldSearch.setPreferredSize(Globals.buttonDimension);
		fieldSearch.setFont(Globals.defaultFontBig);
		fieldSearch.setBackground(Globals.backLightYellow);
		//blinkrate = fieldSearch.getCaret().getBlinkRate(); //save default blinkrate
		fieldSearch.getCaret().setBlinkRate(0);
		
		fieldSearch.getDocument().addDocumentListener(this);
		
		fieldSearch.addKeyListener(this);
		
		searchMenu = new JPopupMenu();
		JMenuItemFormatted popupSearch = new JMenuItemFormatted();
		JMenuItemFormatted popupSearchNext = new JMenuItemFormatted();
		JMenuItemFormatted popupNewSearch = new JMenuItemFormatted();
		JMenuItemFormatted popupMarkHits = new JMenuItemFormatted();
		JMenuItemFormatted popupEmptySearchfield = new JMenuItemFormatted();
		searchMenu.add(popupSearch);
		searchMenu.add(popupSearchNext);
		searchMenu.add(popupNewSearch);
		searchMenu.add(popupMarkHits);
		searchMenu.add(popupEmptySearchfield);
		
		popupSearch.setText("Suchen");
		popupSearch.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				searchTheRow(selectMode);
			}
		});
		
		//popupSearchNext.setText("Zum nächsten Treffer ( F3 ) ");
		popupSearchNext.setText( configed.getResourceValue("SearchPane.popup.searchnext") );

		popupSearchNext.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				searchNextRow(selectMode);
			}
		});
		
		//popupNewSearch.setText("Suche neu starten");
		popupNewSearch.setText( configed.getResourceValue("SearchPane.popup.searchnew") );
		
		popupNewSearch.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				searchTheRow(0, selectMode);
			}
		});
		
		//popupMarkHits.setText("Alle Treffer markieren ( F5 ) ");
		popupMarkHits.setText( configed.getResourceValue("SearchPane.popup.markall") );
		
		popupMarkHits.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				markAll();
			}
		});
		
		//popupEmptySearchfield.setText("Suchfeld leeren");
		popupEmptySearchfield.setText( configed.getResourceValue("SearchPane.popup.empty") );
		
		popupEmptySearchfield.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				fieldSearch.setText("");
			}
		});
		
		fieldSearch.setComponentPopupMenu(searchMenu);
		
		fieldSearch.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				searchNextRow(selectMode);
			}
		});
		
		
		//JLabel labelSearchMode = new JLabel("Modus");
		//labelSearchMode.setFont(Globals.defaultFontStandardBold);
		JLabel labelSearchMode = new JLabel( configed.getResourceValue("SearchPane.searchmode.searchmode") );
		labelSearchMode.setFont(Globals.defaultFont);
		
		comboSearchFieldsMode = new JComboBoxToolTip();
		comboSearchFieldsMode.setFont(Globals.defaultFont);
		
		LinkedHashMap tooltipsMap = new LinkedHashMap();
		/*
		tooltipsMap.put("Volltext", "Suchzeichenfolge irgendwo im Text finden");
		tooltipsMap.put("Anfangstext", "Suchzeichenfolge am Textbeginn finden");
		if (withRegEx) tooltipsMap.put("Schema", "Suchausdruck mit symbolischen Zeichen" );
		*/
		
		tooltipsMap.put(
			configed.getResourceValue("SearchPane.searchmode.fulltext"),
			configed.getResourceValue("SearchPane.mode.fulltext.tooltip")
			);
		tooltipsMap.put(
			configed.getResourceValue("SearchPane.mode.starttext"), 
			configed.getResourceValue("SearchPane.mode.starttext.tooltip")
			);
		if (withRegEx) 
			tooltipsMap.put(
				configed.getResourceValue("SearchPane.mode.regex"), 
				configed.getResourceValue("SearchPane.mode.regex.tooltip")
				);
		
		((JComboBoxToolTip)comboSearchFieldsMode).setValues(tooltipsMap, false);
		comboSearchFieldsMode.setSelectedIndex( START_TEXT_SEARCH );
		
		//comboSearchFieldsMode.setModel(new DefaultComboBoxModel(searchModes));
		comboSearchFieldsMode.setPreferredSize(Globals.lowerButtonDimension);
		
		
		
		//comboSearchFields = new JComboBox(new String[]{"alle Felder"});
		
		comboSearchFields = new JComboBox(new String[]{
			configed.getResourceValue("SearchPane.search.allfields")
		});
		comboSearchFields.setPreferredSize(Globals.lowerButtonDimension);
		comboSearchFields.setFont(Globals.defaultFont);
		
		
		
		GroupLayout layoutSearchPane = new GroupLayout(this);
		this.setLayout(layoutSearchPane);
			
		layoutSearchPane.setHorizontalGroup(
			layoutSearchPane.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layoutSearchPane.createSequentialGroup()
					.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
					.addComponent(fieldSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,  Short.MAX_VALUE)
					.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
					.addComponent(labelSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE)
					.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
					.addComponent(comboSearchFields, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
					.addComponent(labelSearchMode, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE)
					.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
					.addComponent(comboSearchFieldsMode, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
				)
			);
		
		layoutSearchPane.setVerticalGroup(
			layoutSearchPane.createSequentialGroup()
				//.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
				.addGroup(layoutSearchPane.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
					.addComponent(labelSearch, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(fieldSearch, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(labelSearchMode, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(comboSearchFieldsMode, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(comboSearchFields, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
				//.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
			);
		
		/*
		
		layoutSearchPane.setHorizontalGroup(
			layoutSearchPane.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layoutSearchPane.createSequentialGroup()
					.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
					.addComponent(labelSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE)
					.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
					.addComponent(fieldSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
					.addGap(Globals.hGapSize,Globals.hGapSize,Short.MAX_VALUE)
					.addComponent(labelSearchMode, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
					.addComponent(comboSearchFields, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
					.addComponent(comboSearchFieldsMode, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
				)
			);
		
		layoutSearchPane.setVerticalGroup(
			layoutSearchPane.createSequentialGroup()
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
				.addGroup(layoutSearchPane.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
					.addComponent(labelSearch, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(fieldSearch, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(labelSearchMode, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(comboSearchFieldsMode, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(comboSearchFields, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
			);
		*/
	}
	
	
	public void setSearchFields(Integer[] cols)
	{
		for (int j = 0; j < cols.length; j++)
		{
			comboSearchFields.addItem(table.getModel().getColumnName(cols[j]));
		}
	}
	
	public void setSearchFieldsAll()
	{
		for (int i = 0; i < table.getModel().getColumnCount(); i++)
		{
			String colname = table.getModel().getColumnName(i);
			comboSearchFields.addItem(colname);
			logging.info(this, "setSearchFieldsAll, adding colname " + colname);
		}
	}
		
	public void setSearchFields(java.util.List<String> fieldList)
	{
		for (String fieldName : fieldList)
		{
			if(  ( (DefaultComboBoxModel)comboSearchFields.getModel() ).getIndexOf(fieldName) == -1 )
				comboSearchFields.addItem(fieldName);
		}
	}
	
	public void setSelectedSearchField(String field)
	{
		comboSearchFields.setSelectedItem(field);
	}
	
	public void setSearchMode(int a)
	{
		if (a <= START_TEXT_SEARCH)
			comboSearchFieldsMode.setSelectedIndex(a);
		else
		{
			if (withRegEx)
				comboSearchFieldsMode.setSelectedIndex(REGEX_SEARCH);
		}
	}
	
	@Override
	public void requestFocus()
	{
		fieldSearch.requestFocus();
	}
	
	// search functions 
	//----------------------------------
	/*
	protected int findViewRowFromValue(int startviewrow, Object value, Set colIndices)
	{
		return findViewRowFromValue(startviewrow, value, colIndices, false, true);
	}
	
	protected int findViewRowFromValue(int startviewrow, Object value, Set colIndices, boolean fulltext)
	{
		return findViewRowFromValue(startviewrow, value, colIndices, fulltext, false, true);
	}
	*/
	
	
	
	private class Finding {
		boolean success = false;
		int startChar = -1;
		int endChar = -1;
	}
	
	
	
	private Finding stringContainsParts(final String colname, final String s, String[] parts)
	{
		String realS = s;
		
		if (mappedValues.get(colname) != null)
			realS = mappedValues.get(colname).getMapOfStrings().get(s);
		
		
		return stringContainsParts(realS, parts);
	}
	
		
	private Finding stringContainsParts(final String s, String[] parts)
	{
		Finding result = new Finding();
		
		String remainder = s;
		
		if (s == null)
			return result;
		
		if (parts == null)
			return result;
		
		
		int len = parts.length;
		if (len == 0)
		{
			result.success = true;
			return result;
		}
		
		int i = 0;
		boolean searching = true;
		Finding partSearch = new Finding();
		
		while (searching)
		{
			//logging.debug(this, "remainder " + remainder + " searching for " + parts[i]);
			partSearch = stringContains(remainder, parts[i]);
			if (partSearch.success)
			{
				i++;
				//look for the next part?
				if (i >= len) //all parts found
				{
					result.success = true;
					result.endChar = partSearch.endChar;
					searching = false;
				}
				else
				{	
					if (remainder.length() > 0)
						remainder = remainder.substring(partSearch.endChar);
					else
						result.success = false;
				}
			}
			else
			{
				result.success = false;
				searching = false;
			}
		}
		
		return result;
	}
				
	private Finding stringContains(final String s, final String part)
	{
		return stringContains(null, s, part);
	}
	
	private Finding stringContains(final String colname, final String s, final String part)
	{
		Finding result = new Finding();
		
		if (s == null)
			return result;
		
		if (part == null)
			return result;
		
		
		String realS = s;
		
		if (colname != null && mappedValues.get(colname) != null)
			realS = mappedValues.get(colname).getMapOfStrings().get(s);
		
		//logging.debug(this, " realS " + realS);
		
		if (realS == null || part.length() > realS.length())
			return result;
		
		if (part.length() == 0)
		{
			result.success = true;
			result.endChar = 0;
			return result;
		}
		
		result.success =false;
		
		int i = 0;
		result.startChar = 0;
		
		int end = realS.length() - part.length() + 1; 
		
		while (!result.success && i < end)
		{
			result.startChar = i;
			result.success = (comparator.compare(realS.substring(i, i + part.length()), part) == 0);
			result.endChar = i + part.length() - 1;
			i++;
		}
		
		return result;
	}

	private boolean stringStartsWith(final String colname, final String s, final String part)
	{
		if (s == null)
			return false;
		
		if (part == null)
			return false;
		
		String realS = s;
		if (mappedValues.get(colname) != null)
			realS = mappedValues.get(colname).getMapOfStrings().get(s);
		
		if (part.length() > realS.length())
			return false;
		
		if (part.length() == 0)
			return true;
		
		return  ( comparator.compare( realS.substring(0, part.length()), part) == 0);
	}
	
	
	protected int findViewRowFromValue(int startviewrow, Object value, Set colIndices, boolean fulltext, boolean regex, boolean combineCols)
	{
	
		logging.debug(this, "findViewRowFromValue(int startviewrow, Object value, Set colIndices, boolean fulltext, boolean regex): " 
			+ startviewrow + ", " + value + ", " + colIndices 
			+ ", " + fulltext + ", " + regex
			+ ", " + combineCols );
		
		
		
		if (value == null)
			return -1;
		
		String val = value.toString().toLowerCase();
		
		if (val.length() < 2)
			return -1;
		// dont start searching for single chars
		
		String[] valParts = val.split(" ");
		
		//String valLower = val.toLowerCase();
		
		boolean found = false;
		
		int viewrow = 0;
		
		if (startviewrow > 0)
			viewrow = startviewrow;
		
		Pattern pattern = null; 
		if (regex)
		{
			try{
				if (fulltext) 
					val = ".*" + val + ".*";
				pattern = Pattern.compile(val);
			}
			catch(java.util.regex.PatternSyntaxException ex)
			{
				logging.info(this, "pattern problem " +ex);
				return -1;
			}
		}
			
		while (!found && viewrow < table.getModel().getRowCount())
		{
			if (combineCols) //only fulltext
			{
				StringBuffer buffRow = new StringBuffer();
				
				for (int j=0; j< table.getModel().getColumnCount(); j++)
				{
					
					if (
						colIndices !=null //we dont compare all values (comparing all values is default)
						&&
						!colIndices.contains(j)
					)
						continue;
					
					int colJ = table.convertColumnIndexToModel(j);
					
						
					Object valJ = 
						table.getModel().getValueAt(
	
							table.convertRowIndexToModel(viewrow),
							colJ
							
							);
						
					if (valJ != null)
					{
						String valSJ = ("" + valJ).toLowerCase();
						
						String colname = table.getModel().getColumnName(colJ);
						
						if (mappedValues.get(colname) != null)
							valSJ = mappedValues.get(colname).getMapOfStrings().get(valSJ);
						
						buffRow.append(valSJ);
					}
				}
				
				String compareVal = buffRow.toString();
				
				if (compareVal.equals(""))
				{
					if (val.equals(""))
						found = true;
				}
				else
				{
					found = stringContainsParts(compareVal, valParts).success;
				}
					
			}
				
			else
			{
				for (int j=0; j< table.getModel().getColumnCount(); j++)
				{
					
					if (
						colIndices !=null //we dont compare all values (comparing all values is default)
						&&
						!colIndices.contains(j)
					)
					
					//if (j != 0) //test
						continue;
						
					int colJ = table.convertColumnIndexToModel(j); 
						
					Object compareValue = 
						table.getModel().getValueAt(
	
							table.convertRowIndexToModel(viewrow),
							colJ
							
							);
					
					//logging.info(this, "findViewRowFromValue compare colJ " + colJ + " value " + value + " to " + compareValue);
					
					if 	(compareValue == null)
					{
						if (val.equals(""))
							found = true;
					}
					
					else
					{
						String compareVal = ("" + compareValue).toLowerCase();
						
						if (regex)
						{
							//logging.info(this, " try to match " + value + " with " + compareVal);
							if ( pattern.matcher(compareVal).matches() )
								found = true;
							//logging.info(this, " try to match " + value + " with " + compareVal + " found " + found);
						}
						
						else
						{
							if (fulltext)
								found = stringContainsParts(
									table.getModel().getColumnName(colJ),
									compareVal, valParts).success;
								
							/*if (fulltext)
								found = stringContains(
									table.getModel().getColumnName(colJ),
									compareVal, val);
							*/
									
							else
							{
								//logging.info(this, "findViewRowFromValue  not fullltext, startsWith  ");
								found = stringStartsWith(
									table.getModel().getColumnName(colJ),
									compareVal, val);
								//logging.info(this, "findViewRowFromValue  not fullltext,  found  " + found);
							}
							
							/*
							without collator based comparison
							compareVal = compareVal.toLowerCase();
							
							if (fulltext)
							{
								
								if (compareVal.indexOf(val.toLowerCase()) >= 0)
									found = true;
							}
								
							else
							{
	
								if (compareVal.startsWith(valLower))
									found = true;
							}
							*/
						}
					}
					
					if (found)
					{
						//logging.info(this, "findViewRowFromValue identified " + value );
						break;
					}
				}
					
				
			}
				
			if (!found)
				viewrow++;
			
		}
		
		//logging.debug(this, " findViewRowFromValue, found " + found);
		
		if (found)
		{
			return viewrow; 
		}
		
		return -1;
	}	
	
	
	private void getSelectedAndSearch(boolean select)
	{
		getSelectedAndSearch(false, select);
	}
	
	public boolean isSearchActive()
	{
		return searchActive;
	}
	
	public void markAll()
	{
		
		table.clearSelection();
		searchTheRow(0, true);
		int startFoundrow = foundrow;
		//logging.debug(this, "markAll foundrow " + foundrow);
		foundrow = foundrow + 1;
		while (foundrow > startFoundrow)
		{
			getSelectedAndSearch(true, true);  //adding the next row to selection
			//logging.debug(this, "markAll foundrow " + foundrow);
		}
		
	}
	
	private void searchNextRow(boolean select)
	{
		foundrow++;
		searchTheRow(foundrow, false, select);
	}
	
	private void getSelectedAndSearch(boolean addSelection, boolean select)
	{
		
		int startrow = 0;
		if (table.getSelectedRow() >= 0)
			startrow = table.getSelectedRows()[table.getSelectedRows().length-1] + 1;
		
		if (startrow >= table.getRowCount())
			startrow = 0;
		
		searchTheRow(startrow, addSelection, select);
		
		if (foundrow == -1)
			searchTheRow(0, addSelection, select);
		
	}
	
	private void searchTheRow(boolean select)
	{
		searchTheRow(table.getSelectedRow(), select);
	}
	
	private void searchTheRow(int startrow, boolean select)
	{
		searchTheRow(startrow, false, select);
	}
	
	private void setRow(int row, boolean addSelection, boolean select)
	{
		if (select)
		{
			if (addSelection)
				addSelectedRow(row);
			else
				setSelectedRow(row);
		}
		else //make only visible
		{
			table.scrollRectToVisible(table.getCellRect(row, 0, false));
		}
	
	}	
	
	private void searchTheRow(final int startrow, final boolean addSelection, final boolean select)
	{
		
		final String value = fieldSearch.getText();
		
		HashSet<Integer> selectedCols0 = null;
		
		if (comboSearchFields.getSelectedIndex() > 0)
		{
			selectedCols0 = new HashSet<Integer>();
			selectedCols0.add(((AbstractTableModel) table.getModel()).findColumn((String) comboSearchFields.getSelectedItem()));
		}
		
		final HashSet<Integer> selectedCols = selectedCols0;
		
			
		//logging.debug(this, "searchTheRow startrow " +  startrow);
		//logging.debug(this, "markAll foundrow " + foundrow);
		
		final boolean fulltextSearch = (comboSearchFieldsMode.getSelectedIndex() == FULL_TEXT_SEARCH);
		final boolean regexSearch = (comboSearchFieldsMode.getSelectedIndex() == REGEX_SEARCH);
		final boolean combineCols = fulltextSearch;
		
		//fieldSearch.getCaret().setBlinkRate(blinkrate);
		
		//final WaitCursor waitCursor = new WaitCursor(fieldSearch, "SearchPane");
		
		
		//new Thread(){  //destroys search of all 
		//	public void run()
			{
				
				foundrow = findViewRowFromValue(startrow, value, selectedCols, fulltextSearch, regexSearch, combineCols);
				
				//logging.debug(this, "searchTheRow foundrow " +  foundrow);
				
				if (foundrow > -1)
				{
					setRow(foundrow, addSelection, select);
				}
				else
				{
					if (startrow > 0)
					{
						//setRow(0, false, select);
						searchTheRow(0, addSelection, select);
					}
					else
						setRow(0, false, select);
				}
				
				//waitCursor.stop();
				
				//fieldSearch.getCaret().setBlinkRate(0);
			}
		//}.start();
			
	}
	
	public void scrollRowToVisible(int row)
	{
		table.scrollRectToVisible(table.getCellRect(row, 0, false));
	}

	public void addSelectedRow(int row)
	{
		if (table.getRowCount() == 0)
			return;
		
		table.addRowSelectionInterval(row, row); 
		//System.out.println(" --- view row selected " + row);
		table.scrollRectToVisible(table.getCellRect(row, 0, false));
	}
	
	public void setSelectedRow(int row)
	{
		if (table.getRowCount() == 0)
			return;
		
		if (row == -1)
		{
			table.clearSelection();
			return;
		}
		
		table.setRowSelectionInterval(row, row); 
		//System.out.println(" --- view row selected " + row);
		table.scrollRectToVisible(table.getCellRect(row, 0, false));
	}
	
	
	
	/*
	private void saveLastSearchTextLength()
	{
		lastSearchTextLength = fieldSearch.getText().length();
	}
	*/
	
	//----------------------------------
	
	// DocumentListener interface
	public void changedUpdate(DocumentEvent e)
	{
		//logging.debug(this, "changedUpdate");
		if (e.getDocument() == fieldSearch.getDocument())
		{
			searchTheRow(selectMode);
		}
			
	}
    public void insertUpdate(DocumentEvent e)
	{
		//logging.debug(this, "insertUpdate");
		if (e.getDocument() == fieldSearch.getDocument())
		{
			searchTheRow(selectMode);
		}
		
	}
    public void removeUpdate(DocumentEvent e)
	{
		//logging.debug(this, "removeUpdate");
		if (e.getDocument() == fieldSearch.getDocument())
		{
			setRow(0, false, selectMode);
			//go back to start when editing is restarted
			
			/*
			if (fieldSearch.getText().equals(""))
				//setSelectedRow(0);
				setRow(0, false, selectMode);
			
			else
			{
				setRow(0, false, selectMode);
				searchTheRow(0, selectMode);
			}
			*/
		}
		
	}
	
	//KeyListener interface
	public void keyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_F5)
		{
			if (!fieldSearch.getText().equals("")) 
				markAll();
		}
		else if ( e.getKeyCode()== KeyEvent.VK_F3)
		{
			if (!fieldSearch.getText().equals(""))
					searchNextRow(selectMode);
		}
	}
	
	public void keyReleased(KeyEvent e){}
	
	public void keyTyped(KeyEvent e){}
	
	
	public static void main (String[] args)
	{
		/*
		System.out.println(" abc   contains äb " +  stringContains("abc", "äb"));
		System.out.println(" abcde  contains  è " +  stringContains("abcde", "é"));
		System.out.println(" abc  contains  c " +  stringContains("abc", "'"));
		
			
		System.out.println(" abc  starts with  ab " +  stringStartsWith("abc", "ab"));
		System.out.println(" abc  starts with  a " +  stringStartsWith("abc", "a"));
		System.out.println(" abc  starts with abc " +  stringStartsWith("abc", "abc"));
		*/
	}
	
	
}
	