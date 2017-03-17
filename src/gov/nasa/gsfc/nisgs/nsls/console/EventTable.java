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
*  13-Mar-06, 	Original version.
*  22-Sep-06, 	Added processing monitor.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.console;
import gov.nasa.gsfc.nisgs.nsls.*;
import gov.nasa.gsfc.nisgs.nsls.message.*;
import gov.nasa.gsfc.nisgs.nsls.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
// import javax.swing.border.*;

public class EventTable extends JPanel {
  private EventLine headLine = null;
  private EventLine tailLine = null;
  private VPanel listPanel;
  private VPanel auxPanel;
  private HPanel playPanel;
  private JButton autoScrollButton;
  private JScrollBar vScrollBar;
  private CustomButton filtersButton;
  private CustomButton playEventsButton;
  private CustomButton stopEventsButton;
  private CustomLabel statusField = new CustomLabel(" ");
  protected FilterManager filterManager = new FilterManager();
  private boolean autoScroll = true;
  private boolean liveModeOnConnect = false;
  private boolean showFiltersWhenDetailsClosed = false;
  private int nEvents = 0;
  private int nDiscarded = 0;
  private static final int NO_MODE = 0;
  private static final int LIVE_MODE = 1;
  private static final int PLAY_SEARCH_MODE = 2;
  private static final int PLAY_SEND_MODE = 3;
  private static final int PLAY_DONE_MODE = 4;
  private static final int PLAY_STOPPED_MODE = 5;
  private int mode = NO_MODE;
  private static final long serialVersionUID = 1L;
  /****************************************************************************
  * EventTable.
  ****************************************************************************/
  public EventTable () {
    setLayout(new BorderLayout());
    // setBorder(new LineBorder(Color.blue,1));
    listPanel = new VPanel();
    CustomButton startLiveButton = new CustomButton(GUtil.startLiveIcon);
    startLiveButton.addActionListener(new StartLiveButtonClicked());
    startLiveButton.setToolTipText("Start LIVE Mode");
    // CustomButton updateFiltersButton = new CustomButton(GUtil.updateFiltersIcon);
    CustomButton stopLiveButton = new CustomButton(GUtil.stopLiveIcon);
    stopLiveButton.addActionListener(new StopLiveButtonClicked());
    stopLiveButton.setToolTipText("Stop LIVE Mode");
    filtersButton = new CustomButton(GUtil.filtersIcon);
    filtersButton.addActionListener(new FiltersButtonClicked());
    filtersButton.setToolTipText("Display Filter Panel");
    CustomButton eraseButton = new CustomButton(GUtil.eraseIcon);
    eraseButton.addActionListener(new EraseButtonClicked());
    eraseButton.setToolTipText("Erase Events");
    autoScrollButton = new CustomButton(autoScroll ? GUtil.autoScrollOnIcon : GUtil.autoScrollOffIcon);
    autoScrollButton.addActionListener(new AutoScrollButtonClicked());
    autoScrollButton.setToolTipText("Toggle Auto-Scrolling of Events");
    add(new VPanel(GUtil.VSpace(7),new HPanel(GUtil.HSpace(3),EventLine.getHeading(),GUtil.HGlue(),startLiveButton,/*updateFiltersButton,*/stopLiveButton,GUtil.HSpace(10),filtersButton,GUtil.HSpace(10),eraseButton,autoScrollButton,GUtil.HSpace(5)),GUtil.VSpace(1)),BorderLayout.NORTH);
    JScrollPane scrollPane = new JScrollPane(listPanel);
    vScrollBar = scrollPane.getVerticalScrollBar();
    add(new MarginPanel(2,2,scrollPane,2,2),BorderLayout.CENTER);
    playPanel = new HPanel();
    playEventsButton = new CustomButton(GUtil.playEventsIcon);
    playEventsButton.addActionListener(new PlayEventsButtonClicked());
    playEventsButton.setToolTipText("Play Events");
    stopEventsButton = new CustomButton(GUtil.stopEventsIcon);
    stopEventsButton.addActionListener(new StopEventsButtonClicked());
    stopEventsButton.setToolTipText("Stop Playing Events");
    playPanel.add(new HPanel(playEventsButton,stopEventsButton));
    auxPanel = new VPanel();
    // auxPanel.setBorder(new LineBorder(Color.green,1));
    auxPanel.add(filterManager);
    add(new MarginPanel(2,2,auxPanel,2,2),BorderLayout.SOUTH);
  }
  /****************************************************************************
  * getStatusPanel.
  ****************************************************************************/
  public JPanel getStatusPanel () {
    return new HPanel(playPanel,GUtil.HSpace(5),statusField);
  }
  /****************************************************************************
  * connected.
  ****************************************************************************/
  public void connected () {
    if (liveModeOnConnect) {
      Console.serverHandler.send(new StartLiveMode());
    }
  }
  /****************************************************************************
  * disconnected.
  ****************************************************************************/
  public void disconnected () {
    playPanel.removeAll();
    playPanel.add(new HPanel(playEventsButton,stopEventsButton));
    liveModeOnConnect = (mode == LIVE_MODE);
    mode = NO_MODE;
    erase();
  }
  /****************************************************************************
  * liveModeStarted.
  ****************************************************************************/
  public void liveModeStarted () {
    playPanel.removeAll();
    mode = LIVE_MODE;
    erase();
  }
  /****************************************************************************
  * liveModeStopped.
  ****************************************************************************/
  public void liveModeStopped () {
    playPanel.add(new HPanel(playEventsButton,stopEventsButton));
    mode = NO_MODE;
    erase();
  }
  /****************************************************************************
  * playModeStarted.
  ****************************************************************************/
  public void playModeStarted (boolean searching) {
    mode = (searching ? PLAY_SEARCH_MODE : PLAY_SEND_MODE);
    erase();
  }
  /****************************************************************************
  * playModeDone.
  ****************************************************************************/
  public void playModeDone () {
    mode = PLAY_DONE_MODE;
    updateStatus();
  }
  /****************************************************************************
  * playModeStopped.
  ****************************************************************************/
  public void playModeStopped () {
    mode = PLAY_STOPPED_MODE;
    updateStatus();
  }
  /****************************************************************************
  * incrField.
  ****************************************************************************/
  public void incrField (int index, int incr) {
    EventLine line = headLine;
    while (line != null) {
      line.incrField(index,incr);
      line = line.getNextLine();
    }
  }
  /****************************************************************************
  * logEvent.
  ****************************************************************************/
  public void logEvent (LogEvent event) {
    nEvents++;
    // If LIVE, apply filters (during PLAYBACK, the server applies the filters).
    if (mode == LIVE_MODE) {
      if (!filterManager.getFilters().passes(event)) {
        nDiscarded++;
	updateStatus();
      	return;
      }
    }
    // Scroll off oldest event(s).
    while (listPanel.getComponentCount() >= Console.serverHandler.eventTable.filterManager.getMaxCount()) {
      EventLine line = headLine;
      headLine = line.getNextLine();
      line.setNextLine(null);
      if (headLine != null) {
        headLine.setPrevLine(null);
      } else {
        tailLine = null;
      }
      listPanel.remove(line);
    }
    // Add new event.
    EventLine newLine = new EventLine(event);
    switch (listPanel.getComponentCount()) {
      case 0: {
	headLine = newLine;
	tailLine = newLine;
	listPanel.add(newLine);
	break;
      }
      case 1: {
      	if (newLine.getEvent().getDate().getTime() >= tailLine.getEvent().getDate().getTime()) {
      	  headLine.setNextLine(newLine);
      	  newLine.setPrevLine(headLine);
      	  tailLine = newLine;
      	  listPanel.add(newLine);
      	} else {
      	  tailLine.setPrevLine(newLine);
      	  newLine.setNextLine(tailLine);
      	  headLine = newLine;
      	  listPanel.add(newLine,0);
      	}
        break;
      }
      default: {
      	if (newLine.getEvent().getDate().getTime() >= tailLine.getEvent().getDate().getTime()) {
      	  tailLine.setNextLine(newLine);
      	  newLine.setPrevLine(tailLine);
      	  tailLine = newLine;
      	  listPanel.add(newLine);
      	} else {
	  if (newLine.getEvent().getDate().getTime() < headLine.getEvent().getDate().getTime()) {
	    headLine.setPrevLine(newLine);
	    newLine.setNextLine(headLine);
	    headLine = newLine;
	    listPanel.add(newLine,0);
	  } else {
	    int at = listPanel.getComponentCount() - 2;
	    EventLine line = tailLine.getPrevLine();
	    while (true) {
	      if (newLine.getEvent().getDate().getTime() >= line.getEvent().getDate().getTime()) {
	      	newLine.setPrevLine(line);
	      	newLine.setNextLine(line.getNextLine());
	      	line.getNextLine().setPrevLine(newLine);
	      	line.setNextLine(newLine);
	      	listPanel.add(newLine,at+1);
	      	break;
	      }
	      line = line.getPrevLine();
	      at -= 1;
	    }
	  }
      	}
      }
    }
    listPanel.revalidate();
    listPanel.repaint();
    if (autoScroll) {
      SwingUtilities.invokeLater(new DoAutoScroll());
    }
    vScrollBar.setUnitIncrement(newLine.getPreferredSize().height);	// Doing this only once would be better...
    updateStatus();
    if (Console.controlPanel != null) {
	Console.controlPanel.logEvent(event);
    }
  }
  private class DoAutoScroll implements Runnable {
    public void run () {
      vScrollBar.setValue(vScrollBar.getMaximum());
    }
  }
  /****************************************************************************
  * updateStatus.
  ****************************************************************************/
  private void updateStatus () {
    switch (mode) {
      case NO_MODE: {
        statusField.update(" ");
      	break;
      }
      case LIVE_MODE: {
      	String text = "LIVE MODE" + ", " + encodeCount("event",nEvents) +
		      (nDiscarded > 0 ? (" (" + Integer.toString(nDiscarded) + " discarded)") : "");
      	statusField.update(text,Color.black);
      	break;
      }
      case PLAY_SEARCH_MODE: {
      	statusField.update("PLAYING (SEARCHING)",Color.black);
      	break;
      }
      case PLAY_SEND_MODE: {
      	String text = "PLAYING" + " (" + encodeCount("event",nEvents) + ")";
      	statusField.update(text,Color.black);
      	break;
      }
      case PLAY_DONE_MODE: {
      	String text = "DONE" + ", " + encodeCount("event",nEvents);
      	statusField.update(text,Color.black);
      	break;
      }
      case PLAY_STOPPED_MODE: {
      	statusField.update("STOPPED",Color.red);
      	break;
      }
    }
  }
  /****************************************************************************
  * encodeCounts.
  ****************************************************************************/
  private String encodeCount (String type, int count) {
    return Integer.toString(count) + " " + type + (count == 1 ? "" : "s");
  }
  /****************************************************************************
  * erase.
  ****************************************************************************/
  private void erase () {
    listPanel.removeAll();
    headLine = null;
    tailLine = null;
    if (auxPanel.getComponentCount() == 1) {
      Component component = auxPanel.getComponents()[0];
      if (component instanceof EventDetails) {
      	EventDetails details = (EventDetails) component;
      	details.getEventLine().setPrevLine(null);
      	details.getEventLine().setNextLine(null);
      }
    }
    revalidate();
    repaint();
    nEvents = 0;
    nDiscarded = 0;
    updateStatus();
  }
  /****************************************************************************
  * showDetails.
  ****************************************************************************/
  public void showDetails (EventLine line) {
    int minHeight = 0;
    if (auxPanel.getComponentCount() == 1) {
      Component component = auxPanel.getComponent(0);
      if (component instanceof FilterManager) {
      	showFiltersWhenDetailsClosed = true;
      } else {
        if (component instanceof EventDetails) {
      	  EventDetails details = (EventDetails) component;
      	  minHeight = details.getHeight();
      	  details.getEventLine().setShowingDetails(false);
        }
      }
      auxPanel.removeAll();
    }
    line.setShowingDetails(true);
    auxPanel.add(new EventDetails(line,minHeight));
    revalidate();
    repaint();
  }
  /****************************************************************************
  * closeDetails.
  ****************************************************************************/
  public void closeDetails () {
    Component component = auxPanel.getComponent(0);
    EventDetails details = (EventDetails) component;
    details.getEventLine().setShowingDetails(false);
    auxPanel.removeAll();
    if (showFiltersWhenDetailsClosed) {
      auxPanel.add(filterManager);
      showFiltersWhenDetailsClosed = false;
    }
    revalidate();
    repaint();
  }
  /****************************************************************************
  * closeFilters.
  ****************************************************************************/
  public void closeFilters () {
    auxPanel.removeAll();
    revalidate();
    repaint();
  }
  /****************************************************************************
  * StartLiveButtonClicked.
  ****************************************************************************/
  private class StartLiveButtonClicked implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      Console.serverHandler.send(new StartLiveMode());
    }
  }
  /****************************************************************************
  * StopLiveButtonClicked.
  ****************************************************************************/
  private class StopLiveButtonClicked implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      Console.serverHandler.send(new StopLiveMode());
    }
  }
  /****************************************************************************
  * FiltersButtonClicked.
  ****************************************************************************/
  private class FiltersButtonClicked implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      auxPanel.removeAll();
      auxPanel.add(filterManager);
      revalidate();
      repaint();
    }
  }
  /****************************************************************************
  * EraseButtonClicked.
  ****************************************************************************/
  private class EraseButtonClicked implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      if (mode == PLAY_DONE_MODE || mode == PLAY_STOPPED_MODE) {
      	mode = NO_MODE;
      }
      erase();
    }
  }
  /****************************************************************************
  * AutoScrollButtonClicked.
  ****************************************************************************/
  private class AutoScrollButtonClicked implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      autoScroll = !autoScroll;
      autoScrollButton.setIcon(autoScroll ? GUtil.autoScrollOnIcon : GUtil.autoScrollOffIcon);
    }
  }
  /****************************************************************************
  * PlayEventsButtonClicked.
  ****************************************************************************/
  private class PlayEventsButtonClicked implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      Console.serverHandler.send(new PlayEvents(filterManager.getMaxCount(),filterManager.getFilters()));
    }
  }
  /****************************************************************************
  * StopEventsButtonClicked.
  ****************************************************************************/
  private class StopEventsButtonClicked implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      if (mode == PLAY_STOPPED_MODE) {
      	mode = NO_MODE;
      	updateStatus();
      } else {
	Console.serverHandler.send(new StopEvents());
      }
    }
  }
  /****************************************************************************
  * animate.
  ****************************************************************************/
  public void animate (int count) {
    if (filterManager.getFilterCount() > 0) {
      if (auxPanel.getComponentCount() == 0) {
	filtersButton.setIcon(GUtil.filteringIcons[count/4]);
      	return;
      }
      Component component = auxPanel.getComponent(0);
      if (!(component instanceof FilterManager)) {
	filtersButton.setIcon(GUtil.filteringIcons[count/4]);
	return;
      }
    }
    filtersButton.setIcon(GUtil.filtersIcon);
  }
}
