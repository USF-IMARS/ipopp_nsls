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
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class FilterManager extends JPanel {
  private CountFilterHandler countHandler;
  private HPanel filterButtonsPanel;
  private HPanel filterHandlersPanel;
  private static final long serialVersionUID = 1L;
  /****************************************************************************
  * FilterManager.
  ****************************************************************************/
  public FilterManager () {
    setLayout(new BorderLayout());
    countHandler = new CountFilterHandler();
    CustomButton levelFilterButton = new CustomButton(GUtil.levelFilterIcon);
    levelFilterButton.addActionListener(new LevelFilterButtonClicked());
    levelFilterButton.setToolTipText("Display Level Filter");
    CustomButton sourceFilterButton = new CustomButton(GUtil.sourceFilterIcon);
    sourceFilterButton.addActionListener(new SourceFilterButtonClicked());
    sourceFilterButton.setToolTipText("Display Source Filter");
    CustomButton dateTimeFilterButton = new CustomButton(GUtil.dateTimeFilterIcon);
    dateTimeFilterButton.addActionListener(new DateTimeFilterButtonClicked());
    dateTimeFilterButton.setToolTipText("Display Date/Time Filter");
    CustomButton textFilterButton = new CustomButton(GUtil.textFilterIcon);
    textFilterButton.addActionListener(new TextFilterButtonClicked());
    textFilterButton.setToolTipText("Display Text Filter");
    JButton closeButton = new JButton(GUtil.closeIcon);
    closeButton.setMargin(new Insets(0,0,0,0));
    closeButton.addActionListener(new CloseButtonClicked());
    closeButton.setToolTipText("Close Filter Panel (DOES NOT DELETE THE FILTERS)");
    filterButtonsPanel = new HPanel(levelFilterButton,sourceFilterButton,dateTimeFilterButton,textFilterButton);
    add(new VPanel(GUtil.VSpace(5),new HPanel(GUtil.HSpace(2),filterButtonsPanel,GUtil.HGlue(),closeButton,GUtil.HSpace(2)),GUtil.VSpace(2)),BorderLayout.NORTH);
    filterHandlersPanel = new HPanel();
    HPanel h1 = new HPanel(countHandler,filterHandlersPanel);
    // h1.setBorder(new LineBorder(Color.gray,1));
    add(new JScrollPane(h1),BorderLayout.CENTER);
  }
  /****************************************************************************
  * getMaxCount.
  ****************************************************************************/
  public int getMaxCount () {
    return countHandler.getCount();
  }
  /****************************************************************************
  * getFilterCount.
  ****************************************************************************/
  public int getFilterCount () {
    return filterHandlersPanel.getComponentCount();
  }
  /****************************************************************************
  * getFilters.
  ****************************************************************************/
  public Filters getFilters () {
    Filters filters = new Filters();
    Component[] components = filterHandlersPanel.getComponents();
    for (int i = 0; i < components.length; i++) {
       FilterHandler handler = (FilterHandler) components[i];
       Filter filter = handler.getFilter();
       if (filter != null) {
       	 filters.addFilter(filter);
       }
    }
    return filters;
  }
  /****************************************************************************
  * deleteFilter.
  ****************************************************************************/
  public void deleteFilter (FilterHandler handler) {
    filterHandlersPanel.remove(handler);
    revalidate();
    repaint();
  }
  /****************************************************************************
  * LevelFilterButtonClicked.
  ****************************************************************************/
  private class LevelFilterButtonClicked implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      int AddOrNo = 0;

      Component[] components = filterHandlersPanel.getComponents();
      for (int i = 0; i < components.length; i++)
      {
        FilterHandler handler = (FilterHandler) components[i];
        
        if (handler.getClass().getName().contains("LevelFilterHandler"))
        {
          AddOrNo = 1;
        }
      }

      if (AddOrNo == 0)
      {
        addFilter(new LevelFilterHandler());
      }
    }
  }
  /****************************************************************************
  * SourceFilterButtonClicked.
  ****************************************************************************/
  private class SourceFilterButtonClicked implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      int AddOrNo = 0;

      Component[] components = filterHandlersPanel.getComponents();
      for (int i = 0; i < components.length; i++)
      {
        FilterHandler handler = (FilterHandler) components[i];
        
        if (handler.getClass().getName().contains("SourceFilterHandler"))
        {
          AddOrNo = 1;
        }
      }

      if (AddOrNo == 0)
      {
        addFilter(new SourceFilterHandler());
      }
    }
  }
  /****************************************************************************
  * DateTimeFilterButtonClicked.
  ****************************************************************************/
  private class DateTimeFilterButtonClicked implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      int AddOrNo = 0;

      Component[] components = filterHandlersPanel.getComponents();
      for (int i = 0; i < components.length; i++)
      {
        FilterHandler handler = (FilterHandler) components[i];
        
        if (handler.getClass().getName().contains("DateTimeFilterHandler"))
        {
          AddOrNo = 1;
        }
      }

      if (AddOrNo == 0)
      {
        addFilter(new DateTimeFilterHandler());
      }
    }
  }
  /****************************************************************************
  * TextFilterButtonClicked.
  ****************************************************************************/
  private class TextFilterButtonClicked implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      addFilter(new TextFilterHandler());
    }
  }
  /****************************************************************************
  * addFilter.
  ****************************************************************************/
  private void addFilter (FilterHandler handler) {
    filterHandlersPanel.add(handler);
    revalidate();
    repaint();
  }
  /****************************************************************************
  * CloseButtonClicked.
  ****************************************************************************/
  private class CloseButtonClicked implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      Console.serverHandler.eventTable.closeFilters();
    }
  }
}
