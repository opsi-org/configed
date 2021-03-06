package de.uib.configed.gui.ssh;

import de.uib.opsicommand.*;
import de.uib.opsicommand.sshcommand.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
// import javax.swing.border.*;
// import javax.swing.event.*;
// import java.io.*;
import java.util.*;
// import java.nio.charset.Charset;
// import java.util.regex.*;
import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.opsidatamodel.*;
import de.uib.utilities.logging.*;

public class SSHOpsiSetRightsParameterDialog extends FGeneralDialog
{
	private GroupLayout layout;
	private JPanel inputPanel = new JPanel();
	private JPanel buttonPanel = new JPanel();

	private JLabel lbl_info;
	private JComboBox cb_autocompletion;
	private JButton btn_searchDir;

	private JButton btn_doAction;
	private JButton btn_close;
	private CommandOpsiSetRights commandopsisetrights;
	private Vector<String> additional_default_paths= new Vector();
	private SSHCompletionComboButton completion;
	public SSHOpsiSetRightsParameterDialog()
	{
		super(null,configed.getResourceValue("SSHConnection.command.opsisetrights"), false);
		commandopsisetrights = new CommandOpsiSetRights();
		init();
		initLayout();
	}
	public SSHOpsiSetRightsParameterDialog(CommandOpsiSetRights command)
	{
		super(null,configed.getResourceValue("SSHConnection.command.opsisetrights"), false);
		commandopsisetrights = command;
		init();
		initLayout();
	}

	private void init()
	{
		additional_default_paths.addElement(SSHCommandFactory.getInstance().opsipathVarDepot);
	 	completion = new SSHCompletionComboButton(additional_default_paths);

		inputPanel.setBackground(Globals.backLightBlue);
		buttonPanel.setBackground(Globals.backLightBlue);
		getContentPane().add(inputPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		inputPanel.setBorder(BorderFactory.createTitledBorder(""));
		lbl_info = new JLabel(configed.getResourceValue("SSHConnection.command.opsisetrights.additionalPath"));
		inputPanel.add(lbl_info);
		btn_doAction = new JButton();
			buttonPanel.add(btn_doAction);
			btn_doAction.setText(configed.getResourceValue("SSHConnection.buttonExec"));
			btn_doAction.setIcon(Globals.createImageIcon("images/execute16_blue.png", ""));
			if (!(Globals.isGlobalReadOnly()))
				btn_doAction.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						logging.info(this, "btn_doAction pressed");
						doAction1();
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
					cancel();
				}
			});
		setComponentsEnabled(! Globals.isGlobalReadOnly());

		btn_searchDir = completion.getButton();
		cb_autocompletion = completion.getCombobox();
		// completion.doButtonAction();
		cb_autocompletion.setEnabled(true);
		inputPanel.add(cb_autocompletion);
		inputPanel.add(btn_searchDir);
	}

	private void setComponentsEnabled(boolean value)
	{
		btn_doAction.setEnabled(value);
	}

	/* This method is called when button 1 is pressed */
	public void doAction1()
	{
		try
		{
			commandopsisetrights.setDir(completion.combobox_getStringItem());;
			logging.info(this, "doAction1 opsi-set-rights with path: " + commandopsisetrights.getDir());
			SSHConnectExec ssh = new SSHConnectExec((SSHCommand) commandopsisetrights );
			cancel();
		}
		catch (Exception e)
		{
			logging.warning(this, "doAction1, exception occurred " + e);
			logging.logTrace(e);
		}

	}

	// /* This method gets called when button 2 is pressed */
	public void cancel()
	{
		super.doAction2();
	}

	private void initLayout()
	{
		GroupLayout inputPanelLayout = new GroupLayout(inputPanel);
		inputPanel.setLayout(inputPanelLayout);
		inputPanelLayout.setHorizontalGroup(inputPanelLayout.createSequentialGroup()
			.addGap(Globals.gapSize)
			.addGroup(inputPanelLayout.createParallelGroup()
				.addGroup(inputPanelLayout.createSequentialGroup()
					.addComponent(lbl_info, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
				.addGap(Globals.gapSize)
				.addGroup(inputPanelLayout.createSequentialGroup()
					.addComponent(cb_autocompletion, Globals.buttonWidth, Globals.buttonWidth, Short.MAX_VALUE)
					.addComponent(btn_searchDir,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE)
				)
				.addGap(Globals.gapSize)
			)
			.addGap(Globals.gapSize)
		);
	
		inputPanelLayout.setVerticalGroup(inputPanelLayout.createSequentialGroup()
			.addGap(Globals.gapSize)
			.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(lbl_info, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
			)
			.addGap(Globals.gapSize)
			.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(cb_autocompletion, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				.addComponent(btn_searchDir, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
			)
			.addGap(Globals.gapSize)
		);

		this.setSize(600, 200);
		this.centerOn(de.uib.configed.Globals.mainFrame);
		this.setBackground(Globals.backLightBlue);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setVisible (true);
	}
}