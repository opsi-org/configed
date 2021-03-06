/**
 *
 *  copyright:     Copyright (c) 2014-2016
 *  organization: uib.de
 * @author  R. Roeder 
 */


package de.uib.opsidatamodel;

import java.util.*;
import de.uib.utilities.logging.*;
import de.uib.opsicommand.*;
import de.uib.configed.configed;
import de.uib.configed.type.*;
import de.uib.configed.type.licences.*;
import de.uib.opsidatamodel.productstate.*;
import de.uib.utilities.datastructure.*;
import de.uib.utilities.table.*;


public class DataStubNOM extends DataStub
{

	OpsiserviceNOMPersistenceController controller;
	
	public static Integer classCounter = 0;
	
	public DataStubNOM(OpsiserviceNOMPersistenceController controller)
	{
		this.controller = controller;
		classCounter++;
	}
	

	@Override
	public void productDataRequestRefresh()
	{
		product2versionInfoRequestRefresh();
		productsAllDepotsRequestRefresh();
		productPropertyDefinitionsRequestRefresh();
		productPropertyStatesRequestRefresh();
		productPropertyDepotStatesRequestRefresh();
		productDependenciesRequestRefresh();
	}


			
	
	//===================================================
	
	//netbootStatesAndActions
	//localbootStatesAndActions
	
	
	//===================================================

	@Override
	public boolean test()
	{
		return true;
	}

	//===================================================
	protected Map<String, Map<String, OpsiProductInfo>> product2versionInfo2infos;

	@Override
	public void product2versionInfoRequestRefresh()
	{
		product2versionInfo2infos = null;
	}

	@Override
	public Map<String, Map<String, OpsiProductInfo>> getProduct2versionInfo2infos()
	{
		retrieveProductInfos();
		return  product2versionInfo2infos;
	}


	protected void retrieveProductInfos()
	{
		//logging.info(this, "retrieveProductInfos data == null " + (product2versionInfo2infos == null));

		if (product2versionInfo2infos == null)
		{
			ArrayList<String> attribs = new ArrayList<String>();

			for (String key : OpsiPackage.SERVICE_KEYS)
			{
				attribs.add(key);
			}
			
			/*
			attribs.remove(OpsiPackage.SERVICEkeyPRODUCT_ID);
			attribs.add("id");
			*/

			for (String scriptKey : ActionRequest.getScriptKeys())
			{
				attribs.add(scriptKey);
			}

			
			attribs.add(OpsiProductInfo.SERVICEkeyUSER_LOGIN_SCRIPT);
			attribs.add(OpsiProductInfo.SERVICEkeyPRIORITY);

			attribs.remove(OpsiPackage.SERVICEkeyPRODUCT_TYPE);
			attribs.add(OpsiProductInfo.SERVICEkeyPRODUCT_ADVICE);
			attribs.add(OpsiProductInfo.SERVICEkeyPRODUCT_NAME);
			attribs.add(OpsiProductInfo.SERVICEkeyPRODUCT_DESCRIPTION);

			String[] callAttributes = attribs.toArray(new String[]{});

			logging.debug(this, "retrieveProductInfos callAttributes "
			              + Arrays.asList(callAttributes));

			HashMap callFilter = new HashMap();
			//callFilter.put("id", "acroread*");
			
			
			controller.notifyDataLoadingObservers(configed.getResourceValue("LoadingObserver.loadtable") + " product");

			List<Map<String, Object>>  retrievedList  = controller.retrieveListOfMapsNOM(
			            callAttributes,
			            callFilter,
			            "product_getObjects"
			        );
			
			//logging.info(this,  "retrieveProductInfos retrievedList " + retrievedList);

			product2versionInfo2infos = new HashMap<String, Map<String, OpsiProductInfo>>();

			for (Map<String, Object> m : retrievedList)
			{
				//logging.info(this, "retrieveProductInfos " + m);
				String productId = "" + m.get( OpsiPackage.SERVICEkeyPRODUCT_ID0 );
				String versionInfo = OpsiPackage.produceVersionInfo( 
					"" + m.get( OpsiPackage.SERVICEkeyPRODUCT_VERSION ),
					"" + m.get( OpsiPackage.SERVICEkeyPACKAGE_VERSION ) );

				OpsiProductInfo productInfo = new OpsiProductInfo(m);
				Map<String, OpsiProductInfo> version2productInfos = product2versionInfo2infos.get(productId);

				if (version2productInfos == null)
				{
					version2productInfos  = new HashMap <String, OpsiProductInfo>();
					product2versionInfo2infos.put(productId, version2productInfos);
				}
				version2productInfos.put(versionInfo, productInfo);
				
			
				/*
				logging.info(this,  "retrieveProductInfos product  -  version2productInfos " + 
					productId + "  -  " +
					version2productInfos);
				*/
				
				//System.exit(0);
				
			}
			
			
			logging.debug(this,  "retrieveProductInfos " + product2versionInfo2infos);
			
			
			
			//lambda
			/*
			
			Map<String, List<Map<String, Object>>> pInfos = new HashMap<String, List<Map<String, Object>>>();
			String keyP = "id";
			
			retrievedList.forEach(
				m-> 
					{
						if ( pInfos.get( m.get(keyP ) ) == null )
							pInfos.put( (String) m.get(keyP), new ArrayList<Map<String, Object>>() );
					}
			);
			
			retrievedList.forEach(
				m-> pInfos.get( (String) m.get(keyP)).add( m )
			);
			*/
			
			/*
			retrievedList.forEach(
				m->
					{
						String p = (String) m.get(keyP);
						if  (pInfos.get( p ) == null)
							pInfos.put(p, new ArrayList<Map<String, Object>());
						
						List<Map<String, Object> list = pInfos.get( p );
						list.add((Map<String, Object) m);
					}
				);
				
			*/
			//logging.info(this, "lambda expression produced pInfos " + pInfos);
			//System.exit(0);
			

			controller.notifyDataRefreshedObservers("product");
		}
		

	}


	//===================================================

	protected Object2Product2VersionList depot2LocalbootProducts;
	protected Object2Product2VersionList depot2NetbootProducts;
	protected Vector<Vector<Object>> productRows;
	protected Map<String, TreeSet<OpsiPackage>> depot2Packages;
	protected Map<String, Map<String, java.util.List<String>>> product2VersionInfo2Depots;

	@Override
	public void productsAllDepotsRequestRefresh()
	{
		depot2LocalbootProducts = null;
	}
	
	@Override
	public   Map<String, TreeSet<OpsiPackage>> getDepot2Packages()
	{
		retrieveProductsAllDepots();
		return depot2Packages;
	}
	
	@Override
	public  Vector<Vector<Object>> getProductRows()
	{
		retrieveProductsAllDepots();
		return productRows;
	}
	
	@Override
	public Map<String, Map<String, java.util.List<String>>> getProduct2VersionInfo2Depots()
	{
		retrieveProductsAllDepots();
		return product2VersionInfo2Depots;
	}


	@Override
	public Object2Product2VersionList getDepot2LocalbootProducts()
	{
		retrieveProductsAllDepots();
		return depot2LocalbootProducts;
	}

	@Override
	public Object2Product2VersionList getDepot2NetbootProducts()
	{
		retrieveProductsAllDepots();
		return depot2NetbootProducts;
	}


	protected void retrieveProductsAllDepots()
	{
		
		logging.debug(this, "retrieveProductsAllDepots ? " +
		              "depot2LocalbootProducts " + depot2LocalbootProducts + "\n" +
		              "depot2NetbootProducts " + depot2NetbootProducts
		             );
		retrieveProductInfos();

		if (
		    	depot2NetbootProducts == null 
			|| depot2LocalbootProducts == null 
			|| productRows == null
			|| depot2Packages == null
		)
		{
			
			logging.info(this, "retrieveProductsAllDepots, reload");
			logging.info(this, "retrieveProductsAllDepots, reload depot2NetbootProducts == null " + (depot2NetbootProducts == null));
			logging.info(this, "retrieveProductsAllDepots, reload depot2LocalbootProducts == null " + (depot2LocalbootProducts == null));
			logging.info(this, "retrieveProductsAllDepots, reload productRows == null " + (productRows == null));
			logging.info(this, "retrieveProductsAllDepots, reload depot2Packages == null " + (depot2Packages == null));
			controller.notifyDataLoadingObservers(configed.getResourceValue("LoadingObserver.loadtable") + " productOnDepot");
			String[] callAttributes = new String[]{};
			HashMap callFilter = new HashMap();

			List<Map<String, Object>> packages = controller.retrieveListOfMapsNOM(
			                                         callAttributes,
			                                         callFilter,
			                                         "productOnDepot_getObjects"
			                                     );

			depot2LocalbootProducts = new Object2Product2VersionList();
			depot2NetbootProducts = new Object2Product2VersionList();
			product2VersionInfo2Depots = new HashMap<String, Map<String, java.util.List<String>>>();
			
			productRows = new Vector<Vector<Object>>();
			
			depot2Packages = new HashMap<String, TreeSet<OpsiPackage>>();
			
		
			for (Map<String, Object> m : packages)
			{
				String depot = "" + m.get("depotId");
				
				if (!controller.getDepotPermission(depot))
					continue;

				OpsiPackage p = new OpsiPackage(m);
				
				logging.debug(this, "retrieveProductsAllDepots, opsi package " + p); 

				if (p.isNetbootProduct())
				{
					depot2NetbootProducts.addPackage(depot, p.getProductId(), p.getVersionInfo());
				}

				else if (p.isLocalbootProduct())
				{
					depot2LocalbootProducts.addPackage(depot, p.getProductId(), p.getVersionInfo());
				}

				Map<String, java.util.List<String>> versionInfo2Depots = product2VersionInfo2Depots.get(p.getProductId());
				if ( versionInfo2Depots == null )
				{
					versionInfo2Depots = new HashMap<String, java.util.List<String>>();
					product2VersionInfo2Depots.put(p.getProductId(), versionInfo2Depots);
				}

				java.util.List depotsWithThisVersion = versionInfo2Depots.get(p.getVersionInfo());

				if (depotsWithThisVersion == null)
				{
					depotsWithThisVersion = new ArrayList<String>();
					versionInfo2Depots.put(p.getVersionInfo(), depotsWithThisVersion);
				}
				depotsWithThisVersion.add(depot);
				
				
				TreeSet<OpsiPackage> depotpackages = depot2Packages.get(depot);
				if (depotpackages == null)
				{
					depotpackages = new TreeSet<OpsiPackage>();
					depot2Packages.put(depot, depotpackages);
				}
				depotpackages.add(p);
				
				
				Vector<Object> productRow = new Vector<Object>();
				
				productRow.add(p.getProductId());
				
				String productName = null;
				
				try
				{
					productName = product2versionInfo2infos.get(p.getProductId()).get(p.getVersionInfo()).getProductName(); 
					productRow.add( productName );
					
					p.appendValues(productRow);
				
					//logging.info(this, "retrieveProductsAllDepots package " + p + " name  " + productName;
					//logging.info(this, "retrieveProductsAllDepots productRow " + productRow);
				
					if (depotsWithThisVersion.size() == 1)
						productRows.add(productRow);
				}
				catch(Exception ex)
				{
					logging.warning("retrieveProductsAllDepots exception " +  ex);
					logging.warning("retrieveProductsAllDepots exception for package  " +  p);
					logging.warning("retrieveProductsAllDepots exception productId  " +  p.getProductId());
					
					logging.warning("retrieveProductsAllDepots exception for product2versionInfo2infos: " + product2versionInfo2infos); 
					logging.warning("retrieveProductsAllDepots exception for product2versionInfo2infos.get(p.getProductId()) " 
						+ product2versionInfo2infos.get(p.getProductId()));
					if ( product2versionInfo2infos.get(p.getProductId()) == null )
					{
						logging.warning("retrieveProductsAllDepots : product " + p.getProductId() 
							+ " seems not to exist in product table");
					}
						
					
				}
				
				
				//System.exit(0);
				
				
			}

			

			/*
			logging.debug(this, "retrieveDepotProducts localBoot | netBoot " 
				+ "\n"+ depot2LocalbootProducts 
				+ "\n"+ depot2NetbootProducts
				);
			*/

			//logging.info(this, "retrieveProductsAllDepots  product2VersionInfo2Depots " + product2VersionInfo2Depots);
			//System.exit(0);

			controller.notifyDataRefreshedObservers("productOnDepot");
		}
		
			
		logging.debug(this, "getRowsOfProducts " + productRows);
		//System.exit(0);
		
	}


	//===================================================

	protected Map<String, Map<String, Map<String, ListCellOptions>>> // depotId-->productId --> (propertyId --> value)
	depot2Product2PropertyDefinitions;

	@Override
	public void productPropertyDefinitionsRequestRefresh()
	{
		depot2Product2PropertyDefinitions = null;
	}

	@Override
	public Map<String, Map<String, Map<String, ListCellOptions>>>
	getDepot2Product2PropertyDefinitions()
	{
		retrieveAllProductPropertyDefinitions();
		return depot2Product2PropertyDefinitions;
	}


	protected void retrieveAllProductPropertyDefinitions()
	{
		retrieveProductsAllDepots();

		if (depot2Product2PropertyDefinitions == null)
		{
			depot2Product2PropertyDefinitions = new HashMap<String, Map<String, Map<String, ListCellOptions>>>();

			//HashMap<String, java.util.Set<String>> productListForProductID = new HashMap<String, java.util.Set<String>>();
			//HashMap<String, java.util.Set<String>> productListForProductID_notUnique = new HashMap<String, java.util.Set<String>>();

			controller.notifyDataLoadingObservers(configed.getResourceValue("LoadingObserver.loadtable") + " product property");
			
			String[] callAttributes = new String[]{};
			HashMap callFilter = new HashMap();

			List<Map<String, Object>>  retrieved = controller.retrieveListOfMapsNOM(
			                                           callAttributes,
			                                           callFilter,
			                                           "productProperty_getObjects"
			                                       );

			//logging.debug(this, "retrieved: " + retrieved);

			Iterator iter = retrieved.iterator();

			while (iter.hasNext())
			{

				Map<String, Object> retrievedMap = (Map) iter.next();
				Map<String, Object> adaptedMap = new HashMap<String, Object>(retrievedMap);
				//rebuild JSON objects
				Iterator iterInner = retrievedMap.keySet().iterator();
				while (iterInner.hasNext())
				{
					String key = (String) iterInner.next();
					adaptedMap.put(key, JSONReMapper.deriveStandard(retrievedMap.get(key)));
				}

				ConfigOption productPropertyMap = new ConfigOption(adaptedMap);

				String propertyId = (String) retrievedMap.get("propertyId");
				String productId = (String) retrievedMap.get("productId");

				//logging.debug(this, "############ product " + productId + "  property " + propertyId  + "  , retrieved map " + retrievedMap);
				//logging.debug(this, "############ product " + productId + "  property " + propertyId  + "  , property map " + productPropertyMap);

				String productVersion = (String) retrievedMap.get( OpsiPackage.SERVICEkeyPRODUCT_VERSION );
				String packageVersion = (String) retrievedMap.get( OpsiPackage.SERVICEkeyPACKAGE_VERSION );
				String versionInfo = productVersion + de.uib.configed.Globals.ProductPackageVersionSeparator.forKey() + packageVersion;


				if
				(
				    product2VersionInfo2Depots.get(productId) == null
				    ||
				    product2VersionInfo2Depots.get(productId).get(versionInfo) == null
				)
				{
					logging.debug(this, "retrieveAllProductPropertyDefinitions: no depot for " + productId + " version " + versionInfo +
					             "  product2VersionInfo2Depots.get(productId) " +    product2VersionInfo2Depots.get(productId) );
					       
				}
				else
				{
					for (String depot : product2VersionInfo2Depots.get(productId).get(versionInfo))
					{

						Map<String, Map<String, ListCellOptions>> product2PropertyDefinitions = depot2Product2PropertyDefinitions.get(depot);
						if (product2PropertyDefinitions == null)
						{
							product2PropertyDefinitions = new HashMap<String, Map<String, ListCellOptions>>();
							depot2Product2PropertyDefinitions.put(depot, product2PropertyDefinitions);
						}

						Map<String, ListCellOptions> propertyDefinitions = product2PropertyDefinitions.get(productId);

						if (propertyDefinitions == null)
						{
							propertyDefinitions  = new HashMap<String, ListCellOptions>();
							product2PropertyDefinitions.put(productId, propertyDefinitions);
						}

						propertyDefinitions.put(propertyId, (ListCellOptions)productPropertyMap);

					}
				}

			}

			logging.debug(this, "retrieveAllProductPropertyDefinitions " );
			//+ depot2Product2PropertyDefinitions);
			controller.notifyDataRefreshedObservers("productProperty");

		}

	}

	//===================================================

	protected Map<String, Map<String, java.util.List<Map<String, String>>>> // depotId-->productId --> (dependencyKey--> value)
	depot2product2dependencyInfos;

	@Override
	public void productDependenciesRequestRefresh()
	{
		depot2product2dependencyInfos = null;
	}

	@Override
	public Map<String, Map<String, java.util.List<Map<String, String>>>>
	getDepot2product2dependencyInfos()
	{
		retrieveAllProductDependencies();
		return depot2product2dependencyInfos;
	}


	protected void retrieveAllProductDependencies()
	{
		retrieveProductsAllDepots();

		if (depot2product2dependencyInfos == null)
		{
			depot2product2dependencyInfos = new HashMap<String, Map<String, java.util.List<Map<String, String>>>>();
			
			controller.notifyDataLoadingObservers(configed.getResourceValue("LoadingObserver.loadtable") + " product dependency"); 
				
			String[] callAttributes = new String[]{};
			HashMap callFilter = new HashMap();

			java.util.List<Map<String, Object>>  retrievedList = controller.retrieveListOfMapsNOM(
			            callAttributes,
			            callFilter,
			            "productDependency_getObjects"
			        );

			for (Map<String, Object> dependencyItem : retrievedList)
			{
				String productId = "" + dependencyItem.get( OpsiPackage.DBkeyPRODUCT_ID );

				String productVersion = "" + dependencyItem.get( OpsiPackage.SERVICEkeyPRODUCT_VERSION );
				String packageVersion = "" + dependencyItem.get( OpsiPackage.SERVICEkeyPACKAGE_VERSION );
				String versionInfo = productVersion + de.uib.configed.Globals.ProductPackageVersionSeparator.forKey() + packageVersion;

				String action = "" + dependencyItem.get("productAction");
				String requirementType = "";
				if (dependencyItem.get("requirementType") != null)
					requirementType = "" + dependencyItem.get("requirementType");


				String requiredProductId = "" + dependencyItem.get("requiredProductId");
				String requiredAction = "";
				if (dependencyItem.get("requiredAction") != null)
					requiredAction = "" + dependencyItem.get("requiredAction");
				String requiredInstallationStatus = "";
				if (dependencyItem.get("requiredInstallationStatus") != null)
					requiredInstallationStatus = "" + dependencyItem.get("requiredInstallationStatus");


				if (
					(product2VersionInfo2Depots == null)
					||
					(product2VersionInfo2Depots.get(productId) == null)
					||
					(product2VersionInfo2Depots.get(productId).get(versionInfo)  == null)
				)
				{
					logging.warning(this, "unexpected null for product2VersionInfo2Depots productId, versionInfo   " + productId + ", " + versionInfo); 
					continue;
				}
				for ( String depot : product2VersionInfo2Depots.get(productId).get(versionInfo) )
				{
					Map<String, java.util.List<Map<String, String>>> product2dependencyInfos = depot2product2dependencyInfos.get(depot);
					if (product2dependencyInfos == null)
					{
						product2dependencyInfos = new HashMap<String, java.util.List<Map<String, String>>>();
						depot2product2dependencyInfos.put(depot, product2dependencyInfos);
					}

					java.util.List<Map<String, String>> dependencyInfos = product2dependencyInfos.get(productId);

					if (dependencyInfos == null)
					{
						dependencyInfos  = new ArrayList<Map<String, String>>();
						product2dependencyInfos.put(productId, dependencyInfos);
					}

					Map<String, String> dependencyInfo = new HashMap<String, String>();
					dependencyInfo.put("action", action);
					dependencyInfo.put("requiredProductId", requiredProductId);
					dependencyInfo.put("requiredAction", requiredAction);
					dependencyInfo.put("requiredInstallationStatus", requiredInstallationStatus);
					dependencyInfo.put("requirementType", requirementType);


					//logging.info(this, "add dependencyInfo depot " + depot + "  product " + productId + ", "  + dependencyInfo);
					dependencyInfos.add(dependencyInfo);
					//logging.info(this, "dependencyInfos "+ dependencyInfos);
				}
			}

			//logging.info(this, "retrieveAllProductDependencies  " + depot2product2dependencyInfos );
			controller.notifyDataRefreshedObservers("productDependency");

		}

	}

	//===================================================
	
	protected  java.util.List <Map<String, Object>> productPropertyStates;
	protected  java.util.List <Map<String, Object>> productPropertyDepotStates; //will only be refreshed when all product data are refreshed
	
	//protected Map<String, Map<String, Map<String, Object>>> host2product2properties_retrieved = new HashMap<String, Map<String, Map <String, Object>>>();
	protected java.util.Set<String> hostsWithProductProperties;
	//protected java.util.Set<String> depotsWithProductProperties;
	
	public void productPropertyStatesRequestRefresh()
	{
		logging.info(this, "productPropertyStatesRequestRefresh");
		productPropertyStates = null;
		hostsWithProductProperties = null;
	}
	
	public java.util.List<Map<String, Object>> getProductPropertyStates()
	{
		retrieveProductPropertyStates();
		return productPropertyStates;
	}
	
	protected void productPropertyDepotStatesRequestRefresh()
	{
		logging.info(this, "productPropertyDepotStatesRequestRefresh");
		productPropertyDepotStates = null;
	}
	
	public java.util.List<Map<String, Object>> getProductPropertyDepotStates(java.util.Set<String> depots)
	{
		retrieveProductPropertyDepotStates(depots);
		return productPropertyDepotStates;
	}
	
	
	//public Map<String, Map<String, Map<String, Object>>> getHost2product2properties_retrieved  = new HashMap<String, Map<String, Map <String, Object>>>();
	
	public void fillProductPropertyStates(Collection<String> clients)
	{
		logging.info(this, "fillProductPropertyStates for " + clients);
		if (productPropertyStates == null)
		{
			productPropertyStates = produceProductPropertyStates(clients, hostsWithProductProperties);
		}
		else
		{
			productPropertyStates.addAll(produceProductPropertyStates(clients, hostsWithProductProperties));
		}
	}
	
	protected void retrieveProductPropertyStates()
	{
		produceProductPropertyStates( (Collection<String>) null, hostsWithProductProperties);
	}
	
	protected void retrieveProductPropertyDepotStates(java.util.Set<String>depots)
	{
		logging.info(this, "retrieveProductPropertyDepotStates for depots " + depots +  " depotStates == null " + (productPropertyDepotStates == null));
		if (productPropertyDepotStates == null)
		{
			productPropertyDepotStates = produceProductPropertyStates(depots , null);
		}
		
		/*
		if (productPropertyDepotStates == null)
		{
			productPropertyDepotStates = produceProductPropertyStates(depots , null);
			depotsWithProductProperties = depots;
		}
		else
		{
			productPropertyDepotStates = produceProductPropertyStates(depots , depotsWithProductProperties);
		}
		*/	
			
		logging.info(this, "retrieveProductPropertyDepotStates ready  size " + productPropertyDepotStates.size());
	}
	
		
	// client is a set of added hosts, host represents the totality and will be updated as a side effect
	protected java.util.List <Map<String, Object>> produceProductPropertyStates(
			final Collection<String> clients,
			java.util.Set<String> hosts)
	{
		logging.info(this, "produceProductPropertyStates new hosts " + clients  +  " old hosts " + hosts);
		java.util.List<String> newClients = null;
		if (clients == null)
			newClients =  new ArrayList<String>();
		else 
			newClients = new ArrayList<String>(clients);
			
		if (hosts == null)
		{
			hosts = new HashSet<String>();
		}
		else
		{
			newClients.removeAll(hosts);
		}
		
		//logging.info(this, "produceProductPropertyStates, new hosts " + clients);
		
		java.util.List <Map<String, Object>> result = null;
		
		if (newClients.size() == 0)
		{
			//look if propstates is initialized
			result = new ArrayList <Map<String, Object>>();
		}	
		else 
		{
			hosts.addAll(newClients);
			
			//logging.info(this, "produceProductPropertyStates, all hosts " + hosts);
			
			controller.notifyDataLoadingObservers(configed.getResourceValue("LoadingObserver.loadtable") + " product property state");
			String[] callAttributes = new String[]{};//"objectId","productId","propertyId", "values"};
			HashMap callFilter = new HashMap();
			callFilter.put("objectId", controller.exec.jsonArray(newClients));
			
			result = controller.retrieveListOfMapsNOM(
					callAttributes,
					callFilter,
					"productPropertyState_getObjects"
					);
			//logging.info(this, "propstates: " + propstates);
		}
		
		
		
		logging.info(this, "produceProductPropertyStates for hosts " + hosts);
		/*
		for (Map<String, Object> m : result)
		{
			logging.info(this, "produceProductPropertyStates record " + m);
		}
		*/
		
		
		return result;
	}
	
	//===================================================
	protected TreeMap<String, java.util.List<HWAuditClientEntry>> client2hwAuditHostEntries;
	protected TreeMap<String, Map<String, java.util.List<HWAuditClientEntry>>> client2hwType2hwAuditHostEntries;
	
	
	
	//===================================================
	protected TreeMap<String, SWAuditEntry> installedSoftwareInformation;
	protected LinkedList<String> softwareList; //List of idents of software

	@Override
	public void installedSoftwareInformationRequestRefresh()
	{
		installedSoftwareInformation = null;
	}
	
	@Override
	public LinkedList<String> getSoftwareList()
	{
		retrieveInstalledSoftwareInformation();
		return softwareList;
	}
	
	@Override 
	public String getSWident(Integer i)
	{
		retrieveInstalledSoftwareInformation();
		if (softwareList == null || softwareList.size() <  i + 1 || i == -1)
		{
			if (softwareList != null)
				logging.info(this, "getSWident " + " until now softwareList.size() " + softwareList.size());
			
			boolean infoFound = false;
			
			//try reloading?
			int returnedOption =javax.swing.JOptionPane.NO_OPTION;
			returnedOption = javax.swing.JOptionPane.showOptionDialog(	de.uib.configed.Globals.mainFrame,
					                 configed.getResourceValue("DataStub.reloadSoftwareInformation.text"),
					                 configed.getResourceValue("DataStub.reloadSoftwareInformation.title"),
					                 javax.swing.JOptionPane.YES_NO_OPTION,
					                 javax.swing.JOptionPane.QUESTION_MESSAGE,
					                 null, null, null);
			
			if (returnedOption == javax.swing.JOptionPane.YES_OPTION)
			{
				installedSoftwareInformationRequestRefresh();
				retrieveInstalledSoftwareInformation();
				if (softwareList.size() >= i  +1)
					infoFound = true;
				
			}
			
			if (!infoFound)
			{
				logging.warning(this, "missing softwareList entry " + i + " " + softwareList);
				return null;
			}
		}
		return softwareList.get( i );
	}

	@Override
	public TreeMap<String, SWAuditEntry> getInstalledSoftwareInformation()
	{
		retrieveInstalledSoftwareInformation();
		return installedSoftwareInformation;
	}

	protected void retrieveInstalledSoftwareInformation()
	{
		if (installedSoftwareInformation  == null)
		{
			
			controller.notifyDataLoadingObservers(configed.getResourceValue("LoadingObserver.loadtable") + " software");
			
			String[] callAttributes =  new String[]{
					SWAuditEntry.key2serverKey.get(SWAuditEntry.NAME), // "name", //key element
					SWAuditEntry.key2serverKey.get(SWAuditEntry.VERSION), // "version",//key element
					SWAuditEntry.key2serverKey.get(SWAuditEntry.SUBVERSION), //key element
					SWAuditEntry.key2serverKey.get(SWAuditEntry.LANGUAGE), //key element
					SWAuditEntry.key2serverKey.get(SWAuditEntry.ARCHITECTURE), //key element
					SWAuditEntry.key2serverKey.get(SWAuditEntry.WINDOWSsOFTWAREid)
			};
			HashMap callFilter = new HashMap();
			
			List<Map<String, Object>> li =  controller.retrieveListOfMapsNOM(
										 callAttributes,
										 callFilter,
										 "auditSoftware_getHashes"
									 );
			;
			
			Iterator iter = li.iterator();
			
			installedSoftwareInformation = new TreeMap<String, SWAuditEntry>();
			
			//int i = 0;
			
			while (iter.hasNext())
			{
				//i++;
				Map retrievedEntry = (Map) iter.next();
				//logging.info(this, "retrievedEntry " + retrievedEntry);
				SWAuditEntry entry = new SWAuditEntry(retrievedEntry);
				
				installedSoftwareInformation.put(entry.getIdent(), entry);
				
				
				//if (entry.getIdent().indexOf("55375-640") >= 0)
				//	logging.info(this, "retrieveInstalledSoftwareInformation produced " + entry);
				
				//if (i == 4) break;
			}
			
			softwareList = new LinkedList<String>(installedSoftwareInformation.keySet());
			
			logging.info(this, "retrieveInstalledSoftwareInformation produced softwarelist with entries " + softwareList.size());
			
			controller.notifyDataRefreshedObservers("software");
			
		}
	}
				
//===================================================

	protected  java.util.List <Map<String, Object>> softwareAuditOnClients;
	protected  Map<String, java.util.List <SWAuditClientEntry>> client2software;
	protected Map<String, java.util.Set<String>> softwareIdent2clients; 
	//protected Map<Integer, java.util.List<String>> softwareId2clients; 
	
	protected java.sql.Time SOFTWARE_CONFIG_last_entry = null;

	@Override
	public void softwareAuditOnClientsRequestRefresh()
	{
		logging.info(this, "softwareAuditOnClientsRequestRefresh");
		softwareAuditOnClients = null;
		client2software = null;
		softwareIdent2clients = null;
		//softwareId2clients = null;
	}
	
	

	/*
	public  java.util.List <Map<String, Object>> getSoftwareAuditOnClients()
	{
		logging.debug(this, "getSoftwareAuditOnClients");
		retrieveSoftwareAuditOnClients0();
		return softwareAuditOnClients;
	}
	*/
	
	@Override
	public void fillClient2Software(String client)
	{
		logging.info(this, "fillClient2Software " + client);
		if (client2software == null)
		{
			retrieveSoftwareAuditOnClients(client);
			//logging.info(this, "fillClient2Software " + client2software);
			return;
		}
		
		if (client2software.get(client) == null)
			retrieveSoftwareAuditOnClients(client);
		
		//logging.info(this, "fillClient2Software " + client2software);
	}
	
	@Override
	public void fillClient2Software(java.util.List<String> clients)
	{
		logging.info(this, "fillClient2Software " + clients);
		retrieveSoftwareAuditOnClients(clients);
	}
	
	@Override
	public  Map<String, java.util.List<SWAuditClientEntry>> getClient2Software()
	//fill the clientlist by fill ...
	{
		logging.info(this, "getClient2Software  ============= ");
		return client2software;
	}
	
	
	/*
	@Override
	public  Map<Integer, java.util.List<String>> getSoftwareId2clients()
	{
		
		//logging.info(this, "getSoftwareId2clients ============= ");
	
		if (softwareId2clients == null)
			logging.info(this, "getSoftwareId2clients ============= null");
		else
		{
			for (Integer key :   softwareId2clients.keySet())
			{
				logging.info(this, "getSoftwareId2clients ===== key " + key + " " 
					+ softwareId2clients.get(key));
			}
		}
		
		return softwareId2clients;
	}
	*/
	
	
	@Override
	public  Map<String, java.util.Set<String>> getSoftwareIdent2clients()
	//fill the clientlist by fill ...
	{
		for (String ident : softwareIdent2clients.keySet())
		{
			
			//logging.info(this, "getSoftwareIdent2clients = ident == size ===== " + ident + " ===== "
			//+  softwareIdent2clients.get(ident).size() );
		}
		
		return softwareIdent2clients;
	}
	
	
		
	protected void retrieveSoftwareAuditOnClients()
	{
		retrieveSoftwareAuditOnClients(new ArrayList<String>());
	}
		
	protected void retrieveSoftwareAuditOnClients(String client)
	{
		java.util.List<String> clients = new ArrayList<String>();
		clients.add(client);
		retrieveSoftwareAuditOnClients(clients);
	}
	
	protected void retrieveSoftwareAuditOnClients(final java.util.List<String> clients)
	{
		logging.info(this,  "retrieveSoftwareAuditOnClients used memory on start " + de.uib.utilities.Globals.usedMemory());

		retrieveInstalledSoftwareInformation();
		logging.info(this, "retrieveSoftwareAuditOnClients client2Software null " 
			+ (client2software == null) 
			+ "  clients count ======  " + clients.size());
		
		java.util.List<String> newClients = new ArrayList<String>(clients);
		
		if (client2software!= null)
		{
			logging.info(this, "retrieveSoftwareAuditOnClients client2Software.keySet size " 
			+ "   +++  " + client2software.keySet().size());
			
			newClients.removeAll(client2software.keySet());
		}
		
		logging.info(this, "retrieveSoftwareAuditOnClients client2Software null " 
			+ (client2software == null) 
			+ "  new clients count  ====== " + newClients.size());
		
		int stepSize = 100;
		
		//if (client2software == null || softwareId2clients == null || newClients.size() > 0)
		if (client2software == null || softwareIdent2clients == null || newClients.size() > 0)
		{
			int step = 1;
			while (newClients.size() > 0)
			{
				java.util.List<String> clientListForCall = new ArrayList<String>();
			
				for (int i = 0; i<stepSize && i < newClients.size(); i++)
					clientListForCall .add(newClients.get(i));
				
				newClients.removeAll(clientListForCall);
					
			
				//logging.info(this, "retrieveSoftwareAuditOnClients for " + clientListForCall.size()  + " clients " + clientListForCall);
				
				//client2software = new HashMap<String, java.util.List<String>>();
				if (client2software == null) client2software = new HashMap<String, java.util.List<SWAuditClientEntry>>();
				
				if (softwareIdent2clients == null) softwareIdent2clients = new HashMap<String, java.util.Set<String>>();
				//if (softwareId2clients == null) softwareId2clients = new HashMap<Integer, java.util.List<String>>();
			
				
				
				controller.notifyDataLoadingObservers(configed.getResourceValue("LoadingObserver.loadtable") + " software config, step " + step);
				
				logging.info(this, "retrieveSoftwareAuditOnClients, start a request");
				
				String[] callAttributes = new String[] {};
				HashMap callFilter = new HashMap();
				callFilter.put("state", 1);
				if (newClients != null) callFilter.put("clientId",  controller.exec.jsonArray(clientListForCall));
				
				java.util.List <Map<String, Object>> softwareAuditOnClients
				= controller.retrieveListOfMapsNOM(
					 callAttributes,
					 callFilter,
					 "auditSoftwareOnClient_getHashes"
					 );
				
				logging.info(this, "retrieveSoftwareAuditOnClients, finished a request, map size " + softwareAuditOnClients.size());
				
				if (softwareAuditOnClients == null)
				{
					logging.warning(this, "no auditSoftwareOnClient");
				}
				else
				{
					
					for (String clientId : clientListForCall)
					{
						client2software.put(clientId, new LinkedList<SWAuditClientEntry>());
					}
					
					
					for (Map <String, Object> item : softwareAuditOnClients)
					{
						
						SWAuditClientEntry clientEntry = new SWAuditClientEntry(item, controller);
						
						String clientId = clientEntry.getClientId();
						String swIdent = clientEntry.getSWident();
						
						/*
						if (swIdent.startsWith("firefox"))
						{
							logging.info(this, " retrieveSoftwareAuditOnClient clientId : swIdent " + clientId + " : "  + swIdent);
						}
						*/
						
						
						Set<String>clientsWithThisSW = softwareIdent2clients.get(swIdent);
						if (clientsWithThisSW == null)
						{
							clientsWithThisSW = new HashSet<String>();
							softwareIdent2clients.put(swIdent, clientsWithThisSW);
						}
						
						clientsWithThisSW.add(clientId);
						
						/*
						if (clientEntry.getSWid() == -1)
						{
							logging.info("Missing auditSoftware entry for swIdent " + 
								SWAuditClientEntry.produceSWident(item));
							//item.put(SWAuditEntry.WINDOWSsOFTWAREid, "MISSING");
						}
						else
						*/
						{
							if (clientId != null) //null not allowed in mysql
							{
								java.util.List<SWAuditClientEntry> entries = client2software.get(clientId);
								
								//variant1
								/*
								if (entries == null)
								{retrieveSoftwareAuditOnClients, start a request");
					
									entries = new LinkedList<SWAuditClientEntry>();
									client2software.put(clientId, entries);
								}
								*/
								entries.add(clientEntry);
							}
							
						}
					}
					
				}
				
				
				logging.info(this, "retrieveSoftwareAuditOnClients client2software "); // + client2software);
				
				softwareAuditOnClients = null;
				
				step++;
			
			}
			
			
			logging.info(this,  "retrieveSoftwareAuditOnClients used memory on end " + de.uib.utilities.Globals.usedMemory()); 
			System.gc();
			logging.info(this,  "retrieveSoftwareAuditOnClients used memory on end " + de.uib.utilities.Globals.usedMemory()); 
			
			controller.notifyDataRefreshedObservers("softwareConfig");
		}
	}
	
	//===================================================
	
	protected AuditSoftwareXLicencePool auditSoftwareXLicencePool;
	//protected java.sql.Time CONFIG_STATE_last_entry = null;
	
	@Override
	public void auditSoftwareXLicencePoolRequestRefresh()
	{
		logging.info(this, "auditSoftwareXLicencePoolRequestRefresh");
		auditSoftwareXLicencePool= null;
	}
	
	@Override
	public AuditSoftwareXLicencePool getAuditSoftwareXLicencePool()
	{
		retrieveAuditSoftwareXLicencePool();
		return auditSoftwareXLicencePool;
	}
	
	protected void retrieveAuditSoftwareXLicencePool()
	//AUDIT_SOFTWARE_TO_LICENSE_POOL
	{
		if (auditSoftwareXLicencePool != null)
			return;
		
		logging.info(this, "retrieveAuditSoftwareXLicencePool");
		
		controller.notifyDataLoadingObservers(configed.getResourceValue("LoadingObserver.loadtable") + " AUDIT_SOFTWARE_TO_LICENSE_POOL");
		
		List<Map<String, Object>> retrieved 
				= controller.retrieveListOfMapsNOM(
					AuditSoftwareXLicencePool.SERVICE_ATTRIBUTES,
					new HashMap(), //callFilter
					"auditSoftwareToLicensePool_getObjects"
					);
		
		auditSoftwareXLicencePool = new AuditSoftwareXLicencePool(getSoftwareList());
		
		for (Map<String, Object> map : retrieved)
		{
			//logging.info(this, "retrieved map " + map);
			
			auditSoftwareXLicencePool.integrateRaw(map);
		}
			
				
		logging.info(this, "retrieveAuditSoftwareXLicencePool retrieved ");
		//+ auditSoftwareXLicencePool);
		
		//logging.info(this, "retrieveAuditSoftwareXLicencePool by licencepool " + auditSoftwareXLicencePool.getFunctionBy(LicencepoolEntry.idKEY));
	}

	
	
	//===================================================

	protected  Map<String, Map<String, Object>> hostConfigs;
	protected java.sql.Time CONFIG_STATE_last_entry = null;

	@Override
	public void hostConfigsRequestRefresh()
	{
		logging.info(this, "hostConfigsRequestRefresh");
		hostConfigs= null;
	}
	
	@Override
	public Map<String, Map<String, Object>> getConfigs()
	{
		retrieveHostConfigs();
		return hostConfigs;
	}
		

	protected void retrieveHostConfigs()
	{
		if (hostConfigs != null)
			return;
		
		logging.info(this, "retrieveHostConfigs classCounter:" + classCounter);
		
		
		controller.notifyDataLoadingObservers(configed.getResourceValue("LoadingObserver.loadtable") + " config state");
			
		TimeCheck timeCheck = new TimeCheck(this, " retrieveHostConfigs");
		timeCheck.start();
		
		String[] callAttributes = new String[]{};
		HashMap callFilter = new HashMap();
		
		List<Map<String, Object>>  retrieved = controller.retrieveListOfMapsNOM(
			                                           callAttributes,
			                                           callFilter,
			                                           "configState_getObjects"
			                                       );
		hostConfigs = new HashMap<String, Map<String, Object>>();
		
		for (Map<String, Object> listElement : retrieved)
		{
			Object id = listElement.get("objectId");  
			
			//logging.info(this, "retrieveHostConfigs " + id);
			
			if (
				id != null && id instanceof String && !id.equals("") 
			)
			{
				String hostId = (String) id;
				Map<String, Object> configs1Host = hostConfigs.get(id);
				if (configs1Host == null)
				{
					configs1Host = new HashMap<String, Object>();
					hostConfigs.put(hostId, configs1Host);
				}
							
				logging.debug(this, "retrieveHostConfigs objectId,  element " + id  + ": " + listElement);
				
				if (listElement.get("values") == null)
				{
					configs1Host.put(
						(String) listElement.get("configId"),
						new ArrayList<Object>()
					);
					//is a data error but can occur
				}
				else
				{
					String configId = (String) listElement.get("configId"); 
					configs1Host.put(
						configId, 
						((org.json.JSONArray)(listElement.get("values"))).toList()
					);
					
				}
			}
		}
		
		timeCheck.stop();
		logging.info(this, "retrieveHostConfigs retrieved " +
			hostConfigs.keySet());
		
		controller.notifyDataRefreshedObservers("configState");


	}
	
	
	
	
	//===================================================
	protected TreeMap<String, LicencepoolEntry> licencepools;
	//protected java.sql.Time CONFIG_STATE_last_entry = null;
	
	
	@Override
	public void licencepoolsRequestRefresh()
	{
		logging.info(this, "licencepoolsRequestRefresh");
		licencepools= null;
	}
	
	@Override
	public Map<String, LicencepoolEntry> getLicencepools()
	{
		retrieveLicencepools();
		return licencepools;
	}

	protected void retrieveLicencepools()
	{
		if (licencepools != null)
			return;
	
		licencepools = new TreeMap<String, LicencepoolEntry>();

		if (controller.withLicenceManagement)
		{
			String[] attributes =
			    new String[] {
			    	LicencepoolEntry.idKEY, 
			    	LicencepoolEntry.descriptionKEY
			    };
			  
			controller.notifyDataLoadingObservers(configed.getResourceValue("LoadingObserver.loadtable") + " licence pool");
		
			List<Map<String, Object>> retrieved = 
				controller.retrieveListOfMapsNOM
				(
			            attributes,
			            new HashMap(),
			            "licensePool_getObjects"
			         );

			for (Map<String, Object> importedEntry : retrieved)
			{
				LicencepoolEntry entry = new LicencepoolEntry(importedEntry);
				licencepools.put(entry.getLicencepoolId(), entry);
			}

		}
	}
	
	
	//===================================================
	protected java.util.Map<String, LicenceContractEntry> licenceContracts;
	protected Table_LicenceContracts tableLicenceContracts; 
	
	@Override
	public void licenceContractsRequestRefresh()
	{
		logging.info(this, "licenceContractsRequestRefresh");
		tableLicenceContracts = null;
		licenceContracts = null;
	}
	
	@Override
	public  java.util.Map<String, LicenceContractEntry> getLicenceContracts()
	{
		retrieveLicenceContracts();
		return licenceContracts;
	}

	
	protected void retrieveLicenceContracts()
	//LICENSE_CONTRACT 
	{
		if (licenceContracts != null)
			return;
		
		licenceContracts = new HashMap<String, LicenceContractEntry>();
		
		//tableLicenceContracts = new Table_LicenceContracts();

		
		if (controller.withLicenceManagement)
		{
			controller.notifyDataLoadingObservers(configed.getResourceValue("LoadingObserver.loadtable") + " software license");
			
			List<Map<String, Object>> retrieved = 
				controller.retrieveListOfMapsNOM
				(
			            "licenseContract_getObjects"
			         );
			 
			for (Map<String, Object> importedEntry : retrieved)
			{
				LicenceContractEntry entry = new LicenceContractEntry(importedEntry);
				licenceContracts.put(entry.getId(), entry);
			}
		}
	}
		
		
	
	//===================================================
	protected java.util.Map<String, LicenceEntry> licences;
	//protected java.sql.Time CONFIG_STATE_last_entry = null;
	
	@Override
	public void licencesRequestRefresh()
	{
		logging.info(this, "licencesRequestRefresh");
		licences= null;
	}
	
	@Override
	public  java.util.Map<String, LicenceEntry> getLicences()
	{
		retrieveLicences();
		return licences;
	}

	protected void retrieveLicences()
	//SOFTWARE_LICENSE
	{
		if (licences != null)
			return;
		
		licences = new HashMap<String, LicenceEntry>();
		
		if (controller.withLicenceManagement)
		{
			controller.notifyDataLoadingObservers(configed.getResourceValue("LoadingObserver.loadtable") + " software license");
			
			List<Map<String, Object>> retrieved = 
				controller.retrieveListOfMapsNOM
				(
			            "softwareLicense_getObjects"
			         );

			for (Map<String, Object> importedEntry : retrieved)
			{
				LicenceEntry entry = new LicenceEntry(importedEntry);
				licences.put(entry.getId(), entry);
			}
		}
	}
	
	//===================================================
	protected java.util.List<LicenceUsableForEntry> licenceUsabilities;
	//protected java.sql.Time CONFIG_STATE_last_entry = null;
	
	@Override
	public void licenceUsabilitiesRequestRefresh()
	{
		logging.info(this, "licenceUsabilitiesRequestRefresh");
		licenceUsabilities= null;
	}
	
	@Override
	public java.util.List<LicenceUsableForEntry> getLicenceUsabilities()
	{
		retrieveLicenceUsabilities();
		return licenceUsabilities;
	}

	protected void retrieveLicenceUsabilities()
	//SOFTWARE_LICENSE_TO_LICENSE_POOL
	{
		if (licenceUsabilities != null)
			return;
		
		licenceUsabilities = new ArrayList<LicenceUsableForEntry>();
		
		if (controller.withLicenceManagement)
		{
			controller.notifyDataLoadingObservers(configed.getResourceValue("LoadingObserver.loadtable") + " software_license_TO_license_pool");
			
			List<Map<String, Object>> retrieved = 
				controller.retrieveListOfMapsNOM
				(
			            "softwareLicenseToLicensePool_getObjects"
			         );

			for (Map<String, Object> importedEntry : retrieved)
			{
				LicenceUsableForEntry entry = LicenceUsableForEntry.produceFrom(importedEntry);
				licenceUsabilities.add(entry);
			}
		}
			
	}
	
	
	//===================================================
	protected java.util.List<LicenceUsageEntry> licenceUsages;
	//protected java.sql.Time CONFIG_STATE_last_entry = null;
	
	@Override
	public void licenceUsagesRequestRefresh()
	{
		logging.info(this, "licenceUsagesRequestRefresh");
		licenceUsages= null;
	}
	
	@Override
	public java.util.List <LicenceUsageEntry>getLicenceUsages()
	{
		retrieveLicenceUsages();
		return licenceUsages;
	}

	protected void retrieveLicenceUsages()
	//LICENSE_ON_CLIENT
	{
		logging.info(this, "retrieveLicenceUsages");
		if (licenceUsages != null)
			return;
		
		licenceUsages = new ArrayList<LicenceUsageEntry>();
		
		if (controller.withLicenceManagement)
		{
			controller.notifyDataLoadingObservers(configed.getResourceValue("LoadingObserver.loadtable") + " license_on_client");
			
			List<Map<String, Object>> retrieved = 
				controller.retrieveListOfMapsNOM
				(
			            "licenseOnClient_getObjects"
			         );

			    
			for (Map<String, Object> importedEntry : retrieved)
			{
				LicenceUsageEntry entry = new LicenceUsageEntry(importedEntry);
				
				licenceUsages.add(entry);
			}
		}
	
	}
	

		
	//===================================================
	protected LicencePoolXOpsiProduct licencePoolXOpsiProduct;
	
	@Override
	public void licencePoolXOpsiProductRequestRefresh()
	{
		logging.info(this, "licencePoolXOpsiProductRequestRefresh");
		licencePoolXOpsiProduct = null;
	}
	
	@Override
	public LicencePoolXOpsiProduct getLicencePoolXOpsiProduct()
	{
		retrieveLicencePoolXOpsiProduct();
		return licencePoolXOpsiProduct;
	}
	
	protected void retrieveLicencePoolXOpsiProduct()
	//LICENSE_POOL
	{
		if (licencePoolXOpsiProduct != null)
			return;
		
		logging.info(this, "retrieveLicencePoolXOpsiProduct");
		
		controller.notifyDataLoadingObservers(configed.getResourceValue("LoadingObserver.loadtable") + " PRODUCT_ID_TO_LICENSE_POOL");
		
		List<Map<String, Object>> retrieved 
				= controller.retrieveListOfMapsNOM(
					LicencePoolXOpsiProduct.SERVICE_ATTRIBUTES_asArray,
					new HashMap(), //callFilter
					"licensePool_getObjects"
					);
		//integrates two database calls
		
		licencePoolXOpsiProduct = new LicencePoolXOpsiProduct();
		
		for (Map<String, Object> map : retrieved)
		{
			//logging.info(this, "retrieved map " + map);
			
			licencePoolXOpsiProduct.integrateRawFromService(map);
		}
			
				
	}

		
	
	
	
	
}
				
