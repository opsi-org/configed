package de.uib.configed.gui.ssh;

import de.uib.opsicommand.*;
import de.uib.opsicommand.sshcommand.*;

import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.opsidatamodel.*;
import de.uib.utilities.logging.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class SSHMakeProductFileDialog extends FGeneralDialog {
	private JLabel lbl_dir = null;
	private JLabel lbl_productVersion;
	private JLabel lbl_packageVersion;
	private JLabel lbl_exists;
	private JLabel lbl_md5sum;
	private JLabel lbl_zsync;
	private JLabel lbl_versions;
	private JLabel lbl_setRights;
	private JLabel lbl_removeExistingPackage;
	// private JLabel lbl_;
	// private ButtonGroup buttonGroup1;
	// private JRadioButton rb_cancel;
	// private JRadioButton rb_newVersions;
	// private JRadioButton rb_overwrite;
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
	private JButton btn_toPackageManager;
	private JButton btn_exec;
	private JButton btn_cancel;
	private JButton btn_searchDir;
	private String filename = "";
	private ConfigedMain main = null;
	// public SSHMakeProductFileDialog() {
		// this(null)
	// }
	public SSHMakeProductFileDialog(ConfigedMain m) {
		super(null, configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.title"), false);
		main = m; 
		initGUI();
		this.centerOn(de.uib.configed.Globals.mainFrame);
		this.setBackground(Globals.backLightBlue);
		// this.setSize(frameLength, frameHight);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(800, 380);
		this.setVisible (true);
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

	private void getAndSetDirectoriesIn(final String curdir)
	{
		new Thread()
		{
			public void run()
			{
				try
				{
					Empty_Command getDirecoties = new Empty_Command("ls --color=never -d " + curdir + "/*/");
					SSHConnectExec ssh = new SSHConnectExec();
					String result = ssh.exec(getDirecoties, false);logging.info(this, "getDirectoriesIn result " + result);
					setDirectoryItems(result, curdir);
					
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}.start();
	}
	final private void setDirectoryItems(String result, String curdir)
	{
		if (result == null)
		{
			logging.warning("getDirectoriesIn could not find directories in " + curdir );
		}
		else
		{
			cb_mainDir.removeAllItems();
			cb_mainDir.addItem(curdir);
			logging.debug(this, "setDirectoryItems add " + curdir);
			for (String item : result.split("\n"))
			{
				logging.debug(this, "setDirectoryItems add " + item);
				cb_mainDir.addItem(item.replace("//", "/"));
			}
			cb_mainDir.setSelectedItem(curdir);
		}
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

				// lbl_dir = new JLabel(configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.serverDir"));
				PersistenceController persist = PersistenceControllerFactory.getPersistenceController();
				if (persist == null) logging.info(this, "init PersistenceController null");
				String opsiRepo = persist.PRODUCT_DIRECTORY_defaultvalue;
				lbl_dir = new JLabel(opsiRepo);
				{
					ComboBoxModel cb_mainDirModel = 
							new DefaultComboBoxModel(
									/*/home/opsiproducts/*/new String[] { configed.getResourceValue("SSHConnection.ParameterDialog.wget.tooltip.tf_wget_dir") });
					cb_mainDir = new JComboBox();
					cb_mainDir.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.cb_serverDir.tooltip"));
					cb_mainDir.setEditable(true);
					cb_mainDir.setModel(cb_mainDirModel);
					getAndSetDirectoriesIn(configed.getResourceValue("SSHConnection.ParameterDialog.wget.tooltip.tf_wget_dir"));
					// cb_mainDir.addActionListener(new ActionListener()
					// 	{
					// 		public void actionPerformed(ActionEvent e)
					// 		{
					// 			String strcbtext = cb_mainDir.getEditor().getItem().toString();
					// 			if (strcbtext.endsWith("/"))
					// 			{
					// 				getDirectoriesIn(strcbtext);
					// 				cb_mainDir.addItem(strcbtext);
					// 				cb_mainDir.setSelectedItem(strcbtext);
					// 				// cb_mainDir.getEditor().setItem(strcbtext + "....");
					// 			}
					// 		}
					// 	});
				}
				{							
					lbl_exists = new JLabel();
					lbl_exists.setText(configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.packageExists"));
					lbl_packageVersion = new JLabel();
					lbl_packageVersion.setText("    "+configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.packageVersion"));
					lbl_productVersion = new JLabel();
					lbl_productVersion.setText("    "+configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.productVersion"));
					lbl_versions = new JLabel();
					lbl_versions.setText(configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.versions"));
					lbl_setRights = new JLabel();
					lbl_setRights.setText(configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.setRights"));
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
					if (!(Globals.isGlobalReadOnly()))
						btn_getVersions.addActionListener(new ActionListener()
						{
							public void actionPerformed(ActionEvent e)
							{
								doSetActionGetVersions();
							}
						});

					btn_searchDir = new JButton();
					btn_searchDir.setText(configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.btn_searchDir"));
					btn_searchDir.addActionListener(new ActionListener()
						{
							public void actionPerformed(ActionEvent e)
							{
								String strcbtext = cb_mainDir.getEditor().getItem().toString();
								getAndSetDirectoriesIn(strcbtext);
							}
						});
				}
				{
					btn_toPackageManager = new JButton();
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
						.addComponent(lbl_versions, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_packageVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_productVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_zsync, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_md5sum, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_removeExistingPackage, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_setRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGap(Globals.gapSize)
					.addGroup(mainpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addGroup(mainpanelLayout.createSequentialGroup()
							.addComponent(cb_mainDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
							.addComponent(btn_searchDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						)
						.addComponent(btn_getVersions, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(tf_packageVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(tf_productVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(cb_zsync, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(cb_md5sum, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(cb_overwrite, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(cb_setRights, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					)
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
						.addComponent(lbl_versions, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btn_getVersions, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGap(Globals.gapSize)
					.addGroup(mainpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lbl_packageVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(tf_packageVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGap(Globals.gapSize)
					.addGroup(mainpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lbl_productVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(tf_productVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
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
						.addComponent(lbl_removeExistingPackage, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(cb_overwrite, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGap(Globals.gapSize)
					.addGroup(mainpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lbl_setRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(cb_setRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					// .addGap(Globals.gapSize)
				);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private String doActionGetVersions()
	{
		String dir = cb_mainDir.getEditor().getItem().toString() + "/OPSI/control";
		Empty_Command getVersions = new Empty_Command("grep version: " + dir +" --max-count=2  ");
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
	public void cancel()
	{
		super.doAction2();
	}
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
		if (cb_overwrite.isSelected())
		{
			String versions = doActionGetVersions();
			prodVersion = checkVersion(prodVersion, "", versions.split(";;;")[1] );
			packVersion = checkVersion(packVersion, "", versions.split(";;;")[0] );
			filename = dir + "" + getPackageID(dir) + "_" + prodVersion + "-" + packVersion + ".opsi";
			String command = "[ -f " + filename + " ] &&  rm " + filename + " && echo \"File " + filename + " removed\" || echo \"File does not exist\"";
			Empty_Command removeExistingPackage = new Empty_Command(command);
			// Empty_Command removeExistingPackage = new Empty_Command("rm " + dir + "" + getPackageID(dir) + "_" + prodVersion + "-" + packVersion + ".opsi" );
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
		Empty_Command getPackageId = new Empty_Command("cat " + dir + "OPSI/control | grep \"id: \"");
		SSHConnectExec ssh = new SSHConnectExec();
		String result = ssh.exec(getPackageId, false);
		logging.debug(this, "getPackageID result " + result);
		if (result != null)
			return result.replace("id:", "").trim();
		return "";
		// setDirectoryItems(result, curdir);
	}
}  