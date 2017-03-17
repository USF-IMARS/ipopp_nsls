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

package gov.nasa.gsfc.nisgs.nsls.util;
import javax.swing.*;
import java.util.*;

public class MenuBarClock extends JLabel {
  private JLabel thisLabel;
  private static final long serialVersionUID = 1L;
  /****************************************************************************
  * MenuBarClock.
  ****************************************************************************/
  public MenuBarClock (boolean gmt) {
    super(" ");
    thisLabel = this;
  }
  /****************************************************************************
  * start.
  ****************************************************************************/
  public void start () {
    java.util.Timer timer = new java.util.Timer();
    timer.schedule(new UpdateClock(),0,100);
  }
  /****************************************************************************
  * UpdateClock.
  ****************************************************************************/
  private class UpdateClock extends TimerTask {
    public void run () {
      String text = Util.encodeDateNoMS(new Date());
      GUtil.invokeAndWait(new GUtil.UpdateLabel(thisLabel,text));
    }
  }
}
