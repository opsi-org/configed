/*
 * DateEvent.java
 *
 */

package de.uib.utilities.datetime;
/**
 *
 * @author roeder
 */
 
import java.util.Date;
import java.awt.*;


public class DateEvent extends AWTEvent
{
	Object source;
	Date date;
	
	public DateEvent(Object source, Date date)
	{
		super(source, 0);
		this.source = source;
		this.date  = date;
	}
	
	public Object getSource()
	{
		return source;	
	}
	
	public Date getDate()
	{
		return date;	
	}
}
