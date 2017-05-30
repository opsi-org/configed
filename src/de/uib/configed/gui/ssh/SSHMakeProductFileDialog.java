package de.uib.configed.gui.ssh;

import de.uib.opsicommand.*;
import de.uib.opsicommand.sshcommand.*;

import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.opsidatamodel.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.swing.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class SSHMakeProductFileDialog extends FGeneralDialog 
{
	// In dieser Klasse gibt es Linux-Befehle (folgend), die zu Konstanten ausgelagert werden sollen (noch nicht funktioniert)
	public JLabel lbl_exitcode = new JLabel();
	private JLabel lbl_dir = null;
	private JLabel lbl_productVersion;
	private JLabel lbl_packageVersion;
	//private JLabel lbl_exists;
	private JLabel lbl_md5sum;
	private JLabel lbl_zsync;
	private JLabel lbl_versions;
	private JLabel lbl_setRights_now;
	private JLabel lbl_setRights;
	private JLabel lbl_removeExistingPackage;
	private JTextField tf_packageVersion;
	private JTextField tf_productVersion;
	// private JComboBox cb_productDir;
	private JComboBox cb_mainDir;
	private JCheckBox cb_md5sum;
	private JCheckBox cb_zsync;
	private JCheckBox cb_overwrite;
	private JCheckBox cb_setRights;
	private JPanel mainpanel;
	private JPanel buttonPanel;
	private JButton btn_getVersions;
	private JButton btn_setRights;
	private JButton btn_toPackageManager;
	private JButton btn_exec;
	private JButton btn_cancel;
	private JButton btn_searchDir;
	private String filename;
	private ConfigedMain main = null;
	private SSHCommandFactory factory = SSHCommandFactory.getInstance();
	SSHCompletionComboButton autocompletion = new SSHCompletionComboButton();
	//final private int frameWidth = 800;
	//final private int frameHeight = 400;
	
	public SSHMakeProductFileDialog(ConfigedMain m) {
		super(null, configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.title"), false);
		main = m; 
		initGUI();
		
		java.awt.Dimension dim = new java.awt.Dimension(
			de.uib.configed.Globals.dialogFrameDefaultWidth, 
			de.uib.configed.Globals.dialogFrameDefaultHeight + 50
			);
		this.setSize(dim);
		this.centerOn(de.uib.configed.Globals.mainFrame);
		this.setBackground(Globals.backLightBlue);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setVisible (true);
		filename = "";
		setComponentsEnabled(! Globals.isGlobalReadOnly());
	}
	private void setComponentsEnabled(boolean value)
	{
		btn_getVersions.setEnabled(value);
		btn_exec.setEnabled(value);
		// rb_newVersions.setEnabled(value);
		// rb_overwrite.setEnabled(value);
		tf_packageVersion.setEnabled(value);
		tf_productVersion.setEnabled(value);
		cb_mainDir.setEnabled(value);
		cb_md5sum.setEnabled(value);
		cb_zsync.setEnabled(value);
		cb_overwrite.setEnabled(value);
	}
	
	private String setOpsiPackageFilename(String path) 
	{
		filename = path;
		btn_toPackageManager.setEnabled(true);
		btn_toPackageManager.setToolTipText(
			configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.buttonToPackageManager.tooltip") 
			+ " " + filename);
		return filename;
	}
	
	private void initGUI() {
		try {
			// this.setTitle(configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.title"));
			{
				mainpanel = new JPanel();
				buttonPanel = new JPanel();
				mainpanel.setBackground(Globals.backLightBlue);
				buttonPanel.setBackground(Globals.backLightBlue);
				getContentPane().add(mainpanel, BorderLayout.CENTER);
				getContentPane().add(buttonPanel, BorderLayout.SOUTH);
				GroupLayout mainpanelLayout = new GroupLayout((JComponent)mainpanel);
				mainpanel.setLayout(mainpanelLayout);
				mainpanel.setPreferredSize(new java.awt.Dimension(738, 381));
				mainpanel.setBorder(BorderFactory.createTitledBorder(""));
				buttonPanel.setBorder(BorderFactory.createTitledBorder(""));

				lbl_dir = new JLabel(configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.serverDir"));
				// lbl_dir = new JLabel(opsiRepo);
				{
					cb_mainDir = autocompletion.getCombobox();
					btn_searchDir = autocompletion.getButton();
				}
				{							
					//lbl_exists = new JLabel();
					//lbl_exists.setText(configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.packageExists"));
					lbl_packageVersion = new JLabel();
					lbl_packageVersion.setText("    "+configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.packageVersion"));
					lbl_productVersion = new JLabel();
					lbl_productVersion.setText("    "+configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.productVersion"));
					lbl_versions = new JLabel();
					lbl_versions.setText(configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.versions"));
					lbl_setRights = new JLabel();
					lbl_setRights.setText(configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.setRights"));
					lbl_setRights_now = new JLabel();
					lbl_setRights_now.setText(configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.setRights_now"));
					lbl_removeExistingPackage = new JLabel();
					lbl_removeExistingPackage.setText(configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.removeExisting"));
					// lbl_removeExistingPackage.setText("Entferne existierendes Packet");
				}
				{
					tf_packageVersion = new JTextField();
					// grep version:  OPSI/control --max-count=2
					tf_packageVersion.setText(configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.keepVersions"));
					tf_productVersion = new JTextField();
					tf_productVersion.setText(configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.keepVersions"));
				}
				{
					lbl_md5sum = new JLabel();
					lbl_md5sum.setText(configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.lbl_createMd5sum"));
					cb_md5sum = new JCheckBox();
					cb_md5sum.setSelected(true);
					lbl_zsync = new JLabel();
					lbl_zsync.setText(configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.lbl_createZsync"));
					cb_zsync = new JCheckBox();
					cb_zsync.setSelected(true);
					cb_overwrite = new JCheckBox();
					cb_overwrite.setSelected(true);
					cb_setRights = new JCheckBox();
					cb_setRights.setSelected(true);
				}
				{
					btn_getVersions = new JButton();
					btn_getVersions.setText(configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.btn_getVersions"));
					btn_getVersions.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.btn_getVersions.tooltip"));
					if (!(Globals.isGlobalReadOnly()))
						btn_getVersions.addActionListener(new ActionListener()
						{
							public void actionPerformed(ActionEvent e)
							{
								doSetActionGetVersions();
							}
						});

					btn_setRights = new JButton();
					btn_setRights.setText(configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.btn_setRights"));
					btn_setRights.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.btn_setRights.tooltip"));
					if (!(Globals.isGlobalReadOnly()))
						btn_setRights.addActionListener(new ActionListener()
						{
							public void actionPerformed(ActionEvent e)
							{
								doExecSetRights();
							}
						});
				}
				{
					btn_toPackageManager = new JButton();
					btn_toPackageManager.setEnabled(false);
					btn_toPackageManager.setText(configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.buttonToPackageManager"));
					btn_toPackageManager.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.buttonToPackageManager.tooltip"));

					if (!(Globals.isGlobalReadOnly()))
						btn_toPackageManager.addActionListener(new ActionListener()
						{
							public void actionPerformed(ActionEvent e)
							{
								if (main != null)
									new SSHPackageManagerInstallParameterDialog(main, filename);
							}
						});

					btn_exec = new JButton();
					btn_exec.setText(configed.getResourceValue("SSHConnection.buttonExec"));
					btn_exec.setIcon(Globals.createImageIcon("images/execute16_blue.png", ""));
					if (!(Globals.isGlobalReadOnly()))
						btn_exec.addActionListener(new ActionListener()
						{
							public void actionPerformed(ActionEvent e)
							{
								doAction1();
							}
						});
					btn_cancel = new JButton();
					btn_cancel.setText(configed.getResourceValue("SSHConnection.buttonClose"));
					btn_cancel.setIcon(Globals.createImageIcon("images/cancelbluelight16.png", ""));
					btn_cancel.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
						{
							cancel();
						}
					});
					buttonPanel.add(btn_exec);
					buttonPanel.add(btn_toPackageManager);
					buttonPanel.add(btn_cancel);
				}
				mainpanelLayout.setHorizontalGroup(mainpanelLayout.createSequentialGroup()
					.addGap(Globals.gapSize)
					.addGroup(mainpanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(lbl_dir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_setRights_now, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_versions, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_packageVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_productVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_removeExistingPackage, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_zsync, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_md5sum, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_setRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGap(Globals.gapSize)
					.addGroup(mainpanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(mainpanelLayout.createSequentialGroup()
							.addComponent(cb_mainDir, de.uib.configed.Globals.buttonWidth, 2*de.uib.configed.Globals.buttonWidth, Short.MAX_VALUE)
							.addComponent(btn_searchDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						)
						
						.addComponent(btn_setRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btn_getVersions, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGroup(mainpanelLayout.createSequentialGroup()
							.addComponent(tf_packageVersion, de.uib.configed.Globals.buttonWidth, de.uib.configed.Globals.buttonWidth, Short.MAX_VALUE)
							.addGap(50, de.uib.configed.Globals.buttonWidth, Short.MAX_VALUE)
						)
						.addGroup(mainpanelLayout.createSequentialGroup()
							.addComponent(tf_productVersion, de.uib.configed.Globals.buttonWidth, de.uib.configed.Globals.buttonWidth, Short.MAX_VALUE)
							.addGap(50, de.uib.configed.Globals.buttonWidth, Short.MAX_VALUE)
						)
						.addComponent(cb_overwrite, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(cb_zsync, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(cb_md5sum, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(cb_setRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGap(Globals.gapSize)

					//.addComponent(lbl_exitcode, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				);
				mainpanelLayout.setVerticalGroup(mainpanelLayout.createSequentialGroup()
					.addGap(Globals.gapSize)
					.addGroup(mainpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lbl_dir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(cb_mainDir,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btn_searchDir,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGap(Globals.gapSize)
					.addGroup(mainpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lbl_setRights_now, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btn_setRights, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGap(Globals.gapSize)
					.addGroup(mainpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lbl_versions, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btn_getVersions, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGap(Globals.gapSize)
					.addGroup(mainpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lbl_packageVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(tf_packageVersion, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGap(Globals.gapSize)
					.addGroup(mainpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lbl_productVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(tf_productVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGap(Globals.gapSize, 2*Globals.gapSize, 2*Globals.gapSize)
					.addGroup(mainpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lbl_removeExistingPackage, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(cb_overwrite, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGap(Globals.gapSize)
					.addGroup(mainpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lbl_zsync, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(cb_zsync, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGap(Globals.gapSize)
					.addGroup(mainpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lbl_md5sum, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(cb_md5sum, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGap(Globals.gapSize)
					.addGroup(mainpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lbl_setRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(cb_setRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					//.addComponent(lbl_exitcode, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					// .addGap(Globals.gapSize)
				);
			}
		} catch (Exception e) {
			logging.logTrace(e);
		}
	}
	private String doActionGetVersions()
	{
		String dir = cb_mainDir.getEditor().getItem().toString() + "/OPSI/control";
		Empty_Command getVersions = new Empty_Command(factory.str_command_getVersions.replace(factory.str_replacement_dir, dir));
		SSHConnectExec ssh = new SSHConnectExec();
		String result = ssh.exec(getVersions, false);
		if (result == null) 
		{
			logging.error("could not find versions in file " + dir + ".\n\nPlease check if directory exists and contains the file OPSI/control.\n"+"\nPlease also check the rights of the file/s.");
		}
		else 
		{
			logging.info(this, "getDirectories result " + result);
			String[] versions = result.replaceAll("version: ", "").split("\n");
			logging.info(this, "getDirectories result " + versions.toString());
			return versions[0] + ";;;" + versions[1];
			
		}
		return configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.keepVersions");
	}
	public void doSetActionGetVersions()
	{
		String versions = doActionGetVersions();
		if (versions.contains(";;;"))
		{
			tf_packageVersion.setText(versions.split(";;;")[0]);
			tf_productVersion.setText(versions.split(";;;")[1]);
		}
	}
	public void doExecSetRights()
	{
		String dir = cb_mainDir.getEditor().getItem().toString() + "";
		Empty_Command setRights = new Empty_Command("set-rights", "opsi-set-rights " + dir, "set-rights", true);
		SSHConnectExec ssh = new SSHConnectExec();
		SSHConnectionExecDialog.getInstance().setVisible(true);
		String result = ssh.exec(setRights);
	}

	public void cancel()
	{
		super.doAction2();
	}
	
	
	@Override
	public void doAction1()
	{
		SSHCommand_Template str2exec = new SSHCommand_Template();
		boolean sequential = true;
		String dir = cb_mainDir.getEditor().getItem().toString();

		String prodVersion = tf_productVersion.getText();
		String packVersion = tf_packageVersion.getText();
		prodVersion = checkVersion(prodVersion, configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.keepVersions"), "" );
		packVersion = checkVersion(packVersion, configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.keepVersions"), "" );
		CommandOpsimakeproductfile makeProductFile = new CommandOpsimakeproductfile(dir, packVersion, prodVersion, cb_md5sum.isSelected(), cb_zsync.isSelected());
		str2exec.setMainName(makeProductFile.getMenuText());
		if (cb_overwrite.isSelected())
		{
			String versions = doActionGetVersions();
			prodVersion = checkVersion(prodVersion, "", versions.split(";;;")[1] );
			packVersion = checkVersion(packVersion, "", versions.split(";;;")[0] );
			setOpsiPackageFilename(dir + "" + getPackageID(dir) + "_" + prodVersion + "-" + packVersion + ".opsi");
			
			//ToDo: command_strings in sshcommandfactory auslagern
			//
			//
			String command = "[ -f " + filename + " ] &&  rm " + filename + " && echo \"File " + filename + " removed\" || echo \"File did not exist\"";
			// Empty_Command removeExistingPackage = new Empty_Command(str_command_fileexists.replaceAll(str_replacement_filename, filename));
			Empty_Command removeExistingPackage = new Empty_Command(command);
			str2exec.addCommand(removeExistingPackage);

			// Empty_Command removeExistingPackage = new Empty_Command("rm " + dir + "" + getPackageID(dir) + "_" + prodVersion + "-" + packVersion + ".opsi" );
			command = "[ -f " + filename + ".zsync ] &&  rm " + filename + ".zsync && echo \"File " + filename + ".zsync removed\" || echo \"File  " + filename + ".zsync did not exist\"";
			// removeExistingPackage = new Empty_Command(str_command_filezsyncExists.replaceAll(str_replacement_filename, filename));
			removeExistingPackage = new Empty_Command(command);
			str2exec.addCommand(removeExistingPackage);

			command = "[ -f " + filename + ".md5 ] &&  rm " + filename + ".md5 && echo \"File " + filename + ".md5 removed\" || echo \"File  " + filename + ".md5 did not exist\"";
			removeExistingPackage = new Empty_Command(command);
			// removeExistingPackage = new Empty_Command(str_command_filemd5Exists.replaceAll(str_replacement_filename, filename));
			str2exec.addCommand(removeExistingPackage);
		}
		if (cb_setRights.isSelected())
		{
			str2exec.addCommand(new CommandOpsiSetRights(dir) );
		}
		str2exec.addCommand((SSHCommand) makeProductFile);
		SSHConnectExec ssh = new SSHConnectExec(str2exec);
	}
	private String checkVersion(String v, String compareWith, String overwriteWith)
	{
		if (v.equals(compareWith))
			return overwriteWith;
		return v;
	}

	private String getPackageID(String dir)
	{
		// cat " + dir + "OPSI/control | grep "id: "
		Empty_Command getPackageId = new Empty_Command(factory.str_command_catDir.replace(factory.str_replacement_dir, dir));
		SSHConnectExec ssh = new SSHConnectExec();
		String result = ssh.exec(getPackageId, false);
		logging.debug(this, "getPackageID result " + result);
		if (result != null)
			return result.replace("id:", "").trim();
		return "";
		// setDirectoryItems(result, curdir);
	}
}  