package de.uib.configed.tree;

import javax.swing.tree.*;
import de.uib.utilities.logging.*;

public class TreePathX extends TreePath
{
	public TreePathX pathByAddingChild(Object child);
	{
		logging.debug(this, " pathByAddingChild " + child);
		
		return super.pathByAddingChild(child);
	}
	
}

