package de.uib.utilities.pdf;

/**
* @author  m.hammel
* 2014
*/
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.*;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.fonts.otf.TableHeader;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.type.HostInfo;
import de.uib.utilities.*;
import de.uib.utilities.pdf.DocumentElementToPdf.*;
import de.uib.utilities.swing.Containership;
import de.uib.utilities.logging.logging;

public class DocumentToPdf {

	
	private static float mLeft = 36;
	private static float mRight = 36;
	private static float mTop = 74; 			// with header
	private static float mBottom = 54;
	private float xHeaderTop = 803;
	private float headerWidth = 527;
	// dimensions of sheet A4: 595 / 842

	protected static Document document;
	protected static PdfWriter writer;
	
	protected static javax.swing.JTable theTable;
	//private static ArrayList<Integer> leftAlignmentlist = new ArrayList<Integer>(); //
	protected Vector<String> classNames;
	
	protected JFileChooser chooser;
	protected static File exportDirectory;
	protected OpenSaveDialog dialog;

	protected Vector<Integer> excludeCols; 
	
	protected Vector <HashMap<String, Object>> theListOfContentElements;
	protected HashMap<String, String> metaData = new HashMap<String, String>();
	

	
	public DocumentToPdf (String filename, HashMap<String, String> metaData) {
		document = new Document(PageSize.A4, mLeft, mRight, mTop, mBottom);
		this.metaData = metaData;
		theListOfContentElements = new Vector <HashMap<String, Object>>();
		// reset for every document
		de.uib.utilities.pdf.DocumentElementToPdf.setAlignmentLeft(new ArrayList<Integer>());
	}
	
	public void createContentElement (String elem, Object value){
		HashMap <String, Object> content = new HashMap <String, Object>();
		content.put(elem, value);
		theListOfContentElements.add(content);
	}
	public void addEmptyLine () {
		HashMap <String, Object> content = new HashMap <String, Object>();
		String[] data = {" "};
		content.put("paragraph", data);
		theListOfContentElements.add(content);
	}

	public void setJTable (JTable table) {
		this.theTable = table;
	}
	public void setExcludeCols(Vector<Integer> excludeCols)
	//only take into account for excel export at the moment
	{
		this.excludeCols = excludeCols;
	}

	public void setPageSizeA4 (){
			document.setPageSize(PageSize.A4);
			headerWidth = 527;
			xHeaderTop = 803;
	}
		
	public void setPageSizeA4_Landscape (){
			document.setPageSize(PageSize.A4.rotate());
			headerWidth = 770;
			xHeaderTop = 555;
	}

	/**
     * Inner class to add a table as header.
     */
	class TableHeader extends PdfPageEventHelper {
	    /** The header text. */
	    String header = "";
	    /** The template with the total number of pages. */
	    PdfTemplate total;

	    /**
	     * Allows us to change the content of the header.
	     * @param header The new header String
	     */
	    public void setHeader(String header) {
	        this.header = header;
	    }
	    /**
	     * Creates the PdfTemplate that will hold the total number of pages.
	     * @see com.itextpdf.text.pdf.PdfPageEventHelper#onOpenDocument(
	     *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
	     */
	    public void onOpenDocument(PdfWriter writer, Document document) {
	        total = writer.getDirectContent().createTemplate(30, 16);
	    }

	    /**
	     * Adds a header to every page
	     * @see com.itextpdf.text.pdf.PdfPageEventHelper#onEndPage(
	     *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
	     */
	    public void onEndPage(PdfWriter writer, Document document) {
	        PdfPTable table = new PdfPTable(3);
	        // TODO: logo, create String from Globals  
	        //String url = Globals.BUNDLE_NAME + ... ;
	        //String url = "classes/de/uib/configed/gui/images/opsi_full.png";
	        java.net.URL opsi_image_URL = de.uib.configed.Globals.getImageResourceURL("images/opsi_full.png");
	        try {
	        	// add header table with page number
	            table.setWidths(new int[]{24, 24, 2});
	            table.setTotalWidth(headerWidth); // 527
	            table.setLockedWidth(true);
	            table.getDefaultCell().setFixedHeight(20);
	            table.getDefaultCell().setBorder(Rectangle.BOTTOM);
	            table.addCell(header);
	            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
	            table.addCell(String.format(" %d / ", writer.getPageNumber()));
	            PdfPCell cell = new PdfPCell(Image.getInstance(total));
	            cell.setBorder(Rectangle.BOTTOM);
	            table.addCell(cell);
	            table.writeSelectedRows(0, -1, 34, xHeaderTop, writer.getDirectContent());
	            // add footer image
		        document.add(de.uib.utilities.pdf.DocumentElementToPdf.createElement(opsi_image_URL, 25, 25));
	            
	        }
	        catch(DocumentException de) {
	            throw new ExceptionConverter(de);
	        } catch (MalformedURLException ex) {
	        	logging.error("malformed URL --- " + ex);
			} catch (IOException e) { // getInstannce
				logging.error("Error document add footer image --- " + e);
			}
	    }

	    /**
	     * Fills out the total number of pages before the document is closed.
	     * @see com.itextpdf.text.pdf.PdfPageEventHelper#onCloseDocument(
	     *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
	     */
	    public void onCloseDocument(PdfWriter writer, Document document) {
	        ColumnText.showTextAligned(total, Element.ALIGN_LEFT,
	                new Phrase(String.valueOf(writer.getPageNumber() - 1)),
	                2, 2, 0);
	    }
	}  

    
	
	public static void addMetaData(HashMap<String, String> metaData){
		if (metaData == null) {
		    document.addTitle("Document as PDF");
		    document.addSubject("Using iText");
		    document.addKeywords("Java, PDF, iText");
		} else {
			if (metaData.containsKey("title"))
				document.addTitle(metaData.get("title").toString());
			if (metaData.containsKey("subject")) 
				document.addSubject(metaData.get("subject").toString());
			if (metaData.containsKey("keywords")) 
				document.addKeywords(metaData.get("keywords").toString());
		}
	    document.addAuthor(System.getProperty("user.name"));
	    document.addCreator(Globals.APPNAME);
	}
	
	
	public void toPDF()
	{
		File temp = null;
		String fileName = null;;
		HashMap <String, Object> content = new HashMap <String, Object>();

		if( dialog == null ) {
			dialog = new OpenSaveDialog(configed.getResourceValue("OpenSaveDialog.title"));
		} else {
			dialog.setVisible();
		}
		Boolean saveAction = dialog.getSaveAction();
		if (saveAction!=null) {  // 
			if (saveAction) {
				fileName = getFileLocation();
			} else {
				try{
		    	   //create a temp file
		    	   temp = File.createTempFile("report", ".pdf"); 
		    	   fileName = temp.getAbsolutePath();
		    	   //System.out.println("Temp file : " + temp.getAbsolutePath());
		    	}catch(IOException e){
		    	   e.printStackTrace();
		    	}
			}
			try {
				if (fileName == null) {	
					writer = PdfWriter.getInstance(document, new FileOutputStream("report.pdf"));  // fall back
				} else {
					writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
				}
				try {
					TableHeader event = new TableHeader();
					if (metaData.containsKey("header"))
						 event.setHeader(metaData.get("header"));
					else if (metaData.containsKey("title"))
						 event.setHeader(metaData.get("title"));
					writer.setPageEvent(event);
				} catch (Exception ex) {
					logging.error("Error PdfWriter --- " + ex);
				}
	            document.open();
	            addMetaData(metaData);
			    document.add(de.uib.utilities.pdf.DocumentElementToPdf.addTitleLines(metaData));
			    Iterator it = theListOfContentElements.iterator();
			    int chapter = 1;
			    while(it.hasNext()) {
			    	content =(HashMap<String, Object>) it.next();
			    	//System.out.println (content);
			    	if (content.containsKey("table")) {
			    		document.add(de.uib.utilities.pdf.DocumentElementToPdf.createElement((javax.swing.JTable) content.get("table")));
			    	} else 
			    	if (content.containsKey("paragraph")) {
			    		document.add(de.uib.utilities.pdf.DocumentElementToPdf.createElement((String[]) content.get("paragraph")));
			    	} if (content.containsKey("chapter")) {
			    		document.add(de.uib.utilities.pdf.DocumentElementToPdf.createElement((String) content.get("chapter"), chapter));
			    		chapter ++; 
			    	}
			    }
			    document.close();
			}
			catch (FileNotFoundException e) {
				logging.error("file not found: " +  fileName + " --- " + e);
			}
			catch (Exception exp) {
				logging.error("file not found: " +  fileName + " --- " + exp);
			}
			
			if ((saveAction==false) && (temp.getAbsolutePath()!=null)) {
					try {
						Desktop.getDesktop().open(new File(temp.getAbsolutePath()));
					} catch (IOException e) {
						logging.error("file name not valid: " +  temp.getAbsolutePath() + " : " + e);
					}
				
			}
		}
	}
	
	private String getFileLocation (){
		String fileName = null;
		
		File defaultFile = new File("report.pdf");

		chooser = new JFileChooser(exportDirectory);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter( new javax.swing.filechooser.FileNameExtensionFilter("PDF", "pdf" ) );
		chooser.setApproveButtonText("ok");
		chooser.setSelectedFile(defaultFile);
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setDialogTitle(Globals.APPNAME + " " + configed.getResourceValue("DocumentToPdf.chooser"));
		
		int returnVal = chooser.showOpenDialog( de.uib.utilities.Globals.masterFrame);
		if(returnVal == JFileChooser.APPROVE_OPTION) 
		{
			try 
			{
				fileName = chooser.getSelectedFile().getAbsolutePath();
				
				if (!fileName.toLowerCase().endsWith(".pdf"))
						fileName = fileName + ".pdf";
				
				logging.debug(this, "fileName " + fileName);
				
				File file = new File(fileName);
				
				if ( file.exists() )
				{
					int option = JOptionPane.showConfirmDialog(de.uib.utilities.Globals.masterFrame, 
						configed.getResourceValue("DocumentToPdf.showConfirmDialog"), 
						Globals.APPNAME + " Nachfrage",
						JOptionPane.OK_CANCEL_OPTION );
						
					if (option == JOptionPane.CANCEL_OPTION)
						fileName = null;
				}
			}
			catch (Exception fc_e) 
			{
				logging.error("file name not valid: " +  fileName);
			}
		}
		
		if (fileName != null )
		{
			try
			{
				exportDirectory = new File(fileName).getParentFile();
			}
			catch (Exception ex) 
			{
				logging.error("directory not found for " + fileName + " : " + ex);
			}
		}
		return fileName;
	}
	
}