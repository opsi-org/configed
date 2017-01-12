package de.uib.utilities.script;

import java.util.regex.*;
import java.util.*;


public class TestStringReplace
{
	public TestStringReplace()
	{
		
	}
	
	public static void main (String[] args)
	{
		String start = "yy %host% zz";
		String toReplace = "host";
		String replacement = "aa";
		
		for (int i = 0; i < args.length; i++)
		{
			System.out.println("arg " + i + ": " + args[i]);
		}
		
		if (args.length > 0)
			start = args[0];
		
		if (args.length > 1)
			toReplace = args[1];
		
		if (args.length > 2)
			replacement = args[2];
		
		
		
		
		
		System.out.println("result>>"+ 
			start.replace(toReplace, replacement)
			+"<<");
	}
	
	
	
	
}