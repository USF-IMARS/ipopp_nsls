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

public class VPanel extends JPanel {
  private static final long serialVersionUID = 1L;
  /****************************************************************************
  * VPanel (constructor).
  ****************************************************************************/
  public VPanel () {
    construct(new Component[] {});
  }
  public VPanel (Component c1) {
    construct(new Component[] {c1});
  }
  public VPanel (Component c1, Component c2) {
    construct(new Component[] {c1,c2});
  }
  public VPanel (Component c1, Component c2, Component c3) {
    construct(new Component[] {c1,c2,c3});
  }
  public VPanel (Component c1, Component c2, Component c3, Component c4) {
    construct(new Component[] {c1,c2,c3,c4});
  }
  public VPanel (Component c1, Component c2, Component c3, Component c4,
  		 Component c5) {
    construct(new Component[] {c1,c2,c3,c4,c5});
  }
  public VPanel (Component c1, Component c2, Component c3, Component c4,
  		 Component c5, Component c6) {
    construct(new Component[] {c1,c2,c3,c4,c5,c6});
  }
  public VPanel (Component c1, Component c2, Component c3, Component c4,
  		 Component c5, Component c6, Component c7) {
    construct(new Component[] {c1,c2,c3,c4,c5,c6,c7});
  }
  public VPanel (Component c1, Component c2, Component c3, Component c4,
  		 Component c5, Component c6, Component c7, Component c8) {
    construct(new Component[] {c1,c2,c3,c4,c5,c6,c7,c8});
  }
  public VPanel (Component c1, Component c2, Component c3, Component c4,
  		 Component c5, Component c6, Component c7, Component c8,
		 Component c9) {
    construct(new Component[] {c1,c2,c3,c4,c5,c6,c7,c8,c9});
  }
  public VPanel (Component c1, Component c2, Component c3, Component c4,
  		 Component c5, Component c6, Component c7, Component c8,
		 Component c9, Component c10) {
    construct(new Component[] {c1,c2,c3,c4,c5,c6,c7,c8,c9,c10});
  }
  public VPanel (Component c1, Component c2, Component c3, Component c4,
  		 Component c5, Component c6, Component c7, Component c8,
		 Component c9, Component c10, Component c11) {
    construct(new Component[] {c1,c2,c3,c4,c5,c6,c7,c8,c9,c10,c11});
  }
  public VPanel (Component[] components) {
    construct(components);
  }
  private void construct (Component[] components) {
    setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
    for (int i = 0; i < components.length; i++) {
       if (components[i] != null) {
	 add(components[i]);
       }
    }
  }
}
