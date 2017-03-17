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

public class MarginPanel extends JPanel {
  public static final int GLUE = -1;
  private static final long serialVersionUID = 1L;
  /****************************************************************************
  * MarginPanel (constructor).
  ****************************************************************************/
  public MarginPanel (int top, int left,
		      Component component,
		      int bottom, int right) {
    setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
    switch (top) {
      case GLUE: {
	add(GUtil.VGlue());
      	break;
      }
      case 0: {
      	break;
      }
      default: {
        add(GUtil.VSpace(top));
        break;
      }
    }
    Box box = Box.createHorizontalBox();
    switch (left) {
      case GLUE: {
	box.add(GUtil.HGlue());
      	break;
      }
      case 0: {
      	break;
      }
      default: {
        box.add(GUtil.HSpace(left));
        break;
      }
    }
    box.add(component);
    switch (right) {
      case GLUE: {
	box.add(GUtil.HGlue());
      	break;
      }
      case 0: {
      	break;
      }
      default: {
	box.add(GUtil.HSpace(right));
	break;
      }
    }
    add(box);
    switch (bottom) {
      case GLUE: {
	add(GUtil.VGlue());
      	break;
      }
      case 0: {
      	break;
      }
      default: {
	add(GUtil.VSpace(bottom));
	break;
      }
    }
  }
}
