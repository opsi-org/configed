package de.uib.configed.type.licences;
import java.util.*;
import de.uib.configed.type.*;
import de.uib.utilities.datastructure.*;
import de.uib.utilities.logging.*;

public class AuditSoftwareXLicencePool extends Relation
{
	/* 
	AUDIT_SOFTWARE_TO_LICENSE_POOL in database
	
	| Field         		| Type         		| Null  | Key 
	| licensePoolId	| varchar(100) 	| NO   | MUL	| NULL    
	| name          		| varchar(100) 	| NO   | PRI	| NULL    
	| version       		| varchar(100) 	| NO   | PRI	| NULL    
	| subVersion    	| varchar(100) 	| NO   | PRI	| NULL    
	| language      	| varchar(10)  	| NO   | PRI	| NULL    
	| architecture  	| varchar(3)   		| NO   | PRI	| NULL    

	*/
	
	public LinkedList<String> registeredSoftware;
	
	public final static String SwID = "swId";
	
	
	public final static java.util.List<String> ATTRIBUTES;
	//public final static String[] ATTRIBUTES_asArray;
	static{
		ATTRIBUTES = new  LinkedList<String>();
		ATTRIBUTES.add(LicencepoolEntry.idSERVICEKEY);
		//ATTRIBUTES.add(SwID);
		
		ATTRIBUTES.add(SWAuditEntry.NAME);
		ATTRIBUTES.add(SWAuditEntry.VERSION);
		ATTRIBUTES.add(SWAuditEntry.SUBVERSION);
		ATTRIBUTES.add(SWAuditEntry.LANGUAGE);
		ATTRIBUTES.add(SWAuditEntry.ARCHITECTURE);
		
		
		//ATTRIBUTES_asArray = ATTRIBUTES.toArray(new String[0]);
	}
	
	public final static java.util.List<String> INTERFACED_ATTRIBUTES;
	static{
		INTERFACED_ATTRIBUTES = new LinkedList<String>(ATTRIBUTES);
		INTERFACED_ATTRIBUTES.add(SwID);
	}
	
	public final static String[] SERVICE_ATTRIBUTES =
		new String[]{
			LicencepoolEntry.idSERVICEKEY,
			SWAuditEntry.NAME,
			SWAuditEntry.VERSION,
			SWAuditEntry.SUBVERSION,
			SWAuditEntry.LANGUAGE,
			SWAuditEntry.ARCHITECTURE
		};
			
	
	public AuditSoftwareXLicencePool( LinkedList<String> allRegisteredSoftware) 
	{
		super(ATTRIBUTES);
		registeredSoftware = allRegisteredSoftware;
		//logging.info(this, "registeredSoftware");
		//for (String sw : registeredSoftware)  logging.info(this, sw);
			
	}
	
	
	/*
	private String produceSWident(Map<String, String> m)
	{
		String result = de.uib.utilities.Globals.pseudokey(new String[]{
				de.uib.utilities.Globals.getStringValue( m.get(SWAuditEntry.NAME) ),
				de.uib.utilities.Globals.getStringValue( m.get(SWAuditEntry.VERSION) ),
				de.uib.utilities.Globals.getStringValue( m.get(SWAuditEntry.SUBVERSION) ) ,
				de.uib.utilities.Globals.getStringValue( m.get(SWAuditEntry.LANGUAGE) ),
				de.uib.utilities.Globals.getStringValue (m.get (SWAuditEntry.ARCHITECTURE) )
				}
		);
		
		return result;
	}
	*/
	
	
	/*
	private Integer produceSWidRaw(Map<String, Object> m)
	{
		return produceSWid(new RelationElement(m));
	}
	
	private Integer produceSWid(Map<String, String> m)
	{
		 String swIdent = produceSWident(m);
		 
		 int result = -1;
		 
		 boolean newEntry = false;
		 
		 
		 logging.info(this, "produceSWid for " + swIdent);
		 
		 int swId = registeredSoftware.indexOf(swIdent);
		 
		 
		 if (swId > -1)
		 	 result = swId;
		 else
		 {
		 	 //logging.error("no software entry for " + swIdent);
		 	 registeredSoftware.add(swIdent);
		 	 result = registeredSoftware.size();
		 	 newEntry = true;
		 }
		 
		 String info = "";
		 if (m.get(LicencepoolEntry.idSERVICEKEY) != null)
		 	 info = " " + m.get(LicencepoolEntry.idSERVICEKEY) + " "; 
		 
		 //logging.info(this, "registeredSoftware " + registeredSoftware);
		 logging.info(this, "swident  " + swIdent +  " for " + info + "  ==== id (new " + newEntry +") "  + result);
		 
		 return result;
	}
	*/
	
	private String produceSWident(Map<String, Object> m)
	{
		return 
			de.uib.utilities.Globals.pseudokey(new String[]{
				de.uib.utilities.Globals.getStringValue( m.get(SWAuditEntry.NAME ) ),
				de.uib.utilities.Globals.getStringValue( m.get(SWAuditEntry.VERSION ) ),
				de.uib.utilities.Globals.getStringValue( m.get(SWAuditEntry.SUBVERSION )  ),
				de.uib.utilities.Globals.getStringValue( m.get(SWAuditEntry.LANGUAGE ) ),
				de.uib.utilities.Globals.getStringValue( m.get (SWAuditEntry.ARCHITECTURE ) )
			}
		);
	}

	@Override
	public StringValuedRelationElement integrateRaw(Map<String,Object> m)
	{
		StringValuedRelationElement rowmap = new StringValuedRelationElement();
		rowmap.setAllowedAttributes(INTERFACED_ATTRIBUTES);
		String swIdent = "" + produceSWident(m);
		rowmap.put(SwID, swIdent);
		
		/*
		if (swIdent.indexOf("55375-337") > -1 || swIdent.indexOf("55375-640") > -1)
			logging.info(this, "integrateRaw " + m);
		*/
		
		rowmap.put(
			LicencepoolEntry.idSERVICEKEY, 
			de.uib.utilities.Globals.getStringValue(m.get(LicencepoolEntry.idSERVICEKEY))
			);
		add( rowmap );
		
		return rowmap;
	}
	

}
				
	
