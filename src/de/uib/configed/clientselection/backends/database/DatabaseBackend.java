package de.uib.configed.clientselection.backends.database;

import java.util.*;
import java.sql.*;
import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.elements.*;
import de.uib.configed.clientselection.backends.database.operations.*;
import de.uib.utilities.logging.logging;

public class DatabaseBackend extends Backend
{   
    public SelectOperation createOperation( String operation, SelectData data, String element, String[] elementPath )
    {
    		logging.debug(this, "createOperation operation, data, element: " + operation + ", " + data + ",  " + element);
        if( element.equals( "NameElement" ) )
        {
            if( operation.equals("=") )
                return new DatabaseStringEqualsOperation( "hostId", (String) data.getData(), null );
        }
        else if( element.equals( "IPElement" ) )
        {
            if( operation.equals("=") )
                return new DatabaseStringEqualsOperation( "ipAddress", (String) data.getData(), null );
        }
        else if( element.equals( "DescriptionElement" ) )
        {
            if( operation.equals("=") )
                return new DatabaseStringEqualsOperation( "description", (String) data.getData(), null );
        }
        else if( element.equals( "SoftwareNameElement" ) )
        {
            if( operation.equals("=") )
                return new DatabaseStringEqualsOperation( "productId", (String) data.getData(), "PRODUCT_ON_CLIENT" );
        }
        else if( element.equals( "SoftwareVersionElement" ) )
        {
            if( operation.equals("=") )
                return new DatabaseStringEqualsOperation( "productVersion", (String) data.getData(), "PRODUCT_ON_CLIENT" );
        }
        else if( element.equals( "SoftwareRequestElement" ) )
        {
            if( operation.equals("=") || operation.equals("is") )
                return new DatabaseStringEqualsOperation( "actionRequest", (String) data.getData(), "PRODUCT_ON_CLIENT" );
        }
        else if( element.equals( "SoftwareTargetConfigurationElement" ) )
        {
            if( operation.equals("=") || operation.equals("is") )
                return new DatabaseStringEqualsOperation( "targetConfiguration", (String) data.getData(), "PRODUCT_ON_CLIENT" );
        }
        else if( element.equals( "SoftwareInstallationStatusElement" ) )
        {
            if( operation.equals("=") || operation.equals("is") )
                return new DatabaseStringEqualsOperation( "installationStatus", (String) data.getData(), "PRODUCT_ON_CLIENT" );
        }
        else if( element.equals( "SoftwareActionProgressElement" ) )
        {
            if( operation.equals("=") || operation.equals("is") )
                return new DatabaseStringEqualsOperation( "actionProgress", (String) data.getData(), "PRODUCT_ON_CLIENT" );
        }
        else if( element.equals( "SoftwareActionResultElement" ) )
        {
            if( operation.equals("=") || operation.equals("is") )
                return new DatabaseStringEqualsOperation( "actionResult", (String) data.getData(), "PRODUCT_ON_CLIENT" );
        }
        else if( element.equals( "SoftwareLastActionElement" ) )
        {
            if( operation.equals("=") || operation.equals("is") )
                return new DatabaseStringEqualsOperation( "lastAction", (String) data.getData(), "PRODUCT_ON_CLIENT" );
        }
        else if( element.equals( "SoftwarePackageVersionElement" ) )
        {
            if( operation.equals("<") )
                return new DatabaseIntLessThanOperation( "packageVersion", (Integer) data.getData(), "PRODUCT_ON_CLIENT" );
            if( operation.equals(">") )
                return new DatabaseIntGreaterThanOperation( "packageVersion", (Integer) data.getData(), "PRODUCT_ON_CLIENT" );
            if( operation.equals("=") )
                return new DatabaseIntEqualsOperation( "packageVersion", (Integer) data.getData(), "PRODUCT_ON_CLIENT" );
        }
        else if( element.equals( "SoftwareModificationTimeElement" ) )
        {
            if( operation.equals("=") )
                return new DatabaseStringEqualsOperation( "modificationTime", (String) data.getData(), "PRODUCT_ON_CLIENT" );
        }
        else if( element.equals( "GroupElement" ) )
        {
            if( operation.equals("=") || operation.equals("is") )
                return new DatabaseGroupOperation( new DatabaseStringEqualsOperation( "groupId", (String) data.getData(), "OBJECT_TO_GROUP" ) );
        }
        else if( element.equals( "GenericTextElement" ) )
        {
            if( elementPath.length == 2 )
            {
                String table=getHardwareTableName( elementPath[0] );
                String id="";
                String idName=elementPath[1];
                if( idName.equals("Vendor") )
                    id="vendor";
                else if( idName.equals("Name") )
                    id="name";
                else if( idName.equals("Model") )
                    id="model";
                else if( idName.equals("Description") )
                    id="description";
                else if( idName.equals("Level") )
                    id="level";
                
                if( operation.equals("=") )
                    return new DatabaseStringEqualsOperation( id, (String) data.getData(), table );
            }       
        }
        else if( element.equals( "GenericIntegerElement" ) )
        {
            if( elementPath.length == 2 )
            {
                String table=getHardwareTableName( elementPath[0] );
                String id="";
                String idName=elementPath[1];
                if( idName.equals("Max. Size" ) )
                    id="maxSize";
                
                if( operation.equals("<") )
                    return new DatabaseIntLessThanOperation( id, (Integer) data.getData(), table );
                if( operation.equals(">") )
                    return new DatabaseIntGreaterThanOperation( id, (Integer) data.getData(), table );
                if( operation.equals("=") )
                    return new DatabaseIntEqualsOperation( id, (Integer) data.getData(), table );
            }
        }
        else if( element.equals( "GenericEnumElement" ) )
        {
            if( elementPath.length == 2 )
            {
                String table=getHardwareTableName( elementPath[0] );
                String id="";
                String idName=elementPath[1];
                if( idName.equals("Location") )
                    id="location";
                    
                if( operation.equals("=") || operation.equals("is") )
                    return new DatabaseStringEqualsOperation( id, (String) data.getData(), table );
            }
        }
        logging.error( this, "The operation " +operation+ " was not found on " +elementPath );
        throw new IllegalArgumentException( "The operation " +operation+ " was not found on " +elementPath );
    }
    
    public SelectGroupOperation createGroupOperation( String operation, List<SelectOperation> operations )
    {
    		logging.debug(this, "createGroupOperation " + operation);
        if( operation.equals("and") && operations.size() >= 2 )
            return new DatabaseAndOperation( operations );
        if( operation.equals("or")  && operations.size() >= 2 )
            return new DatabaseOrOperation( operations );
        if( operation.equals("not") && operations.size() == 1 )
            return new DatabaseNotOperation( operations.get(0) );
        if( operation.equals(de.uib.opsidatamodel.OpsiProduct.NAME) && operations.size() == 1 )
            return new DatabaseSoftwareOperation( operations.get(0) );
        if( operation.equals("Hardware") && operations.size() == 1 )
            return new DatabaseHardwareOperation( operations.get(0) );
        logging.error(this, "The group operation " +operation+" was not found with " +operations.size()+" operations" );
        throw new IllegalArgumentException( "The group operation " +operation+" was not found with " +operations.size()+" operations" );
    }
    
    protected List<Client> getClients()
    {
        List<Client> clients = new LinkedList<Client>();
        try {
            String sql = "SELECT hostId FROM HOST;";
            logging.debug( this, sql );
            ResultSet result = DbConnect2.getConnection().createStatement().executeQuery(sql);
            logging.debug( this, result.toString() );
            while( result.next() )
            {
                DatabaseClient client = new DatabaseClient( result.getString(1) );
                clients.add( client );
            }
        }
        catch( SQLException e )
        {
            logging.error( this, e.getMessage(), e );
        }
        return clients;
    }
    
    //to implement
    public TreeSet<String> getProductIDs()
    {
    		return new TreeSet<String>();
    }
    
    public List<String> getGroups()
    {
        List<String> groups = new LinkedList<String>();
        try {
            ResultSet result = DbConnect2.getConnection().createStatement().executeQuery("SELECT groupId FROM opsi.GROUP;");
            while( result.next() )
            {
                groups.add( result.getString(1) );
            }
        }
        catch( SQLException e )
        {
            logging.error( this, e.getMessage(), e );
        }
        return groups;
    }
    
    public Map<String, List<SelectElement> > getHardwareList()
    {
        Map<String, List<SelectElement> > map = new HashMap<String, List<SelectElement> >();
        
        List<SelectElement> elements = new LinkedList<SelectElement>();
        map.put( "HARDWARE_DEVICE_1394_CONTROLLER", elements );
        
        elements = new LinkedList<SelectElement>();
        elements.add( new GenericTextElement(new String[] {"Audio Controller","Vendor"}) );
        elements.add( new GenericTextElement(new String[] {"Audio Controller","Name"} ) );
        elements.add( new GenericTextElement(new String[] {"Audio Controller","Model"} ) );
        map.put( "HARDWARE_DEVICE_AUDIO_CONTROLLER", elements );
        
        elements = new LinkedList<SelectElement>();
        elements.add( new GenericTextElement(new String[] {"Base Board","Vendor"}) );
        elements.add( new GenericTextElement(new String[] {"Base Board","Name"} ) );
        elements.add( new GenericTextElement(new String[] {"Base Board","Model"} ) );
        elements.add( new GenericTextElement(new String[] {"Base Board","Description"} ) );
        map.put( "HARDWARE_DEVICE_BASE_BOARD", elements );
        
        elements = new LinkedList<SelectElement>();
        elements.add( new GenericTextElement(new String[] {"Bios","Vendor"}) );
        elements.add( new GenericTextElement(new String[] {"Bios","Name"} ) );
        elements.add( new GenericTextElement(new String[] {"Bios","Model"} ) );
        elements.add( new GenericTextElement(new String[] {"Bios","Description"} ) );
        map.put( "HARDWARE_DEVICE_BIOS", elements );
        
        elements = new LinkedList<SelectElement>();
        elements.add( new GenericIntegerElement( new String[] {"Cache Memory","Installed Size"} ) );
        elements.add( new GenericTextElement( new String[] {"Cache Memory","Name"} ) );
        elements.add( new GenericTextElement( new String[] {"Cache Memory","Level"} ) );
        elements.add( new GenericIntegerElement( new String[] {"Cache Memory","Max. Size"} ) );
        elements.add( new GenericEnumElement( new String[] {"Internal", "External"}, new String[] {"Cache Memory","Location"} ) );
        elements.add( new GenericTextElement( new String[] {"Cache Memory","Description"} ) );
        map.put( "HARDWARE_DEVICE_CACHE_MEMORY", elements );
        
        elements = new LinkedList<SelectElement>();
//         elements.add( new GenericTextElement( "Chassis", "Type" ) );
//         elements.add( new GenericTextElement( "Chassis", "Name" ) );
//         elements.add( new GenericTextElement( "Chassis", "Description" );
        map.put( "HARDWARE_DEVICE_CHASSIS", elements );
        
        elements = new LinkedList<SelectElement>();
//         elements.add( new GenericTextElement( "Computer System", "Vendor" ) );
//         elements.add( new GenericTextElement( "Computer System", "Model" ) );
//         elements.add( new GenericTextElement( "Computer System", "Description" ) );
        map.put( "HARDWARE_DEVICE_COMPUTER_SYSTEM", elements );
        
        elements = new LinkedList<SelectElement>();
//         elements.add( new GenericTextElement( "Disk Partition", "Name" );
//         elements.add( new GenericTextElement( "Disk Partition", "Description" ) );
        map.put( "HARDWARE_DEVICE_DISK_PARTITION", elements );
        
        elements = new LinkedList<SelectElement>();
//         elements.add( new GenericTextElement( "Floppy Controller", "Vendor" ) );
//         elements.add( new GenericTextElement( "Floppy Controller", "Name" ) );
//         elements.add( new GenericTextElement( "Floppy Controller", "Device Type" ) );
//         elements.add( new GenericTextElement( "Floppy Controller", "Model" ) );
//         elements.add( new GenericTextElement( "Floppy Controller", "Revision" ) );
//         elements.add( new GenericTextElement( "Floppy Controller", "Description" ) );
        map.put( "HARDWARE_DEVICE_FLOPPY_CONTROLLER", elements );
        
        elements = new LinkedList<SelectElement>();
//         elements.add( new GenericTextElement( "Floppy Drive", "Vendor" ) );
//         elements.add( new GenericTextElement( "Floppy Drive", "Name" ) );
//         elements.add( new GenericTextElement( "Floppy Drive", "Description" ) );
//         elements.add( new GenericTextElement( "Floppy Drive", "Model" ) );
        map.put( "HARDWARE_DEVICE_FLOPPY_DRIVE", elements );
        
        elements = new LinkedList<SelectElement>();
//         elements.add( new GenericTextElement( "Harddisk Drive", "Vendor" ) );
//         elements.add( new GenericTextElement( "Harddisk Drive", "Name" ) );
//         elements.add( new GenericIntegerElement( "Harddisk Drive", "Sectors" ) ):
//         elements.add( new GenericTextElement( "Harddisk Drive", "Description" ) );
//         elements.add( new GenericTextElement( "Harddisk Drive", "Heads" ) );
//         elements.add( new GenericIntegerElement( "Harddisk Drive", "Cylinders" ) );
//         elements.add( new GenericTextElement( "Harddisk Drive", "Model" ) );
        map.put( "HARDWARE_DEVICE_HARDDISK_DRIVE", elements );
        
        elements = new LinkedList<SelectElement>();
//         String name = "HDAudio Device";
//         elements.add( new GenericTextElement( name, "Description" ) );
//         elements.add( new GenericTextElement( name, "Name" ) );
//         elements.add( new GenericTextElement( name, "Device Type" ) );
//         elements.add( new GenericTextElement( name, "Address" ) );
//         elements.add( new GenericIntegerElement( name, "Revision" ) );
        map.put( "HARDWARE_DEVICE_HDAUDIO_DEVICE", elements );
        
        elements = new LinkedList<SelectElement>();
        map.put( "HARDWARE_DEVICE_IDE_CONTROLLER", elements );
        
        elements = new LinkedList<SelectElement>();
        map.put( "HARDWARE_DEVICE_KEYBOARD", elements );
        
        elements = new LinkedList<SelectElement>();
        map.put( "HARDWARE_DEVICE_MEMORY_BANK", elements );
        
        elements = new LinkedList<SelectElement>();
        map.put( "HARDWARE_DEVICE_MEMORY_MODULE", elements );
        
        elements = new LinkedList<SelectElement>();
        map.put( "HARDWARE_DEVICE_MONITOR", elements );
        
        elements = new LinkedList<SelectElement>();
        map.put( "HARDWARE_DEVICE_NETWORK_CONTROLLER", elements );
        
        elements = new LinkedList<SelectElement>();
        map.put( "HARDWARE_DEVICE_OPTICAL_DRIVE", elements );
        
        elements = new LinkedList<SelectElement>();
        map.put( "HARDWARE_DEVICE_PCI_DEVICE", elements );
        
        elements = new LinkedList<SelectElement>();
        map.put( "HARDWARE_DEVICE_PCMCIA_CONTROLLER", elements );
        
        elements = new LinkedList<SelectElement>();
        map.put( "HARDWARE_DEVICE_POINTING_DEVICE", elements );
        
        elements = new LinkedList<SelectElement>();
        map.put( "HARDWARE_DEVICE_PORT_CONNECTOR", elements );
        
        elements = new LinkedList<SelectElement>();
        map.put( "HARDWARE_DEVICE_PRINTER", elements );
        
        elements = new LinkedList<SelectElement>();
        map.put( "HARDWARE_DEVICE_PROCESSOR", elements );
        
        elements = new LinkedList<SelectElement>();
        map.put( "HARDWARE_DEVICE_SCSI_CONTROLLER", elements );
        
        elements = new LinkedList<SelectElement>();
        map.put( "HARDWARE_DEVICE_SYSTEM_SLOT", elements );
        
        elements = new LinkedList<SelectElement>();
        map.put( "HARDWARE_DEVICE_TAPE_DRIVE", elements );
        
        elements = new LinkedList<SelectElement>();
        map.put( "HARDWARE_DEVICE_USB_CONTROLLER", elements );
        
        elements = new LinkedList<SelectElement>();
        map.put( "HARDWARE_DEVICE_USB_DEVICE", elements );
        
        elements = new LinkedList<SelectElement>();
        map.put( "HARDWARE_DEVICE_VIDEO_CONTROLLER", elements );
        
        return map;
    }
    
    private String getHardwareTableName( String name )
    {
        if( name.equals("1394 Controller") )
            return "HARDWARE_DEVICE_1394_CONTROLLER";
        if( name.equals("Audio Controller") )
            return "HARDWARE_DEVICE_AUDIO_CONTROLLER";
        if( name.equals("Base Board") )
            return "HARDWARE_DEVICE_BASE_BOARD";
        if( name.equals("Bios") )
            return "HARDWARE_DEVICE_BIOS";
        if( name.equals("Cache Memory") )
            return "HARDWARE_DEVICE_CACHE_MEMORY";
        if( name.equals("Chassis") )
            return "HARDWARE_DEVICE_CHASSIS";
        if( name.equals("Computer System") )
            return "HARDWARE_DEVICE_COMPUTER_SYSTEM";
        if( name.equals("Disk Partition") )
            return "HARDWARE_DEVICE_DISK_PARTITION";
        if( name.equals("Floppy Controller") )
            return "HARDWARE_DEVICE_FLOPPY_CONTROLLER";
        if( name.equals("Floppy Drive") )
            return "HARDWARE_DEVICE_FLOPPY_DRIVE";
        if( name.equals("Harddisk Drive") )
            return "HARDWARE_DEVICE_HARDDISK_DRIVE";
        if( name.equals("HDAudio Device") )
            return "HARDWARE_DEVICE_HDAUDIO_DEVICE";
        if( name.equals("IDE Controller") )
            return "HARDWARE_DEVICE_IDE_CONTROLLER";
        if( name.equals("Keyboard") )
            return "HARDWARE_DEVICE_KEYBOARD";
        if( name.equals("Memory Bank") )
            return "HARDWARE_DEVICE_MEMORY_BANK";
        if( name.equals("Memory Module") )
            return "HARDWARE_DEVICE_MEMORY_MODULE";
        if( name.equals("Monitor") )
            return "HARDWARE_DEVICE_MONITOR";
        if( name.equals("Network Cable") )
            return "HARDWARE_DEVICE_NETWORK_CONTROLLER";
        if( name.equals("Optical Drive") )
            return "HARDWARE_DEVICE_OPTICAL_DRIVE";
        if( name.equals("PCI Device") )
            return "HARDWARE_DEVICE_PCI_DEVICE";
        if( name.equals("PCMCIA Controller") )
            return "HARDWARE_DEVICE_PCMCIA_CONTROLLER";
        if( name.equals("Pointing Device") )
            return "HARDWARE_DEVICE_POINTING_DEVICE";
        if( name.equals("Port Connector") )
            return "HARDWARE_DEVICE_PORT_CONNECTOR";
        if( name.equals("Printer") )
            return "HARDWARE_DEVICE_PRINTER";
        if( name.equals("Processor") )
            return "HARDWARE_DEVICE_PROCESSOR";
        if( name.equals("SCSI Controller") )
            return "HARDWARE_DEVICE_SCSI_CONTROLLER";
        if( name.equals("System Slot") )
            return "HARDWARE_DEVICE_SYSTEM_SLOT";
        if( name.equals("Tape Drive") )
            return "HARDWARE_DEVICE_TAPE_DRIVE";
        if( name.equals("USB Controller") )
            return "HARDWARE_DEVICE_USB_CONTROLLER";
        if( name.equals("USB Device") )
            return "HARDWARE_DEVICE_USB_DEVICE";
        if( name.equals("Video Controller") )
            return "HARDWARE_DEVICE_VIDEO_CONTROLLER";
        return "";
    }
}