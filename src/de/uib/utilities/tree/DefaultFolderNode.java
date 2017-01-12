package de.uib.utilities.tree;

import javax.swing.tree.DefaultMutableTreeNode;

public class  DefaultFolderNode extends DefaultMutableTreeNode
{
  public DefaultFolderNode (Object userObject)
  {
     super (userObject);
  }
  
  public boolean isLeaf ()
  {
    return false;
  }
}


