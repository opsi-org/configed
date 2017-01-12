/*
 * DateEventSubject.java
 *
 */

package de.uib.utilities.datetime;
import java.awt.event.*;


public interface DateEventSubject 
{
	void registerDateEventObserver( DateEventObserver o );
	
	void communicateDateEvent (DateEvent e);
}


