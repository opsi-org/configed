package de.uib.configed.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;

import java.util.*;
import java.math.*;

import de.uib.configed.*;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.swing.*;

import java.io.*;
import java.util.zip.*;

public class PanelTabbedDocuments extends ClippedTitleTabbedPane
{
	LogPane[]  textPanes;
	String[] idents;
	final java.util.List<String> identsList;
	
	protected JFileChooser chooser;
	protected File chooserDirectory;
	
	public PanelTabbedDocuments (final String[] idents, String defaultText)
	{
		
		this.idents = idents;
		
		identsList = Arrays.asList(idents);
		
		setProportionOfTotalWidth(0.5);
		
		setForeground(de.uib.utilities.Globals.blue);
		//setFont(Globals.defaultFont);
		
		textPanes = new LogPane[idents.length];
		
		//System.out.println ( " ------------------------------  idents.length " + idents.length); 
		
		for (int i = 0; i < idents.length; i++)
		{
			final String ident = idents[i];
			LogPane showPane = 
			new LogPane(defaultText)
			{
				@Override 
				protected void reload()
				{
					super.reload();
					loadDocument(ident);
				}
				
				@Override 
				protected void save()
				{
					String filename = ident;
					if (getInfo() != null)
					{
						filename = getInfo().replace('.','_') + "___"+ident + ".log";
					}
					logging.debug(this, "save with filename " + filename);	
					String pathname = openFile(filename + ".log");
					if ( pathname !=null && !pathname.equals("") )
						saveToFile(pathname, lines);
					//showLogfile.setTitle(fn);
				}
				@Override 
				protected void saveAsZip()
				{
					logging.info(this, "saveAsZip");
					
					String filename = ident;
					if (getInfo() != null)
					{
						filename = getInfo().replace('.','_') + "___"+ident;
					}
					String pathname = openFile(filename + ".zip");
					if ( pathname !=null && !pathname.equals("") ) {
						saveToZipFile(pathname, filename, lines);
					}
						
				}
				@Override 
				protected void saveAllAsZip(boolean loadMissingDocs)
				{
					logging.info(this, "saveAllAsZip got ident " + ident + " loadMissingDocs " + loadMissingDocs);
					
					String fname = ident;
					if (getInfo() != null)
					{
						fname = getInfo().replace('.','_') + "_all";
					}
					
					logging.info(this, "saveAllAsZip, start getting pathname");
					String pathname = openFile(fname + ".zip");
				
					if ( pathname !=null && !pathname.equals("") )
					{
						logging.info(this, "saveAllAsZip, got pathname");
					
						if ( loadMissingDocs )
						{
							for (int logNo = 0; logNo < idents.length; logNo++)
							{	
								if (textPanes[logNo].lines.length<=1)//empty 
								{
									loadDocument(idents[logNo]);
								}
								
								logging.info(this,  "saveAllAsZip textPanes[" + logNo + 
								 "].lines.length " + textPanes[logNo].lines.length);
							
							}
						}
						
						/*
						for (int logNo = 0; logNo < idents.length; logNo++)
						{
							logging.info(this,  "saveAllAsZip textPanes[" + logNo + 
							 "].lines.length " + textPanes[logNo].lines.length);
						}
						*/
						
						saveAllToZipFile(pathname);
					}
					
				}			
			
			};
			
			textPanes[i] = showPane;
			
			addTab	(ident, textPanes[i]);
			
			/*
			LogPane LogPane = new LogPane(defaultText)
			{
				@Override 
				protected void reload()
				{
					super.reload();
					loadDocument(ident);
				}
			};
			*/
		
			
		}
		
	}		
	
	public void loadDocument(String ident)
	//override in subclasses
	{
		logging.debug(this, "loadDocument ident " + ident);
	}
	
	/*
	public void updateDocument(final String ident, final String s, final String info)
	{
		final WaitCursor waitCursor = new WaitCursor( de.uib.configed.Globals.mainContainer, "updateDocument");
		SwingUtilities.invokeLater( new Runnable(){
			public void run(){
				setDocument(ident, s, info);
				waitCursor.stop();
			}
		});
	}
	*/
	
	private void setDocument(int i, final String document, final String info)
	{
		logging.info(this, "setDocument " + i + " document == null " + (document == null));
		if (i < 0 || i >= idents.length)
			return;
		
		if (document == null)
		{
			textPanes[i].setText("");
			textPanes[i].setTitle("");
			return;
		}
		
		textPanes[i].setTitle(idents[i] + "  " + info);
		textPanes[i].setInfo(info); //should be name of client, delivered from info textfield
		textPanes[i].setText(document);
		
		
	}
	
	private void setDocument(String ident, final String document,  final String info)
	{
		int i = identsList.indexOf(ident);
		setDocument(i, document, info);
	}
		
		
		
	
	public void setDocuments(final Map<String,String> documents, final String info)
	{
		logging.info(this," ------------------------------  idents.length " + idents.length + " info: " + info);
		for (int i = 0; i < idents.length; i++)
		{
			setDocument(idents[i], documents.get(idents[i]), info);
		}		
	
	}
	
	
	private void setFileChooser (String fn) {
		if (chooser==null) {
			chooser = new JFileChooser(fn);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY); //AND_DIRECTORIES);
			chooser.setFileFilter( new javax.swing.filechooser.FileNameExtensionFilter("logfiles: .log, .zip, .gz, .7z", "log", "zip", "gz", "7z" ) );
			//chooser.setFileHidingEnabled(false);
			chooser.setApproveButtonText("O.K.");
			chooser.setDialogType(JFileChooser.SAVE_DIALOG);
			chooser.setDialogTitle(de.uib.configed.Globals.APPNAME + " " + configed.getResourceValue("PanelTabbedDocument.saveFileChooser"));
		}
	}
	
	private String openFile(String typename)
	{
		String fileName = null;
		
		if (chooser==null) {
			setFileChooser("");
			chooserDirectory = chooser.getCurrentDirectory();
		}
		
		File f = new File(chooserDirectory, typename);
		chooser.setSelectedFile(f);
		
		int returnVal = chooser.showSaveDialog(de.uib.configed.Globals.frame1);
		if(returnVal == JFileChooser.APPROVE_OPTION) 
		{
			if (chooser!=null) {
				fileName = chooser.getSelectedFile().getAbsolutePath();
				chooserDirectory = chooser.getCurrentDirectory();
			}
			else {
				logging.error("Not a valid filename: " +  fileName);
				//showDialog("Not a valid filename: \n" +  fileName);
			}
		}
		
		
		return fileName;
	}
	
	
	private void saveToFile (String fn, String[]  lines) {
		FileWriter fWriter = null;
		try {
			fWriter = new FileWriter(fn);
		} catch (IOException ex) {
			logging.error("Error opening file: " +  fn + "\n --- " + ex);
		}
		int i = 0;
		while (i < lines.length) {
			try {
				fWriter.write(lines[i] + "\n");
				//setTitle(de.uib.configed.Globals.APPNAME + " : " + fn);
			} catch (IOException ex) {
				logging.error("Error writing file: " +  fn + "\n --- " + ex);
			}
			i++;
		}
		try {
			fWriter.close();
		} catch (IOException ex) {
			logging.error("Error closing file: " +  fn + "\n --- " + ex);
		}
	}
	
	private void saveAllToZipFile (String pn) {
		ZipOutputStream out = null;
		
		byte[] CRLF = new byte[2];
		CRLF[0] = '\r';
		CRLF[1] = '\n';
		
		byte[] buffer;
		try {
			out = new ZipOutputStream(new FileOutputStream(pn));
			out.setMethod(ZipOutputStream.DEFLATED);
			for (int logNo=0; logNo< idents.length; logNo++ ) { 
				// load all logfiles ???
				// 
				if (textPanes[logNo].lines.length>1) {  // save only if not empty
					ZipEntry entry = new ZipEntry(textPanes[logNo].title.replace(" ", "_").replace(".","_") + ".log");
					out.putNextEntry(entry);
					int i = 0;
					while (i < textPanes[logNo].lines.length) {
						try {
							buffer = textPanes[logNo].lines[i].getBytes();
							out.write(buffer, 0, textPanes[logNo].lines[i].length());
							out.write(CRLF, 0, 2);
						} catch (IOException ex) {
							logging.error("Error writing zip file: " +  pn + "\n --- " + ex);
						}
						i++;
					}
				}
				out.closeEntry();
			}
		} catch (IOException ex) {
			logging.error("Error writing zip file: " +  pn + "\n --- " + ex);
		}
		try {
			out.close();
		} catch (IOException ex) {
			logging.error("Error closing zip file: " +  pn + "\n --- " + ex);
		}
	}
	
	private void saveToZipFile  (String pn, String fn, String[]  lines) {
		
		ZipOutputStream out = null;
		
		byte[] CRLF = new byte[2];
		CRLF[0] = '\r';
		CRLF[1] = '\n';
		byte[] buffer;
		try {
			out = new ZipOutputStream(new FileOutputStream(pn));
			out.setMethod(ZipOutputStream.DEFLATED);
			ZipEntry entry = new ZipEntry(fn);
			out.putNextEntry(entry);
			int i = 0;
			while (i < lines.length) {
				try {
					
					buffer = lines[i].getBytes();
					out.write(buffer, 0, lines[i].length());
					out.write(CRLF, 0, 2);
				} catch (IOException ex) {
					logging.error("Error writing zip file: " +  fn + "\n --- " + ex);
				}
				i++;
			}
			out.closeEntry();
		} catch (IOException ex) {
			logging.error("Error writing zip file: " +  fn + "\n --- " + ex);
		}
		try {
			out.close();
		} catch (IOException ex) {
			logging.error("Error closing zip file: " +  fn + "\n --- " + ex);
		}
	}
	
	
}
