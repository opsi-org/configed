package de.uib.configed.gui.ssh;

import de.uib.opsicommand.*;
import de.uib.opsicommand.sshcommand.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;
import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.opsidatamodel.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.swing.*;

public class SSHWgetParameterDialog extends FGeneralDialog
{
	private GroupLayout layout;
	private JPanel inputPanel = new JPanel();
	private JPanel buttonPanel = new JPanel();

	private JLabel lbl_url = new JLabel();
	private JLabel lbl_dir = new JLabel();
	private JLabel lbl_verbosity = new JLabel();
	private JLabel lbl_needAuthentication = new JLabel();
	private JCheckBox cb_needAuthentication;
	private JLabel lbl_user = new JLabel();
	private JTextField tf_user = new JTextField();
	private JTextField tf_pswd = new JPasswordField();
	private JLabel lbl_pswd = new JLabel();

	private JLabel lbl_freeInput = new JLabel();
	private JLabel lbl_fullCommand = new JLabel();

	private JButton btn_help;
	private JButton btn_execute;
	private JButton btn_close;
	private JButton btn_searchDir;

	private JTextField tf_url;
	private JTextField tf_dir;
	private JComboBox cb_dir;
	private JComboBox cb_verbosity;
	private JTextField tf_freeInput;

	final private int frameWidth = 800;
	final private int frameHeight = 400;

	private ConfigedMain main;
	CommandWget commandWget = new CommandWget();
	private SSHCommandFactory factory = SSHCommandFactory.getInstance();
	SSHCompletionComboButton completion = new SSHCompletionComboButton();

	public SSHWgetParameterDialog()
	{
		this(null);
	}
	public SSHWgetParameterDialog(ConfigedMain m)
	{
		super(null,configed.getResourceValue("SSHConnection.ParameterDialog.wget.title"), false);
		main = m;
		init();
		initLayout();
		pack();
		setSize(de.uib.configed.Globals.dialogFrameDefaultSize);
		this.centerOn(de.uib.configed.Globals.mainFrame);
		this.setBackground(Globals.backLightBlue);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setVisible (true);
		if (Globals.isGlobalReadOnly())
			setComponentsEnabled_RO(false);
		cb_dir.setEnabled(true);
	}

	private void setComponentsEnabled_RO(boolean value)
	{
		tf_url.setEnabled(value);
		tf_url.setEditable(value);
		tf_dir.setEnabled(value);
		tf_dir.setEditable(value);
		cb_dir.setEnabled(value);
		cb_dir.setEditable(value);
		cb_verbosity.setEnabled(value);
		// cb_verbosity.setEditable(value);
		tf_freeInput.setEnabled(value);
		tf_freeInput.setEditable(value);
		setComponentsEditable(value);
		
		btn_execute.setEnabled(value);
		btn_help.setEnabled(value);
		cb_needAuthentication.setEnabled(value);
	}

	private void init() 
	{
		inputPanel.setBackground(Globals.backLightBlue);
		buttonPanel.setBackground(Globals.backLightBlue);
		getContentPane().add(inputPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		inputPanel.setBorder(BorderFactory.createTitledBorder(""));
		inputPanel.setPreferredSize(new java.awt.Dimension(376, 220));
		{
			lbl_url.setText(configed.getResourceValue("SSHConnection.ParameterDialog.wget.jLabelUrl"));
			tf_url = new JTextField();
			tf_url.setText(configed.getResourceValue("SSHConnection.ParameterDialog.wget.tooltip.tf_wget_url"));
			tf_url.getDocument().addDocumentListener(new DocumentListener() 
			{
				public void changedUpdate(DocumentEvent documentEvent) { changeUrl();}
				public void insertUpdate(DocumentEvent documentEvent) { changeUrl();}
				public void removeUpdate(DocumentEvent documentEvent) { changeUrl();}
			});
			tf_url.addFocusListener(new FocusAdapter() {
				public void focusGained(FocusEvent e) {
					if ( tf_url.getText().equals(configed.getResourceValue("SSHConnection.ParameterDialog.wget.tooltip.tf_wget_url")))
					{
						tf_url.setSelectionStart(0);
						tf_url.setSelectionEnd(tf_url.getText().length());
					}
				}
			});

			lbl_dir.setText(configed.getResourceValue("SSHConnection.ParameterDialog.wget.jLabelDirectory"));
			tf_dir = new JTextField();

			cb_dir = completion.getCombobox();
			btn_searchDir = completion.getButton();
		}
		{
			lbl_verbosity.setText(configed.getResourceValue("SSHConnection.ParameterDialog.jLabelVerbosity"));
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
					commandWget.setVerbosity(((int)cb_verbosity.getSelectedItem()));
					updateCommand();
				}
			});
		}
		{
			lbl_freeInput.setText(configed.getResourceValue("SSHConnection.ParameterDialog.jLabelFreeInput"));
			tf_freeInput = new JTextField();
			tf_freeInput.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.tooltip.freeInput"));
			tf_freeInput.getDocument().addDocumentListener(new DocumentListener() 
			{
				public void changedUpdate(DocumentEvent documentEvent) { changeFreeInput();}
				public void insertUpdate(DocumentEvent documentEvent) { changeFreeInput();}
				public void removeUpdate(DocumentEvent documentEvent) { changeFreeInput();}
			});
		}
		{

			lbl_needAuthentication.setText(configed.getResourceValue("SSHConnection.ParameterDialog.wget.needAuthentication"));
			lbl_needAuthentication.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.wget.needAuthentication.tooltip"));
			cb_needAuthentication = new JCheckBox();
			cb_needAuthentication.addItemListener(new ItemListener() 
			{
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					if(e.getStateChange() == ItemEvent.SELECTED) 
						setComponentsEditable(true);
					else setComponentsEditable(false);
				}
			});
			lbl_user.setText(configed.getResourceValue("SSHConnection.ParameterDialog.wget.username"));
			lbl_pswd.setText(configed.getResourceValue("SSHConnection.ParameterDialog.wget.password"));
			((JPasswordField)tf_pswd).setEchoChar('*');
			tf_user.setText(""); //main.USER);
			tf_pswd.setText(""); //main.PASSWORD);
			setComponentsEditable(false);
		}
		{
			// btn_help = new JButton();
			btn_help = new JButton("", Globals.createImageIcon("images/help-about.png", ""));
			btn_help.setText(configed.getResourceValue("SSHConnection.buttonParameterInfo"));
			btn_help.setToolTipText(configed.getResourceValue("SSHConnection.buttonParameterInfo.tooltip"));
			buttonPanel.add(btn_help);
			btn_help.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					doActionHelp();
				}
			});

			btn_execute = new JButton();
			buttonPanel.add(btn_execute);
			btn_execute.setText(configed.getResourceValue("SSHConnection.buttonExec"));
			btn_execute.setIcon(Globals.createImageIcon("images/execute16_blue.png", ""));
			btn_execute.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if (!(Globals.isGlobalReadOnly()))
					{
						doAction1();
					}
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
		}
		{
			lbl_fullCommand.setText("wget ");
			// changeDir();
			changeUrl();
			changeFreeInput();
		}
	}

	private void setComponentsEditable(boolean value)
	{
		// tf_host.setEnabled(value);
		// cb_host.setEnabled(value);
		tf_user.setEnabled(value);
		tf_pswd.setEnabled(value);
	}

	private void updateCommand()
	{
		lbl_fullCommand.setText(commandWget.getCommand());
	}
	private void changeFreeInput()
	{
		if (tf_freeInput.getText().trim() != "") commandWget.setFreeInput(tf_freeInput.getText().trim());
		else commandWget.setFreeInput("");
		updateCommand();
	}

	// private void changeDir()
	// {
	// 	if (!(tf_dir.getText().equals(""))) commandWget.setDir(tf_dir.getText().trim());
	// 	else commandWget.setDir("");
	// 	updateCommand();
	// }

	private void changeUrl()
	{
		if (!(tf_url.getText().equals(""))) commandWget.setUrl(tf_url.getText().trim());
		else commandWget.setUrl("");
		updateCommand();
	}

	/* This method is called when button 1 is pressed */
	public void doAction1()
	{
		commandWget.setDir((String)cb_dir.getSelectedItem());
		if (cb_needAuthentication.isSelected())
		{
			commandWget.setAuthentication(" --no-check-certificate --user=" + tf_user.getText() 
				+ " --password=" + new String(((JPasswordField)tf_pswd).getPassword()) + " ");
		}else commandWget.setAuthentication(" ");
		updateCommand();

		if (commandWget.checkCommand()) 
		{
			new Thread()
			{
				public void run()
				{
					try
					{
						logging.info(this, "doAction1 wget ");
						SSHConnectExec ssh = new SSHConnectExec(((SSHCommand)commandWget));
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}.start();
		}
	}

	public void doActionHelp()
	{
		SSHConnectionExecDialog dia = commandWget.startHelpDialog();
		dia.setVisible(true);
		/*
			JOptionPane.showMessageDialog(	Globals.mainFrame, 
				  "first call", 
				  "debug",
				  JOptionPane.OK_OPTION);
		*/
	}
	/* This method gets called when button 2 is pressed */
	// public void doAction2()
	// {
	// 	this.setVisible (false);
	// 	this.dispose ();
	// }
	public void cancel()
	{
		super.doAction2();
	}

	private void initLayout()
	{
		GroupLayout inputPanelLayout = new GroupLayout((JComponent)inputPanel);
		inputPanel.setLayout(inputPanelLayout);
		inputPanelLayout.setHorizontalGroup(inputPanelLayout.createSequentialGroup()
			
			.addGap(de.uib.configed.Globals.gapSize)
			.addGroup(inputPanelLayout.createParallelGroup()
				.addComponent(lbl_url,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(lbl_dir,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(lbl_verbosity,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(lbl_needAuthentication,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGroup(inputPanelLayout.createSequentialGroup()
					.addGap(de.uib.configed.Globals.gapSize*2)
					.addComponent(lbl_user,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
				.addGroup(inputPanelLayout.createSequentialGroup()
					.addGap(de.uib.configed.Globals.gapSize*2)
					.addComponent(lbl_pswd,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
				.addComponent(lbl_freeInput,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
			// .addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
			.addGroup(inputPanelLayout.createParallelGroup()
				.addGroup(inputPanelLayout.createSequentialGroup()
					.addComponent(tf_url,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					//.addGap( de.uib.configed.Globals.gapSize )
				)
				.addGroup(inputPanelLayout.createSequentialGroup()
					.addComponent(cb_dir, de.uib.configed.Globals.buttonWidth, de.uib.configed.Globals.buttonWidth, Short.MAX_VALUE)
					.addComponent(btn_searchDir,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					//.addGap( de.uib.configed.Globals.gapSize )
				)
				.addComponent(cb_verbosity,GroupLayout.Alignment.LEADING, Globals.iconWidth, Globals.iconWidth, Globals.iconWidth)
				.addComponent(cb_needAuthentication, GroupLayout.Alignment.LEADING,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(tf_user, de.uib.configed.Globals.buttonWidth, de.uib.configed.Globals.buttonWidth, de.uib.configed.Globals.buttonWidth*2) 
				.addComponent(tf_pswd,de.uib.configed.Globals.buttonWidth, de.uib.configed.Globals.buttonWidth, de.uib.configed.Globals.buttonWidth*2)
				.addComponent(tf_freeInput,de.uib.configed.Globals.buttonWidth, de.uib.configed.Globals.buttonWidth, Short.MAX_VALUE)
			)
			.addGap(de.uib.configed.Globals.gapSize)
		);
	
		inputPanelLayout.setVerticalGroup(inputPanelLayout.createSequentialGroup()
			.addGap(de.uib.configed.Globals.gapSize)
			.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(tf_url, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				.addComponent(lbl_url, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
			)
			.addGap(de.uib.configed.Globals.gapSize)
			.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(btn_searchDir, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				.addComponent(cb_dir, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				.addComponent(lbl_dir, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
			)
			.addGap(de.uib.configed.Globals.gapSize)
			// .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
			.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(cb_verbosity,GroupLayout.Alignment.LEADING, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				.addComponent(lbl_verbosity, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
			)
			.addGap(de.uib.configed.Globals.gapSize)
			// .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
			.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(lbl_needAuthentication, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				.addComponent(cb_needAuthentication, GroupLayout.Alignment.LEADING, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
			)
			.addGap(de.uib.configed.Globals.gapSize)
			// .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
			.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				// .addGap(de.uib.configed.Globals.gapSize)
				.addComponent(lbl_user, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				.addComponent(tf_user, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
			)
			.addGap(de.uib.configed.Globals.gapSize)
			// .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
			.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				// .addGap(de.uib.configed.Globals.gapSize*3)
				.addComponent(lbl_pswd, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				.addComponent(tf_pswd, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
			)
			.addGap(de.uib.configed.Globals.gapSize)
			// .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
			.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(tf_freeInput, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				.addComponent(lbl_freeInput, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
			)
			.addGap(de.uib.configed.Globals.gapSize)
			// .addComponent(lbl_fullCommand, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
			// .addGap(10)
			.addContainerGap(70, 70)
		);
	}
}
