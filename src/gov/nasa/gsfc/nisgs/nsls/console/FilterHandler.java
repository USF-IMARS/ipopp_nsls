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
*  13-Oct-05, 	Original version.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.console;
import gov.nasa.gsfc.nisgs.nsls.filter.*;
import gov.nasa.gsfc.nisgs.nsls.util.*;
import java.awt.event.*;

public abstract class FilterHandler extends VPanel {
  protected CustomButton deleteButton;
  /****************************************************************************
  * FilterHandler.
  ****************************************************************************/
  public FilterHandler () {
    deleteButton = new CustomButton(GUtil.trashIcon);
    deleteButton.addActionListener(new DeleteButtonClicked());
    deleteButton.setToolTipText("Delete Filter");
  }
  /****************************************************************************
  * getFilter.
  ****************************************************************************/
  public abstract Filter getFilter ();
  /****************************************************************************
  * DeleteButtonClicked.
  ****************************************************************************/
  private class DeleteButtonClicked implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      delete();
    }
  }
  private void delete () {
    Console.serverHandler.eventTable.filterManager.deleteFilter(this);
  }
}
