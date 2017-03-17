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
import java.awt.*;
import java.net.*;
import javax.swing.*;

public abstract class GUtil {
  public static final ImageIcon infoDetailsIcon = GUtil.getImageIcon("images/infoDetails.png");
  public static final ImageIcon warningDetailsIcon = GUtil.getImageIcon("images/warningDetails.gif");
  public static final ImageIcon errorDetailsIcon = GUtil.getImageIcon("images/errorDetails.gif");
  public static final ImageIcon eraseIcon = GUtil.getImageIcon("images/erase.gif");
  public static final ImageIcon lockedIcon = GUtil.getImageIcon("images/locked.gif");
  public static final ImageIcon unlockedIcon = GUtil.getImageIcon("images/unlocked.gif");
  public static final ImageIcon autoScrollOnIcon = GUtil.getImageIcon("images/asOn.gif");
  public static final ImageIcon autoScrollOffIcon = GUtil.getImageIcon("images/asOff.gif");
  public static final ImageIcon prevIcon = GUtil.getImageIcon("images/prev.gif");
  public static final ImageIcon nextIcon = GUtil.getImageIcon("images/next.gif");
  public static final ImageIcon saveIcon = GUtil.getImageIcon("images/save.png");
  public static final ImageIcon closeIcon = GUtil.getImageIcon("images/close.gif");
  public static final ImageIcon trashIcon = GUtil.getImageIcon("images/trash.png");
  public static final ImageIcon startLiveIcon = GUtil.getImageIcon("images/startLive.gif");
  public static final ImageIcon updateFiltersIcon = GUtil.getImageIcon("images/updateFilters.gif");
  public static final ImageIcon stopLiveIcon = GUtil.getImageIcon("images/stopLive.gif");
  public static final ImageIcon filtersIcon = GUtil.getImageIcon("images/filters.gif");
  public static final ImageIcon playEventsIcon = GUtil.getImageIcon("images/playEvents.gif");
  public static final ImageIcon stopEventsIcon = GUtil.getImageIcon("images/stopEvents.gif");
  public static final ImageIcon levelFilterIcon = GUtil.getImageIcon("images/levelFilter.gif");
  public static final ImageIcon sourceFilterIcon = GUtil.getImageIcon("images/sourceFilter.gif");
  public static final ImageIcon dateTimeFilterIcon = GUtil.getImageIcon("images/dateTimeFilter.gif");
  public static final ImageIcon textFilterIcon = GUtil.getImageIcon("images/textFilter.gif");
  public static final ImageIcon detailsIcon = GUtil.getImageIcon("images/details.png");
  public static final ImageIcon detailsOnIcon = GUtil.getImageIcon("images/detailsOn.png");
  public static final ImageIcon[] filteringIcons = new ImageIcon[] {
    GUtil.getImageIcon("images/filtering/filtering1.png"),
    GUtil.getImageIcon("images/filtering/filtering2.png")
  };
  public static final ImageIcon[] stagingIcons = new ImageIcon[] {
    GUtil.getImageIcon("images/processing/staging/staging1.png"),
    GUtil.getImageIcon("images/processing/staging/staging2.png"),
    GUtil.getImageIcon("images/processing/staging/staging3.png"),
    GUtil.getImageIcon("images/processing/staging/staging4.png"),
    GUtil.getImageIcon("images/processing/staging/staging5.png"),
    GUtil.getImageIcon("images/processing/staging/staging6.png"),
    GUtil.getImageIcon("images/processing/staging/staging7.png"),
    GUtil.getImageIcon("images/processing/staging/staging8.png")
  };
  public static final ImageIcon[] runningIcons = new ImageIcon[] {
    GUtil.getImageIcon("images/processing/running3/running1.png"),
    GUtil.getImageIcon("images/processing/running3/running2.png"),
    GUtil.getImageIcon("images/processing/running3/running3.png"),
    GUtil.getImageIcon("images/processing/running3/running4.png"),
    GUtil.getImageIcon("images/processing/running3/running5.png"),
    GUtil.getImageIcon("images/processing/running3/running6.png"),
    GUtil.getImageIcon("images/processing/running3/running7.png"),
    GUtil.getImageIcon("images/processing/running3/running8.png")
  };
  public static final ImageIcon[] warningIcons = new ImageIcon[] {
    GUtil.getImageIcon("images/processing/warning/warning1.png"),
    GUtil.getImageIcon("images/processing/warning/warning1.png"),
    GUtil.getImageIcon("images/processing/warning/warning2.png"),
    GUtil.getImageIcon("images/processing/warning/warning2.png"),
    GUtil.getImageIcon("images/processing/warning/warning1.png"),
    GUtil.getImageIcon("images/processing/warning/warning1.png"),
    GUtil.getImageIcon("images/processing/warning/warning2.png"),
    GUtil.getImageIcon("images/processing/warning/warning2.png")
  };
  public static final ImageIcon[] errorIcons = new ImageIcon[] {
    GUtil.getImageIcon("images/processing/error/error1.png"),
    GUtil.getImageIcon("images/processing/error/error1.png"),
    GUtil.getImageIcon("images/processing/error/error2.png"),
    GUtil.getImageIcon("images/processing/error/error2.png"),
    GUtil.getImageIcon("images/processing/error/error1.png"),
    GUtil.getImageIcon("images/processing/error/error1.png"),
    GUtil.getImageIcon("images/processing/error/error2.png"),
    GUtil.getImageIcon("images/processing/error/error2.png")
  };
  public static final ImageIcon[] successIcons = new ImageIcon[] {
    GUtil.getImageIcon("images/processing/success/success1.png"),
    GUtil.getImageIcon("images/processing/success/success1.png"),
    GUtil.getImageIcon("images/processing/success/success1.png"),
    GUtil.getImageIcon("images/processing/success/success1.png"),
    GUtil.getImageIcon("images/processing/success/success1.png"),
    GUtil.getImageIcon("images/processing/success/success1.png"),
    GUtil.getImageIcon("images/processing/success/success1.png"),
    GUtil.getImageIcon("images/processing/success/success1.png")
  };
    // New(ish) icons for ControlPanel
  public static final ImageIcon[] notInstalledIcons = new ImageIcon[] {
    GUtil.getImageIcon("images/processing/notInstalled/notInstalled.png"),
    GUtil.getImageIcon("images/processing/notInstalled/notInstalled.png"),
    GUtil.getImageIcon("images/processing/notInstalled/notInstalled.png"),
    GUtil.getImageIcon("images/processing/notInstalled/notInstalled.png"),
    GUtil.getImageIcon("images/processing/notInstalled/notInstalled.png"),
    GUtil.getImageIcon("images/processing/notInstalled/notInstalled.png"),
    GUtil.getImageIcon("images/processing/notInstalled/notInstalled.png"),
    GUtil.getImageIcon("images/processing/notInstalled/notInstalled.png")
  };
  public static final ImageIcon[] processOFFIcons = new ImageIcon[] {
    GUtil.getImageIcon("images/processing/processOFF/OFF.png"),
    GUtil.getImageIcon("images/processing/processOFF/OFF.png"),
    GUtil.getImageIcon("images/processing/processOFF/OFF.png"),
    GUtil.getImageIcon("images/processing/processOFF/OFF.png"),
    GUtil.getImageIcon("images/processing/processOFF/OFF.png"),
    GUtil.getImageIcon("images/processing/processOFF/OFF.png"),
    GUtil.getImageIcon("images/processing/processOFF/OFF.png"),
    GUtil.getImageIcon("images/processing/processOFF/OFF.png")
  };
  public static final ImageIcon[] processONIcons = new ImageIcon[] {
    GUtil.getImageIcon("images/processing/processON/ON.png"),
    GUtil.getImageIcon("images/processing/processON/ON.png"),
    GUtil.getImageIcon("images/processing/processON/ON.png"),
    GUtil.getImageIcon("images/processing/processON/ON.png"),
    GUtil.getImageIcon("images/processing/processON/ON.png"),
    GUtil.getImageIcon("images/processing/processON/ON.png"),
    GUtil.getImageIcon("images/processing/processON/ON.png"),
    GUtil.getImageIcon("images/processing/processON/ON.png")
  };
  public static final ImageIcon[] didNotRunIcons = new ImageIcon[] {
    GUtil.getImageIcon("images/processing/didNotRun/ERROR.png"),
    GUtil.getImageIcon("images/processing/didNotRun/ERROR.png"),
    GUtil.getImageIcon("images/processing/didNotRun/blank.png"),
    GUtil.getImageIcon("images/processing/didNotRun/blank.png"),
    GUtil.getImageIcon("images/processing/didNotRun/ERROR.png"),
    GUtil.getImageIcon("images/processing/didNotRun/ERROR.png"),
    GUtil.getImageIcon("images/processing/didNotRun/blank.png"),
    GUtil.getImageIcon("images/processing/didNotRun/blank.png")
  };
  public static final ImageIcon[] editEnabledIcons = new ImageIcon[] {
    GUtil.getImageIcon("images/processing/editEnabled/ENABLED.png"),
    GUtil.getImageIcon("images/processing/editEnabled/ENABLED.png"),
    GUtil.getImageIcon("images/processing/editEnabled/ENABLED.png"),
    GUtil.getImageIcon("images/processing/editEnabled/ENABLED.png"),
    GUtil.getImageIcon("images/processing/editEnabled/ENABLED.png"),
    GUtil.getImageIcon("images/processing/editEnabled/ENABLED.png"),
    GUtil.getImageIcon("images/processing/editEnabled/ENABLED.png"),
    GUtil.getImageIcon("images/processing/editEnabled/ENABLED.png")
  };
  public static final ImageIcon[] editDisabledIcons = new ImageIcon[] {
    GUtil.getImageIcon("images/processing/editDisabled/DISABLED.png"),
    GUtil.getImageIcon("images/processing/editDisabled/DISABLED.png"),
    GUtil.getImageIcon("images/processing/editDisabled/DISABLED.png"),
    GUtil.getImageIcon("images/processing/editDisabled/DISABLED.png"),
    GUtil.getImageIcon("images/processing/editDisabled/DISABLED.png"),
    GUtil.getImageIcon("images/processing/editDisabled/DISABLED.png"),
    GUtil.getImageIcon("images/processing/editDisabled/DISABLED.png"),
    GUtil.getImageIcon("images/processing/editDisabled/DISABLED.png")
  };
  public static final ImageIcon[] HuhIcons = new ImageIcon[] {
    GUtil.getImageIcon("images/processing/partialRun/HUH.png"),
    GUtil.getImageIcon("images/processing/partialRun/HUH.png"),
    GUtil.getImageIcon("images/processing/partialRun/blank.png"),
    GUtil.getImageIcon("images/processing/partialRun/blank.png"),
    GUtil.getImageIcon("images/processing/partialRun/HUH.png"),
    GUtil.getImageIcon("images/processing/partialRun/HUH.png"),
    GUtil.getImageIcon("images/processing/partialRun/blank.png"),
    GUtil.getImageIcon("images/processing/partialRun/blank.png")
  };
  public static final ImageIcon sinusoidalGrid = GUtil.getImageIcon("images/sinusoidal.gif");
  public static final Color DARK_GREEN = new Color(0,170,0);
  public static final Color LIGHT_BLUE = new Color(128,128,255);
  /****************************************************************************
  * buttonGroup.
  ****************************************************************************/
  public static ButtonGroup buttonGroup (AbstractButton b1, AbstractButton b2) {
    return buttonGroup(new AbstractButton[] {b1,b2});
  }
  public static ButtonGroup buttonGroup (AbstractButton b1, AbstractButton b2,
  					 AbstractButton b3) {
    return buttonGroup(new AbstractButton[] {b1,b2,b3});
  }
  public static ButtonGroup buttonGroup (AbstractButton b1, AbstractButton b2,
  					 AbstractButton b3, AbstractButton b4) {
    return buttonGroup(new AbstractButton[] {b1,b2,b3,b4});
  }
  public static ButtonGroup buttonGroup(AbstractButton[] buttons) {
    ButtonGroup group = new ButtonGroup();
    for (int i = 0; i < buttons.length; i++) {
       group.add(buttons[i]);
    }
    return group;
  }
  /****************************************************************************
  * getImageIcon.
  ****************************************************************************/
  public static ImageIcon getImageIcon (String path) {
    try {
      Object o = new Object();
      Class c = o.getClass();
      URL url = c.getResource("/"+path);
      return new ImageIcon(url);
    } catch (Exception e) {
    	
    }
    try {
      return new ImageIcon(path);
    } catch (Exception e) {
    	
    }
    return new ImageIcon();
  }
  /****************************************************************************
  * HGlue.
  ****************************************************************************/
  public static Component HGlue () {
    return Box.createHorizontalGlue();
  }
  /****************************************************************************
  * HSpace.
  ****************************************************************************/
  public static Component HSpace (int width) {
    return Box.createRigidArea(new Dimension(width,1));
  }
  /****************************************************************************
  * VGlue.
  ****************************************************************************/
  public static Component VGlue () {
    return Box.createVerticalGlue();
  }
  /****************************************************************************
  * VSpace.
  ****************************************************************************/
  public static Component VSpace (int height) {
    return Box.createRigidArea(new Dimension(1,height));
  }
  /****************************************************************************
  * configureWindow.
  ****************************************************************************/
  public static void configureWindow (Window window,
				      double pctOfWidth,
				      double pctOfHeight) {
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    int screenWidth = d.width;
    int screenHeight = d.height;
    int windowWidth = (int) Math.round(screenWidth * pctOfWidth);
    int windowHeight = (int) Math.round(screenHeight * pctOfHeight);
    window.setSize(windowWidth,windowHeight);
    window.setLocation((screenWidth - windowWidth) / 2,
		       (screenHeight - windowHeight) / 2);
  }
  public static void configureWindow (Window window, int width, int height) {
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    int screenWidth = d.width;
    int screenHeight = d.height;
    window.setSize(width,height);
    window.setLocation((screenWidth - width) / 2,
		       (screenHeight - height) / 2);
  }
  /****************************************************************************
  * centerWindow.
  ****************************************************************************/
  public static void centerWindow (Window window) {
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    int screenWidth = d.width;
    int screenHeight = d.height;
    int windowWidth = window.getWidth();
    int windowHeight = window.getHeight();
    window.setLocation((screenWidth - windowWidth) / 2,
		       (screenHeight - windowHeight) / 2);
  }
  /****************************************************************************
  * invokeAndWait.
  ****************************************************************************/
  public static void invokeAndWait (Runnable runnable) {
    if (SwingUtilities.isEventDispatchThread()) {
      runnable.run();
    } else {
      try {
        SwingUtilities.invokeAndWait(runnable);
      } catch (Exception e) {
    	
      }
    }
  }
  /****************************************************************************
  * UpdateLabel.
  ****************************************************************************/
  public static class UpdateLabel implements Runnable {
    private JLabel field;
    private String value;
    private Color color;
    public UpdateLabel (JLabel field, String value) {
      this.field = field;
      this.value = value;
      this.color = null;
    }
    public UpdateLabel (JLabel field, String value, Color color) {
      this.field = field;
      this.value = value;
      this.color = color;
    }
    public void run () {
      field.setText(value);
      if (color != null) {
      	field.setForeground(color);
      }
      field.repaint();
    }
  }
}
