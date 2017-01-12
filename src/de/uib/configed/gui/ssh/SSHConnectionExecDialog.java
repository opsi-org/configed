package de.uib.configed.gui.ssh;

import de.uib.utilities.*;
import de.uib.utilities.logging.*;
import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.opsicommand.*;
import de.uib.opsicommand.sshcommand.*;
import java.awt.*;
import java.awt.event.*;

import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.GroupLayout.*;

import javax.swing.border.TitledBorder;

public class SSHConnectionExecDialog extends SSHConnectionOutputDialog
{
	public JButton btn_killProcess;
	
	public SSHConnectionExecDialog(String title , SSHCommand command)
	{
		super(title);
		buildFrame = false;
		initGUI();
		
		if ((command != null) && (command.getDialog() != null)) this.centerOn(command.getDialog());
		else this.centerOn(de.uib.configed.Globals.mainFrame);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		this.setSize(700, 400);
		logging.info(this, "SSHConnectionExecDialog build" );
		if (SSHCommandFactory.getInstance().ssh_always_exec_in_background==false)
			this.setVisible (true);
		buildFrame = true;
	}
	public SSHConnectionExecDialog(SSHCommand command, String title )
	{ this(title, command);}
	public SSHConnectionExecDialog(String title )
	{ this(title, null); }
	public SSHConnectionExecDialog(SSHCommand c )
	{ this("", c); }
	public SSHConnectionExecDialog( )
	{ this("", null); }


	private void initGUI() 
	{
		try 
		{
			btn_killProcess= new de.uib.configed.gui.IconButton(
				de.uib.configed.configed.getResourceValue("SSHConnection.buttonKillProcess") ,
				"images/edit-delete.png", "images/edit-delete.png", "images/edit-delete.png",true
			);
			btn_killProcess.setPreferredSize(new Dimension(de.uib.configed.Globals.graphicButtonWidth + 15 ,de.uib.configed.Globals.buttonHeight + 3));
			btn_killProcess.setToolTipText(configed.getResourceValue("SSHConnection.buttonKillProcess"));
			createLayout(konsolePanelLayout, jScrollPane,de.uib.configed.Globals.gapSize,de.uib.configed.Globals.gapSize, true);
			createLayout(mainPanelLayout, inputPanel,0,0, false);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	public boolean showResult = true;
	public void setVisibility(boolean v)
	{
		this.setVisible(v);
	}
	public void setStatusFinish()
	{
		if (showResult) setVisibility(true);
		else cancel();
	}

	private void createLayout(GroupLayout layout, Component comp, int vgap, int hgap, boolean addInputField)
	{
		int pref = GroupLayout.PREFERRED_SIZE;
		int max = Short.MAX_VALUE;
		GroupLayout.Alignment leading = GroupLayout.Alignment.LEADING;
		layout.setAutoCreateGaps(true);
		SequentialGroup verticalGroup=layout.createSequentialGroup();
		
		verticalGroup.addGap(vgap) .addComponent(comp);
		if (addInputField)
			verticalGroup.addGroup(layout.createParallelGroup()
				.addGap(vgap, vgap, vgap)
				.addComponent(btn_killProcess,pref, pref,pref )
				.addComponent(btn_close,pref, pref,pref )
				.addGap(vgap)
			);
		verticalGroup.addGap(vgap);

		ParallelGroup horizontalGroup=layout.createParallelGroup();
		horizontalGroup.addGroup(layout.createSequentialGroup()
				.addGap(hgap)
				.addComponent(comp)
				.addGap(hgap)
		);
		if (addInputField)
			 horizontalGroup.addGroup(layout.createSequentialGroup()
				.addGap(hgap, hgap, max)
				.addComponent(btn_killProcess,pref, pref,pref )
				.addComponent(btn_close,pref, pref,pref )
				.addGap(hgap)
			);
	
		layout.setVerticalGroup(verticalGroup);
		layout.setHorizontalGroup(horizontalGroup);	
	}
}