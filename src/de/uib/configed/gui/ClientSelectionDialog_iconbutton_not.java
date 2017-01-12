package de.uib.configed.gui;

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.Font;
import java.awt.Color;
import de.uib.configed.*;
import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.serializers.*;
import de.uib.configed.clientselection.elements.*;
import de.uib.utilities.logging.logging;
import de.uib.utilities.selectionpanel.JTableSelectionPanel;

/**
 * This dialog shows a number of options you can use to select specific clients.
 */
public class ClientSelectionDialog extends FGeneralDialog
{
    private static ClientSelectionDialog instance=null;
    private GroupLayout layout;
    private GroupLayout.SequentialGroup vGroup;
    private GroupLayout.SequentialGroup vHostGroup;
    private GroupLayout.ParallelGroup hGroupParenthesisClose;
    private GroupLayout.ParallelGroup hGroupParenthesisOpen;
    private GroupLayout.ParallelGroup hGroupRemoveBtn;
    private GroupLayout.ParallelGroup hGroupNegate;
    private GroupLayout.ParallelGroup hGroupConnections;
    private GroupLayout.ParallelGroup hGroupElements;
    private GroupLayout.ParallelGroup hGroupOperations;
    private GroupLayout.ParallelGroup hGroupData;
    private JPanel contentPane;
    private JComboBox newElementBox;
    private JTextField saveNameField;
    private JButton saveButton;
    private JLabel savedSearchLabel;
    private JComboBox savedSearchBox;
        
    private List<SelectElement> elements;
    private LinkedList<SimpleGroup> simpleElements;
    private List<SoftwareGroup> softwareElements;
    private List<HardwareGroup> hardwareElements;
    
    private SelectionManager manager;
    private JTableSelectionPanel selectionPanel;
    
    // The font colors of the logical (AND,OR,NOT) buttons
    private final Color selectedColor = Color.red;
    private final Color deselectedColor = Color.gray;
    
    private ClientSelectionDialog( JTableSelectionPanel selectionPanel )
    {
        super( null,
               configed.getResourceValue("ClientSelectionDialog.title")/*"Select clients"*/ +  " (" + Globals.APPNAME +")",
               false,
               new String[]{
                        configed.getResourceValue("GroupsManager.buttonSet"), 
                        configed.getResourceValue("GroupsManager.buttonClose") },
               700,620 );
        this.selectionPanel = selectionPanel;
        setDefaultCloseOperation( JDialog.HIDE_ON_CLOSE );
        manager = new SelectionManager("OpsiData");
        elements = new LinkedList<SelectElement>();
        simpleElements = new LinkedList<SimpleGroup>();
        softwareElements = new LinkedList<SoftwareGroup>();
        hardwareElements = new LinkedList<HardwareGroup>();
        init();
        pack();
    }
    
    public static ClientSelectionDialog getInstance( JTableSelectionPanel selectionPanel )
    {
        if( instance == null )
            instance = new ClientSelectionDialog(selectionPanel);
        return instance;
    }
    
    public void refreshDialog()
    {
        for( SimpleGroup group: simpleElements )
        {
            if( group.element instanceof GroupElement )
            {
                JComboBox box = (JComboBox) group.dataComponent;
                box.removeAllItems();
                for( SelectElement element: manager.getElements() )
                    box.addItem(element.toString() );
                box.addItem( configed.getResourceValue("ClientSelectionDialog.softwareGroup") );
                for( String data: group.element.getEnumData( group.element.supportedOperations().get(0) ) )
                    box.addItem(data);
            }
        }
    }
    
    protected void doAction1()
    {
        collectData();
        
        List<String> clients = manager.selectClients();
        if( clients == null )
            return;
        logging.debug( this, clients.toString() );
        selectionPanel.setSelectedValues( clients );
    }
    
    private void init()
    {
        contentPane = new JPanel();
        layout = new GroupLayout(contentPane);
        contentPane.setLayout( layout );
        //layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHonorsVisibility(false);
        
        GroupLayout.SequentialGroup vMainGroup = layout.createSequentialGroup();
        GroupLayout.ParallelGroup hMainGroup = layout.createParallelGroup();
        
        vGroup = layout.createSequentialGroup();
        vHostGroup = layout.createSequentialGroup();
        vGroup.addGroup( vHostGroup );
        vMainGroup.addGroup( vGroup );
        layout.setVerticalGroup( vMainGroup );
        hGroupParenthesisClose = layout.createParallelGroup();
        hGroupParenthesisOpen = layout.createParallelGroup();
        hGroupRemoveBtn = layout.createParallelGroup();
        hGroupNegate = layout.createParallelGroup();
        hGroupConnections = layout.createParallelGroup();
        hGroupElements = layout.createParallelGroup();
        hGroupOperations = layout.createParallelGroup();
        hGroupData = layout.createParallelGroup();
        hMainGroup.addGroup( layout.createSequentialGroup()
            .addGroup(hGroupParenthesisOpen)
            .addGroup(hGroupNegate)
            .addGroup(hGroupElements)
            .addGroup(hGroupOperations)
            .addGroup(hGroupData)
            .addGroup(hGroupParenthesisClose)
            .addGroup(hGroupConnections)
            .addGroup(hGroupRemoveBtn) );
        layout.setHorizontalGroup( hMainGroup );
        
        JLabel hostLabel = new JLabel(configed.getResourceValue("ClientSelectionDialog.hostGroup")+":");
        hostLabel.setIcon( Globals.createImageIcon( "images/client_small.png", "Client" ) );
        GroupLayout.ParallelGroup vRow = layout.createParallelGroup(); 
        vRow.addComponent( hostLabel, GroupLayout.Alignment.CENTER, 20, 20, 20 );
        vHostGroup.addGroup( vRow );
        hGroupElements.addComponent( hostLabel );
        contentPane.add( hostLabel );
        
        newElementBox = new JComboBox( new String[] {configed.getResourceValue("ClientSelectionDialog.newElementsBox")} );
        for( SelectElement element: manager.getElements() )
        {
            newElementBox.addItem( element.toString() );
            elements.add( element );
            SimpleGroup group = createSimpleGroup(element, true);
            simpleElements.add(group);
        }
        simpleElements.getLast().connectionType.setVisible(false);
        newElementBox.addItem( configed.getResourceValue("ClientSelectionDialog.softwareGroup") );
        List<String> hardwareList = new LinkedList( manager.getHardwareList().keySet() );
        Collections.sort( hardwareList );
        for( String hardware: hardwareList )
            newElementBox.addItem( hardware );
        
        newElementBox.setMaximumSize( new Dimension( newElementBox.getPreferredSize().width, newElementBox.getPreferredSize().height ) );
        newElementBox.addActionListener( new AddElementListener() );
        vMainGroup.addGap( 10 );
        vMainGroup.addComponent( newElementBox );
        hMainGroup.addComponent( newElementBox );
        contentPane.add( newElementBox );
        
        saveNameField = new JTextField();
        saveNameField.setMaximumSize( new Dimension( saveNameField.getMaximumSize().width, saveNameField.getPreferredSize().height ) );
        saveButton = new JButton( configed.getResourceValue("ClientSelectionDialog.saveSearchButton") );
        saveButton.setMaximumSize( new Dimension( saveButton.getMaximumSize().width, saveButton.getPreferredSize().height ) );
        vMainGroup.addGap( 10 );
        saveButton.addActionListener( new SaveButtonListener() );
        GroupLayout.SequentialGroup saveHGroup = layout.createSequentialGroup();
        saveHGroup.addComponent( saveNameField, 150, 150, 150 );
        saveHGroup.addComponent( saveButton );
        hMainGroup.addGroup( saveHGroup );
        GroupLayout.ParallelGroup saveVGroup = layout.createParallelGroup();
        saveVGroup.addComponent( saveNameField, GroupLayout.Alignment.CENTER );
        saveVGroup.addComponent( saveButton, GroupLayout.Alignment.CENTER );
        vMainGroup.addGroup( saveVGroup );
        
        savedSearchLabel = new JLabel( configed.getResourceValue("ClientSelectionDialog.chooseSavedSearch") );
        savedSearchLabel.setMaximumSize( new Dimension( savedSearchLabel.getMaximumSize().width, savedSearchLabel.getPreferredSize().height ) );
        Vector<String> availableSearches = manager.getSavedSearches();
        availableSearches.add(0, "");
        savedSearchBox = new JComboBox( availableSearches );
        savedSearchBox.setMaximumSize( new Dimension( savedSearchBox.getMaximumSize().width, 20 ) );
        savedSearchBox.addActionListener( new SavedBoxListener() );
        vMainGroup.addGap(10);
        GroupLayout.SequentialGroup savedHGroup = layout.createSequentialGroup();
        savedHGroup.addComponent( savedSearchLabel );
        savedHGroup.addComponent( savedSearchBox, 150, 150, 150 );
        hMainGroup.addGroup( savedHGroup );
        GroupLayout.ParallelGroup savedVGroup = layout.createParallelGroup();
        savedVGroup.addComponent( savedSearchLabel, GroupLayout.Alignment.CENTER );
        savedVGroup.addComponent( savedSearchBox, GroupLayout.Alignment.CENTER, 20, 20, 20 );
        vMainGroup.addGroup( savedVGroup );
        
        softwareElements.add( createSoftwareGroup() );
        scrollpane.getViewport().add(contentPane);
    }
    
    private SimpleGroup createSimpleGroup( SelectElement element, boolean toHostGroup )
    {
        SimpleGroup result = new SimpleGroup();
        result.element = element;
        String[] operations = element.supportedOperations().toArray(new String[0]);
        if( operations.length == 0 )
        {
            logging.warning("Elements without any operations are not supported");
            return null;
        }
        
        if( toHostGroup )
        {
            result.removeButton = new IconButton(configed.getResourceValue("ClientSelectionDialog.removeAction"), 
                                  "images/user-trash.png", "images/user-trash_over.png", "images/user-trash.png", "images/user-trash_disabled.png") ;
            result.removeButton.addActionListener( new RemoveButtonListener() );
            result.removeButton.setMaximumSize( new Dimension( result.removeButton.getPreferredSize().width, result.removeButton.getPreferredSize().height ) );
        }
        result.negateButton = new IconButton( "NOT", "images/boolean_not_disabled.png", "images/boolean_not_over.png", "images/boolean_not.png", null);
        result.negateButton.setActivated( false );
        result.negateButton.setMaximumSize( new Dimension( result.negateButton.getMaximumSize().width, result.negateButton.getPreferredSize().height ) );
        result.negateButton.addActionListener( new NotButtonListener() );
        result.connectionType = new AndOrSelectButton();
        result.connectionType.addActionListener( new AndOrButtonListener() );
        result.connectionType.setMaximumSize( new Dimension( result.connectionType.getMaximumSize().width, result.connectionType.getPreferredSize().height ) );
        result.elementLabel = new JLabel( element.toString() );
        result.elementLabel.setMaximumSize( new Dimension( result.elementLabel.getMaximumSize().width, result.connectionType.getPreferredSize().height ) );
        if( operations.length > 1 )
            result.operationComponent = new JComboBox( operations );
        else
            result.operationComponent = new JLabel( operations[0], JLabel.CENTER );
        result.operationComponent.setMaximumSize( new Dimension( result.operationComponent.getMaximumSize().width, result.operationComponent.getPreferredSize().height ) );
        result.dataComponent = new JLabel(); // to reserve the place
        result.dataComponent.setMaximumSize( new Dimension( result.dataComponent.getMaximumSize().width, result.dataComponent.getPreferredSize().height ) );
        result.openParenthesis = new JLabel("(");
        Font font = result.openParenthesis.getFont().deriveFont( Font.BOLD, 18.0f );
        result.openParenthesis.setFont(font);
        result.openParenthesis.setForeground( selectedColor );
        result.openParenthesis.setVisible(false);
        result.closeParenthesis = new JLabel(")");
        result.closeParenthesis.setFont( font );
        result.closeParenthesis.setForeground( selectedColor );
        result.closeParenthesis.setVisible( false );
        
        result.vRow = layout.createParallelGroup();
        if( toHostGroup )
            result.vRow.addComponent( result.removeButton, GroupLayout.Alignment.CENTER );
        result.vRow.addComponent( result.negateButton, GroupLayout.Alignment.CENTER );
        result.vRow.addComponent( result.connectionType, GroupLayout.Alignment.CENTER );
        result.vRow.addComponent( result.elementLabel, GroupLayout.Alignment.CENTER );
        result.vRow.addComponent( result.operationComponent, GroupLayout.Alignment.CENTER );
        result.vRow.addComponent( result.dataComponent, GroupLayout.Alignment.CENTER );
        result.vRow.addComponent( result.openParenthesis, GroupLayout.Alignment.CENTER );
        result.vRow.addComponent( result.closeParenthesis, GroupLayout.Alignment.CENTER );
        
        if( toHostGroup )
            vHostGroup.addGroup( result.vRow );
        else
            vGroup.addGroup( result.vRow );
        
        if( toHostGroup )
            hGroupRemoveBtn.addComponent( result.removeButton );
        hGroupNegate.addComponent( result.negateButton );
        hGroupConnections.addComponent( result.connectionType, 100, 100, 100 );
        hGroupElements.addComponent( result.elementLabel );
        hGroupOperations.addComponent( result.operationComponent, 65, 70, 70 );
        hGroupData.addComponent( result.dataComponent, 100, 100, Short.MAX_VALUE );
        hGroupParenthesisOpen.addComponent( result.openParenthesis );
        hGroupParenthesisClose.addComponent( result.closeParenthesis );
        
        if( toHostGroup )
            contentPane.add( result.removeButton );
        contentPane.add( result.negateButton );
        contentPane.add( result.connectionType );
        contentPane.add( result.elementLabel );
        contentPane.add( result.operationComponent );
        contentPane.add( result.dataComponent );
        contentPane.add( result.openParenthesis );
        contentPane.add( result.closeParenthesis );
        
        if( operations.length > 1 )
        {
            ((JComboBox) result.operationComponent).addActionListener( new SelectOperationListener() );
            addDataComponent( result, ((JComboBox) result.operationComponent).getSelectedItem().toString() );
        }
        else if( operations.length == 1 ) {
            addDataComponent( result, ((JLabel) result.operationComponent).getText() );
        }
        
        return result;
    }
    
    private SoftwareGroup createSoftwareGroup()
    {
        SoftwareGroup result = new SoftwareGroup();
        result.removeButton = new IconButton(configed.getResourceValue("ClientSelectionDialog.removeAction"), 
                                  "images/user-trash.png", "images/user-trash_over.png", "images/user-trash.png", "images/user-trash_disabled.png") ;
        result.removeButton.setMaximumSize( new Dimension( result.removeButton.getPreferredSize().width, result.removeButton.getPreferredSize().height ) );
        result.removeButton.addActionListener( new RemoveButtonListener() );
        result.topLabel = new JLabel(configed.getResourceValue("ClientSelectionDialog.softwareGroup") + ":");
        result.topLabel.setIcon( Globals.createImageIcon("images/package.png", configed.getResourceValue("ClientSelectionDialog.softwareGroup") ) );
        result.topLabel.setMaximumSize( new Dimension( result.topLabel.getMaximumSize().width, result.removeButton.getPreferredSize().height ) );
        result.andLabel = new JLabel( "AND", JLabel.CENTER );
        result.andLabel.setForeground( selectedColor );
        GroupLayout.ParallelGroup vRow = layout.createParallelGroup();
        vRow.addComponent( result.topLabel, GroupLayout.Alignment.CENTER, 20,20,20 );
        vRow.addComponent( result.removeButton, GroupLayout.Alignment.CENTER );
        vRow.addComponent( result.andLabel, GroupLayout.Alignment.CENTER );
        vGroup.addGroup( vRow );
        hGroupRemoveBtn.addComponent( result.removeButton );
        hGroupElements.addComponent( result.topLabel );
        hGroupConnections.addComponent( result.andLabel, GroupLayout.Alignment.CENTER );
        contentPane.add( result.topLabel );
        contentPane.add( result.andLabel );
        
        result.groupList = new LinkedList<SimpleGroup>();
        
        result.groupList.add( createSimpleGroup( new SoftwareNameElement(), false ) );
        result.groupList.add( createSimpleGroup( new SoftwareInstallationStatusElement(), false ) );
        result.groupList.add( createSimpleGroup( new SoftwareTargetConfigurationElement(), false ) );
        result.groupList.add( createSimpleGroup( new SoftwareActionResultElement(), false ) );
        result.groupList.add( createSimpleGroup( new SoftwareRequestElement(), false ) );
        result.groupList.add( createSimpleGroup( new SoftwareActionProgressElement(), false ) );
        result.groupList.add( createSimpleGroup( new SoftwareLastActionElement(), false ) );
        result.groupList.add( createSimpleGroup( new SoftwareVersionElement(), false ) );
        result.groupList.add( createSimpleGroup( new SoftwareModificationTimeElement(), false ) );
        result.groupList.getLast().connectionType.setVisible(false);
        
        return result;
    }
    
    private HardwareGroup createHardwareGroup( String hardware )
    {
        HardwareGroup result = new HardwareGroup();
        result.removeButton = new IconButton(configed.getResourceValue("ClientSelectionDialog.removeAction"), 
                                  "images/user-trash.png", "images/user-trash_over.png", "images/user-trash.png", "images/user-trash_disabled.png") ;
        result.removeButton.setMaximumSize( new Dimension( result.removeButton.getPreferredSize().width, result.removeButton.getPreferredSize().height ) );
        result.removeButton.addActionListener( new RemoveButtonListener() );
        result.topLabel = new JLabel(hardware);
        result.topLabel.setIcon( Globals.createImageIcon( "images/hwaudit.png", "Hardware" ) );
        result.topLabel.setMaximumSize( new Dimension( result.topLabel.getMaximumSize().width, result.removeButton.getPreferredSize().height ) );
        result.andLabel = new JLabel( "AND", JLabel.CENTER );
        GroupLayout.ParallelGroup vRow = layout.createParallelGroup();
        vRow.addComponent( result.topLabel, GroupLayout.Alignment.CENTER, 20,20,20 );
        vRow.addComponent( result.removeButton, GroupLayout.Alignment.CENTER );
        vRow.addComponent( result.andLabel, GroupLayout.Alignment.CENTER );
        vGroup.addGroup( vRow );
        hGroupRemoveBtn.addComponent( result.removeButton );
        hGroupElements.addComponent( result.topLabel );
        hGroupConnections.addComponent( result.andLabel, GroupLayout.Alignment.CENTER );
        contentPane.add( result.removeButton );
        contentPane.add( result.topLabel );
        contentPane.add( result.andLabel );
        
        result.groupList = new LinkedList<SimpleGroup>();
        List<SelectElement> elements = manager.getHardwareList().get(hardware);
        for( SelectElement element: elements )
        {
            result.groupList.add( createSimpleGroup( element, false ) );
        }
        result.groupList.getLast().connectionType.setVisible(false);
        return result;
    }
    
    private OperationData getOperation( SimpleGroup group )
    {
        String operationString;
        if( group.operationComponent instanceof JComboBox )
            operationString = (String) ((JComboBox) group.operationComponent).getSelectedItem();
        else
            operationString = ((JLabel) group.operationComponent).getText();
        
        if( operationString.equals("") )
            return null;
        
        Object data = null;
        SelectData.DataType type = group.element.dataNeeded( operationString );
        switch( type )
        {
            case DoubleType:
            case TextType:
                String text = ((JTextField) group.dataComponent).getText();
                if( text.isEmpty() )
                    return null;
                data = text;
                break;
            case IntegerType:
                Integer value = (Integer) ((JSpinner) group.dataComponent).getValue();
                if( value == 0 )
                    return null;
                data = value;
                break;
            case BigIntegerType:
                Long value2 = (Long) ((SpinnerWithExt) group.dataComponent).getValue();
                if( value2 == 0 )
                    return null;
                data = value2;
                break;
            case EnumType:
                String textEnum = ((JComboBox) group.dataComponent).getSelectedItem().toString();
                if( textEnum.isEmpty() )
                    return null;
                data = textEnum;
                break;
            case NoneType:
        }
        
        return new OperationData( group.element.getElementName(), group.element.getPathArray(), operationString, new SelectData( data, type ), null );
    }   
    
    private SelectionManager.ConnectionStatus getStatus( SimpleGroup group )
    {
        SelectionManager.ConnectionStatus conStatus;
        boolean andSelected = group.connectionType.isAndSelected();
        boolean notSelected = group.negateButton.isActivated();
        if( andSelected )
        {
            if( notSelected )
                conStatus = SelectionManager.ConnectionStatus.AndNot;
            else
                conStatus = SelectionManager.ConnectionStatus.And;
        }
        else
        {
            if( notSelected )
                conStatus = SelectionManager.ConnectionStatus.OrNot;
            else
                conStatus = SelectionManager.ConnectionStatus.Or;
        }
        return conStatus;
    }
    
    private void removeGroup( SimpleGroup group )
    {
        if( group.removeButton != null )
            contentPane.remove( group.removeButton );
        contentPane.remove( group.negateButton );
        contentPane.remove( group.connectionType );
        contentPane.remove( group.elementLabel );
        contentPane.remove( group.operationComponent );
        if( group.dataComponent != null )
            contentPane.remove( group.dataComponent );
        contentPane.remove( group.openParenthesis );
        contentPane.remove( group.closeParenthesis );
        contentPane.revalidate();
        contentPane.repaint();
    }
    
    private void showParenthesesForGroup( LinkedList<SimpleGroup> groups )
    {
        boolean inOr=false;
        for( SimpleGroup group: groups )
        {
            group.openParenthesis.setVisible(false);
            group.closeParenthesis.setVisible(false);
            if( getOperation(group) == null )
                continue;
            if( group.connectionType.isAndSelected() && inOr )
            {
                inOr = false;
                group.closeParenthesis.setVisible(true);
            }
            if( group.connectionType.isOrSelected() && !inOr )
            {
                inOr = true;
                group.openParenthesis.setVisible(true);
            }
        }
        if( inOr )
        {
            SimpleGroup group = groups.getLast();
            group.closeParenthesis.setVisible(true);
        }
    }
    
    private void buildParentheses()
    {         
        showParenthesesForGroup( simpleElements );
        for( SoftwareGroup group: softwareElements )
            showParenthesesForGroup( group.groupList );
        for( HardwareGroup group: hardwareElements )
            showParenthesesForGroup( group.groupList );
    }
    
    private void addDataComponent( SimpleGroup sourceGroup, String operation )
    {
        if( operation.equals("") )
            return;
        switch( sourceGroup.element.dataNeeded( operation ) )
        {
            case TextType:
            case DoubleType:
                JTextField textField = new JTextField();
                textField.setColumns(10);
                textField.setToolTipText(/*"Use * as wildcard"*/configed.getResourceValue("ClientSelectionDialog.textInputToolTip") );
                textField.getDocument().addDocumentListener( new ValueChangeListener() );
                sourceGroup.dataComponent = textField;
                break;
            case EnumType:
                JComboBox box = new JComboBox( sourceGroup.element.getEnumData( operation ) );
                box.setEditable( true );
                box.setToolTipText( configed.getResourceValue("ClientSelectionDialog.textInputToolTip") );
                box.setSelectedItem("");
                box.addActionListener( new ValueChangeListener() );
                sourceGroup.dataComponent = box;
                break;
            case IntegerType:
                JSpinner spinner = new JSpinner();
                spinner.addChangeListener( new ValueChangeListener() );
                sourceGroup.dataComponent = spinner;
                break;
            case BigIntegerType:
                SpinnerWithExt swx = new SpinnerWithExt();
                swx.addChangeListener( new ValueChangeListener() );
                sourceGroup.dataComponent = swx;
                break;
            case NoneType:
                return;
        }
        sourceGroup.dataComponent.setMaximumSize( new Dimension( sourceGroup.dataComponent.getMaximumSize().width, sourceGroup.dataComponent.getPreferredSize().height ) );
        sourceGroup.vRow.addComponent( sourceGroup.dataComponent, GroupLayout.Alignment.CENTER );
        hGroupData.addComponent( sourceGroup.dataComponent, 100, 100, Short.MAX_VALUE );
    }
    
    private void collectData()
    {
        manager.clearOperations();
        for( SimpleGroup group: simpleElements )
        {
            OperationData op = getOperation(group);
            if( op != null )
                manager.addOperation( op, getStatus(group) );
        }
        
        for( SoftwareGroup software: softwareElements )
        {
            List<OperationData> operations = new LinkedList<OperationData>();
            List<SelectionManager.ConnectionStatus> statusList = new LinkedList<SelectionManager.ConnectionStatus>();
            
            for( SimpleGroup group: software.groupList )
            {
                OperationData op = getOperation( group );
                if( op != null )
                {
                    operations.add(op);
                    statusList.add( getStatus(group) );
                }
            }
            
            if( !operations.isEmpty() )
                manager.addGroupOperation( "Software", SelectionManager.ConnectionStatus.And, operations, statusList );
        }
        
        for( HardwareGroup hardware: hardwareElements )
        {
            List<OperationData> operations = new LinkedList<OperationData>();
            List<SelectionManager.ConnectionStatus> statusList = new LinkedList<SelectionManager.ConnectionStatus>();
            
            for( SimpleGroup group: hardware.groupList )
            {
                OperationData op = getOperation( group );
                if( op != null )
                {
                    operations.add(op);
                    statusList.add( getStatus(group) );
                }
            }
            
            if( !operations.isEmpty() )
                manager.addGroupOperation( "Hardware", SelectionManager.ConnectionStatus.And, operations, statusList );
        }
    }

    private class SimpleGroup
    {
        public SelectElement element;
        public IconButton removeButton=null;
        public IconButton negateButton;
        public AndOrSelectButton connectionType;
        public JLabel elementLabel;
        public JComponent operationComponent; // may be JLabel or JComboBox
        public JComponent dataComponent;
        public GroupLayout.ParallelGroup vRow;
        public JLabel openParenthesis;
        public JLabel closeParenthesis;
    }
    
    private class SoftwareGroup
    {
        public IconButton removeButton;
        public JLabel topLabel=null;
        public JLabel andLabel;
        public LinkedList<SimpleGroup> groupList;
    }
    
    private class HardwareGroup
    {
        public IconButton removeButton;
        public JLabel topLabel;
        public JLabel andLabel;
        public LinkedList<SimpleGroup> groupList;
    }
    
    private class RemoveButtonListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            Iterator<SimpleGroup> simpleIterator = simpleElements.iterator();
            while( simpleIterator.hasNext() )
            {
                SimpleGroup group = simpleIterator.next();
                if( group.removeButton == e.getSource() )
                {
                    removeGroup( group );
                    simpleIterator.remove();
                    if( !simpleElements.isEmpty() )
                        simpleElements.getLast().connectionType.setVisible(false);
                    buildParentheses();
                    return;
                }
            }
            
            Iterator<SoftwareGroup> softwareIterator = softwareElements.iterator();
            while( softwareIterator.hasNext() )
            {
                SoftwareGroup group = softwareIterator.next();
                if( group.removeButton == e.getSource() )
                {
                    contentPane.remove( group.topLabel );
                    contentPane.remove( group.removeButton );
                    contentPane.remove( group.andLabel );
                    for( SimpleGroup simple: group.groupList )
                        removeGroup( simple );
                    softwareIterator.remove();
                    buildParentheses();
                    return;
                }
            }
            
            Iterator<HardwareGroup> hardwareIterator = hardwareElements.iterator();
            while( hardwareIterator.hasNext() )
            {
                HardwareGroup group = hardwareIterator.next();
                if( group.removeButton == e.getSource() )
                {
                    contentPane.remove( group.topLabel );
                    contentPane.remove( group.removeButton );
                    contentPane.remove( group.andLabel );
                    for( SimpleGroup simple: group.groupList )
                        removeGroup( simple );
                    contentPane.revalidate();
                    contentPane.repaint();
                    hardwareIterator.remove();
                    buildParentheses();
                    return;
                }
            }
        }
    }
    
    private class AddElementListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            int index = newElementBox.getSelectedIndex();
            if( index == 0 )
                return;
            index--;
            List<SelectElement> elements = manager.getElements();
            if( index < elements.size() )
            {
                if( !simpleElements.isEmpty() )
                    simpleElements.getLast().connectionType.setVisible(true);
                simpleElements.add( createSimpleGroup( elements.get(index), true ) );
                simpleElements.getLast().connectionType.setVisible(false);
            }
            else if( index == elements.size() )
                softwareElements.add( createSoftwareGroup() );
            else
                hardwareElements.add( createHardwareGroup( newElementBox.getSelectedItem().toString() ) );
            
            contentPane.revalidate();
            contentPane.repaint();
            newElementBox.setSelectedIndex(0);
        }
    }
    
    private class SelectOperationListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            JComponent source=null;
            SimpleGroup sourceGroup=null;
            for( SimpleGroup group: simpleElements )
            {
                if( group.operationComponent == e.getSource() )
                {
                    source = group.operationComponent;
                    sourceGroup = group;
                    break;
                }
            }
            if( source == null )
            {
                for( SoftwareGroup group: softwareElements )
                {
                    for( SimpleGroup simple: group.groupList )
                    {
                        if( simple.operationComponent == e.getSource() )
                        {
                            source = simple.operationComponent;
                            sourceGroup = simple;
                            break;
                        }
                    }
                    if( source != null )
                        break;
                }
            }
            if( source == null )
            {
                for( HardwareGroup group: hardwareElements )
                {
                    for( SimpleGroup simple: group.groupList )
                    {
                        if( simple.operationComponent == e.getSource() )
                        {
                            source = simple.operationComponent;
                            sourceGroup = simple;
                            break;
                        }
                    }
                    if( source != null )
                        break;
                }
            }
            if( source == null )
                return;
                
            if( sourceGroup.dataComponent != null )
            {
                contentPane.remove( sourceGroup.dataComponent );
                sourceGroup.dataComponent = null;
            }
                
            String op="";
            if( source instanceof JComboBox )
                op = ((JComboBox) source).getSelectedItem().toString();
            else if( source instanceof JLabel )
                op = ((JLabel) source).getText();
            addDataComponent( sourceGroup, op );
            
            buildParentheses();
            
            contentPane.revalidate();
            contentPane.repaint();
        }
    }
    
    private class NotButtonListener implements ActionListener
    {
        public void actionPerformed( ActionEvent event )
        {
            if( !(event.getSource() instanceof IconButton) )
                return;
            IconButton button = (IconButton) event.getSource();
            button.setActivated( !button.isActivated() );
        }
    }
    
    private class AndOrButtonListener implements ActionListener
    {
        public void actionPerformed( ActionEvent event )
        {
            buildParentheses();
        }
    }
    
    private class ValueChangeListener implements ActionListener, ChangeListener, DocumentListener
    {
        public void actionPerformed( ActionEvent event )
        {
            buildParentheses();
        }
        
        public void stateChanged( ChangeEvent e )
        {
            buildParentheses();
        }
        
        public void changedUpdate( DocumentEvent e )
        {
            buildParentheses();
        }
        
        public void insertUpdate( DocumentEvent e )
        {
            buildParentheses();
        }
        
        public void removeUpdate( DocumentEvent e )
        {
            buildParentheses();
        }
    }
    
    private class SpinnerWithExt extends JPanel
    {
        private JSpinner spinner;
        private JComboBox box;
        
        public SpinnerWithExt()
        {
            spinner = new JSpinner( new SpinnerNumberModel( (Number) new Long(0), Long.MIN_VALUE, Long.MAX_VALUE, new Long(1) ) );
            spinner.setMinimumSize( new Dimension(0,0) );
            box = new JComboBox( new String[] {"", "k", "M", "G", "T"} );
            box.setMinimumSize( new Dimension(50, 0) );
            GroupLayout layout = new GroupLayout(this);
            layout.setVerticalGroup( layout.createParallelGroup()
                                           .addComponent( spinner )
                                           .addComponent( box ) );
            layout.setHorizontalGroup( layout.createSequentialGroup()
                                             .addComponent( spinner )
                                             .addComponent( box ) );
            setLayout( layout );
            add( spinner );
            add( box );
        }
        
        public long getValue()
        {
            long value = (Long) spinner.getValue();
            for( int i=0; i<box.getSelectedIndex(); i++ )
                value *= 1024l;
            return value;
        }
        
        public void addChangeListener( ChangeListener listener )
        {
            spinner.addChangeListener( listener );
        }
    }
    
    private class SaveButtonListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            String text = saveNameField.getText();
            if( text.isEmpty() )
            {
                logging.debug( this, "Name for search is missing." );
                return;
            }
            collectData();
            manager.saveSearch( text );
            savedSearchBox.removeAllItems();
            savedSearchBox.addItem( "" );
            for( String search: manager.getSavedSearches() )
                savedSearchBox.addItem( search );
        }
    }
    
    private class SavedBoxListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            manager.loadSearch( (String) savedSearchBox.getSelectedItem() );
        }
    }
}