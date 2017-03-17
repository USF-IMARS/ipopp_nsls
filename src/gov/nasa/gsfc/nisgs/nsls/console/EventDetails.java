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
*   8-Feb-06, 	Original version.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.console;
import gov.nasa.gsfc.nisgs.nsls.LogEvent;
import gov.nasa.gsfc.nisgs.nsls.util.*;
import javax.swing.*;
// import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class EventDetails extends JPanel {
  private EventLine line;
  private static final int maxHeight = 400;
  private static final Font font = new Font("SansSerif",Font.PLAIN,12);
  private static final long serialVersionUID = 1L;
  /****************************************************************************
  * EventDetails.
  ****************************************************************************/
  public EventDetails (EventLine line, int minHeight) {
    this.line = line;
    LogEvent event = line.getEvent();
    setLayout(new BorderLayout());
    // setBorder(new LineBorder(Color.red,1));
    JButton prevButton = new JButton(GUtil.prevIcon);
    prevButton.setMargin(new Insets(0,0,0,0));
    prevButton.addActionListener(new PrevButtonClicked());
    prevButton.setToolTipText("Examine Previous Event");
    JButton nextButton = new JButton(GUtil.nextIcon);
    nextButton.setMargin(new Insets(0,0,0,0));
    nextButton.addActionListener(new NextButtonClicked());
    nextButton.setToolTipText("Examine Next Event");
    JButton saveButton = new JButton(GUtil.saveIcon);
    saveButton.setMargin(new Insets(0,0,0,0));
    saveButton.addActionListener(new SaveButtonClicked());
    saveButton.setToolTipText("Save Event Details to File");
    JButton closeButton = new JButton(GUtil.closeIcon);
    closeButton.setMargin(new Insets(0,0,0,0));
    closeButton.addActionListener(new CloseButtonClicked());
    closeButton.setToolTipText("Close Event Details");
    add(new VPanel(GUtil.VSpace(3),new HPanel(GUtil.HSpace(3),prevButton,nextButton,GUtil.HGlue(),saveButton,closeButton,GUtil.HSpace(3)),GUtil.VSpace(3)),BorderLayout.NORTH);
    VPanel v1 = new VPanel();
    v1.add(new HPanel(new CustomLabel("Level:"),GUtil.HGlue()));
    v1.add(new HPanel(GUtil.HSpace(10),new CustomLabel(event.encodeLevel(),font),GUtil.HGlue()));
    v1.add(new HPanel(new CustomLabel("Date:"),GUtil.HGlue()));
    v1.add(new HPanel(GUtil.HSpace(10),new CustomLabel(Util.encodeDate(event.getDate()),font),GUtil.HGlue()));
    v1.add(new HPanel(new CustomLabel("Source:"),GUtil.HGlue()));
    v1.add(new HPanel(GUtil.HSpace(10),new CustomLabel(event.getSource() != null ? event.getSource().toString() : " ",font),GUtil.HGlue()));
    v1.add(new HPanel(new CustomLabel("Parameters:"),GUtil.HGlue()));
    if (event.getParameters() != null) {
      String[] lines = event.getParameters().toStrings();
      for (int i = 0; i < lines.length; i++) {
         // if (lines[i] != null) {
      	   v1.add(new HPanel(GUtil.HSpace(10),new CustomLabel(lines[i],font),GUtil.HGlue()));
         // }
      }
    } else {
      v1.add(new HPanel(GUtil.HSpace(10),new CustomLabel(" ",font),GUtil.HGlue()));
    }
    v1.setAlignmentY(TOP_ALIGNMENT);
    VPanel v2 = new VPanel();
    v2.add(new HPanel(new CustomLabel("Host:"),GUtil.HGlue()));
    v2.add(new HPanel(GUtil.HSpace(10),new CustomLabel((event.getHost() != null ? event.getHost() : "NULL HOST"),font),GUtil.HGlue()));
    v2.add(new HPanel(new CustomLabel("Thread:"),GUtil.HGlue()));
    v2.add(new HPanel(GUtil.HSpace(10),new CustomLabel(event.getThreadName(),font),GUtil.HGlue()));
    v2.add(new HPanel(new CustomLabel("Stack:"),GUtil.HGlue()));
    String[] stackTrace = event.getStackTrace();
    for (int i = 0; i < stackTrace.length; i++) {
      v2.add(new HPanel(GUtil.HSpace(10),new CustomLabel(stackTrace[i],font),GUtil.HGlue()));
    }
    v2.setAlignmentY(TOP_ALIGNMENT);
    VPanel v3 = new VPanel();
    if (event.getThrowable() != null) {
      v3.add(new HPanel(new CustomLabel("Throwable:"),GUtil.HGlue()));
      String[] lines = Util.encodeThrowableLines(event.getThrowable());
      for (int i = 0; i < lines.length; i++) {
      	 v3.add(new HPanel(GUtil.HSpace(10),new CustomLabel(lines[i],font),GUtil.HGlue()));
      }
    }
    v3.setAlignmentY(TOP_ALIGNMENT);
    VPanel v4 = new VPanel();
    v4.add(new HPanel(new CustomLabel("Text:"),GUtil.HGlue()));
    if (event.getText() != null) {
      String[] lines = Util.parseLines(event.getText(),"\n");
      for (int i = 0; i < lines.length; i++) {
	 v4.add(new HPanel(GUtil.HSpace(10),new WrapPanel(lines[i])));
      }
    } else {
      v4.add(new HPanel(GUtil.HSpace(10),new CustomLabel(" ",font),GUtil.HGlue()));
    }
    add(new JScrollPane(new MarginPanel(4,4,new VPanel(new HPanel(v1,v2,v3),v4),4,4),JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),BorderLayout.CENTER);
    add(GUtil.VSpace(3),BorderLayout.SOUTH);
    if (minHeight != 0) {
      if (getPreferredSize().height < minHeight) {
        setMinimumSize(new Dimension(getMinimumSize().width,minHeight));
        setPreferredSize(new Dimension(getPreferredSize().width,minHeight));
      } else {
      	if (getPreferredSize().height > maxHeight) {
	  setPreferredSize(new Dimension(getPreferredSize().width,maxHeight));
	  setMaximumSize(new Dimension(Integer.MAX_VALUE,maxHeight));
      	}
      }
    } else {
      if (getPreferredSize().height > maxHeight) {
	setPreferredSize(new Dimension(getPreferredSize().width,maxHeight));
	setMaximumSize(new Dimension(Integer.MAX_VALUE,maxHeight));
      }
    }
  }
  /****************************************************************************
  * getEventLine.
  ****************************************************************************/
  public EventLine getEventLine () {
    return line;
  }
  /****************************************************************************
  * PrevButtonClicked.
  ****************************************************************************/
  private class PrevButtonClicked implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      if (line.getPrevLine() != null) {
      	Console.serverHandler.eventTable.showDetails(line.getPrevLine());
      }
    }
  }
  /****************************************************************************
  * NextButtonClicked.
  ****************************************************************************/
  private class NextButtonClicked implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      if (line.getNextLine() != null) {
      	Console.serverHandler.eventTable.showDetails(line.getNextLine());
      }
    }
  }
  /****************************************************************************
  * SaveButtonClicked.
  ****************************************************************************/
  private class SaveButtonClicked implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      JFileChooser chooser = new JFileChooser();
      File file = new File(System.getProperty("user.home") +
			   File.separator + "event.txt");
      chooser.setCurrentDirectory(file);
      chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
      chooser.setSelectedFile(file);
      switch (chooser.showSaveDialog(null)) {
        case JFileChooser.APPROVE_OPTION: {
	  file = chooser.getSelectedFile();
	  saveAs(file);
          break;
        }
      }
    }
  }
  private void saveAs (File file) {
    PrintWriter out = null;
    try {
      out = new PrintWriter(file);
      writeEvent(out);
      out.close();
    } catch (Exception e) {
      JOptionPane.showMessageDialog(null,e.toString(),"Error Saving Event Details to File",JOptionPane.ERROR_MESSAGE);
    } finally {
      try {out.close();} catch (Exception e) {}
    }
  }
  private void writeEvent (PrintWriter out) throws Exception {
    LogEvent event = line.getEvent();
    out.println("Date/Time:");
    out.println("  " + Util.encodeDate(event.getDate()));
    out.println("Level:");
    out.println("  " + event.encodeLevel());
    if (event.getHost() != null) {
      out.println("Host:");
      out.println("  " + event.getHost());
    }
    if (event.getSource() != null) {
      out.println("Source:");
      out.println("  " + event.getSource().toString());
    }
    if (event.getParameters() != null) {
      out.println("Parameters:");
      String[] lines = event.getParameters().toStrings();
      for (int i = 0; i < lines.length; i++) {
	 out.println("  " + lines[i]);
      }
    }
    out.println("Thread:");
    out.println("  " + event.getThreadName());
    out.println("Stack:");
    String[] stackTrace = event.getStackTrace();
    for (int i = 0; i < stackTrace.length; i++) {
       out.println("  " + stackTrace[i]);
    }
    if (event.getText() != null) {
      out.println("Text:");
      String[] lines = Util.parseLines(event.getText(),"\n");
      for (int i = 0; i < lines.length; i++) {
         out.println("  " + lines[i]);
      }
    }
    if (event.getThrowable() != null) {
      out.println("Throwable:");
      String[] lines = Util.encodeThrowableLines(event.getThrowable());
      for (int i = 0; i < lines.length; i++) {
         out.println("  " + lines[i]);
      }
    }
  }
  /****************************************************************************
  * CloseButtonClicked.
  ****************************************************************************/
  private class CloseButtonClicked implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      Console.serverHandler.eventTable.closeDetails();
    }
  }
}
