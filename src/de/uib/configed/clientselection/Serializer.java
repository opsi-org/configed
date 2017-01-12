package de.uib.configed.clientselection;

import java.util.*;
import de.uib.configed.clientselection.elements.*;
import de.uib.configed.clientselection.serializers.*;
import de.uib.configed.clientselection.operations.*;
import de.uib.opsidatamodel.*;
import de.uib.utilities.logging.logging;

/**
 * A serializer is able to save and load searches.
 */
public abstract class Serializer
{
	protected SelectionManager manager;

	public Serializer( SelectionManager manager )
	{
		this.manager = manager;
	}

	/**
	 * Save the given tree of operations under the given name.
	 * If the name already exists, overwrite it.
	 */
	public void save( SelectOperation topOperation, String name, String description )
	{
		Map<String, Object> data = produceData( topOperation );
		saveData( name, description, data );
	}

	/**
	 * Get a list of the names of all saved searches.
	 */
	abstract public List<String> getSaved();

	/**
	* Get the saved searches map
	*/
	abstract public SavedSearches getSavedSearches();


	/**
	* reproduce a search
	*/
	public SelectOperation deserialize( Map<String, Object> data )
	{
		logging.info("deserialize data " + data);
		if (data.get("elementPath") != null)
		{
			logging.info("deserialize, elementPath " + Arrays.toString((String[])data.get("elementPath")));
		}
			
		
		try{
			if( data == null )
				return null;
			SelectOperation operation = getOperation(data, null );
			if( getSearchDataVersion() == 1 )
			{
				operation = checkForHostGroup(operation);
			}
			return operation;
		}
		catch( Exception e )
		{
			//e.printStackTrace();
			logging.error("deserialize error for data " + data + " message " + e.getMessage(), e);
			return null;
		}
	}
	
	
	/**
	* reproduce a search from a serialization string
	*/
	
	public SelectOperation deserialize( String serialized )
	{
		logging.info(this, "deserialize serialized " + serialized);
		SelectOperation result = null;
		
		try{
			Map<String, Object> data = decipher(serialized);
			result = deserialize(data);
		}
		catch( Exception e )
		{
			logging.error("deserialize error " +  e.getMessage(), e);
		}
		
		return result;
	}
			
			
			

	/**
	 * Get one search from searches map
	 */
	public SelectOperation load( String name )
	{
		logging.info(this, "load " + name);
		try
		{
			 Map<String, Object> data = getData(name) ;
			 return deserialize(data);
		}
		
		catch( Exception e )
		{
			//e.printStackTrace();
			logging.error( e.getMessage(), e );
			return null;
		} 
	}

	/**
	 * Remove a saved search from the server
	 */
	abstract public void remove( String name );

	/** Get the data for the given saved search */
	abstract protected Map<String, Object> getData( String name ) throws WrongVersionException;
	
	/** 
	* produce map format of serializiation object
	*/
	abstract protected Map<String, Object> decipher(String serialization) throws WrongVersionException;

	/** Save the search data with the given name.*/
	abstract protected void saveData( String name, String description, Map<String, Object> data );

	/** Get the data version of the currently loaded saved search */
	abstract protected int getSearchDataVersion();

	/* Create a SelectOperation from the given data. This function works recursively. */
	private SelectOperation getOperation( Map<String, Object> data, Map<String, List<SelectElement>> hardware ) throws Exception
	{
		logging.info(this, "getOperation for map " + data + "; hardware " + hardware);
		String elementPathS = null;
		if (data.get("elementPath") != null)
		{
			elementPathS = Arrays.toString((String[])data.get("elementPath"));
			logging.info("getOperation, elementPath in data " + elementPathS);
		}
		// Element
		SelectElement element=null;
		if( data.get("element") != null && !((String) data.get("element")).isEmpty() )
		{
			String elementName = (String) data.get("element");
			logging.info(this, "Element name: " + elementName);
			String[] elementPath = (String[]) data.get("elementPath");
			if( elementName.equals("SoftwareNameElement") )
				element = manager.getNewSoftwareNameElement();
			else if( elementName.equals("GroupElement") )
				element = new GroupElement( manager.getBackend().getGroups().toArray(new String[0]) );
			else if( elementName.startsWith("Generic") )
			{
				if( hardware == null )
				{
					hardware = manager.getBackend().getHardwareList();
				}
				logging.info(this, "getOperation elementPath[0] " + elementPath[0]);
				List<SelectElement> elements = hardware.get(elementPath[0]);
				//logging.info(this, "getOperation hardware " + hardware);
				//logging.info(this, "getOperation hardware elements " + elements );
				
				for( SelectElement possibleElement: elements )
				{
					logging.info(this, "getOperation possibleElement.getClassName() " 
						+ possibleElement + " compare with elementName " + elementName + " or perhaps with elementPathS " + elementPathS);
					
					//if( possibleElement.getClassName().equals( elementName ) )
					//originally, but is nonsense -------------------------------------------
					if( possibleElement.getClassName().equals( elementName ) 
						&&
						Arrays.toString(possibleElement.getPathArray()).equals( elementPathS )
						)
					{
						element = possibleElement;
						break;
					}
				}
			}
			else
			{
				element = (SelectElement) Class.forName("de.uib.configed.clientselection.elements."+elementName).newInstance();
			}
		}
		
		if (element != null)
		{
			String elS = "" + element + " class " + element.getClass() +  " path " + element.getPath() ;
			logging.info( this, "getOperation element " + elS);
		}

		// Children
		List<Map<String, Object> > childrenData = (List<Map<String, Object> >) data.get("children");
		LinkedList<SelectOperation> children = new LinkedList<SelectOperation>();
		if( childrenData != null )
			for( Map<String, Object> child: childrenData )
				children.add( getOperation(child, hardware) );

		// Operation
		String operationName = (String) data.get("operation");
		logging.info( this, "getOperation Operation name: " + operationName);
		SelectOperation operation;
		
		if( getSearchDataVersion() == 1 )
		{
			operation = parseOperationVersion1( operationName, element, children );
		}
		else
		{
			Class operationClass = Class.forName("de.uib.configed.clientselection.operations."+operationName);
			logging.info( this, "getOperation operationClass  " + operationClass.toString() );
			if( element != null )
			{
				logging.info( this, "getOperation element != null, element  " + element );
				operation = (SelectOperation) operationClass.getConstructors()[0].newInstance(element);
			}
			else // GroupOperation
			{
				Class list = Class.forName("java.util.List");
				logging.info( this, "getOperation List name: " + list.toString() );
				operation = (SelectOperation) operationClass.getConstructor(list).newInstance(children);
			}
		}
		
		logging.info(this, "getOperation  " + operation);

		// Data
		SelectData.DataType dataType = (SelectData.DataType) data.get("dataType");
		Object realData = data.get("data");
		SelectData selectData;
		if( dataType == null || data == null )
			selectData = null;
		else
			selectData = new SelectData( realData, dataType );
		operation.setSelectData( selectData );

		return operation;
	}

	/* Create data from the operation recursively. */
	private Map<String, Object> produceData( SelectOperation operation )
	{
		Map<String, Object> map = new HashMap<String, Object>();
		SelectElement element = operation.getElement();
		if( element == null )
		{
			map.put( "element", null );
			map.put( "elementPath", null );
		}
		else
		{
			map.put( "element", element.getClassName() );
			map.put( "elementPath", element.getPathArray() );
		}
		map.put( "operation", operation.getClassName() );
		if( operation.getSelectData() == null )
		{
			map.put( "dataType", null );
			map.put( "data", null );
		}
		else
		{
			map.put( "dataType", operation.getSelectData().getType() );
			map.put( "data", operation.getSelectData().getData() );
		}
		if( operation instanceof SelectGroupOperation )
		{
			List<Map<String, Object> > childData = new LinkedList<Map<String, Object> >();
			for( SelectOperation child: ((SelectGroupOperation) operation).getChildOperations() )
				childData.add( produceData(child) );
			map.put( "children", childData );
		}
		else
		{
			map.put( "children", null );
		}
		logging.info(this, "produced " + map);
		return map;
	}

	/* Parse the operations with the old (version 1) operation names */
	private SelectOperation parseOperationVersion1( String name, SelectElement element, List<SelectOperation> children )
	{
		logging.info(this, "parseOperationVersion1");
		
		if( element != null )
		{
			for( SelectOperation operation: element.supportedOperations() )
			{
				if( operation.getOperationString().equals( name ) )
					return operation;
			}
			throw new IllegalArgumentException("While parsing ver 1 saved search: " + name);
		}
		if( name.equals("Hardware") )
			return new HardwareOperation( children );
		if( name.equals("Software") )
			return new SoftwareOperation( children );
		if( name.equals("SwAudit") )
			return new SwAuditOperation( children );
		if( name.equals("and") )
			return new AndOperation( children );
		if( name.equals("or") )
			return new OrOperation( children );
		if( name.equals("not") )
			return new NotOperation( children );
		throw new IllegalArgumentException("While parsing ver 1 saved search: " + name);
	}

	/* Needed for version 1 data. Adds HostOperations, as they didn't exist in version 1 */
	private SelectOperation checkForHostGroup( SelectOperation operation )
	{
		if( !(operation instanceof SelectGroupOperation) )
		{
			logging.debug("No group: " + operation.getClassName() + ", element path size: " + operation.getElement().getPathArray().length );
			if( operation.getElement().getPathArray().length == 1 )
				return new HostOperation( operation );
			else
				return operation;
		}
		if( operation instanceof HardwareOperation || operation instanceof SoftwareOperation || operation instanceof SwAuditOperation )
			return operation;
		if( !(operation instanceof AndOperation) )
			return new HostOperation( operation );
		AndOperation andOperation = (AndOperation) operation;
		SelectOperation notGroup = operation;
		while( notGroup instanceof SelectGroupOperation )
			notGroup = ((SelectGroupOperation) notGroup).getChildOperations().get(0);
		SelectOperation leftNotGroup = andOperation.getChildOperations().get(1);
		while( leftNotGroup instanceof SelectGroupOperation )
			leftNotGroup = ((SelectGroupOperation) leftNotGroup).getChildOperations().get(0);
		if( notGroup.getElement().getPathArray().length != 1 )
			return operation;
		if( notGroup.getElement().getPathArray().length == 1 && leftNotGroup.getElement().getPathArray().length == 1 )
			return new HostOperation( andOperation );
		List<SelectOperation> ops = andOperation.getChildOperations();
		HostOperation host = new HostOperation( ops.get(0) );
		ops.remove(0);
		ops.add(0, host);
		return new AndOperation( ops );
	}
}