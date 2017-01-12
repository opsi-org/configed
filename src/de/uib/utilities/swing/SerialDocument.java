package de.uib.utilities.swing;
/**
 * MaskedDocument.java
 * Copyright:     Copyright (c) 2012
 * Organisation:  uib
 * @author Rupert Roeder
 */

import java.util.*;
import javax.swing.text.*;
import de.uib.utilities.logging.*;


public class SerialDocument extends PlainDocument 
{
	
	private SerialElement[] parts;
	
	public SerialDocument(SerialElement[] parts)
	{
		this.parts = parts;
	}
	
	public void insertString(int offs, String s, AttributeSet a)
	{
		
		
	}
	
	private SerialElement findInsertionElement(int offs)
	{
		String text = getText(0, getLength());
		
		int indexOfPartToCheck = 0;
		boolean partOk = true;
		
		while (indexOfPartToCheck < parts.length && partOk)
		{
			part = parts[indexOfPartToCheck];
			int endOfPart = part.getMinLength();
			
			while (endOfPart <= part.getMaxLength() && partOk)
			{
				partText = text.substring(pos, endOfPart);
				partOk = part.check(partText))
				
				if (partOk)
					endOfPart++;
			}
			
			if (partOk)
				indexOfPartToCheck++;
			
		}
		if (!partOk)
			indexOfPartToCheck--;
		
		return indexOfPartToCheck;
	}
		
				
				
		
		
	
	public static interface SerialElement
	{
		public boolean check(String s);
		public int getLastAcceptedIndex(String s);
		public int getMinLength();
		public int getMaxLength();
		public int 
	}
	
	public static class CharElement implements SerialElement 
	{
		char[] allowedChars; 
		public CharElement(char[] allowed)
		{
			this.allowedChars = allowed;
			Arrays.sort(this.allowedChars);
		}
		
		public int getMinLength()
		{
			return 1;
		}
		
		public int getMaxLength()
		{
			return 1;
		}
		
		public boolean check(String partString)
		{
			if (partString.length() == 0)
				return false;
			
			if (Arrays.binarySearch(allowedChars, partString.charAt(0)) >= 0) //first char of partial string is allowed
				return true;
			
			return false;
		}
			
	}
	
	public static class SmallIntElement implements SerialElement
	{
		public SmallIntElement()
		{
		}
		
		public int getMinLength()
		{
			return 0;
		}
		
		public int getMaxLength()
		{
			return 3;
		}
		
		public boolean check(String partString)
		{
			if (partString == null
				|| partString.length() == 0
			)
				return false;
		
			boolean isNumber = true;
			int i = 0;
			while (isNumber && i < partString.length())
			{
				isNumber = Character.isDigit(partString.charAt(i));
				i++;
			}
			
			if (!isNumber)
				return false;
				
			if (partString.length()>2 && partString.charAt(0) != '1')
				return false;
			
			try
			{
				
				int val = Integer.parseInt(partString);
				
				if (val >=0 && val <256)
					return true;
				
				return false;
			}
			catch (NumberFormatException ex)
			{
			}
			
			return false;
		}
	}
				
			
