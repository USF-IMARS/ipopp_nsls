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
import java.awt.*;
import javax.swing.*;

public class CustomButton extends JButton {
  private static final long serialVersionUID = 1L;
  /****************************************************************************
  * CustomButton.
  ****************************************************************************/
  public CustomButton (ImageIcon icon) {
    super(icon);
    setMargin(new Insets(0,0,0,0));
  }
}
