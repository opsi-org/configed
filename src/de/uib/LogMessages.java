 package de.uib;
 
 import javax.swing.*;
 import javax.swing.text.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.table.*;
 import javax.swing.event.*;
 import java.time.*;
 import java.util.*;
 import java.io.*;
 
 /**
  * class LogMessages
  * 
  * description: Class to execute LogMessages
  * 
  * @author Nils Otto
  */
public class LogMessages {
	
	public static void logComponent(Component component) {
		ausgabeAbtrennerStart();
		ausgabe(component);
		ausgabeChildren(component);
		ausgabeAbtrennerEnde();
	}
	
	public static void logDocument(DocumentEvent de) {
		Document document = de.getDocument();
		try {
			System.out.print("Änderung in Textfeld; neuer Text: ");
			System.out.println(document.getText(0, document.getLength()));
		} catch(BadLocationException ble) {
			System.out.println(ble);
		}
	}
	
	private static void ausgabeChildren(Component component) {
		int parentNr = 0;
		while((component = component.getParent()) != null) {
			parentNr++;
			System.out.print("Ist Child von: ");
			for(int i = 0; i < parentNr; i++)
				System.out.print('-');
			System.out.print("> ");
			ausgabe(component);
		}
	}
	
	private static void ausgabe(Component component) {
		if(component instanceof JLabel)
			ausgabeJLabel((JLabel) component);

		else if(component instanceof JButton)
			ausgabeJButton((JButton) component);

		else if(component instanceof JCheckBox)
			ausgabeJCheckBox((JCheckBox) component);

		else if(component instanceof JPopupMenu)
			ausgabeJPopupMenu((JPopupMenu) component);
			
		else if(component instanceof JCheckBoxMenuItem)
			ausgabeJCheckBoxMenuItem((JCheckBoxMenuItem) component);
			
		else if(component instanceof JComboBox)
			ausgabeJComboBox((JComboBox) component);
		
		else if(component instanceof JSpinner)
			ausgabeJSpinner((JSpinner) component);
			
		else if(component instanceof JTextComponent)
			ausgabeJTextComponent((JTextComponent) component);

		else if(component instanceof JMenu)
			ausgabeJMenu((JMenu) component);
			
		else if(component instanceof JMenuItem)
			ausgabeJMenuItem((JMenuItem) component);

		else if(component instanceof JFrame)
			ausgabeJFrame((JFrame) component);

		else
			ausgabeComponent(component);
	}

	private static void ausgabeJLabel(JLabel jLabel) {
		System.out.println("JLabel: Text: " + jLabel.getText() + "; ");
	}

	private static void ausgabeJButton(JButton jButton) {		
		System.out.println("JButton: Text: " + jButton.getText() + "; ");
	}

	private static void ausgabeJCheckBox(JCheckBox jCheckBox) {
		System.out.print("JCheckBox: Text: " + jCheckBox.getText() + "; ");
		System.out.println("Neuer Wert: " + (jCheckBox.isSelected() ? "nicht " : "") + "ausgewählt; ");
	}

	private static void ausgabeJCheckBoxMenuItem(JCheckBoxMenuItem jCheckBoxMenuItem) {
		System.out.print("JCheckBoxmenuItem: Text: " + jCheckBoxMenuItem.getText() + "; ");
		System.out.println("Neuer Wert: " + (jCheckBoxMenuItem.isSelected() ? "" : "nicht ") + "ausgewählt; ");
	}
	
	private static void ausgabeJComboBox(JComboBox jComboBox) {
		System.out.print("StateChange: > JComboBox; ");
		System.out.println("AuswahlNeu: " + jComboBox.getSelectedItem());
	}
	
	private static void ausgabeJSpinner(JSpinner jSpinner) {
		System.out.println("StateChange: > JSpinner; ");
		System.out.println("AuswahlNeu: " + jSpinner.getValue().toString());
	}
	
	private static void ausgabeJTextComponent(JTextComponent jTextComponent) {
		System.out.print("FocusChange: Focus gerichtet auf: ");
	}

	private static void ausgabeJMenuItem(JMenuItem jMenuItem) {
		System.out.println("JMenuItem: Text: " + jMenuItem.getText() + "; ");
	}

	private static void ausgabeJMenu(JMenu jMenu) {
		System.out.println("JMenu: Text: " + jMenu.getText() + "; ");
	}

	private static void ausgabeJPopupMenu(JPopupMenu jPopupMenu) {
		System.out.println("JPopupMenu in: { ");
		ausgabe(jPopupMenu.getInvoker());
		ausgabeChildren(jPopupMenu.getInvoker());
		System.out.println("} // Ende JPopupmenu");
	}

	private static void ausgabeJFrame(JFrame jFrame) {
		System.out.println("JFrame: Titel: " + jFrame.getTitle() + "; ");
	}
	
	private static void ausgabeComponent(Component Component) {
		System.out.println(Component.getClass().toString());
	}

	private static void ausgabeAbtrennerStart() {
		System.out.println("================");

		LocalDate datum = LocalDate.now();
		LocalTime uhrzeit = LocalTime.now();
		
		System.out.println(datum.toString() + "  " + uhrzeit.toString());
	}

	private static void ausgabeAbtrennerEnde() {
		System.out.println("================");
	}
}
