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

public class CustomLabel extends JLabel{
  private static final Font defaultFont = new Font("SansSerif",Font.BOLD,12);
  private static final long serialVersionUID = 1L;
  /****************************************************************************
  * CustomLabel.
  ****************************************************************************/
  public CustomLabel (String text) {
    super(text);
    set(Color.black,defaultFont);
  }
  public CustomLabel (String text, int width) {
    super(text);
    set(Color.black,defaultFont);
    Dimension dims = new Dimension(width,getPreferredSize().height);
    setMinimumSize(dims);
    setPreferredSize(dims);
    setMaximumSize(dims);
  }
  public CustomLabel (String text, Font font) {
    super(text);
    set(Color.black,font);
  }
  public CustomLabel (String text, Color color) {
    super(text);
    set(color,defaultFont);
  }
  /****************************************************************************
  * set.
  ****************************************************************************/
  private void set (Color color, Font font) {
    setFont(font);
    setForeground(color);
  }
  /****************************************************************************
  * update.
  ****************************************************************************/
  public void update (String text) {
    setText(text);
  }
  public void update (String text, Color color) {
    setForeground(color);
    setText(text);
  }
}
