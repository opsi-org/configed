/*
 * PanelGenEditTable.java
 *
 * By uib, www.uib.de, 2008-2015
 * Author: Rupert Röder
 * 
 */

package de.uib.utilities.table.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import java.util.*;

import de.uib.utilities.pdf.DocumentToPdf;
import de.uib.utilities.swing.*;
import de.uib.utilities.table.*;
import de.uib.utilities.table.updates.*;
import de.uib.utilities.Globals;
import de.uib.configed.configed;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.thread.WaitCursor;

/**
 *
 * @author  roeder
 */
public class PanelGenEditTable extends JPanel
			implements
			ActionListener,
			TableModelListener,
			ListSelectionListener,
			KeyListener,
			MouseListener,
			ComponentListener
{
	protected Comparator[] comparators;

	protected javax.swing.JScrollPane scrollpane;
	protected javax.swing.JTable theTable;
	protected de.uib.utilities.table.GenTableModel tableModel;
	//protected javax.swing.JButton jButtonCommit; standard version
	//protected javax.swing.JButton jButtonCancel;
	protected de.uib.configed.gui.IconButton buttonCommit;
	protected de.uib.configed.gui.IconButton buttonCancel;
	protected javax.swing.JLabel label;

	protected javax.swing.JLabel labelRowCount;
	protected javax.swing.JLabel labelMarkedCount;
	protected String textMarkedCount = "selected";

	private JTextField celleditorfield;

	//protected Color backgroundColor = new Color(250, 250, 240);//Globals.backLightYellow;
	protected Color backgroundColorEditFieldsSelected =  Globals.defaultTableCellSelectedBgColor;// new Color(206, 224, 235);// Color.white; //Globals.backLightYellow;//backLightGreen;
	protected Color backgroundColorSelected = Globals.defaultTableCellSelectedBgColorNotEditable;

	protected JPopupMenu popupMenu;

	protected boolean dataChanged = false;

	protected UpdateController myController;

	protected int maxTableWidth = Short.MAX_VALUE;

	protected boolean editing = true;

	protected boolean switchLineColors = true;

	protected boolean awareOfSelectionListener = false;
	protected boolean followSelectionListener = true;


	protected boolean withSearchPane = false;

	protected SearchPane searchPane;


	protected String title = "";

	protected int generalPopupPosition = 0;

	protected int popupIndex = 0;

	private int oldrowcount = -1;

	protected LinkedHashMap<Integer, SortOrder> sortDescriptor;
	protected LinkedHashMap<Integer, SortOrder> specialSortDescriptor;

	protected DocumentToPdf tableToPDF;
	//public ExportTable exportTable;

	public static final int POPUP_DELETE_ROW = 1;
	public static final int POPUP_SAVE = 2;
	public static final int POPUP_CANCEL = 3;
	public static final int POPUP_RELOAD = 4;
	public static final int POPUP_SORT_AGAIN = 5;
	public static final int POPUP_PRINT = 6;
	public static final int POPUP_EXPORT_EXCEL = 7;
	public static final int POPUP_EXPORT_SELECTED_EXCEL = 8;
	public static final int POPUP_EXPORT_CSV = 9;
	public static final int POPUP_EXPORT_SELECTED_CSV = 10 ;
	public static final int POPUP_NEW_ROW = 11;
	public static final int POPUP_COPY_ROW = 12;
	public static final int POPUP_FLOATINGCOPY = 13;
	public static final int POPUP_PDF = 14;

	public static final int[] POPUPS_EDITABLE_TABLE =
	    new int[]{
	        POPUP_NEW_ROW,
	        POPUP_COPY_ROW,
	        POPUP_DELETE_ROW,
	        POPUP_SAVE,
	        POPUP_CANCEL,
	        POPUP_EXPORT_SELECTED_EXCEL,
	        POPUP_EXPORT_EXCEL,
	        POPUP_RELOAD,
	        POPUP_SORT_AGAIN
	        //,
	        //POPUP_PDF 
	    };
	public static final int[] POPUPS_EDITABLE_TABLE_PRINTABLE =
	    new int[]{
	        POPUP_NEW_ROW,
	        POPUP_COPY_ROW,
	        POPUP_DELETE_ROW,
	        POPUP_SAVE,
	        POPUP_CANCEL,
	        POPUP_PRINT,
	        POPUP_PDF,
	        POPUP_EXPORT_SELECTED_EXCEL,
	        POPUP_EXPORT_EXCEL,
	        POPUP_RELOAD,
	        POPUP_SORT_AGAIN
	        //,
	        //POPUP_PDF 
	    };
	public static final int[] POPUPS_NOT_EDITABLE_TABLE =
	    new int[]{
	        POPUP_EXPORT_SELECTED_EXCEL,
	        POPUP_EXPORT_EXCEL,
	        POPUP_RELOAD,
	        POPUP_SORT_AGAIN
	        //,
	        //POPUP_PDF 
	    };
	public static final int[] POPUPS_NOT_EDITABLE_TABLE_PRINTABLE =
	    new int[]{
	        POPUP_PRINT,
	        POPUP_PDF,
	        POPUP_EXPORT_SELECTED_EXCEL,
	        POPUP_EXPORT_EXCEL,
	        POPUP_RELOAD,
	        POPUP_SORT_AGAIN
	        //,
	        //POPUP_PDF 
	    };
	public static final int[] POPUPS_MINIMAL =
	    new int[]{
	        POPUP_RELOAD,
	        POPUP_SORT_AGAIN
	        //,
	        //POPUP_PDF 
	        //POPUP_FLOATINGCOPY

	    };

	protected java.util.List<Integer> popups;

	JMenuItemFormatted menuItemDeleteRelation;
	JMenuItemFormatted menuItemSave;
	JMenuItemFormatted menuItemCancel;
	JMenuItemFormatted menuItemReload;
	JMenuItemFormatted menuItemSortAgain;
	JMenuItemFormatted menuItemPrint;
	JMenuItemFormatted menuItemExportExcel;
	JMenuItemFormatted menuItemExportSelectedExcel;
	JMenuItemFormatted menuItemExportCSV;
	JMenuItemFormatted menuItemExportSelectedCSV;
	JMenuItemFormatted menuItemNewRow;
	JMenuItemFormatted menuItemCopyRelation;
	JMenuItemFormatted menuItemFloatingCopy;
	JMenuItemFormatted menuItemPDF;

	protected Vector<JMenuItem> menuItemsRequesting1SelectedLine;
	protected Vector<JMenuItem> menuItemsRequestingMultiSelectedLines;

	private boolean separatorAdded = false;


	public PanelGenEditTable(String title, int maxTableWidth,
	                         boolean editing, int generalPopupPosition, boolean switchLineColors, int[] popups,
	                         boolean withSearchPane)
	{
		this.withSearchPane = withSearchPane;

		menuItemsRequesting1SelectedLine = new Vector<JMenuItem> ();
		menuItemsRequestingMultiSelectedLines = new Vector<JMenuItem>();


		this.generalPopupPosition = generalPopupPosition;
		if (popups != null)
		{
			this.popups = new ArrayList<Integer>();
			for (int j = 0; j < popups.length; j++)
				this.popups.add(popups[j]);
		}
		else
		{
			this.popups = new ArrayList<Integer>();
			this.popups.add(POPUP_RELOAD);
			//this.popups.add(POPUP_PRINT);
			this.popups.add(POPUP_PDF);
		}

		if (maxTableWidth > 0)
			this.maxTableWidth = maxTableWidth;
		if (title != null)
			this.title = title;
		this.editing = editing;
		
		if (!de.uib.configed.Globals.isServerFullPermission())
			this.editing = false;

		this.switchLineColors = switchLineColors;

		initComponents();
		if (generalPopupPosition == 0) addPopupStandard();
		//if -1 dont use a standard popup
		//if > 0 the popup is added later after installing another popup
	}

	public PanelGenEditTable(String title, int maxTableWidth,
	                         boolean editing, int generalPopupPosition, boolean switchLineColors, int[] popups)
	{
		this(title,  maxTableWidth, editing, generalPopupPosition, switchLineColors, popups, false);
	}

	public PanelGenEditTable(String title, int maxTableWidth, boolean editing, int generalPopupPosition, boolean switchLineColors)
	{
		this(title, maxTableWidth, editing, generalPopupPosition, switchLineColors, null);
	}

	public PanelGenEditTable(String title, int maxTableWidth, boolean editing, int generalPopupPosition){
		this(title, maxTableWidth, editing, generalPopupPosition, false);
	}

	public PanelGenEditTable(String title, int maxTableWidth, boolean editing){
		this(title, maxTableWidth, editing, 0);
	}

	public PanelGenEditTable(String title, int maxTableWidth){
		this(title, maxTableWidth, true);
	}

	public PanelGenEditTable(int maxTableWidth){
		this("", maxTableWidth);
	}

	public PanelGenEditTable(){
		this(0);
	}

	//Override in subclasses; delegates to table header renderer
	protected Object modifyHeaderValue(Object value)
	{
		return value;
	}


	public void requestFocus()
	{
		if (theTable != null)
			theTable.requestFocus();

		if (withSearchPane)
			searchPane.requestFocus();
	}

	public void setAutoResizeMode(int mode)
	// mode are JTable constants:  - One of 5 legal values: AUTO_RESIZE_OFF, AUTO_RESIZE_NEXT_COLUMN, AUTO_RESIZE_SUBSEQUENT_COLUMNS, AUTO_RESIZE_LAST_COLUMN, AUTO_RESIZE_ALL_COLUMNS
	{
		theTable.setAutoResizeMode(mode);
	}


	public void setUpdateController(UpdateController c)
	{
		myController = c;
	}

	public void addListSelectionListener(ListSelectionListener l)
	{
		getListSelectionModel().addListSelectionListener(l);
	}

	public void removeListSelectionListener(ListSelectionListener l)
	{
		getListSelectionModel().removeListSelectionListener(l);
	}

	protected void initComponents()
	{
		setBackground(Globals.backgroundWhite);

		addComponentListener(this);

		buttonCommit = new de.uib.configed.gui.IconButton(
		                   de.uib.configed.configed.getResourceValue("PanelGenEditTable.SaveButtonTooltip") ,
		                   "images/apply.png",
		                   "images/apply_over.png",
		                   "images/apply_disabled.png");
		//new javax.swing.JButton("save");
		buttonCommit.setPreferredSize(Globals.smallButtonDimension);
		if (!editing)
			buttonCommit.setVisible(false);

		buttonCancel = new de.uib.configed.gui.IconButton(
		                   de.uib.configed.configed.getResourceValue("PanelGenEditTable.CancelButtonTooltip") ,
		                   "images/cancel.png",
		                   "images/cancel_over.png",
		                   "images/cancel_disabled.png");
		//new javax.swing.JButtonCancel("cancel");
		buttonCancel.setPreferredSize(Globals.smallButtonDimension);
		if (!editing)
			buttonCancel.setVisible(false);

		buttonCommit.addActionListener(this);
		buttonCancel.addActionListener(this);
		setDataChanged(false);

		label = new JLabel(title);
		label.setFont(Globals.defaultFontStandardBold);
		if (title == null || title.equals(""))
			label.setVisible(false);

		labelRowCount = new JLabel(title);
		labelRowCount.setFont(Globals.defaultFontStandardBold);

		labelMarkedCount = new JLabel("");
		labelMarkedCount.setFont(Globals.defaultFontStandard);





		//popupMenu = new JPopupMenu();
		theTable = new de.uib.utilities.table.JTableWithToolTips();
		//new de.uib.utilities.table.JTableWithContextMenu(popupMenu);



		searchPane = new SearchPane(theTable, true);

		searchPane.setVisible(withSearchPane);


		theTable.getTableHeader().addMouseListener(this);



		// add the popup to the scrollpane for the case that the table is empty
		scrollpane = new javax.swing.JScrollPane();
		//scrollpane.addMouseListener(new utils.PopupMouseListener(popupMenu)); DOES NOT WORK


		if (switchLineColors)
			theTable.setDefaultRenderer(Object.class, new StandardTableCellRenderer());
		else
			theTable.setDefaultRenderer(Object.class, new ColorTableCellRenderer());

		theTable.addMouseListener(this);


		theTable.setShowHorizontalLines(true);
		theTable.setGridColor(Color.WHITE);


		theTable.getTableHeader().setDefaultRenderer
		(
		    new ColorHeaderCellRenderer(theTable.getTableHeader().getDefaultRenderer())
		    {

			    @Override
			    protected Object modifyValue(Object value)
			    {
				    return modifyHeaderValue(value);
			    }


		    }

		);

		;

		//new javax.swing.JTable();
		// we prefer the simple behaviour:
		theTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		theTable.getTableHeader().setReorderingAllowed(false);

		theTable.addKeyListener(this);


		getListSelectionModel().addListSelectionListener(this);

		theTable.setDragEnabled(true);
		theTable.setDropMode(DropMode.ON);

		theTable.setAutoCreateRowSorter(false);

		scrollpane = new javax.swing.JScrollPane();


		scrollpane.setViewportView(theTable);
		scrollpane.getViewport().setBackground(de.uib.utilities.Globals.backLightBlue);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);

		layout.setHorizontalGroup(
		    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		    .addGroup(layout.createSequentialGroup()
		              .addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
		              .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		                        .addGroup(layout.createSequentialGroup()
		                                  .addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
		                                  .addComponent(label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE,javax.swing.GroupLayout.PREFERRED_SIZE)
		                                 )
		                        .addComponent(searchPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE,Short.MAX_VALUE)
		                        .addComponent(scrollpane, javax.swing.GroupLayout.DEFAULT_SIZE, 100, maxTableWidth)
		                        .addGroup(layout.createSequentialGroup()
		                                  .addComponent(buttonCommit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		                                  .addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
		                                  .addComponent(buttonCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		                                 )
		                       )
		              .addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
		             )
		);

		layout.setVerticalGroup(
		    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		    .addGroup(layout.createSequentialGroup()
		              .addContainerGap()
		              .addComponent(label)
		              .addGap(Globals.vGapSize/2, Globals.vGapSize/2, Globals.vGapSize/2)
		              .addComponent(searchPane)
		              .addGap(Globals.vGapSize/2, Globals.vGapSize/2, Globals.vGapSize/2)
		              .addComponent(scrollpane, 20, 100, Short.MAX_VALUE)
		              .addGap(Globals.vGapSize/2, Globals.vGapSize/2, Globals.vGapSize/2)
		              .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
		                        .addComponent(buttonCommit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		                        .addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
		                        .addComponent(buttonCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		                       )
		              .addGap(Globals.vGapSize/2, Globals.vGapSize/2, Globals.vGapSize/2)
		             )
		);

	}

	public void setColumnSelectionAllowed(boolean b)
	//destroys search function
	{
		theTable.setColumnSelectionAllowed(b);
	}

	public void setRowSelectionAllowed(boolean b)
	{
		theTable.setRowSelectionAllowed(b);
	}



	public void sortAgainAsConfigured()
	{
		logging.debug(this, "sortAgainAsConfigured " + specialSortDescriptor);

		if (specialSortDescriptor != null && specialSortDescriptor.size() > 0)
			sortDescriptor = specialSortDescriptor;

		if (sortDescriptor != null && sortDescriptor.size() > 0 )
		{
			int selRow = getSelectedRow();

			Object selVal = null;
			if (selRow > -1 && tableModel.getKeyCol() > -1)
			{
				selVal = tableModel.getValueAt(
				             theTable.convertRowIndexToModel(selRow), tableModel.getKeyCol()
				         );
			}

			setSortOrder(sortDescriptor);
			((javax.swing.DefaultRowSorter)getRowSorter()).sort();
			setSorter();

			if (selVal != null)
			{
				int viewRow = findViewRowFromValue(selVal, tableModel.getKeyCol());
				moveToRow(viewRow);
				setSelectedRow(viewRow);
			}
		}
	}

	public void requestReload()
	{
		tableModel.requestReload();
	}

	/*
	reproduces data from source
	if reload is requested data are loaded completely new
	*/
	public void reset()
	{
		tableModel.reset();
	}

	public void reload()
	{
		logging.info(this, "reload()");
		WaitCursor waitCursor = new WaitCursor(this);
		tableModel.requestReload();
		tableModel.reset();
		setDataChanged(false);
		waitCursor.stop();
	}

	public void setTitle(String title)
	{
		logging.info(this, "setTitle " + title);
		this.title = title;
		label.setText(title);
		//labelRowCount.setText(title + tableModel.getRowCount());

	}

	protected void addPopupStandard()
	{
		if (generalPopupPosition > 0)
			popupMenu.addSeparator();

		Iterator iter = popups.iterator();
		while (iter.hasNext())
		{

			int popuptype = (Integer) iter.next();
			//System.out.println ("....... popuptype " + popuptype);
			switch (popuptype)
			{
			case POPUP_SAVE:
				menuItemSave = new JMenuItemFormatted(configed.getResourceValue("PanelGenEditTable.saveData"));
				menuItemSave.setEnabled(false);
				menuItemSave.addActionListener(new ActionListener(){
					                               public void actionPerformed(ActionEvent e)
					                               {
						                               commit();
					                               }
				                               });
				addPopupItem(menuItemSave);

				break;

			case POPUP_CANCEL:
				menuItemCancel = new JMenuItemFormatted(configed.getResourceValue("PanelGenEditTable.abandonNewData"));
				menuItemCancel.setEnabled(false);
				menuItemCancel.addActionListener(new ActionListener(){
					                                 public void actionPerformed(ActionEvent e)
					                                 {
						                                 cancel();
					                                 }
				                                 });
				addPopupItem(menuItemCancel);

				break;


			case POPUP_RELOAD:
				menuItemReload = new JMenuItemFormatted(configed.getResourceValue("PanelGenEditTable.reload"),
				                                        de.uib.configed.Globals.createImageIcon("images/reload16.png", ""));
				//menuItemReload.setPreferredSize(Globals.buttonDimension);
				//menuItemReload.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0)); does not work
				menuItemReload.addActionListener(new ActionListener(){
					                                 public void actionPerformed(ActionEvent e)
					                                 {
						                                 reload();
					                                 }
				                                 });
				if (popupIndex > 1)
					popupMenu.addSeparator();
				addPopupItem(menuItemReload);

				break;


			case POPUP_SORT_AGAIN:
				menuItemSortAgain = new JMenuItemFormatted(configed.getResourceValue("PanelGenEditTable.sortAsConfigured"));
				menuItemSortAgain.addActionListener(new ActionListener(){
					                                    public void actionPerformed(ActionEvent e)
					                                    {
						                                    sortAgainAsConfigured();

					                                    }
				                                    });
				//if (sortDescriptor != null)
				addPopupItem(menuItemSortAgain);

				break;

			case POPUP_DELETE_ROW:
				menuItemDeleteRelation = new JMenuItemFormatted(configed.getResourceValue("PanelGenEditTable.deleteRow"));
				menuItemDeleteRelation.setEnabled(false);
				menuItemDeleteRelation.addActionListener(new ActionListener(){
					        public void actionPerformed(ActionEvent e)
					        {
						        if (getSelectedRowCount() == 0)
						        {
							        JOptionPane.showMessageDialog( de.uib.configed.Globals.mainContainer,
							                                       configed.getResourceValue("PanelGenEditTable.noRowSelected"),
							                                       configed.getResourceValue("ConfigedMain.Licences.hint.title"),
							                                       JOptionPane.OK_OPTION);

							        return;
						        }
						        else
						        {
							        tableModel.deleteRow(getSelectedRowInModelTerms());
						        }
					        }
				        });
				addPopupItem(menuItemDeleteRelation);

				break;

			case POPUP_PRINT:
				menuItemPrint = new JMenuItemFormatted(configed.getResourceValue("PanelGenEditTable.print"));
				menuItemPrint.addActionListener(new ActionListener(){
					                                public void actionPerformed(ActionEvent e)
					                                {
						                                try
						                                {
							                                theTable.print();
						                                }
						                                catch (Exception ex)
						                                {
							                                logging.debugOut(logging.LEVEL_ERROR, "printing error " + ex);
						                                }
					                                }
				                                });

				/*
				if (popupIndex > 1)
					popupMenu.addSeparator();
				*/

				addPopupItem(menuItemPrint);

				break;


			case POPUP_FLOATINGCOPY:

				menuItemFloatingCopy = new JMenuItemFormatted(configed.getResourceValue("PanelGenEditTable.floatingCopy"));
				menuItemFloatingCopy.addActionListener(new ActionListener(){
					                                       public void actionPerformed(ActionEvent e)
					                                       {
						                                       floatExternal();
					                                       }
				                                       })
				;


				if (popupIndex > 1)
					popupMenu.addSeparator();


				addPopupItem(menuItemFloatingCopy);
				break;

			case POPUP_PDF:
				menuItemPDF = new JMenuItemFormatted(configed.getResourceValue("FGeneralDialog.pdf"),
				                                     de.uib.configed.Globals.createImageIcon("images/acrobat_reader16.png", ""));
				menuItemPDF.addActionListener(new ActionListener(){
					                              public void actionPerformed(ActionEvent e)
					                              {
						                              try
						                              {
							                              HashMap<String, String> metaData = new HashMap<String, String>();
							                              metaData.put("header", title);
							                              metaData.put("subject", "report of table");
							                              metaData.put("keywords", "");
							                              tableToPDF = new DocumentToPdf (null, metaData); //  no filename, metadata

							                              tableToPDF.createContentElement("table", theTable);

							                              tableToPDF.setPageSizeA4_Landscape();  //
							                              tableToPDF.toPDF(); //   create Pdf
						                              }
						                              catch (Exception ex)
						                              {
							                              logging.debugOut(logging.LEVEL_ERROR, "pdf printing error " + ex);
						                              }
					                              }
				                              });
				/*
				if (popupIndex > 1)
					popupMenu.addSeparator();
				*/

				addPopupItem(menuItemPDF);

				break;
			}
		}
	}

	public void addPopupItem (JMenuItem item)
	{
		if (item == null)
			return;

		if (popupMenu == null)
		{
			//for the first item, we create the menu
			popupMenu = new JPopupMenu();
			theTable.addMouseListener(new utils.PopupMouseListener(popupMenu));

			// add the popup to the scrollpane if the table is empty
			scrollpane.addMouseListener(new utils.PopupMouseListener(popupMenu));
		}

		popupMenu.add(item);

		popupIndex++;
		if (popupIndex == generalPopupPosition)
			addPopupStandard();


	}


	public void setSortOrder(LinkedHashMap<Integer,SortOrder> sortDescriptor)
	{
		this.sortDescriptor = sortDescriptor;
		//setSorter();
	}

	protected java.util.List <RowSorter.SortKey> buildSortkeysFromColumns()
	{
		logging.debug(this, "buildSortkeysFromColumns,  sortDescriptor " + sortDescriptor);
		java.util.List <RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();

		if (tableModel.getColumnCount() == 0)
			return null;

		else if (sortDescriptor == null)
			//default sorting
		{
			sortDescriptor = new LinkedHashMap<Integer, SortOrder>();

			if (tableModel.getKeyCol() > -1)
			{
				try
				{
					//sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
					//sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
					sortKeys.add(new RowSorter.SortKey(tableModel.getKeyCol(), SortOrder.ASCENDING));

					sortDescriptor.put(tableModel.getKeyCol(), SortOrder.ASCENDING);
				}
				catch(Exception ex)
				{
					logging.debug(this, "sortkey problem " + ex);
				}

			}

			else if (tableModel.getFinalCols() != null && tableModel.getFinalCols().size() > 0)
			{
				Iterator<Integer> iter =  tableModel.getFinalCols().iterator();
				//sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
				//sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
				while (iter.hasNext())
				{
					Integer col = iter.next();
					sortKeys.add(new RowSorter.SortKey(col, SortOrder.ASCENDING));

					sortDescriptor.put(col, SortOrder.ASCENDING);
				}
			}

			else
				sortKeys = null;

		}

		else
		{
			/*Iterator iter =  sortkeysBaseColumns.iterator();
			while (iter.hasNext())
		{
				sortKeys.add(new RowSorter.SortKey((Integer) iter.next(), SortOrder.ASCENDING));
		}
			*/
			for (Integer col : sortDescriptor.keySet())
			{
				sortKeys.add(new RowSorter.SortKey(col, sortDescriptor.get(col)));
			}

		}

		return sortKeys;

	}

	private void setSorter()
	{
		logging.info(this, "setSorter");

		if (tableModel == null)
			return;

		TableRowSorter<TableModel> sorter
		= new TableRowSorter<TableModel> (tableModel)
		  {
			  protected boolean useToString(int column)
			  {
				  try{
					  return super.useToString(column);
				  }
				  catch (Exception ex)
				  {
					  logging.info(this, "column " + column + " ------------------- no way to string");
					  return false;
				  }
			  }

			  public Comparator<?> getComparator(int column)
			  {
				  try{
					  logging.debug(this, " comparator for col " + column + " is " + super.getComparator(column));
					  return super.getComparator(column);
				  }
				  catch (Exception ex)
				  {
					  logging.warning(this, "column " + column + " ------------------- not getting comparator ");
					  return null;
				  }

				  //NullPointerException at java.lang.Class.isAssignableFrom
			  }
		  }
		  ;


		if (sorter instanceof DefaultRowSorter)
			//is always the case since TableRowSorter extends DefaultRowSorter
		{
			for (int j = 0; j < tableModel.getColumnCount(); j++)
			{
				if (comparators[j] != null)
					//restore previously explicitly assigned comparator
					((DefaultRowSorter) sorter).setComparator(j,
					        comparators[j]);

				else
					if (tableModel.getClassNames().get(j).equals("java.lang.Integer"))
					{
						((DefaultRowSorter) sorter).setComparator(j,
						        new de.uib.utilities.IntComparatorForStrings());
					}
			}
		}


		theTable.setRowSorter(sorter);

		java.util.List <RowSorter.SortKey> sortKeys = buildSortkeysFromColumns();

		if (sortKeys != null && sortKeys.size()>0)
			sorter.setSortKeys(sortKeys);

	}




	public void setTableModel(GenTableModel m)
	{
		theTable.setRowSorter(null);
		//just in case there was one

		theTable.setModel(m);
		tableModel = m;
		//exportTable = new ExportTable(theTable, m.getClassNames()	);

		comparators = new Comparator[m.getColumnCount()];

		setSorter();

		//System.out.println ( "----------------------------- model row count " + m.getRowCount());
		//logging.debug("---------  getColumnModel() size == 0 " + !theTable.getColumnModel().getColumns().hasMoreElements());
		setDataChanged(false);
		setCellRenderers();
	}

	/*

		TableRowSorter<TableModel> sorter
		= new TableRowSorter<TableModel>(m)
		  {
			  protected boolean useToString(int column)
			  {
				  try{
					  return super.useToString(column);
				  }
				  catch (Exception ex)
				  {
					  System.out.println ("------------------- no way to string");
					  return false;
				  }
			  }

			  public Comparator<?> getComparator(int column)
			  {
				  try{
					  return super.getComparator(column);
				  }
				  catch (Exception ex)
				  {
					  System.out.println ("------------------- not getting comparator ");
					  return null;
				  }

				  //NullPointerException at java.lang.Class.isAssignableFrom
			  }
		  }
		  ;
		theTable.setRowSorter(sorter);

		if (tableModel.getKeyCol() > -1)
		{

			java.util.List <RowSorter.SortKey> sortKeys
			= new ArrayList<RowSorter.SortKey>();
			//sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
			//sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
			sortKeys.add(new RowSorter.SortKey(tableModel.getKeyCol(), SortOrder.ASCENDING));
			sorter.setSortKeys(sortKeys);

		}

		else if (tableModel.getFinalCols() != null && tableModel.getFinalCols().size() > 0)
		{

			java.util.List <RowSorter.SortKey> sortKeys
			= new ArrayList<RowSorter.SortKey>();
			Iterator iter =  tableModel.getFinalCols().iterator();
			//sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
			//sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
			while (iter.hasNext())
				sortKeys.add(new RowSorter.SortKey((Integer) iter.next(), SortOrder.ASCENDING));

			sorter.setSortKeys(sortKeys);
		}



		//System.out.println ( "----------------------------- model row count " + m.getRowCount());
		//System.out.println("---------  getColumnModel() size == 0 " + !theTable.getColumnModel().getColumns().hasMoreElements());
		setDataChanged(false);

}
	*/

	/**
	* set special comparator for a column
	*/
	public void setComparator(String colName, Comparator comparator)
	{
		int modelCol = tableModel.getColumnNames().indexOf(colName);

		if (modelCol < 0)
		{
			logging.warning(this, "invalid column name");
			return;
		}
		DefaultRowSorter sorter = (DefaultRowSorter) theTable.getRowSorter();
		if (sorter == null)
			logging.warning(this, "no sorter");

		sorter.setComparator(modelCol, comparator);
	}

	/**
	* 	set columns for which the searchpane shall work
	*/
	public void setSearchColumns(Integer[] cols)
	{
		if (!withSearchPane)
		{
			logging.debug(this, "setSearchColumns: no search panel");
			return;
		}

		searchPane.setSearchFields(cols);
	}
	
	/**
	* 	set all columns for column selection in search pane; requires the correct model is initialized
	*/
	public void setSearchColumnsAll()
	{
		if (!withSearchPane)
		{
			logging.debug(this, "setSearchColumns: no search panel");
			return;
		}

		searchPane.setSearchFieldsAll();
	}

	/**
	* set search mode 
	* possible values 
	*		SearchPane.FULL_TEXT_SEARCH
	*		SearchPane.START_TEXT_SEARCH = 1;
	*		SearchPane.REGEX_SEARCH
	*/
	public void setSearchMode(int a)
	{
		searchPane.setSearchMode(a);
	}


	/**
	* set if a search results in a new selection
	*/
	public void setSearchSelectMode(boolean select)
	{
		searchPane.setSelectMode(select);
	}

	/**
	* transfer mappings to the searchpane
	*/
	public void setMapping(String columnName, Mapping<Integer, String> mapping)
	{
		searchPane.setMapping(columnName, mapping);
	}

	/**
	* set predefinition of searchfield
	*/
	public void setSelectedSearchField(String field)
	{
		searchPane.setSelectedSearchField(field);
	}


	/**
	* should mark the columns which are editable after being generated 
	*/
	public void setEmphasizedColumns(int[] cols)
	{
		if (cols == null)
			return;


		//System.out.println("---------  getColumnModel() size == 0 " + !theTable.getColumnModel().getColumns().hasMoreElements());

		if (theTable.getColumnModel().getColumns().hasMoreElements())
		{
			//System.out.println("---------  we have elements ");
			for (int j = 0; j < cols.length; j++)
			{
				theTable.getColumnModel().getColumn(cols[j])
				.setCellRenderer(new TableCellRendererConfigured(null, Globals.lightBlack , Globals.defaultTableCellBgColor1, Globals.defaultTableCellBgColor2, backgroundColorSelected, backgroundColorEditFieldsSelected));
			}
		}
	}

	protected void setTimestampRenderer(String classname, javax.swing.table.TableColumn col)
	{

		if (classname.equals("java.sql.Timestamp"))
			col.setCellRenderer(new TableCellRendererDate());


	}

	protected void setBigDecimalRenderer(String classname, javax.swing.table.TableColumn col)
	{
		if (classname.equals("java.math.BigDecimal"))
			col.setCellRenderer(new TableCellRendererCurrency());

	}

	protected void setBooleanRenderer(String classname, javax.swing.table.TableColumn col)
	{
		if (classname.equals("java.lang.Boolean"))
			col.setCellRenderer(new TableCellRendererByBoolean());

	}



	protected void setCellRenderers()
	{
		for (int i = 0; i < tableModel.getColumnCount(); i++)
		{
			Class cl = tableModel.getColumnClass(i);
			String name = tableModel.getColumnName(i);
			TableColumn col = theTable.getColumn(name);
			String classname = tableModel.getClassNames().get(i);


			//logging.debug(this, "setCellrenderer i, name, class, classname "
			//	+ i + ", " + name + ", " + cl + ", " + classname );

			setTimestampRenderer(classname, col);
			setBigDecimalRenderer(classname, col);
			setBooleanRenderer(classname, col);

		}
	}


	public void setDataChanged(boolean b)
	{
		dataChanged = b;
		buttonCommit.setEnabled(b);
		if (menuItemSave != null) menuItemSave.setEnabled(b);
		buttonCancel.setEnabled(b);
		if (menuItemCancel != null) menuItemCancel.setEnabled(b);
	}

	public boolean isDataChanged()
	{
		return dataChanged;
	}

	public void stopCellEditing()
	{
		if (theTable.getCellEditor() != null)
			//we are editing
		{
			logging.info(this, "we are editing a cell");
			theTable.getCellEditor().stopCellEditing();
		}
		else
			logging.info(this, "no cell editing");
	}


	protected void commit()
	{
		stopCellEditing();

		if ( myController == null )
			return;

		if ( myController.saveChanges() )
		{
			setDataChanged(false);
		}
	}

	protected void cancel()
	{
		if ( myController == null )
			return;

		if ( myController.cancelChanges() )
		{
			setDataChanged(false);
		}
	}

	protected void deleteCurrentRow()
	{
		if (getSelectedRowCount() > 0)
			tableModel.deleteRow(getSelectedRowInModelTerms());
	}


	public void setTableColumnInvisible( int col) {

		TableColumn column = null;
		try
		{
			column = theTable.getColumnModel().getColumn(col);
		}
		catch(Exception ex)
		{
			logging.info(this, "setTableColumnInvisible  " + ex);
		}

		if (column != null)
		{
			logging.info(this, "setTableColumnInvisible col " + col);
			column.setWidth(0);
			column.setMaxWidth(100);
			column.setMinWidth(0);
			column.setPreferredWidth(0);
			column.setResizable(true);
			theTable.getTableHeader().resizeAndRepaint();
		}

	}


	public  JTable getTheTable()
	{
		return theTable;
	}

	public GenTableModel getTableModel()
	{
		return tableModel;
	}

	public TableColumnModel getColumnModel()
	{
		return theTable.getColumnModel();
	}

	public ListSelectionModel getListSelectionModel()
	{
		return theTable.getSelectionModel();
	}

	/**
	* set the selection model for the table conceived as a list
	* the usage of any other model than the default ListSelectionModel.SINGLE_SELECTION
	* may be not fully supported
	*/
	public void setListSelectionMode(int selectionMode)
	{
		theTable.setSelectionMode(selectionMode);
	}

	public int getSelectedRowCount()
	{
		return theTable.getSelectedRowCount();
	}

	public int getSelectedRow()
	{
		return theTable.getSelectedRow();
	}

	public void setSelectedRow(int row)
	{
		theTable.setRowSelectionInterval(row, row);
		//System.out.println(" --- view row selected " + row);
		showSelectedRow();
	}

	public void showSelectedRow()
	{
		int row = getSelectedRow();
		if (row != -1)
			theTable.scrollRectToVisible(theTable.getCellRect(row, 0, false));
	}

	public int getSelectedRowInModelTerms()
	{
		return theTable.convertRowIndexToModel(theTable.getSelectedRow());
	}

	public void setSelectedRowFromModel(int row)
	{
		theTable.setRowSelectionInterval(
		    theTable.convertRowIndexToView(row),
		    theTable.convertRowIndexToView(row)
		);

		theTable.scrollRectToVisible(theTable.getCellRect(theTable.convertRowIndexToView(row), 0, true));
	}

	public void setValueAt(Object value, int row, int col)
	{
		tableModel.setValueAt(value, theTable.convertRowIndexToModel(row), theTable.convertColumnIndexToModel(col));
	}

	public Object getValueAt(int row, int col)
	{
		try{
			return tableModel.getValueAt(theTable.convertRowIndexToModel(row), theTable.convertColumnIndexToModel(col));
		}
		catch (Exception ex)
		{
			return null;
		}
	}

	public void selectedRowChanged()
	{
		//logging.info(this, "selectedRowChanged");
		//System.out.println(" new selected row ");
	}

	public void setAwareOfSelectionListener(boolean b)
	{
		awareOfSelectionListener = b;
	}


	public java.util.List<String> getSelectedKeys()
	{
		ArrayList<String> result = new ArrayList<String>();

		if (tableModel.getKeyCol() < 0)
			return result;

		//System.out.println(" selected " + theTable.getSelectedRows());

		for (int i = 0; i < theTable.getSelectedRows().length; i++)
		{
			result.add(
			    tableModel.getValueAt(  theTable.convertRowIndexToModel(theTable.getSelectedRows()[i]),
			                            tableModel.getKeyCol()).toString()
			);
		}

		return result;

	}

	public void setSelectedValues( java.util.List<String> values, int col)
	{
		getListSelectionModel().clearSelection();

		if (values == null || values.size() == 0)
			return;

		setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		Iterator iter = values.iterator();

		//System.out.println(" setSelectedValues for " + values);

		while (iter.hasNext())
		{
			int viewRow = findViewRowFromValue((String) iter.next(), col);
			//System.out.println(" viewRow " + viewRow);
			getListSelectionModel().addSelectionInterval(viewRow, viewRow);
		}

	}


	protected int findViewRowFromValue(int startviewrow, Object value, int col)
	{
		logging.debug(this, "findViewRowFromValue startviewrow, value, col "
		              + startviewrow + ", " + value + ", " + col);

		if (value == null)
			return -1;

		String val = value.toString();

		boolean found = false;


		int viewrow = 0;

		if (startviewrow > 0)
			viewrow = startviewrow;

		while (!found && viewrow < tableModel.getRowCount())
		{
			Object compareValue =
			    tableModel.getValueAt(

			        theTable.convertRowIndexToModel(viewrow),
			        col

			    );

			//logging.debug(this, "findViewRowFromValue compare " + value + " to " + compareValue);

			if 	(compareValue == null)
			{
				if (val == null || val.equals(""))
					found = true;
			}

			else
			{
				String compareVal = compareValue.toString();

				if (val.equals(compareVal))
					found = true;
			}

			if (!found)
				viewrow++;

		}


		if (found)
		{
			return viewrow;
		}

		return -1;
	}

	public int findViewRowFromValue(Object value, int col)
	{
		return findViewRowFromValue(0, value, col);
	}


	/*
	protected int findViewRowFromValue(String value, int col)
{

		if (value == null)
			return -1;

		boolean found = false;

		int viewrow = 0;

		while (!found && viewrow < tableModel.getRowCount())
		{

			if (value.equals(
			            tableModel.getValueAt(

			                theTable.convertRowIndexToModel(viewrow),
			                col

			            ).toString())
			   )
				found = true;
			else
				viewrow++;
		}


		if (found)
		{
			return viewrow;
		}

		return -1;
}
	*/

	public boolean moveToValue(String value, int col)
	{
		return moveToValue(value, col, true);
	}

	public boolean moveToValue(String value, int col, boolean selecting)
	{
		logging.info(this, "moveToValue " + value + " col " + col + " selecting " + selecting);
		int viewrow = findViewRowFromValue(value, col);
		theTable.scrollRectToVisible(theTable.getCellRect(viewrow, col, false));

		if (viewrow == -1)
			return false;

		if (selecting) setSelectedRow(viewrow);

		return true;
	}

	public boolean moveToKeyValue(String keyValue)
	{

		boolean found = false;

		if (keyValue == null)
			return false;

		//System.out.println(" -------------++----- keyValue " + keyValue);
		if (tableModel.getKeyCol() > -1)
		{
			found = moveToValue(keyValue, tableModel.getKeyCol());
		}

		else
			// try to use pseudokey
		{
			int viewrow = 0;

			while (!found && viewrow < tableModel.getRowCount())
			{
				String[] partialkeys = new String[tableModel.getFinalCols().size()];


				for (int j = 0; j < tableModel.getFinalCols().size(); j++)
				{
					partialkeys[j] =
					    tableModel.getValueAt(
					        theTable.convertRowIndexToModel(viewrow),
					        tableModel.getFinalCols().get(j)
					    ).toString();
				};

				if (
				    keyValue.equals(Globals.pseudokey(partialkeys))
				)
					found = true;
				else
					viewrow++;
			}


			if (found)
			{
				setSelectedRow(viewrow);
			}

			else
			{
				//try value for col 0 as target for search
				found = moveToValue(keyValue, 0);
			}


		}

		return found;

	}



	public void moveToRow(int n)
	{
		//logging.debug(this, "moveToRow " + n);
		if (tableModel.getRowCount() == 0)
			return;

		if (getSelectedRowCount() != 1)
			return;

		if (n < 0 || n >= theTable.getRowCount())
			return;

		theTable.scrollRectToVisible(theTable.getCellRect(n, 0, true));
		theTable.setRowSelectionInterval(n, n);
		//logging.debug(this, "moveToRow success " + n + "getSelectedRowInModelTerms " + getSelectedRowInModelTerms());
	}

	public void moveToModelRow(int n)
	{
		if (tableModel.getRowCount() == 0)
			return;

		if (getSelectedRowCount() != 1)
			return;

		if (n < 0 || n >= theTable.getRowCount())
			return;

		theTable.scrollRectToVisible(theTable.getCellRect(theTable.convertRowIndexToView(n), 0, true));
		theTable.setRowSelectionInterval(
		    theTable.convertRowIndexToView(n),
		    theTable.convertRowIndexToView(n)
		);
	}

	public boolean canNavigate()
	{
		return tableModel.getRowCount() > 0
		       && getSelectedRowCount() == 1;
	}


	public boolean isFirstRow()
	{
		return getSelectedRow() == 0;
	}

	public boolean isLastRow()
	{
		return getSelectedRow() == tableModel.getRowCount() - 1;
	}


	public void moveToLastRow()
	{
		moveToRow(tableModel.getRowCount()-1);
	}

	public void moveToFirstRow()
	{
		moveToRow(0);
	}

	public void moveRowBy(int i)
	{
		if (getSelectedRowCount() != 1)
			return;

		int n = theTable.getSelectedRow();


		if (i > 0)
		{
			if (n + i < theTable.getRowCount())
				n = n + i;
		}
		else if (i < 0)
		{
			if (n + i >= 0)
				n = n + i;
		}

		moveToRow(n);
	}



	public RowSorter getRowSorter()
	{
		return theTable.getRowSorter();
	}


	//
	// TableModelListener
	public void tableChanged(TableModelEvent e)
	{
		//System.out.println (" -------- tableChanged event " + e);
		setDataChanged(true);
		if (tableModel != null && oldrowcount  !=  tableModel.getRowCount())
		{
			oldrowcount =  tableModel.getRowCount();
		}

	}


	//
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
	}

	//
	// KeyListener interface
	public void keyPressed(KeyEvent e)
	{
		if (e.getSource() == theTable)
		{
			//System.out.println(" event on table " + e);
			if (e.getKeyCode() == KeyEvent.VK_DELETE)
				deleteCurrentRow();

		}
	}
	public void keyReleased(KeyEvent e)
	{
		/*
		if (e.getSource() == theTable)
		{
			System.out.println(" event on table " + e);
	}
		*/
	}
	public void keyTyped(KeyEvent e)
	{
		/*
		if (e.getSource() == theTable)
		{
			System.out.println(" event on table " + e);
	}
		*/
	}


	//
	//ListSelectionListener
	public void valueChanged(ListSelectionEvent e)
	{
		//logging.debug(this, "ListSelectionEvent");
		//Ignore extra messages.
		if (e.getValueIsAdjusting()) return;

		ListSelectionModel lsm =
		    (ListSelectionModel)e.getSource();

		if (awareOfSelectionListener)
			setDataChanged(true);

		if (lsm.isSelectionEmpty()) {
			logging.debug(this, "no rows selected");
			if (menuItemDeleteRelation != null)
				menuItemDeleteRelation.setEnabled(false);
		}
		else
		{
			int selectedRow = lsm.getMinSelectionIndex();
			if (followSelectionListener) selectedRowChanged();
			if (menuItemDeleteRelation != null)
				menuItemDeleteRelation.setEnabled(true);
		}
	}

	//MouseListener, hook for subclasses
	public void mouseClicked(MouseEvent e)
	{
		//logging.info(this, "mouse event " + e);
		//logging.info(this, "row " + theTable.rowAtPoint(e.getPoint()) );
	}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}


	//ComponentListener for table
	public void componentHidden(ComponentEvent e){}
	public void componentMoved(ComponentEvent e){}
	public void componentShown(ComponentEvent e){}
	public void componentResized(ComponentEvent e)
	{
		showSelectedRow();
	}



	protected void floatExternal()
	{

		PanelGenEditTable copyOfMe;
		de.uib.configed.gui.GeneralFrame  externalView;

		copyOfMe = new PanelGenEditTable(title, maxTableWidth, false);
		//copyOfMe.setSoftwareInfo(hostId, swRows);
		copyOfMe.setTableModel(tableModel);

		externalView = new de.uib.configed.gui.GeneralFrame(null, "hallo", false);
		externalView.addPanel(copyOfMe);
		externalView.setup();
		externalView.setSize(this.getSize());
		externalView.centerOn(de.uib.configed.Globals.mainFrame);

		externalView.setVisible(true);
	}


}
