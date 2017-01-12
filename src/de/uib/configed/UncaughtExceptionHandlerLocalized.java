package de.uib.configed;

import de.uib.utilities.logging.logging;

public class UncaughtExceptionHandlerLocalized extends  de.uib.utilities.logging.UncaughtExceptionHandler
{
	private static String lastException = "";
	
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		
		//System.out.println("uncaughtException fProgress " + configed.fProgress); 
		
		de.uib.utilities.thread.WaitCursor.stopAll();
		
		if (configed.fProgress != null)
		{
			try{
				configed.fProgress.stopWaiting();
				configed.fProgress = null;
			}
			catch(Exception ex)
			{
				logging.debug(this, "Exception " + ex);
			}
		}
		
		Integer saveInjectedLogLevel = logging.getInjectedLogLevel();
		System.out.println(" " + this + " saveInjectedLogLevel " + saveInjectedLogLevel);
		if (saveInjectedLogLevel != null)
			logging.injectLogLevel(logging.LEVEL_INFO);
		System.out.println(" " + this + " injectedLogLevel " + logging.getInjectedLogLevel());
		
		
		if (e instanceof Exception)
		{
			logging.warning("Error in thread " + t);
			logging.logTrace((Exception) e);
			
			String errorText = configed.getResourceValue("UncaughtExceptionHandler.notForeseenError") + " " 
				+ ((Exception)e).getMessage(); 
			
			
			if (e.getMessage().endsWith("cannot be cast to javax.swing.Painter"))
			{
				// https://netbeans.org/bugzilla/show_bug.cgi?id=230528
				logging.warning(errorText);
			}
			else
			{
				logging.error(
					errorText
					+ "\n" 
					+ configed.getResourceValue("UncaughtExceptionHandler.pleaseCheckLogfile"),
					(Exception) e
					);
			}
		}
		else
		{
			logging.logTrace(e);
			logging.warning("Thread " + t + " - RunTime Error -  " + e);
			if ( e instanceof java.lang.OutOfMemoryError )
			{

				if (!lastException.equals(e.toString()))
				{
					lastException = e.toString();
					logging.error(
						configed.getResourceValue("UncaughtExceptionHandler.OutOfMemoryError")
						);
				}
			}
			
			
		}
		
		if (saveInjectedLogLevel != null)
				logging.injectLogLevel(saveInjectedLogLevel);
		
		
			
	}
}
