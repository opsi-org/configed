package de.uib.utilities;

import de.uib.utilities.logging.*;

public class ExtendedInteger
	implements Comparable
{
	
	final static String infiniteImport = "infinite";
	final static String sINFINITE = "INFINITE";
	final static String displayInfinite = "\u221E";//"INF";
	
	public final static ExtendedInteger INFINITE = new ExtendedInteger(sINFINITE);
	public final static ExtendedInteger ZERO = new ExtendedInteger(0);
	
	private Integer number;
	private String value;
	

	private ExtendedInteger(Integer number, String value)
	{
		this.number = number;
		this.value = value;
	}
	
	public class NotComparableException extends Exception
	{
		public NotComparableException()
		{
			super("INFINITE not comparable to INFINITE");
		}
	}
		
	
	public ExtendedInteger(int intVal)
	{
		number = new Integer(intVal);
		value = "" + intVal;
	}
	
	public ExtendedInteger(ExtendedInteger ei)
	{
		value = ei.getString();
		number = ei.getNumber();
	}

	public ExtendedInteger(String s)
	{
		//logging.info(this, "construct for >>" + s + "<<");
		
		number = null;
		value = null;

		if (s.equals(sINFINITE) || s.equals(infiniteImport) || s.equals(displayInfinite))
		{
			value = sINFINITE;
			
			//logging.info(this, "value >>" + value + "<<");

		}
		else
		{
			try
			{
				number = Integer.decode(s);
				// no exception:
				value = s;
			}
			catch(Exception ex)
			{
				if (s.equals(sINFINITE) || s.toLowerCase().equals(infiniteImport) || s.equals(displayInfinite))
					value = sINFINITE;
				else
					logging.error("possible values are numbers  or \"" + infiniteImport + "\" resp. \"" + displayInfinite + "\"");
			}
		}
	}
	public Integer getNumber()
	{
		return number;
	}
	public String getString()
	{
		return value;
	}
	public String getDisplay()
	{
		if (value.equals(sINFINITE))
			return displayInfinite;
		else
			return value;
	}
	
	@Override
	public boolean equals(Object x)
	{
		if (!(x instanceof ExtendedInteger))
			return false;
		
		ExtendedInteger ei = (ExtendedInteger) x;
		
		return ei.getString().equals(getString());
	}
		

	public ExtendedInteger add(ExtendedInteger ei)
	{
		ExtendedInteger result;
		
		if (ei.equals(INFINITE) || this.equals(INFINITE))
		{
			result =new ExtendedInteger(INFINITE);
		}
		else
		{
			//logging.info(this, " adding " + getNumber() + " + " + ei.getNumber());
			int sum  = this.getNumber() + ei.getNumber();
			result = new ExtendedInteger(new Integer(sum), "" + sum); 
		}
		//logging.info(this, " add giving  " + result);
		return result;
	}
	
	public ExtendedInteger add(Integer z)
	{
		ExtendedInteger result;
		
		if (this.equals(INFINITE))
		{
			result = new ExtendedInteger(INFINITE);
		}
		else
		{
			int sum  = this.getNumber() + z;
			result = new ExtendedInteger(sum, "" + sum);
		}
		return result;
	}
	
	//Interface Comparable
	public int compareTo(Object o) //throws NotComparableException
	{
		ExtendedInteger ei = null;
		
		if (o instanceof ExtendedInteger)
			ei = (ExtendedInteger) ( o );
		
		else if (o instanceof Integer)
			ei = new ExtendedInteger( (Integer) o) ;
				
		//if (o == null)
		//	throw new NotComparableException(); 
		
		
		if (this.equals( INFINITE ) )
		{
			if (ei.equals( INFINITE ) )
				return 0; //throw new NotComparableException(); 
			else
				return 1;
		}
		else
		{
			if (ei.equals( INFINITE ) )
				return -1;
			else
				return (getNumber() - ei.getNumber() );
		}
	}
			
	
	@Override
	public String toString()
	{
		return getString();
	}
	
	
	public static void main(String[] args)
	{
		int result;
		
		try
		{
			System.out.println(" INFINITE.compareTo( INFINITE ) ");
			result = INFINITE.compareTo( INFINITE );
			System.out.println("" + result);
		}
		catch(Exception ex)
		{
			System.out.println( "" + ex );
		}
		
		
		try
		{
			System.out.println(" INFINITE.compareTo( new ExtendedInteger( 20) ) ");
			result = INFINITE.compareTo( new ExtendedInteger( 20 ) );
			System.out.println("" + result);
		}
		catch(Exception ex)
		{
			System.out.println( "" + ex );
		}
		
		
		
		try
		{
			System.out.println(" INFINITE.compareTo( new ExtendedInteger( -20) ) ");
			result = INFINITE.compareTo( new ExtendedInteger( -20 ) );
			System.out.println("" + result);
		}
		catch(Exception ex)
		{
			System.out.println( "" + ex );
		}
		
		try
		{
			System.out.println(" new ExtendedInteger( -20).compareTo( INFINITE)  ");
			result = new ExtendedInteger( -20).compareTo( INFINITE) ;
			System.out.println("" + result);
		}
		catch(Exception ex)
		{
			System.out.println( "" + ex );
		}
			
		try
		{
			System.out.println(" new ExtendedInteger( 20).new ExtendedInteger( 20) ) ");
			result = new ExtendedInteger( 20).compareTo(new ExtendedInteger( 20) );
			System.out.println("" + result);
		}
		catch(Exception ex)
		{
			System.out.println( "" + ex );
		}
		
		try
		{
			System.out.println(" new ExtendedInteger( 20).new ExtendedInteger( -20) ) ");
			result = new ExtendedInteger( 20).compareTo( new ExtendedInteger( -20) );
			System.out.println("" + result);
		}
		catch(Exception ex)
		{
			System.out.println( "" + ex );
		}
		
		
		try
		{
			System.out.println(" new ExtendedInteger( 20).new ExtendedInteger( -20) ) ");
			result = new ExtendedInteger( 20).compareTo( new ExtendedInteger( -20) );
			System.out.println("" + result);
		}
		catch(Exception ex)
		{
			System.out.println( "" + ex );
		}
		
		try
		{
			System.out.println(" new ExtendedInteger( 20).compareTo( INFINITE) ");
			result = new ExtendedInteger( 20).compareTo( INFINITE);
			System.out.println("" + result);
		}
		catch(Exception ex)
		{
			System.out.println( "" + ex );
		}
	}
	
}

