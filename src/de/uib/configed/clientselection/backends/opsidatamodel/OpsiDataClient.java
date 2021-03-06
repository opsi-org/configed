package de.uib.configed.clientselection.backends.opsidatamodel;

import de.uib.configed.type.*;
import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.backends.opsidatamodel.operations.*;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.logging;
import java.util.*;

public class OpsiDataClient implements Client
{
    public final static String HOSTINFO_MAP="HostMap";
    public final static String SOFTWARE_MAP="SoftwareMap";
    public final static String SWAUDIT_MAP="SwAuditMap";
    
    private String hostId;
    private Map infoMap;
    private Set groupsSet; // The opsi groups for clients
    private List< Map<String, Object> > hardwareInfo;
    private List productList; // The products (a list of maps)
    private List<String> productNames; // Like above, but just the productIDs
    private List<SWAuditClientEntry> swauditList;
    private PersistenceController controller;
    
    private Map softwareValue; // current software to be checked
    private Map swauditValue; // current swaudit to be checked
    private Iterator<Map<String, Object> > hardwareIterator=null;
    private Map hardwareValue=null; // current hardware to be checked
    
    public OpsiDataClient( String id )
    {
        hostId = id;
        groupsSet = new HashSet();
        productList = new LinkedList();
        productNames = new ArrayList<String>();
        swauditList = new LinkedList<SWAuditClientEntry>();
        hardwareInfo = new LinkedList< Map<String, Object> >();
        controller = null;
    }
    
    /** Set the map with the information about hosts */
    public void setInfoMap( Map map )
    {
        infoMap = map;
    }
    
    /** Set the existing opsi client groups */
    public void setGroups( Set groups )
    {
        groupsSet = groups;
    }
    
    /** Set the list of opsi products */
    public void setOpsiProductList( List productList )
    {
        this.productList = productList;
        
        for (Object element : productList)
        {
        		productNames.add( 
        			(String) ((Map) element).get("productId")
        		);
        }
    }
    
    /** Set the products found by software audit */
    public void setSwAuditList( List<SWAuditClientEntry> swauditList )
    {
        this.swauditList = swauditList;
    }
    
    /** Set the controller (to get the information) */
    public void setController( PersistenceController controller )
    {
        this.controller = controller;
    }
    
    /** Get a map by name */
    public Map getMap( String map )
    {
        if( map.equals( HOSTINFO_MAP ) )
            return infoMap;
        else if( map.equals( SOFTWARE_MAP ) )
            return softwareValue;
        else if( map.equals( SWAUDIT_MAP ) )
            return swauditValue;
        else
            return getHardwareMap( map );
    }
    
    /** Get the ID of this client */
    public String getId()
    {
        return hostId;
    }
    
    public String toString()
    {
    		return hostId + " ( " + this.getClass() + " )";
    }
    
    /** Get the list of opsi products */
    public List getSoftwareList()
    {
        return productList;
    }
    
    /** Get the list of opsi product IDs */
    public List<String> getProductNames()
    {
    		return productNames;
    }
    
    /** Get the list of software audit products */
    public List<SWAuditClientEntry> getSwAuditList()
    {
        return swauditList;
    }
    
    /** Get the opsi client groups */
    public Set getGroups()
    {
        return groupsSet;
    }
    
    /** Set the current opsi software value */
    public void setCurrentSoftwareValue( Map value )
    {
        softwareValue = value;
    }
    
    /** Set the current software audit value */
    public void setCurrentSwAuditValue( Map value )
    {
        swauditValue = value;
    }
    
    /** Start the iterator interator. */
    public void startHardwareIterator()
    {
        hardwareIterator = null;
    }
    
    /** Go to the next hardware. Return false, if that is not possibe */
    public boolean hardwareIteratorNext()
    {
        if( hardwareIterator == null || !hardwareIterator.hasNext() )
            return false;
        
        hardwareValue = hardwareIterator.next();
        logging.debug( this,  "hardwareIteratorNext: " + hardwareValue.toString() );
        return true;
    }
    
    /** Set the hardware information */
    public void setHardwareInfo( List< Map<String, Object> > hardwareInfo )
    {
        this.hardwareInfo = hardwareInfo;
    }
    
    /** Get the hardware Map for this string */
    private Map getHardwareMap( String key )
    {
        if( hardwareIterator == null )
        {
            logging.debug( this, "getHardwareMap key " + key );
            logging.debug( this, "getHardwareMap hardwareInfo " + hardwareInfo);
            HashSet<Map<String, Object> > values = new HashSet<Map<String, Object> >();
            for( Map<String, Object> map: hardwareInfo )
                if( key.equals( (String) map.get("hardwareClass") ) )
                    values.add( map );
            logging.debug( this, values.toString() );
            
            hardwareValue = null;
            hardwareIterator = values.iterator();
            hardwareIteratorNext();
        }
        if( hardwareValue != null )
        {
            return hardwareValue;
        }
        return new HashMap();
    }
}
