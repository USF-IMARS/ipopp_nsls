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

public class TextFilterHandler extends FilterHandler {
  private CustomTextField field;
  private JCheckBox textOnlyCheckBox;
  private JCheckBox notCheckBox;
  private static final long serialVersionUID = 1L;
  /****************************************************************************
  * TextFilterHandler.
  ****************************************************************************/
  public TextFilterHandler () {
    MarginPanel m1 = new MarginPanel(3,5,new HPanel(new CustomLabel("Text:"),GUtil.HGlue(),deleteButton),3,3);
    field = new CustomTextField();
    HPanel h1 = new HPanel(GUtil.HSpace(10),new VPanel(GUtil.VSpace(5),field,GUtil.VSpace(5)),GUtil.HSpace(8));
    textOnlyCheckBox = new JCheckBox("Text Field Only",true);
    notCheckBox = new JCheckBox("Not",false);
    HPanel h2 = new HPanel(GUtil.HSpace(5),textOnlyCheckBox,GUtil.HGlue(),notCheckBox,GUtil.HSpace(5));
    VPanel v1 = new VPanel(m1,h1,GUtil.VGlue(),h2);
    v1.setBorder(new BevelBorder(BevelBorder.LOWERED));
    add(new MarginPanel(2,2,v1,2,2));
  }
  /****************************************************************************
  * getFilter.
  ****************************************************************************/
  public Filter getFilter () {
    String text = field.getText();
    if (!text.trim().equals("")) {
      return new TextFilter(text,textOnlyCheckBox.isSelected(),notCheckBox.isSelected());
    }
    return null;
  }
}
