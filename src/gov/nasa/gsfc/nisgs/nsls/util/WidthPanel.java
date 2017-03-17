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
*  13-Mar-06, 	Original version.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.util;
// import javax.swing.border.*;
import javax.swing.*;
import java.awt.*;

public class WidthPanel extends JPanel {
  private int width;
  private int height;
  private JComponent component;
  private static final long serialVersionUID = 1L;
  /****************************************************************************
  * WidthPanel.
  ****************************************************************************/
  public WidthPanel (int width, JComponent component) {
    this(width,component,null);
  }
  public WidthPanel (int width, JComponent component, Color color) {
    this.width = (width != 0 ? width : component.getPreferredSize().width);
    this.component = component;
    this.height = component.getPreferredSize().height;
    setLayout(null);
    if (color != null) {
      setBackground(color);
    }
    set();
    add(component);
  }
  /****************************************************************************
  * incrWidth.
  ****************************************************************************/
  public void incrWidth (int incr) {
    width += incr;
    set();
  }
  /****************************************************************************
  * set.
  ****************************************************************************/
  private void set () {
    Dimension dim = new Dimension(width,height);
    setMinimumSize(dim);
    setPreferredSize(dim);
    setMaximumSize(dim);
    component.setBounds(0,0,Util.min(width,component.getPreferredSize().width),height);
  }
}
