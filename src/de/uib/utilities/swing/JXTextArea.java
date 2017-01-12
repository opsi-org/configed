package de.uib.utilities.swing;

import java.awt.*;
import javax.swing.*;
import de.uib.configed.*;

public class JXTextArea extends javax.swing.JTextArea
{
	protected int findNextOccurence(String searchS, boolean wrapAtEnd, boolean caseSensitive)
	{
		int pos = getCaretPosition();
		int i = -1;
		int result = pos;
		
		String s;
		String source;
		
		
		if (caseSensitive)
		{
			source = getText();
			s = searchS;
		}
		else
		{
			source = getText().toUpperCase();
			s = searchS.toUpperCase();
		}
		
		
		i = source.indexOf(s, pos + 1);
		
		if (i == -1)
		{
			if (wrapAtEnd)
			{
				i = source.indexOf(s);
			}
		}
		
		if (i > -1)
		{
			result = i;
			
			//System.out.println (" ---------- found " + i);
			setCaretPosition(i);
			setSelectionStart(i);
			setSelectionEnd(i + searchS.length());
		}
		
		return result;
	}
	
}
 
