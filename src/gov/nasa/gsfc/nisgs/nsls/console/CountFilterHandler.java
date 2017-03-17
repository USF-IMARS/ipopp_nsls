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
import gov.nasa.gsfc.nisgs.nsls.util.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class CountFilterHandler extends VPanel {
  private JRadioButton fiftyRadioButton;
  private JRadioButton fiveHundredRadioButton;
  private JRadioButton specifiedRadioButton;
  private CustomTextField specifiedField;
  private static final int DEFAULT_COUNT = 1000;
  private static final int MAX_COUNT = 5000;
  private static final long serialVersionUID = 1L;
  /****************************************************************************
  * CountFilterHandler.
  ****************************************************************************/
  public CountFilterHandler () {
    MarginPanel m1 = new MarginPanel(2,4,new HPanel(new CustomLabel("Count:"),GUtil.HSpace(70)),2,2);
    m1.setAlignmentX(LEFT_ALIGNMENT);
    fiftyRadioButton = new JRadioButton("50",false);
    fiveHundredRadioButton = new JRadioButton("500",true);
    specifiedRadioButton = new JRadioButton("",false);
    GUtil.buttonGroup(fiftyRadioButton,fiveHundredRadioButton,specifiedRadioButton);
    specifiedField = new CustomTextField(Integer.toString(DEFAULT_COUNT),6);
    specifiedField.addKeyListener(new KeyTyped());
    HPanel h0 = new HPanel(specifiedRadioButton,specifiedField);
    h0.setAlignmentX(LEFT_ALIGNMENT);
    VPanel v0 = new VPanel(GUtil.VSpace(5),fiftyRadioButton,fiveHundredRadioButton,h0,GUtil.VSpace(5));
    v0.setAlignmentX(LEFT_ALIGNMENT);
    HPanel h1 = new HPanel(GUtil.HSpace(10),v0,GUtil.HSpace(8));
    h1.setAlignmentX(LEFT_ALIGNMENT);
    VPanel v1 = new VPanel(m1,h1,GUtil.VGlue());
    v1.setBorder(new BevelBorder(BevelBorder.LOWERED));
    add(new MarginPanel(2,2,v1,2,2));
  }
  /****************************************************************************
  * getCount.
  ****************************************************************************/
  public int getCount () {
    if (fiftyRadioButton.isSelected()) {
      return 50;
    }
    if (fiveHundredRadioButton.isSelected()) {
      return 500;
    }
    if (specifiedRadioButton.isSelected()) {
      try {
        int count = Integer.parseInt(specifiedField.getText());
        if (count < 1 || count > MAX_COUNT) {
          throw new Exception("illegal count");
        }
        return count;
      } catch (Exception e) {
        SwingUtilities.invokeLater(new ResetSpecifiedField());
      	return DEFAULT_COUNT;
      }
    }
    return DEFAULT_COUNT;
  }
  /****************************************************************************
  * ResetSpecifiedField.
  ****************************************************************************/
  private class ResetSpecifiedField implements Runnable {
    public void run () {
      specifiedField.setForeground(Color.black);
      specifiedField.setText(Integer.toString(DEFAULT_COUNT));
    }
  }
  /****************************************************************************
  * KeyTyped.
  ****************************************************************************/
  private class KeyTyped extends KeyAdapter {
    public void keyTyped (KeyEvent ke) {
      specifiedRadioButton.setSelected(true);
      CustomTextField field = (CustomTextField) ke.getSource();
      SwingUtilities.invokeLater(new CheckField(field));
    }
  }
  private class CheckField implements Runnable {
    private CustomTextField field;
    public CheckField (CustomTextField field) {
      this.field = field;
    }
    public void run () {
      try {
      	int count = Integer.parseInt(field.getText());
      	if (count < 1 || count > MAX_COUNT) {
      	  throw new Exception("illegal count");
      	}
      	field.setForeground(Color.black);
      } catch (Exception e) {
      	field.setForeground(Color.red);
      }
    }
  }
}
