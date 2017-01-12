/*
 * FEditDate.java
 *
 */

package de.uib.utilities.swing.timeedit;
/**
 *
 * @author roeder
 */
 
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import de.uib.utilities.*;
import de.uib.utilities.swing.*;
import java.text.*;
import de.uib.utilities.logging.*;


 
public class FEditDate extends FEdit 
	implements /*DateEventObserver, */ 
		org.jdesktop.swingx.event.DateSelectionListener
		,MouseListener, KeyListener
		
{
	public static final Dimension AREA_DIMENSION = new Dimension(210,198);//new Dimension(191,198);
    private DateTimeEditor dateEditor;
    
    protected DateFormat dateFormat;
    
	
	public FEditDate(String initialText, boolean withTime)
	{
		super(initialText);
		areaDimension = this.AREA_DIMENSION;
		
		dateFormat = DateFormat.getDateInstance(Globals.dateFormatStylePattern);//DateFormat.LONG);
		
		dateEditor = new DateTimeEditor(withTime);

		editingArea.add(dateEditor, BorderLayout.CENTER);
		dateEditor.setEditable(true);
		//dateEditor.registerDateEventObserver(this);
		dateEditor.addDateSelectionListener(this);
		dateEditor.addKeyListener(this);
		dateEditor.addMonthViewMouseListener(this);
		
		setStartText(this.initialText);
		
	
    }

    
    @Override 
	protected void createComponents()
	{
		super.createComponents();
		//buttonCommit.setText("");
		//buttonCommit.setPreferredSize(Globals.smallButtonDimension );
		//buttonCancel.setText("");
		
		//buttonCancel.setPreferredSize(Globals.smallButtonDimension );
		//cancelbutton.setVisible(false);
		
		
	}
	
	@Override 
	public void setStartText(String s)
	{
		//System.out.println(" set start text " + s);
		super.setStartText(s);
		//textarea.setText(s);
		setDataChanged(false);
		if (s == null || s.equals(""))
		{
			dateEditor.setDate(false);
		}
		else
		{
			String s1 = null;
			java.util.Date newDate = null;
			
			try
			{
				newDate = dateFormat.parse(s);
				dateEditor.setSelectionDate(newDate);
				setDataChanged(false);
			}
			catch (ParseException pex) 
			{
				try //fallback for standard sql time format
				{
					//logging.debug(" fallback for standard sql time");
					s1 = s;
					if (s1.indexOf(' ') == -1)
						s1 = s1 + " 00:00:00";
					newDate = java.sql.Timestamp.valueOf(s1);
					dateEditor.setSelectionDate(newDate);
					//System.out.println(" date parsed");
					setDataChanged(false);
				}
				catch(IllegalArgumentException ex)
				{
					logging.warning("not valid date: " + s1);
					dateEditor.setDate();
					setDataChanged(true);
				}
			}
		}
		
	}
	
	
	@Override
	public void setVisible(boolean b)
	{
		if (b) dateEditor.requestFocus();
		//get focus in order to receive keyboard events
		super.setVisible(b);
	}
	
	private String getSelectedDate()
	{
		if (dateEditor.getSelectedSqlTime() == null)
			return "";
		
		String selectedDateTimeS = dateEditor.getSelectedSqlTime().toString();
		// return only date part
		return selectedDateTimeS.substring(0, selectedDateTimeS.indexOf(' '));
	}
	
	private String getSelectedDateTime()
	{
		logging.debug(this, " getSelectedDateTime() : " + dateEditor.getSelectedSqlTime() );  
		
		if (dateEditor.getSelectedSqlTime() == null)
			return "";
		
		return dateEditor.getSelectedSqlTime().toString();
	}
	
	protected String getSelectedDateString()
	//at the moment, the implementation decides about the date formatting
	{
		return getSelectedDateTime();
		//return getSelectedDate();
		/*
		java.util.Date date = dateEditor.getSelectedSqlTime();
		
		if (date == null)
			return "";
		
		return DateFormat.getDateInstance(DateFormat.LONG).format(date);
		*/
	}
	
	@Override 
	public String getText()
	{
		initialText = getSelectedDateString(); //set new initial text for use in processWindowEvent
		//System.out.println("FEditDate.getText():   " + initialText);
		//return textarea.getText();
		return initialText;
	}
	
	/*DateEventListener
	public void dateChanged(DateEvent e)
	{
		setDataChanged(true);
	}
	*/
	
	
	//DateSelectionListener
	public void valueChanged(org.jdesktop.swingx.event.DateSelectionEvent ev)
	{
		setDataChanged(true);
		//updateCaller(getSelectedDate());
		updateCaller(getSelectedDateString());
	}
	
	
	// KeyListener
	public void keyPressed (KeyEvent e)
	{
		System.out.println(" key event " + e);
		super.keyPressed(e);
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
		{
			cancel();
		}
		else if (e.getKeyCode() == KeyEvent.VK_ENTER)
		{
			commit();
		}
	}
	public void keyTyped (KeyEvent e)  
	{
		super.keyTyped(e);
	}
	public void keyReleased (KeyEvent e)
	{
		super.keyReleased(e);
	}

	
	
	
	//MouseListener
	public void mouseClicked(MouseEvent e)
	{
		//System.out.println(" MouseEvent " + e);
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2)
			commit();
			
	}
    public void mouseEntered(MouseEvent e) 
    {}
    public void mouseExited(MouseEvent e) 
    {}
    public void mousePressed(MouseEvent e) 
    {}
    public void mouseReleased(MouseEvent e)
	{}
	

}
