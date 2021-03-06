package de.uib.opsidatamodel.datachanges;

import java.util.*;
import de.uib.utilities.logging.*;

/**
*/
public class UpdateCollection
			implements UpdateCommand, CountedCollection
{

	protected Collection<Object> implementor;

	// we delegate all Collection methods to this object extending only add()
	public UpdateCollection (Collection<Object> implementor)
	{
		this.implementor = implementor;
	}

	public boolean addAll(Collection c)
	{
		boolean success = true;
		Iterator it=c.iterator();
		while (it.hasNext() && success)
		{
			Object ob = it.next();
			logging.debug(this, "addAll, element of Collection: " + ob);
			if (!add(ob))
				success = false;
		}
		return success;
	}

	public void clear()
	{
		logging.debug(this, "clear()");
		Iterator it = implementor.iterator();
		while (it.hasNext())
		{
			Object obj = it.next();
			if (obj != null)
			{
				if    (obj instanceof Collection)
					// a element of the collection is a collection, we do our best to clear recursively
				{
					try
					{
						logging.debug(this, "by recursion, we will clear " + obj);
						((Collection) obj).clear();

					}
					catch (Exception ex) //perhaps not implemented
					{
					}
				}
			}

		}
		logging.debug(this, "to clear elements of implementor " + implementor);
		implementor.clear();
		logging.debug(this, "cleared: elements of implementor " + implementor.size());
	}

	public void clearElements()
	// *** perhaps we should instead implement a recursive empty which clears only the implementors but does not remove the elements
	{
		//logging.info(this, "clearElements()");

		Iterator it = implementor.iterator();
		while (it.hasNext())
		{
			Object obj = it.next();
			if (obj != null)
			{
				//logging.info(this,"clearElements, element " + obj.getClass());
				if    (obj instanceof UpdateCollection)
					// a element of the collection is a collection, we do our best to clear recursively
				{
					try
					{ ((UpdateCollection) obj).clearElements();
					}
					catch (Exception ex) //perhaps not implemented
					{
					}
				}
			}
		}
	}
	
	public void revert()
	{
		logging.info(this, "revert()");

		Iterator it = implementor.iterator();
		while (it.hasNext())
		{
			Object obj = it.next();
			if (obj != null)
			{
				//logging.info(this,"revert, element " + obj.getClass());
				if    (obj instanceof UpdateCollection)
					// a element of the collection is a collection, we do our best to clear recursively
				{
					try
					{ ((UpdateCollection) obj).revert();
					}
					catch (Exception ex) //perhaps not implemented
					{
					}
				}
			}
		}
	}
	
	public void cancel()
	{
		revert();
		clearElements();
	}
		

	public boolean contains(Object o)
	{
		return implementor.contains(o);
	}

	public boolean containsAll(Collection c)
	{
		return implementor.containsAll(c);
	}

	public boolean equals(Object o)
	{
		return implementor.equals(o);
	}

	public int  hashCode()
	{
		return implementor.hashCode();
	}

	public boolean isEmpty()
	{
		return implementor.isEmpty();
	}

	public Iterator iterator()
	{
		return implementor.iterator();
	}

	public boolean remove(Object o)
	{
		return implementor.remove(o);
	}

	public boolean removeAll(Collection c)
	{
		return implementor.removeAll(c);
	}

	public boolean retainAll(Collection c)
	{
		return implementor.retainAll(c);
	}

	public int size()
	{
		return implementor.size();
	}

	public Object[] toArray()
	{
		return implementor.toArray();
	}

	public Object[] toArray(Object[] a)
	{
		return implementor.toArray(a);
	}

	@SuppressWarnings("unused")
	public boolean add (Object obj)
	{
		logging.debug(this, "###### UpdateCollection add Object  " + obj);
		if  ( !(obj instanceof UpdateCommand))
		{
			logging.debugOut(logging.LEVEL_ERROR, "wrong element type, found" + obj.getClass().getName()
			                 + ", expected an    + UpdateCommand");

			return false;
		}

		if (obj == null)
			return true;

		return  implementor.add(obj);
	}

	public int accumulatedSize()
	{
		if (size() == 0)
			return 0;

		int result = 0;

		Iterator it = implementor.iterator();
		while (it.hasNext())
		{
			Object obj = it.next();
			if (obj != null)
			{
				if    (obj instanceof CountedCollection)
					// a element of the collection is a collection, we retrieve the size recursively
				{
					result = result + ((CountedCollection)obj).accumulatedSize();
				}
				else
					// we found an 'ordinary' element and add 1
					result++;
			}
		}

		return result;
	}

	public Object getController( )
	{
		return null;
	}

	public void setController( Object cont)
	{
	}

	/**
	 doCall calls doCall on all members.
	 This  will give a recursion for members being update collections themselves.
	*/
	public void doCall()
	{
		logging.debug(this, "doCall, element count: " + size());


		if (size() == 0)
			return;

		Iterator it = implementor.iterator();
		while (it.hasNext())
		{
			UpdateCommand theCommand = ((UpdateCommand) it.next());
			//System.out.println ("----------- call updateCommand "  + theCommand);
			if (theCommand != null)
				theCommand.doCall();
		}

		//  removeAll();  do it in the client
	}

}
