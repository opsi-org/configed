package de.uib.utilities.table.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.*;
import de.uib.configed.Globals;

public class CellEditor4TableText  
		extends AbstractCellEditor
        implements TableCellEditor 
		//, ActionListener
		, MouseListener
		, KeyListener
		, FocusListener
                                 
{
	String oldValue; 
	String currentValue;
	JTextField editorContent;
	de.uib.utilities.swing.FEdit fEdit;
	boolean globalFEdit = false;
	boolean fEditInitialized = false;
	Dimension initSize;
	
	public CellEditor4TableText(de.uib.utilities.swing.FEdit fEdit, Dimension initSize)
	{
		editorContent = new JTextField();
		
		if (fEdit == null)
			this.fEdit = new de.uib.utilities.swing.FEditText("");
		else
		{
			this.fEdit = fEdit;
			globalFEdit = true;
		}
		
		fEditInitialized = false;
		
		this.initSize = initSize; 
		
		editorContent.setEditable(false);
		//editorContent.setFont(editorContent.getFont().deriveFont(Font.ITALIC));
		//editorContent.addActionListener(this);
		editorContent.addMouseListener(this);
		editorContent.addKeyListener(this);
		editorContent.addFocusListener(this);
		editorContent.setBorder(null);
	}
	
	public CellEditor4TableText()
	{
		this(null, null);
	}
	
	//interface
	//ActionListener
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == editorContent)
		{
			//System.out.println("action event occurred on editorContent");
			fireEditingStopped();
		}

	}
	
	// interface
	// MouseListener
	public void mouseClicked(MouseEvent e)
	{
		if (e.getSource() == editorContent)
		{
			if (e.getClickCount() >1 || e.getButton() != MouseEvent.BUTTON1)
			{
				//System.out.println("double clicked");
				fEdit.setVisible(true);
			}
				
		}
	}
    public void mouseEntered(MouseEvent e)
	{}
    public void mouseExited(MouseEvent e)
	{}
    public void mousePressed(MouseEvent e)
	{}
    public void mouseReleased(MouseEvent e)
	{}
	
	// interface
	// KeyListener
	public void keyPressed(KeyEvent e)
	{
		//System.out.println("key event " + e);
		if (e.getSource() == editorContent)
		{
			if (e.getKeyCode() == 32)
				fEdit.setVisible(true);
		}
		
	}
    public void  keyReleased(KeyEvent e)
	{}
    public void  keyTyped(KeyEvent e)
	{}
	
	// interface
	// FocusListener
	public void focusGained(FocusEvent e)
	{
		//System.out.println(" focus event, we have  " +  fEdit.getText());
		if (e.getSource() == editorContent)
		{
			editorContent.setText(fEdit.getText());
		}
	}
	
    public void focusLost(FocusEvent e)
	{
	}
	
	//Implement the one CellEditor method that AbstractCellEditor doesn't.
    public Object getCellEditorValue() {
		currentValue = fEdit.getText();
		//System.out.println("getCellEditorValue(): " + currentValue);
		fEdit.setVisible(false);
		if (!fEditInitialized)
			fEditInitialized = true;
		
        return currentValue;
    }
	

    //Implement the one method defined by TableCellEditor.
    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column) 
	{
        oldValue = (String)value;
		fEdit.setCaller(editorContent);
		//System.out.println(" getTableCellEditorComponent() , value: " + value + " oldValue: " +  oldValue); 
		fEdit.setStartText(oldValue);
		
		if (!fEditInitialized)
		{
			if (initSize != null)
				fEditInitialized = fEdit.init(initSize);
			else
				fEditInitialized = fEdit.init(new Dimension(table.getCellRect(row,column, true).width + 60, table.getCellRect(row, column, true).height + 30));
		}
		
		java.awt.Point loc = table.getLocationOnScreen();
		java.awt.Rectangle rec = table.getCellRect(row, column, true);
		fEdit.setLocation((int) (loc.getX() + rec.getX() + 30), (int) (loc.getY() + rec.getY() + 20));
		
		fEdit.setTitle(" (" + Globals.APPNAME + ")  '" + table.getColumnName(column) + "'");
		fEdit.setVisible(true); 
		
		currentValue = oldValue;
		return editorContent; 
    }
	
}
