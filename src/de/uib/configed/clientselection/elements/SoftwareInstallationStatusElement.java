package de.uib.configed.clientselection.elements;

import java.util.*;
import de.uib.configed.clientselection.*;
import de.uib.configed.*;
import de.uib.opsidatamodel.productstate.InstallationStatus;

public class SoftwareInstallationStatusElement extends GenericEnumElement
{
    public SoftwareInstallationStatusElement()
    {
        super( removeFirst(2, InstallationStatus.getLabels().toArray(new String[0])), new String[]{de.uib.opsidatamodel.OpsiProduct.NAME, "Installation Status"},
                 configed.getResourceValue("ClientSelectionDialog.softwareName"), configed.getResourceValue("InstallationStateTableModel.installationStatus") );
    }
}
