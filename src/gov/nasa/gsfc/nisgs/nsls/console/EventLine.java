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
*  18-Mar-06, 	Original version.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.console;
import gov.nasa.gsfc.nisgs.nsls.*;
import gov.nasa.gsfc.nisgs.nsls.util.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class EventLine extends HPanel {
  private LogEvent event;
  private EventLine prevLine = null;
  private EventLine nextLine = null;
  private Color defaultBackground;
  private CustomButton detailsField;
  private WidthPanel[] widthPanels;
  private static WidthPanel[] headingWidthPanels = new WidthPanel[] {null,null,null,null};
  private static int[] fieldWidth = new int[] {13,200,455,0};
  private static final int DETAILS = 0;
  private static final int DATE_TIME = 1;
  private static final int HOST_SOURCE = 2;
  private static final int SUMMARY = 3;
  private static final int SPACER_WIDTH_1 = 10;
  private static final int SPACER_WIDTH_2 = 25;
  private static final int INCR = 5;
  private static final Font font = new Font("SansSerif",Font.PLAIN,12);
  private static final long serialVersionUID = 1L;
  /****************************************************************************
  * EventLine.
  ****************************************************************************/
  public EventLine (LogEvent event) {
    this.event = event;
    setAlignmentX(LEFT_ALIGNMENT);
    defaultBackground = getBackground();
    setBorder(new EmptyBorder(2,5,2,1));
    Color levelColor = getLevelColor(event.getLevel());
    setBackground(levelColor);
    detailsField = new CustomButton(GUtil.detailsIcon);
    detailsField.addActionListener(new DetailsFieldClicked());
    String description = (event.getText() != null ? event.getText() : "");
    if (event.getThrowable() != null) {
      description += (description.length() > 0 ? ", " : "") +
      		     event.getThrowable().toString();
    } else {
      if (event.getParameters() != null) {
      	description += (description.length() > 0 ? ", " : "") +
		       event.getParameters().toString();
      }
    }
    String hostSource = (event.getHost() != null ? event.getHost() : "NULL HOST") + ", " + (event.getSource() != null ? event.getSource().toString() : "UNKNOWN SOURCE");
    widthPanels = new WidthPanel[] {
      new WidthPanel(fieldWidth[DETAILS],detailsField,levelColor),
      new WidthPanel(fieldWidth[DATE_TIME],new CustomTextField(Util.encodeDate(event.getDate()),font,null,getBackground(),false),levelColor),
      new WidthPanel(fieldWidth[HOST_SOURCE],new CustomTextField(hostSource,font,null,getBackground(),false),levelColor),
      new WidthPanel(fieldWidth[SUMMARY],new CustomTextField(description,font,null,getBackground(),false),levelColor)
    };
    HPanel h1 = new HPanel(widthPanels[DETAILS],GUtil.HSpace(SPACER_WIDTH_1),
    			   widthPanels[DATE_TIME],GUtil.HSpace(SPACER_WIDTH_2),
		           widthPanels[HOST_SOURCE],GUtil.HSpace(SPACER_WIDTH_2),
		           widthPanels[SUMMARY]);
    h1.setBackground(levelColor);
    add(h1);
    add(GUtil.HGlue());
  }
  /****************************************************************************
  * getEvent.
  ****************************************************************************/
  public LogEvent getEvent () {
    return event;
  }
  /****************************************************************************
  * getNextLine.
  ****************************************************************************/
  public EventLine getNextLine () {
    return nextLine;
  }
  /****************************************************************************
  * getPrevLine.
  ****************************************************************************/
  public EventLine getPrevLine () {
    return prevLine;
  }
  /****************************************************************************
  * getLevelColor.
  ****************************************************************************/
  private Color getLevelColor (int level) {
    switch (level) {
      case Log.INFO_EVENT: {
      	return defaultBackground;
      }
      case Log.WARNING_EVENT: {
      	return Color.yellow;
      }
      case Log.ERROR_EVENT: {
      	return Color.red;
      }
      default: {
      	return Color.gray;
      }
    }
  }
  /****************************************************************************
  * setPrevLine.
  ****************************************************************************/
  public void setPrevLine (EventLine prevLine) {
    this.prevLine = prevLine;
  }
  /****************************************************************************
  * setNextLine.
  ****************************************************************************/
  public void setNextLine (EventLine nextLine) {
    this.nextLine = nextLine;
  }
  /****************************************************************************
  * setShowingDetails.
  ****************************************************************************/
  public void setShowingDetails (boolean showing) {
    detailsField.setIcon(showing ? GUtil.detailsOnIcon : GUtil.detailsIcon);
  }
  /****************************************************************************
  * incrField.
  ****************************************************************************/
  public void incrField (int index, int incr) {
    widthPanels[index].incrWidth(incr);
    widthPanels[index].revalidate();
    widthPanels[index].repaint();
  }
  /****************************************************************************
  * DetailsFieldClicked.
  ****************************************************************************/
  private class DetailsFieldClicked implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      showDetails();
    }
  }
  private void showDetails () {
    Console.serverHandler.eventTable.showDetails(this);
  }
  /****************************************************************************
  * getHeading.
  ****************************************************************************/
  public static HPanel getHeading () {
    headingWidthPanels[DETAILS] = new WidthPanel(fieldWidth[DETAILS],new CustomLabel(" ",fieldWidth[DETAILS]));
    headingWidthPanels[DATE_TIME] = new WidthPanel(fieldWidth[DATE_TIME],new CustomLabel("Date/Time"));
    headingWidthPanels[DATE_TIME].addMouseListener(new HeadingPanelClicked(DATE_TIME));
    headingWidthPanels[HOST_SOURCE] = new WidthPanel(fieldWidth[HOST_SOURCE],new CustomLabel("Host/Source"));
    headingWidthPanels[HOST_SOURCE].addMouseListener(new HeadingPanelClicked(HOST_SOURCE));
    headingWidthPanels[SUMMARY] = new WidthPanel(fieldWidth[SUMMARY],new CustomLabel("Summary"));
    HPanel panel =  new HPanel(headingWidthPanels[DETAILS],GUtil.HSpace(SPACER_WIDTH_1),
    		               headingWidthPanels[DATE_TIME],GUtil.HSpace(SPACER_WIDTH_2),
		               headingWidthPanels[HOST_SOURCE],GUtil.HSpace(SPACER_WIDTH_2),
		               headingWidthPanels[SUMMARY]);
    panel.setBorder(new EmptyBorder(1,5,1,1));
    return panel;
  }
  private static class HeadingPanelClicked extends MouseAdapter {
    private int index;
    public HeadingPanelClicked (int index) {
      this.index = index;
    }
    public void mouseClicked (MouseEvent me) {
      int incr = (me.getButton() == MouseEvent.BUTTON1 ? INCR : -INCR);
      fieldWidth[index] += incr;
      headingWidthPanels[index].incrWidth(incr);
      Console.serverHandler.eventTable.incrField(index,incr);
      headingWidthPanels[index].revalidate();
      headingWidthPanels[index].repaint();
    }
  }
}
