package de.uib.configed.gui.ssh;

import de.uib.configed.gui.*;
import de.uib.opsicommand.*;
import de.uib.opsicommand.sshcommand.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;
import java.nio.charset.Charset;
import java.util.regex.*;
import de.uib.configed.*;
import de.uib.opsidatamodel.*;
import de.uib.utilities.logging.*;
import javax.swing.border.LineBorder.*;

public class SSHPackageManagerParameterDialog extends /*javax.swing.JDialog */ FGeneralDialog
{
	// protected ArrayList<HelpRow> helpLinesSplitted;
	
	protected int frameWidth = 900;
	protected int frameHeight = 650;
	
	protected JPanel buttonPanel = new JPanel();
	protected JLabel lbl_verbosity = new JLabel();
	protected JLabel lbl_freeInput = new JLabel();

	protected JButton btn_help;
	protected JButton btn_execute;
	protected JButton btn_reload;
	protected JButton btn_close;

	protected String defaultProduct = configed.getResourceValue("SSHConnection.ParameterDialog.defaultProduct");
	protected String defaultDepot = configed.getResourceValue("SSHConnection.ParameterDialog.defaultDepot");
	
	protected String opsiProd = "/home/opsiproducts/";
	protected String opsiRepo = "/var/lib/opsi/repository/";
	// protected String opsiRepo = "";// /var/lib/opsi/depot/";


	private String configRepo = "repositoryLocalUrl";
	private String configDepot = "depotLocalUrl"; // soll zu /home/opsiproducts-Config geändert werden!

	private static SSHPackageManagerParameterDialog instance;
	protected ConfigedMain main;
	protected PersistenceController persist = PersistenceControllerFactory.getPersistenceController();

	public SSHPackageManagerParameterDialog(String title)
	{
		 super(null, title, null);
		// super(null,title, false);
		// public FGeneralDialog(JFrame owner, String title, JPanel pane)
		// {
			// super(owner, false);
			// logging.info(this, "created by constructor 1");
			// registerWithRunningInstances();
		// 	this.owner = owner;
		setTitle (title);
		setFont(Globals.defaultFont);
		setIconImage (Globals.mainIcon);
			// additionalPane  = pane;
		// }
		this.setSize(new Dimension(
			de.uib.configed.Globals.dialogFrameDefaultWidth,
			frameHeight));
		this.centerOn(de.uib.configed.Globals.mainFrame);
		this.setBackground(Globals.backLightBlue);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		// getRepositoriesFromConfigs(null);
	}
	protected void setComponentsEnabled(boolean value)
	{
		if (btn_help != null) btn_help.setEnabled(value);
		if (btn_execute != null) btn_execute.setEnabled(value);
	}
	protected void getRepositoriesFromConfigs(String depot)
	{
		logging.info(this, "getRepositoriesFromConfigs depot " + depot);
		TreeMap<String, HashMap<String, Object>> depotProperties = (TreeMap)persist.getHostInfoCollections().getAllDepots();
		logging.info(this, "getRepositoriesFromConfigs depotProperties " + depotProperties);
		

		HashMap<String,Object> firstDepot;
 		if ((depot==null) || (depot == defaultDepot) || (depot == "all")) firstDepot = depotProperties.get(depotProperties.firstKey());
 		else firstDepot =  depotProperties.get(depot);
 		logging.info(this, "getRepositoriesFromConfigs firstDepot " + firstDepot);
		
		
		String o_repo = ((String)firstDepot.get(configRepo)).replace("file://", "");
		if ((o_repo != null) && (o_repo != "null") && (o_repo.trim() != "")) 
			opsiRepo = o_repo + "/";

		logging.info(this, "getRepositoriesFromConfigs o_repo " + o_repo);
		logging.info(this, "getRepositoriesFromConfigs opsiRepo " + opsiRepo);
		
		// try
		// {
		// 	String o_prod = ((String)firstDepot.get(configDepot)).replace("file://", "");
		// 	if (o_prod != null) opsiProd = o_prod;
		// 	logging.info(this, "getRepositoriesFromConfigs depot "+ depot +" opsiProd " + o_prod);
		// } catch (Exception e)
		// {
		// 	e.printStackTrace();
		// }

		logging.info(this, "getRepositoriesFromConfigs opsiRepo " + opsiRepo);
		logging.info(this, "getRepositoriesFromConfigs opsiProd " + opsiProd);
		// // String od = firstDepot.get(configDepot).replace("file://", "");
		// // logging.info(this, "getRepositoriesFromConfigs depot "+ depot +" opsiDepot " + opsiDepot);
		// // if (od != null) opsiDepot = od;
	}

	protected void initLabels()
	{
		lbl_verbosity.setText(configed.getResourceValue("SSHConnection.ParameterDialog.jLabelVerbosity"));
		lbl_freeInput.setText(configed.getResourceValue("SSHConnection.ParameterDialog.jLabelFreeInput"));
	}

	protected void initButtons(final SSHPackageManagerParameterDialog caller)
	{
		{
			// btn_help = new JButton();
			btn_help = new JButton("", Globals.createImageIcon("images/help-about.png", ""));
			btn_help.setText(configed.getResourceValue("SSHConnection.buttonHelp"));
			//buttonPanel.add(btn_help);
			btn_help.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					doActionHelp(caller);
				}
			});

			btn_execute = new JButton();
			buttonPanel.add(btn_execute);
			btn_execute.setText(configed.getResourceValue("SSHConnection.buttonExec"));
			btn_execute.setIcon(Globals.createImageIcon("images/execute16_blue.png", ""));
			if (!(Globals.isGlobalReadOnly()))
				btn_execute.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						if (caller instanceof SSHPackageManagerUninstallParameterDialog)
							((SSHPackageManagerUninstallParameterDialog)caller).doAction1();
						else if (caller instanceof SSHPackageManagerInstallParameterDialog)
							((SSHPackageManagerInstallParameterDialog)caller).doAction1();
					}
				});
			btn_reload = new JButton();
			buttonPanel.add(btn_reload);
			btn_reload.setText(configed.getResourceValue("SSHConnection.buttonPackagesReload"));
			btn_reload.setIcon(Globals.createImageIcon("images/reloadcomplete16.png",""));
			btn_reload.setToolTipText(configed.getResourceValue("SSHConnection.buttonPackagesReload.tooltip"));
			if (!(Globals.isGlobalReadOnly()))
				btn_reload.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						logging.debug(this, "ActionEvent on btn_reload");
						main.reload();
						consolidate();
					}
				});

			btn_close = new JButton();
			buttonPanel.add(btn_close);
			btn_close.setText(configed.getResourceValue("SSHConnection.buttonClose"));
			btn_close.setIcon(Globals.createImageIcon("images/cancelbluelight16.png", ""));
			btn_close.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// doAction2();
					cancel();
				}
			});

			setComponentsEnabled(! de.uib.configed.Globals.isGlobalReadOnly());
		}
	}
	
	protected void consolidate()
	{
		main.reload();
	}

	protected void doActionHelp(final SSHPackageManagerParameterDialog caller)
	{
		SSHConnectionExecDialog dia = null;
		if (caller instanceof SSHPackageManagerUninstallParameterDialog)
		{
			dia = new CommandOpsiPackageManagerUninstall().startHelpDialog();
		}
		else if (caller instanceof SSHPackageManagerInstallParameterDialog)
		{
			dia = new CommandOpsiPackageManagerInstall().startHelpDialog();
		}
		dia.setVisible(true);
	}
	// protected void doActionHelp(final SSHPackageManagerParameterDialog caller))
	// {
	// 	SSHCommand command = null;
	// 	if (caller instanceof SSHPackageManagerUninstallParameterDialog)
	// 	{
	// 		command = new CommandHelp(new CommandOpsiPackageManagerUninstall());
	// 	}
	// 	else if (caller instanceof SSHPackageManagerInstallParameterDialog)
	// 	{
	// 		command = new CommandHelp(new CommandOpsiPackageManagerInstall());
	// 	}
	// 	SSHConnectionExecHelpDialog outputDialog = new SSHConnectionExecHelpDialog(
	// 		command, 
	// 		configed.getResourceValue("SSHConnection.Exec.title")+ " \""+command.getCommand() + "\" "
	// 	);
	// }


	/* This method gets called when button 2 is pressed */
	// public void doAction2()
	// {
	// 	// setVisible(false);
	// 	this.setVisible (false);
	// 	this.dispose();
	// }
	public void cancel()
	{
		super.doAction2();
	}
}