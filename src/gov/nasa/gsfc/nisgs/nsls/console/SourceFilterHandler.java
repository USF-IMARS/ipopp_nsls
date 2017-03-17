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
*   9-Jun-06, 	Added RT-STPS.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.console;
import gov.nasa.gsfc.nisgs.nsls.filter.*;
import gov.nasa.gsfc.nisgs.nsls.util.*;
import javax.swing.*;
import javax.swing.border.*;

public class SourceFilterHandler extends FilterHandler {
  private JCheckBox mmsCheckBox;
  private JCheckBox dsmCheckBox;
  private JCheckBox ncsCheckBox;
  private JCheckBox isCheckBox;
  private JCheckBox scCheckBox;
  private JCheckBox rtstpsCheckBox;
  private JCheckBox notCheckBox;
  private static final long serialVersionUID = 1L;
  /****************************************************************************
  * SourceFilterHandler.
  ****************************************************************************/
  public SourceFilterHandler () {
    MarginPanel m1 = new MarginPanel(3,5,new HPanel(new CustomLabel("Source:"),GUtil.HGlue(),deleteButton),3,3);
    mmsCheckBox = new JCheckBox("MMS",false);
    dsmCheckBox = new JCheckBox("DSM",false);
    ncsCheckBox = new JCheckBox("NCS",false);
    isCheckBox = new JCheckBox("IS",false);
    scCheckBox = new JCheckBox("SC",false);
    rtstpsCheckBox = new JCheckBox("RT-STPS",false);
    VPanel v1 = new VPanel(mmsCheckBox,scCheckBox,rtstpsCheckBox,GUtil.VGlue());
    VPanel v2 = new VPanel(ncsCheckBox,dsmCheckBox,isCheckBox,GUtil.VGlue());
    notCheckBox = new JCheckBox("Not",false);
    VPanel v3 = new VPanel(GUtil.VGlue(),notCheckBox);
    HPanel h1 = new HPanel(GUtil.HSpace(5),v1,GUtil.HSpace(10),v2,GUtil.HSpace(20),GUtil.HGlue(),v3,GUtil.HSpace(5));
    VPanel v4 = new VPanel(m1,h1);
    v4.setBorder(new BevelBorder(BevelBorder.LOWERED));
    add(new MarginPanel(2,2,v4,2,2));
  }
  /****************************************************************************
  * getFilter.
  ****************************************************************************/
  public Filter getFilter () {
    return new SourceFilter(mmsCheckBox.isSelected(),dsmCheckBox.isSelected(),
    			    ncsCheckBox.isSelected(),isCheckBox.isSelected(),
    			    scCheckBox.isSelected(),rtstpsCheckBox.isSelected(),
    			    notCheckBox.isSelected());
  }
}
