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
*  13-Dec-12,   forked from ProcessingMonitor as default_config.file editor
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.console;
import gov.nasa.gsfc.nisgs.nsls.*;
import gov.nasa.gsfc.nisgs.nsls.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.text.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.FilenameFilter;
// Importing crap from interp for reading XML
import  org.w3c.dom.Node;
import  org.w3c.dom.Element;
import  org.w3c.dom.NodeList;

public class ConfigurationEditor extends JFrame {
    protected static JFrame frame;
    // Used for event dispatch - one nsls event may dispatch to
    // more than one Station display object
    //private StringMap monitors = new StringMap();
    private Map<String, List<Station>> monitors
	= new HashMap<String, List<Station>> ();
    private JTabbedPane tabbedPane;
    private ArrayList<MonitorPanel> monitorPanels = new ArrayList<MonitorPanel>();
    private File defaultConfigFile = null;
    private File ncsStationDir = null;
    private int count = 0;

    private static final long serialVersionUID = 1L;
    /****************************************************************************
     * ConfigurationEditor.
     ****************************************************************************/
    public ConfigurationEditor (String monConfig, String defaultConfig, String stationRoot) throws Exception {

	System.err.println("stationRoot: " + stationRoot);

	// Set up the ncsStationDir
	if(stationRoot != null) {
	    ncsStationDir = new File(stationRoot);
	    // Just to make things pretty, canonicalize the path
	    ncsStationDir = ncsStationDir.getCanonicalFile();
	    if(!ncsStationDir.isDirectory()) {
		throw new Exception(stationRoot + " is not a directory for NCS");
	    }
	}

	// Read in the monitor config file
	Node configRoot = DOMUtil.readXMLFile(monConfig);
	// Spit if this isn't our XML file
	if(!configRoot.getNodeName().equals("ProcessingMonitors"))
	    throw new Exception(monConfig + " is a " + configRoot.getNodeName()
				+ " , not a ProcessingMonitor XML document");
	frame = this;
	// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	setTitle("IPOPP Configuration Editor");
	// Creating JTabbedPane here
	tabbedPane = new JTabbedPane();
	// Loop down the monitor descriptions, creating a tab for each
	for (Node pn : DOMUtil.Children(configRoot, "ProcessingMonitor")) {
	    // get the columns and satellite name
	    int columns = Integer.decode(DOMUtil.getNodeAttribute(pn, "columns"));
	    String satellite = DOMUtil.getNodeAttribute(pn, "satellite");
	    String moverName = DOMUtil.getNodeAttribute(pn, "moverName");
	    String moverCall = DOMUtil.getNodeAttribute(pn, "moverCall");
	    MonitorScrollPane newMSP = new MonitorScrollPane(columns, moverName, moverCall);
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
	

	// Add a menu bar with a Save default_config.file command
	
	JMenuBar menuBar = new JMenuBar();
	JMenu fileMenu = new JMenu("File");

	JMenuItem jmi = new JMenuItem("Load...");
	jmi.addActionListener(new LoadMenuClicked());
	fileMenu.add(jmi);

	jmi = new JMenuItem("Save...");
	jmi.addActionListener(new SaveMenuClicked());
	fileMenu.add(jmi);

	jmi = new JMenuItem("Check Installed");
	jmi.addActionListener(new CheckInstallMenuClicked());
	fileMenu.add(jmi);
	fileMenu.add(jmi);

	/*
	jmi = new JMenuItem("How Big");
	jmi.addActionListener(new HowBigMenuClicked());
	fileMenu.add(jmi);
	*/

	menuBar.add(fileMenu);
	JPanel mainPanel = new JPanel(new BorderLayout());
	mainPanel.setLayout(new BorderLayout());
	mainPanel.add(menuBar,BorderLayout.NORTH);
	mainPanel.add(new MarginPanel(10,10,tabbedPane,10,10),BorderLayout.CENTER);

	getContentPane().setLayout(new BorderLayout());
	getContentPane().add(mainPanel ,BorderLayout.CENTER);
	pack();
	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	// GUtil.configureWindow(this,0.95,0.85);
	// Right here:
	// Ask all MonitorPanels for their preferred dimensions
	// Use GUtil.configureWindow to snap us to that size
	// Tell that size to those windows
	GUtil.configureWindow(this,1000,700);
	setVisible(true);
	javax.swing.Timer timer = new javax.swing.Timer(125,new Animate());
	timer.start();
	/*
	for (MonitorPanel mp : monitorPanels) {
	    System.err.println("MPDIM: " + mp.getSize());
	    mp.setPreferredSize(mp.getSize());
	}
	*/
	// Load up the default_config.file
	if(defaultConfig != null) {
	    defaultConfigFile = new File(defaultConfig);
	    // Just to make things pretty, canonicalize the path
	    defaultConfigFile = defaultConfigFile.getCanonicalFile();
	    readConfigFile(defaultConfigFile);
	}
	// Force repaints on all the MonitorPanels to get the display right?
	for(MonitorPanel mp : monitorPanels) {
	    mp.forciblyUpdate();
	    //	    mp.revalidate();
	    // mp.repaint();
	}
    }

    private class LoadMenuClicked implements ActionListener {
	public void actionPerformed (ActionEvent ae) {
	    String dialogTitle = "Load IPOPP configuration file";
	    if(defaultConfigFile != null)
		try {
		    JFileChooser jfc = new JFileChooser(defaultConfigFile);
		    jfc.setSelectedFile(defaultConfigFile);
		    jfc.setDialogTitle(dialogTitle);
		    int returnVal = jfc.showDialog(ConfigurationEditor.this, "Load");
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
			File chosenFile = jfc.getSelectedFile();
			readConfigFile(chosenFile);
			JOptionPane.showMessageDialog(frame,
						      chosenFile.getCanonicalPath() + " loaded",
						      dialogTitle,
						      JOptionPane.INFORMATION_MESSAGE);
			// Force repaints on all the MonitorPanels to get the display right?
			for(MonitorPanel mp : monitorPanels) {
			    mp.forciblyUpdate();
			}
		    }
		}
		catch (Exception e) {
		    JOptionPane.showMessageDialog(frame,e.toString(),"Error",
						  JOptionPane.ERROR_MESSAGE);
		}
		    
	}
    }

    private class SaveMenuClicked implements ActionListener {
	public void actionPerformed (ActionEvent ae) {
	    String dialogTitle = "Save IPOPP configuration file";
	    if(defaultConfigFile != null)
		try {
		    JFileChooser jfc = new JFileChooser(defaultConfigFile);
		    jfc.setDialogType(JFileChooser.SAVE_DIALOG);
		    jfc.setSelectedFile(defaultConfigFile);
		    jfc.setDialogTitle(dialogTitle);
		    int returnVal = jfc.showDialog(ConfigurationEditor.this, "Save");
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
			File chosenFile = jfc.getSelectedFile();
			writeConfigFile(chosenFile);
			JOptionPane.showMessageDialog(frame,
						      chosenFile.getCanonicalPath() + " written",
						      dialogTitle,
						      JOptionPane.INFORMATION_MESSAGE);
		    }
		}
		catch (Exception e) {
		    JOptionPane.showMessageDialog(frame,e.toString(),"Error",
						  JOptionPane.ERROR_MESSAGE);
		}
		    
	}
    }

    private class CheckInstallMenuClicked implements ActionListener {
	public void actionPerformed (ActionEvent ae) {
	    for (MonitorPanel mp : monitorPanels)
		mp.checkInstalled();
	}
    }
    /*
    private class HowBigMenuClicked implements ActionListener {
	public void actionPerformed (ActionEvent ae) {
	    for (MonitorPanel mp : monitorPanels)
		System.err.println("MP: " + mp.getSize());
	}
    }
    */


    private Station createStation (MonitorPanel mp, String label, String directory) {
	Station newStation = new Station(mp, label, directory);
	List<Station> lm = monitors.get(directory);
	if(lm == null) {
	    lm = new LinkedList<Station>();
	    monitors.put(directory, lm);
	}
	lm.add(newStation);
	newStation.checkInstalled();
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
		List<Station> lm = monitors.get(line);
		
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
		List<Station> lm = monitors.get(line);

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
    /*
    public void logEvent (LogEvent event) {
	// If this event is the creation of a Pass, top the appropriate tab
	for (MonitorPanel mp : monitorPanels) {
	    int myIndex = mp.newPass_p(event);
	    if (myIndex != -1) {
		tabbedPane.setSelectedIndex(myIndex);
		// Have the MonitorPanel check if anything in it
		// is active, and reset itself if not
		if(!mp.monitorIsRunning())
		    mp.resetMonitors();
		// The gyrations you have to do to get a date formatted...
		StringBuffer sb = new StringBuffer();
		SimpleDateFormat sf = new SimpleDateFormat("MM-dd HH:mm:ss");
		sb = sf.format(new Date(), new StringBuffer(), new FieldPosition(0));
		mp.addInfoPanelText(sb.toString());
	    }
	}

	// If this event matches a list of Stations, pass it off to them
	List<Station> lm = monitors.get(event.getSource().toString());
	if (lm != null) {
	    for (Station monitor : lm) {
		monitor.logEvent(event);
	    }
	}
    }
    */

    /**
     * StationMouseListener - notices mouse entry/exit on Station display
     * and lights it up somehow
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
	}
     
	public void mouseReleased(MouseEvent e) {
	    if(station.getInstalled()) {
		station.setEnabled(!station.getEnabled());
		station.updateFromState();
	    }
	}
     
	public void mouseEntered(MouseEvent e) {
	    if(station.getInstalled())
		station.lightUp();
	    //System.err.println(station);
	}
     
	public void mouseExited(MouseEvent e) {
	    if(station.getInstalled())
		station.lightOff();
	}
     
	public void mouseClicked(MouseEvent e) {
	}
    }
    /****************************************************************************
     * Station.
     ****************************************************************************/
    private class Station extends HPanel {
	private String name;
	private String directory;
	private String node = "";
	private String text = null;
	private Color color = Color.GRAY;
	private ImageIcon[] icons = null;
	private static final long serialVersionUID = 1L;

	private boolean installedP;
	private boolean enabledP;

	private MonitorPanel mp;

	public Station (MonitorPanel mp, String name, String directory) {
	    this.mp = mp;
	    this.name = name;
	    this.directory = directory;
	    new StationMouseListener(this);
	}

	/**
	 * Returns a good width to use for a decent display.
	 */
	public static final int bestWidth = 115;
	/**
	 * Returns a good height to use for a decent display.
	 */
	public static final int bestHeight = 78;

	public void setInstalled(boolean s) {
	    installedP = s;
	}

	public void setEnabled(boolean s) {
	    enabledP = s;
	}

	public boolean getEnabled() { return enabledP; }

	public boolean getInstalled() { return installedP; }

	public void updateFromState() {
	    if (installedP) {
		if(enabledP) {
		    color = Color.green;
		    text = "ON";
		}
		else {
		    color = Color.yellow;
		    text = "OFF";
		}
	    }
	    else {
		color = Color.lightGray;
		text = "XXX";
	    }
	    repaint();
	}
		    

	public void lightUp() {
	    color = Color.WHITE;
	    mp.lightConnections(this, true);
	    //mp.drawStationConnectors(this, Color.red);
	    repaint();
	}

	public void lightMeUp() {
	    color = Color.BLUE;
	}

	public void lightOff() {
	    mp.lightConnections(this, false);
	    updateFromState();
	    //mp.drawStationConnectors(this, Color.black);
	    //repaint();
	}

	/**
	 *
	 */
	/*	public void setState (int val) {
	    state = val;
	    switch (state) {
	    case NOT_INSTALLED:
		color = Color.yellow;
		text = "NOT INSTALLED";
		break;
	    case OFF:
		color = Color.lightGray;
		text = "OFF";
		break;
	    case ON:
		color = Color.green;
		text = "ON";
		break;
	    }
	    repaint();
	    }*/
	/*
	public void logEvent (LogEvent event) {
	    if (node.equals("")) {
		String namepart[] = event.getHost().split("\\.");
		node = namepart[0];
	    }
	    switch (event.getLevel()) {
	    case Log.INFO_EVENT: {
		if (event.getText().contains("Putting reserved object") ||	// XSLT generated
		    event.getText().contains("got product")) {		// Charlie generated
		    text = "STAGING";
		    color = GUtil.LIGHT_BLUE;
		    icons = GUtil.stagingIcons;
		    break;
		}
		if (event.getText().contains("launching algorithm") ||	// XSLT generated
		    event.getText().contains("program starting")) {		// Charlie generated
		    activeAt = System.currentTimeMillis();
		    completedAt = 0;
		    text = "ACTIVE";
		    color = GUtil.LIGHT_BLUE;
		    icons = GUtil.runningIcons;
		    break;
		}
		if (event.getText().contains("Done with algorithm") || // XSLT generated
		    event.getText().contains("program ending")) {      // Charlie generated
		    // Always stop the clock
		    completedAt = System.currentTimeMillis();
		    // If our current state is WARNING, don't change color
		    // The text slot could be null if, say, monitor is started
		    // in the middle of a run, so...
		    if(!"WARNING".equals(text)) {
			text = "COMPLETE";
			color = Color.GREEN;
			icons = GUtil.successIcons;
		    }
		    break;
		}
		break;
	    }
	    case Log.WARNING_EVENT: {
		text = "WARNING";
		color = Color.YELLOW;
		icons = GUtil.warningIcons;
		break;
	    }
	    case Log.ERROR_EVENT: {
		completedAt = System.currentTimeMillis();
		text = "ERROR";
		color = Color.RED;
		icons = GUtil.errorIcons;
		break;
	    }
	    }
	    repaint();
	}
	*/
	public void paint (Graphics g) {
	    int w = getWidth();
	    int h = getHeight();
	    FontMetrics fm = g.getFontMetrics();
	    g.setColor(Color.BLACK);
	    g.fillRect(0,0,w,h);
	    g.setColor(color);
	    int y = 15;
	    for (String part : name.split(" ")) {
		g.drawString(part,(w-fm.stringWidth(part))/2,y);
		y += 13;
	    }
	    if (icons != null) {
		Image image = icons[count].getImage();
		g.drawImage(image,(w-image.getWidth(null))/2,(h-image.getHeight(null))/2,null);
	    }
	    if (text != null) {
		g.drawString(text,(w-fm.stringWidth(text))/2,h-20);
	    }
	}
	/**
	 * Resets this node to initial visual state
	 */
	public void reset() {
	    text = null;
	    color = Color.GRAY;
	    icons = null;
	}
	/**
	 * Checks whether this monitor believes its underlying
	 * station is running
	 */
	public boolean isRunning() {
	    // Somewhat perverted style, but text can be null...
	    return "STAGING".equals(text) || "ACTIVE".equals(text);
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
	public void checkInstalled()
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
		if (getInstalled() != hasDir) {
		    setInstalled(hasDir);
		    updateFromState();
		}
	    }
	}

	/**
	 * For debugging
	 */
	public String toString() {
	    return name + ": " + (enabledP ? "ON " : "OFF ") + (installedP ? "" : "XXX");
	}
    }
    /****************************************************************************
     * InfoPanel - used to 
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
				  String moverName, String moverCall) {
	    super(new MonitorPanel(columns, moverName, moverCall),
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

	private String moverSource;
	private String moverNewPassFlag;
	private String moverOldPassFlag;
	private int columns;
	private int rows;
	private int tabIndex;
	private int[][] connectors;

	private InfoPanel infoPanel;

	public MonitorPanel (int columns,
			     String moverName, String moverCall) {
	    super(new GridLayout(0,columns,HGAP,VGAP));
	    this.columns = columns;
	    rows = 0;
	    moverSource = "DSM/" + moverName + "/" + moverCall;
	    moverNewPassFlag = moverCall + ": New pass --";
	    moverOldPassFlag = moverCall + ": pass already exists"; 
	}

	public int getRows() { return rows; }
	public int getColumns() { return columns; }

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
	    setPreferredSize(new Dimension(bestColumns * Station.bestWidth,
					   bestRows * Station.bestHeight));
	}

	/**
	 * If this event is the creation of a Pass relevant to this panel,
	 * return the tabIndex (so the main event loop can focus the tab).
	 * Otherwise return -1.
	 */
	public int newPass_p(LogEvent event) {

	    if (event.getSource().toString().equals(moverSource)
		&& (event.getText().contains(moverNewPassFlag) ||
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
		    ((Station)c).reset();
		}
	    }
	    clearInfoPanel();
	    addInfoPanelText("START TIMES");
	    addInfoPanelText("");
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
		    Station newStation = createStation(this, label, directory);
		    add(newStation);
		}
	    }
	    rows = nodeCount / columns;

	    // All stations created, check their install status
	    checkInstalled();
	    // We know the dimensions; this is the place to nail them?
	    // ???

	    // Shove in the InfoPanel
	    infoPanel = new InfoPanel(Color.GREEN);
	    remove(0);
	    add(infoPanel, 0);

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
	    // After loading and setting up, nail the size
	    //System.err.println("SIZE: " + this.getSize(null));
	    //setPreferredSize(getSize(null));
	}

	public void clearInfoPanel()
	{
	    infoPanel.clearText();
	}

	public void addInfoPanelText(String s)
	{
	    infoPanel.addText(s);
	}
    }
}
