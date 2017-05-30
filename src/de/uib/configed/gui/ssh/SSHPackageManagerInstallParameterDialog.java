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
	private JLabel lbl_opsi_product = new JLabel();
	private JLabel lbl_wget_url = new JLabel();
	private JLabel lbl_wget_includeZsync = new JLabel();
	private JLabel lbl_wget_includeZsync2 = new JLabel();
	private JLabel lbl_wget_compareMd5Sum = new JLabel();
	private JLabel lbl_wget_dir = new JLabel();
	private JLabel lbl_properties = new JLabel();

	private JLabel lbl_updateInstalled = new JLabel();
	private JLabel lbl_setupInstalled = new JLabel();
	private JLabel lbl_overwriteExisting = new JLabel();

	private JRadioButton rb_from_server ;
	private JRadioButton rb_by_wget ;

	private JComboBox cb_verbosity;
	private JTextField tf_selecteddepots;
	private JButton btn_depotselection;
	private JCheckBox cb_includeZsync;
	private JCheckBox cb_compareMD5;
	private JCheckBox cb_properties;

	private JCheckBox checkb_updateInstalled;
	private JCheckBox checkb_setupInstalled;

	private JTextField tf_wget_url;
// 
	private JComboBox cb_autocompletion_wget; 
	private JButton btn_autocompletion_wget;
// 
	private JComboBox cb_autocompletion_packagepath;
	private JButton btn_autocompletion_packagepath;
	
	private JTextField tf_product;
	
	private CommandOpsiPackageManagerInstall commandPMInstall = new CommandOpsiPackageManagerInstall();
	// Not using following functionality yet
	SSHCompletionComboButton autocompletion_packagepath;
	// SSHCompletionComboButton autocompletion_packagepath = new SSHCompletionComboButton(".opsi");
	// SSHCompletionComboButton autocompletion_wget = new SSHCompletionComboButton();
	SSHCompletionComboButton autocompletion_wget;
	private SSHCommandFactory factory = SSHCommandFactory.getInstance();
	SSHConnectionExecDialog lastWgetDialog;
	
	PersistenceController persist;
	
	FDepotselectionList fDepotList;
	
	private Vector<String> depots;
	private Vector<String> additional_default_paths= new Vector();
	
	private String opsiProd = "/home/opsiproducts/"; // wird noch vom persistencecontroller überschrieben
	private String given_package_path_from_makeproductfile;
	
	protected Vector<String> getAllowedInstallTargets()
	{
		Vector<String> result = new java.util.Vector<String>();
		
		if (persist.isDepotsFullPermission())
		{
			tf_selecteddepots.setEditable(true);
			result.add(persist.DEPOT_SELECTION_NODEPOTS);
			result.add(persist.DEPOT_SELECTION_ALL);
		}
		else
			tf_selecteddepots.setEditable(false);
		
		for (String depot  : persist.getHostInfoCollections().getDepotNamesList())
		{
			if (persist.getDepotPermission( depot ) )
				result.add( depot );
		}
		
		logging.info(this, "getAllowedInstallTargets " + result);
		
		return result;
	}
	
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
		super(
			Globals.APPNAME + "  " +
			configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.title"));

		given_package_path_from_makeproductfile = fullPathToPackage;

		additional_default_paths.addElement(factory.opsipathVarRepository);

		autocompletion_packagepath = new SSHCompletionComboButton(additional_default_paths, ".opsi", fullPathToPackage);
		autocompletion_wget = new SSHCompletionComboButton(additional_default_paths);
		persist = PersistenceControllerFactory.getPersistenceController();
		if (persist == null) logging.info(this, "init PersistenceController null");
		opsiProd = persist.WORKBENCH_defaultvalue;
		
		WaitCursor waitCursor = new WaitCursor(this.getContentPane());
		main = m;
		
		fDepotList = new FDepotselectionList(this){
			@Override
			public void setListData(Vector<? extends String> v)
			{
				if (v == null || v.size() == 0)
				{
					setListData(new Vector<String>());
					jButton1.setEnabled(false);
				}
				else
				{
					super.setListData(v);
					jButton1.setEnabled(true);
				}
			}
				
			@Override
			public void doAction1()
			{
			
				tf_selecteddepots.setText(produceDepotParameter());
				super.doAction1();
			}
		};
		
		init();
		initDepots();
		
		
		pack();	
		enableComponents(false); // wget selected
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setComponentsEnabled(! de.uib.configed.Globals.isGlobalReadOnly());
		
		this.setSize(new Dimension( frameWidth, frameHeight));
		this.centerOn(de.uib.configed.Globals.mainFrame);
		this.setVisible (true);
		if (!(fullPathToPackage.equals("")))
		{
			cb_autocompletion_packagepath.addItem(fullPathToPackage);
			cb_autocompletion_packagepath.setSelectedItem(fullPathToPackage);
		}
		enableComponents(false); // wget selected
		waitCursor.stop();
	}
	
	@Override
	protected void setComponentsEnabled(boolean value)
	{
		super.setComponentsEnabled(value);
		// rb_from_server.setEnabled(value);
		// rb_from_server.setEditable(value);

		// rb_by_wget.setEnabled(value);
		// rb_by_wget.setEditable(value);

		cb_verbosity.setEnabled(value);
		cb_verbosity.setEditable(value);

		cb_includeZsync.setEnabled(value);
		// cb_includeZsync.setEditable(value);

		cb_compareMD5.setEnabled(value);
		checkb_updateInstalled.setEnabled(value);
		// checkb_updateInstalled.setEditable(value);

		checkb_setupInstalled.setEnabled(value);
		// checkb_setupInstalled.setEditable(value);
		
		cb_autocompletion_wget.setEnabled(value);
		// cb_autocompletion_wget.setEditable(value);

		tf_wget_url.setEnabled(value);
		tf_wget_url.setEditable(value);

		cb_autocompletion_packagepath.setEnabled(value);
		// cb_autocompletion_packagepath.setEditable(value);
		
		
		btn_depotselection.setEnabled(value);
		
	}
	
	protected String produceDepotParameter()
	{
		String depotParameter = ""; 
		java.util.List<String> selectedDepots = fDepotList.getSelectedDepots();
		
		if (selectedDepots.size() == 0)
		{
			if (persist.isDepotsFullPermission())
			{
				depotParameter = persist.DEPOT_SELECTION_NODEPOTS;
			}
			else if (depots.size() > 0)
			{
				depotParameter = depots.get(0);
			}
		}
		else
		{
			if (selectedDepots.contains(persist.DEPOT_SELECTION_NODEPOTS))
			{
				depotParameter = persist.DEPOT_SELECTION_NODEPOTS;
			}
			else if (selectedDepots.contains(persist.DEPOT_SELECTION_ALL))
			{
				depotParameter = "all";
			}
			else	
			{
				StringBuffer sb = new StringBuffer();            
				for (String s : selectedDepots)
				{
					sb.append(s);
					sb.append(",");
				}
				depotParameter = sb.toString();
				depotParameter = depotParameter.substring(0, depotParameter.length()-1);
			}
		}
		
		logging.info(this, "produce depot parameter " + depotParameter);
		return depotParameter;
	}
		
	protected void initDepots()
	{
		depots = getAllowedInstallTargets();
		fDepotList.setListData( depots );
		if (depots.size() == 0)
		//probably no permission
		{
			btn_execute.setVisible(false);
		}
		tf_selecteddepots.setText("" + depots.get(0));
	}
		
 
	protected void init() 
	{
		getRepositoriesFromConfigs(null);
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
		//BorderFactory.createTitledBorder(""));//new LineBorder(de.uib.configed.Globals.blueGrey));
		installPanel.setPreferredSize(new java.awt.Dimension(376, 220));
		{

			lbl_install.setText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelInstall"));
			lbl_properties.setText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.lbl_properties"));
			cb_properties = new JCheckBox();
			cb_properties.setSelected(true);
			cb_properties.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.lbl_properties.tooltip"));
			cb_properties.addItemListener(new ItemListener() 
			{
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					changeProperty();
				}
			});
		}

		{
			ButtonGroup group = new ButtonGroup();
			
			rb_from_server = new JRadioButton(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelFromServer"),true);	group.add(rb_from_server);	addListener(rb_from_server);
			rb_by_wget = new JRadioButton(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetFrom"));	group.add(rb_by_wget);	addListener(rb_by_wget);
		}
		{
			lbl_on.setText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelOn"));
			

			
			btn_depotselection = new JButton(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager.depotselection"));
			btn_depotselection.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e)
					{
						initDepots();
						if (btn_depotselection != null)
							fDepotList.centerOn(btn_depotselection);
						fDepotList.setVisible(true);
					}
				}
			);
			
			
			
			tf_selecteddepots = new JTextField();
			tf_selecteddepots.setEditable(false);
		}
		{
			cb_includeZsync = new JCheckBox();
			cb_includeZsync.setSelected(true);
			cb_includeZsync.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jCheckBoxIncludeZsync.tooltip"));
			cb_includeZsync.addItemListener(new ItemListener() 
			{
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					if (e.getStateChange() == ItemEvent.SELECTED)
					{
						cb_compareMD5.setSelected(true);
						cb_compareMD5.setEnabled(true);
					}
					else
					{
						cb_compareMD5.setSelected(false);
						cb_compareMD5.setEnabled(false);
					}
				}
			});

			cb_compareMD5 = new JCheckBox();
			cb_compareMD5.setSelected(true);;
			cb_compareMD5.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jCheckBoxCompareMD5.tooltip"));
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
			lbl_opsi_product.setText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelOtherPath"));
			lbl_wget_dir.setText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetDir"));
			lbl_wget_url.setText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetUrl"));
			lbl_wget_includeZsync.setText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetIncludeZsync"));
			lbl_wget_includeZsync2.setText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetIncludeZsync2"));
			lbl_wget_compareMd5Sum.setText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetCompareMD5Sum"));

			cb_autocompletion_wget = autocompletion_wget.getCombobox();
			btn_autocompletion_wget = autocompletion_wget.getButton();

			final String url_def_text = "<"+configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetUrl").replace(":","")+">";
			tf_wget_url = new JTextField(url_def_text);
			tf_wget_url.setBackground(Globals.backLightYellow);
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


			tf_product = autocompletion_packagepath.getTextField();
			cb_autocompletion_packagepath = autocompletion_packagepath.getCombobox();
			btn_autocompletion_packagepath = autocompletion_packagepath.getButton();
			btn_autocompletion_packagepath.setText(configed.getResourceValue("SSHConnection.ParameterDialog.autocompletion.button_andopsipackage"));
			btn_autocompletion_packagepath.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.autocompletion.button_andopsipackage.tooltip"));
		}

		initLabels();
		initButtons(this);
		initLayout();
		updateLabels();
		changeProduct();
		changeVerbosity();
		// changeFreeInput();
	}

	private void updateLabels()
	{
		// rb_1.setText(opsiProd);
		// rb_2.setText(opsiRepo);
		cb_autocompletion_packagepath.setSelectedItem(this.opsiProd);
		cb_autocompletion_wget.setSelectedItem(opsiProd);
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
			logging.logTrace(e);
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
						else*/ 
						if (comp_rb==rb_from_server) 
						{
							enableComponents(false);
							tf_product.setEnabled(true);
						}
						else if (comp_rb==rb_by_wget)
						{
							enableComponents(true); // wget is active == true
							tf_product.setEnabled(false);
						}
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
		if (rb_by_wget.isSelected())/* exec wget*/ 
		{
			if (!( ((String) cb_autocompletion_wget.getSelectedItem()).equals("")) 
				//&& !(tf_wget_url.getText().equals("<"+configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetUrl").replace(":","")+">")) 
				)
				commandPMInstall.setOpsiproduct(((String) cb_autocompletion_wget.getSelectedItem()) + getFilenameFromUrl(tf_wget_url.getText()) );
				// commandPMInstall.setOpsiproduct(cb_wget_pdir.getText() );
			if (filename != null) commandPMInstall.setOpsiproduct(filename);
		}
		else if (rb_from_server.isSelected()) commandPMInstall.setOpsiproduct(((String) tf_product.getText()).replaceAll("\n", ""));
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

	private void changeProperty()
	{
		commandPMInstall.setProperty(cb_properties.isSelected());
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
			cb_autocompletion_wget.setEnabled(cb_wget_isActive);
			cb_includeZsync.setEnabled(cb_wget_isActive);

			if (cb_includeZsync.isSelected())
				cb_compareMD5.setEnabled(true);
			else
				cb_compareMD5.setEnabled(false);
			if (! cb_wget_isActive)
				cb_compareMD5.setEnabled(false);
			
			cb_autocompletion_packagepath.setEnabled(!cb_wget_isActive);
			changeProduct();
		}
		 // enableComponents(false, false, val_tf1, val_tf2, val_tf3);	
	}
	String mainProduct = "";
	String mainDir = "";
	// download.uib.de/opsi4.0/products/localboot/opsi-configed_4.0.6.3.5.1-3.opsi
	
	@Override
	public void doAction1() 
	{
		logging.info(this, " doAction1 install " );
		final SSHConnectExec ssh = new SSHConnectExec();
	
		SSHCommand_Template commands = new SSHCommand_Template();
		boolean sequential = false;
		if (rb_by_wget.isSelected()) 
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
				// ToDo: Folgender Parameter String (befehl) muss noch in die klasse sshcommandfactory ausgelagert werden
				commands.addCommand(new Empty_Command("md5_vergleich", 
					" if [ -z $((cat " + product + ".md5" + ") | " + 
					"grep $(md5sum " + product +"  | head -n1 | cut -d \" \" -f1)) ] ; " +
					" then echo \"" +configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.md5sumsAreNotEqual") +
					"\"; else echo \""+ configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.md5sumsAreEqual")+ "\"; fi",
					"", false ));	
			}
		}
		
		else
		{
			String product = tf_product.getText();
			changeProduct(product);
			// ToDo: Folgender Parameter String (befehl) muss noch in die klasse sshcommandfactory ausgelagert werden
			// commands.addCommand(new Empty_Command("md5_vergleich", 
			// 		" if [ -z $((cat " + product + ".md5" + ") | " + 
			// 		"grep $(md5sum " + product +"  | head -n1 | cut -d \" \" -f1)) ] ; " +
			// 		" then echo \"" +configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.md5sumsAreNotEqual") +
			// 		"\"; else echo \""+ configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.md5sumsAreEqual")+ "\"; fi",
			// 		"", false ));	
		}
		

		if ( commandPMInstall.checkCommand())
		{
			commands.addCommand((SSHCommand) commandPMInstall);
		}
		
		/* produces second dialog
		ssh.setDialog(new SSHConnectionExecDialog
					("Installiere", commands, "Zur Integration "));
		*/
		
		try 
		{
			 ((SSHConnectExec)ssh).exec_template(commands, 
			 	// new SSHConnectionExecDialog
				//	("Installiere", commands, "Zur Integration "),
			 	 sequential);
			logging.info(this, "doAction1 end " );
		} 
		catch(Exception e)
		{ 
			logging.error(this, "doAction1 Exception while exec_template " + e);
			logging.logTrace(e);
		}
	}

	private CommandWget getWgetCommand()
	{
		String d = opsiProd;
		String u = "";
		String additionalProds = "";
		String wgetDir = ((String) cb_autocompletion_wget.getSelectedItem());

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
			logging.logTrace(e);
		}
		return null;
	}

	private String getFilenameFromUrl(String url)
	{
		int p=url.lastIndexOf("/");
		String e=url.substring(p+1);
		return e;
	}

	@Override
	public void doAction2()
	{
		this.setVisible(false);
		this.dispose();
	}
	
	@Override
	public void leave()
	{
		fDepotList.exit();
		super.leave();
	}
	
	
	private void initLayout()
	{
		int PREF = GroupLayout.PREFERRED_SIZE;
		int MAX = Short.MAX_VALUE;
		GroupLayout.Alignment leading = GroupLayout.Alignment.LEADING;
		GroupLayout.Alignment center = GroupLayout.Alignment.CENTER;
		
		GroupLayout radioPanelLayout = new GroupLayout(radioPanel);
		radioPanel.setLayout(radioPanelLayout);
		radioPanelLayout.setVerticalGroup( radioPanelLayout.createSequentialGroup()
			.addGap(2*Globals.gapSize)
			.addGroup( radioPanelLayout.createParallelGroup(center)
				.addComponent(rb_from_server, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				)
			.addGap(Globals.gapSize)
			.addGroup( radioPanelLayout.createParallelGroup(center)
				//.addComponent(lbl_server_dir, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				.addComponent(cb_autocompletion_packagepath, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				.addComponent(btn_autocompletion_packagepath, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				)
			.addGap(Globals.gapSize)
			.addGroup( radioPanelLayout.createParallelGroup(center)
				.addComponent(lbl_opsi_product, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				.addComponent(tf_product, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				)
			.addGap(2*Globals.gapSize)
			.addGroup( radioPanelLayout.createParallelGroup(center)
				.addComponent(rb_by_wget, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				)
			.addGap(Globals.gapSize)
			.addGroup( radioPanelLayout.createParallelGroup(center)
				.addComponent(lbl_wget_url, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				.addComponent(tf_wget_url, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				)
			.addGroup( radioPanelLayout.createParallelGroup(center)
				.addComponent(lbl_wget_dir, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				.addComponent(cb_autocompletion_wget, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				.addComponent(btn_autocompletion_wget, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight) 
				)
			.addGroup( radioPanelLayout.createParallelGroup(center)
				.addComponent(lbl_wget_includeZsync, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				.addComponent(cb_includeZsync,  Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				.addComponent(lbl_wget_includeZsync2, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				)
			.addGroup( radioPanelLayout.createParallelGroup(center)
				.addComponent(lbl_wget_compareMd5Sum, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				.addComponent(cb_compareMD5,  Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				)
			.addGap(2*Globals.gapSize)
			);
		
		
		radioPanelLayout.setHorizontalGroup( radioPanelLayout.createSequentialGroup()
			.addGap(2*Globals.gapSize)
			
			.addGroup( radioPanelLayout.createParallelGroup(  )
				.addComponent(rb_from_server,  PREF, PREF, PREF)
				//.addComponent(lbl_server_dir, PREF, PREF, PREF)
				.addGroup( radioPanelLayout.createSequentialGroup()
					.addGap(2*Globals.gapSize)
					.addComponent(lbl_opsi_product, PREF, PREF, PREF)
				)
				.addComponent(rb_by_wget,  PREF, PREF, PREF)
				.addGroup( radioPanelLayout.createSequentialGroup()
					.addGap(2*Globals.gapSize)
					.addComponent(lbl_wget_url, PREF, PREF, PREF)
				)
				.addGroup( radioPanelLayout.createSequentialGroup()
					.addGap(2*Globals.gapSize)
					.addComponent(lbl_wget_dir, PREF, PREF, PREF)
				)
				.addGroup( radioPanelLayout.createSequentialGroup()
					.addGap(2*Globals.gapSize)
					.addComponent(lbl_wget_includeZsync,PREF, PREF, PREF)
				)
				.addGroup( radioPanelLayout.createSequentialGroup()
					.addGap(2*Globals.gapSize)
					.addComponent(lbl_wget_compareMd5Sum, PREF, PREF, PREF)
				)
			)
			.addGap(Globals.gapSize)
			.addGroup( radioPanelLayout.createParallelGroup(  )
				.addGroup( radioPanelLayout.createSequentialGroup()
					.addComponent(cb_autocompletion_packagepath, Globals.buttonWidth, Globals.buttonWidth, MAX)
					.addComponent(btn_autocompletion_packagepath, PREF, PREF, PREF)
				)
				.addGroup( radioPanelLayout.createSequentialGroup()
					.addComponent(tf_product, Globals.buttonWidth, Globals.buttonWidth, MAX)
				)
				.addGroup( radioPanelLayout.createSequentialGroup()
					.addComponent(tf_wget_url, Globals.buttonWidth,  Globals.buttonWidth, MAX)
				)
				.addGroup( radioPanelLayout.createSequentialGroup()
					.addComponent(cb_autocompletion_wget, Globals.buttonWidth,  Globals.buttonWidth, MAX)
					.addComponent(btn_autocompletion_wget, PREF, PREF, PREF)
				)
				.addGroup( radioPanelLayout.createSequentialGroup()
					.addComponent(cb_includeZsync,  PREF, PREF, PREF)
					.addGap(Globals.gapSize)
					.addComponent(lbl_wget_includeZsync2, PREF, PREF, PREF)
				)
				.addGroup( radioPanelLayout.createSequentialGroup()
					.addComponent(cb_compareMD5,  PREF, PREF, PREF)
				)
			)
			
			.addGap(2*Globals.gapSize)
		);
			
		
		GroupLayout installPanelLayout = new GroupLayout(installPanel);
		installPanel.setLayout(installPanelLayout);
		installPanelLayout.setHorizontalGroup(installPanelLayout.createSequentialGroup()
				.addGap(Globals.gapSize)
				.addGroup(installPanelLayout.createParallelGroup(center)
					.addComponent(lbl_install, PREF, PREF, MAX)
					.addComponent(radioPanel, PREF, PREF, MAX)
					.addGroup(installPanelLayout.createSequentialGroup()
						.addGroup(installPanelLayout.createParallelGroup()
							.addGroup(installPanelLayout.createSequentialGroup()
								.addComponent(lbl_on,PREF, PREF, PREF)
								.addGap(Globals.gapSize)
								.addComponent(tf_selecteddepots, PREF, PREF, Short.MAX_VALUE)
							)
							.addComponent(lbl_verbosity,PREF, PREF, PREF)
							.addComponent(lbl_properties,PREF, PREF, PREF)
							.addComponent(lbl_setupInstalled,PREF, PREF, PREF)
							.addComponent(lbl_updateInstalled,PREF, PREF, PREF)
						)
						.addGap(Globals.gapSize)
						.addGroup(installPanelLayout.createParallelGroup()
							.addComponent(btn_depotselection, PREF, PREF, PREF) //Globals.iconWidth, Globals.iconWidth, Globals.iconWidth) 
							.addComponent(cb_verbosity, Globals.iconWidth, Globals.iconWidth, Globals.iconWidth) 
							.addComponent(cb_properties,PREF, PREF, PREF)
							.addComponent(checkb_setupInstalled,PREF, PREF, PREF)
							.addComponent(checkb_updateInstalled,PREF, PREF, PREF)
						)
						.addGap(Globals.gapSize, Globals.gapSize, MAX)
					)
				)
				.addGap(Globals.gapSize)
			);

		installPanelLayout.setVerticalGroup(
			installPanelLayout.createSequentialGroup()
			.addGap(Globals.gapSize)
			.addComponent(lbl_install)
			.addGap(Globals.gapSize)
			.addGap(Globals.gapSize)
			.addComponent(radioPanel)
			.addGap(Globals.gapSize)
			.addGroup(installPanelLayout.createParallelGroup(center)			
				.addComponent(lbl_on,Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				//.addComponent(cb_depots,leading, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				.addComponent(tf_selecteddepots, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
				.addComponent(btn_depotselection, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
			)
			.addGap(Globals.gapSize)			
			.addGroup(installPanelLayout.createParallelGroup(center)
				.addComponent(lbl_verbosity,Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				.addComponent(cb_verbosity,leading, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
			)
			.addGap(Globals.gapSize)
			.addGroup(installPanelLayout.createParallelGroup(center)
				.addComponent(lbl_properties,Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				.addComponent(cb_properties, leading, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
			)
			.addGap(Globals.gapSize)
			.addGroup(installPanelLayout.createParallelGroup(center)
				.addComponent(lbl_setupInstalled,Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				.addComponent(checkb_setupInstalled, leading, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
			)
			.addGap(Globals.gapSize)
			.addGroup(installPanelLayout.createParallelGroup(center)
				.addComponent(lbl_updateInstalled,Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				.addComponent(checkb_updateInstalled, leading ,Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
			)
			.addGap(Globals.gapSize)
		);
	}
}
