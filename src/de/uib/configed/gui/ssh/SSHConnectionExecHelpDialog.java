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

public class SSHConnectionExecHelpDialog extends SSHConnectionOutputDialog
{
	// private JCheckBox cb_showResult;
	// private JButton btn_inBackground;
	private boolean buildFrame = false;
	private JLabel lbl = new JLabel(" ");

	public SSHConnectionExecHelpDialog(String title , SSHCommand command)
	{
		super(title);
		buildFrame = false;
		initGUI();
		
		if ((command != null) && (command.getDialog() != null)) this.centerOn(command.getDialog());
		else this.centerOn(de.uib.configed.Globals.mainFrame);
		this.setSize(700, 400);
		logging.info(this, "SSHConnectionExecHelpDialog end of constructor" );
		this.setVisible (true);
		this.buildFrame = true;
		new SSHConnectExec(command, this);
	}
	public SSHConnectionExecHelpDialog(SSHCommand command, String title )
	{ this(title, command); }

	public SSHConnectionExecHelpDialog(String title )
	{ this(title, null); }
	public SSHConnectionExecHelpDialog(SSHCommand c )
	{ this("", c); }
	public SSHConnectionExecHelpDialog( )
	{ this("", null); }


	private void initGUI() 
	{
		try 
		{
			createLayout(konsolePanelLayout, jScrollPane,de.uib.configed.Globals.gapSize,de.uib.configed.Globals.gapSize, true);
			createLayout(mainPanelLayout, inputPanel,0,0, false);
		}
		catch (Exception e) 
		{
			logging.logTrace(e);
		}
	}
	
	// @Override
	private void createLayout(GroupLayout layout, Component comp, int vgap, int hgap, boolean addInputField)
	{
		int pref = GroupLayout.PREFERRED_SIZE;
		int max = Short.MAX_VALUE;
		GroupLayout.Alignment leading = GroupLayout.Alignment.LEADING;
		// int vgap = de.uib.configed.Globals.gapSize;
		// int hgap = de.uib.configed.Globals.gapSize;

		layout.setAutoCreateGaps(true);
		SequentialGroup verticalGroup=layout.createSequentialGroup();
		verticalGroup.addGap(vgap)
					 .addComponent(comp)
		;
		if (addInputField)
			verticalGroup.addGroup(layout.createParallelGroup(leading)
				.addGap(vgap)
				// .addGroup(layout.createSequentialGroup()
				// 	.addGap(5)
				// 	.addComponent(cb_showResult,GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE )
				// )
				.addComponent(lbl,pref, pref,pref )
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
				.addGap(hgap)
				.addComponent(lbl,pref, pref,max )
				// .addComponent(btn_inBackground,GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE )
				.addComponent(btn_close,pref, pref,pref )
				.addGap(hgap)
			);
	
		layout.setVerticalGroup(verticalGroup);
		layout.setHorizontalGroup(horizontalGroup);	
	}
	// public void cancel()
	// {
	// 	// isOpen = false;
	// 	buildFrame = false;
	// 	this.setVisible (false);
	// 	this.dispose ();
	// 	// ssh.disconnect();
	// }
	public boolean showResult = false;
	private void setVisibility(boolean v)
	{
		this.setVisible(v);
	}
	public void setStatusFinish()
	{
		// finish = f;
		if (showResult) setVisibility(true);
		else cancel();
	}

	// class InputFilter extends DocumentFilter 
	// {
	// 	JTextPane editor;

	// 	public InputFilter(JTextPane editor) 
	// 	{
	// 		this.editor = editor;
	// 	}

	// 	@Override
	// 	public void remove(final FilterBypass fb, final int offset, final int length) throws BadLocationException 
	// 	{
	// 		if (!isGlobalReadOnly()) super.remove(fb, offset, length);
	// 	}

	// 	@Override
	// 	public void replace(final FilterBypass fb, final int offset, final int length, final String text, final AttributeSet attrs)
	// 		throws BadLocationException 
	// 	{
	// 		if (!isGlobalReadOnly()) super.replace(fb, offset, length, text, attrs);
	// 	}

	// 	private boolean isGlobalReadOnly() 
	// 	{
	// 		AttributeSet attributeSet = editor.getCharacterAttributes();
	// 		return attributeSet != null && attributeSet.getAttribute("readonly") != null;
	// 	}
	// }
}


// package de.uib.configed.gui.ssh;


// import java.io.*;
// import java.util.*;
// import java.util.regex.*;
// // import java.util.ArrayList;
// import de.uib.configed.*;
// import de.uib.configed.gui.*;
// import de.uib.opsicommand.*;
// import java.awt.*;
// import java.awt.event.*;
// import javax.swing.text.*;
// import javax.swing.*;
// import javax.swing.table.*;
// import javax.swing.GroupLayout.*;

// import de.uib.utilities.*;
// import de.uib.utilities.logging.*;
// import de.uib.configed.*;
// import de.uib.configed.gui.*;
// import de.uib.opsicommand.*;
// import de.uib.opsicommand.sshcommand.*;


// public class SSHConnectionExecHelpDialog extends GeneralFrame
// {
// 	private JScrollPane jScrollPane;
// 	private JButton btn_cancel;
// 	private GroupLayout thisLayout;
// 	private JTable tableHelp;
// 	private int columns;
// 	private final SSHCommand command_help;
	
// 	// public SSHConnectionExecHelpDialog(String title, int col)
// 	// {
// 	// 	super(null,title,false);
// 	// 	columns = col;
// 	// 	this.centerOn(de.uib.configed.Globals.mainFrame);		
// 	// 	initGUI();
// 	// 	createLayout();
// 	// 	buildHelp();
// 	// 	this.setBackground(de.uib.configed.Globals.backLightBlue);
// 	// 	this.setVisible (true);
// 	// }
// 	public SSHConnectionExecHelpDialog(SSHCommand com, String title)
// 	{
// 		super(null,title,false);
// 		this.command_help = com;
// 		this.columns = ((CommandHelp) command_help).getHelpColumns();
// 		this.centerOn(de.uib.configed.Globals.mainFrame);		
// 		initGUI();
// 		createLayout();
// 		buildHelp();
// 		this.setBackground(de.uib.configed.Globals.backLightBlue);
// 		this.setVisible (true);
// 	}

// 	public void addRow(String[] row)
// 	{
// 		DefaultTableModel model = (DefaultTableModel) tableHelp.getModel();
// 		model.addRow(row);
// 	}
	
// 	private void initGUI() 
// 	{
// 		try 
// 		{
// 			String[] titles = new String[columns];
// 			for (int i=0; i<columns;i++) titles[i]="  ";
// 			TableModel table_Model = new DefaultTableModel( null, titles );
// 			tableHelp = new JTable()
// 			{
// 				@Override
// 				public boolean isCellEditable(int row, int column) {
// 					setBackground(de.uib.configed.Globals.backLightBlue);
// 					return false;
// 				}

// 			};
// 			tableHelp.isCellEditable(0,0);
// 			tableHelp.setModel(table_Model);
// 			tableHelp.setDefaultRenderer(
// 				String.class, 
// 				new de.uib.utilities.table.TableCellRendererConfigured(
// 					null, de.uib.configed.Globals.backLightBlue, de.uib.configed.Globals.backLightBlue, de.uib.configed.Globals.backLightBlue
// 				)
// 			);
// 			TableColumn column = null;
// 			for (int i = 0; i < columns; i++) 
// 			{
// 				column = tableHelp.getColumnModel().getColumn(i);
// 				if (i == columns-1) column.setPreferredWidth(100); //last column is bigger
// 				else column.setPreferredWidth(25);
// 			}
// 			tableHelp.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

// 			thisLayout = new GroupLayout((JComponent)getContentPane());
// 			getContentPane().setLayout(thisLayout);
			
// 			jScrollPane = new JScrollPane(tableHelp);
// 			jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
// 			jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
// 			jScrollPane.getViewport().setBackground(de.uib.configed.Globals.backLightBlue);
			
// 			btn_cancel = new JButton();
// 			btn_cancel.setText("cancel");
// 		}
// 		catch (Exception e) {
// 			e.printStackTrace();
// 		}
// 	}
	
// 	private void createLayout()
// 	{
// 		thisLayout.setAutoCreateGaps(true);
// 		SequentialGroup verticalGroup=thisLayout.createSequentialGroup();
// 		verticalGroup.addGroup(thisLayout.createParallelGroup()
// 			.addGap(10)
// 			.addComponent(jScrollPane)
// 			.addGap(10)
// 		);

// 		ParallelGroup horizontalGroup=thisLayout.createParallelGroup();
// 		horizontalGroup.addGroup(thisLayout.createSequentialGroup()
// 			.addComponent(jScrollPane)
// 		);

// 		horizontalGroup.addGroup(thisLayout.createSequentialGroup()
// 			.addGap(10)
// 		);

// 		thisLayout.setVerticalGroup(verticalGroup);
// 		thisLayout.setHorizontalGroup(horizontalGroup);

// 		this.setSize(700, 400);
// 	}

// 	private void buildHelp()
// 	{
// 		if (((CommandHelp) command_help).getHelpLines() != null) fillHelpDialog();
// 		else
// 		{
// 			try
// 			{
// 				logging.info(this, "buildHelp ");
// 				final String result = new SSHConnectExec().exec(command_help, falseonly return string);
// 				final String[] regexes = ((CommandHelp) command_help).getRegexes();
// 				new Thread()
// 				{
// 					public void run()
// 					{
// 						((CommandHelp) command_help).setHelpLines(createSplittetHelpList( result, regexes));
// 						fillHelpDialog();
// 					}
// 				}.start();
			
// 			}
// 			catch (Exception e)
// 			{
// 				e.printStackTrace();
// 			}
// 		}
// 	}

// 	private final void fillHelpDialog()
// 	{
// 		// int columns = ((CommandHelp) command_help).getHelpColumns();
// 		// SSHConnectionExecHelpDialog outputDialog = new SSHConnectionExecHelpDialog(
// 		// 		configed.getResourceValue("SSHConnection.Exec.title")+ " \""+command.getCommand() + "\" ", 
// 		// 		columns
// 		// );
// 		LinkedList<CommandHelp_Row> commands = ((CommandHelp) command_help).getHelpLines();
		
// 		for (CommandHelp_Row co : commands)
// 		{
// 			String[] row = co.getRow();
// 			if  (row[columns-1].contains("\n"))
// 			{
// 				String[] items = row[columns-1].split("\n");
// 				row[columns-1] = items[0];
// 				addRow(row);

// 				for (int i=1;i<items.length;i++	) 
// 				{
// 					String[] tmp = new String[columns];
// 					tmp[columns-1] = items[i];
// 					addRow(tmp);
// 				}
// 			}
// 			else addRow(row);
// 		}
// 	}

// 	protected LinkedList<CommandHelp_Row> createSplittetHelpList( String fullHelpStr, String[] regex)
// 	{
// 		logging.debug(this, "createSplittetHelpList ");
// 		if ((fullHelpStr == null) || (fullHelpStr.trim() == "")) 
// 			logging.error(this,command_help.getCommand() + " returned null", new NullPointerException() );

// 		LinkedList<String> allLines = new LinkedList<String>(Arrays.asList(fullHelpStr.split("\n")));
// 		LinkedList<CommandHelp_Row> helpLinesSplittet = new LinkedList<CommandHelp_Row>();
		
// 		// int columns = ((CommandHelp) command_help).getHelpColumns();
	
// 		CommandHelp_Row lastcommand = new CommandHelp_Row(null, columns);
		
// 		for (String line: allLines)
// 		{
// 			line = line.trim();
// 			if ((line != null) && (line.length() > 0))
// 			{
// 				String[] splittedLine = new String[columns];
// 				String firstPart = "";
// 				for (int i = 0; i<columns-1;i++)
// 				{
// 					splittedLine[i] = findRegex(regex[i], line);
// 					firstPart += splittedLine[i];
// 					splittedLine[i] = splittedLine[i].trim();
// 				}
// 				if (firstPart.trim().length() <= 0) 
// 				{
// 					if ((line.trim().startsWith("Usage")) || (line.trim().startsWith("Manage")) ||(line.trim().charAt(line.trim().length() - 1) == ':')) 
// 					{
// 						CommandHelp_Row curcommand = new CommandHelp_Row(new String[columns], columns);
// 						curcommand.addFirst(line.trim());
// 						helpLinesSplittet.add(new CommandHelp_Row(new String[columns], columns)); // empty line before Usage/Manage
// 						helpLinesSplittet.add(curcommand);
// 						lastcommand = curcommand;
// 					}
// 					else lastcommand.appendDesk(line.trim());
// 				}
// 				else
// 				{
// 					String lastPart = line;
// 					for (String part : splittedLine) 
// 					{
// 						if (part!= null) lastPart = lastPart.replace(part, "");
// 					}
// 					splittedLine[columns-1] = lastPart.trim();
// 					CommandHelp_Row curcommand = new CommandHelp_Row(splittedLine, columns);
// 					helpLinesSplittet.add(curcommand);
// 					lastcommand = curcommand;
// 				}
// 			}
// 		}
// 		return helpLinesSplittet;
// 	}

// 	private String findRegex(String regex, String line)
// 	{
// 		Pattern p= Pattern.compile(regex);
// 		Matcher matcher = p.matcher(line);
// 		String found = "";
// 		while (matcher.find()) 
// 		{
// 			found = found + matcher.group(0);
// 		}
// 		return found;
// 	}


// 	// public class HelpRow
// 	// {
// 	// 	private String[] row;
// 	// 	private int columns;
// 	// 	HelpRow(String[] parts, int col)
// 	// 	{
// 	// 		columns = col;
// 	// 		row = parts;
// 	// 	}
// 	// 	public void addFirst(String s)
// 	// 	{
// 	// 		row[0] = s;
// 	// 	}
// 	// 	public void appendDesk(String de)
// 	// 	{
// 	// 		row[columns-1] = row[columns-1] + "\n" + de;
// 	// 	}
// 	// 	public String[] getRow()
// 	// 	{
// 	// 		for (int i = 0; i < columns; i++)
// 	// 			if (row[i] == null)
// 	// 		 		row[i] = "";
// 	// 		return row;
// 	// 	}
// 	// 	public String toString()
// 	// 	{
// 	// 		String l = "";
// 	// 		for (String s : row) {
// 	// 			l = l + ";" +s;
// 	// 		}
// 	// 		return l;
// 	// 	}
// 	// }

// 	private void cancel()
// 	{
// 		this.setVisible (false);
// 		this.dispose ();
// 	}

// }


