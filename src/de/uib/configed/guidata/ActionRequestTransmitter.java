package de.uib.configed.guidata;

import de.uib.configed.configed;
import de.uib.configed.Globals;
import de.uib.configed.ConfigedMain;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;

import de.uib.opsidatamodel.productstate.*;

public abstract class ActionRequestTransmitter
{
	final String clientId;
	final java.util.Map<String, Set<String>> product2setOfClientsWithNewAction;
	final java.util.Set<String> tsProductNames;
	
	final Map<String, Map<String, String>> product2changedStates; //product2statekey2statevalue
	
	final Map<String, ActionRequest> productActions;
	
	public  ActionRequestTransmitter(
		final String clientId,
		final java.util.Set<String> tsProductNames,
		java.util.Map<String, Set<String>> product2setOfClientsWithNewAction,
		Map<String, Map<String, String>> product2changedStates,
		Map<String, ActionRequest> productActions
	)
	{
		this.clientId = clientId;
		this.tsProductNames = tsProductNames;
		
		//initialize external collections if they did not exist)
		this.product2setOfClientsWithNewAction = product2setOfClientsWithsNewAction;
	
		if (product2changedStates == null)
			product2changedStates = new HashMap<String, Map<String, String>>();
		this.product2changedStates = product2changedStates;
		
		this.productActions = productActions;
		
	}
	
	
	protected void setChangedState(String product, String stateKey, String state)
	{
		Map<String, String> changedStatesForProduct = product2changedState.get(product);
		if (changedStatesForProduct == null)
		{
				changedStatesForProduct = new HashMap<String, String>();
				product2changedState.put(product, changedStatesForProduct);
		}
		changedStatesForProduct.put(stateKey, state);	
		
		Set<String> aSetOfClients = product2setOfClientsWithNewAction.get(product);
		if ( aSetOfClients  == null )
		{
			  aSetOfClients = new HashSet<String>();
			  product2setOfClientsWithNewAction.put(product, aSetOfClients);
		}
		aSetOfClients.add(clientId);
	}
	
	
	
	public void changeActionRequestRecursive(String product, ActionRequest request)
	{
		setChangedState(product, request);
		productActions.put (product, request);
		
		setVisualModel(
		
		
		if (request.get_Val() == ActionRequest.UNINSTALL)
		{
			followRequirements(requirement.get(deinstall))
		}
		else
		{
			followRequirements(requirements.get(pre))
			followRequirements(requirements.get(neutral))
			followRequirements(requirements.get(post))
		}
	}
			
	
	
	
	
	private String getChangedState(String product, String stateKey)
	{
		Map<String, String> changedStatesForProduct = product2changedStates.get(product);
		if (changedStatesForProduct == null)
			return null;
		
		return changedStatesForProduct.get (stateKey);
		
	}
	
	
	
	
	private void followRequirements(Map<String, String> requirements)
	{
		String requirement;
		String requiredAction;
		String requiredState;
		
		logging.debug(this, "-- followRequirements for client " + clientId );
		
		for (String requiredProduct : requirements.keySet())
		{
			logging.debug(this, "requiredProduct: " + requiredProduct); 
			requirement = requirements.get(requiredProduct);
				
			logging.debug(this, "requirement String: " + requirement);
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
			else if ( getChangedState( requiredProduct, ActionRequest.KEY  ) != null  ) 
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
								logging.debug (this, "followRequirements:   recursion into " + requiredProduct);
								recursiveChangeActionRequest(clientId, requiredProduct, new ActionRequest(requiredAR));
							}
						}
					
					}
				}
			}
		}
		
	}
	
	
		
}	
		
			
		
	
	
	
	
	
	
	
