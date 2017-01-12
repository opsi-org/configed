package de.uib.utilities.logging;

public class TimeCheck
{
	Object caller;
	String mesg;
	long startmillis;
	int loglevel = logging.LEVEL_CHECK;
	
	public TimeCheck(Object caller, int loglevel, String mesg)
	{
		this.caller = caller;
		this.mesg = mesg;
		this.loglevel = loglevel;
	}
	
	public TimeCheck(Object caller, String mesg)
	{
		this(caller, logging.LEVEL_CHECK, mesg);
	}
	
	public TimeCheck start()
	{
		startmillis = System.currentTimeMillis();
		logging.debugOut(caller, loglevel,  " ------  started: " + mesg + " "); // +  startmillis);
		return this;
	}
	
	public void stop()
	{
		stop(null);
	}
	
	public void stop(String stopMessage)
	{
		String info = stopMessage;
		if (stopMessage == null)
			info = mesg;
		long endmillis = System.currentTimeMillis();
		logging.debugOut(caller, loglevel,  " ------  stopped: " + info + " "); // +  endmillis);
		logging.debugOut(caller, loglevel,  " ======  diff " + (endmillis - startmillis) 
			 + " ms  (" + info + ")" );
	}
}
			
