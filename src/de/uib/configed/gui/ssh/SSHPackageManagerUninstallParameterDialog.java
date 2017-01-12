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
// import java.nio.charset.Charset;
// import java.util.regex.*;
import de.uib.configed.*;
import de.uib.opsidatamodel.*;
import de.uib.utilities.thread.WaitCursor;
import de.uib.utilities.logging.*;
import javax.swing.border.LineBorder.*;
import de.uib.utilities.swing.*;

public class SSHPackageManagerUninstallParameterDialog extends /*javax.swing.JDialog */ SSHPackageManagerParameterDialog
{
	
	private GroupLayout gpl;
	private JPanel uninstallPanel = new JPanel();

	private JLabel lbl_uninstall = new JLabel();
	private JLabel lbl_product = new JLabel();
	private JLabel lbl_on = new JLabel();
	private JLabel lbl_fullCommand = new JLabel();
	private JLabel lbl_keepFiles = new JLabel();


	private JComboBox cb_opsiproducts;
	private JComboBox cb_verbosity;
	private JComboBox cb_depots;
	private JCheckBox checkb_keepFiles;

	private JTextField tf_freeInput;
	
	final protected int frameLength = 500;
	final protected int frameHight = 270;

	public String wgetResult="";
	private CommandOpsiPackageManagerUninstall commandPMUninstall = new CommandOpsiPackageManagerUninstall();
	
	public SSHPackageManagerUninstallParameterDialog()
	{
		this(null);
	}
	public SSHPackageManagerUninstallParameterDialog(ConfigedMain m)
	{
		super(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_uninstall.title"));
		WaitCursor waitCursor = new WaitCursor(this.getContentPane());
		main = m;
		init();
		pack();
		this.setSize(frameLength, frameHight);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setComponentsEnabled(! de.uib.configed.Globals.isGlobalReadOnly());
		waitCursor.stop();
		this.setVisible (true);
	}

	@Override
	protected void setComponentsEnabled(boolean value)
	{
		super.setComponentsEnabled(value);
		cb_opsiproducts.setEnabled(value);
		cb_opsiproducts.setEditable(value);
		cb_verbosity.setEnabled(value);
		cb_verbosity.setEditable(value);
		cb_depots.setEnabled(value);
		cb_depots.setEditable(value);
		checkb_keepFiles.setEnabled(value);
		// checkb_keepFiles.setEditable(value);
	}
	protected void init() 
	{
		uninstallPanel.setBackground(Globals.backLightBlue);
		buttonPanel.setBackground(Globals.backLightBlue);
		getContentPane().add(uninstallPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		uninstallPanel.setBorder(BorderFactory.createTitledBorder(""));
		uninstallPanel.setPreferredSize(new java.awt.Dimension(376, 220));
		{
			lbl_uninstall.setText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_uninstall.jLabelUninstall"));
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
			// tf_freeInput = new JTextField();
			// tf_freeInput.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.tooltip.freeInput"));
			// tf_freeInput.getDocument().addDocumentListener(new DocumentListener() 
			// {
			// 	public void changedUpdate(DocumentEvent documentEvent) { changeFreeInput(); }
			// 	public void insertUpdate(DocumentEvent documentEvent) { changeFreeInput(); }
			// 	public void removeUpdate(DocumentEvent documentEvent) { changeFreeInput(); }
			// });
			lbl_keepFiles.setText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_uninstall.jLabelKeepFiles"));
			checkb_keepFiles = new JCheckBox();
			checkb_keepFiles.addItemListener(new ItemListener() 
			{
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					changeKeepFiles();
				}
			});
		}
		{
			lbl_product.setText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_uninstall.jLabelProduct"));
			cb_opsiproducts = new JComboBox();
			cb_opsiproducts.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_uninstall.tooltip.jLabelProduct"));
			cb_opsiproducts.addItemListener(new ItemListener() 
			{
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					changeProduct();
				}
			});
		}
		{
			lbl_on.setText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_uninstall.jLabelOn"));
			cb_depots = new JComboBox();
			cb_depots.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_uninstall.tooltip.opsidepot"));
			cb_depots.addItem(defaultDepot);	
			cb_depots.addItemListener(new ItemListener() 
			{
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					// getRepositotiesFromConfigs(cb_depots.getSelectedItem());
					updateProductsOnDepot();
					changeDepot();

				}
			});
			cb_depots.addItem("all");
			LinkedList<String> depotList = persist.getHostInfoCollections().getDepotNamesList();
			 for (String depot : depotList)
			 	cb_depots.addItem(depot);
			cb_depots.setSelectedItem(defaultDepot);
			
		}
		initLabels();
		initButtons(this);
		initLayout();
		updateProductsOnDepot();
		changeDepot();
		changeProduct();
		changeVerbosity();
		// changeFreeInput();
	}





	
	private void updateProductsOnDepot()
	{

		logging.info(this, "updateProductsOnDepot in cb_opsiproducts");
		cb_opsiproducts.removeAllItems();
		if (persist == null) logging.error(this, "updateProductsOnDepot PersistenceController null" );
		LinkedList<String> productnames;
		if ( ((String)cb_depots.getSelectedItem()) == defaultDepot )
		{
			productnames = new LinkedList((ArrayList)persist.getAllNetbootProductNames());
			productnames.addAll((ArrayList)persist.getAllLocalbootProductNames());
		}
		else
		{
			productnames = new LinkedList((ArrayList)persist.getAllNetbootProductNames((String)cb_depots.getSelectedItem()));
			productnames.addAll((ArrayList)persist.getAllLocalbootProductNames((String)cb_depots.getSelectedItem()));
		}
		Collections.sort(productnames);
		for (String item : productnames)
			cb_opsiproducts.addItem(item);
	}

	private void updateCommand()
	{
		lbl_fullCommand.setText(commandPMUninstall.getCommand());
	}
	private void changeKeepFiles()
	{
		commandPMUninstall.setKeepFiles((boolean)checkb_keepFiles.isSelected());
		updateCommand();
	}
	// private void changeFreeInput( )
	// {
	// 	commandPMUninstall.setFreeInput(tf_freeInput.getText().trim());
	// 	updateCommand();
	// }
	private void changeVerbosity()
	{
		commandPMUninstall.setVerbosity((int)cb_verbosity.getSelectedItem());
		updateCommand();
	}
	private void changeDepot()
	{
		if (cb_depots.getSelectedItem().equals(defaultDepot))
			commandPMUninstall.setDepot("");
		else commandPMUninstall.setDepot((String)cb_depots.getSelectedItem());
		updateCommand();
	}
	private void changeProduct()
	{
		commandPMUninstall.setOpsiproduct((String)cb_opsiproducts.getSelectedItem());
		updateCommand();
	}




	boolean execFinished ;
	/* This method is called when button 1 is pressed */
	public void doAction1() 
	{
		logging.info(this, "doAction1 uninstall  ");
		Thread execThread = new Thread()
		{
			public void run()
			{
				try
				{
					logging.info (this, "start exec thread ");
					if ( commandPMUninstall.checkCommand())
					{
						logging.info(this, "start exec from doAction1");
						SSHConnectExec ssh = new SSHConnectExec((SSHCommand) commandPMUninstall);
						// ssh.exec((SSHCommand) commandPMUninstall);
						cb_opsiproducts.removeItem( (String)cb_opsiproducts.getSelectedItem());
						execFinished = true;
					}
					logging.debug(this, "end exec thread");
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		};

		Thread reloadThread = new Thread()
		{
			public void run()
			{
				try
				{
					boolean ready = false;
					logging.info (this, "start reload thread ");

					while (!ready)
					{
						if (execFinished )
						{
							logging.info(this, "start reload from doAction1");
							main.reload();
							ready = true;
						}
						else Thread.sleep(1000);
					}
					logging.info (this, "end reload thread ");
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		try
		{
			execThread.start();
			reloadThread.start();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		} 
	}

	// @Override
	public void doAction2()
	{
		// productMissing=false;
		execFinished=true;
		this.setVisible(false);
		this.dispose();
	}



	private void initLayout()
	{
		int pref = GroupLayout.PREFERRED_SIZE;
		int max = Short.MAX_VALUE;
		GroupLayout.Alignment leading = GroupLayout.Alignment.LEADING;
		JLabel empty_lbl = new JLabel();
		GroupLayout uninstallPanelLayout = new GroupLayout((JComponent)uninstallPanel);
		uninstallPanel.setLayout(uninstallPanelLayout);
		uninstallPanelLayout.setHorizontalGroup(uninstallPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addComponent(lbl_uninstall, pref, pref, max)
			.addGap(Globals.gapSize *2)
			.addGroup(uninstallPanelLayout.createSequentialGroup()
				.addGroup(uninstallPanelLayout.createParallelGroup()
					.addComponent(lbl_product,250, 250, 250)
					.addComponent(lbl_on,250, 250, 250)
					.addComponent(lbl_verbosity,pref, pref, pref)
					.addComponent(lbl_keepFiles,pref, pref, pref)
				)
				.addGap(Globals.gapSize)
				.addGroup(uninstallPanelLayout.createParallelGroup()
					.addComponent(cb_opsiproducts,pref, pref, max)
					.addComponent(cb_depots,pref, pref, max)
					.addComponent(cb_verbosity,pref, pref, max)
					// .addGroup(uninstallPanelLayout.createParallelGroup()				
					.addGroup(uninstallPanelLayout.createSequentialGroup()
						// .addGap(Globals.gapSize*2 + Globals.minGapSize-2)
						.addComponent(empty_lbl,pref, pref, max)
						.addComponent(checkb_keepFiles,pref, pref, pref)
					)
					// )
				)
			// )
			// .addGroup(uninstallPanelLayout.createSequentialGroup()
				// .addGroup(uninstallPanelLayout.createParallelGroup()
				// )
				// .addGap(Globals.gapSize)
			)
			// .addComponent(lbl_fullCommand, pref, pref,max)
		);

		uninstallPanelLayout.setVerticalGroup(uninstallPanelLayout.createSequentialGroup()
			.addComponent(lbl_uninstall)
			.addGap(Globals.gapSize*2)
			.addGroup(uninstallPanelLayout.createParallelGroup(leading)
				.addComponent(lbl_product,pref,pref,pref)
				.addComponent(cb_opsiproducts, pref,pref, pref)
			)
			.addGap(Globals.minGapSize)
			.addGroup(uninstallPanelLayout.createParallelGroup(leading)
				.addComponent(lbl_on,pref,pref,pref)
				.addComponent(cb_depots,pref,pref,pref)
			)
			.addGap(Globals.minGapSize)
			.addGroup(uninstallPanelLayout.createParallelGroup(leading)
				.addComponent(lbl_verbosity,pref,pref,pref)
				.addComponent(cb_verbosity,pref,pref,pref)
			)
			.addGap(Globals.minGapSize)
			.addGroup(uninstallPanelLayout.createParallelGroup(leading)
				.addComponent(lbl_keepFiles,pref,pref,pref)
				.addGroup(uninstallPanelLayout.createParallelGroup(leading)
					.addComponent(empty_lbl,pref,pref,pref)
					.addComponent(checkb_keepFiles,pref,pref,pref)
				)
			)
			.addGap(Globals.minGapSize)
			// .addComponent(lbl_fullCommand)
		);
	}
}



