package de.uib.opsicommand.sshcommand;

import java.util.ArrayList;
import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.configed.gui.ssh.*;
import de.uib.utilities.logging.*;

public class CommandListOpsiProducts /*extends*/implements SSHCommand
{
	private String command;
	private String baseName = "find";
	protected FGeneralDialog dialog = null;
	private boolean needSudo = false;
	// private boolean needRoot = false;
	private boolean needParameter = false;
	private boolean isMultiCommand = false;
	// private int helpColumns=0;
	private String directory ;
	private int priority = 0 ;
	public CommandListOpsiProducts()
	{
		// command = "find /home/opsiproducts -name '*.opsi' | sort";
	}
	public CommandListOpsiProducts(String dir)
	{
		logging.info(this, "CommandListOpsiProducts find in " + dir);
		directory = dir;
		// command = "find " + dir + " -name '*.opsi' | sort";
	}

	@Override
	public String getId()
	{
		return "CommandListOpsiProducts";
	}

	// @Override
	// public String getBasicName()
	// {
	// 	return baseName;
	// }
	
	@Override
	public String getMenuText()
	{
		return null;
	}
	@Override
	public String getParentMenuText()
	{
		return null;
	}
	@Override
	public boolean isMultiCommand()
	{
		return isMultiCommand;
	}

	@Override
	public String getToolTipText()
	{
		return null;
	}
	@Override
	public String getCommand()
	{
		command = "find " + directory + " -name '*.opsi' | sort";
		return command ;
	}
	@Override
	public String getCommandRaw()
	{
		return command;
	}
	/** 
	* Sets the given command
	* @param c (command): String
	**/
	public void setCommand(String c)
	{ command = c; }
	
	@Override
	public boolean needSudo()
	{
		return needSudo;
	}
	
	@Override 
	public boolean needParameter()
	{
		return needParameter;
	}
	@Override
	public FGeneralDialog  getDialog()
	{
		return dialog;
	}
	public void setDirectory(String dir)
	{
		directory = dir;
	}
	@Override
	public int getPriority()
	{
		return priority;
	}
	
	
	@Override
	public ArrayList<String> getParameterList()
	{
		return null;
	}
}