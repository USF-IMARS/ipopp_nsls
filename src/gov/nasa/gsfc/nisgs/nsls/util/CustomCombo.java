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

@SuppressWarnings("unchecked")
public class CustomCombo extends JComboBox {
  private static final long serialVersionUID = 1L;
  /****************************************************************************
  * CustomCombo.
  ****************************************************************************/
  public CustomCombo (String[] items) {
    super(items);
    setMaximumSize(getPreferredSize());
  }
}
