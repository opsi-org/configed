//package de.uib.utilities.script;

import java.util.regex.*;
import java.util.*;


public class TestCmdSplit
{
	public TestCmdSplit()
	{
		
	}
	
	
	public static String[] splitCommand(String cmd)
	{
		ArrayList<String> result = new ArrayList<String>();
		
		String blankDelims = " \t\n\r\f";
		String citMarks = "'\"";
		String lastCitMark = null;
		
		
		StringTokenizer tok = new StringTokenizer(cmd, blankDelims + citMarks, true);
		
		StringBuffer partBuff = null;
		
		
		while (tok.hasMoreTokens())
		{
			String s = tok.nextToken();
			//System.out.println(s);
			if (citMarks.indexOf(s) > -1) 
			{
				if (partBuff == null)
				{
					//start of citation
					partBuff = new StringBuffer();
					lastCitMark = s;
				}
				else
				{
					if (s.equals(lastCitMark))
						//end of citation
					{
						//System.out.println( partBuff.toString() );
						result.add(partBuff.toString());
						partBuff = null;
					}
					else
						partBuff.append(s);
						
				}
			}
			else if (blankDelims.indexOf(s) > -1)
			{
				if (partBuff == null)
				//no buff started, real split
				{
				}
				else
				// buff started
					partBuff.append(s);
			}
			else
			//no delimiter
				if (partBuff == null)
				//no buff started
				{
					//System.out.println(s);
					result.add(s);
				}
				else
					partBuff.append(s);
		}
		
		if (partBuff != null)
		{
			//System.out.println("String not closed: " + partBuff);
			result.add(partBuff.toString());
		}
		
		return result.toArray(new String[] {});
	}
	
	
	public static void main (String[] args)
	{
		
		StringBuffer buf = new StringBuffer();
		
		
		for (int i = 0; i < args.length; i++)
		{
			buf.append(args[i]);
		}
		
		System.out.println("got " + buf);
		
	
		//String start="xterm -title \"ping '%host%'\"  -hold -e ping -c 4 %host%";
		
		String start = buf.toString();
		System.out.println(start);
		
		
		String[] splitted = splitCommand(start);
		System.out.println(Arrays.toString(splitted));
		
		
		
		
				
	}
	
	
	
	
	
	
}