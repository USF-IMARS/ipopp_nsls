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
*   7-Apr-06, 	Original version.
*  18-Aug-06, 	Debug tweaks.
*  22-Sep-06, 	Added processing monitor.
*  04-Jun-07, 	Changed window proportional size to absolute size.
*                               Added processingMonitor to to console. 
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.console;
import gov.nasa.gsfc.nisgs.nsls.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Console extends JFrame {
  protected static JFrame frame;
  protected static ServerHandler serverHandler = null;
  protected static ControlPanel controlPanel = null;
  private JPanel mainPanel;
  private JPanel handlerPanel;
  private MenuBarClock clock;
  private String pmfile;
  private String configfile;
  private String tilelistcfg;
  private String stationroot;
  protected static String serverVersion = null;
  private static final String USAGE = "Usage: Console [-processingMonitor pm.xml] [-configuration default_config.file] [-stationRoot ncs/stations/dir] [-openDashboard] [-connectTolocalhost] [<server-host:port>]";
  private static final String[] serverChoices = new String[] {
      "localhost:3500", "nisds1.gsfc.nasa.gov:3500","nisds1.sci.gsfc.nasa.gov:3500"
  };
  private static final long serialVersionUID = 1L;
  /****************************************************************************
  * main.
  ****************************************************************************/
  public static void main (String[] args) throws Exception {
    HostPort serverAddr = null;
    String pmfile = null;
    String configfile = null;
    String tilelist = null;
    String stationroot = null;
    boolean openDashboard = false;
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
	else if(arg.equals("-openDashboard")) {
	    openDashboard = true;
	    serverAddr = new HostPort("localhost:3500");
	}
	else if(arg.equals("-connectTolocalhost")) {
	    serverAddr = new HostPort("localhost:3500");
	}
	else {
	    if (serverAddr == null) {
		serverAddr = new HostPort(arg);
		continue;
	    }
	    throw new Exception("unexpected argument: " + arg);
	}
      }
    } catch (Exception e) {
      System.out.println(USAGE);
      System.exit(0);
    }
    Console console = new Console(pmfile, configfile, tilelist, stationroot, openDashboard);
    console.start(serverAddr);
  }
  /****************************************************************************
  * Console.
  ****************************************************************************/
    public Console (String pmConfig, String defaultConfig, String tilelist, String stationRoot, boolean openDashboard) throws Exception {
    frame = this;
    pmfile = pmConfig;
    configfile = defaultConfig;
    tilelistcfg = tilelist;
    stationroot = stationRoot;
    // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    UIManager.getDefaults().put("ComboBox.disabledForeground",Color.black);
    setTitle("SLS Console");
    mainPanel = new JPanel(new BorderLayout());
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(mainPanel,BorderLayout.CENTER);
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    JMenuItem connectItem = new JMenuItem("Connect...");
    connectItem.addActionListener(new ConnectButtonClicked());
    fileMenu.add(connectItem);
    JMenuItem disconnectItem = new JMenuItem("Disconnect");
    disconnectItem.addActionListener(new DisconnectButtonClicked());
    fileMenu.add(disconnectItem);
    JMenuItem panelItem = new JMenuItem("IPOPP Dashboard");
    panelItem.addActionListener(new ControlPanelButtonClicked());
    fileMenu.add(panelItem);
    JMenuItem exitItem = new JMenuItem("Exit");
    exitItem.addActionListener(new ExitButtonClicked());
    fileMenu.add(exitItem);
    menuBar.add(fileMenu);
    JMenu helpMenu = new JMenu("Help");
    JMenuItem aboutItem = new JMenuItem("About SLS...");
    aboutItem.addActionListener(new AboutButtonClicked());
    helpMenu.add(aboutItem);
    menuBar.add(helpMenu);
    menuBar.add(GUtil.HGlue());
    clock = new MenuBarClock(true);
    menuBar.add(clock);
    menuBar.add(GUtil.HSpace(5));
    mainPanel.add(menuBar,BorderLayout.NORTH);
    handlerPanel = new JPanel(new BorderLayout());
    handlerPanel.add(new MarginPanel(-1,-1,new CustomLabel("No server selected."),-1,-1),BorderLayout.CENTER);
    mainPanel.add(handlerPanel,BorderLayout.CENTER);
    pack();
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    // GUtil.configureWindow(this,0.95,0.85);
    GUtil.configureWindow(this,1000,700);
    setVisible(true);
    
    if(openDashboard)
	controlPanel = new ControlPanel(pmfile, configfile, tilelistcfg, stationroot, true);
  }
  /****************************************************************************
  * start.
  ****************************************************************************/
  public void start (HostPort serverAddr) throws Exception {
    Util.startThread(new AnimateLoop());
    clock.start();
    if (serverAddr != null) {
      SwingUtilities.invokeLater(new ServerSelected(serverAddr));
    } else {
      SwingUtilities.invokeLater(new PromptForServer());
    }
  }
  /****************************************************************************
  * PromptForServer.
  ****************************************************************************/
  private class PromptForServer implements Runnable {
    private JDialog dialog;
    private JComboBox combo;
    public void run () {
      dialog = new JDialog(frame,"SLS Server Selection",true);
      dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
      dialog.setResizable(false);
      Container content = dialog.getContentPane();
      combo = new JComboBox(serverChoices);
      combo.setEditable(true);
      combo.setMaximumSize(combo.getPreferredSize());
      JButton selectButton = new JButton("Select");
      selectButton.addActionListener(new SelectButtonClicked());
      JButton cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(new CancelButtonClicked());
      content.add(new MarginPanel(10,10,new VPanel(
        new HPanel(GUtil.HGlue(),new JLabel("Select the SLS Server host:port..."),GUtil.HGlue()),
        GUtil.VSpace(10),
        new HPanel(GUtil.HGlue(),combo,GUtil.HGlue()),
        GUtil.VSpace(10),
        new HPanel(GUtil.HGlue(),selectButton,GUtil.HSpace(10),cancelButton,GUtil.HGlue()),
        GUtil.VGlue()
      ),10,10));
      dialog.pack();
      GUtil.centerWindow(dialog);
      dialog.setVisible(true);
    }
    private class SelectButtonClicked implements ActionListener {
      public void actionPerformed (ActionEvent ae) {
	try {
          HostPort serverAddr = new HostPort((String)combo.getSelectedItem());
          SwingUtilities.invokeLater(new ServerSelected(serverAddr));
	} catch (Exception e) {
          JOptionPane.showMessageDialog(frame,e.toString(),"Error",
            				JOptionPane.ERROR_MESSAGE);
      	}
        dialog.dispose();
      }
    }
    private class CancelButtonClicked implements ActionListener {
      public void actionPerformed (ActionEvent ae) {
        dialog.dispose();
      }
    }
  }
  /****************************************************************************
  * ConnectButtonClicked.
  ****************************************************************************/
  private class ConnectButtonClicked implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      SwingUtilities.invokeLater(new PromptForServer());
    }
  }
  /****************************************************************************
  * DisconnectButtonClicked.
  ****************************************************************************/
  private class DisconnectButtonClicked implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      if (serverHandler != null) {
        serverHandler.stop();
        handlerPanel.removeAll();
	handlerPanel.add(new MarginPanel(-1,-1,new CustomLabel("No server selected."),-1,-1),BorderLayout.CENTER);
        handlerPanel.revalidate();
        handlerPanel.repaint();
      }
    }
  }
  /****************************************************************************
  * ControlPanelButtonClicked.
  ****************************************************************************/
  private class ControlPanelButtonClicked implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      if (controlPanel == null) {
	try {
	    controlPanel = new ControlPanel(pmfile, configfile, tilelistcfg,stationroot,
					    serverHandler != null && serverHandler.isLocalhost());
	}
	catch (Exception e) {
          JOptionPane.showMessageDialog(frame,e.toString(),"Dashboard Open Error",
            				JOptionPane.ERROR_MESSAGE);
      	};
      }
      else {
	  // We already have one, just make it visible again
	  try {
	      controlPanel.onLocalhost(serverHandler != null
				       && serverHandler.isLocalhost());
	      controlPanel.setVisible(true);
	  }
	  catch (Exception e) {
          JOptionPane.showMessageDialog(frame,e.toString(),"Dashboard Reopen Error",
            				JOptionPane.ERROR_MESSAGE);
      	};
      }
    }
  }
  /****************************************************************************
  * ExitButtonClicked.
  ****************************************************************************/
  private class ExitButtonClicked implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      System.exit(0);
    }
  }
  /****************************************************************************
  * AboutButtonClicked.
  ****************************************************************************/
  private class AboutButtonClicked implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      String text = "SLS Console Version " + SLS.VERSION +
      		    (serverVersion != null ? (", Server Version " + serverVersion) : "");
      JOptionPane.showMessageDialog(frame,text,"About SLS Console...",
				    JOptionPane.INFORMATION_MESSAGE);
    }
  }
  /****************************************************************************
  * ServerSelected.
  ****************************************************************************/
  private class ServerSelected implements Runnable {
    private HostPort serverAddr;
    public ServerSelected (HostPort serverAddr) {
      this.serverAddr = serverAddr;
    }
    public void run () {
      if (serverHandler != null) {
        serverHandler.stop();
      }
      serverHandler = new ServerHandler(serverAddr);
      handlerPanel.removeAll();
      handlerPanel.add(serverHandler,BorderLayout.CENTER);
      handlerPanel.revalidate();
      handlerPanel.repaint();
      Util.startThread(serverHandler);
    }
  }
  /****************************************************************************
  * AnimateLoop.
  ****************************************************************************/
  private class AnimateLoop implements Runnable {
    private int count = 0;
    Animate animate = new Animate();
    public void run () {
      while (true) {
        count = (count + 1) % 8;
        GUtil.invokeAndWait(animate);
        Util.sleep(0.125);
      }
    }
    private class Animate implements Runnable {
      public void run () {
      	if (serverHandler != null) {
	  serverHandler.eventTable.animate(count);
      	}
      }
    }
  }
}
