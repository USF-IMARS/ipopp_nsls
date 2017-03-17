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
*  17-Oct-06, 	Original version.
*  06-Jun-07, 	Changed satellite name location and color.
*  07-Mar-12,   XML config, other hacks for NPP and beyond.
*  13-Dec-13,   forked from ProcessingMonitor as default_config.file editor
*  20-Jan-14,   merged into overall Control Panel
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.console;
import gov.nasa.gsfc.nisgs.nsls.*;
import gov.nasa.gsfc.nisgs.nsls.util.*;
import gov.nasa.gsfc.nisgs.nsls.console.SimpleProgressMonitor;
import gov.nasa.gsfc.nisgs.nsls.console.CoreService;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import java.text.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
// Importing crap from interp for reading XML
import  org.w3c.dom.Node;
import  org.w3c.dom.Element;
import  org.w3c.dom.NodeList;

@SuppressWarnings("unchecked")
public class ControlPanel extends JFrame {
    // States the control panel can be in
    private enum PanelMode {
	MONITOR,		// Not on localhost, monitor only
	CONTROL,		// Start/stop IPOPP and stations
	EDITOR			// Edit configuration
    };
    private static String controlTitle = "IPOPP Dashboard: Process Monitor";
    private static String editorTitle = "IPOPP Dashboard: Configuration Editor";
    // Putting this here because "inner classes cannot have static declarations"
    private static final Border litBorder = BorderFactory.createLineBorder(Color.yellow, 2);

    // Good sizes for Station boxes; can be changed in config file
    private static int bestHeight = 78;
    private static int bestWidth = 115;

    protected static JFrame frame;
    protected static String serverVersion = null;
    private static final String USAGE = "Usage: ControlPanel [-processingMonitor pm.xml] [-configuration default_config.file] [-stationRoot ncs/stations/dir] [-topTabOnNewPass] [<server-host:port>]";
    // Two maps: one for event dispatch (name x group) => List
    // one for default_config.file (directory-name/pattern) => Station
    // Used for event dispatch - one nsls event may dispatch to
    // more than one Station display object
    //private StringMap monitors = new StringMap();
    private Map<String, List<Station>> event2station
	= new HashMap<String, List<Station>> ();
    private Map<String, List<Station>> dir2station
	= new HashMap<String, List<Station>> ();
    private JTabbedPane tabbedPane;
    private ArrayList<MonitorPanel> monitorPanels = new ArrayList<MonitorPanel>();
    private File defaultConfigFile = null;
    private File tilelistConfigFile = null;
    private File ncsStationDir = null;
    private File toolsDir = null;    
    private File ipoppRootDir = null;

    // Remembers whether or not we have a console
    private boolean haveConsole = true;

    // Global boolean flags controlling dashboard modes
    /**
     * Remembers whether dashboard was launched connected to localhost
     * (and thus editor and commands will work)
     */
    private boolean isLocalhost;

    private boolean topTabOnNewPass = false;
    /**
     * Are we in editor mode?
     */
    private boolean isEditor;

    // Basic menu structure - items remain and are greyed as needed
    private JMenu modeMenu;
    private JMenuItem processMonitorItem;
    private JMenuItem configurationEditorItem;
    private JMenu actionsMenu;
    private JMenuItem startServicesItem;
    private JMenuItem stopServicesItem;
    private JMenuItem startSPAsItem;
    private JMenuItem stopSPAsItem;
    private JMenuItem checkIPOPPServicesItem;
    private JMenuItem resetIPOPPItem;
    private JMenuItem configureProjectionItem;
    private JMenuItem configureTileItem;
    //private JMenuItem installSPAItem;
    private JMenuItem saveConfigurationItem;

    private CustomLabel statusField;
    public CustomLabel getStatusField() { return statusField; }

    // InfoWindow (pop-up for more data per station).  There will be at most
    // one visible InfoWindow at a time
    InfoWindow infoWindow = null;

    // Global counter used for animation
    private int count = 0;

    // Global flags used to control background updating
    private boolean keepUpdating = true;
    private boolean updateNow = false;
    private boolean updateWithMonitor = false;

    // the background update Thread
    Thread backgroundUpdater = null;

    private static final long serialVersionUID = 1L;


  /****************************************************************************
  * main.
  ****************************************************************************/
  public static void main (String[] args) throws Exception {
      HostPort serverAddr = null;
      boolean localHostDefault = true;
    String pmfile = null;
    String configfile = null;
    String tilelist = null;
    String stationroot = null;
    boolean topTabOnNewPass = false;
    try {
      int x = 0;
      while (x < args.length) {
        String arg = args[x++];
	if(arg.equals("-processingMonitor")) {
	    pmfile = args[x++];
	}
	else if(arg.equals("-configuration")) {
	  configfile = args[x++];
	}
	else if(arg.equals("-tilelist")) {
		  tilelist = args[x++];
		}
	else if(arg.equals("-stationRoot")) {
	  stationroot = args[x++];
	}
	else if(arg.equals("-topTabOnNewPass")) {
	    topTabOnNewPass = true;
	}
	else {
	    if (serverAddr == null) {
		serverAddr = new HostPort(arg);
		localHostDefault = false;
		continue;
	    }
	    throw new Exception("unexpected argument: " + arg);
	}
      }
    } catch (Exception e) {
      System.out.println(USAGE);
      System.exit(0);
    }
    // All you have to do here is spin up our connection and create the ControlPanel
    // 
    if(serverAddr == null) {
	serverAddr = new HostPort("localhost:3500");
    }
    ControlPanel panel = new ControlPanel(pmfile, configfile, tilelist, stationroot, localHostDefault, false);
    panel.topTabOnNewPass = topTabOnNewPass;

    DashboardServerHandler dashboardServerHandler = new DashboardServerHandler(serverAddr, panel, panel.getStatusField());
    Util.startThread(dashboardServerHandler);
    // Still need to GUI-connect dashboardServerHandler...
  }

    /****************************************************************************
     * ControlPanel.
     ****************************************************************************/
    public ControlPanel (String monConfig, String defaultConfig, String tilelistConfig, String stationRoot, boolean isLocalhost, boolean haveConsole) throws Exception {

	this.isLocalhost = isLocalhost;
	this.haveConsole = haveConsole;
	isEditor = false;

	// Set up the ncsStationDir and friends
	if(stationRoot != null) {
	    ncsStationDir = new File(stationRoot);
	    // Just to make things pretty, canonicalize the path
	    ncsStationDir = ncsStationDir.getCanonicalFile();
	    if(!ncsStationDir.isDirectory()) {
		throw new Exception(stationRoot + " is not a directory for NCS");
	    }

	    // If that worked, we should be able to find the toolsDir
	    // as ncsStationDir/../../tools
	    File ncsDir = ncsStationDir.getParentFile();
	    if(ncsDir == null || !ncsDir.isDirectory())
		throw new Exception("Could not find ncs directory from " + ncsStationDir);
	    ipoppRootDir = ncsDir.getParentFile();
	    if(ipoppRootDir == null || !ipoppRootDir.isDirectory())
		throw new Exception("Could not find IPOPP root directory from " + ncsDir);
	    ipoppRootDir = ipoppRootDir.getCanonicalFile();

	    toolsDir= new File(ipoppRootDir, "tools");
	    toolsDir = toolsDir.getCanonicalFile(); 
	    if(!toolsDir.isDirectory()) {
		throw new Exception("Could not find IPOPP tools directory at " + toolsDir);
	    }
	}

	// Read in the monitor config file
	Node configRoot = DOMUtil.readXMLFile(monConfig);
	// Spit if this isn't our XML file
	if(!configRoot.getNodeName().equals("ProcessingMonitors"))
	    throw new Exception(monConfig + " is a " + configRoot.getNodeName()
				+ " , not a ProcessingMonitor XML document");

	// If we have bestWidth and bestHeight attributes, use them to mung
	// bestWidth and bestHeight
	String bestString = DOMUtil.getNodeAttribute(configRoot, "bestHeight");
	if(!bestString.equals("")) {
	    bestHeight = Integer.parseInt(bestString);
	}
	bestString = DOMUtil.getNodeAttribute(configRoot, "bestWidth");
	if(!bestString.equals("")) {
	    bestWidth = Integer.parseInt(bestString);
	}

	frame = this;

	// Creating JTabbedPane here
	tabbedPane = new JTabbedPane();
	// Loop down the monitor descriptions, creating a tab for each
	for (Node pn : DOMUtil.Children(configRoot, "ProcessingMonitor")) {
	    // get the columns and satellite name
	    int columns = Integer.decode(DOMUtil.getNodeAttribute(pn, "columns"));
	    String satellite = DOMUtil.getNodeAttribute(pn, "satellite");
	    String moverName = DOMUtil.getNodeAttribute(pn, "moverName");
	    String moverCall = DOMUtil.getNodeAttribute(pn, "moverCall");
	    MonitorScrollPane newMSP = new MonitorScrollPane(columns, satellite, moverName, moverCall);
	    MonitorPanel newMP = newMSP.getMonitorPanel();
	    tabbedPane.addTab(satellite, newMSP);
	    monitorPanels.add(newMP);
	    newMP.setTabIndex(tabbedPane.indexOfTab(satellite));
	    newMP.loadMonitors(pn);
	}

	// With everything loaded, we can examine the panels and get the
	// right rows and columns
	int rightRows = 0;
	int rightColumns = 0;
	for(MonitorPanel mp : monitorPanels) {
	    if (mp.getRows() > rightRows)
		rightRows = mp.getRows();
	    if (mp.getColumns() > rightColumns)
		rightColumns = mp.getColumns();
	}
	// And we can force everybodys dimensions to match
	for(MonitorPanel mp : monitorPanels) {
	    mp.setBestSize(rightRows, rightColumns);
	}
       
	// Locate the default_config.file
	if(defaultConfig != null) {
	    defaultConfigFile = new File(defaultConfig);
	    // Just to make things pretty, canonicalize the path
	    defaultConfigFile = defaultConfigFile.getCanonicalFile();
	    readConfigFile(defaultConfigFile);
	}
	// Locate the tile config
	if(tilelistConfig != null) {
	    tilelistConfigFile = new File(tilelistConfig);		    
	}

	// Add a menu bar with commands:
	// Mode
	//   IPOPP Process Monitor
	//   IPOPP Configuration Editor
	// Actions
	//   Start Services
	//   Stop Services
	//   Start SPA Services
	//   Stop SPA Services
	//   Check IPOPP Services
	//   Reset IPOPP
	//   Save IPOPP Configuration

	JMenuBar menuBar = new JMenuBar();

	modeMenu = new JMenu("Mode");

	processMonitorItem = new JMenuItem("IPOPP Process Monitor");
	processMonitorItem.addActionListener(new ProcessMonitorMenuClicked());
	modeMenu.add(processMonitorItem);
	
	configurationEditorItem = new JMenuItem("IPOPP Configuration Editor");
	configurationEditorItem.addActionListener(new ConfigurationEditorMenuClicked());
	modeMenu.add(configurationEditorItem);

	actionsMenu = new JMenu("Actions");

	startServicesItem = new JMenuItem("Start Services");
	startServicesItem.addActionListener(new StartServicesMenuClicked());
	actionsMenu.add(startServicesItem);
	
	stopServicesItem = new JMenuItem("Stop Services");
	stopServicesItem.addActionListener(new StopServicesMenuClicked());
	actionsMenu.add(stopServicesItem);

	startSPAsItem = new JMenuItem("Start SPA Services");
	startSPAsItem.addActionListener(new StartSPAsMenuClicked());
	actionsMenu.add(startSPAsItem);
	
	stopSPAsItem = new JMenuItem("Stop SPA Services");
	stopSPAsItem.addActionListener(new StopSPAsMenuClicked());
	actionsMenu.add(stopSPAsItem);
	
	checkIPOPPServicesItem = new JMenuItem("Check IPOPP Services");
	checkIPOPPServicesItem.addActionListener(new CheckIPOPPMenuClicked());
	actionsMenu.add(checkIPOPPServicesItem);

	resetIPOPPItem = new JMenuItem("Reset IPOPP");
	resetIPOPPItem.addActionListener(new ResetIPOPPMenuClicked());
	actionsMenu.add(resetIPOPPItem);

	//installSPAItem = new JMenuItem("Install SPA");
	//installSPAItem.addActionListener(new InstallSPAMenuClicked());
	//actionsMenu.add(installSPAItem);
	
	configureProjectionItem = new JMenuItem("Configure Projection");
	configureProjectionItem.addActionListener(new ConfigureProjectionMenuClicked());
	actionsMenu.add(configureProjectionItem);
	
	configureTileItem = new JMenuItem("Configure Tiles");
	configureTileItem.addActionListener(new ConfigureTileMenuClicked());
	actionsMenu.add(configureTileItem);
	
	saveConfigurationItem = new JMenuItem("Save IPOPP Configuration");
	saveConfigurationItem.addActionListener(new SaveConfigurationMenuClicked());
	actionsMenu.add(saveConfigurationItem);
    
	menuBar.add(modeMenu);
	menuBar.add(actionsMenu);

	// And smack the initial mode appropriately
	setControlPanelMode(isLocalhost ? PanelMode.CONTROL : PanelMode.MONITOR);

	JPanel mainPanel = new JPanel(new BorderLayout());
	mainPanel.setLayout(new BorderLayout());
	mainPanel.add(menuBar,BorderLayout.NORTH);

	// Nest a VPanel containing the tabbedPane on top of
	// a MarginPanel containing HGlue and the statusField
	statusField = new CustomLabel("No status just yet");
	JPanel stackPanel = new VPanel(tabbedPane, new MarginPanel(5,5,new HPanel(GUtil.HGlue(),statusField), 5,5));

	mainPanel.add(new MarginPanel(10,10,stackPanel,0,10),BorderLayout.CENTER);

	getContentPane().setLayout(new BorderLayout());
	getContentPane().add(mainPanel, BorderLayout.CENTER);
	pack();
	setDefaultCloseOperation(haveConsole ?
				 JFrame.DISPOSE_ON_CLOSE
				 : JFrame.EXIT_ON_CLOSE );
	// GUtil.configureWindow(this,0.95,0.85);
	GUtil.configureWindow(this,1000,700);
	setVisible(true);
	javax.swing.Timer timer = new javax.swing.Timer(125,new Animate());
	timer.start();

	// If we're local, spin up the background updating thread
	if(isLocalhost) {
	    backgroundUpdater =
		new Thread() {
		    public void run() {
			backgroundUpdaterLoop();
		    }
		};
	    backgroundUpdater.start();
	}
	// Otherwise forcibly update to start
	else {
	    updateAllMonitorPanels();
	}
    }

    /**
     * The usual crap for defaulting an optional argument
     */
 public ControlPanel (String monConfig, String defaultConfig, String tilelistConfig, String stationRoot, boolean isLocalhost) throws Exception {
     this(monConfig, defaultConfig, tilelistConfig, stationRoot, isLocalhost, true);
 }


    /**
     * The loops-almost-forver function that checks the install and run/stop
     * status of all stations on the display.  It pauses while we are in edit mode,
     * updates immediately on request, and stops altogether on request.
     */
    public void backgroundUpdaterLoop()
    {
	boolean updateRequested = false;
	while(keepUpdating) {
	    // check the installed/running status if we're not editing
	    if(!isEditor) {
		SimpleProgressMonitor spm = null;
		if(updateWithMonitor) {
		    updateWithMonitor = false;
		    int enabledStations = 0;
		    for (MonitorPanel mp : monitorPanels) {
			enabledStations += mp.countEnabledStations();
		    }
		    try {
			spm = new SimpleProgressMonitor(frame,
						    "Synchronizing Display",
						    "Synchronizing...",
						    enabledStations,
						    " running");
		    }
		    // If we get an exception creating spm, we don't get
		    // a monitor.  Tragic, I know...
		    catch (Exception e) {};
		}
		for (MonitorPanel mp : monitorPanels) {
		    mp.checkInstalled();
		    mp.checkStationRunStatus(spm);
		    mp.updateMoverPanel();
		    if(updateRequested)
			mp.forciblyUpdate();
		}
		if(spm != null)
		    spm.close();
	    }
	    updateRequested = false;
	    // Sleep 30 seconds or until one of our flags gets flipped
	    int secondsToWait = 30;
	    while(keepUpdating && secondsToWait > 0) {
		try {
		    Thread.sleep(1000);
		}
		catch (Exception e) {
		    // For debugging, print this
		    System.err.println("backgroundUpdater INTERRUPTED:");
		    System.err.println(e);
		}
		--secondsToWait;
		if(updateNow) {
		    secondsToWait = 0;
		    updateNow = false;
		    updateRequested = true;
		}
	    }
	}
	    
    }

    /**
     * Abstract interface to background updater control -
     * pokes the backgroundUpdater thread to request an update run
     * within the next second.
     */
    public void requestBackgroundUpdate(boolean withMonitor) {
	updateNow = true;
	updateWithMonitor = withMonitor;
    }

    public void requestBackgroundUpdate() {
	requestBackgroundUpdate(false);
    }

    /**
     * Abstract interface to background updater control -
     * tells the backgroundUpdater thread to stop
     * within the next second.
     */
    public void stopBackgroundUpdate() {
	keepUpdating = false;
    }

    public void setControlPanelMode (PanelMode pm) {
	switch (pm) {
	case MONITOR:	// Disable everything
	    processMonitorItem.setEnabled(false);
	    configurationEditorItem.setEnabled(false);
	    startServicesItem.setEnabled(false);
	    stopServicesItem.setEnabled(false);
	    startSPAsItem.setEnabled(false);
	    stopSPAsItem.setEnabled(false);
	    checkIPOPPServicesItem.setEnabled(false);
	    resetIPOPPItem.setEnabled(false);
	    //installSPAItem.setEnabled(false);
	    saveConfigurationItem.setEnabled(false);
	    configureProjectionItem.setEnabled(false);
	    configureTileItem.setEnabled(false);
	    break;
	case CONTROL:	// Enable controls
	    ControlPanel.this.setTitle(controlTitle);
	    isEditor = false;
	    processMonitorItem.setEnabled(false);
	    configurationEditorItem.setEnabled(true);
	    startServicesItem.setEnabled(true);
	    stopServicesItem.setEnabled(true);
	    startSPAsItem.setEnabled(true);
	    stopSPAsItem.setEnabled(true);
	    checkIPOPPServicesItem.setEnabled(true);
	    resetIPOPPItem.setEnabled(true);
	    //installSPAItem.setEnabled(false);
	    saveConfigurationItem.setEnabled(false);
	    configureProjectionItem.setEnabled(false);
	    configureTileItem.setEnabled(false);
	    // Smack background to normal
	    // This isn't really the right color...
	    for (MonitorPanel mp : monitorPanels) {
		mp.setBackground(Color.lightGray);
	    }

	    break;
	case EDITOR:	// Enable editing
	    ControlPanel.this.setTitle(editorTitle);
	    isEditor = true;
	    processMonitorItem.setEnabled(true);
	    configurationEditorItem.setEnabled(false);
	    startServicesItem.setEnabled(false);
	    stopServicesItem.setEnabled(false);
	    startSPAsItem.setEnabled(false);
	    stopSPAsItem.setEnabled(false);
	    checkIPOPPServicesItem.setEnabled(false);
	    resetIPOPPItem.setEnabled(false);
	    //installSPAItem.setEnabled(true);
	    saveConfigurationItem.setEnabled(true);
	    configureProjectionItem.setEnabled(true);
	    configureTileItem.setEnabled(true);
	    // Smack background to editor dark
	    // This looks TOO dark to me...
	    for (MonitorPanel mp : monitorPanels) {
		mp.setBackground(Color.darkGray);
	    }
	    break;
	}
    }

    public void onLocalhost(boolean isLocalhost)
    {
	if (isLocalhost != this.isLocalhost) {
	    this.isLocalhost = isLocalhost;
	    // !!! Force GUI to recognize change somehow...
	}
    }
    /**
     * Count the number of stations in the default_config.file, by running
     * getstations.sh and counting the number of whitespace chars in the
     * output
     */
    private int countConfiguredStations()
	throws Exception
    {
	File getstations = new File(toolsDir, "getstations.sh");
	String cmd = getstations.getCanonicalPath() + " " + defaultConfigFile.getCanonicalPath();
	
	Process p = Runtime.getRuntime().exec(cmd);
	// This process take no input, so just close its OutputStream
	// right now...
	p.getOutputStream().close();

	// I don't think there will be any error output, but drain it anyway
	// (the Process will wedge if there is output and it fills the buffer)
	StreamCopier errOut = new StreamCopier(p.getErrorStream(), null, null);
	int result = 0;
	int nextb;
	try {
	    while((nextb = p.getInputStream().read()) >= 0) {
		char nextc = (char)nextb;
		if(nextc == ' ' || nextc == '\n')
		    ++result;
	    }
	    p.waitFor();
	}
	finally {
	    // If you close() all three streams of a Process, their
	    // resources are released and you don't have to destroy() it
	    // (at least, that's how it appears to work on several Linux
	    //  JVMs)
	    //p.destroy();
	    // Closing these streams here shouldn't hurt, and it might
	    // help if there was an error and one of the StreamCopiers
	    // didn't do its job...
	    
	    p.getOutputStream().close();
	    p.getInputStream().close();
	    p.getErrorStream().close();
	}
	return result;
    }

    /**
     * Run an external command, wait for it to finish, return its return code.
     * If a SimpleProgressMonitor is passed in, hand it off to the output
     * StreamCopiers
     * True paranoia would be running the Process in a separate thread with a
     * timer to wait a maximum time and kill it.  Maybe later if necessary...
     */
    private int runCommand(String command, SimpleProgressMonitor pm)
	throws Exception
    {
	int result = -1;
	Process p = Runtime.getRuntime().exec(command);
	try {
	    // These processes take no input, so just close their OutputStream
	    // right now...
	    p.getOutputStream().close();

	    // These StreamCopier objects spin threads that drain their
	    // respective stdout/stderr streams and throw the output away.
	    // Go see the original StreamCopier in ncs supporting Ncs_run for
	    // other exciting capabilities.
	    StreamCopier stdstream = new StreamCopier(p.getInputStream(), null, pm);
	    StreamCopier errstream = new StreamCopier(p.getErrorStream(), null, pm);

	    stdstream.close();
	    errstream.close();

	    p.waitFor();
	    result =  p.exitValue();
	}
	finally {
	    // If you close() all three streams of a Process, their
	    // resources are released and you don't have to destroy() it
	    // (at least, that's how it appears to work on several Linux
	    //  JVMs)
	    //p.destroy();
	    // Closing these streams here shouldn't hurt, and it might
	    // help if there was an error and one of the StreamCopiers
	    // didn't do its job...
	    
	    p.getOutputStream().close();
	    p.getInputStream().close();
	    p.getErrorStream().close();
	}
	return result;
    }

    private int runCommand(String command)
	throws Exception
    {
	return runCommand(command, null);
    }

    /**
     * Forcibly update all MonitorPanels in another thread
     */
    private void updateAllMonitorPanels()
    {
	new Thread() {
	    public void run() {
		for(MonitorPanel mp : monitorPanels) {
		    mp.forciblyUpdate();
		}
	    }
	}.start();
    }

    private class ProcessMonitorMenuClicked implements ActionListener {
	public void actionPerformed (ActionEvent ae) {
	    // Ask about saving current configuration
	    switch(JOptionPane.showConfirmDialog(frame,
					      "Do you want to save current changes to IPOPP Configuration?",
					      "Exit IPOPP Configuration Mode",
						 JOptionPane.YES_NO_CANCEL_OPTION)) {
	    case JOptionPane.YES_OPTION:
		try {
		    writeConfigFile(defaultConfigFile);
		    setControlPanelMode(PanelMode.CONTROL);

		    JOptionPane.showMessageDialog(frame,
						  "IPOPP configuration saved",
						  "Finished editing configuration",
						  JOptionPane.INFORMATION_MESSAGE);
		    requestBackgroundUpdate(true);

		}
		catch (Exception e) {
		    JOptionPane.showMessageDialog(frame,e.toString(),"Error",
						  JOptionPane.ERROR_MESSAGE);
		}
		break;
	    case JOptionPane.NO_OPTION:
		try {
		    // Reread the config file to force the display into
		    // agreement with it
		    readConfigFile(defaultConfigFile);
		    setControlPanelMode(PanelMode.CONTROL);

		    // Force repaints on all the MonitorPanels to get the display right,
		    // in another Thread to keep it responsive
		    //updateAllMonitorPanels();

		    JOptionPane.showMessageDialog(frame,
						  "Changes not saved",
						  "Finished editing configuration",
						  JOptionPane.INFORMATION_MESSAGE);
		    requestBackgroundUpdate(true);
		}
		catch (Exception e) {
		    JOptionPane.showMessageDialog(frame,e.toString(),"Error",
						  JOptionPane.ERROR_MESSAGE);
		}
		break;
	    case JOptionPane.CANCEL_OPTION:
		// Ignore, continue editing
		break;
	    }
	}
    }

    private class ConfigurationEditorMenuClicked implements ActionListener {
	public void actionPerformed (ActionEvent ae) {
	    // Warn about stopping services, then do it and switch to
	    // editing mode
	    if (JOptionPane.showConfirmDialog(frame,
					      "SPA services will be stopped. Confirm?",
					      "Enter IPOPP Configuration Mode",
					      JOptionPane.YES_NO_OPTION)
		== JOptionPane.YES_OPTION)
		try {
		    StartStopSPAs(false);
		    readConfigFile(defaultConfigFile);
		    setControlPanelMode(PanelMode.EDITOR);
		    updateAllMonitorPanels();
		}
		catch (Exception e) {
		    JOptionPane.showMessageDialog(frame,e.toString(),"Error",
						  JOptionPane.ERROR_MESSAGE);
		}
	}
    }

    private class StartServicesMenuClicked implements ActionListener {
	public void actionPerformed (ActionEvent ae) {
	    if (JOptionPane.showConfirmDialog(frame,
					      "Confirm?",
					      "Starting IPOPP",
					      JOptionPane.YES_NO_OPTION)
		== JOptionPane.YES_OPTION)
		StartStopServices(true);
	}
    }

    private class StopServicesMenuClicked implements ActionListener {
	public void actionPerformed (ActionEvent ae) {
	    if (JOptionPane.showConfirmDialog(frame,
					      "Confirm?",
					      "Stopping IPOPP",
					      JOptionPane.YES_NO_OPTION)
		== JOptionPane.YES_OPTION)
		StartStopServices(false);
	}
    }

    private class StartSPAsMenuClicked implements ActionListener {
	public void actionPerformed (ActionEvent ae) {
	    if (JOptionPane.showConfirmDialog(frame,
					      "Confirm?",
					      "Starting SPA Services",
					      JOptionPane.YES_NO_OPTION)
		== JOptionPane.YES_OPTION)
		StartStopSPAs(true);
	}
    }

    private class StopSPAsMenuClicked implements ActionListener {
	public void actionPerformed (ActionEvent ae) {
	    if (JOptionPane.showConfirmDialog(frame,
					      "Confirm",
					      "Stopping SPA Services",
					      JOptionPane.YES_NO_OPTION)
		== JOptionPane.YES_OPTION)
	    StartStopSPAs(false);
	}
    }

    private class CheckIPOPPMenuClicked implements ActionListener {
	public void actionPerformed (ActionEvent ae) {
	    CheckIPOPPServices();
	}
    }

    private class ResetIPOPPMenuClicked implements ActionListener {
	public void actionPerformed (ActionEvent ae) {
	    if (JOptionPane.showConfirmDialog(frame,
					      "Confirm?",
					      "Reset IPOPP",
					      JOptionPane.YES_NO_OPTION)
		== JOptionPane.YES_OPTION) {
		ResetIPOPP();
	    }
	}
    }

    private class SaveConfigurationMenuClicked implements ActionListener {
	public void actionPerformed (ActionEvent ae) {
	    // We try to save the configuration
	    if (JOptionPane.showConfirmDialog(frame,
					      "Click OK to continue",
					      "Save Configuration Now?",
					      JOptionPane.OK_CANCEL_OPTION)
		== JOptionPane.OK_OPTION)
	    try {
		writeConfigFile(defaultConfigFile);
		JOptionPane.showMessageDialog(frame,
					      "IPOPP Configuration saved",
					      "Finished editing configuration",
					      JOptionPane.INFORMATION_MESSAGE);
	    }
	    catch (Exception e) {
		JOptionPane.showMessageDialog(frame,e.toString(),"Error",
					      JOptionPane.ERROR_MESSAGE);
	    }
	}
    }
    
    private class InstallSPAMenuClicked implements ActionListener {
    	public void actionPerformed (ActionEvent ae) {

    		JPanel installpanel = new JPanel();
    		installpanel.setLayout(new BoxLayout(installpanel, BoxLayout.Y_AXIS)); 
    		JComboBox availableinstalls = new JComboBox();
    		File[] flist=ipoppRootDir.listFiles();
    		int SPAsavailable=0;
    		for (File file : flist) {
    		
    	        if (file.isFile()) {
    	        	boolean containsSPA = (file.getName().indexOf("SPA")>=0);
    	        	boolean containsPatch = (file.getName().indexOf("PATCH")>=0);
    	        	boolean endswithtargz = file.getName().endsWith(".tar.gz");
    	        	if(containsSPA && !containsPatch && endswithtargz)
    	        	{
    	              availableinstalls.addItem(file.getName());
    	              SPAsavailable=SPAsavailable+1;
    	        	}
    	        }
    		}
    		Object[] options= {"Ok", "Cancel"};;
    		if (SPAsavailable==0)
    		{
    			installpanel.add(new JLabel("No SPA tarballs available in drl/ for installation"));    			
    		}else{
    			installpanel.add(availableinstalls);
    			options[0] = "Install SPA";
    		}
    		if (JOptionPane.showOptionDialog(frame,
    					      installpanel, 
    					      "Install SPA",
    					      JOptionPane.OK_CANCEL_OPTION, 
    					      JOptionPane.QUESTION_MESSAGE,
    					      null, options, null)
    					      == JOptionPane.OK_OPTION){
    			  if(availableinstalls.getSelectedItem()!=null)
    		        installSPA(availableinstalls.getSelectedItem().toString());
    		}
    		
        }
    }

    private class ConfigureProjectionMenuClicked implements ActionListener {
    	public void actionPerformed (ActionEvent ae) {
    	    // We try to save the configuration
    		JPanel mappanel = new JPanel();
    		mappanel.setLayout(new BoxLayout(mappanel, BoxLayout.Y_AXIS));    		
    		JComboBox projection = new JComboBox();
    		projection.addItem("Select Projection...");
    		projection.addItem("Geographic (not appropriate at extreme latitudes)");
    		projection.addItem("Stereographic");
    		mappanel.add(projection);
    		Object[] options = {"Configure Projection", "Cancel"};
    		if (JOptionPane.showOptionDialog(frame,
    					      mappanel, 
    					      "Configure Projection",
    					      JOptionPane.OK_CANCEL_OPTION, 
    					      JOptionPane.QUESTION_MESSAGE,
    					      null, options, null)
    					      == JOptionPane.OK_OPTION){
    		switch (projection.getSelectedIndex()){
    			case 0: //Do Nothing
    				                 break;
    			case 1: preconfigureH2G("Geographic");
    			                      break;
    			case 2: preconfigureH2G("Stereographic");
    			        break;
    		}
    		}
    		
        }
    }
    
    private class ConfigureTileMenuClicked implements ActionListener {
    	
    	JPanel mappanel;
    	JList fulllist, selectionlist;
    	JScrollPane fulllistpanel, selectionlistpanel;
    	//JComboBox projection;
    	
    	public ConfigureTileMenuClicked(){
    		 // We try to save the configuration
    		mappanel = new JPanel(new GridBagLayout());
    		GridBagConstraints c = new GridBagConstraints();
    		JLabel modistiles=new JLabel(GUtil.sinusoidalGrid);     //new ImageIcon("/home/sdasgupta/drl/nsls/images/sinusoidal.gif"));
    				
    		c.fill = GridBagConstraints.HORIZONTAL;
    		c.gridx = 0;
    		c.gridy = 0;
    		c.gridwidth = 10;
    		c.gridheight =10;
    		mappanel.add(modistiles,c);
    		
    		
        		
    		DefaultListModel modelfulllist=new DefaultListModel();
    		
    		try {
    		BufferedReader in = new BufferedReader(new FileReader(tilelistConfigFile));
    		String line, tile;   		
    		
    		    while ((line = in.readLine()) != null) {
    		    	StringTokenizer byBlanks = new StringTokenizer(line);
    				tile = byBlanks.nextToken();
    		        modelfulllist.addElement(tile); //to populate jlist
    	         }
    	        in.close();
    	    } catch (Exception e) {
    	    }
    		fulllist=new JList(modelfulllist);
    		//for (int i = 0; i < 15; i++)
    			
    		      //modelfulllist.addElement("Element " + i);
    		
    		DefaultListModel modelselectionlist=new DefaultListModel();
    	    	selectionlist = new JList(modelselectionlist);
    		fulllistpanel = new JScrollPane(fulllist);
    		selectionlistpanel = new JScrollPane(selectionlist);
    		JLabel alltiles= new JLabel(     "Land Tiles (See Location on grid)");
    		JLabel selectedtiles= new JLabel("Selected Land Tiles (maximum 10)");
    		JButton addtiles = new JButton("Add>");
    		addtiles.setActionCommand("Add");
    		addtiles.addActionListener(this);
    		JButton removetiles = new JButton("<Remove");
    		removetiles.setActionCommand("Remove");
    		removetiles.addActionListener(this);
    		
		/* IPOPP 2.4: Projection settings removed from Tile Configuration
    		JLabel projlabel= new JLabel("Projection for Tile Imagery (H2G)");
    		projection = new JComboBox();
    		projection.addItem("Geographic (not appropriate at extreme latitudes)");
    		projection.addItem("Stereographic");
    		*/
    		
    		
    		c.fill = GridBagConstraints.HORIZONTAL;
    		c.gridx = 0;
    		c.gridy = 10;
    		c.gridwidth = 4;
    		c.gridheight =1;
    		mappanel.add(alltiles,c);
    		
    		c.fill = GridBagConstraints.HORIZONTAL;
    		c.gridx = 6;
    		c.gridy = 10;
    		c.gridwidth = 4;
    		c.gridheight =1;
    		mappanel.add(selectedtiles,c);
    		
    		c.fill = GridBagConstraints.HORIZONTAL;
    		c.gridx = 0;
    		c.gridy = 11;
    		c.gridwidth = 4;
    		c.gridheight = 15;    		
    		mappanel.add(fulllistpanel,c);
    		
    		c.fill = GridBagConstraints.HORIZONTAL;
    		c.gridx = 6;
    		c.gridy = 11;
    		c.gridwidth = 4;
    		c.gridheight = 15;
    		mappanel.add(selectionlistpanel,c);
    		
    		c.fill = GridBagConstraints.HORIZONTAL;
    		c.gridx = 4;
    		c.gridy = 15;
    		c.gridwidth = 1;
    		c.gridheight =1;
    		mappanel.add(addtiles,c);
    		
    		c.fill = GridBagConstraints.HORIZONTAL;
    		c.gridx = 4;
    		c.gridy = 17;
    		c.gridwidth = 1;
    		c.gridheight =1; 		
    		mappanel.add(removetiles,c);
    		
    	   	/* IPOPP 2.4: Projection settings removed from Tile Configuration	
    		c.fill = GridBagConstraints.HORIZONTAL;
    		c.gridx = 0;
    		c.gridy = 35;
    		c.gridwidth = 4;
    		c.gridheight =1; 		
    		mappanel.add(projlabel,c);
    		
    		c.fill = GridBagConstraints.HORIZONTAL;
    		c.gridx = 0;
    		c.gridy = 36;
    		c.gridwidth = 4;
    		c.gridheight =1;
    		mappanel.add(projection,c);
    		*/

    	}
    	
    	public void actionPerformed (ActionEvent ae) {
    	   
         
    	  
          if (ae.getActionCommand() == "Configure Tiles")
          {
        	   Object[] options = {"Configure Tiles", "Cancel"};
          		if (JOptionPane.showOptionDialog(frame,
    					      mappanel, 
    					      "Configure Tiles",
    					      JOptionPane.OK_CANCEL_OPTION, 
    					      JOptionPane.QUESTION_MESSAGE,
    					      null, options, null)
    					      == JOptionPane.OK_OPTION){
          	     DefaultListModel updatedselectionlist=(DefaultListModel)selectionlist.getModel();
          	         
			 // IPOPP 2.4: Set the first argument to "dummy" since the preconfigureTiles.sh script expects 
			 // the first argument to be the projection
          		 String argumentlist="dummy",selectedtile;

          	     /* IPOPP 2.4: Projection settings removed from Tile Configuration	 
         	     switch (projection.getSelectedIndex()){
  			
  			      case 0: argumentlist="Geographic";
  			              break;
  			      case 1: argumentlist="Stereographic";
  			               break;
  		         }
		     */

              	  for (int i = 0; i < updatedselectionlist.size(); i++) {
              		  selectedtile = updatedselectionlist.getElementAt(i).toString();
                  	  argumentlist=argumentlist + " "+ selectedtile;                  	                    	  
              	  }
              		//System.out.println(argumentlist);
          		preconfigureTiles(argumentlist);
    		}
          }
          else if (ae.getActionCommand() == "Add"){
        	  Object[] selectedtilelist_str=fulllist.getSelectedValues();
        	  ArrayList selectedtilelist=new ArrayList(Arrays.asList(selectedtilelist_str));//(List)fulllist.getSelectedValues();
        	  DefaultListModel updatedselectionlist=(DefaultListModel)selectionlist.getModel();
        	  //DefaultListModel updatedfulllist=(DefaultListModel)fulllist.getModel();
        	  
        	  //Create unique list
        	  String selectedtile;
        	  for (int i = 0; i < selectedtilelist.size(); i++) {
      		    selectedtile = selectedtilelist.get(i).toString();
      		    if(updatedselectionlist.contains(selectedtile))
      		    	selectedtilelist.remove(selectedtile);      		    
      		    }
        	  
        	  if(updatedselectionlist.size()+selectedtilelist.size()>10)
        	  {
        		  JOptionPane.showMessageDialog(frame,"Error: Cannot select more than 10 tiles","Error",
						  JOptionPane.ERROR_MESSAGE);    			  
        	  }
        	  else
        	  {
        	     
        	     for (int i = 0; i < selectedtilelist.size(); i++) {
        		    selectedtile = (String) selectedtilelist.get(i);
        		    if(!updatedselectionlist.contains(selectedtile)){
        		      updatedselectionlist.addElement(selectedtile);
            	      //updatedfulllist.removeElement(selectedtile);
        	         }
        		    
        	     }
        	    
        		selectionlist.setModel(updatedselectionlist);
        	    selectionlistpanel.revalidate();
        	    selectionlistpanel.repaint();
        	    //fulllist.setModel(updatedfulllist);
        	    fulllist.clearSelection();
        	    fulllistpanel.revalidate();
        	    fulllistpanel.repaint();
        	  }
          }
          else if (ae.getActionCommand() == "Remove"){
        	  Object[] selectedtilelist_str=selectionlist.getSelectedValues();
        	  List selectedtilelist=new ArrayList(Arrays.asList(selectedtilelist_str));//(List)selectionlist.getSelectedValues();
        	  DefaultListModel updatedselectionlist=(DefaultListModel)selectionlist.getModel();
        	  //DefaultListModel updatedfulllist=(DefaultListModel)fulllist.getModel();
        	  String selectedtile;
        	  for (int i = 0; i < selectedtilelist.size(); i++) {
        		  selectedtile = (String) selectedtilelist.get(i);
            	  updatedselectionlist.removeElement(selectedtile);
            	  //updatedfulllist.addElement(selectedtile);
        	  }
        		  
        	  selectionlist.setModel(updatedselectionlist);
        	  selectionlistpanel.revalidate();
        	  selectionlistpanel.repaint();
        	  //fulllist.setModel(updatedfulllist);
        	  //fulllistpanel.revalidate();
        	  //fulllistpanel.repaint();
          }
        }
    }
    
    private void StartStopSPAs(boolean shouldBeRunning) {
	StartStopStuff(false, shouldBeRunning);
    }

    private void StartStopServices(boolean shouldBeRunning) {
	StartStopStuff(true, shouldBeRunning);
    }

    /**
     * Helper method to start/stop services and/or SPAs.
     * It loads the config file (to insure
     * the enabled flags are consistent), then runs an appropriate script
     * in tools, then probes the installed stations to see if they're running.
     * Do all this in a separate Thread because Java's
     * GUI libraries like it better that way...
     */
    private void StartStopStuff(final boolean servicesToo, final boolean shouldBeRunning)
    // Un-fsck-ing believable.  See that "final" above?  Java brain-damaged
    // "closures" strike again...
    {
	new Thread() {
	    public void run() {
		try {
		    String cmdText = (shouldBeRunning ? "Starting" : "Stopping");
		    String jswCommand = (shouldBeRunning ? "start" : "stop");

		    String launchCommand = new File(toolsDir,
						    (servicesToo ? 
						     "services.sh"
						     : "spa_services.sh")
						    ).getCanonicalPath();
		    
		    // Sleazy trick to improve apparent performance; we create
		    // the progress monitor early with a range of 100, then
		    // reset the range after we've counted the real number of
		    // events
		    SimpleProgressMonitor spm =
			new SimpleProgressMonitor(frame,
						  cmdText +
						  (servicesToo ?
						   " IPOPP services"
						   :" SPA services"),
						  cmdText +
						  (servicesToo ?
						   " IPOPP"
						   :" all SPAs..."),
						  100,
						  "^" + cmdText + " ");
		    readConfigFile(defaultConfigFile);
		    spm.bumpState();
		    int numStations = countConfiguredStations();
		    // We expect two events per SPA station as it starts/stops
		    // (one from the start/stop command, one from the
		    // checkStationRunStatus afterwards)
		    //
		    // If we are lanuching IPOPP services too, we expect
		    // CoreService.count() more events
		    // (NOTE this only works for single-node now)
		    //
		    // We add 2 events for the padding calls to bumpState(),
		    // and 5 more for the delay loop below, making 7
		    //
		    int newmax =
			(servicesToo ? CoreService.count() : 0)
			+ numStations * 2
			+ 7;
		    spm.setMaximum(newmax);
		    // The return code from spa_services.sh is not useful -
		    // it's the return code of the last station.  So, we ignore it.
		    int retcode = runCommand(launchCommand + " " + jswCommand, spm);
		    spm.bumpState();
		    spm.setNote("Checking status...");
		    // If a station fails to start, it can take several seconds
		    // to do so, during which its probe status is OK.
		    // So, we wait five seconds, bumping the status while
		    // waiting
		    for (int i=0; i<5; i++) {
			Thread.sleep(1000);
			spm.bumpState();
		    }
		    // Now it ought to be safe to probe the stations
		    // The true tells the prober we just flipped on/off
		    for (MonitorPanel mp : monitorPanels)
			mp.checkStationRunStatus(spm, true, shouldBeRunning);
		    // We ought to be done here; forcibly close if we missed
		    // an event somehow
		    spm.close();
		}
		catch (Exception e) {
		    JOptionPane.showMessageDialog(frame,e.toString(),"Error",
						  JOptionPane.ERROR_MESSAGE);
		}
	    }
	}.start();
    }

    /**
     * Checks status of IPOPP core services,
     * updates station status displays,
     * checks for rogue processes.
     * Do all this in a separate Thread because Java's
     * GUI libraries like it better that way...
     */
    private void CheckIPOPPServices()
    {
	new Thread() {
	    public void run() {
		try {
		    // The number of events we expect is
		    // 1 to start with
		    // + number of statusCommands
		    // + 1 for SPA status check
		    // + 1 for rogue check
		    SimpleProgressMonitor spm =
			new SimpleProgressMonitor(frame,
						  "Check IPOPP Services",
						  "Checking core services...",
						  3 + CoreService.count(),
						  "^done\\.");
		    spm.bumpState();
		    // Check the core services
		    int coreRunning = 0;
		    List<String> coreProblem = new LinkedList<String>();
		    for(CoreService cs : CoreService.coreService) {
			String statusCommand = ipoppRootDir.getPath()
			    + cs.getPath() + " status";
			spm.setNote("Checking " + cs.getLabel() + " Services ...");
			int retcode = runCommand(statusCommand);
			spm.bumpState();
			if(retcode == 0)
			    ++coreRunning;
			else
			    coreProblem.add("    " + cs.getLabel() + " Services");
			Thread.sleep(1000);
		    }

		    // Check SPA services (in another thread, sleep anyway)
		    spm.setNote("Checking SPA status...");
		    requestBackgroundUpdate();
		    spm.bumpState();
		    Thread.sleep(1000);
		    
		    // Check for rogue processes
		    String rogueCommand = new File(toolsDir, "check_services.sh").getCanonicalPath();
		    spm.setNote("Checking for rogue processes...");
		    int rogueStatus = runCommand(rogueCommand);
		    Thread.sleep(1000);
		    spm.bumpState();
		    // Forcibly close in case we missed an event somehow
		    spm.close();

		    // We're done.  Throw up a dialog with the summary
		    if(rogueStatus !=0 && coreRunning == CoreService.count()) {
			JOptionPane.showMessageDialog(frame,
						      "System services and health verified",
						      "Check IPOPP Services",
						      JOptionPane.INFORMATION_MESSAGE);
		    }
		    else {
			// Build the array of Strings the warning dialog box will want
			if(coreProblem.size() != 0) {
			    coreProblem.add(0, "These IPOPP services are not running:");
			    coreProblem.add("");
			    coreProblem.add("Try Actions > Start Services to start them");
			}
			if(rogueStatus == 0) {
			    coreProblem.add(" ");
			    coreProblem.add("System error detected!");
			    coreProblem.add(" ");
			    coreProblem.add("Restore system health?");
			}
			// Smash the strings into an array for the dialog
			Object coreProblemArray[] = coreProblem.toArray();
			// We only need a response if we have a suspicious process
			// problem
			if(rogueStatus == 0) {
			    // We want a WARNING dialog with YES/NO buttons
			    // We kill services only on YES
			    int dialogResult =
				JOptionPane.showConfirmDialog(frame,
							     coreProblemArray,
							     "Check IPOPP Services",
							     JOptionPane.YES_NO_OPTION,
							     JOptionPane.WARNING_MESSAGE);
			    if(dialogResult == JOptionPane.YES_OPTION) {
				// User wants us to kill things
				// Recycle spm, steal code from StartStopStuff
				spm = new SimpleProgressMonitor(frame,
								"Restoring System Health",
								"Stopping services...",
								100,
								"^Stopping ");
				spm.bumpState();
				int numStations = countConfiguredStations();
				// Event count is services to stop (core + SPAs) + 2
				int newmax = CoreService.count() + numStations + 2;
				spm.setMaximum(newmax);
				String killCommand = new File(toolsDir, "kill_services.sh").getCanonicalPath();
				runCommand(killCommand, spm);
				// spm should still be open; bump it
				spm.bumpState();
				// Forcibly close in case we missed an event somehow
				spm.close();
				// We're done - ask if we should start things up
				if(JOptionPane.showConfirmDialog(frame,
								 "System Health Restored. Start IPOPP Services now?",
								 "Check IPOPP Services",
								 JOptionPane.YES_NO_OPTION)
				   == JOptionPane.YES_OPTION)
				    StartStopServices(true);
			    }
			}
			else {
			    // Just show it
			    JOptionPane.showMessageDialog(frame,
							  coreProblemArray,
							  "Check IPOPP Services",
							  JOptionPane.WARNING_MESSAGE);
			}	
		    }
		}
		catch (Exception e) {
		    JOptionPane.showMessageDialog(frame,e.toString(),"Error",
						  JOptionPane.ERROR_MESSAGE);
		}
	    }
	}.start();
    }


    /**
     * Runs reset_ipopp.sh, and watches for its "done." messages
     * Do all this in a separate Thread because Java's
     * GUI libraries like it better that way...
     */
    private void ResetIPOPP()
    {
	new Thread() {
	    public void run() {
		try {
		    String launchCommand = new File(toolsDir, "reset_ipopp.sh").getCanonicalPath();
		    
		    SimpleProgressMonitor spm =
			new SimpleProgressMonitor(frame,
						  "Resetting IPOPP",
						  "Resetting IPOPP...",
						  5,
						  "^done\\.");
		    // We expect 3 "done." events from reset_ipopp.sh, and
		    // we'll delay a little bit after running it so the user
		    // can see the final message
		    Thread.sleep(1000);
		    spm.bumpState();
		    // The return code from reset_ipopp.sh is not useful
		    // So, we ignore it.
		    int retcode = runCommand(launchCommand, spm);
		    spm.setNote("Finishing IPOPP Reset...");
		    // Check everybody's status now, to insure we notice
		    // everyone is down
		    requestBackgroundUpdate();
		    Thread.sleep(2000);
		    spm.bumpState();
		    // Forcibly close in case we missed an event somehow
		    spm.close();
		    JOptionPane.showMessageDialog(frame,
						  "IPOPP Reset complete",
						  "Resetting IPOPP",
						  JOptionPane.INFORMATION_MESSAGE);
		}
		catch (Exception e) {
		    JOptionPane.showMessageDialog(frame,e.toString(),"Error",
						  JOptionPane.ERROR_MESSAGE);
		}
	    }
	}.start();
    }	
    
    private void installSPA(final String installtarball)
    {
	new Thread() {
	    public void run() {
		try {
		    String launchCommand = new File(toolsDir, "install_spa.sh "+installtarball).getCanonicalPath();
		    
		    SimpleProgressMonitor spm =
			new SimpleProgressMonitor(frame,
						  "Installing SPA",
						  "Installing SPA",
						  5,
						  "^done\\.");
		    
		    Thread.sleep(1000);
		    spm.bumpState();
		    int retcode = runCommand(launchCommand, spm);
		    spm.setNote("Finishing SPA Installation...");
		    // Check everybody's status now, to insure we notice
		    // everyone is down
		    requestBackgroundUpdate();
		    Thread.sleep(1000);
		    spm.bumpState();
		    // Forcibly close in case we missed an event somehow
		    spm.close();
		    if (retcode==0)
		      JOptionPane.showMessageDialog(frame,
						  installtarball+" SPA Installation complete",
						  "Installing SPA",
						  JOptionPane.INFORMATION_MESSAGE);
		    else 
		    	JOptionPane.showMessageDialog(frame,
						  "Error: SPA installation failed",
						  "Installing SPA",
						  JOptionPane.INFORMATION_MESSAGE);
		
		}
		catch (Exception e) {
		    JOptionPane.showMessageDialog(frame,e.toString(),"Error",
						  JOptionPane.ERROR_MESSAGE);
		}
	    }
	}.start();
    }	
    
    
    private void preconfigureH2G(final String projchoice)
    {
	new Thread() {
	    public void run() {
		try {
		    String launchCommand = new File(toolsDir, "preconfigureH2G.sh "+projchoice).getCanonicalPath();
		    
		    SimpleProgressMonitor spm =
			new SimpleProgressMonitor(frame,
						  "Configuring Projection",
						  "Configuring Projection",
						  5,
						  "^done\\.");
		    // We expect 3 "done." events from reset_ipopp.sh, and
		    // we'll delay a little bit after running it so the user
		    // can see the final message
		    Thread.sleep(1000);
		    spm.bumpState();
		    // The return code from reset_ipopp.sh is not useful
		    // So, we ignore it.
		    int retcode = runCommand(launchCommand, spm);
		    spm.setNote("Finishing Projection Configuration...");
		    // Check everybody's status now, to insure we notice
		    // everyone is down
		    requestBackgroundUpdate();
		    Thread.sleep(1000);
		    spm.bumpState();
		    // Forcibly close in case we missed an event somehow
		    spm.close();
		    if (retcode==0)
		      JOptionPane.showMessageDialog(frame,
						  projchoice+" Projection Configuration complete",
						  "Configuring Projection",
						  JOptionPane.INFORMATION_MESSAGE);
		    else if (retcode==1)
		    	JOptionPane.showMessageDialog(frame,
						  "Error: H2G not installed",
						  "Configuring Projection",
						  JOptionPane.INFORMATION_MESSAGE);
		    else if (retcode==2)
		    	JOptionPane.showMessageDialog(frame,
						  "Error: Incorrect Input",
						  "Configuring Projection",
						  JOptionPane.INFORMATION_MESSAGE);
		    else 
		    	JOptionPane.showMessageDialog(frame,
						  "Error",
						  "Configuring Projection",
						  JOptionPane.INFORMATION_MESSAGE);
		}
		catch (Exception e) {
		    JOptionPane.showMessageDialog(frame,e.toString(),"Error",
						  JOptionPane.ERROR_MESSAGE);
		}
	    }
	}.start();
    }	
    
    private void preconfigureTiles(final String argumentlist)
    {
	new Thread() {
	    public void run() {
		try {
		    String launchCommand = new File(toolsDir, "preconfigureTiles.sh "+argumentlist).getCanonicalPath();
		    
		    SimpleProgressMonitor spm =
			new SimpleProgressMonitor(frame,
						  "Configuring Tiles",
						  "Configuring Tiles",
						  5,
						  "^done\\.");
		    // We expect 3 "done." events from reset_ipopp.sh, and
		    // we'll delay a little bit after running it so the user
		    // can see the final message
		    Thread.sleep(1000);
		    spm.bumpState();
		    // The return code from reset_ipopp.sh is not useful
		    // So, we ignore it.
		    int retcode = runCommand(launchCommand, spm);
		    spm.setNote("Finishing Tile Configuration...");
		    // Check everybody's status now, to insure we notice
		    // everyone is down
		    requestBackgroundUpdate();
		    Thread.sleep(1000);
		    spm.bumpState();
		    // Forcibly close in case we missed an event somehow
		    spm.close();
		    if (retcode==0)
		      JOptionPane.showMessageDialog(frame,
						  "Tile Configuration complete",
						  "Configuring Tiles",
						  JOptionPane.INFORMATION_MESSAGE);
		    else if (retcode==2)
		    	JOptionPane.showMessageDialog(frame,
						  "Error: Required SPAs not installed",
						  "Configuring Tiles",
						  JOptionPane.INFORMATION_MESSAGE);
		    else if (retcode==3)
		    	JOptionPane.showMessageDialog(frame,
						  "Error: No Tiles Selected",
						  "Configuring Tiles",
						  JOptionPane.INFORMATION_MESSAGE);
		    else if (retcode==4)
		    	JOptionPane.showMessageDialog(frame,
						  "Error: Incorrect Projection Choice",
						  "Configuring Tiles",
						  JOptionPane.INFORMATION_MESSAGE);
		    else 
		    	JOptionPane.showMessageDialog(frame,
						  "Error",
						  "Configuring Tiles",
						  JOptionPane.INFORMATION_MESSAGE);
		}
		catch (Exception e) {
		    JOptionPane.showMessageDialog(frame,e.toString(),"Error",
						  JOptionPane.ERROR_MESSAGE);
		}
	    }
	}.start();
    }	

    /**
     * Creates a new Station display object and registers it with the
     * global maps for lookup by directory or event.
     */
    private Station createStation (MonitorPanel mp, String label, String directory, String event, String infoText) {
	Station newStation = new Station(mp, label, directory, infoText);
	
	List<Station> lm = dir2station.get(directory);
	if(lm == null) {
	    lm = new LinkedList<Station>();
	    dir2station.put(directory, lm);
	}
	lm.add(newStation);

	lm = event2station.get(event);
	if(lm == null) {
	    lm = new LinkedList<Station>();
	    event2station.put(event, lm);
	}
	lm.add(newStation);

	// At first creation, force installation check/update
	newStation.checkInstalled(true);
	return newStation;
    }

    /**
     * Reads a configuration file and sets the GUI state to match.
     * 
     */
    private void readConfigFile (File dcFile)
	throws Exception
    {
	BufferedReader br = new BufferedReader(new FileReader(dcFile));
	String line;
	try {
	    while ((line = br.readLine()) != null) {
		// Check to see if this line is commented out,
		// and trim off leading "#"
		boolean commentP = false;
		if(line.startsWith("#")) {
		    commentP = true;
		    while(line.startsWith("#")) {
			line = line.substring(1);
		    }
		}
		// Trim off leading/trailing white space
		line = line.trim();
		
		// Look it up in the stations bucket
		List<Station> lm = dir2station.get(line);
		
		if(lm != null) {
		    for (Station s : lm) {
			s.setEnabled(!commentP);
		    }
		}
	    }
	}
	finally {
	    br.close();
	}
    }

    /**
     * Writes a config file based on the input file and the current GUI state
     */
    private void writeConfigFile (File dcFile)
	throws Exception
    {
	BufferedReader br = new BufferedReader(new FileReader(dcFile));
	File outFile = File.createTempFile(dcFile.getName(), ".temp", dcFile.getParentFile());
	PrintWriter pr = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
	String inLine;
	try {
	    while ((inLine = br.readLine()) != null) {
		// Keep the original in inLine
		String line = inLine;
		// Check to see if this line is commented out,
		// and trim off leading "#"
		boolean commentP = false;
		if(line.startsWith("#")) {
		    commentP = true;
		    while(line.startsWith("#")) {
			line = line.substring(1);
		    }
		}
		// Trim off leading/trailing white space
		line = line.trim();

		// Look it up in the stations bucket
		List<Station> lm = dir2station.get(line);

		if(lm != null) {
		    // A Station is written out ON iff it is enabled and installed
		    // otherwise it's written out OFF (commented out)
		    if(lm.get(0).getEnabled() && lm.get(0).getInstalled())
			pr.println(line);
		    else
			pr.println("#" + line);
		}
		else {
		    // This line is just a comment, pass it through
		    pr.println(inLine);
		}
	    }
	}
	finally {
	    pr.close();
	    br.close();
	    outFile.renameTo(dcFile);
	}
    }

    /****************************************************************************
     * logEvent.
     ****************************************************************************/
    public void logEvent (LogEvent event) {
	// We only log events when not editing
	if(!isEditor) {
	    // If this event is the creation of a Pass, top the appropriate tab
	    // (only if topTabOnNewPass is true)
	    if (topTabOnNewPass) {
		for (MonitorPanel mp : monitorPanels) {
		    int myIndex = mp.newPass_p(event);
		    if (myIndex != -1) {
			tabbedPane.setSelectedIndex(myIndex);
			// Have the MonitorPanel check if anything in it
			// is active, and reset itself if not
			if(!mp.monitorIsRunning())
			    mp.resetMonitors();
			// The gyrations you have to do to get a date formatted...
			//StringBuffer sb = new StringBuffer();
			//SimpleDateFormat sf = new SimpleDateFormat("MM-dd HH:mm:ss");
			//sb = sf.format(new Date(), new StringBuffer(), new FieldPosition(0));
			// mp.addInfoPanelText(sb.toString());
		    }
		}
	    }

	    // If this event matches a list of Stations, pass it off to them
	    // The event tag string is of the form:
	    // NCS/<station-name>/<group-tag>
	    // We lop the <group-tag> off to allow events with the same
	    // <station-name> but different <group-tag>s to go to the same
	    // place
	    String eventTag = event.getSource().toString();
	    int lastSlash = eventTag.lastIndexOf('/');
	    eventTag = eventTag.substring(0, lastSlash+1);
	    List<Station> lm = event2station.get(eventTag);
	    if (lm != null) {
		for (Station monitor : lm) {
		    monitor.logEvent(event);
		}
	    }
	}
    }
    

    /**
     * StationMouseListener - notices mouse entry/exit on Station display
     * and lights it up.  Also allows station enable/disable.  Only
     * takes effect when in editing mode.
     */
    private class StationMouseListener
	implements MouseListener
    {
	private Station station;

	StationMouseListener (Station station) {
	    this.station = station;
	    station.addMouseListener(this);
	}

	public void mousePressed(MouseEvent e) {
	    //System.err.println(station);
	}
     
	public void mouseReleased(MouseEvent e) {
	    if(isEditor) {
		if(station.getInstalled()) {
		    station.setEnabled(!station.getEnabled());
		    station.updateFromState();
		}
	    }
	}
     
	public void mouseEntered(MouseEvent e) {
	    if(isEditor) {
		station.lightUp();
		//System.err.println(station);
	    }
	}
     
	public void mouseExited(MouseEvent e) {
	    if(isEditor) {
		station.lightOff();
	    }
	}
     
	public void mouseClicked(MouseEvent e) {
	}
    }

    /**
     * States a Station can be in
     */
    private enum RunState {
	STOPPED,	// Not running - JSW status 0
	FAILED,		// Not running - JSW status not 0 or 1
	HUH,		// Multiple stations, some running, some not
	READY,		// Running - JSW status 1
	STAGING,	// Staging - setting up to run algorithm
	PROCESSING,	// Processing - executing algorithm
	COMPLETE,	// Complete - algorithm succeeded
	WARNING,	// Warning - "normal" failure (e.g. night pass)
	ERROR		// Error - abnormal algorithm failure
    }

    /****************************************************************************
     * Station.
     ****************************************************************************/
    private class Station extends HPanel {
	private String name;
	private String directory;
	//private String text = null;
	private String infoText = null;
	/**
	 * Foreground text color
	 */
	private Color color = Color.WHITE;
	private ImageIcon[] icons = null;
	private long activeAt = 0;	// Time when station "started"
	private long completedAt = 0;
	private int runningCount = 0;	// Number of stations "running"
	private RoundButton infoButton = null;
	private static final long serialVersionUID = 1L;

	// State variables
	private boolean installedP;
	private boolean enabledP;
	private RunState runState = RunState.STOPPED;
	public RunState getState() { return runState;}

	private MonitorPanel mp;

	public Station (MonitorPanel mp, String name, String directory, String infoText) {
	    this.mp = mp;
	    this.name = name;
	    this.directory = directory;
	    this.infoText = infoText;
	    new StationMouseListener(this);
	    //setLayout(null);
	    // The button used to contains Unicode 24D8 ('i' in a circle),
	    // encoded as "\u24D8"
	    infoButton = new RoundButton("i");
	    infoButton.addActionListener(new InfoButtonListener());
	    infoButton.setPreferredSize(new Dimension(20,20));
	    setLayout(new LowRightLayout(this));
	    add(infoButton);
	}

	public void setInstalled(boolean s) {
	    installedP = s;
	}

	public void setEnabled(boolean s) {
	    enabledP = s;
	}

	public boolean getEnabled() { return enabledP; }

	public boolean getInstalled() { return installedP; }

	/**
	 * Notes a new station starting to run.  Triggered by an event that
	 * says processing started.  Returns true if nothing was running
	 * before this call.
	 */
	synchronized private boolean addProcessing()
	{
	    boolean result = false;
	    runningCount++;
	    if (runningCount == 1) {
		activeAt =  System.currentTimeMillis();
		completedAt = 0;
		result = true;
	    }
	    return result;
	}

	/**
	 * Notes a station stopping.  Triggered by events that say normal
	 * or abnormal termination.  Returns true if nothing is running
	 * after this call.
	 */
	synchronized private boolean removeProcessing()
	{
	    boolean result = false;
	    --runningCount;
	    if(runningCount <= 0) {
		runningCount = 0;
		completedAt =  System.currentTimeMillis();
		result = true;
	    }
	    return result;
	}

	/**
	 * Forces runningCount to zero.
	 */
	synchronized private void clearProcessing()
	{
	    runningCount = 0;
	    completedAt = activeAt =0;
	}

	/**
	 * Sets Station color and icon from its state slots.
	 */
	public void updateFromState() {
	    if(isEditor) {
		// We're config editing - only legit states are
		// not installed, not enabled, enabled
		if(installedP) {
		    if(enabledP){
			//color = Color.green;
			icons = GUtil.editEnabledIcons;
		    }
		    else {
			//color = color.yellow;
			icons = GUtil.editDisabledIcons;
		    }
		}
		else {
		    //color =  Color.lightGray;
		    icons = GUtil.notInstalledIcons;
		}
	    }
	    else {
		// We're monitoring - there are lots of possible states
		// This will likely get more complex later
		if (installedP) {
		    if(enabledP) {
			//color = Color.green;
			switch(runState) {
			case STOPPED:
			    //color = color.yellow;
			    icons = GUtil.processOFFIcons;
			    break;
			case FAILED:
			    //color = Color.red;
			    icons = GUtil.didNotRunIcons;
			    break;
			case HUH:
			    //color = Color.red;
			    icons = GUtil.HuhIcons;
			    break;
			case READY:
			    //color = GUtil.LIGHT_BLUE;
			    icons = GUtil.processONIcons;
			    break;
			case STAGING:
			    //color = GUtil.LIGHT_BLUE;
			    icons = GUtil.stagingIcons;
			    break;
			case PROCESSING:
			    //color = GUtil.LIGHT_BLUE;
			    icons = GUtil.runningIcons;
			    break;
			case COMPLETE:
			    //color = Color.green;
			    icons = GUtil.successIcons;
			    break;
			case WARNING:
			    //color = Color.YELLOW;
			    icons = GUtil.warningIcons;
			    break;
			case ERROR:
			    //color = Color.RED;
			    icons = GUtil.errorIcons;
			    break;
			}
		    }
		    else {
			//color = Color.yellow;
			icons = GUtil.processOFFIcons;
		    }
		}
		else {
		    //color = Color.lightGray;
		    icons = GUtil.notInstalledIcons;
		}
	    }
	    // No matter what, turn our border off
	    setBorder(null);
	    repaint();
	}
		    

	public void lightUp() {
	    //color = Color.WHITE;
	    setBorder(litBorder);
	    mp.lightConnections(this, true);
	    //mp.drawStationConnectors(this, Color.red);
	    repaint();
	}

	public void lightMeUp() {
	    //color = Color.BLUE;
	    setBorder(litBorder);
	}

	public void lightOff() {
	    mp.lightConnections(this, false);
	    updateFromState();
	    //mp.drawStationConnectors(this, Color.black);
	    //repaint();
	}

	
	public void logEvent (LogEvent event) {

	    // To make code elsewhere simpler - if we have an event dispatched
	    // to this station, for display purposes it is installed and
	    // enabled
	    installedP = true;
	    enabledP = true;

	    boolean weChanged = false;

	    switch (event.getLevel()) {
	    case Log.INFO_EVENT: {
		if (event.getText().contains("Putting reserved object") ||	// XSLT generated
		    event.getText().contains("got product")) {		// Charlie generated
		    runState = RunState.STAGING;
		    weChanged = true;
		    break;
		}
		if (event.getText().contains("launching algorithm") ||	// XSLT generated
		    event.getText().contains("program starting")) {	// Charlie generated
		    if (addProcessing()) {
			runState = RunState.PROCESSING;
			weChanged = true;
		    }
		    break;
		}
		if (event.getText().contains("Done with algorithm") || // XSLT generated
		    event.getText().contains("program ending")) {      // Charlie generated
		    // Normal completion
		    if (removeProcessing()) {
			// If our current state is WARNING or ERROR,
			// don't change it.
			// This supports stations that actually represent
			// multiple SPA stations underneath.
			if(runState != RunState.WARNING
			   && runState != RunState.ERROR) {
			    runState = RunState.COMPLETE;
			    weChanged = true;
			}
		    }
		    break;
		}
		break;
	    }
		
	    case Log.WARNING_EVENT: {
		// In our system, warnings are always followed by normal
		// completion events (!), so do not do a removeProcessing()
		// here
		runState = RunState.WARNING;
		weChanged = true;
		break;
	    }
	    case Log.ERROR_EVENT: {
		removeProcessing();
		runState = RunState.ERROR;
		weChanged = true;
		break;
	    }
	    }
	    if (weChanged) {
		updateFromState();
		//repaint();
	    }
	}
	
	public void paint (Graphics g) {
	    int w = getWidth();
	    int h = getHeight();
	    FontMetrics fm = g.getFontMetrics();
	    g.setColor(Color.BLACK);
	    g.fillRect(0,0,w,h);
	    g.setColor(color);
	    int y = 15;
	    for (String part : name.split("\\|")) {
		g.drawString(part,(w-fm.stringWidth(part))/2,y);
		y += 13;
	    }
	    if (icons != null) {
		Image image = icons[count].getImage();
		g.drawImage(image,(w-image.getWidth(null))/2,(h-image.getHeight(null))/2,null);
	    }
	    // Timer - do not paint in Editor mode
	    if (activeAt != 0 && !isEditor) {
		String elapsed;
		if (completedAt != 0) {
		    elapsed = encodeElapsed(completedAt-activeAt);
		}
		else {
		    elapsed = encodeElapsed(System.currentTimeMillis()-activeAt);
		}
		g.setColor(GUtil.LIGHT_BLUE);
		g.drawString(elapsed,(w-fm.stringWidth(elapsed))/2,h-7);
	    }
	    paintBorder(g);
	    paintComponents(g);
	}
	/**
	 * Checks whether this monitor believes its underlying
	 * station is running
	 */
	public boolean isRunning() {
	    return runState == RunState.STAGING || runState == RunState.PROCESSING;
	}

	/**
	 * Checks whether this monitor's current state is not executing.
	 */
	public boolean isNotExecuting() {
	    return runState == RunState.STOPPED
		|| runState == RunState.FAILED
		|| runState == RunState.HUH;
	}

	DecimalFormat lz2 = new DecimalFormat("00");
	private String encodeElapsed (long ms) {
	    int secs = (int) ((ms / 1000) % 60);
	    int mins = (int) ((ms / 1000) / 60) % 60;
	    int hours = (int) ((ms / 1000) / 60 / 60);
	    return Integer.toString(hours)
		+ ":" + lz2.format(mins)
		+ ":" + lz2.format(secs);
	}

	/**
	 * The non-nio way to see if any files match a given pattern.
	 */
	class GlobPattern implements FilenameFilter
	{
	    Pattern globPattern;

	    public GlobPattern (String gp) {
		// Translate from glob regexp to Java regexp
		// (we simply change unescaped "*" to ".*")
		// (well, as "simply" as you can mung a regexp with a regexp)
		String rp = gp.replaceAll("([^\\\\])\\*", "$1.*");
		// And just to be completely paranoid, catch the case
		// where the string begins with "*"
		if (rp.startsWith("*"))
		    rp = "." + rp;
		// We insist on anchored matching, of course
		rp = "^" + rp + "$";

		// Initialize the Pattern
		globPattern = Pattern.compile(rp);
	    }
	    
	    public boolean accept(File dir, String name) {
		return globPattern.matcher(name).find();
	    }
	}

	/**
	 * Checks to see if this Station is installed
	 * and changes its display state appropriately.
	 * A station is installed if its directory string names a
	 * directory (straight up, or as a glob-pattern)
	 */
	public void checkInstalled(boolean updateNoMatterWhat)
	{
	    if(ncsStationDir != null) {
		boolean hasDir = false;
		File[] matchedDir = ncsStationDir.listFiles(new GlobPattern(directory));
		for (File df : matchedDir) {
		    if(df.isDirectory()) {
			hasDir = true;
			break;
		    }
		}
		if (updateNoMatterWhat || getInstalled() != hasDir) {
		    setInstalled(hasDir);
		    updateFromState();
		}
	    }
	}

	public void checkInstalled() { checkInstalled(false); }

	/**
	 * Checks this station's run status, by checking the return code
	 * of jsw/bin/wrapper.sh, and updates the station's display status
	 * accordingly.  We attempt to handle directory patterns by checking
	 * all the matching directories and spitting if something is in
	 * the wrong state.
	 */
	public void checkRunStatus(SimpleProgressMonitor spm, boolean justChanged, boolean shouldBeRunning)
	{
	    // Check ncsStationDir just for paranoia - this should
	    // not be called if it's not set
	    if(ncsStationDir != null) {
		if(getEnabled()) {
		    try {
			File[] matchedDir = ncsStationDir.listFiles(new GlobPattern(directory));
			// We know how many directories we're going to check
			int dirCount = matchedDir.length;
			int runCount = 0;
			for (File df : matchedDir) {
			    String jswCmd = df.getCanonicalPath()
				+ "/jsw/bin/wrapper.sh status";
			    if(spm != null)
				spm.setNote("Checking " + name + "...");
			    int retcode = runCommand(jswCmd);
			    if(spm != null)
				spm.bumpState();
			    if(retcode == 0)
				runCount++;
			}
			// There are three possibilities here:
			// runCount == 0
			// 	everyone is off, STOPPED or FAILED
			// runCount == dirCount
			//	everybody is running, READY
			// Otherwise
			//	some are running, some aren't, HUH

			if(runCount == dirCount) {
			    // Running
			    if(justChanged) {
				if(!shouldBeRunning) {
				    // running, but should have just stopped
				    // Do we have/need an icon for this?
				    // Or should we punt
				    // and claim we're READY?
				    runState = RunState.READY;
				}
				else {
				    // running, and should have just started,
				    // so we go to READY
				    // (if current state is not running;
				    //  never clobber an executing state
				    //  with READY)
				    if(isNotExecuting())
					runState = RunState.READY;
				}
			    }
			    else {
				// If current state is not running,
				// change to READY
				// Otherwise, leave it alone
				if(isNotExecuting())
				    runState = RunState.READY;
			    }
			}
			else if (runCount == 0) {
			    // Not running
			    // No matter what, stomp the Processing
			    clearProcessing();
			    if(justChanged) {
				if(shouldBeRunning) {
				    // Not running, but should have just started
				    runState = RunState.FAILED;
				}
				else {
				    // Not running, and should have just stopped
				    runState = RunState.STOPPED;
				}
			    }
			    else {
				// If current state is FAILED, leave it
				// otherwise slap it to STOPPED
				if (runState != RunState.FAILED)
				    runState = RunState.STOPPED;
			    }
			}
			else
			    // Some are running, some not; this is always '?'
			    runState = RunState.HUH;
		    }
		    catch (Exception e) {};
		}
		else
		    runState = RunState.STOPPED;
		updateFromState();
	    }
	}

	/**
	 * For debugging
	 */
	public String toString() {
	    // Unicode 2205 = circle with slash
	    return name + ":" + (enabledP ? " ON" : " OFF") + (installedP ? "" : " \u2205") + (icons == null ? " NULL" : " SET");
	}

    /**
     * Create our InfoWindow
     */
	public InfoWindow createInfoWindow()
	{
	    //String infoText = "<html><body><table border=1><tr><td>FOO<td>BAR<tr><td>MUCH LONGER<td>And Longer Still</table></body></html>";
	    if (infoWindow != null)
		infoWindow.dispose();
	    infoWindow = new InfoWindow(frame, name, infoText);
	    return infoWindow;
	}
    }

    /**
     * Button listener for info button
     */
    private class InfoButtonListener implements java.awt.event.ActionListener
    {
        public void actionPerformed(ActionEvent event)
        {
            JComponent s = (JComponent)event.getSource();
	    // Our parent component here is the station (yay!)
	    Station st = ((Station)(s.getParent()));
	    Point loc = s.getLocationOnScreen();
	    InfoWindow iw = st.createInfoWindow();
	    iw.setLocation(loc);
	    iw.setVisible(true);
        }
    }
    /****************************************************************************
     * InfoPanel - special station block used for global status stuff
     ****************************************************************************/
    private class InfoPanel extends HPanel {
	private ArrayList<String> text;
	private Color color;
	private static final long serialVersionUID = 1L;
	public InfoPanel (Color color) {
	    this.text = new ArrayList<String>();
	    this.color = color;
	}
	public void paint (Graphics g) {
	    int w = getWidth();
	    int h = getHeight();
	    //g.setFont(new Font("SansSerif",Font.BOLD,18));
	    FontMetrics fm = g.getFontMetrics();
	    g.setColor(Color.BLACK);
	    g.fillRect(0,0,w,h);
	    g.setColor(color);
	    int y = 15;
	    for (String part : text) {
		g.drawString(part,(w-fm.stringWidth(part))/2,y);
		y += 13;
	    }
	}
	public void addText(String text) {
	    this.text.add(text);
	}
	public void clearText() {
	    this.text.clear();
	}
    }

    /**
     * A special InfoPanel to track the status of certain CoreServices
     */
    private class MoverPanel extends HPanel {
	private CoreService cs = null;
	private Color color = Color.WHITE;
	private ImageIcon[] icons = null;
	private static final long serialVersionUID = 1L;

	/**
	 * Looks up a CoreService by SLS name to initialize
	 */
	public MoverPanel(String slsName) throws Exception
	{
	    cs = CoreService.getBySLSName(slsName);
	    if(cs == null)
		throw new Exception("No CoreService with SLS name " + slsName);
	}	
	public void paint (Graphics g) {
	    if(!isEditor) {
		int w = getWidth();
		int h = getHeight();
		FontMetrics fm = g.getFontMetrics();
		g.setColor(Color.BLACK);
		g.fillRect(0,0,w,h);
		g.setColor(color);
		int y = 15;
		String label = cs.getLabel();
		g.drawString(label,(w-fm.stringWidth(label))/2,y);

		if (icons != null) {
		    Image image = icons[count].getImage();
		    g.drawImage(image,(w-image.getWidth(null))/2,(h-image.getHeight(null))/2,null);
		}
		paintBorder(g);
		paintComponents(g);
	    }
	}

	/**
	 * Checks the status of the appropriate CoreService (if we are on
	 * the localhost) and updates its display
	 */
	public void update()
	{
	    if(ipoppRootDir != null) {
		String statusCommand = ipoppRootDir.getPath()
		    + cs.getPath() + " status";
		// If the statusCommand fails, just ignore it
		try {
		    int status = runCommand(statusCommand);
		    icons = ((status == 0) ?
			     GUtil.processONIcons
			     : GUtil.processOFFIcons);
		}
		catch (Exception e) {};
	    }
	}
    }
	    
	    
	    
    /****************************************************************************
     * Animate.
     ****************************************************************************/
    private class Animate implements ActionListener {
	public void actionPerformed (ActionEvent ae) {
	    count = (count+1) % 8;
	    repaint();
	}
    }
    /****************************************************************************
     * NullPanel.
     ****************************************************************************/
    private class NullPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	public NullPanel () {

	}
	public void paint (Graphics g) {

	}
    }

    /****************************************************************************
     * MonitorScrollPane
     ****************************************************************************/
    // Each MonitorPanel lives inside a JScrollPane, like this:
    private class MonitorScrollPane extends JScrollPane {
	private MonitorPanel mp;

	public MonitorPanel getMonitorPanel() { return mp; }

	public MonitorScrollPane (int columns,
				  String tabName,
				  String moverName, String moverCall) {
	    super(new MonitorPanel(columns, tabName, moverName, moverCall),
		  VERTICAL_SCROLLBAR_ALWAYS,
		  HORIZONTAL_SCROLLBAR_ALWAYS);
	    mp = (MonitorPanel)(viewport.getView());
	    // There will no doubt be default size settings, etc...
	}
    }
	    
    /****************************************************************************
     * MonitorPanel.
     ****************************************************************************/
    private class MonitorPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	// Parameters for grid layout
	private static final int HGAP = 10;
	private static final int VGAP = 20;

	private String tabName;
	private String moverSource;
	private String moverNewPassFlag1;
	private String moverNewPassFlag2;
	private String moverOldPassFlag;
	private int columns;
	private int rows;
	private int tabIndex;
	private int[][] connectors;

	private MoverPanel moverPanel;

	public MonitorPanel (int columns,
			     String tabName,
			     String moverName, String moverCall) {
	    super(new GridLayout(0,columns,HGAP,VGAP));
	    this.columns = columns;
	    rows = 0;
	    this.tabName = tabName;
	    moverSource = "DSM/" + moverName + "/" + moverCall;
	    // 2014-01-28 - For difficult to determine reasons, RDRMover
	    // emits the PassFlag2 form.
	    moverNewPassFlag1 = moverCall + ": New pass --";
	    moverNewPassFlag2 = moverCall + ": creating new pass:";
	    moverOldPassFlag = moverCall + ": pass already exists"; 
	}

	public int getRows() { return rows; }
	public int getColumns() { return columns; }
	public String getTabName() { return tabName; }

	public void setTabIndex (int tabIndex) {
	    this.tabIndex = tabIndex;
	}

	public void forciblyUpdate() {
	    for (Component c : getComponents()) {
		if (c instanceof Station) {
		    ((Station)c).updateFromState();
		}
	    }
	}

	public void setBestSize (int bestRows, int bestColumns)
	{
	    Dimension bestSize = new Dimension(bestColumns * bestWidth,
					       bestRows * bestHeight);
	    //setMinimumSize(bestSize);
	    setMaximumSize(bestSize);
	    setPreferredSize(bestSize);
	}

	/**
	 * If this event is the creation of a Pass relevant to this panel,
	 * return the tabIndex (so the main event loop can focus the tab).
	 * Otherwise return -1.
	 */
	public int newPass_p(LogEvent event) {

	    if (event.getSource().toString().equals(moverSource)
		&& (event.getText().contains(moverNewPassFlag1) ||
		    event.getText().contains(moverNewPassFlag2) ||
		    event.getText().contains(moverOldPassFlag))) {
		return tabIndex;
	    }
	    else return -1;
	}

	/**
	 * Return true if any of our Components is a running
	 * Station
	 */
	public boolean monitorIsRunning() {
	    for (Component c : getComponents()) {
		if (c instanceof Station) {
		    if (((Station)c).isRunning()) {
			return true;
		    }
		}
	    }
	    return false;
	}

	/**
	 * Return all the station monitors to clear
	 */
	public void resetMonitors() {
	    for (Component c : getComponents()) {
		if (c instanceof Station) {
		    ((Station)c).updateFromState();
		}
	    }
	    moverPanel.update();
	}

	/**
	 * Checks the install status of all Stations
	 */
	public void checkInstalled()
	{
	    for (Component c : getComponents()) {
		if (c instanceof Station) {
		    ((Station)c).checkInstalled();
		}
	    }
	}

	/**
	 * Has each station check its running status.
	 * We only do this for stations that are enabled
	 */
	public void checkStationRunStatus(SimpleProgressMonitor spm, boolean justChanged, boolean shouldBeRunning)
	{
	    for (Component c : getComponents()) {
		if (c instanceof Station) {
		    Station st = (Station)c;
		    if(st.getEnabled()) {
			st.checkRunStatus(spm, justChanged, shouldBeRunning);
		    }
		}
	    }
	}

	public void checkStationRunStatus(SimpleProgressMonitor spm) {
	    checkStationRunStatus(spm, false, false);
	}

	/**
	 * Count the number of enabled stations
	 */
	public int countEnabledStations()
	{
	    int result = 0;
	    for (Component c : getComponents()) {
		if (c instanceof Station) {
		    if(((Station)c).getEnabled())
			++result;
		}
	    }
	    return result;
	}

	public void paint (Graphics g) {
	    super.paint(g);
      
	    Component[] components = getComponents();
	    if (components.length > 0)
		{
		    for (int i = 0; i < connectors.length; i++)
			{
			    Component c1 = getComponent(connectors[i][0]);
			    Component c2 = getComponent(connectors[i][1]);
			    connectComponents(g, c1, c2, Color.BLACK);
			}
		}
	    paintChildren(g);
	}


	/**
	 * Draw a line connecting the two components on the screen
	 * NOTE that this changes the Graphics context color as a side effect.
	 */
	public void connectComponents (Graphics g, Component c1, Component c2, Color color)
	{
	    g.setColor(color);
	    int x1 = c1.getLocation().x+(c1.getSize().width/2);
	    int y1 = c1.getLocation().y+c1.getSize().height;
	    int x2 = c2.getLocation().x+(c2.getSize().width/2);
	    int y2 = c2.getLocation().y-1;
	    if (x1 == x2) {
		// Boxes are in same column - draw straight down
		// regardless of length
		g.drawLine(x1,y1,x2,y2);
	    }
	    else if (Math.abs(y2 - y1) <= VGAP) {
		// One row apart - simple wiggle between rows
		int y = y1 + VGAP/2;
		g.drawLine(x1, y1, x1, y);
		g.drawLine(x1, y,  x2, y);
		g.drawLine(x2, y,  x2, y2);
	    }
	    else {
		// 1+ rows and cols apart - put two bends
		// in and hope that's enough
		int ytop = y1 + VGAP/2;
		int ybot = y2 - VGAP/2;
		int dir = (x2 < x1 ? -1 : 1);
		int xmid = x1 + dir * (c1.getSize().width + HGAP)/2;
		g.drawLine(x1,   y1,   x1,   ytop);
		g.drawLine(x1,   ytop, xmid, ytop);
		g.drawLine(xmid, ytop, xmid, ybot);
		g.drawLine(xmid, ybot, x2,   ybot);
		g.drawLine(x2,   ybot, x2,   y2);
	    }
	}

	/**
	 * Light up the parents and children of a given Station
	 */
	public void lightConnections (Component c, boolean lightP)
	{
	    // Dredge up the Graphics context
	    Graphics g = getGraphics();
	    // Look up the Component's number
	    int cnum = getComponentZOrder(c);
	    if (cnum >= 0) {
		// Walk the connectors and draw lines for anything
		// mentioning this Component
		for(int i=0; i < connectors.length; i++) {
		    Station otherc = null;
		    if(connectors[i][0] == cnum) {
			otherc = (Station)getComponent(connectors[i][1]);
		    }
		    else if (connectors[i][1] == cnum) {
			otherc = (Station)getComponent(connectors[i][0]);
		    }
		    if(otherc != null) {
			if(lightP) {
			    otherc.lightMeUp();
			}
			else {
			    otherc.updateFromState();
			}
		    }
			    
		}
	    }
	    else
		System.err.println("WTF? not our Component?");
	}

	public void loadMonitors (Node configRoot) throws Exception {
	    removeAll();

	    int nodeCount = 0;
	    // turn all the Node nodes into monitor panels
	    Node nodeRoot = DOMUtil.find_node(configRoot, "Nodes");
	    for (Node n : DOMUtil.Children(nodeRoot, "Node")) {
		nodeCount++;
		String label = DOMUtil.getNodeAttribute(n, "label");
		if (label.equals(""))
		    add(new NullPanel());
		else {
		    String directory = DOMUtil.getNodeAttribute(n, "directory");
		    String name = DOMUtil.getNodeAttribute(n, "name");
		    String group = DOMUtil.getNodeAttribute(n, "group");
		    String infoText = "";
		    Node content = DOMUtil.find_node(n, "html");
		    if (content != null) {
			infoText = DOMUtil.node2String(content);
		    }

		    // The event string we search for is:
		    // NCS/<station-name>/ - ignoring the group tag which
		    // used to be on the end
		    Station newStation = createStation(this, label, directory, "NCS/" + name + "/", infoText);
		    add(newStation);
		}
	    }
	    rows = nodeCount / columns;

	    // All stations created, check their install status
	    //checkInstalled();

	    // Shove in the MoverPanel
	    String slsName = DOMUtil.getNodeAttribute(configRoot, "moverName");
	    moverPanel = new MoverPanel(slsName);
	    remove(0);
	    add(moverPanel, 0);

	    // And collect up all the connectors
	    Node connRoot = DOMUtil.find_node(configRoot, "Connections");
	    ArrayList<int []> clist = new ArrayList<int []>();
	    for (Node n : DOMUtil.Children(connRoot, "c")) {
		int from = Integer.decode(DOMUtil.getNodeAttribute(n, "from"));
		int to = Integer.decode(DOMUtil.getNodeAttribute(n, "to"));
		int foo[] = new int[2];
		foo[0] = from; foo[1] = to;
		clist.add(foo);
	    }

	    // *&^%$#@! Java toArray semantics!
	    connectors = new  int [1][2];
	    connectors = clist.toArray(connectors);

	    revalidate();
	    repaint();
	}

	public void updateMoverPanel()
	{
	    moverPanel.update();
	}
    }
}
