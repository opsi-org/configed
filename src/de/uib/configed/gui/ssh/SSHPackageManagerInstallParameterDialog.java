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
import de.uib.utilities.thread.WaitCursor;
import de.uib.utilities.logging.*;
import javax.swing.border.LineBorder.*;

public class SSHPackageManagerInstallParameterDialog extends SSHPackageManagerParameterDialog
{
	private GroupLayout gpl;
	private JPanel installPanel = new JPanel();
	private JPanel radioPanel = new JPanel();

	private JLabel lbl_install = new JLabel();
	private JLabel lbl_on = new JLabel();
	private JLabel lbl_fullCommand = new JLabel();
	private JLabel lbl_server_dir = new JLabel();
	private JLabel lbl_wget_url = new JLabel();
	private JLabel lbl_wget_includeZsync = new JLabel();
	private JLabel lbl_wget_includeZsync2 = new JLabel();
	private JLabel lbl_wget_compareMd5Sum = new JLabel();
	private JLabel lbl_wget_dir = new JLabel();

	private JLabel lbl_updateInstalled = new JLabel();
	private JLabel lbl_setupInstalled = new JLabel();
	private JLabel lbl_overwriteExisting = new JLabel();

	// private JRadioButton rb_1 ;
	// private JRadioButton rb_2 ;
	private JRadioButton rb_3 ;
	private JRadioButton rb_4 ;

	// private JComboBox cb_opsiproducts;
	// private JComboBox cb_opsirepo;
	private JComboBox cb_verbosity;
	private JComboBox cb_depots;
	private JCheckBox cb_includeZsync;
	private JCheckBox cb_compareMD5;

	private JCheckBox checkb_updateInstalled;
	private JCheckBox checkb_setupInstalled;
	// private JCheckBox checkb_overwriteExisting;

	// private JTextField tf_rb_4_dir;
	private JTextField tf_wget_url;
	private JComboBox cb_wget_dir; // cb_wget_dir
	private JButton btn_searchDir_wget;

	private JComboBox cb_package_path;
	private JButton btn_searchDir_server;
	// private JTextField cb_package_path;
	// private JTextField tf_freeInput;
	private CommandOpsiPackageManagerInstall commandPMInstall = new CommandOpsiPackageManagerInstall();
	
	SSHConnectionExecDialog lastWgetDialog ;
	// protected ConfigedMain main;
	final protected int frameLength = 900;
	final protected int frameHight = 520;
	private String opsiProd = "/home/opsiproducts/";
	public SSHPackageManagerInstallParameterDialog()
	{
		this(null);
	}
	public SSHPackageManagerInstallParameterDialog(ConfigedMain m)
	{
		this(m, "");
	}
	public SSHPackageManagerInstallParameterDialog(ConfigedMain m, String fullPathToPackage)
	{
		super(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.title"));

		WaitCursor waitCursor = new WaitCursor(this.getContentPane());
		main = m;
		this.setSize(frameLength, frameHight);
		init();
		if (!(fullPathToPackage.equals("")))
		{
			cb_package_path.addItem(fullPathToPackage);
			cb_package_path.setSelectedItem(fullPathToPackage);
		}
		pack();	
		enableComponents(false); // wget selected
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setComponentsEnabled(! de.uib.configed.Globals.isGlobalReadOnly());
		this.setVisible (true);
		this.setSize(frameLength, frameHight);
		enableComponents(false); // wget selected
		waitCursor.stop();
	}
	@Override
	protected void setComponentsEnabled(boolean value)
	{
		super.setComponentsEnabled(value);
		// rb_3.setEnabled(value);
		// rb_3.setEditable(value);

		// rb_4.setEnabled(value);
		// rb_4.setEditable(value);

		cb_verbosity.setEnabled(value);
		cb_verbosity.setEditable(value);
		cb_depots.setEnabled(value);
		cb_depots.setEditable(value);

		cb_includeZsync.setEnabled(value);
		// cb_includeZsync.setEditable(value);

		cb_compareMD5.setEnabled(value);
		checkb_updateInstalled.setEnabled(value);
		// checkb_updateInstalled.setEditable(value);

		checkb_setupInstalled.setEnabled(value);
		// checkb_setupInstalled.setEditable(value);
		
		cb_wget_dir.setEnabled(value);
		// cb_wget_dir.setEditable(value);

		tf_wget_url.setEnabled(value);
		tf_wget_url.setEditable(value);

		cb_package_path.setEnabled(value);
		// cb_package_path.setEditable(value);
	}

	protected void init() 
	{
		getRepositotiesFromConfigs(null);
		logging.info(this, "init opsiProd " + this.opsiProd);
		// logging.info(this, "init opsiRepo " + opsiRepo);
		installPanel.setBackground(Globals.backLightBlue);
		buttonPanel.setBackground(Globals.backLightBlue);
		radioPanel.setBackground(Globals.backLightBlue);
		getContentPane().add(installPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		installPanel.setBorder(BorderFactory.createTitledBorder(""));
		radioPanel.setBorder(new LineBorder(de.uib.configed.Globals.blueGrey));
		installPanel.setPreferredSize(new java.awt.Dimension(376, 220));
		{

			lbl_install.setText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelInstall"));
		}

		{
			ButtonGroup group = new ButtonGroup();
			// rb_1 = new JRadioButton(opsiProd);		group.add(rb_1);	addListener(rb_1);
			// rb_2 = new JRadioButton(opsiRepo); 		group.add(rb_2);	addListener(rb_2);
			rb_3 = new JRadioButton(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelFromServer"),true);	group.add(rb_3);	addListener(rb_3);
			rb_4 = new JRadioButton(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetFrom"));	group.add(rb_4);	addListener(rb_4);
		}
		{
			lbl_on.setText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelOn"));
			cb_depots = new JComboBox();
			cb_depots.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.tooltip.depot"));
			cb_depots.addItem(defaultDepot);
			cb_depots.addItem("all");
			PersistenceController persist = PersistenceControllerFactory.getPersistenceController();
			if (persist == null) logging.info(this, "init PersistenceController null");
			opsiProd = persist.PRODUCT_DIRECTORY_defaultvalue;

			LinkedList<String> depotList = persist.getHostInfoCollections().getDepotNamesList();
			for (String depot : depotList)
				cb_depots.addItem(depot);
			cb_depots.addItemListener(new ItemListener() 
			{
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					getRepositotiesFromConfigs((String)cb_depots.getSelectedItem());
					updateLabels();
					changeDepot();
				}
			});
		}
		
			cb_includeZsync = new JCheckBox();
			cb_includeZsync.setSelected(true);
			cb_includeZsync.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jCheckBoxIncludeZsync.tooltip"));
			
			cb_compareMD5 = new JCheckBox();
			cb_compareMD5.setSelected(true);;
			cb_compareMD5.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jCheckBoxCompareMD5.tooltip"));
		{
			// cb_opsiproducts = new JComboBox();
			// cb_opsiproducts.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.tooltip.opsiproduct") + opsiProd);
			// cb_opsiproducts.addItem(defaultProduct);
			// // addProducts(cb_opsiproducts, opsiProd );
			// cb_opsiproducts.addItemListener(new ItemListener() 
			// {
			// 	@Override
			// 	public void itemStateChanged(ItemEvent e) 
			// 	{
			// 		changeProduct();
			// 	}
			// });
			
			// cb_opsirepo = new JComboBox();
			// cb_opsirepo.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.tooltip.cb_opsirepo") + opsiRepo);
			// cb_opsirepo.addItem(defaultProduct);
			// // addProducts(cb_opsirepo, opsiRepo );
			// cb_opsirepo.addItemListener(new ItemListener() 
			// {
			// 	@Override
			// 	public void itemStateChanged(ItemEvent e) 
			// 	{
			// 		changeProduct();
			// 	}
			// });
		}

		{
			cb_verbosity = new JComboBox();
			cb_verbosity.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.tooltip.verbosity"));
			for (int i = 0; i < 5; i++)
				cb_verbosity.addItem(i);
			cb_verbosity.setSelectedItem(1);
			cb_verbosity.addItemListener(new ItemListener() 
			{
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					changeVerbosity();
				}
			});
		}
		{
			lbl_updateInstalled.setText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.updateInstalled"));
			checkb_updateInstalled = new JCheckBox();
			checkb_updateInstalled.addItemListener(new ItemListener() 
			{
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					changeUpdateInstalled();
				}
			});

			lbl_setupInstalled.setText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.setupInstalled"));
			checkb_setupInstalled = new JCheckBox();
			checkb_setupInstalled.addItemListener(new ItemListener() 
			{
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					changeSetupInstalled();
				}
			});
		}
		{
			lbl_server_dir.setText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelOtherPath"));
			lbl_wget_dir.setText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetDir"));
			lbl_wget_url.setText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetUrl"));
			lbl_wget_includeZsync.setText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetIncludeZsync"));
			lbl_wget_includeZsync2.setText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetIncludeZsync2"));
			lbl_wget_compareMd5Sum.setText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetCompareMD5Sum"));

			// cb_wget_dir = new JTextField(opsiProd);
			// cb_wget_dir.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.wget.tooltip.tf_wget_dir"));
			// addListener( cb_wgetdir );
			cb_wget_dir = new JComboBox(); 
			cb_wget_dir.addItem(opsiProd);
			cb_wget_dir.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.wget.tooltip.tf_wget_dir"));
			cb_wget_dir.setEditable(true);

			final String url_def_text = "<"+configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetUrl").replace(":","")+">";
			tf_wget_url = new JTextField(url_def_text);
			tf_wget_url.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.wget.tooltip.tf_wget_url"));
			tf_wget_url.addFocusListener(new FocusAdapter() {
				public void focusGained(FocusEvent e) {
					if ( tf_wget_url.getText().equals(url_def_text))
					{
						tf_wget_url.setSelectionStart(0);
						tf_wget_url.setSelectionEnd(tf_wget_url.getText().length());
					}
				}
			});
			addListener( tf_wget_url );




			// cb_package_path = new JTextField(this.opsiProd);
			// cb_package_path.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.tooltip.tf_sonstiges"));
			// addListener( cb_package_path );
			cb_package_path = new JComboBox(); 
			cb_package_path.addItem(this.opsiProd);
			cb_package_path.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.tooltip.tf_sonstiges"));
			cb_package_path.setEditable(true);




			btn_searchDir_wget = new JButton();
			btn_searchDir_wget.setText(configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.btn_searchDir"));
			btn_searchDir_wget.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						String strcbtext = cb_wget_dir.getEditor().getItem().toString();
						getAndSetDirectoriesIn(cb_wget_dir, strcbtext, true);
					}
				});
			btn_searchDir_server = new JButton();
			btn_searchDir_server.setText(configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.btn_searchDir"));
			btn_searchDir_server.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						String strcbtext = cb_package_path.getEditor().getItem().toString();
						getAndSetDirectoriesIn(cb_package_path, strcbtext, false);
					}
				});
		}

		initLabels();
		initButtons(this);
		initLayout();
		updateLabels();
		changeProduct();
		changeDepot();
		changeVerbosity();
		getAndSetDirectoriesIn(cb_wget_dir, opsiProd, true);
		getAndSetDirectoriesIn(cb_package_path, opsiProd, false);
		// changeFreeInput();
	}

	private void getAndSetDirectoriesIn(final JComboBox cb, final String curdir, final boolean onlyDirs)
	{
		new Thread()
		{
			public void run()
			{
				try 
				{
					SSHConnectExec ssh = new SSHConnectExec();
					Empty_Command getFiles;
					String result = "";
					if (onlyDirs) 
					{
						getFiles = new Empty_Command("ls --color=never -d " + curdir + "/*/");
						result = ssh.exec(getFiles, false);
					}
					else
					{
						getFiles = new Empty_Command("ls --color=never -d " + curdir + "/*/");
						result = ssh.exec(getFiles, false);
						if ((result == null) || (result.trim().equals("null")))
							result = "";


						getFiles = new Empty_Command("ls --color=never " + curdir + "/*.opsi");
						try {
							////// FUNKTIONIERT NUR WENN BERECHTIGUNGEN RICHTIG SIND.....
							// Bricht nach nächster Bedinung ab und schreibt keinen result  ---> try-catch
							String tmp_result = ssh.exec(getFiles, false);
							if ((tmp_result != null) || (tmp_result.trim() != "null"))
								result += tmp_result;
						}
						catch (Exception ei)
						{
							logging.warning(this, "Could not find .opsi files in directory " + curdir + " (It may be the rights are setted wrong.)");
						}
					}
					logging.info(this, "getDirectoriesIn curdir " + curdir + " result " + result);
					setDirectoryItems(cb, result, curdir);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				
			}
		}.start();
	}

	final private void setDirectoryItems(JComboBox cb, String result, String curdir)
	{
		logging.info(this, "setDirectoryItems curdir " + curdir + " result " + result);
		if (result == null)
		{
			logging.warning("getDirectoriesIn could not find directories in " + curdir );
		}
		else
		{
			cb.removeAllItems();
			cb.addItem(curdir);
			logging.debug(this, "setDirectoryItems add " + curdir);
			for (String item : result.split("\n"))
			{
				logging.debug(this, "setDirectoryItems add " + item);
				cb.addItem(item.replace("//", "/"));
			}
			cb.setSelectedItem(curdir);
		}
	}

	private void updateLabels()
	{
		// rb_1.setText(opsiProd);
		// rb_2.setText(opsiRepo);
		cb_package_path.setSelectedItem(this.opsiProd);
		cb_wget_dir.setSelectedItem(opsiProd);
		logging.info(this, "updateLabels this.opsiProd " + this.opsiProd);
		logging.info(this, "updateLabels opsiProd " + opsiProd);
		// logging.info(this, "updateLabels opsiRepo " + opsiRepo);
	}
	

	private void addProducts(JComboBox cb, String dir)
	{
		logging.info(this, "addProducts dir " + dir);
		try 
		{
			SSHCommand command = new CommandListOpsiProducts(dir);
			String opsiproductlist = new SSHConnectExec().exec(command, false ); // only return string
			logging.info(this, "addProducts dir " + dir + " opsiproductlist " + opsiproductlist);
			String[] productItems = opsiproductlist.replaceAll(dir,"").split("\n");
			for (String prod : productItems)
				cb.addItem(prod);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void addListener(Object comp)
	{

		if (!Globals.isGlobalReadOnly())
		{
			if ( comp instanceof JRadioButton )
			{
				((JRadioButton)comp).addActionListener(new ActionListener() 
				{
					@Override
					public void actionPerformed(ActionEvent e) 
					{
						JRadioButton comp_rb = (JRadioButton) e.getSource();
						/*if (comp_rb==rb_1)	enableComponents(true, false, false, false, false);
						else if (comp_rb==rb_2) enableComponents(false, true, false, false, false);
						else*/ if (comp_rb==rb_3) enableComponents(false);
						else if (comp_rb==rb_4) enableComponents(true); // wget is active == true
					}
				});
			}
			else if ( comp instanceof JTextField )
			{
				// if (!Globals.isGlobalReadOnly())
				((JTextField)comp).getDocument().addDocumentListener(new DocumentListener() 
				{
					public void changedUpdate(DocumentEvent documentEvent) { changeProduct(); }
					public void insertUpdate(DocumentEvent documentEvent) { changeProduct(); }
					public void removeUpdate(DocumentEvent documentEvent) { changeProduct(); }
				});
			}
		}
	}
	

	private void updateCommand()
	{
		lbl_fullCommand.setText(commandPMInstall.getCommand());
	}
	private void changeProduct() { changeProduct(null);}
	private void changeProduct(String filename)
	{
		// if (rb_1.isSelected())
		// {
		// 	if (cb_opsiproducts.getSelectedItem() != defaultProduct) commandPMInstall.setOpsiproduct(opsiProd + cb_opsiproducts.getSelectedItem());
		// 	else commandPMInstall.setOpsiproduct("");
		// }
		// else if (rb_2.isSelected())
		// {
		// 	if (cb_opsirepo.getSelectedItem() != defaultProduct) commandPMInstall.setOpsiproduct(opsiRepo + cb_opsirepo.getSelectedItem());
		// 	else commandPMInstall.setOpsiproduct("");
		// }
		// else 
		if (rb_4.isSelected())/* exec wget*/ 
		{
			if (!( ((String) cb_wget_dir.getSelectedItem()).equals("")) 
				//&& !(tf_wget_url.getText().equals("<"+configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetUrl").replace(":","")+">")) 
				)
				commandPMInstall.setOpsiproduct(((String) cb_wget_dir.getSelectedItem()) + getFilenameFromUrl(tf_wget_url.getText()) );
				// commandPMInstall.setOpsiproduct(cb_wget_pdir.getText() );
			if (filename != null) commandPMInstall.setOpsiproduct(filename);
		}
		else if (rb_3.isSelected()) commandPMInstall.setOpsiproduct((String) cb_package_path.getSelectedItem());
		updateCommand();
	}

	// private void changeFreeInput()
	// {
	// 	if (tf_freeInput.getText().trim() != "") commandPMInstall.setFreeInput(tf_freeInput.getText().trim());
	// 	else commandPMInstall.setFreeInput("");
	// 	updateCommand();
	// }
	private void changeVerbosity()
	{
		commandPMInstall.setVerbosity((int)cb_verbosity.getSelectedItem());
		updateCommand();
	}
	private void changeDepot()
	{
		if (cb_depots.getSelectedItem().equals(defaultDepot)) commandPMInstall.setDepot("");
		else commandPMInstall.setDepot((String)cb_depots.getSelectedItem());
		updateCommand();
	}

	private void changeUpdateInstalled()
	{
		// if (checkb_updateInstalled.isSelected().equals(defaultDepot)) commandPMInstall.setDepot("");
		// else 
		commandPMInstall.setUpdateInstalled((boolean)checkb_updateInstalled.isSelected());
		updateCommand();
	}

	private void changeSetupInstalled()
	{
		commandPMInstall.setSetupInstalled((boolean)checkb_setupInstalled.isSelected());
	}

	private void enableComponents(boolean cb_wget_isActive)
	{
		if (!(Globals.isGlobalReadOnly()))
		{
			tf_wget_url.setEnabled(cb_wget_isActive);
			cb_wget_dir.setEnabled(cb_wget_isActive);
			cb_includeZsync.setEnabled(cb_wget_isActive);
			cb_compareMD5.setEnabled(cb_wget_isActive);
			
			cb_package_path.setEnabled(!cb_wget_isActive);
			changeProduct();
		}
		 // enableComponents(false, false, val_tf1, val_tf2, val_tf3);	
	}
	// private void enableComponents(boolean val_cb1, boolean val_cb2, boolean val_tf1, boolean val_tf2, boolean val_tf3)
	// {
	// 	if (!(Globals.isGlobalReadOnly()))
	// 	{
	// 		tf_wget_url.setEnabled(val_tf1);
	// 		cb_includeZsync.setEnabled(val_tf1);
	// 		cb_compareMD5.setEnabled(val_tf1);

	// 		cb_package_path.setEnabled(val_tf3);
	// 		changePdir.setEnabled(val_tf2);
	// 		changeProduct();
	// 	}
	// }

	String mainProduct = "";
	String mainDir = "";
	// download.uib.de/opsi4.0/products/localboot/opsi-configed_4.0.6.3.5.1-3.opsi
	public void doAction1() 
	{
		logging.info(this, " doAction1 install " );
		final SSHConnect ssh = new SSHConnectExec();
		
		SSHCommand_Template commands = new SSHCommand_Template();
		boolean sequential = false;
		if (rb_4.isSelected()) 
		{
			CommandWget wget = getWgetCommand();
			sequential = true;
			if (wget != null)
			{
				commands.addCommand((SSHCommand) wget);
				logging.info(this,"doAction1 wget " + wget);
			}
			if (cb_compareMD5.isSelected())
			{
				String product = mainDir + "/" + getFilenameFromUrl(mainProduct) ;
				commands.addCommand(new Empty_Command("md5_vergleich", 
					" if [ -z $((cat " + product + ".md5" + ") | " + 
					"grep $(md5sum " + product +"  | head -n1 | cut -d \" \" -f1)) ] ; " +
					" then echo \"" +configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.md5sumsAreNotEqual") +
					"\"; else echo \""+ configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.md5sumsAreEqual")+ "\"; fi",
					"", false ));	
			}
		}

		if ( commandPMInstall.checkCommand())
		{
			commands.addCommand((SSHCommand) commandPMInstall);
		}
		try 
		{
			 ((SSHConnectExec)ssh).exec_template(commands, sequential);
			logging.info(this, "doAction1 end " );
		} 
		catch(Exception e)
		{ 
			logging.error(this, "doAction1 Exception while exec_template " + e);
			e.printStackTrace();
		}
	}

	private CommandWget getWgetCommand()
	{
		String d = opsiProd;
		String u = "";
		String additionalProds = "";
		String wgetDir = ((String) cb_wget_dir.getSelectedItem());

		String tmp_tf_dir ="<"+configed.getResourceValue("SSHConnection.ParameterDialog.wget.jLabelDirectory") + ">";
		if ((wgetDir != "") || (wgetDir != tmp_tf_dir))  d = wgetDir;
		else return null;

		String tmp_tf_url ="<"+configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetUrl").replace(":","") + ">";
		if ((tf_wget_url.getText() != "") ||  (tf_wget_url.getText() != tmp_tf_url))   u = tf_wget_url.getText();
		else return null;

		mainProduct = u;
		mainDir = d;
		// additionalProds = u;
		if (cb_includeZsync.isSelected())
		{
			additionalProds = " " + u.replace(".opsi", ".opsi.zsync");
			additionalProds = additionalProds + " " + u.replace(".opsi", ".opsi.md5");
		}

		try
		{
			CommandWget wget = new CommandWget(d, u, additionalProds);
			changeProduct( wget.getProduct());
			return  wget;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	private String getFilenameFromUrl(String url)
	{
		int p=url.lastIndexOf("/");
		String e=url.substring(p+1);
		return e;
	}

	// @Override
	public void doAction2()
	{
		this.setVisible(false);
		this.dispose();
	}


	private void initLayout()
	{
		int pref = GroupLayout.PREFERRED_SIZE;
		int max = Short.MAX_VALUE;
		GroupLayout.Alignment leading = GroupLayout.Alignment.LEADING;
		GroupLayout radioPanelLayout = new GroupLayout((JComponent)radioPanel);
		radioPanel.setLayout(radioPanelLayout);
		JLabel empty_lbl = new JLabel("");
		GroupLayout.SequentialGroup leftToRight = radioPanelLayout.createSequentialGroup();
		GroupLayout.SequentialGroup topToBotton = radioPanelLayout.createSequentialGroup();
		GroupLayout.ParallelGroup leftColumn = radioPanelLayout.createParallelGroup(leading);
		GroupLayout.ParallelGroup rightColumn = radioPanelLayout.createParallelGroup(leading);

		// GroupLayout.ParallelGroup topToBotton = radioPanelLayout.createParallelGroup(leading);
		GroupLayout.ParallelGroup line1 = radioPanelLayout.createParallelGroup(leading);
		GroupLayout.ParallelGroup line2 = radioPanelLayout.createParallelGroup(leading);
		GroupLayout.ParallelGroup line3 = radioPanelLayout.createParallelGroup(leading);
		GroupLayout.ParallelGroup line4 = radioPanelLayout.createParallelGroup(leading);
		GroupLayout.ParallelGroup line5 = radioPanelLayout.createParallelGroup(leading);
		GroupLayout.ParallelGroup line6 = radioPanelLayout.createParallelGroup(leading);
		GroupLayout.ParallelGroup line7 = radioPanelLayout.createParallelGroup(leading);
		
		leftColumn.addGap(Globals.minGapSize)
			.addComponent(rb_3)
			.addGap(Globals.minGapSize)
			.addGroup(radioPanelLayout.createSequentialGroup()
				.addGap(Globals.minGapSize*5)
				.addComponent(lbl_server_dir)
			)
			.addGap(Globals.minGapSize)
			.addComponent(rb_4)
			.addGap(Globals.minGapSize)
			.addGroup(radioPanelLayout.createSequentialGroup()
				.addGap(Globals.minGapSize*5)
				.addComponent(lbl_wget_url)
			)
			.addGap(Globals.minGapSize)
			.addGroup(radioPanelLayout.createSequentialGroup()
				.addGap(Globals.minGapSize*5)
				.addComponent(lbl_wget_dir)
			)
			.addGap(Globals.minGapSize)
			.addGroup(radioPanelLayout.createSequentialGroup()
				.addGap(Globals.minGapSize*5)
				.addComponent(lbl_wget_includeZsync)
			)
			.addGap(Globals.minGapSize)
			.addGroup(radioPanelLayout.createSequentialGroup()
				.addGap(Globals.minGapSize*5)
				.addComponent(lbl_wget_compareMd5Sum)
			)

			.addGap(Globals.minGapSize)
		;
		rightColumn//.addGap(Globals.minGapSize)
			.addGroup(radioPanelLayout.createSequentialGroup()
				.addComponent(cb_package_path, pref,pref,max)
				.addComponent(btn_searchDir_server, pref,pref,pref)
			)
			.addGap(Globals.minGapSize)
			.addComponent(tf_wget_url, pref,pref,max)
			.addGap(Globals.minGapSize)
			.addGroup(radioPanelLayout.createSequentialGroup()
				.addComponent(cb_wget_dir, pref,pref,max)
				.addComponent(btn_searchDir_wget, pref,pref,pref)
			)
			.addGap(Globals.minGapSize)
			.addGroup(radioPanelLayout.createSequentialGroup()
				.addComponent(lbl_wget_includeZsync2, pref,pref,max)
				.addComponent(cb_includeZsync, pref,pref,pref)
			)
			.addGap(Globals.minGapSize)
			.addGroup(radioPanelLayout.createSequentialGroup()
				.addComponent(empty_lbl, pref,pref,max)
				.addComponent(cb_compareMD5, pref, pref,pref)
			)
			.addGap(Globals.minGapSize)
		;
		leftToRight.addGap(Globals.minGapSize).addGroup(leftColumn).addGap(Globals.minGapSize).addGroup(rightColumn).addGap(Globals.minGapSize);


		line1.addComponent(rb_3, pref,pref,pref);
		line2.addGap(Globals.minGapSize*5)
			.addGroup(radioPanelLayout.createSequentialGroup()
				.addGap(Globals.minGapSize)
				.addComponent(lbl_server_dir, pref,pref,pref)
			)
			// .addGroup(radioPanelLayout.createSequentialGroup()
				.addGroup(radioPanelLayout.createParallelGroup()
					.addComponent(cb_package_path, pref,pref,pref)
					.addComponent(btn_searchDir_server, pref,pref,pref)
				)
			// )
		;
		line3.addComponent(rb_4,pref,pref,pref);
		line4.addGap(Globals.minGapSize*5)
			.addGroup(radioPanelLayout.createSequentialGroup()
				.addGap(Globals.minGapSize)
		        		.addComponent(lbl_wget_url, pref,pref,pref)
		        	)
			.addComponent(tf_wget_url, pref,pref,pref)
		;
		line5.addGap(Globals.minGapSize*5)
			.addGroup(radioPanelLayout.createSequentialGroup()
				.addGap(Globals.minGapSize)
				.addComponent(lbl_wget_dir, pref,pref,pref)
			)
			// .addComponent(cb_wget_dir, pref,pref,pref)
			// .addGroup(radioPanelLayout.createSequentialGroup()
				.addGroup(radioPanelLayout.createParallelGroup()
					.addComponent(cb_wget_dir, pref,pref,pref)
					.addComponent(btn_searchDir_wget, pref,pref,pref)
				)
			// )
		;
		line6.addGap(Globals.minGapSize*5)
			// .addGroup(radioPanelLayout.createSequentialGroup()
			.addGroup(radioPanelLayout.createParallelGroup()
				.addGroup(radioPanelLayout.createSequentialGroup()
					.addGap(Globals.minGapSize)
					.addComponent(lbl_wget_includeZsync)
				)
			// .addGroup(radioPanelLayout.createSequentialGroup()
				.addGroup(radioPanelLayout.createSequentialGroup()
				// .addGroup(radioPanelLayout.createParallelGroup()
					.addGap(Globals.minGapSize)
					.addComponent(lbl_wget_includeZsync2)
				)
				.addGroup(radioPanelLayout.createSequentialGroup()
				// .addGroup(radioPanelLayout.createParallelGroup()
					// .addGap(Globals.minGapSize)
					.addComponent(cb_includeZsync)
				)
			)
		;
		line7.addGap(Globals.minGapSize*5)
			// .addGroup(radioPanelLayout.createSequentialGroup()
			.addGroup(radioPanelLayout.createParallelGroup()
				.addGroup(radioPanelLayout.createSequentialGroup()
					.addGap(Globals.minGapSize)
					.addComponent(lbl_wget_compareMd5Sum)
				)
			// .addGroup(radioPanelLayout.createSequentialGroup()
				.addGroup(radioPanelLayout.createSequentialGroup()
				// .addGroup(radioPanelLayout.createParallelGroup()
					.addGap(Globals.minGapSize)
					.addComponent(empty_lbl)
				)
				.addGroup(radioPanelLayout.createSequentialGroup()
				// .addGroup(radioPanelLayout.createParallelGroup()
					// .addGap(Globals.minGapSize)
					.addComponent(cb_compareMD5)
				)
			)
		;
		topToBotton.addGap(Globals.minGapSize)
			.addGroup(line1)
			.addGap(Globals.minGapSize)
			.addGroup(line2)
			.addGap(Globals.minGapSize)
			.addGroup(line3)
			.addGap(Globals.minGapSize)
			.addGroup(line4)
			.addGap(Globals.minGapSize)
			.addGroup(line5)
			.addGap(Globals.minGapSize)
			.addGroup(line6)
			.addGap(Globals.minGapSize)
			.addGroup(line7)
			.addGap(Globals.minGapSize)
		;
		radioPanelLayout.setHorizontalGroup(leftToRight);
		radioPanelLayout.setVerticalGroup(topToBotton);

		GroupLayout installPanelLayout = new GroupLayout((JComponent)installPanel);
		installPanel.setLayout(installPanelLayout);
		installPanelLayout.setHorizontalGroup(
			installPanelLayout.createParallelGroup(leading)
			.addGap(Globals.gapSize)
			.addComponent(lbl_install, pref, pref, max)
			.addGap(Globals.gapSize)
			.addComponent(radioPanel, pref, pref, max)
			.addGap(Globals.gapSize)
			.addGroup(installPanelLayout.createSequentialGroup()
				.addComponent(lbl_on,300, 300, 300)
				.addComponent(cb_depots,pref, pref, max)
			)
			.addGroup(installPanelLayout.createSequentialGroup()
				.addComponent(lbl_verbosity,pref, pref, max)
				.addComponent(cb_verbosity,pref, pref, pref)
			)
			.addGroup(installPanelLayout.createSequentialGroup()
				.addComponent(lbl_setupInstalled,pref, pref, max)
				.addGap((Globals.gapSize*2) + Globals.minGapSize)
				.addComponent(checkb_setupInstalled,pref, pref, pref)
			)
			.addGroup(installPanelLayout.createSequentialGroup()
				.addComponent(lbl_updateInstalled,pref, pref, max)
				.addGap((Globals.gapSize*2) + Globals.minGapSize)
				.addComponent(checkb_updateInstalled,pref, pref, pref)
			)
		);

		installPanelLayout.setVerticalGroup(
			installPanelLayout.createSequentialGroup()
			.addGap(Globals.gapSize)
			.addComponent(lbl_install)
			.addGap(Globals.gapSize)
			.addGap(Globals.gapSize)
			.addComponent(radioPanel)
			.addGap(Globals.gapSize)
			.addGroup(installPanelLayout.createParallelGroup(leading)			
				.addComponent(lbl_on,pref, pref, pref)
				.addComponent(cb_depots,pref, pref, pref)
			)
			.addGap(Globals.gapSize)			
			.addGroup(installPanelLayout.createParallelGroup(leading)
				.addComponent(lbl_verbosity,pref, pref, pref)
				.addComponent(cb_verbosity,pref, pref, pref)
			)
			.addGap(Globals.gapSize)
			.addGroup(installPanelLayout.createParallelGroup(leading)
				.addComponent(lbl_setupInstalled,pref, pref, pref)
				.addComponent(checkb_setupInstalled,pref, pref, pref)
			)
			.addGap(Globals.gapSize+Globals.minGapSize)
			.addGroup(installPanelLayout.createParallelGroup(leading)
				.addComponent(lbl_updateInstalled,pref, pref, pref)
				.addComponent(checkb_updateInstalled,pref, pref, pref)
			)
		);
	}
}