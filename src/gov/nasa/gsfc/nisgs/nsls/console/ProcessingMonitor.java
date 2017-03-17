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
// Importing crap from interp for reading XML
import  org.w3c.dom.Node;
import  org.w3c.dom.Element;
import  org.w3c.dom.NodeList;

public class ProcessingMonitor extends JFrame {
    protected static JFrame frame;
    // Used for event dispatch - one nsls event may dispatch to
    // more than one StationMonitor display object
    //private StringMap monitors = new StringMap();
    private Map<String, List<StationMonitor>> monitors
	= new HashMap<String, List<StationMonitor>> ();
    private JTabbedPane tabbedPane;
    private ArrayList<MonitorPanel> monitorPanels = new ArrayList<MonitorPanel>();
    private int count = 0;

    private static final long serialVersionUID = 1L;
    /****************************************************************************
     * ProcessingMonitor.
     ****************************************************************************/
    public ProcessingMonitor (String monConfig) throws Exception {

	// Read in the monitor config file
	Node configRoot = DOMUtil.readXMLFile(monConfig);
	// Spit if this isn't our XML file
	if(!configRoot.getNodeName().equals("ProcessingMonitors"))
	    throw new Exception(monConfig + " is a " + configRoot.getNodeName()
				+ " , not a ProcessingMonitor XML document");
	frame = this;
	// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	setTitle("NSLS Processing Monitor");
	// Creating JTabbedPane here
	tabbedPane = new JTabbedPane();
	// Loop down the monitor descriptions, creating a tab for each
	for (Node pn : DOMUtil.Children(configRoot, "ProcessingMonitor")) {
	    // get the columns and satellite name
	    int columns = Integer.decode(DOMUtil.getNodeAttribute(pn, "columns"));
	    String satellite = DOMUtil.getNodeAttribute(pn, "satellite");
	    String moverName = DOMUtil.getNodeAttribute(pn, "moverName");
	    String moverCall = DOMUtil.getNodeAttribute(pn, "moverCall");
	    MonitorPanel newMP = new MonitorPanel(columns, moverName, moverCall);
	    tabbedPane.addTab(satellite, newMP);
	    monitorPanels.add(newMP);
	    newMP.setTabIndex(tabbedPane.indexOfTab(satellite));
	    newMP.loadMonitors(pn);
	}
	getContentPane().setLayout(new BorderLayout());
	getContentPane().add(new MarginPanel(10,10,tabbedPane,10,10),BorderLayout.CENTER);
	pack();
	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	// GUtil.configureWindow(this,0.95,0.85);
	GUtil.configureWindow(this,1000,700);
	setVisible(true);
	javax.swing.Timer timer = new javax.swing.Timer(125,new Animate());
	timer.start();
    }
    private StationMonitor createMonitor (String name, String source) {
	StationMonitor monitor = new StationMonitor(name);
	List<StationMonitor> lm = monitors.get(source);
	if(lm == null) {
	    lm = new LinkedList<StationMonitor>();
	    monitors.put(source, lm);
	}
	lm.add(monitor);
	return monitor;
    }
    /****************************************************************************
     * logEvent.
     ****************************************************************************/
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
	/*
	// From PDSMover indicating a new passs?
	if (event.getSource().toString().equals("DSM/PdsMover/PdsPassCreate") ||
	event.getSource().toString().equals("DSM/RDRMover/RDRPassCreate")) {
	if (event.getText().contains("PdsPassCreate: New pass --") ||
	event.getText().contains("PdsPassCreate: pass already exists") ||
	event.getText().contains("RDRPassCreate: New pass --") ||
	event.getText().contains("RDRPassCreate: pass already exists")) {
	try {
	loadMonitors();
	}
	catch (Exception e) {
	e.printStackTrace();
	JOptionPane.showMessageDialog(frame,e.toString(),"Error",
	JOptionPane.ERROR_MESSAGE);
	};
	//loadMonitors(event.getText().contains("AQUA"));
	}
	}
	*/
	// If this event matches a list of Stations, pass it off to them
	List<StationMonitor> lm = monitors.get(event.getSource().toString());
	if (lm != null) {
	    for (StationMonitor monitor : lm) {
		monitor.logEvent(event);
	    }
	}
    }
    /****************************************************************************
     * StationMonitor.
     ****************************************************************************/
    private class StationMonitor extends HPanel {
	private String name;
	private String node = "";
	private String text = null;
	private Color color = Color.WHITE;
	private ImageIcon[] icons = null;
	private long activeAt = 0;
	private long completedAt = 0;
	private static final long serialVersionUID = 1L;
	public StationMonitor (String name) {
	    this.name = name;
	}
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
	    if (activeAt != 0) {
		if (completedAt != 0) {
		    String elapsed = node + "   " + encodeElapsed(completedAt-activeAt);
		    g.drawString(elapsed,(w-fm.stringWidth(elapsed))/2,h-7);
		} else {
		    String elapsed = node + "   " + encodeElapsed(System.currentTimeMillis()-activeAt);
		    g.drawString(elapsed,(w-fm.stringWidth(elapsed))/2,h-7);
		}
	    }
	}
	/**
	 * Resets this node to initial visual state
	 */
	public void reset() {
	    text = null;
	    color = Color.WHITE;
	    icons = null;
	    activeAt = 0;
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
	private int tabIndex;
	private int[][] connectors;

	private InfoPanel infoPanel;

	public MonitorPanel (int columns,
			     String moverName, String moverCall) {
	    super(new GridLayout(0,columns,HGAP,VGAP));
	    moverSource = "DSM/" + moverName + "/" + moverCall;
	    moverNewPassFlag = moverCall + ": New pass --";
	    moverOldPassFlag = moverCall + ": pass already exists"; 
	}

	public void setTabIndex (int tabIndex) {
	    this.tabIndex = tabIndex;
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
	 * StationMonitor
	 */
	public boolean monitorIsRunning() {
	    for (Component c : getComponents()) {
		if (c instanceof StationMonitor) {
		    if (((StationMonitor)c).isRunning()) {
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
		if (c instanceof StationMonitor) {
		    ((StationMonitor)c).reset();
		}
	    }
	    clearInfoPanel();
	    addInfoPanelText("START TIMES");
	    addInfoPanelText("");
	}

	public void paint (Graphics g) {
	    super.paint(g);
      
	    g.setColor(Color.BLACK);
	    Component[] components = getComponents();
	    if (components.length > 0)
		{
		    for (int i = 0; i < connectors.length; i++)
			{
			    Component c1 = getComponent(connectors[i][0]);
			    Component c2 = getComponent(connectors[i][1]);
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
		}
	    paintChildren(g);
	}

	public void loadMonitors (Node configRoot) throws Exception {
	    removeAll();
	    // turn all the Node nodes into monitor panels
	    Node nodeRoot = DOMUtil.find_node(configRoot, "Nodes");
	    for (Node n : DOMUtil.Children(nodeRoot, "Node")) {
		String label = DOMUtil.getNodeAttribute(n, "label");
		if (label.equals(""))
		    add(new NullPanel());
		else {
		    String name = DOMUtil.getNodeAttribute(n, "name");
		    String group = DOMUtil.getNodeAttribute(n, "group");
		    add(createMonitor(label,"NCS/" + name + "/" + group));
		}
	    }

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
