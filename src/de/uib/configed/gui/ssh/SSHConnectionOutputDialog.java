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
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.GroupLayout.*;

import javax.swing.border.TitledBorder;

public class SSHConnectionOutputDialog extends FGeneralDialog///*javax.swing.JDialog */ GeneralFrame
{
	protected JTextPane output;	
	protected JScrollPane jScrollPane;
	
	// private JCheckBox cb_showResult;
	// private JButton btn_inBackground;
	protected JButton btn_close;
	protected boolean buildFrame = false;
	
	protected JPanel mainPanel = new JPanel();
	protected JPanel inputPanel = new JPanel();

	protected GroupLayout konsolePanelLayout;
	protected GroupLayout mainPanelLayout;
	
	private Color linecolor =  de.uib.configed.Globals.lightBlack;
	private final String ansi_escape1 = "";
	private final String ansi_escape2 = "\u001B";

	public final String ansiCodeEnd = "[0;0;0m";
	public final String ansiCodeEnd1 = "\u001B[0;0;0m";
	public final String ansiCodeEnd2 = "[0;0;0m";

	public final String ansiCodeInfo = "[0;info;0m"; // user info not really ansi code !!
	public final String ansiCodeError = "[0;error;0m"; // user info "error" not really ansi code !!
	
	public final Map<String, Color> ansiCodeColors = new HashMap<String, Color>(){{
		put( "[0;info;0m", de.uib.configed.Globals.greyed);  // user info not really ansi code !!
		put( "[0;error;0m", de.uib.configed.Globals.actionRed); // user info "error" not really ansi code !!
		put( "[0;30;40m", Color.BLACK);
		// ansis beginning with "[1": lines should be unterlined - are not !
		put( "[1;30;40m", Color.BLACK); 
		put( "[0;40;40m", Color.BLACK);
		put( "[1;40;40m", Color.BLACK); 
		put( "[0;31;40m", de.uib.configed.Globals.actionRed);
		put( "[1;31;40m", de.uib.configed.Globals.actionRed);
		put( "[0;41;40m", de.uib.configed.Globals.actionRed);
		put( "[1;41;40m", de.uib.configed.Globals.actionRed);
		put( "[0;32;40m", de.uib.configed.Globals.okGreen);
		put( "[1;32;40m", de.uib.configed.Globals.okGreen);
		put( "[0;42;40m", de.uib.configed.Globals.okGreen);
		put( "[1;42;40m", de.uib.configed.Globals.okGreen);
		put( "[0;33;40m", de.uib.configed.Globals.darkOrange);
		put( "[1;33;40m", de.uib.configed.Globals.darkOrange);
		put( "[0;43;40m", de.uib.configed.Globals.darkOrange);
		put( "[1;43;40m", de.uib.configed.Globals.darkOrange);
		put( "[0;34;40m", de.uib.configed.Globals.blue);
		put( "[1;34;40m", de.uib.configed.Globals.blue);
		put( "[0;44;40m", de.uib.configed.Globals.blue);
		put( "[1;44;40m", de.uib.configed.Globals.blue);
		put( "[0;35;40m", Color.MAGENTA);
		put( "[1;35;40m", Color.MAGENTA);
		put( "[0;45;40m", Color.MAGENTA);
		put( "[1;45;40m", Color.MAGENTA);
		put( "[0;36;40m", Color.CYAN);
		put( "[1;36;40m", Color.CYAN);
		put( "[0;46;40m", Color.CYAN);
		put( "[1;46;40m", Color.CYAN);
		put( "[0;37;40m", de.uib.configed.Globals.lightBlack);
		put( "[1;37;40m", de.uib.configed.Globals.lightBlack);
		put( "[0;47;40m", de.uib.configed.Globals.lightBlack);
		put( "[1;47;40m", de.uib.configed.Globals.lightBlack);
	}};
	
	protected class DialogCloseListener implements ActionListener{
			public void actionPerformed(ActionEvent e)
			{
				logging.debug(this, "actionPerformed " + e);
				cancel();
				//JOptionPane.showMessageDialog(de.uib.configed.Globals.mainFrame, "we got cancel");
			}
		}
	;
	
	DialogCloseListener closeListener;


	// protected JDialog parentDialog;
	// private static SSHConnectionOutputDialog instance;
	public SSHConnectionOutputDialog(String title)
	{
		super(null,title, false);
		buildFrame = false;
		closeListener =new DialogCloseListener();
		initOutputGui();
		this.setSize(700, 400);
		this.centerOn(de.uib.configed.Globals.mainFrame);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	public void setStartAnsi(Color c)
	{
		linecolor = c;
	}

	public void append(String line, Component focusedComponent)
	{
		append("", line);
	}

	private String findAnsiCodeColor(Map.Entry entry, String key, String line )
	{
		if (line.trim().replaceAll("\\t","").replaceAll(" ","").startsWith(key))
		{
			linecolor = (Color) entry.getValue();
			line = line.replace(key, "");
			logging.debug(this, "append parseAnsiCodes found color key " + key + " value " + ((Color)entry.getValue()).toString());

			// if ( (line.trim().replaceAll("\\t","").replaceAll(" ","").charAt(0) == ansi_escape1.toCharArray()[0]) 
			// 	|| (line.trim().replaceAll("\\t","").replaceAll(" ","").charAt(0) == ansi_escape2.toCharArray()[0]) )
			// 	line = line.replace(ansi_escape2, "");
			line = line.replace(ansi_escape1, "").replace(ansi_escape2, "");
		}
		return line ;
	}

	public void append(String line)
	{
		append("", line);
	}
	public void append(String caller, String line)
	{
		// if ((line == null) || (line.trim().length() <=0)) return;
		// Color linecolor = Color.BLACK;
		if (SSHCommandFactory.getInstance().ssh_colored_output)
		{
			if ((line != null) && (!line.trim().replaceAll("\\t","").replaceAll(" ","").equals("")))
				for (Map.Entry entry : ansiCodeColors.entrySet()) 
					line = findAnsiCodeColor(entry, (String) entry.getKey() ,line );

		}
		logging.debug(this, "line " + line.replace("\n", "") + " color " + linecolor.toString());
		StyleContext sc = StyleContext.getDefaultStyleContext();
        		AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, linecolor);
		aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

		output.setCaretPosition( output.getDocument().getLength());
		output.setCharacterAttributes(aset, false);
		if ( (line.contains(ansiCodeEnd)) || (line.contains(ansiCodeEnd1)) || (line.contains(ansiCodeEnd2)) )
		{
			line = line.replace(ansiCodeEnd, "").replace(ansiCodeEnd1, "").replace(ansiCodeEnd2, "");
			linecolor = Color.BLACK;
		}
		try 
		{
			StyledDocument doc = output.getStyledDocument();
			doc.insertString(doc.getLength(), caller + line, aset);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}


	private void initOutputGui() 
	{
		try 
		{
			Dimension btn_dim = new Dimension(de.uib.configed.Globals.graphicButtonWidth + 15 ,de.uib.configed.Globals.buttonHeight + 3);
			inputPanel.setBackground(de.uib.configed.Globals.backLightBlue);
			mainPanel.setBackground(de.uib.configed.Globals.backLightBlue);
			getContentPane().add(mainPanel, BorderLayout.CENTER);

			mainPanelLayout = new GroupLayout((JComponent)mainPanel);
			konsolePanelLayout = new GroupLayout((JComponent)inputPanel);
			
			inputPanel.setLayout(konsolePanelLayout);
			mainPanel.setLayout(mainPanelLayout);
			
			jScrollPane = new JScrollPane();
			jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			
			output = new JTextPane();
			output.setEditable(false);
			output.setBackground(Color.GREEN);
			output.setContentType("text/rtf");
			output.setPreferredSize(new Dimension(250, 200));
			StyledDocument doc = (StyledDocument) output.getDocument();
			Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
			Style readonlyStyle = doc.addStyle("readonlyStyle", defaultStyle);

			StyleConstants.setBackground(readonlyStyle, Color.GREEN);// Kein grün :(
			Style style = doc.addStyle("StyleName", null);

        			// StyleConstants.setBackground(style, Color.blue);
			StyleConstants.setForeground(readonlyStyle, Color.RED); // Was ist rot?

			SimpleAttributeSet readOnlyAttributeSet = new SimpleAttributeSet(doc.getStyle("readonlyStyle"));
			readOnlyAttributeSet.addAttribute("readonly", true);
			((AbstractDocument) doc).setDocumentFilter(new InputFilter(output));

			DefaultCaret caret = (DefaultCaret)output.getCaret();
			caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
			
			jScrollPane.setViewportView(output);
			output.setText("");

			// btn_close = new JButton();
			// // buttonPanel.add(btn_close);
			// btn_close.setText(configed.getResourceValue("SSHConnection.buttonClose"));
			btn_close= new de.uib.configed.gui.IconButton(
				de.uib.configed.configed.getResourceValue("SSHConnection.buttonClose") ,
				"images/cancel.png", "images/cancel.png", "images/cancel.png",true
			);
			// btn_test_command.setSize(new Dimension( Globals.graphicButtonWidth + 15 ,Globals.lineHeight));
			// btn_test_command.setSize(new Dimension( Globals.graphicButtonWidth + 15 ,Globals.lineHeight));
			btn_close.setPreferredSize(btn_dim);
		
			btn_close.addActionListener(closeListener); 
			
			// lbl_userhost = new JLabel();
			// lbl_userhost.setText("user@host");
			
			// createLayout(konsolePanelLayout, jScrollPane,de.uib.configed.Globals.gapSize, de.uib.configed.Globals.gapSize, false);
			// createLayout(mainPanelLayout, inputPanel,0,0, false);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	public boolean showResult = true;
	
	public void setStatusFinish(String s)
	{
		if (showResult) setVisible(true);
		else cancel();
	}
	public void setStatusFinish()
	{
		
		if (showResult) setVisible(true);
		else cancel();
		
	}
	
	@Override
	public void  setVisible(boolean b)
	{
		logging.debug(this, "setVisible " + b);
		super.setVisible(b);
	}
	
	public void cancel() 
	{
		buildFrame = false;
		logging.debug(this, "cancel");
		super.doAction2();
	}


	class InputFilter extends DocumentFilter 
	{
		JTextPane editor;

		public InputFilter(JTextPane editor) 
		{
			this.editor = editor;
		}

		@Override
		public void remove(final FilterBypass fb, final int offset, final int length) throws BadLocationException 
		{
			if (!isReadOnly()) super.remove(fb, offset, length);
		}

		@Override
		public void replace(final FilterBypass fb, final int offset, final int length, final String text, final AttributeSet attrs)
			throws BadLocationException 
		{
			if (!isReadOnly()) super.replace(fb, offset, length, text, attrs);
		}

		private boolean isReadOnly() 
		{
			AttributeSet attributeSet = editor.getCharacterAttributes();
			return attributeSet != null && attributeSet.getAttribute("readonly") != null;
		}
	}
}