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
import java.util.*;
import java.awt.*;
import java.awt.event.*;

public class DateTimeFilterHandler extends FilterHandler {
  private CustomTextField startField;
  private CustomTextField stopField;
  // private CustomCombo setCombo;
  private JCheckBox notCheckBox;
  private static final String[] countChoices = new String[] {
    "1","2","3","5","10","12","15","30","45"
  };
  private static final String[] unitChoices = new String[] {
    "minutes","hours","days","weeks"
  };
  private static final long serialVersionUID = 1L;
  /****************************************************************************
  * DateTimeFilterHandler.
  ****************************************************************************/
  public DateTimeFilterHandler () {
    MarginPanel m1 = new MarginPanel(2,4,new HPanel(new CustomLabel("Date/Time:"),GUtil.HGlue(),deleteButton),2,2);
    startField = new CustomTextField();
    startField.addKeyListener(new KeyTyped());
    stopField = new CustomTextField();
    stopField.addKeyListener(new KeyTyped());    
    JMenuBar menuBar = new JMenuBar();
    JMenu setMenu = new JMenu("Set To...");
    setMenu.add(new JMenuItem("Today")).addActionListener(new SetTo("today"));
    setMenu.add(new JMenuItem("Yesterday")).addActionListener(new SetTo("yesterday"));
    JMenu pastMenu = new JMenu("Past");
    for (int i = 0; i < countChoices.length; i++) {
       JMenu countMenu = new JMenu(countChoices[i]);
       for (int j = 0; j < unitChoices.length; j++) {
          countMenu.add(new JMenuItem(unitChoices[j])).addActionListener(new SetTo(unitChoices[j]+"."+countChoices[i]));
       }
       pastMenu.add(countMenu);
    }
    setMenu.add(pastMenu);
    menuBar.add(setMenu);
    menuBar.setBorder(new LineBorder(Color.gray,1));
    menuBar.setMaximumSize(menuBar.getPreferredSize());
    notCheckBox = new JCheckBox("Not",false);
    HPanel h0a = new HPanel(menuBar,GUtil.HGlue());
    HPanel h0b = new HPanel(GUtil.HGlue(),notCheckBox);
    VPanel v0 = new VPanel(GUtil.VSpace(5),startField,GUtil.VSpace(5),stopField,GUtil.VSpace(5),h0a,GUtil.VGlue(),h0b);
    HPanel h1 = new HPanel(GUtil.HSpace(10),v0,GUtil.HSpace(8));
    VPanel v1 = new VPanel(m1,h1);
    v1.setBorder(new BevelBorder(BevelBorder.LOWERED));
    add(new MarginPanel(2,2,v1,2,2));
  }
  /****************************************************************************
  * getFilter.
  ****************************************************************************/
  public Filter getFilter () {
    try {
      Date start = Util.parseDate(startField.getText());
      Date stop = Util.parseDate(stopField.getText());
      return new DateTimeFilter(start,stop,notCheckBox.isSelected());
    } catch (Exception e) {
      return null;
    }
  }
  /****************************************************************************
  * KeyTyped.
  ****************************************************************************/
  private class KeyTyped extends KeyAdapter {
    public void keyTyped (KeyEvent ke) {
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
      	// Date date = Util.parseDate(field.getText());
      	field.setForeground(Color.black);
      } catch (Exception e) {
      	field.setForeground(Color.red);
      }
    }
  }
  /****************************************************************************
  * SetTo.
  ****************************************************************************/
  private class SetTo implements ActionListener {
    private String when;
    public SetTo (String when) {
      this.when = when;
    }
    public void actionPerformed (ActionEvent ae) {
      set();
    }
    private void set () {
      if (when.equals("today")) {
      	long now = System.currentTimeMillis();
      	long ms = now /*- (now % 1000)*/;
      	ms -= (ms % Util.MS_PER_DAY);
      	startField.setText(Util.encodeDate(new Date(ms)));
      	ms += (Util.MS_PER_DAY - 1/*000*/);
      	stopField.setText(Util.encodeDate(new Date(ms)));
        return;
      }
      if (when.equals("yesterday")) {
      	long now = System.currentTimeMillis();
      	long ms = now /*- (now % 1000)*/;
      	ms -= Util.MS_PER_DAY;
      	ms -= (ms % Util.MS_PER_DAY);
      	startField.setText(Util.encodeDate(new Date(ms)));
      	ms += (Util.MS_PER_DAY - 1/*000*/);
      	stopField.setText(Util.encodeDate(new Date(ms)));
        return;
      }
      StringTokenizer byDot = new StringTokenizer(when,".");
      String unit = byDot.nextToken();
      int count = Integer.parseInt(byDot.nextToken());
      if (unit.equals("minutes")) {
        setPastMins(count);
        return;
      }
      if (unit.equals("hours")) {
        setPastMins(60*count);
        return;
      }
      if (unit.equals("days")) {
        setPastMins(24*60*count);
        return;
      }
      if (unit.equals("weeks")) {
        setPastMins(7*24*60*count);
        return;
      }
    }
  }
  /****************************************************************************
  * setPastMins.
  ****************************************************************************/
  private void setPastMins (long nMins) {
    long now = System.currentTimeMillis();
    long ms = now;
    stopField.setText(Util.encodeDate(new Date(ms)));
    ms -= ((nMins * Util.MS_PER_MIN) - 1);
    startField.setText(Util.encodeDate(new Date(ms)));
  }
}
