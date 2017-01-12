package de.uib.utilities.table;

/* 
*	Copyright uib (uib.de) 2008
*	Author Rupert Röder
*
*/
 
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import de.uib.utilities.swing.*;

public class JTableWithContextMenu extends JTableWithToolTips
	implements KeyListener, MouseListener
	//provides key control for pop up menus
{
	JPopupMenu popup;
	
	boolean shiftpressed = false;
	
	public JTableWithContextMenu(JPopupMenu contextmenu)
	{
		this.popup = contextmenu;
		addMouseListener (new utils.PopupMouseListener(contextmenu));
		addMouseListener (this);
		addKeyListener(this); 
	}
	
	
	public void valueChanged(javax.swing.event.ListSelectionEvent e)
	{
		super.valueChanged(e);
		//System.out.println ( " jTableGeraeteBuchbar ListSelectionEvent ");
	}
	
	// KeyListener  
	public void keyPressed(KeyEvent e)
	{
		//logging.debug(this, "keypressed " + e);
		if (e.getKeyCode() == KeyEvent.VK_SHIFT)
			shiftpressed = true;
		
		else if (shiftpressed && e.getKeyCode() == KeyEvent.VK_F10)
		{
			Rectangle rect = getCellRect(getSelectedRow(), 0, false);
			popup.show(this, rect.x + (rect.width / 2), rect.y + (rect.height/2)); 
		}
	}
	
	public void keyReleased(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_SHIFT)
			shiftpressed = false;
	}
	
    public void keyTyped(KeyEvent e)
	{
		
	}
	
	// MouseListener
	public void mousePressed(MouseEvent e)
	{
		int rowOfEvent = rowAtPoint(e.getPoint());
		// set row also when right mouse click occured
		setRowSelectionInterval( rowOfEvent, rowOfEvent );
		//popup.show(e.getComponent(), e.getX(), e.getY());
		
	}
	
	public void mouseClicked(MouseEvent e) {} 
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
	
	
}

	
	
