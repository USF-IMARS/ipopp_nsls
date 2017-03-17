/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.nisgs.nsls.console;
import gov.nasa.gsfc.nisgs.nsls.console.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.html.HTMLEditorKit;
import java.util.TreeSet;
import java.util.Collection;

class InfoWindow extends javax.swing.JDialog
{
    private static final long serialVersionUID = 1L;

    /**
     * Creates and pops up a free-standing window containing formatted text.
     * Formatting is HTML - see DSM's ProductWindow for examples
     */
    InfoWindow(java.awt.Frame parent, String title, String text)
    {
        super(parent,title,false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationByPlatform(true);

        HTMLEditorKit editor = new HTMLEditorKit();
        JEditorPane ep = new JEditorPane();
        ep.setEditorKit(editor);

        ep.setText(text);
        //ep.setBorder(BorderFactory.createMatteBorder(16,16,16,16,java.awt.Color.gray));

        JPanel bottom = new JPanel(new FlowLayout());
        bottom.add(new Closer());

        java.awt.Container cp = getContentPane();
        cp.setLayout(new java.awt.BorderLayout());
        getContentPane().add(ep,BorderLayout.CENTER);
        cp.add(bottom,BorderLayout.SOUTH);
        pack();
    }

    private class Closer extends JButton implements ActionListener
    {
        /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Closer()
        {
            super("Close");
            setToolTipText("Close this window.");
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent event)
        {
            InfoWindow.this.dispose();
        }
    }
}
