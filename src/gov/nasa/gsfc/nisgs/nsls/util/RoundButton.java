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
*  2014-01-31, 	Original version, much code copped from the web.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.util;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

public class RoundButton extends JButton {
  public RoundButton(String label) {
    super(label);
    setMargin (new Insets(0,0,0,0));
    Dimension size = getPreferredSize();
    size.width = size.height = Math.max(size.width,size.height);
    setPreferredSize(size);

    setContentAreaFilled(false);
  }

  protected void paintComponent(Graphics g) {
    if (getModel().isArmed()) {
      g.setColor(Color.lightGray);
    } else {
      g.setColor(getBackground());
    }
    g.fillOval(0, 0, getSize().width-1,getSize().height-1);

    super.paintComponent(g);
  }

  protected void paintBorder(Graphics g) {
    g.setColor(getForeground());
    g.drawOval(0, 0, getSize().width-1,     getSize().height-1);
  }

  Shape shape;
  public boolean contains(int x, int y) {
    if (shape == null || 
      !shape.getBounds().equals(getBounds())) {
      shape = new Ellipse2D.Float(0, 0, getWidth(), getHeight());
    }
    return shape.contains(x, y);
  }
    /*
  public static void main(String[] args) {
    JButton button = new RoundButton("Click");
    button.setBackground(Color.gray);

    JFrame frame = new JFrame();
    frame.getContentPane().add(button);
    frame.getContentPane().setLayout(new FlowLayout());
    frame.setSize(150, 150);
    frame.setVisible(true);
    }*/
}
