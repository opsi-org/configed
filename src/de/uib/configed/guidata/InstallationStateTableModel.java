package de.uib.configed.guidata;

import de.uib.configed.configed;
import de.uib.configed.Globals;
import de.uib.configed.ConfigedMain;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;
import java.awt.Color;
import java.util.*;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ComboBoxModel;
import java.text.Collator; 

import de.uib.opsidatamodel.PersistenceController;
import de.uib.opsidatamodel.productstate.*;


/**
Defining the TableModel for the product table for a specific client.
Since we here have the required data the class implements the ComboBoxModeler for getting cell editors.
*/ 
public class InstallationStateTableModel 
extends javax.swing.table.AbstractTableModel
implements ComboBoxModeller,
	IFInstallationStateTableModel
{
	
	public static final String EMPTYFIELD = "_";
	//public static final String NOT_AVAILABLEstring = "--";
	public static final String CONFLICTstring = Globals.CONFLICTSTATEstring;
	
	public static final Color backgroundGrey = new Color (220,220,220);
	public static final Color conflictBackColor =  backgroundGrey;
	public static final Color conflictTextColor = backgroundGrey;
	public static final Color defaultBackColor = Color.white;
	public static final Color defaultTextColor = Color.black;
	public static final Color notUpdatedTextColor =  defaultTextColor; // new Color(250, 30, 0); // needed for update_version_display

	final String initString = "";
	
	protected static Map<String, String> columnDict;
	protected static java.util.List<String> columnsLocalized;
	
	protected String actualProduct = "";
	
	protected ConfigedMain main;
	
	protected Vector productsV = null;
	
	/*
	protected Map targets; //combined values for selected clients
	protected Map states;  //combined values for selected clients
	protected Map installationInfos;  //combined values for selected clients
	protected Map results; //combined values for selected clients
	protected Map progresses; //combined values for selected clients
	protected Map lastActions; //combined values for selected clients
	protected Map actions; //combined values for selected clients
	protected Map priorities; //combined values for selected clients
	protected Map positions; //combined values for selected clients
	protected Map versionInfos; //combined values for selected clients
	protected Map productVersions; //combined values for selected clients
	protected Map packageVersions; //combined values for selected clients
	*/
	
	protected Map<String, Map<String, String>> combinedVisualValues; //state key  (column name) --> product name --> visual value
	
	protected Map stateChanges;
	
	protected Map<String, Set<String>> product2setOfClientsWithNewAction; 
							//for each product, we shall collect the clients that have a changed action request
	protected Map<String, ActionRequest> productActions;
							//for each product, we remember the visual action that is set
	
	
	protected Map<String, Map<String, Map<String, String>>>allClientsProductStates; // (clientId -> (productId -> (property key -> property value)))
	
	
	protected PersistenceController persist;
	protected Map<String, Map<String, Map<String, String>>> collectChangedStates;
	protected final String[] selectedClients;
	protected Map possibleActions;
	protected Map<String, Map<String, Object>> globalProductInfos;
	protected String theClient; 
	protected TreeSet tsProductNames;
	protected Vector<String> productNamesInDeliveryOrder;
	
	
	protected ActionRequest actionInTreatment;
	protected boolean changeActionIsSet = false;
	
	protected List<String> displayColumns;
	protected int numberOfColumns;
	protected List<String> preparedColumns; //the columns for which code exists
	protected List<String> columnTitles;
	protected int[] indexPreparedColumns; //the indices of the displayColumns in the displayColumns
	protected boolean[] editablePreparedColumns;
	
	//collects titles for the columns prepared in this class
	
	public static void restartColumnDict()
	{
		columnDict = null;	
	}
	
	public static String getColumnTitle(String column)
	{
		if (columnDict == null)
		{
			columnDict = new HashMap<String, String>();
			columnDict.put("productId", configed.getResourceValue("InstallationStateTableModel.productId"));
			columnDict.put(ProductState.KEY_productName, configed.getResourceValue("InstallationStateTableModel.productName"));
			columnDict.put(ProductState.KEY_targetConfiguration, configed.getResourceValue("InstallationStateTableModel.targetConfiguration"));
			columnDict.put(ProductState.KEY_installationStatus, configed.getResourceValue("InstallationStateTableModel.installationStatus"));
			
			columnDict.put("installationInfo", "Report");
			//combines the following three
			columnDict.put(ProductState.KEY_actionProgress, configed.getResourceValue("InstallationStateTableModel.actionProgress"));
			columnDict.put(ProductState.KEY_actionResult, configed.getResourceValue("InstallationStateTableModel.actionResult"));
			columnDict.put(ProductState.KEY_lastAction, configed.getResourceValue("InstallationStateTableModel.lastAction"));
			
			columnDict.put(ProductState.KEY_actionRequest, configed.getResourceValue("InstallationStateTableModel.actionRequest"));
			columnDict.put(ProductState.KEY_productPriority, configed.getResourceValue("InstallationStateTableModel.priority"));
			columnDict.put(ProductState.KEY_actionSequence, "actionSequence");
			
			columnDict.put(ProductState.KEY_position,  configed.getResourceValue("InstallationStateTableModel.position") );
			
			columnDict.put(ProductState.KEY_versionInfo, "Version"); 
			//combines the following two				
			columnDict.put(ProductState.KEY_productVersion, configed.getResourceValue("InstallationStateTableModel.productVersion"));
			columnDict.put(ProductState.KEY_packageVersion, configed.getResourceValue("InstallationStateTableModel.packageVersion"));
			
			
			columnDict.put(ProductState.KEY_lastStateChange, configed.getResourceValue("InstallationStateTableModel.lastStateChange"));
			
		}
		
		if (columnDict.get(column) == null)
			return "";
		
		return columnDict.get(column);
	}	
	
	public static java.util.List<String> localizeColumns(java.util.List<String> cols)
	{
		java.util.List<String> result = new ArrayList<String>();
		
		if (columnDict != null)
		{
			for (String col : cols)
			{
				if (columnDict.get(col) != null)
					result.add(columnDict.get(col));
			}
		}
		return result;
	}
			
	
	public InstallationStateTableModel (	String[] selectedClients, ConfigedMain main, 
						Map<String, Map<String, Map<String, String>>> collectChangedStates,
						List listOfInstallableProducts,
						Map<String, java.util.List<Map<String, String>>> statesAndActions,
						Map possibleActions,
						Map<String, Map<String, Object>> productGlobalInfos, 
						List<String> displayColumns
						)
	{
		logging.debug(this, "creating an InstallationStateTableModel ");
		//logging.debug(this, " statesAndActions " + statesAndActions);
		this.main = main;
		
		this.collectChangedStates = collectChangedStates;
		this.selectedClients = selectedClients;
		
		this.possibleActions = possibleActions;
		this.globalProductInfos = productGlobalInfos;
		
		initColumnNames(displayColumns);
		initChangedStates();
		
		Iterator  iter = ProductState.KEYS.iterator();
		
		combinedVisualValues = new HashMap<String, Map<String, String>>();
		while (iter.hasNext())
		{
			String key = (String) iter.next();
			//logging.debug(this, "generating combinedVisualValues for  " + key);
			HashMap combinedVisualValuesForOneColumn = new  HashMap<String, String>();
			combinedVisualValues.put(key, combinedVisualValuesForOneColumn);
		}
		
		/*
		targets = new HashMap();
		states = new HashMap();
		
		installationInfos = new HashMap();
		//combines the following three:
		results = new HashMap();
		progresses = new HashMap();
		lastActions = new HashMap();
		
		actions = new HashMap();
		
		priorities = new HashMap();
		positions = new HashMap();
		
		versionInfos = new HashMap();
		//combines the following two
		productVersions = new HashMap();
		packageVersions = new HashMap();
		
		stateChanges = new HashMap();
		*/
		
		persist = main.getPersistenceController();
		Collator myCollator = Collator.getInstance();
		//myCollator.setStrength(Collator.PRIMARY); //ignores hyphens
		myCollator.setStrength(Collator.SECONDARY);
		
		//logging.info(this, "listOfInstallableProducts " + listOfInstallableProducts);
		productNamesInDeliveryOrder = new Vector<String>();
		if (listOfInstallableProducts != null)
		{
			for (int i = 0; i < listOfInstallableProducts.size(); i++)
			{
				String product = (String) listOfInstallableProducts.get(i);
				productNamesInDeliveryOrder.add(product);
			}
		}
			
		
		tsProductNames = new TreeSet (myCollator);
		tsProductNames.addAll(productNamesInDeliveryOrder);
		productsV = new Vector (tsProductNames);
		
		logging.debug(this, "tsProductNames " + tsProductNames);

		allClientsProductStates = new HashMap<String, Map<String, Map<String, String>>>();
		// later we need an alternative structuring of the data 
		
		
		produceVisualStatesFromExistingEntries(statesAndActions);
		completeVisualStatesByDefaults();
		
		//logging.debug(this, "created an InstallationStateTableModel ");
	}
	
	private void produceVisualStatesFromExistingEntries( Map<String, java.util.List<Map<String, String>>> clientAllProductRows)
	{
		if (clientAllProductRows == null)
			return;
		
		Iterator clientIter = clientAllProductRows.keySet().iterator();
		//iterate through all clients for which a list of products/states/actionrequests exist
		//logging.debug(this, ""  + clientAllProductRows);
		
		
		while (clientIter.hasNext())
		{
			String clientId = (String) clientIter.next();
		
			Map<String, Map<String, String>> productRows = new HashMap<String, Map<String, String>>();
			
			allClientsProductStates.put(clientId, productRows);
			// for each client we build the productstates map
			
			List<Map<String, String>> productRowsList1client = clientAllProductRows.get(clientId);
			for (int i = 0;  i < productRowsList1client.size(); i++)
			{
				
				Map<String, String> stateAndAction = productRowsList1client.get(i);
				String productId = stateAndAction.get(ProductState.KEY_productId);
				
				
				/*
					creating an adapted object has been transferred to PersistenceController
				
				Map<String, String> productMapAsRetrieved = productRowsList1client.get(i);  //old name : aProduct
				String productId = (String) productMapAsRetrieved.get(ProductState.KEY_productId);
				
				Map<String, String> stateAndAction = new ProductState(productMapAsRetrieved); 
				//logging.debug(this, "produceVisualStatesFromExistingEntries stateAndAction " + stateAndAction);
				//System.exit(0);
				//transform values, e.g. include installationinfo
				//add to own collection
				
				*/
				
				
				
				productRows.put (productId, stateAndAction);
				
				
				
				
				//change values for visual output
				String targetConfiguration = stateAndAction.get(ProductState.KEY_targetConfiguration);
				if (targetConfiguration.equals(""))
					targetConfiguration = TargetConfiguration.getLabel(TargetConfiguration.UNDEFINED);
				stateAndAction.put(ProductState.KEY_targetConfiguration, targetConfiguration);
				
				String priority = "";
				if (globalProductInfos != null &&  globalProductInfos.get(productId) != null)
					priority = "" +  globalProductInfos.get(productId).get("priority");//aProduct.get(ActionSequence.KEY);
				stateAndAction.put(ProductState.KEY_productPriority, priority);
				
				stateAndAction.put(ProductState.KEY_actionSequence, priority);
				
				//stateAndAction.put(ProductState.KEY_lastStateChange, "test");
				
		
				//build visual states
				Iterator  iter = ProductState.KEYS.iterator();
				
				while (iter.hasNext())
				{
					String key = (String) iter.next();
					
					//logging.debug(this, "produceVisualStates, clientId " + clientId + ", productMap " +  productMapAsRetrieved);
					//logging.debug(this, "produceVisualStates, clientId " + clientId + ", stateAndAction " +  stateAndAction);
					mixToVisualState ( combinedVisualValues.get(key),
						productId, 
						stateAndAction.get(key)
						);
					//logging.info(this, "produceVisualStates, clientId " + clientId + ", lastStateChange " +  stateAndAction.get(ProductState.KEY_lastStateChange));
					//logging.info(this, "produceVisualStates, clientId " + clientId + ", lastStateChange " +  combinedVisualValues.get(ProductState.KEY_lastStateChange));
					
				}
				
				/*	
				mixToVisualState (states, productId, installationStatus);
				mixToVisualState (productVersions, productId, productVersion);
				mixToVisualState (packageVersions, productId, packageVersion);
				mixToVisualState(progresses, productId, actionProgress);
				
				mixToVisualState(targets, productId, targetConfiguration);
				mixToVisualState(results, productId, actionResult);
				//logging.debug(this, "results " + results + ", actionResult mixed in: " + actionResult);
				mixToVisualState(lastActions, productId, lastAction);
				mixToVisualState(priorities, productId, priority);
				
				//mixToVisualState(positions, productId, position); //directly set in retrieveValueAt
				*/
			
				
			}
		}
	}
	
	private void completeVisualStatesByDefaults()
	{
		for (int i = 0; i < selectedClients.length; i++)
		{
			
			//check if productstates exist
			Map<String, Map<String, String>> productStates = allClientsProductStates.get(selectedClients[i]);
			if (productStates == null)
			{
				productStates  = new HashMap<String, Map<String, String>> ();
				allClientsProductStates.put(selectedClients[i], productStates);
			}
			
			
			//check if products for clients exist
			for (int j = 0; j < productsV.size(); j++)
			{
				
				String productId = (String) productsV.get(j);
				Map<String, String> stateAndAction = productStates.get(productId); 
				
				if (stateAndAction == null)
				{
					
					/*
					stateAndAction = new ProductState(null);
					//ProductState.getDEFAULT(); //for testing
					//defaults for the product
					productStates.put( productId, stateAndAction);
					
					stateAndAction.put(ProductState.KEY_productId, productId);
					
					
				
					String priority = "";
					if (globalProductInfos != null &&  globalProductInfos.get(productId) != null)
						priority = "" +  globalProductInfos.get(productId).get("priority");//aProduct.get(ActionSequence.KEY);
					stateAndAction.put(ProductState.KEY_productPriority, priority);
					
					stateAndAction.put(ProductState.KEY_actionSequence, priority);
					
					*/
					
					//build visual states
					Iterator  iter = ProductState.KEYS.iterator();
					
					String priority = "";
						
					if (globalProductInfos != null &&  globalProductInfos.get(productId) != null)
						priority = "" +  globalProductInfos.get(productId).get("priority");//aProduct.get(ActionSequence.KEY);
					
					while (iter.hasNext())
					{
						String key = (String) iter.next();
						
						if (key == ProductState.KEY_productPriority
							||
							key == ProductState.KEY_actionSequence)
						{
							mixToVisualState(combinedVisualValues.get(key), productId, priority);
						}
						else
						{
							mixToVisualState ( combinedVisualValues.get(key),
								productId, 
								ProductState.getDEFAULT().get(key)
							);
						}
					}
				
					/*
					
					mixToVisualState (actions, productId, actionRequest);
					mixToVisualState (states, productId, installationStatus);
					mixToVisualState (productVersions, productId, productVersion);
					mixToVisualState (packageVersions, productId, packageVersion);
					mixToVisualState(progresses, productId, actionProgress);
					
					mixToVisualState(targets, productId, targetConfiguration);
					mixToVisualState(results, productId, actionResult);
					//logging.debug(this, "results " + results + ", actionResult mixed in: " + actionResult);
					mixToVisualState(lastActions, productId, lastAction);
					mixToVisualState(priorities, productId, priority);
					*/
				}	
			}
		}
	}		
	
		
	protected void mixToVisualState (Map visualStates, String productId, String mixinValue)
	{
		String oldValue = (String) visualStates.get(productId);
		//logging.debug(this, " -------------------  mix to visual states, productId " + productId + " mixinValue " + mixinValue); 
		
		if (oldValue == null)
		//! states.containsKey(productId) 
		{
			visualStates.put(productId,  mixinValue);
		}
		else
		{
			if  ( ! oldValue.equalsIgnoreCase(mixinValue) )
			{
				visualStates.put(productId, CONFLICTstring);
			}
		}
	}
	
	private Boolean preparedColumnIsEditable(int j)
	{
		if (editablePreparedColumns == null || j < 0 || j >= editablePreparedColumns.length)
			return null;
		
		if (Globals.isGlobalReadOnly())
			return false;
		
		return  editablePreparedColumns[j];
	}
	
	// builds list of all prepared column key names (preparedColumns)
	// defines which column might be editable (editablePreparedColumns)
	// builds index of the currently displayed columns in terms of the prepared columns (indexPreparedColumns)
	private void initColumnNames(List<String> columnsToDisplay)
	{
		preparedColumns = new ArrayList<String>();
		editablePreparedColumns = new boolean[16];
		
		
		preparedColumns.add(0, ProductState.KEY_productId);
		editablePreparedColumns[0] = false;
		
		preparedColumns.add(1, ProductState.KEY_productName);
		editablePreparedColumns[1] = false;
		
		preparedColumns.add(2, ProductState.KEY_targetConfiguration);
		editablePreparedColumns[2] = true;
		
		preparedColumns.add(3, ProductState.KEY_installationStatus);
		editablePreparedColumns[3] = true;
		
		preparedColumns.add(4, ProductState.KEY_installationInfo);
		editablePreparedColumns[4] = true; //false;
		
		preparedColumns.add(5, ProductState.KEY_actionProgress);
		editablePreparedColumns[5] = false;
		
		preparedColumns.add(6, ProductState.KEY_actionResult);
		editablePreparedColumns[6] = false;
		
		preparedColumns.add(7, ProductState.KEY_lastAction);
		editablePreparedColumns[7] = false;
		
		preparedColumns.add(8, ProductState.KEY_actionRequest);
		editablePreparedColumns[8] = true;
		
		preparedColumns.add(9, ProductState.KEY_productPriority);
		editablePreparedColumns[9] = false;
		
		preparedColumns.add(10, ProductState.KEY_actionSequence);
		editablePreparedColumns[10] = false;
		
		preparedColumns.add(11, ProductState.KEY_position);
		editablePreparedColumns[11] = false;
		
		preparedColumns.add(12, ProductState.KEY_versionInfo);
		editablePreparedColumns[12] = false;
		
		preparedColumns.add(13, ProductState.KEY_productVersion);
		editablePreparedColumns[13] = false;
		
		preparedColumns.add(14, ProductState.KEY_packageVersion);
		editablePreparedColumns[14] = false;
		
		preparedColumns.add(15, ProductState.KEY_lastStateChange);
		editablePreparedColumns[15] = false;
		
		if (columnsToDisplay == null)
		{
			logging.error(this, "columnsToDisplay are null");
			return;
		}
		
		displayColumns = columnsToDisplay;
		
		logging.info(this, "preparedColumns:  " + preparedColumns);
		logging.info(this, "columnsToDisplay: " + columnsToDisplay);
 		
		indexPreparedColumns = new int[columnsToDisplay.size()];
		columnTitles = new ArrayList<String>();
		{
			Iterator iter = columnsToDisplay.iterator();
			int j = 0;
			while (iter.hasNext())
			{
				String column = (String) iter.next();
				logging.debug(this, " ------- treat column " + column);
				int k = preparedColumns.indexOf(column);
				if (k >= 0)
				{
					indexPreparedColumns[j] = k;
					logging.debug(this, "indexPreparedColumns of displayColumn " + j + " is " + k);
					columnTitles.add(getColumnTitle(column));
				}
				else
				{
					logging.info(this, "column " + column + " is not prepared");
					columnTitles.add(column);
				}
					
				j++;
			}
		}
		numberOfColumns = displayColumns.size() ;
		logging.info(this, " -------- numberOfColumns " + numberOfColumns);
	
		
	}
			
	
	public int getColumnIndex(String columnName)
	{
		return displayColumns.indexOf(columnName);
	}
	
	
	private void initChangedStates()
	{
		for (int i = 0; i < selectedClients.length; i++)
		{
			Map<String, Map<String, String>> changedStates = new HashMap<String, Map<String, String>>();
			collectChangedStates.put (selectedClients[i], changedStates);
		}
	}
	
	public void clearCollectChangedStates()
	{
		collectChangedStates.clear();
		changeActionIsSet = false;
		//initChangedStates();
	}
	
	protected void setInstallationInfo(String product, String value)
	{
		combinedVisualValues.get(ProductState.KEY_installationInfo).put (product, value);
	
		for (int i = 0; i < selectedClients.length; i ++)
		{
			 setInstallationInfo(selectedClients[i], product, value);
		}
	}
	
	private void setInstallationInfo(String clientId,  String product, String value)
	{
		logging.debug(this, "setInstallationInfo for product, client, value " + product + ", " + clientId + ", " + value);
		
		Map<String,  Map<String, String>> changedStatesForClient = (Map<String, Map<String, String>>) ( collectChangedStates.get (clientId) );
		if  (changedStatesForClient == null)
		{
			changedStatesForClient = new HashMap<String, Map<String, String>> ();
			collectChangedStates.put(clientId, changedStatesForClient);
		}
		
		Map<String, String> changedStatesForProduct = (Map <String, String>) changedStatesForClient.get(product);
		if (changedStatesForProduct == null)
		{
			changedStatesForProduct = new HashMap<String, String>();
			changedStatesForClient.put(product,  changedStatesForProduct);
		}
		
		//reverse from putting together the values in ProductState
		
		if (
			value.equals(InstallationInfo.NONEstring ) //we set this in the calling method
			|| value.equals(InstallationInfo.NONEdisplayString ) //this is asked only for formal independence of the method
			)
		{
			changedStatesForProduct.put (ProductState.KEY_lastAction, LastAction.getLabel(ActionResult.NONE));
			changedStatesForProduct.put (ProductState.KEY_actionResult, LastAction.getLabel(ActionResult.NONE));
			changedStatesForProduct.put (ProductState.KEY_actionProgress, InstallationInfo.NONEstring);
			
		}
		else if (value.equals(InstallationInfo.FAILEDdisplayString) )
		{
			changedStatesForProduct.put (ProductState.KEY_lastAction, LastAction.getLabel(ActionResult.NONE));
			changedStatesForProduct.put (ProductState.KEY_actionResult, ActionResult.getLabel(ActionResult.FAILED ) );
			changedStatesForProduct.put (ProductState.KEY_actionProgress, InstallationInfo.MANUALLY);
		}
		else if (value.equals(InstallationInfo.SUCCESSdisplayString) )
		{
			changedStatesForProduct.put (ProductState.KEY_lastAction, LastAction.getLabel(ActionResult.NONE));
			changedStatesForProduct.put (ProductState.KEY_actionResult, ActionResult.getLabel(ActionResult.SUCCESSFUL ) );
			changedStatesForProduct.put (ProductState.KEY_actionProgress, InstallationInfo.MANUALLY);
		}
		else
		{
			changedStatesForProduct.put (ProductState.KEY_lastAction,  ActionResult.getLabel(ActionResult.NONE) );
			changedStatesForProduct.put (ProductState.KEY_actionResult, LastAction.getLabel(ActionResult.NONE));
			changedStatesForProduct.put (ProductState.KEY_actionProgress, value);
		}
		
	}
	
	private void setChangedState(String clientId,  String product,  String stateType, String state)
	{
		Map<String,  Map<String, String>> changedStatesForClient = (Map<String, Map<String, String>>) ( collectChangedStates.get (clientId) );
		if  (changedStatesForClient == null)
		{
			changedStatesForClient = new HashMap<String, Map<String, String>> ();
			collectChangedStates.put(clientId, changedStatesForClient);
		}
		
		Map<String, String> changedStatesForProduct = (Map <String, String>) changedStatesForClient.get(product);
		if (changedStatesForProduct == null)
		{
			changedStatesForProduct = new HashMap<String, String>();
			changedStatesForClient.put(product,  changedStatesForProduct);
		}
	
		//logging.debug(this, "setChangedState client " + clientId + ", product " + product + ", stateType " + stateType + ", state " + state);
		changedStatesForProduct.put (stateType, state);
	}
	
	
	
	private String getChangedState (String clientId,  String product,  String stateType )
	{
		Map<String,  Map<String, String>> changedStatesForClient = collectChangedStates.get (clientId) ;
		if  (changedStatesForClient == null)
			return null;
		
		Map<String, String> changedStatesForProduct = (Map <String, String>) changedStatesForClient.get(product);
		if (changedStatesForProduct == null)
			return null;
		
		return changedStatesForProduct.get (stateType);
	}
	
	
	
	protected void registerStateChange (String product, String stateType, String value)
	{
		for (int i = 0; i < selectedClients.length; i ++)
		{
			setChangedState(selectedClients[i], product, stateType, value);
		}
	}
	
	
	protected void changeActionRequest (String product, String theActionRequestString)
	{
		//logging.debug(this, "changeActionRequest called");
		ActionRequest actionInTreatment = ActionRequest.produceFromDisplayLabel(theActionRequestString);
		
		changeActionIsSet = true;
		
		
		product2setOfClientsWithNewAction = new HashMap<String, Set<String>>(); //for each product, we shall collect the clients that have a changed action request
		productActions = new HashMap<String, ActionRequest>(); //for each product, we remember the visual action that is set 
		//changedStatesForClient.get(product);
		// by recursion,  we find all new settings
		for (int i = 0; i < selectedClients.length; i ++)
		{
			/*
			new ActionRequestTransmitter(selectedClients[i]
			tsProductNames,
			product2setOfClientsWithNewAction, 
			productActions
			
			*/
			recursiveChangeActionRequest (selectedClients[i], product, actionInTreatment);
		}
	
	
		// show the new settings
		for (String productId : product2setOfClientsWithNewAction.keySet())
		{
			if (      ( product2setOfClientsWithNewAction.get(productId) ).size() <    selectedClients.length    )
			// not each client got a new action for this product
			{
				//mixToVisualActions(actions, productId, actionInTreatment.toString());
				mixToVisualState(combinedVisualValues.get(ProductState.KEY_actionRequest), productId, (productActions.get(productId)).toString());
			}
			else
			{
				combinedVisualValues.get(ProductState.KEY_actionRequest).put (productId, (productActions.get(productId)).toString() );
			}
		}	
	
	}

	
	protected void  recursiveChangeActionRequest ( String clientId,  String product,  ActionRequest ar )
	{
		setChangedState(clientId, product, ActionRequest.KEY, ar.toString()); 
		
		Set<String> aSetOfClients = product2setOfClientsWithNewAction.get(product);
		
		if ( aSetOfClients  == null )
		{
			  aSetOfClients = new HashSet<String>();
			  product2setOfClientsWithNewAction.put(product, aSetOfClients);
		}
		
		aSetOfClients.add(clientId);
		
		productActions.put (product, ar);
		
		//actions.put (product, actionInTreatment.toString());  // if necessary change of visible action in combined actions
		
		//logging.debug(this, "productsV.indexOf must be calculated in a different way in subclass " + productsV.indexOf(actualProduct));
		//int modelRow = getRowFromID(actualProduct);
		int modelRow = getRowFromID(product);
		logging.debug(this, "recursiveChangeActionRequest product " + product + " modelRow " + modelRow);
		if (modelRow > -1) 
		{
			logging.info(this, "recursiveChangeActionRequest fire update  " + modelRow);
			//fireTableCellUpdated(modelRow, displayColumns.indexOf(ActionRequest.KEY));  // tell the table model listeners where a change occurred
			fireTableRowsUpdated(modelRow, modelRow);
			//displayColumns.indexOf(ActionRequest.KEY));  // tell the table model listeners where a change occurred
		}
		
		logging.debug(this, " change action request for client " + clientId + ",  product " + product + " to " + 
		  ar.toString());
		
		if (ar.getVal() == ActionRequest.UNINSTALL)
			followRequirements(clientId, persist.getProductDeinstallRequirements( null, product));
		
		else
		{
			followRequirements(clientId, persist.getProductPreRequirements( null, product));
			followRequirements(clientId, persist.getProductRequirements( null, product));
			followRequirements(clientId, persist.getProductPostRequirements( null, product ));
		}
		
		
	}
	
	

	private void followRequirements  ( String clientId, Map<String, String> requirements)
   	{
		String requirement;
		String requiredAction;
		String requiredState;
		
		logging.debug(this, "-- followRequirements for client " + clientId );
		
		for (String requiredProduct : requirements.keySet())
		{
			logging.debug(this, "requiredProduct: " + requiredProduct); 
			requirement = requirements.get(requiredProduct);
			requiredAction = ActionRequest.getLabel(ActionRequest.NONE);
			requiredState = InstallationStatus.getLabel(InstallationStatus.UNDEFINED);
			
			int colonpos = requirement.indexOf(":");
			if (colonpos >= 0)
			{
				requiredState = requirement.substring(0,colonpos);
				requiredAction = requirement.substring(colonpos+1);
			}
			
			logging.debug(this, "followRequirements, required product: " +requiredProduct);
			logging.debug(this, "followRequirements, required action: " +requiredAction);
			logging.debug(this, "followRequirements, required state: " +requiredState);
			
			if (!tsProductNames .contains (requiredProduct))
			{
				logging.warning("followRequirements: required product: '" +requiredProduct+ "' not installable");
			}
			else if ( getChangedState( clientId, requiredProduct, ActionRequest.KEY  ) != null  ) 
			{
				logging.debug(this, "required product: '" +requiredProduct+ "'  has already been treated - stop recursion ");
			}

			else
			//do something
			{
				//logging.debug(this, "---- requiredProduct " + requiredProduct + ", client "  +  clientId);
				// retrieving the actual state and actionRequest of the required product 
				Map productStates = (Map) allClientsProductStates.get(clientId);
				if (productStates != null)
				{
					Map stateAndAction = (Map) productStates.get(requiredProduct);
					logging.debug(this, "---- stateAndAction " + stateAndAction);
					
					if (stateAndAction == null)
						stateAndAction = new ProductState(null);
					
					if (stateAndAction != null)
					{
						String actionRequestForRequiredProduct = (String) stateAndAction.get(ActionRequest.KEY);
						
						logging.debug(this,"---- stateAndAction: ActionRequest for requiredProduct " + actionRequestForRequiredProduct);
						
						String installationStatusOfRequiredProduct = (String) stateAndAction.get(InstallationStatus.KEY);
						
						logging.debug(this,"---- stateAndAction: InstallationStatus for requiredProduct " + installationStatusOfRequiredProduct);
						
						logging.debug(this,"requiredAction " + requiredAction);
						logging.debug(this,"ActionRequest.getVal(requiredAction) "  + ActionRequest.getVal(requiredAction));
						int requiredAR = ActionRequest.getVal(requiredAction);
						
						int requiredIS = InstallationStatus.getVal(requiredState);
						
						logging.debug(this, " requiredInstallationsStatus " + InstallationStatus.getDisplayLabel(requiredIS));
						// handle state requests
						if (   (requiredIS == InstallationStatus.INSTALLED || requiredIS == InstallationStatus.NOT_INSTALLED )
							// the only relevant states for which we should eventually do something 
							&& InstallationStatus.getVal (installationStatusOfRequiredProduct) != requiredIS
							)
						// we overwrite the required action request
						{
							
							String requiredStatusS = InstallationStatus.getLabel(requiredIS); 
							//logging.debug(this," requiredStatusS " + requiredStatusS);
							String neededAction = de.uib.opsidatamodel.productstate.Config.getInstance().requiredActionForStatus.get(requiredStatusS);
							//logging.debug(this," action therefore " + neededAction);
							
							requiredAR  //= ActionRequest.leadingTo(requiredIS);
							= 
							ActionRequest.getVal( neededAction );
						}
						
						//logging.debug(this,"resulting requiredAction " + ActionRequest.getLabel(requiredAR));
			
						// handle resulting action requests
						if (
							requiredAR > ActionRequest.NONE
						)
						/*
						  requiredAR == ActionRequest.SETUP
						  || requiredAR == ActionRequest.ALWAYS
						  || requiredAR == ActionRequest.ONCE
						  || requiredAR == ActionRequest.CUSTOM
						  || requiredAR == ActionRequest.UNINSTALL
						  )
						 */
						{
							if (    
									//an action is required and already set
									ActionRequest.getVal(actionRequestForRequiredProduct) == requiredAR
								)
							{
								logging.debug(this, "followRequirements:   no change of action request necessary for " + requiredProduct);
							}
							else
							{
								logging.info (this, "followRequirements:   ===== recursion into " + requiredProduct);
								recursiveChangeActionRequest(clientId, requiredProduct, new ActionRequest(requiredAR));
							}
						}
					
					}
				}
			}
		}
	}    
	
	public int getRowFromID(String id)
	{
		return productsV.indexOf(id);
	}
	
	public  Map<String, Map<String, Object>> getGlobalProductInfos()
	{
		return globalProductInfos;
	}
	
	//  interface ComboBoxModeller       
	public  ComboBoxModel getComboBoxModel (int row,  int column)
	{
		actualProduct = (String) productsV.get(row); //products[row];
		//logging.debug(this,  "getComboBoxModel(), actualproduct " + actualProduct + " row " + row + " col " + column);
		
		if (column == displayColumns.indexOf(ActionRequest.KEY) )  // selection of actions
		{    
			logging.debug(this,   " possible actions  " + possibleActions);
			Object[] actionsForProduct = null;
			if (possibleActions  != null)
			{
				ArrayList actionList = new ArrayList ();
				
				
				
				//actionList.addALL  (List) possibleActions.get(actualProduct)
				// instead of this we take the display strings:
				
				/*
				if (possibleActions.get(actualProduct) == null) 
				{
					actionList.add( ActionRequest.getDisplayLabel( ActionRequest.NOT_AVAILABLE ) );
				}
				else
				*/
				{
				
					Iterator iter = ((List) possibleActions.get(actualProduct)) .iterator(); // we shall iterate throught all possible actionRequest ID strings for the actual product 
					
					/*
					if (!iter.hasNext())
						actionList.add( ActionRequest.getDisplayLabel( ActionRequest.NOT_AVAILABLE ));
					else
					*/
					{
					
			
						while (iter.hasNext())
						{
							String label = (String) iter.next();
							ActionRequest ar = ActionRequest.produceFromLabel( label );
							actionList.add (ActionRequest.getDisplayLabel( ar.getVal() ) ) ;
						}
						
						//actionList.add(ActionRequest.UNDEFINEDstring);
						// add UNDEFINED string only to local copy but we dont want to set anything to UNDEFINED
					}
				}
				
				actionsForProduct =    actionList.toArray();
				
				logging.debugOut(logging.LEVEL_DONT_SHOW_IT,   " possible actions as array  " + actionsForProduct);
			}
			
			if  (actionsForProduct == null )
				actionsForProduct = new String[]{"null"};
			
			return  new DefaultComboBoxModel(  actionsForProduct );
		}
		
		else if (column == displayColumns.indexOf(InstallationStatus.KEY) ) //selection of status
		{
			//logging.debug(this,"return InstallationStatus.allStates " + InstallationStatus.allStates);
			if (possibleActions.get(actualProduct) == null)
			// we dont have the product in our depot selection
			{
				String state = (String) combinedVisualValues.get(ProductState.KEY_installationStatus).get(actualProduct);
				if (state == null)
					return new DefaultComboBoxModel( new String[]{"null"} );
				
				return new DefaultComboBoxModel (new String[]{});
			}
				
			return new DefaultComboBoxModel ( InstallationStatus.getDisplayLabelsForChoice() );
		}
		
		else if (column == displayColumns.indexOf(TargetConfiguration.KEY) ) //selection of status
		{
			//logging.debug(this,"return InstallationStatus.allStates " + InstallationStatus.allStates);
			if (possibleActions.get(actualProduct) == null)
			// we dont have the product in our depot selection
			{
				String state = (String) combinedVisualValues.get(ProductState.KEY_installationStatus).get(actualProduct);
				if (state == null)
					return new DefaultComboBoxModel( new String[]{"null"} );
				
				return new DefaultComboBoxModel (new String[]{});
			}
				
			return new DefaultComboBoxModel ( TargetConfiguration.getDisplayLabelsForChoice() );
		}
		
		else if (column == displayColumns.indexOf(ProductState.KEY_installationInfo ))
		{
			String delivered = (String) getValueAt(row, column);
			if (delivered == null) 
				delivered = "";
			
			LinkedHashSet<String> values = new LinkedHashSet<String>();

			if (!InstallationInfo.defaultDisplayValues.contains(delivered)) 
				values.add(delivered);
			
			values.addAll(InstallationInfo.defaultDisplayValues);
			
			/*
			if (!delivered.equals("")) 
				values.add(InstallationInfo.NONEdisplayString);
			
			if (!delivered.startsWith(ActionResult.getLabel(ActionResult.SUCCESSFUL ) ) )
				values.add(InstallationInfo.SUCCESSdisplayString);
			
			if (!delivered.startsWith(ActionResult.getLabel(ActionResult.FAILED ) ) )
				values.add(InstallationInfo.FAILEDdisplayString);
			*/
			
			return new DefaultComboBoxModel(new Vector(values));
			
		}
		
		return null;
		
		
		
		
	}
	
	
	// table model
	
	public int getColumnCount()
	{
		return numberOfColumns; // 3;
	}
	
	public int getRowCount()
	{
		return productsV.size();
	}
	
	public String getColumnName (int col)
	{
		return columnTitles.get(col);
		/*
		switch (col)
		{
			case 0 : result = " "; break;
			case 1 : result = configed.getResourceValue("InstallationStateTableModel.installationStatus"); break;
			case 2 : result = configed.getResourceValue("InstallationStateTableModel.productActionProgress"); break;
			case 3 : result = configed.getResourceValue("InstallationStateTableModel.actionRequest"); break;
			case 4 : result = configed.getResourceValue("InstallationStateTableModel.productVersion"); break;
			case 5 : result = configed.getResourceValue("InstallationStateTableModel.packageVersion"); break;
		};
		
		return result;
		*/
	}
	
	public String getLastStateChange(int row)
	{
		String actualProduct =  (String) productsV.get(row);
		//logging.debug(this, "combinedVisualValues.get(ProductState.KEY_lastStateChange) " + combinedVisualValues.get(ProductState.KEY_lastStateChange));
		//logging.debug(this, " actualProduct, ..get(actualProduct) " + actualProduct + ", " + combinedVisualValues.get(ProductState.KEY_lastStateChange).get(actualProduct)); 
		return combinedVisualValues.get(ProductState.KEY_lastStateChange).get(actualProduct);
	}
	
	//this method may be overwritten e.g.for row filtering but retrieveValue continues to work  
	public Object getValueAt(int row, int displayCol)
	{
		return retrieveValueAt(row, displayCol);
	}
	
	private Object retrieveValueAt(int row, int displayCol)
	{
		//logging.debug (this, "retrieveValueAt, displayCol " + displayCol  + " value " +
		//	combinedVisualValues.get(ProductState.KEY_lastStateChange).get(actualProduct));
		
		//if (productsV == null || productsV.size() == 0) return "";
		
		Object result = null;
		actualProduct =  (String) productsV.get(row);  //  products[row];
		
		/*
		boolean productExistsForClient =  (states.get(actualProduct) != null);
		if  (!productExistsForClient) 
		 	return NOT_AVAILABLEstring;
		*/
		
		
		if (displayCol >= indexPreparedColumns.length)
			return "";
		
		int col = indexPreparedColumns[displayCol];
		
		//logging.debug (this, "retrieveValueAt, displayCol " + displayCol + " --------- corresponding preparedCol " + col );
		
		switch(col)
		{
			case 0 : 
				result = actualProduct; 
				break;
				
			case 1 : 
				result =  globalProductInfos.get(actualProduct).get(ProductState.KEY_productName);
				//combinedVisualValues.get(ProductState.KEY_productName).get(actualProduct);
				//there we have not got the value
				break;
				
			case 2:
				result = combinedVisualValues.get(ProductState.KEY_targetConfiguration).get(actualProduct);
				break;
				
			case 3 :
				//result = "" + states.get(actualProduct);
				InstallationStatus is = InstallationStatus.produceFromLabel ( 
					combinedVisualValues.get(ProductState.KEY_installationStatus).get(actualProduct) 
					);
				result =  InstallationStatus.getDisplayLabel( is.getVal() ) ;
				break;
				
			case 4:
				result = combinedVisualValues.get(ProductState.KEY_installationInfo).get(actualProduct);
				break;
				
			case 5 :  
				result = combinedVisualValues.get(ProductState.KEY_actionProgress).get(actualProduct);
				break;
				
			case 6:
				result = combinedVisualValues.get(ProductState.KEY_actionResult).get(actualProduct);
				break;
				
			case 7:
				result = combinedVisualValues.get(ProductState.KEY_lastAction).get(actualProduct);
				break;
				
			case 8 : 
				//result = (String) actions.get(actualProduct) ;
				ActionRequest ar = ActionRequest.produceFromLabel ( 
					combinedVisualValues.get(ProductState.KEY_actionRequest).get(actualProduct) 
					)
					;
				result =  ActionRequest.getDisplayLabel( ar.getVal() ) ;
				
				//logging.debug(this," --------- row, col " + row + ", " + col + " result " + result);
				break;
				
			case 9:
				result = combinedVisualValues.get(ProductState.KEY_productPriority).get(actualProduct); 
				break;
			
			case 10:
				result = combinedVisualValues.get(ProductState.KEY_actionSequence).get(actualProduct);
				//logging.info(this, " actualProduct " +  actualProduct  + " , actionSequence  " + result); 
				break;
				
			case 11:
				result = productNamesInDeliveryOrder.indexOf(actualProduct); //ProductState.KEY_position
				//logging.info(this, " actualProduct " +  actualProduct  + " , position  " + result); 
				break;
				
			case 12:   
				result = combinedVisualValues.get(ProductState.KEY_versionInfo).get(actualProduct);
				break;
				
			case 13:   
				result = combinedVisualValues.get(ProductState.KEY_productVersion).get(actualProduct);
				break;
				
			case 14:   
				result =  combinedVisualValues.get(ProductState.KEY_packageVersion).get(actualProduct);
				break;
			
			case 15:   
				result = combinedVisualValues.get(ProductState.KEY_lastStateChange).get(actualProduct);
				//logging.debug(this, " -------(lastStateChange)-- row, col " + row + ", " + col + " result " + result);
				break;
				
		}
		
		return result;
	}
	
	
	/*
	* JTable uses this method to determine the default renderer/
	* editor for each cell.  If we didn't implement this method,
	* then the last column would contain text
	*/
	public Class getColumnClass(int c) 
	{
		Object val = retrieveValueAt(0, c);
		if (val == null)
			return null;
		else
			return val.getClass();
	}
	
	/*
	* editable columns
	*/
	public boolean isCellEditable(int row, int col) 
	{
		if (preparedColumnIsEditable(indexPreparedColumns[col]))
		{
			return true;
		}
		
		return false;
	}
	
	/*
	* change method for edited cells
	*/
	public void setValueAt(Object value, int row, int col)
	{
		changeValueAt(value, row, col);
		fireTableCellUpdated(row, col);
	}
	
	protected void changeValueAt(Object value, int row, int col)
	{
		String cl = "nul";
		if (value != null)
			cl = value.getClass().toString();
		
		logging.debug(this,"Setting value at " + row + "," + col
							+ " to " + value
							+ " (an instance of "
							+ cl + ")");
		
		
		//data[row][col] = value;   //this is the trivial version
		actualProduct = (String) productsV.get(row);
		
		if (combinedVisualValues.get(ProductState.KEY_installationStatus).get(actualProduct) == null)  return; // not a product in our depot 
		
		if  (  !  ((String) retrieveValueAt (row, col)).equals ( (String) value )  )   
		{
			if (indexPreparedColumns[col] == preparedColumns.indexOf(InstallationStatus.KEY))
			{
				combinedVisualValues.get(ProductState.KEY_installationStatus).put (actualProduct, (String) value);
				registerStateChange (actualProduct, InstallationStatus.KEY, (String) value);
			}
			
			else if (indexPreparedColumns[col] == preparedColumns.indexOf(TargetConfiguration.KEY))
			{
				combinedVisualValues.get(ProductState.KEY_targetConfiguration).put (actualProduct, (String) value);
				registerStateChange (actualProduct, TargetConfiguration.KEY, (String) value);
			}
			
			else if (indexPreparedColumns[col] == preparedColumns.indexOf(ActionRequest.KEY))
			{    
				// an action has changed
				// change  recursively visible action changes and collect the changes for saving
				
				changeActionRequest (actualProduct, (String) value) ;
			}
			
			else if (indexPreparedColumns[col] == preparedColumns.indexOf(
				ProductState.KEY_installationInfo))
			{
				if (value.equals(InstallationInfo.NONEdisplayString ) )
					value  = InstallationInfo.NONEstring;
		
				setInstallationInfo(actualProduct, (String) value);
			}
				
			main.getGeneralDataChangedKeeper().dataHaveChanged(this);
		}
		
	}

}
