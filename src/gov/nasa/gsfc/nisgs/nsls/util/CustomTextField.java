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
*  14-Mar-06, 	Original version.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.util;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class CustomTextField extends JTextField {
  private static final long serialVersionUID = 1L;
  /****************************************************************************
  * CustomTextField.
  ****************************************************************************/
  public CustomTextField () {
    super();
    setMaximumSize(new Dimension(getMaximumSize().width,getPreferredSize().height));
  }
  public CustomTextField (String text) {
    super(text);
    setMaximumSize(new Dimension(getMaximumSize().width,getPreferredSize().height));
  }
  public CustomTextField (int columns) {
    super(columns);
    setMaximumSize(getPreferredSize());
  }
  public CustomTextField (String text, int columns) {
    super(text,columns);
    setMaximumSize(new Dimension(getPreferredSize().width,getPreferredSize().height));
  }
  public CustomTextField (String text, Font font, Border border, Color background, boolean editable) {
    super(text);
    setFont(font);
    setBorder(border);
    setBackground(background);
    setEditable(editable);
    setCaretPosition(0);
    setMaximumSize(new Dimension(getMaximumSize().width,getPreferredSize().height));
  }
}
