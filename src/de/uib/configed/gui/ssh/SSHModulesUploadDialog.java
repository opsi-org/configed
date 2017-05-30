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
import java.nio.charset.Charset;
import java.util.regex.*;
import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.opsidatamodel.*;
import de.uib.utilities.logging.*;

public class SSHModulesUploadDialog extends FGeneralDialog
{
	private GroupLayout layout;
	private JPanel inputPanel = new JPanel();
	private JPanel buttonPanel = new JPanel();
	private JPanel winAuthPanel = new JPanel();

	private JRadioButton rb_from_server;
	private JRadioButton rb_local;

	private JFileChooser filechooser_local;
	private JTextField tf_local_path;

	private JButton btn_filechooser;
	private JButton btn_execute;
	private JButton btn_close;
	
	private JLabel lbl_set_rights;
	private JLabel lbl_modules_from;
	private JLabel lbl_url;
	private JLabel lbl_host;
	private JLabel lbl_passw;
	private JLabel lbl_user;
	private JLabel lbl_overwriteExisting;
	private JLabel lbl_copy_to_modules_d;

	private JButton btn_search;
	private JCheckBox cb_copy_to_modules_d;
	private JCheckBox cb_setRights;
	private JCheckBox cb_overwriteExisting;
	private JTextField tf_url;
	private JTextField tf_host;
	private JTextField tf_passw;
	private JTextField tf_user;

	private CommandModulesUpload command;
	SSHCompletionComboButton autocompletion = new SSHCompletionComboButton();
	SSHCommandFactory factory = SSHCommandFactory.getInstance();
	SSHConnectionExecDialog dia;
	private ConfigedMain main;

	public SSHModulesUploadDialog()
	{
		this(null);
	}
	public SSHModulesUploadDialog(CommandModulesUpload com)
	{
		super(null,configed.getResourceValue("SSHConnection.ParameterDialog.modulesupload.title"), false);
		this.command = com;
		if (this.command == null)
			command = new CommandModulesUpload();

		init();
		initGUI();
		this.setSize(de.uib.configed.Globals.dialogFrameDefaultWidth,de.uib.configed.Globals.dialogFrameDefaultHeight + 100 );
		this.centerOn(de.uib.configed.Globals.mainFrame);
		this.setBackground(Globals.backLightBlue);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setVisible (true);
		logging.info(this, "SSHModulesUploadDialog build");
	}

	protected void init() 
	{
		inputPanel.setBackground(Globals.backLightBlue);
		buttonPanel.setBackground(Globals.backLightBlue);
		
		getContentPane().add(inputPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		inputPanel.setBorder(BorderFactory.createTitledBorder(""));

		ButtonGroup group = new ButtonGroup();
		rb_from_server = new JRadioButton(configed.getResourceValue("SSHConnection.ParameterDialog.modulesupload.rb_from_server"));
		group.add(rb_from_server);
		addListener(rb_from_server);
		rb_local = new JRadioButton(configed.getResourceValue("SSHConnection.ParameterDialog.modulesupload.rb_local"), true);
		group.add(rb_local);
		addListener(rb_local);


		lbl_host = new JLabel();
		lbl_host.setText(configed.getResourceValue("SSHConnection.ParameterDialog.modulesupload.lbl_host"));
		lbl_user = new JLabel();
		lbl_user.setText(configed.getResourceValue("SSHConnection.ParameterDialog.modulesupload.lbl_user"));
		lbl_passw = new JLabel();
		lbl_passw.setText(configed.getResourceValue("SSHConnection.ParameterDialog.modulesupload.lbl_passw"));
		lbl_url = new JLabel();
		lbl_url.setText(configed.getResourceValue("SSHConnection.ParameterDialog.modulesupload.lbl_url"));
		lbl_overwriteExisting = new JLabel();
		lbl_overwriteExisting.setText(configed.getResourceValue("SSHConnection.ParameterDialog.modulesupload.lbl_overwriteExisting"));

		lbl_set_rights = new JLabel();
		lbl_set_rights.setText(configed.getResourceValue("SSHConnection.ParameterDialog.modulesupload.lbl_setRights"));
		lbl_copy_to_modules_d = new JLabel();
		lbl_copy_to_modules_d.setText(configed.getResourceValue("SSHConnection.ParameterDialog.modulesupload.lbl_copy_to_modules_d"));
		lbl_modules_from = new JLabel();
		lbl_modules_from.setText(configed.getResourceValue("SSHConnection.ParameterDialog.modulesupload.lbl_modules_from"));
		
			// tf_host = new JTextField();
			// tf_host.setText("bonifax");
			tf_url = new JTextField();
			tf_url.setText("<URL>");
			tf_url.addFocusListener(new FocusAdapter() {
				public void focusGained(FocusEvent e) {
					if ( tf_url.getText().equals("<URL>"))
					{
						tf_url.setSelectionStart(0);
						tf_url.setSelectionEnd(tf_url.getText().length());
					}
				}
			});
			tf_user = new JTextField();
			tf_user.setText("");
			tf_passw = new JPasswordField();
			tf_passw.setText("");
			((JPasswordField)tf_passw).setEchoChar('*');
			tf_local_path = new JTextField();
			tf_local_path.setEditable(false);
			tf_local_path.setBackground(Globals.backLightYellow);

		cb_copy_to_modules_d = new JCheckBox();
		cb_copy_to_modules_d.setSelected(true);
		cb_setRights = new JCheckBox();
		cb_setRights.setSelected(true);
		cb_overwriteExisting = new JCheckBox();
		cb_overwriteExisting.setSelected(true);

		filechooser_local = new JFileChooser();
		filechooser_local.setFileSelectionMode(JFileChooser.FILES_ONLY);
		filechooser_local.setApproveButtonText( configed.getResourceValue("FileChooser.approve") );
		
		filechooser_local.setDialogType(JFileChooser.OPEN_DIALOG);
		filechooser_local.setDialogTitle(de.uib.configed.Globals.APPNAME + " " +configed.getResourceValue("SSHConnection.ParameterDialog.modulesupload.filechooser.title"));
		
		btn_filechooser = new JButton( "", de.uib.configed.Globals.createImageIcon("images/folder_16.png", "" ));
		btn_filechooser.setSelectedIcon( de.uib.configed.Globals.createImageIcon("images/folder_16.png", "" ) );
		btn_filechooser.setPreferredSize(de.uib.configed.Globals.smallButtonDimension);
		btn_filechooser.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.modulesupload.filechooser.tooltip"));
		btn_filechooser.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					int returnVal = filechooser_local.showOpenDialog( inputPanel );
					
					if (returnVal == JFileChooser.APPROVE_OPTION)
					{
						String path_modules = filechooser_local.getSelectedFile().getPath();
						tf_local_path.setText(path_modules);
						command.setFullSourcePath(path_modules);
						tf_local_path.setCaretPosition(path_modules.length());
					}
					else
					{
						tf_local_path.setText("");
					}
				}
			}
		);
		btn_execute = new JButton();
		buttonPanel.add(btn_execute);
		btn_execute.setText(configed.getResourceValue("SSHConnection.buttonExec"));
		btn_execute.setIcon(Globals.createImageIcon("images/execute16_blue.png", ""));
		if (!(Globals.isGlobalReadOnly()))
			btn_execute.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{doAction1();}
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
		enableComponents(rb_from_server.isSelected());
		SSHConnectExec testFile = new SSHConnectExec();
		String result = testFile.exec(new Empty_Command(
			factory.str_command_fileexists_notremove
				.replaceAll(
					factory.str_replacement_filename, 
					command.unofficial_modules_directory) // 	/etc/opsi/modules.d
				), 
			false);
		if (result.trim().equals(factory.str_file_exists))
		{
			lbl_copy_to_modules_d.setVisible(true);
			cb_copy_to_modules_d.setVisible(true);
			cb_copy_to_modules_d.setSelected(true);
		}
		else 
		{
			lbl_copy_to_modules_d.setVisible(false);
			cb_copy_to_modules_d.setVisible(false);
			cb_copy_to_modules_d.setSelected(false);
		}
		
	}

	private void enableComponents(boolean rb_local_isSelected)
	{
		// tf_host.setEnabled(rb_local_isSelected);
		tf_user.setEnabled(rb_local_isSelected);
		tf_passw.setEnabled(rb_local_isSelected);
		tf_url.setEnabled(rb_local_isSelected);
		tf_local_path.setEnabled(!rb_local_isSelected);
		btn_filechooser.setEnabled(!rb_local_isSelected);
		
	}
	
	private void initGUI() {
		try {
			GroupLayout inputPanelLayout = new GroupLayout((JComponent)inputPanel);

			getContentPane().add(inputPanel, BorderLayout.CENTER);
			getContentPane().add(buttonPanel, BorderLayout.SOUTH);
			inputPanel.setLayout(inputPanelLayout);
			
			inputPanelLayout.setHorizontalGroup(inputPanelLayout.createParallelGroup()
				.addGap(Globals.gapSize)
				.addGroup(inputPanelLayout.createSequentialGroup()
					.addGap(Globals.gapSize*2)
					.addGroup(inputPanelLayout.createParallelGroup()
						.addGap(Globals.gapSize*2)
						.addGroup(inputPanelLayout.createSequentialGroup()
							.addComponent(rb_local, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						)
						.addGap(Globals.gapSize*2)
						.addGroup(inputPanelLayout.createSequentialGroup()
							.addComponent(rb_from_server, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						)
						.addGroup(inputPanelLayout.createSequentialGroup()
							.addGroup(inputPanelLayout.createParallelGroup()
								.addGroup(inputPanelLayout.createSequentialGroup()
									.addComponent(lbl_modules_from, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								)
								.addComponent(lbl_set_rights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lbl_overwriteExisting, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lbl_copy_to_modules_d, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
							)
							.addGap(Globals.gapSize)
							.addGroup(inputPanelLayout.createParallelGroup()
								.addComponent(cb_setRights, Globals.iconWidth, Globals.iconWidth, Globals.iconWidth)
								.addComponent(cb_overwriteExisting, Globals.iconWidth, Globals.iconWidth, Globals.iconWidth)
								.addComponent(cb_copy_to_modules_d, Globals.iconWidth, Globals.iconWidth, Globals.iconWidth)
							)
						)
					)
					.addGap(Globals.gapSize)
				)
				.addGroup(inputPanelLayout.createSequentialGroup()
					.addGap(Globals.gapSize)
					.addGroup(inputPanelLayout.createParallelGroup()
						.addGap(Globals.gapSize*2)
						.addGroup(GroupLayout.Alignment.LEADING, inputPanelLayout.createSequentialGroup()
							.addGap(Globals.gapSize*3)
							.addComponent(tf_local_path, Globals.buttonWidth,  Globals.buttonWidth, Short.MAX_VALUE)
							.addGap(Globals.gapSize)
							.addComponent(btn_filechooser,  GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
							.addGap(Globals.gapSize)
						)
						.addGroup(inputPanelLayout.createSequentialGroup()
							.addGap(Globals.gapSize*3)
							.addGroup(inputPanelLayout.createParallelGroup()
								.addComponent(lbl_url, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lbl_user, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lbl_passw, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
							)
							.addGap(Globals.gapSize)
							.addGroup(inputPanelLayout.createParallelGroup()
								.addComponent(tf_url, Globals.buttonWidth, Globals.buttonWidth, Short.MAX_VALUE)
								.addComponent(tf_passw, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
								.addComponent(tf_user, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
							)
							.addGap(Globals.gapSize)
						)
						.addGap(Globals.gapSize*3)
					)
					.addGap(Globals.gapSize)
				)
				.addGap(Globals.gapSize)
			);
			inputPanelLayout.setVerticalGroup(inputPanelLayout.createSequentialGroup()
				.addGap(Globals.gapSize)
				.addComponent(lbl_modules_from, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.gapSize)
				.addGap(Globals.gapSize)
				.addComponent(rb_local, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.gapSize)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addGap(Globals.gapSize*3)
					.addComponent(tf_local_path, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(btn_filechooser, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(Globals.gapSize*3)
				)
				.addGap(Globals.gapSize)
				.addComponent(rb_from_server, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.gapSize)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addGap(Globals.gapSize*3)
					.addComponent(tf_url, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(lbl_url, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(Globals.gapSize*3)
				)
				.addGap(Globals.gapSize)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addGap(Globals.gapSize*3)
					.addComponent(tf_user, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(lbl_user, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(Globals.gapSize*3)
				)
				.addGap(Globals.gapSize)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addGap(Globals.gapSize*3)
					.addComponent(tf_passw, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(lbl_passw, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(Globals.gapSize*3)
				)
				.addGap(Globals.gapSize*3)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addComponent(lbl_set_rights, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(cb_setRights, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
				.addGap(Globals.gapSize)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addComponent(lbl_overwriteExisting, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(cb_overwriteExisting, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
				.addGap(Globals.gapSize)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addComponent(lbl_copy_to_modules_d, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(cb_copy_to_modules_d, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
				.addGap(Globals.gapSize)
			);
			this.setSize(709, 325);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// // /* This method gets called when button 2 is pressed */
	public void cancel()
	{
		super.doAction2();
	}

	/* This method is called when button 1 is pressed */
	public void doAction1()
	{
		logging.info(this, "doAction1 modules upload ");
		String modules_server_path = command.actually_modules_directory + command.getTargetFilename();
		if (cb_copy_to_modules_d.isVisible() && cb_copy_to_modules_d.isSelected())
		{
			modules_server_path = command.unofficial_modules_directory;
			command.setTargetPath(modules_server_path);
			command.setTargetFilename(filechooser_local.getSelectedFile().getName());
		}

		try
		{
			SSHCommand_Template fullcommand = new SSHCommand_Template();
			fullcommand.setMainName("ModulesUpload");
			if (rb_from_server.isSelected())
			{
				CommandWget comWget = new CommandWget();
				if (cb_copy_to_modules_d.isVisible() && cb_copy_to_modules_d.isSelected())
					comWget.setDir(modules_server_path);
				else comWget.setFilename(modules_server_path);
				comWget.setUrl(tf_url.getText());
				if  ((tf_user.getText() != null) && (!tf_user.getText().equals("")))
					comWget.setAuthentication(" --no-check-certificate --user=" + tf_user.getText() 
					+ " --password=" + new String(((JPasswordField)tf_passw).getPassword()) + " "
						);
				fullcommand.addCommand((SSHCommand) comWget);
			}
			else
			{
				command.setOverwriteMode(cb_overwriteExisting.isSelected());
				fullcommand.addCommand((SSHCommand) command);
			}

			if (cb_setRights.isSelected())
				fullcommand.addCommand((SSHCommand) new CommandOpsiSetRights());
			new SSHConnectExec(fullcommand);
		}
		catch (Exception e)
		{
			logging.warning(this, "doAction1, exception occurred " + e);
			logging.logTrace(e);
		}
	}

	private void addListener(Component comp)
	{
		if (comp instanceof JRadioButton)
			((JRadioButton)comp).addActionListener(new ActionListener() 
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{enableComponents(rb_from_server.isSelected());}
			});
	}
}