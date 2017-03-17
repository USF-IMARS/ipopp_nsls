/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
/******************************************************************************
*
*  NISGS/NSLS
*
*  History:
*
*  14-Feb-14,   Dragged out of ControlPanel.java

*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.console;
import java.util.Iterator;

public class CoreService
{
    private String pathToScript;
    private String label;
    private String slsName;

    /**
     * path from drl root to JSW control script
     */
    public String getPath() { return pathToScript; }
    /**
     * label to be displayed when referring to this service
     */
    public String getLabel() { return label; }
    /**
     * SLS service name
     */
    public String getSLSname() { return slsName; }

    public CoreService (String label, String pathToScript, String slsName) {
	this.label = label;
	this.pathToScript = pathToScript;
	this.slsName = slsName;
    }

    // Array of core service control objects
    public static CoreService coreService[] = {
	new CoreService("Logging",
			"/nsls/jsw/bin/nsls-server.sh",
			""),
	new CoreService("Ancillary Retriever & Registration",
			"/is/jsw/bin/is-retriever.sh",
			"retriever"),
	new CoreService("File Management",
			"/is/jsw/bin/is-deleter.sh",
			"deleter"),
	new CoreService("Database Maintenance",
			"/dsm/jsw/bin/validatedb.sh",
			"ValidateDB"),
	new CoreService("PDS Ingest",
			"/dsm/jsw/bin/pdsmover.sh",
			"PdsMover"),
	new CoreService("xDR Ingest",
			"/tools/mover.sh",
			"RDRMover")
    };

    public static int count() { return coreService.length; }

    public static CoreService getBySLSName(String sn)
    {
	CoreService result = null;
	for(CoreService cs : coreService)
	    if(cs.slsName.equals(sn)) {
		result = cs;
		break;
	    }
	return result;
    }
}
