package de.uib.utilities.pdf;

/**
* @author  m.hammel
* 2014
*/

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Anchor;
import com.itextpdf.text.Annotation;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.List;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

import de.uib.configed.Globals;
import de.uib.utilities.logging.logging;

public abstract class DocumentElementToPdf {
	

	private static BaseFont bf;
	
	
	private static Font catFont = new Font(Font.FontFamily.HELVETICA, 14,
		      Font.BOLD);
	
	private static Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 14,
			  Font.BOLD);
	private static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 10,
		      Font.BOLD);
	private static Font small = new Font(Font.FontFamily.TIMES_ROMAN, 10,
		      Font.NORMAL);
	
	private static ArrayList<Integer> leftAlignmentlist = new ArrayList<Integer>();
	
	
	
	protected abstract void work(); 
	
	public static void setAlignmentLeft(ArrayList<Integer> list) {
		 leftAlignmentlist = list;
	}

	 public static PdfPTable createElement(javax.swing.JTable theTable)
		      throws BadElementException {
		 
		    
		    PdfPTable table = new PdfPTable(theTable.getColumnCount());
		    PdfPCell h;
		    PdfPCell value = null;

		    table.setWidthPercentage(98);
		    
		    BaseColor headerBackground = new BaseColor(150,150,150);
		    BaseColor evenBackground = new BaseColor(230,230,230);
		    BaseColor oddBackground = new BaseColor(250,250,250);
		    Font symbol_font;
		    try {
		    	bf = BaseFont.createFont(BaseFont.SYMBOL,BaseFont.SYMBOL, BaseFont.EMBEDDED);
		    	symbol_font = new Font(bf, 11);
		    } catch (Exception e){
		    	logging.warning("DocumentElementToPdf::createElement " , "BaseFont can't be created :" + e);
		    	symbol_font = small;
		    }
		    PdfPCell defaultCell = table.getDefaultCell();
		    defaultCell.setBackgroundColor(new BaseColor(100,100,100));  
		    
		    for (int i=0; i<theTable.getColumnCount(); i++) {
		    	 h = new PdfPCell(new Phrase(theTable.getColumnName(i)));
		    	 h.setHorizontalAlignment(Element.ALIGN_CENTER);
		    	 h.setBackgroundColor(headerBackground);  
		    	 table.addCell(h);
		    }
		    table.setHeaderRows(1);
		    
		    
		    for (int j=0; j< theTable.getRowCount()  ; j++) 
			    for (int i=0; i<theTable.getColumnCount(); i++) {
			    	value = new PdfPCell(new Phrase(" "));  // reset
			    	String s = "";
			    	try {
			    		s = theTable.getValueAt(j, i).toString();
			    	}
			    	catch (Exception ex){ // nullPointerException, cell empty
			    		s = "";
			    	}	
			    	// div. symbols: http://severinghaus.org/projects/html-entities#LetSym
			    	switch ( s ) {
			    	case "∞":
			    		value = new PdfPCell(new Phrase("\u221e",symbol_font));
			    		break;
			    	case "true":
			    		value = new PdfPCell(new Phrase("\u221a",symbol_font)); // radic
			    		// tests mit 2713  ; 22A8
			    		break;
			    	case "false":
			    		break;
			    	default:
			    		value = new PdfPCell(new Phrase(s, small));
			    	}
			    	if ( j % 2  == 0)
			    		value.setBackgroundColor(evenBackground);
			    	else
			    		value.setBackgroundColor(oddBackground);
			    	//if (leftAlignmentlist.isEmpty()){
			    	//	value.setHorizontalAlignment(Element.ALIGN_CENTER);
			    	//} else {
		    		if (leftAlignmentlist.contains(i)) {
		    			//System.out.println(" column " + i + "leftAlignmentList " + leftAlignmentlist);
		    			value.setHorizontalAlignment(Element.ALIGN_LEFT);
		    		} else {
		    			value.setHorizontalAlignment(Element.ALIGN_CENTER);
		    		}
			    	//}
			    	value.setVerticalAlignment(Element.ALIGN_MIDDLE);
				    table.addCell(value);
			    }
			
		    return table;

	}
	 
	 public static Paragraph createElement (String[] listOfStrings) {
		 Paragraph contentElement = new Paragraph();
		    for (int i = 0; i < listOfStrings.length; i++)
		    	contentElement.add(new ListItem(listOfStrings[i]));
		 return contentElement; 
	 }
	 
	 private static List createElement(boolean numbered, boolean lettered, float symbolIndent, String[] listOfStrings) {
	    List list = new List(numbered, lettered, symbolIndent); // f.e.: (true, false, 10)
	    for (int i = 0; i < listOfStrings.length; i++)
	    	list.add(new ListItem(listOfStrings[i]));
	    return list;
	}
	
	 public static Chapter createElement (String text, int number) {
		 Anchor anchor = new Anchor(text, catFont);
		 anchor.setName(text);
		 return new Chapter(new Paragraph(anchor), number);
	 }
	 
	 public static Image createElement(URL imageSource, float posx, float posy )
	 //http://kievan.hubpages.com/hub/How-to-Create-a-Basic-iText-PDF-Document
	  throws DocumentException, IOException
	  {
		    Image img = com.itextpdf.text.Image.getInstance( imageSource );
		    // no scaling
		    img.setAbsolutePosition( posx, posy );
		    return img;
	  }
	
	 
	 public static Image createElement(String imageSource, float posx, float posy )
	 //http://kievan.hubpages.com/hub/How-to-Create-a-Basic-iText-PDF-Document
	  throws DocumentException, IOException
	  {
		    Image img = com.itextpdf.text.Image.getInstance( imageSource );
		    // no scaling
		    img.setAbsolutePosition( posx, posy );
		    return img;
	  }
	
	 private static PdfPCell createElement(String imageSource) 
		 throws DocumentException, IOException {
		 Image img = com.itextpdf.text.Image.getInstance( imageSource );
		 return new PdfPCell(img);
	 }
	 /*
	 public static Paragraph addTitlePage()
		      throws DocumentException {
			SimpleDateFormat dateFormatter = new SimpleDateFormat ("dd. MMMMM yyyy");

		    Paragraph preface = new Paragraph();
		    // We add one empty line
		    addEmptyLine(preface, 1);
		    // Lets write a big header
		    preface.add(new Paragraph("Title of the document", catFont));

		    addEmptyLine(preface, 1);
		    // Will create: Report generated by: _name, _date
		    preface.add(new Paragraph("Report generated by: " + System.getProperty("user.name") + ", " + dateFormatter.format(new Date()), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		        smallBold));
		    addEmptyLine(preface, 3);
		    preface.add(new Paragraph("This document describes .... ",
		        smallBold));
		    return preface;
		    // Start a new page
		    //document.newPage();
	 }*/
	 
	 public static Paragraph addEmptyLines(int number) 
	 {
		 Paragraph content = new Paragraph();
		    for (int i = 0; i < number; i++) {
		      content.add(new Paragraph(" "));
		    }
		    return content;
	 }
	 public static Paragraph addTitleLines(HashMap<String, String> metaData) throws DocumentException {
		 // TODO timezone
		 	SimpleDateFormat dateFormatter = new SimpleDateFormat ("dd. MMMMM yyyy");
		    // Second parameter is the number of the chapter
		    Paragraph content = new Paragraph();
		    // addEmptyLine(content, 1);
		    if (metaData.containsKey("title"))
		    	content.add(new Paragraph(metaData.get("title"), catFont));
		    content.add(new Paragraph("Report generated by: " + System.getProperty("user.name") + ", " + dateFormatter.format(new Date()), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		    		smallBold));
		    content.add(addEmptyLines(1));
		    return content;
	 }
	 

}
