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
import java.util.*;
import javax.swing.*;

public class WrapPanel extends JPanel {
  private Color textColor;
  private int minHeight;
  private Word[] words;
  private int borderSize;
  private static final long serialVersionUID = 1L;
  /****************************************************************************
  * WrapPanel.
  ****************************************************************************/
  public WrapPanel (String text) {
    construct(text,Color.black,0,0);
  }
  public WrapPanel (String text, int minRows) {
    construct(text,Color.black,minRows,0);
  }
  public WrapPanel (String text, Color textColor, int borderSize) {
    construct(text,textColor,0,borderSize);
  }
  private void construct (String text, Color textColor,
			  int minRows, int borderSize) {
    this.textColor = textColor;
    this.borderSize = borderSize;
    this.words = parseWords(text);
    setFont(new Font("SansSerif",Font.PLAIN,12));
    minHeight = borderSize + (minRows * getFontMetrics(getFont()).getHeight()) + borderSize;
    if (minHeight > 0) {
      setMinimumSize(new Dimension(getMinimumSize().width,minHeight));
      setPreferredSize(new Dimension(getPreferredSize().width,minHeight));
      setMaximumSize(new Dimension(getMaximumSize().width,minHeight));
    }
  }
  private Word[] parseWords (String text) {
    StringTokenizer byBlank = new StringTokenizer(text," ");
    Word[] words = new Word[byBlank.countTokens()];
    for (int i = 0; i < words.length; i++) {
       String word = byBlank.nextToken();
       words[i] = new Word(word);
    }
    return words;
  }
  /****************************************************************************
  * Word.
  ****************************************************************************/
  private class Word {
    private String word;
    public Word (String word) {
      this.word = word;
    }
    public String getWord () {
      return word;
    }
  }
  /****************************************************************************
  * setText.
  ****************************************************************************/
  public void setText (String text) {
    words = parseWords(text);
    revalidate();
    repaint();
  }
  /****************************************************************************
  * paint.
  ****************************************************************************/
  public void paint (Graphics g) {
    Dimension dims = getSize();
    int availableWidth = dims.width - (2 * borderSize);
    g.setColor(getParent().getBackground());
    g.fillRect(0,0,dims.width,dims.height);
    FontMetrics metrics = g.getFontMetrics(g.getFont());
    int spaceWidth = metrics.stringWidth(" ");
    g.setColor(textColor);
    int x = borderSize;
    int y = borderSize + metrics.getAscent();
    for (int i = 0; i < words.length; i++) {
       Word word = words[i];
       int wordWidth = metrics.stringWidth(word.getWord());
       if (wordWidth > availableWidth) {
         // Just in case the current position would only allow a space to fit...
         if (x + spaceWidth > dims.width - borderSize) {
	   x = borderSize;
	   y += metrics.getHeight();
         }
         // Break the word into pieces...
	 String s = word.getWord();
	 while (s.length() > 0) {
	   // Going all the way down to a zero-length substring handles the case where the space fits but not the first character...
	   for (int j = s.length(); j >= 0; j--) {
	      String ss = s.substring(0,j);
	      int ssWidth = metrics.stringWidth(ss);
	      if (x + spaceWidth + ssWidth <= dims.width - borderSize) {
	        x += spaceWidth;
	        g.drawString(ss,x,y);
	        s = s.substring(j);
	        if (s.length() > 0) {
		  x = borderSize;
		  y += metrics.getHeight();
	        } else {
	          x += ssWidth;
	        }
	        break;
	      }
	   }
	 }
       } else {
         if (x + spaceWidth + wordWidth > dims.width - borderSize) {
	   x = borderSize;
	   y += metrics.getHeight();
         }
         x += spaceWidth;
         g.drawString(word.getWord(),x,y);
         x += wordWidth;
       }
    }
    int neededHeight = Util.max(minHeight,y + metrics.getDescent() + borderSize);
    if (neededHeight != dims.height) {
      SwingUtilities.invokeLater(new AdjustHeight(neededHeight));
    }
  }
  /****************************************************************************
  * AdjustHeight.
  ****************************************************************************/
  private class AdjustHeight implements Runnable {
    private int newHeight;
    public AdjustHeight (int newHeight) {
      this.newHeight = newHeight;
    }
    public void run () {
      setMinimumSize(new Dimension(getMinimumSize().width,newHeight));
      setPreferredSize(new Dimension(getPreferredSize().width,newHeight));
      setMaximumSize(new Dimension(getMaximumSize().width,newHeight));
      revalidate();
      repaint();
    }
  }
}
