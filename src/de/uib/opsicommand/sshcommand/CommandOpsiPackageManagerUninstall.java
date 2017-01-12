package de.uib.opsicommand.sshcommand;
import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.configed.gui.ssh.*;

public class CommandOpsiPackageManagerUninstall extends CommandOpsiPackageManager implements SSHCommandNeedParameter
{
	protected FGeneralDialog dialog = null;
	private String command;
	private boolean install;
	private int priority = 10;
	// private boolean isMultiCommand = false;
	String opsiproduct = "";
	String depot = "";
	String verbosity = "";
	String keepFiles = "";
	String freeInput = "";
	public CommandOpsiPackageManagerUninstall()
	{
		command = "opsi-package-manager";
	}

	@Override
	public String getId()
	{
		return "CommandOpsiPackageManagerUninstall";
	}
	@Override
	public String getMenuText()
	{
		return configed.getResourceValue("SSHConnection.command.opsipackagemanager_uninstall");
	}
	@Override
	public String getParentMenuText()
	{
		// return "Package-Manager";
		return super.getMenuText();
	}
	
	@Override
	public String getBasicName()
	{
		return "opsi-package-manager";
	}
	@Override
	public String getToolTipText()
	{
		return configed.getResourceValue("SSHConnection.command.opsipackagemanager_uninstall.tooltip");
	}
	@Override
	public FGeneralDialog getDialog()
	{
		return dialog;
	}
	@Override
	public void startParameterGui()
	{
		dialog = new SSHPackageManagerUninstallParameterDialog();
	}
	@Override
	public void startParameterGui(ConfigedMain main)
	{
		dialog = new SSHPackageManagerUninstallParameterDialog(main);
	}
	@Override
	public SSHConnectionExecDialog startHelpDialog()
	{
		SSHCommand command = new CommandHelp(this);
		SSHConnectExec exec = new SSHConnectExec(command, new SSHConnectionExecDialog(command, configed.getResourceValue("SSHConnection.Exec.title") + " \""+command.getCommand() + "\" "));
		// SSHConnectExec exec = new SSHConnectExec();
		// exec.exec(command, true, new SSHConnectionExecDialog(command, configed.getResourceValue("SSHConnection.Exec.title") + " \""+command.getCommand() + "\" "));
		return (SSHConnectionExecDialog) exec.getDialog();
	}
	
	public int getPriority()
	{
		return priority;
	}

	@Override
	public String getCommand()
	{
		command = "opsi-package-manager -q " + verbosity + depot + freeInput + opsiproduct;
		if (needSudo()) return SSHCommandFactory.getInstance().sudo_text+" "+ command + " 2>&1";
		return command + " 2>&1";
	}
	@Override
	public String getCommandRaw()
	{
		return command;
	}
	
	public void setKeepFiles(boolean kF)
	{
		if (kF) keepFiles = " --keep-files ";
		else keepFiles = "";
	}

	public void setOpsiproduct(String prod)
	{
		if (prod != "") opsiproduct = " -r " + prod;
		else opsiproduct = "";
	}
	public void setDepot(String dep)
	{
		if (dep != "") depot = " -d " + dep;
		else depot = "";
	}
	public void setVerbosity(int v_sum)
	{
		String v = "";
		for (int i = 0; i < v_sum; i++) 
			v = v + "v";
		verbosity = " -" + v + " ";
		if (v_sum==0) verbosity = "";
	}
	public void setFreeInput(String fI)
	{
		freeInput = " " + fI ;
	}
	public boolean checkCommand()
	{
		if (opsiproduct == "") return false;
		return true;
	}


}