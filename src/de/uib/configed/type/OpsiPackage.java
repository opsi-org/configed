/**
 *  OpsiPackage
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *    
 *  copyright:     Copyright (c) 2014
 *  organization: uib.de
 *  @author  R. Roeder 
 */


package de.uib.configed.type;
import java.util.*;
import de.uib.utilities.logging.*;

//data source table productOnDepot
public class OpsiPackage
	implements Comparable
{
	protected String productId;
	protected int productType;
	protected String versionInfo;
	protected String productVersion;
	protected String packageVersion;
	
	protected String representation; 
	
	public static final String DBkeyPRODUCT_ID = "productId";
	public static final String SERVICEkeyPRODUCT_ID0 = "id";
	public static final String SERVICEkeyPRODUCT_VERSION = "productVersion";
	public static final String SERVICEkeyPACKAGE_VERSION = "packageVersion";
	public static final String SERVICEkeyPRODUCT_TYPE = "productType";
	public static final String VERSION_INFO = "versionInfo";
	
	public static final String LOCALBOOT_PRODUCT_SERVER_STRING = "LocalbootProduct";
	public static final String NETBOOT_PRODUCT_SERVER_STRING = "NetbootProduct";
	
	public static final ArrayList<String> SERVICE_KEYS;
	static{
		SERVICE_KEYS = new ArrayList<String>();
		SERVICE_KEYS.add(SERVICEkeyPRODUCT_ID0);
		SERVICE_KEYS.add(SERVICEkeyPRODUCT_VERSION);
		SERVICE_KEYS.add(SERVICEkeyPACKAGE_VERSION);
		SERVICE_KEYS.add(SERVICEkeyPRODUCT_TYPE);
	}
	
	public final static Vector<String> COLUMN_NAMES;
	static{
		COLUMN_NAMES = new Vector<String>();
		COLUMN_NAMES.add(DBkeyPRODUCT_ID);
		COLUMN_NAMES.add(SERVICEkeyPRODUCT_VERSION);
		COLUMN_NAMES.add(SERVICEkeyPACKAGE_VERSION);
		COLUMN_NAMES.add(SERVICEkeyPRODUCT_TYPE);
		
	}
	
	//public class PackageRow extends Vector<Object>;
	
	public static  final int TYPE_LOCALBOOT = 0;
	public static  final int TYPE_NETBOOT = 1;
	
	public static int lastIndex = -1;
	
	
	//public static Map<Integer, String> productName2Id = new HashMap<String, Integer>();
	
	public OpsiPackage(String productId, String productVersion, String packageVersion, String productType)
	{
		this.productId = productId;
		this.productVersion = productVersion;
		this.packageVersion = packageVersion;
		this.versionInfo = productVersion  + de.uib.configed.Globals.ProductPackageVersionSeparator.forKey() 
					+ packageVersion;
		
		if (productType.equals(LOCALBOOT_PRODUCT_SERVER_STRING))
			this.productType = 0;
		else if (productType .equals(NETBOOT_PRODUCT_SERVER_STRING) )
			this.productType = 1;
		else 
			this.productType = -1;
		
		
		logging.debug(this, "created : " + productId + ", " + productType + ", " + versionInfo);
		
		
		
		representation = buildRepresentation();
	}
	
	public OpsiPackage(Map<String, Object> m)
	{
		this(
			"" + m.get(DBkeyPRODUCT_ID),
			"" + m.get(SERVICEkeyPRODUCT_VERSION),
			"" + m.get(SERVICEkeyPACKAGE_VERSION),
			"" + m.get(SERVICEkeyPRODUCT_TYPE)
		);
		logging.debug(this, "built from " + m);
		
		/*
		if (m.get("id") == null)
		{
			logging.warning(this, " has unexpected key 'productId' with value " +  m.get("productId") +  " from Map " + m );
		}
		*/
	}
	
	public String getProductId()
	{
		return productId;
	}
	
	public String getProductVersion()
	{
		return productVersion;
	}
	
	public String getPackageVersion()
	{
		return packageVersion;
	}
	
	public String getVersionInfo()
	{
		return versionInfo;
	}
	
	public static String produceVersionInfo(String productVersion, String packageVersion)
	{
		return productVersion + de.uib.configed.Globals.ProductPackageVersionSeparator.forKey() + packageVersion;
	}
	
	public int getProductType()
	{
		return productType;
	}
	
	public Vector<Object> appendValues(Vector<Object> row)
	{
		//row.add(getProductId());
		row.add(giveProductType(getProductType()));
		row.add(getProductVersion());
		row.add(getPackageVersion());
		return row;
	}
	
	public boolean  isLocalbootProduct()
	{
		return (productType == TYPE_LOCALBOOT);
	}
	
	
	public boolean isNetbootProduct()
	{
		return (productType == TYPE_NETBOOT);
	}
	
	
	public static String giveProductType(int type)
	{
		switch (type)
		{
			case TYPE_LOCALBOOT : return LOCALBOOT_PRODUCT_SERVER_STRING;
			case TYPE_NETBOOT : return NETBOOT_PRODUCT_SERVER_STRING;
		}
		return "error";
	}
	
	protected String buildRepresentation()
	{
		return  
			//getClass().getName() + 
		"{"
		+ DBkeyPRODUCT_ID + ":\"" + productId + "\";"
		+ SERVICEkeyPRODUCT_TYPE + ":\"" + giveProductType(productType) + "\";"
		+ VERSION_INFO + ":\"" + versionInfo 
		+ "\"}";
	}
		
	
	@Override
	public String toString()
	{
		return representation;
	}
	
	//Interface Comparable
	public int compareTo( Object o )
	{
		return representation.compareTo( o.toString() );
	}
	
	@Override
	public boolean equals(Object o)
	{
		return representation.equals( o.toString() );
	}
		
}
	

