package de.uib.configed.guidata;

import java.util.*;
import javax.swing.table.*;
import javax.swing.event.*;
import de.uib.utilities.*;


public interface IFInstallationStateTableModel extends TableModel, ComboBoxModeller
{
		public int getColumnIndex(String columnName);
			
		public void clearCollectChangedStates();
	
		public String getLastStateChange(int row);
		
		public  Map<String, Map<String, Object>> getGlobalProductInfos();
		
}		

