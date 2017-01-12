/* 
 *
 * Entwickelt von uib, www.uib.de, 2012
 * @Author rupert roeder
 *
 */
package de.uib.utilities.table;
import java.awt.Component;
import javax.swing.*; 
import java.text.*;
import de.uib.utilities.*;
import de.uib.utilities.table.gui.ColorTableCellRenderer;
 
public class TableCellRendererBigDecimal extends ColorTableCellRenderer 
{
	JLabel label = new JLabel();
	private java.text.DecimalFormat decimalFormat;

    public TableCellRendererBigDecimal () 
    {
        label.setText("");
        decimalFormat = new DecimalFormat();
    }
    
 
	public Component getTableCellRendererComponent(
			JTable table,
			Object value,            // value to display
			boolean isSelected,      // is the cell selected
			boolean hasFocus,
			int row,
			int column)
			{
				Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				
				String selectedString = "";
				ImageIcon selectedIcon = null;
				
				if (value != null && value instanceof String && !((String)value).equals(""))
				{
					
					try
					{
						//java.math.BigDecimal number = new java.math.BigDecimal((String) value);
						double number = Double.valueOf((String)value);
						//logging.debug(this, " format number " + number);
						selectedString = decimalFormat.format(number);
						
					}
					catch (Exception ex)
					{
						logging.debug(this, " format exception: " + ex);
						logging.logTrace(ex);
					}			
		
				} else {
					selectedString = "";
				}
				
				if (result instanceof JLabel) {
					((JLabel)result).setText(selectedString);
					((JLabel)result).setIcon(selectedIcon);
					((JLabel)result).setToolTipText(selectedString);
					//((JLabel)result).setHorizontalAlignment(CENTER);
				}
				
				
				
				return result;
			}

}
