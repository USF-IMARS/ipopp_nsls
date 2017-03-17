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
import gov.nasa.gsfc.nisgs.nsls.filter.*;
import gov.nasa.gsfc.nisgs.nsls.util.*;
import javax.swing.*;
import javax.swing.border.*;

public class LevelFilterHandler extends FilterHandler {
  private JCheckBox infoCheckBox;
  private JCheckBox warningCheckBox;
  private JCheckBox errorCheckBox;
  private JCheckBox notCheckBox;
  private static final long serialVersionUID = 1L;
  /****************************************************************************
  * LevelFilterHandler.
  ****************************************************************************/
  public LevelFilterHandler () {
    MarginPanel m1 = new MarginPanel(2,4,new HPanel(new CustomLabel("Level:"),GUtil.HGlue(),deleteButton),2,2);
    infoCheckBox = new JCheckBox("INFO",false);
    warningCheckBox = new JCheckBox("WARNING",false);
    errorCheckBox = new JCheckBox("ERROR",false);
    VPanel v1 = new VPanel(infoCheckBox,warningCheckBox,errorCheckBox,GUtil.VGlue());
    notCheckBox = new JCheckBox("Not",false);
    VPanel v2 = new VPanel(GUtil.VGlue(),notCheckBox);
    HPanel h1 = new HPanel(GUtil.HSpace(5),v1,GUtil.HSpace(20),GUtil.HGlue(),v2,GUtil.HSpace(5));
    VPanel v3 = new VPanel(m1,h1);
    v3.setBorder(new BevelBorder(BevelBorder.LOWERED));
    add(new MarginPanel(2,2,v3,2,2));
  }
  /****************************************************************************
  * getFilter.
  ****************************************************************************/
  public Filter getFilter () {
    return new LevelFilter(infoCheckBox.isSelected(),warningCheckBox.isSelected(),
			   errorCheckBox.isSelected(),notCheckBox.isSelected());
  }
}
