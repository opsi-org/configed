package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import de.uib.utilities.logging.logging;
import java.util.*;

public class TestStringSplit
{
	
	public TestStringSplit(String s)
	{
		String[] splitted = s.split("\\*");
		
		System.out.println("TestStringSplit, splitted input by * >>" + Arrays.toString(splitted));
	}
	
	public static void main(String[] args)
	{
		if (args.length == 0)
			System.out.println(" give a parameter with an * ");
		new TestStringSplit(args[0]);
	}
}
		

