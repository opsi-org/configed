package de.uib.utilities.table;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import javax.swing.*;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;


import jxl.Workbook;
import jxl.Sheet;
import jxl.Cell;
import jxl.LabelCell;
import jxl.write.DateFormat;
import jxl.write.DateTime;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.Label;
import jxl.write.Number;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;

public class ExportTable {

	protected javax.swing.JTable theTable;
	protected String sheetLabel = "Seite 1";//must not be empty!
	protected Vector<String> classNames;
	
	protected String CSVencoding = "UTF8";
	public String CSVseparator = "\t";
	public String CSVseparatorForExcel = ";";
	
	protected File exportDirectory;
	
	protected Vector<Integer> excludeCols; 
	
	DecimalFormat f = new DecimalFormat("#0.00"); 
	
	public ExportTable (javax.swing.JTable table, Vector<String> classNames) 
	{
		this.theTable = table;
		this.classNames = classNames;
	}

	public void setTableAndClassNames (javax.swing.JTable table, Vector<String> classNames)
	{
		this.theTable = table;
		this.classNames = classNames;
	}
	
	public void setExcludeCols(Vector<Integer> excludeCols)
	//only take into account for excel export at the moment
	{
		this.excludeCols = excludeCols;
	}
	
	public void toExcel(String fileName, boolean onlySelectedRows)
	{
		WritableWorkbook workbook;
		WritableSheet sheet;
		
		//we retrieve the current classNames if we get it 
		if (theTable.getModel() instanceof GenTableModel)
			classNames = ((GenTableModel) theTable.getModel()).getClassNames();
		
		
		Date date1 = null;
		
		//System.out.println("export to excel");
		
		if (fileName==null) 
		{
			JFileChooser chooser = new JFileChooser(exportDirectory);
			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			javax.swing.filechooser.FileNameExtensionFilter filter = new javax.swing.filechooser.FileNameExtensionFilter(
				"Excel-Dateien", "xls");
			chooser.addChoosableFileFilter(filter);
			
			chooser.setDialogType(JFileChooser.SAVE_DIALOG);
			//chooser.setApproveButtonText("ok");
			chooser.setApproveButtonToolTipText("Verzeichnis oder Datei auswählen");
			chooser.setDialogTitle(Globals.SHORT_APPNAME + " Export - Speicherort bestimmen");
			
			int returnVal = chooser.showDialog(Globals.masterFrame, null);
			logging.debug(this, "toExcel returnVal from chooser "+ returnVal);
			if(returnVal == JFileChooser.APPROVE_OPTION) 
			{
				try 
				{
					fileName = chooser.getSelectedFile().getAbsolutePath();
					
					File file = new File(fileName);
					
					if (file.isDirectory())
						fileName = fileName + File.separator + "export.xls";
					
					else
					{
						if (!fileName.toLowerCase().endsWith(".xls"))
							fileName = fileName + ".xls";
					}
					
					logging.debug(this, "fileName " + fileName);
					
					file = new File(fileName);
					
					if ( file.exists() )
					{
						int option = JOptionPane.showConfirmDialog(	Globals.masterFrame, 
							"Soll die bestehende Datei\n überschrieben werden?", 
							Globals.SHORT_APPNAME + " Nachfrage",
							JOptionPane.OK_CANCEL_OPTION );
							
						if (option == JOptionPane.CANCEL_OPTION)
							fileName = null;
					}
				}
				catch (Exception fc_e) 
				{
					logging.error("Es wurde kein gültiger Dateiname angegeben");
				}
			}
		}
		
		if (fileName != null)
		{
			try
			{
				exportDirectory = new File(fileName).getParentFile();
			}
			catch (Exception e) 
			{
				logging.error("Problem mit dem Verzeichnis von " + fileName + " : " + e);
			}
		}
			
		
		logging.debug(this,"export to " + fileName);
		
		if (fileName!=null) 
		{
			DateFormat customDateFormat = new DateFormat ("dd.MM.yyyy");
			
			try
			{
				//logging.debug(this, "starting workbook ");
				workbook = Workbook.createWorkbook(new File(fileName));
				//logging.debug(this, "creating sheet ");
				sheet = workbook.createSheet(sheetLabel, 0);
				
				//logging.debug(this, "starting header, " + theTable);
				//logging.debug(this, "starting header, " + theTable.getColumnCount());
				
				int colSheet = -1;
				for (int colI = 0; colI<theTable.getColumnCount();colI++) 
				{ // i column
					//logging.debug(this, theTable.getColumnName(colI));
					if (excludeCols != null && excludeCols.indexOf(colI) > -1)
								continue;
					colSheet++;
					Label label = new Label(colSheet, 0, theTable.getColumnName(colI));
					sheet.addCell(label);
				}
				
				int rowExcel = 0;
				for (int rowI = 0; rowI < theTable.getRowCount(); rowI++)
				{
					if (!onlySelectedRows || theTable.isRowSelected(rowI))
					{
						colSheet = -1;
						for (int colI = 0; colI<theTable.getColumnCount();colI++) 
						{
							//logging.debug(this, "export row, col " + rowI + ",   " + colI);
							 
							if (excludeCols != null && excludeCols.indexOf(colI) > -1)
								continue;
							colSheet++;
							
							//logging.debug(this, "export row, col " + rowI + ",   " + colI);
							
							date1 = null;
							
							if (theTable.getValueAt(rowI, colI)!=null) {  // ignore null
								//System.out.println(" ---- className " + classNames.get(colI));
								if (classNames.get(colI).equals("java.lang.String")) {
									Label contentLabel = new Label(colSheet, rowExcel+1, (String) theTable.getValueAt(rowI, colI));
									sheet.addCell(contentLabel);
								}
								else if (classNames.get(colI).equals("java.lang.Integer")) {
									Number number = new Number(colSheet, rowExcel+1, Integer.valueOf((String)theTable.getValueAt(rowI, colI)).intValue());
									sheet.addCell(number); 
								}
								else if (classNames.get(colI).equals("java.lang.Boolean")) {
									Label contentLabel = new Label(colSheet, rowExcel+1, "" + theTable.getValueAt(rowI, colI));
									sheet.addCell(contentLabel); 
									 // im false-Fall nichts ausgeben ???
								}
								else if (classNames.get(colI).equals("java.lang.Float")) {
									Number number = new Number(colSheet, rowExcel+1, Float.valueOf((String)theTable.getValueAt(rowI, colI)).floatValue()); 
									sheet.addCell(number); 
								}
								else if (classNames.get(colI).equals("java.lang.Double")) {
									Number number = new Number(colSheet, rowExcel+1, Double.valueOf((String)theTable.getValueAt(rowI, colI)).doubleValue()); 
									sheet.addCell(number); 
								}
								else if (classNames.get(colI).equals("java.sql.Timestamp")) {
									// format date for Excel 
									if ((theTable.getValueAt(rowI, colI)!=null) && (!theTable.getValueAt(rowI, colI).equals(""))) {
										try {
											//System.out.println("vor parsing " + theTable.getValueAt(rowI, colI));
											
											date1 = java.sql.Timestamp.valueOf((String) theTable.getValueAt(rowI, colI) );
											
											//System.out.println("nach parsing " + date1);
											
										} catch (Exception ex2) {
											logging.error("Fehler Datumskonversion bei Export nach Excel: " + ex2);
										}
										if (date1 != null) {
											WritableCellFormat dateFormat = new WritableCellFormat (customDateFormat);
											DateTime dateCell = new DateTime(colSheet, rowExcel+1, date1 , dateFormat);
											sheet.addCell(dateCell);
										}
									}
								}
								else {
									Label contentLabel = new Label(colSheet, rowExcel+1, (String) theTable.getValueAt(rowI, colI));
									sheet.addCell(contentLabel);
								}
							}
		
						}
						rowExcel++;
					}
					
				}
				
				workbook.write();
				workbook.close();
			
			}
			catch (Exception ex)
			{	
				logging.error("Fehler beim Export von : " + ex.toString());
				//return false;
			}
		}
	}
	
	private String removeImpossibleChars(Object value)
	{
		if (value == null)
			return "";
		
		String result = ((String) value).replace('\"', '\'');
		//result = result.replace(CSVseparator, " ");
		return result;
	}
	
	public void toCSV (String fileName, boolean onlySelectedRows, String csvSep)
	{
		if (theTable.getModel() instanceof GenTableModel)
			classNames = ((GenTableModel) theTable.getModel()).getClassNames();
		
		logging.debug(this, "toCSV fileName, onlySelectedRows, csvSep " +
			fileName + ", " + onlySelectedRows + ", " + csvSep);
		
		
		Date date1 = null;
		
		if (fileName==null) 
		{
			JFileChooser chooser = new JFileChooser(exportDirectory);
			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			javax.swing.filechooser.FileNameExtensionFilter filter = new javax.swing.filechooser.FileNameExtensionFilter(
				"CSV-Dateien", "csv");
			chooser.addChoosableFileFilter(filter);
			
			chooser.setDialogType(JFileChooser.SAVE_DIALOG);
			chooser.setDialogTitle(Globals.SHORT_APPNAME + " Export csv - Speicherort bestimmen");
			
			//chooser.setApproveButtonText("ok");
			chooser.setApproveButtonToolTipText("Verzeichnis oder Datei auswählen");
			
			
			int returnVal = chooser.showDialog(Globals.masterFrame, null);
			if(returnVal == JFileChooser.APPROVE_OPTION) 
			{
				try 
				{
					fileName = chooser.getSelectedFile().getAbsolutePath();
					
					File file = new File(fileName);
					
					if (file.isDirectory())
						fileName = fileName + File.separator + "export.csv";
					
					else
					{
						if (!fileName.toLowerCase().endsWith(".csv"))
							fileName = fileName + ".csv";
					}
					
					logging.debug(this, "fileName " + fileName);
					
					file = new File(fileName);
					
					if ( file.exists() )
					{
						int option = JOptionPane.showConfirmDialog(	Globals.masterFrame, 
							"Soll die bestehende Datei\n überschrieben werden?", 
							Globals.SHORT_APPNAME + " Nachfrage",
							JOptionPane.OK_CANCEL_OPTION );
							
						if (option == JOptionPane.CANCEL_OPTION)
							fileName = null;
					}
				}
				catch (Exception fc_e) 
				{
					logging.error("Es wurde kein gültiger Dateiname angegeben");
				}
			}
		}
		
		if (fileName != null)
		{
			try
			{
				exportDirectory = new File(fileName).getParentFile();
			}
			catch (Exception e) 
			{
				logging.error("Problem mit dem Verzeichnis von " + fileName + " : " + e);
			}
		}

		logging.debug(this,"export to " + fileName);

		if (fileName!=null) 
		{
			try 
			{
				OutputStream os = (OutputStream) new FileOutputStream(fileName);
				OutputStreamWriter osw = new OutputStreamWriter(os); //, CSVencoding); ergibt sonst Sonderzeichen unter Windows
				BufferedWriter bw = new BufferedWriter(osw);
				//logging.debugOut(logging.LEVEL_WARNING,"BufferedWriter created");
				// write header
				StringBuffer line = new StringBuffer();
				for (int colI=0; colI<theTable.getColumnCount();colI++) 
				{ // i column
					line.append(theTable.getColumnName(colI));
					if (colI<theTable.getColumnCount()-1) 
					{
						line.append(csvSep);
					}
				}
				line.append("\n");
				bw.write(line.toString());
				bw.flush();
				// write rows	
				
				SimpleDateFormat dateFormatter = new SimpleDateFormat ("dd. MMMMM yyyy");
				SimpleDateFormat dateParser = new SimpleDateFormat ("dd.MM.yyyy");

				
				for (int rowI=0; rowI<theTable.getRowCount(); rowI++) 
				{ 
					
					if (!onlySelectedRows || theTable.isRowSelected(rowI))
					{
						line = new StringBuffer();
						for (int colI=0; colI<theTable.getColumnCount();colI++) 
						{ // i column	
							date1 = null;  // reset
							
						
							/*
							logging.debug(this, "toCsv, handle row, col, value: " + rowI + ", " + colI + ", " +  theTable.getValueAt(rowI, colI));
							if (theTable.getValueAt(rowI, colI) == null)
								logging.debug(this, "toCsv, handle row, col, value: " + rowI + ", " + colI + ",  null (will be ignored)");
							else
								logging.debug(this, "toCsv, handle row, col, class: " + rowI + ", " + colI + ", "  +  theTable.getValueAt(rowI, colI).getClass());
							*/
							
							if (theTable.getValueAt(rowI,colI)!=null) 
							{
								if (classNames.get(colI).equals("java.lang.String")) {
									String inString = new String();
									inString = removeImpossibleChars(theTable.getValueAt(rowI,colI));
									
									if (inString.matches("\\d{2}.\\d{2}.\\d{4}") ||
											inString.matches("\\d{1}.\\d{2}.\\d{4}") 
											|| inString.matches("\\d{1}.\\d{1}.\\d{4}")
											|| inString.matches("\\d{2}.\\d{1}.\\d{4}")) {
										date1 = dateParser.parse(inString);
										line.append(dateFormatter.format(date1));
									} else {
										line.append("\"" + inString + "\"");
									}
								}
								
								else if (classNames.get(colI).equals("java.lang.Integer")){
									line.append(theTable.getValueAt(rowI,colI));
								}
								else if (classNames.get(colI).equals("java.lang.Double")) {
									logging.debug(this, "decimal place --- double: " + theTable.getValueAt(rowI,colI));
									line.append(theTable.getValueAt(rowI,colI));
								}
								else if (classNames.get(colI).equals("java.lang.Float")) {
									logging.debug(this, "decimal place --- float: " + theTable.getValueAt(rowI,colI));
									line.append(theTable.getValueAt(rowI,colI));
								}
								else if (classNames.get(colI).equals("java.math.BigDecimal")) {
									logging.debug(this, "decimal place --- bigdecimal: " + theTable.getValueAt(rowI,colI));
									line.append( f.format(Double.parseDouble(theTable.getValueAt(rowI,colI).toString())));
								}
								else if (classNames.get(colI).equals("java.lang.Boolean")) {
									boolean booleanValue = (Boolean) theTable.getValueAt(rowI, colI);
									line.append(booleanValue);
									//if (booleanValue) {
									//	line.append("ja");
									//} // im false-Fall nichts ausgeben
								}
								
								else if (classNames.get(colI).equals("java.sql.Timestamp")) {
									// format date for csv 
									if ((theTable.getValueAt(rowI, colI)!=null) && (!theTable.getValueAt(rowI, colI).equals(""))) {
										try {
											date1 = java.sql.Timestamp.valueOf((String) theTable.getValueAt(rowI, colI) );
											
										} catch (Exception ex2) {
											logging.error("Fehler bei Datumskonversion bei Export nach CSV: " + ex2);
										}
										if (date1 != null) {
											line.append(dateFormatter.format(date1));
										}
									}
								}
								
								else // append other values
									line.append(theTable.getValueAt(rowI,colI));
							}
							
							if (colI<theTable.getColumnCount()-1) {
								line.append(csvSep);
							}
						}
						
						if (rowI<theTable.getRowCount()-1) {
							line.append("\n");
						}
						bw.write(line.toString());
						bw.flush();
					}
				}
				
				bw.close();
				osw.close();
				os.close();
				//logging.debugOut(logging.LEVEL_WARNING,"Datei wurde exportiert");
			}
			catch (Exception ex) {
				logging.debugOut(logging.LEVEL_ERROR, "Fehler bei CSV-Export: " + ex.toString());
			}
		}
		
	}
	
}